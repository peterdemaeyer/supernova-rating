package su.pernova.rating.de.maeyer;

import static java.util.Objects.requireNonNull;

import java.io.Serial;

import su.pernova.rating.Player;

/**
 * This weight policy takes the maximum of the weights of multiple policies.
 */
public class MaxWeightPolicy implements WeightPolicy {

	@Serial
	private static final long serialVersionUID = 1L;

	private final WeightPolicy[] weightPolicies;

	public MaxWeightPolicy(final WeightPolicy... weightPolicies) {
		this.weightPolicies = requireNonNull(weightPolicies, "array of weight policies is null");
	}

	@Override
	public double weight(final Player player) {
		double maxWeight = 0.;
		for (WeightPolicy weightPolicy : weightPolicies) {
			final double weight = weightPolicy.weight(player);
			if (weight > maxWeight) {
				maxWeight = weight;
			}
		}
		return maxWeight;
	}
}
