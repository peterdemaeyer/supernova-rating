package su.pernova.rating;

import static java.lang.Double.isNaN;
import static java.lang.Math.sqrt;

public class SqrtAverageRatingCombiner implements RatingCombiner {

	@Override
	public double combineRatings(Player... players) {
		double sumOfSqrtsOfRatings = 0.;
		for (Player player : players) {
			final double sqrt = sqrt(player.rating);
			if (!isNaN(sqrt)) {
				sumOfSqrtsOfRatings += sqrt;
			}
		}
		return sqr(sumOfSqrtsOfRatings / players.length);
	}

	private static double sqr(double v) {
		return v * v;
	}
}
