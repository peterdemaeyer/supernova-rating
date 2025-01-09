package su.pernova.rating.de.maeyer;

import java.io.Serializable;

/**
 * Splits the win (or loss) across players in a team according to the player's weights in the ante.
 * Weights are relative to each player's rating, so they do not say much about the absolute value of the ante, but
 * rather how much a player has anted relative to their own rating.
 * <p/>
 * In a typical situation where the weight policy is a {@link MaxWeightPolicy} combining an {@link AverageWeightPolicy}
 * with a {@link FixedWeightPolicy}, the weight will be higher for novice players, and will flat out afterward.
 * This means ratings will fluctuate more for novice players, which corresponds to intuition.
 */
public class WeighedRatingSplitter implements RatingSplitter {

	private static final long serialVersionUID = 1L;

	@Override
	public double[] split(final double combinedWeighedRating, final TeamContext teamContext) {
		final PlayerContext[] playerContexts = teamContext.playerContexts;
		final int playerCount = playerContexts.length;
		final double[] splitWeighedRatings = new double[playerCount];
		final double combinedWeighedWin = combinedWeighedRating - teamContext.sumOfWeighedRatings;
		for (int playerIndex = 0; playerIndex != playerCount; playerIndex++) {
			final PlayerContext playerContext = playerContexts[playerIndex];
			splitWeighedRatings[playerIndex] = playerContext.weighedRating + playerContext.weight / teamContext.sumOfWeights * combinedWeighedWin;
		}
		if (combinedWeighedRating < teamContext.sumOfWeighedRatings) {
			while (true) {
				double remainingCombinedWeighedRating = 0.;
				double remainingSumOfWeights = 0.;
				for (int playerIndex = 0; playerIndex != playerCount; playerIndex++) {
					double splitWeighedRating = splitWeighedRatings[playerIndex];
					if (splitWeighedRating <= 0.) {
						splitWeighedRatings[playerIndex] = 0.;
						remainingCombinedWeighedRating += splitWeighedRating;
					} else {
						remainingSumOfWeights += playerContexts[playerIndex].weight;
					}
				}
				if (remainingCombinedWeighedRating == 0.) {
					break;
				}
				for (int playerIndex = 0; playerIndex != playerCount; playerIndex++) {
					double splitWeighedRating = splitWeighedRatings[playerIndex];
					if (splitWeighedRating > 0.) {
						splitWeighedRatings[playerIndex] += playerContexts[playerIndex].weight / remainingSumOfWeights * remainingCombinedWeighedRating;
					}
				}
			}
		}
		return splitWeighedRatings;
	}
}
