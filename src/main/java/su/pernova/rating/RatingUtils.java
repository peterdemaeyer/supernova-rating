package su.pernova.rating;

import static java.lang.System.arraycopy;
import static java.util.Objects.requireNonNull;

public final class RatingUtils {

	private RatingUtils() {
	}

	public static double sumOfRatings(final Player... players) {
		double sumOfRatings = 0.;
		for (final Player player : players) {
			sumOfRatings += player.rating;
		}
		return sumOfRatings;
	}

	public static double[] padWeights(final double... weights) {
		final double sumOfWeights = sumOfWeights(requireNonNull(weights, "array of weights is null"));
		if (sumOfWeights > 1.) {
			throw new IllegalArgumentException("sum of weights > 1.0: " + sumOfWeights);
		}
		if (sumOfWeights == 1.) {
			return weights;
		} else {
			final double[] paddedWeights = new double[weights.length + 1];
			arraycopy(weights, 0, paddedWeights, 0, weights.length);
			paddedWeights[weights.length] = 1. - sumOfWeights;
			return paddedWeights;
		}
	}

	public static double sumOfWeights(double... weights) {
		double sumOfWeights = 0.;
		for (double weight : weights) {
			if (weight < 0.) {
				throw new IllegalArgumentException("weight < 0.0: " + weight);
			}
			if (weight > 1.) {
				throw new IllegalArgumentException("weight > 1.0: " + weight);
			}
			sumOfWeights += weight;
		}
		return sumOfWeights;
	}
}
