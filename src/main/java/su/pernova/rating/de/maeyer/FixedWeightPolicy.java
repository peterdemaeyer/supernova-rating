package su.pernova.rating.de.maeyer;

import su.pernova.rating.Player;

/**
 * This policy uses a fixed weight, regardless of any parameters such as match count.
 */
public class FixedWeightPolicy implements WeightPolicy {

	private final double weight;

	public FixedWeightPolicy(final double weight) {
		this.weight = weight;
	}

	@Override
	public double weight(final Player player) {
		return weight;
	}
}
