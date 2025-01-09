package su.pernova.rating;

import java.io.Serializable;

/**
 * Combines the ratings of multiple players into a single combined rating.
 * This is useful when applying rating algorithms to team games.
 */
public interface RatingCombiner extends Serializable {

	double combineRatings(Player... players);
}
