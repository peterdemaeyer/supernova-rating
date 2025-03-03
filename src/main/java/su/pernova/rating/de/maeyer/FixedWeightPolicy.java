package su.pernova.rating.de.maeyer;

import java.io.Serial;

import su.pernova.rating.Player;

/**
 * This policy uses a fixed weight, regardless of any parameters such as match count.
 */
public class FixedWeightPolicy implements WeightPolicy {

	@Serial
	private static final long serialVersionUID = 1L;

	private final double weight;

	public FixedWeightPolicy(final double weight) {
		this.weight = weight;
	}

	@Override
	public double weight(final Player player) {
		return weight;
	}
}
