package su.pernova.rating.de.maeyer;

import static java.util.Arrays.fill;

public class AverageRatingSplitter implements RatingSplitter {

	private static final long serialVersionUID = 1L;

	@Override
	public double[] split(double combinedWeighedRating, TeamContext teamContext) {
		final PlayerContext[] playerContexts = teamContext.playerContexts;
		final int playerCount = playerContexts.length;
		final double[] splitWeighedRatings = new double[playerCount];
		fill(splitWeighedRatings, combinedWeighedRating / playerCount);
		return splitWeighedRatings;
	}
}
