package su.pernova.rating.elo;

import static java.lang.Double.isNaN;
import static java.lang.Math.pow;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.concat;

import su.pernova.rating.AverageRatingCombiner;
import su.pernova.rating.Match;
import su.pernova.rating.RatingCombiner;
import su.pernova.rating.RatingSystem;
import su.pernova.rating.Set;

/**
 * This class implements the Elo rating system for zero-sum games for two teams,
 * scores in any range, and any number of players per team.
 * Elo's rating system is designed for chess, where the score are binary:
 * 1.0 for a win, 0.5 for  a tie, and 0.0 for a loss.
 * It is also designed for one-person teams.
 * This system has some changes to make the system suitable for padel or foosball,
 * where there are two teams of two people, and scores are in a range 0-N.
 */
public class EloRatingSystem implements RatingSystem {

	private static final long serialVersionUID = 1L;

	public final RatingCombiner ratingCombinerAlgorithm;

	public final double initialRating;

	public final double base;

	public final double scale;

	public final double kFactor;

	/**
	 * @param ratingCombinerAlgorithm a rating combination algorithm that combines ratings of multiple players of a team, which must not be {@code null}.
	 * @param initialRating the initial rating for a player that has never played.
	 * @param base the exponential base separating levels, 10 in Elo's standard chess algorithm.
	 * @param scale the logarithmic increase per level as given by base, 400 in Elo's standard chess algorithm.
	 * @param kFactor the weight of the last match compared to all the ones before, in the range of 20-40 in Elo's standard chess algorithm (depending on the rating).
	 */
	public EloRatingSystem(RatingCombiner ratingCombinerAlgorithm, double initialRating, double base, double scale, double kFactor) {
		this.ratingCombinerAlgorithm = requireNonNull(ratingCombinerAlgorithm, "rating combination algorithm is null");
		this.initialRating = initialRating;
		this.base = base;
		this.scale = scale;
		this.kFactor = kFactor;
	}

	public EloRatingSystem() {
		this(new AverageRatingCombiner(), 1500., 10., 400., 32.);
	}

	@Override
	public void apply(Match match) {
		final double[] actualScores = actualScores(match);
		final double s1 = actualScores[0];
		final double s2 = actualScores[1];
		if (isNaN(s1)) {
			return;
		}
		final double[] expectedScores = expectedScores(match);
		final double e1 = expectedScores[0];
		final double e2 = expectedScores[1];
		stream(match.teams[0].players)
				.forEach(player -> {
					player.rating += kFactor * (s1 - e1);
					player.matchCount++; });
		stream(match.teams[1].players)
				.forEach(player -> {
					player.rating += kFactor * (s2 - e2);
					player.matchCount++; });
	}

	private static double[] actualScores(Match match) {
		long s1s2 = 0L;
		long s1 = 0L;
		long s2 = 0L;
		for (Set set: match.sets) {
			s1s2 += set.score[0] + set.score[1];
			s1 += set.score[0];
			s2 += set.score[1];
		}
		// Normalize the scores.
		// A score of n-0 results in 1.0, a score of 0-n in 0.0.
		return new double[] {
				(double) s1 / s1s2,
				(double) s2 / s1s2
		};
	}

	private double[] expectedScores(Match match) {
		concat(stream(match.teams[0].players), stream(match.teams[1].players))
				.filter(player -> player.matchCount == 0L)
				.forEach(player -> player.rating = initialRating);
		final double r1 = ratingCombinerAlgorithm.combineRatings(match.teams[0].players);
		final double r2 = ratingCombinerAlgorithm.combineRatings(match.teams[1].players);
		final double q1 = pow(base, r1 / scale);
		final double q2 = pow(base, r2 / scale);
		final double q1q2 = q1 + q2;
		return new double[] { q1 / q1q2, q2 / q1q2 };
	}
}
