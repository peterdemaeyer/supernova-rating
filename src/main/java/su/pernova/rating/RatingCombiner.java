package su.pernova.rating;

/**
 * Combines the ratings of multiple players into a single combined rating.
 * This is useful when applying rating algorithms to team games.
 */
public interface RatingCombiner {

	double combineRatings(Player... players);
}
