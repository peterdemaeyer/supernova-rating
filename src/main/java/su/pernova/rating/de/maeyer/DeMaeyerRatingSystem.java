package su.pernova.rating.de.maeyer;

import static java.lang.Double.isNaN;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

import su.pernova.rating.AverageRatingCombiner;
import su.pernova.rating.Match;
import su.pernova.rating.Player;
import su.pernova.rating.RatingCombiner;
import su.pernova.rating.RatingSystem;
import su.pernova.rating.Set;
import su.pernova.rating.Team;

public class DeMaeyerRatingSystem implements RatingSystem {

	private static final long serialVersionUID = 1L;

	private final RatingCombiner ratingCombiner;

	private final RatingSplitter ratingSplitter;

	private final WeightPolicy weightPolicy;

	private final double absorptionFactor;

	private final double newPlayerRating;

	private final double newPlayerPooledRating;

	public double systemPooledRating;

	/**
	 * @param ratingCombiner a rating combiner, not {@code null}.
	 * A rating combiner combines the ratings of all players of a team into one rating.
	 * @param ratingSplitter a rating splitter, not {@code null}.
	 * A rating splitter splits the rating ante across all players of a team.
	 * @param weightPolicy a policy returning the weight of the last match compared to all previous matches.
	 */
	private DeMaeyerRatingSystem(RatingCombiner ratingCombiner, RatingSplitter ratingSplitter,
								 WeightPolicy weightPolicy, double absorptionFactor, double newPlayerRating, double newPlayerPooledRating,
								 double systemPooledRating) {
		this.ratingCombiner = requireNonNull(ratingCombiner, "rating combiner is null");
		this.ratingSplitter = requireNonNull(ratingSplitter, "rating splitter is null");
		this.weightPolicy = requireNonNull(weightPolicy, "weight policy is null");
		this.absorptionFactor = absorptionFactor;
		this.newPlayerRating = newPlayerRating;
		this.newPlayerPooledRating = newPlayerPooledRating;
		this.systemPooledRating = systemPooledRating;
	}

	static class ActualGameRatioResult {

		final double actualGameRatio;

		/**
		 * Maximum score, sum of maximum scores of all sets.
		 */
		final long maxGames;

		final double minExpectedGameRatio;

		final double maxExpectedGameRatio;

		ActualGameRatioResult(final double actualGameRatio, final long maxGames) {
			this.actualGameRatio = actualGameRatio;
			this.maxGames = maxGames;
			minExpectedGameRatio = 1. / maxGames;
			maxExpectedGameRatio = maxGames;
		}
	}

	@Override
	public void apply(final Match match) {
		// Calculate the actual score ratio relative to team 1.
		// The relative to team 2 is the inverse, we don't need to calculate it.
		final ActualGameRatioResult r = computeActualGameRatio(match);
		if (isNaN(r.actualGameRatio)) {
			// A score of 0-0 behaves as if it never happened.
			// The ratings do not change and the match count does not increase.
			return;
		}
		// The rating is a measure for the probability that a team wins a game.
		// Calculate the expected score ratio relative to team 2, based on the ratings.
		// The relative to team 2 is the inverse, we don't need to calculate it.
		final double expectedGameRatio = computeExpectedGameRatio(match);
		// Given the rating update newRating = (1 - weight) * oldRating + weight * ratingUpdate,
		// and given that the total pool of rating in the system must remain constant (no inflation nor deflation),
		// the total ante to be split across all players is the sum of all player's individual antes: weight * oldRating.
		// The weight is a constant factor, so we can modulo that and the (unweighted) ante becomes the sum of all player's ratings.
		final TeamContext teamContext1 = new TeamContext(match.teams[0]);
		final TeamContext teamContext2 = new TeamContext(match.teams[1]);
		sumOfWeighedRatings(teamContext1);
		sumOfWeighedRatings(teamContext2);
		// Total ante = team 1 ante + team 2 ante.
		final double weighedAnte1 = teamContext1.sumOfWeighedRatings;
		final double weighedAnte2 = teamContext2.sumOfWeighedRatings;
		double weighedAnte = weighedAnte1 + weighedAnte2;
		// Calculate the win ratio relative to team 1.
		// It is a measure of "how well" team 1 actually performed against team 2 compared to expectations.
		final double winRating1 = ((expectedGameRatio < r.minExpectedGameRatio && r.actualGameRatio < r.minExpectedGameRatio)
				|| (expectedGameRatio > r.maxExpectedGameRatio && r.actualGameRatio > r.maxExpectedGameRatio)) ?
			computeWinRating1(1., 1., weighedAnte1, weighedAnte2, weighedAnte) :
			computeWinRating1(r.actualGameRatio, expectedGameRatio, weighedAnte1, weighedAnte2, weighedAnte);
		// weighedAnte will be potentially increased with pool ante, so we compute the winWeight1 BEFORE that.
		final double winWeight1 = winRating1 / weighedAnte;
		synchronized (this) {
			if (systemPooledRating > 0.) {
				final double poolAnte = min(absorptionFactor * weighedAnte, systemPooledRating);
				weighedAnte += poolAnte;
//				System.out.println("Decrementing rating pool excess: " + ratingPoolExcess + " with: " + poolAnte);
				systemPooledRating -= poolAnte;
			} else if (systemPooledRating < 0.) {
				throw new IllegalStateException("rating pool deficit is not yet implemented");
			}
		}
		// Now this total ante needs to be split across all players according to the win ratio (relative to team 1).
		// The win ratio is team 1's actual score compared to their expected score.
		// Remember that team 2's win ratio is the inverse of team 1's.
		// The win weight is a team's fraction of the total ante.
		final double combinedWeighedRating1 = winWeight1 * weighedAnte;
		final double combinedWeighedRating2 = (1. - winWeight1) * weighedAnte;
		final double[] newWeighedRatings1 = ratingSplitter.split(combinedWeighedRating1, teamContext1);
		final double[] newWeighedRatings2 = ratingSplitter.split(combinedWeighedRating2, teamContext2);
		updateRatings(teamContext1, newWeighedRatings1);
		updateRatings(teamContext2, newWeighedRatings2);
		updateMatchCounts(match);
	}

