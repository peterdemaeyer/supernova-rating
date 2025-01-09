package su.pernova.rating.de.maeyer;

import su.pernova.rating.Player;

/**
 * This policy weighs the current match as much as the previous matches, effectively averaging out the scores.
 */
public class AverageWeightPolicy implements WeightPolicy {

	private static final long serialVersionUID = 1L;

	@Override
	public double weight(final Player player) {
		return 1. / (player.matchCount + 1L);
	}
}
