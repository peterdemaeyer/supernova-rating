package su.pernova.rating.de.maeyer;

import java.io.Serializable;

/**
 * Splits a combined rating across multiple players.
 * This is useful when combining a rating algorithm to team games.
 */
public interface RatingSplitter extends Serializable {

	/**
	 * Splits a combined rating amongst given players.
	 * Given players are typically all the players of a team.
	 * Given players may be empty, but not {@code null}.
	 *
	 * @param combinedWeighedRating a combined weighed rating to be split amongst players, &ge; 0.
	 * @param teamContext a team context, not {@code null}.
	 */
	double[] split(final double combinedWeighedRating, final TeamContext teamContext);
}
