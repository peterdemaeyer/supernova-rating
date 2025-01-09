package su.pernova.rating;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;

import static su.pernova.rating.RatingUtils.padWeights;

import java.util.List;

/**
 * Combines ratings by taking the <i>weighted</i> average of the player's ratings.
 * Weights are decimal numbers in the range [0.0, 1.0] and apply from weakest to strongest player.
 * The # weights may be one less than the # players, in which case the strongest player's weight is computed as 1.0
 * minus the sum of the weights of the other players.
 * For example, in a 2-player team game, a weight of 0.7 means that the combined rating is 70 % of the weakest player's
 * rating plus 30 % of the strongest player's rating.
 * This "weakest link" model allows to compensate for a weaker player being the weakest link in the team.
 */
public class WeighedAverageRatingCombiner implements RatingCombiner {

	private final double[] weights;

	/**
	 * @param weights array of weights in order of weakest to strongest player, not {@code null}.
	 * The array of weights may be one less in size than the number of players,
	 * in which case the weight of the strongest player will be 1 - &lt;sum of weights of weaker players&gt;.
	 */
	public WeighedAverageRatingCombiner(double... weights) {
		this.weights = padWeights(weights);
	}

	@Override
	public double combineRatings(Player... players) {
		final int playerCount = weights.length;
		final List<Player> sortedPlayers = stream(players).sorted(comparingDouble(player -> player.rating)).collect(toList());
		double combinedRating = 0.;
		for (int sortedPlayerIndex = 0; sortedPlayerIndex != playerCount; sortedPlayerIndex++) {
			combinedRating += weights[sortedPlayerIndex] * sortedPlayers.get(sortedPlayerIndex).rating;
		}
		return combinedRating;
	}
}