	private void sumOfWeighedRatings(final TeamContext teamContext) {
		for (final PlayerContext playerContext : teamContext.playerContexts) {
			playerContext.weight = weightPolicy.weight(playerContext.player);
			teamContext.sumOfWeights += playerContext.weight;
			playerContext.weighedRating = playerContext.weight * playerContext.player.rating;
			teamContext.sumOfWeighedRatings += playerContext.weighedRating;
		}
	}

	private void updateRatings(final TeamContext teamContext, final double[] newWeighedRatings) {
		final int playerCount = teamContext.playerContexts.length;
		for (int playerIndex = 0; playerIndex != playerCount; playerIndex++) {
			final PlayerContext playerContext = teamContext.playerContexts[playerIndex];
			final Player player = playerContext.player;
			final double weight = playerContext.weight;
			player.rating = (1. - weight) * player.rating + newWeighedRatings[playerIndex];
		}
	}

	private static void updateMatchCounts(final Match match) {
		for (final Team team : match.teams) {
			for (final Player player : team.players) {
				player.matchCount++;
				player.lastMatchTime = match.time;
			}
		}
	}

	private static ActualGameRatioResult computeActualGameRatio(final Match match) {
		long games1 = 0L;
		long games2 = 0L;
		for (final Set set : match.sets) {
			games1 += set.score[0];
			games2 += set.score[1];
		}
		final long maxGames = max(games1, games2);
		// Clip the game ratio to avoid 0 and +Infinity ratios.
		final double maxGameRatio = maxGames + 1L;
		final double minGameRatio = 1. / maxGameRatio;
		final double actualGameRatio = min(max((double) games1 / games2, minGameRatio), maxGameRatio);
		return new ActualGameRatioResult(actualGameRatio, maxGames);
	}

	private double computeExpectedGameRatio(final Match match) {
		for (final Team team : match.teams) {
			for (final Player player : team.players) {
				if (player.matchCount == 0L) {
					// Unrated player -> compute initial rating from team combined ratings.
					player.rating = newPlayerRating;
					synchronized (this) {
						systemPooledRating += newPlayerPooledRating;
					}
				}
			}
		}
		return ratingCombiner.combineRatings(match.teams[0].players) / ratingCombiner.combineRatings(match.teams[1].players);
	}

