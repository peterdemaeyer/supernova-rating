package su.pernova.rating;

/**
 * Combines ratings of all players by taking the average of the player's ratings.
 */
public class AverageRatingCombiner implements RatingCombiner {

	private static final long serialVersionUID = 1L;

	@Override
	public double combineRatings(final Player... players) {
		double sumOfRatings = 0.;
		for (final Player player : players) {
			sumOfRatings += player.rating;
		}
		return sumOfRatings / players.length;
	}
}