	private static double computeWinRating1(double actualGameRatio1, double expectedGameRatio1, double sumOfWeighedRatings1, double sumOfWeighedRatings2, double sumOfWeighedRatings) {
		if (actualGameRatio1 == expectedGameRatio1) {
			// Break even.
			return sumOfWeighedRatings1;
		}
		// Whichever team wins, this limits the loss of the other team to the actual game ratio.
		final double ratingLimit1 = computeRatingLimit1(actualGameRatio1, sumOfWeighedRatings);
		if (actualGameRatio1 < expectedGameRatio1) {
			// Team 2 wins.
			if (ratingLimit1 > sumOfWeighedRatings1) {
				// Contradictory situation: team 2 loses weight even though they did better than expected.
				// Assume it can't happen.
				throw new IllegalArgumentException("losing rating limit: " + ratingLimit1 + " > sum of weighed ratings 1: " + sumOfWeighedRatings1);
			}
			return ratingLimit1 + actualGameRatio1 / expectedGameRatio1 * (sumOfWeighedRatings1 - ratingLimit1);
		}
		// Team 1 wins.
		if (ratingLimit1 < sumOfWeighedRatings1) {
			// Contradictory situation: team 1 loses weight even though they did better than expected.
			// Assume it can't happen.
			throw new IllegalArgumentException("winning rating limit: " + ratingLimit1 + " < sum of weighed ratings 1: " + sumOfWeighedRatings1);
		}
		return sumOfWeighedRatings - expectedGameRatio1 / actualGameRatio1 * (sumOfWeighedRatings2 - ratingLimit1);
	}

	private static double computeRatingLimit1(final double gameRatio1, final double sumOfWeighedRatings) {
		if (gameRatio1 <= 1.) {
			return 1. / (1. / gameRatio1 + 1.) * sumOfWeighedRatings;
		}
		return gameRatio1 / (gameRatio1 + 1.) * sumOfWeighedRatings;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[ratingPoolExcess=" + systemPooledRating + "]";
	}

	public static class Builder {

		private RatingCombiner ratingCombiner = new AverageRatingCombiner();

		private RatingSplitter ratingSplitter = new WeighedRatingSplitter();

		private double systemPooledRating;

		private WeightPolicy weightPolicy = new MaxWeightPolicy(new AverageWeightPolicy(), new FixedWeightPolicy(.2));

		private double newPlayerRating = 50.;

		private double newPlayerPooledRating = 150.;

		private double absorptionFactor = .05;

		public Builder() {
		}

		public Builder setRatingCombiner(final RatingCombiner ratingCombiner) {
			this.ratingCombiner = requireNonNull(ratingCombiner, "rating combiner is null");
			return this;
		}

		public Builder setRatingSplitter(final RatingSplitter ratingSplitter) {
			this.ratingSplitter = requireNonNull(ratingSplitter, "rating splitter is null");
			return this;
		}

		/**
		 * Sets the initial rating for new players, 50 by default.
		 *
		 * @param rating the initial rating for new players.
		 * @return this builder.
		 */
		public Builder setNewPlayerRating(final double rating) {
			this.newPlayerRating = rating;
			return this;
		}

		/**
		 * Sets the initial rating excess for new players, 150 by default.
		 * The initial rating excess for new players goes hand in hand with initial rating.
		 * The initial rating excess is an additional contribution to the rating system's rating pool, on top of the
		 * initial rating.
		 *
		 * @param rating the initial rating pool contribution for new players.
		 * @return this builder.
		 */
		public Builder setNewPlayerPooledRating(final double rating) {
			this.newPlayerPooledRating = rating;
			return this;
		}

		/**
		 * Sets the initial rating pool for the rating system, which is 0 by default.
		 * The rating pool is the excess of rating in the whole system, which may be negative.
		 * This should not be set in normal circumstances, unless for deserialization purposes.
		 *
		 * @param rating an initial rating pool for the rating system.
		 * @return this builder.
		 */
		public Builder setSystemPooledRating(final double rating) {
			this.systemPooledRating = rating;
			return this;
		}

		/**
		 * Sets the weight of the rating of the last match compared to all the previous matches.
		 */
		public Builder setWeightPolicy(final WeightPolicy weightPolicy) {
			this.weightPolicy = requireNonNull(weightPolicy, "weight policy is null");
			return this;
		}

		/**
		 * Sets the absorption factor of rating pool excess, 0.05 (5 %) by default.
		 * The absorption factor must be a decimal number in the range [0.0, 1.0].
		 *
		 * @param absorptionFactor an absorption factor of rating pool excess in the range [0.0, 1.0].
		 * @return this builder.
		 */
		public Builder setAbsorptionFactor(final double absorptionFactor) {
			this.absorptionFactor = absorptionFactor;
			return this;
		}

		public DeMaeyerRatingSystem build() {
			return new DeMaeyerRatingSystem(ratingCombiner, ratingSplitter, weightPolicy, absorptionFactor,
					newPlayerRating, newPlayerPooledRating, systemPooledRating);
		}
	}
}
