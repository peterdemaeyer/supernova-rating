package su.pernova.rating;

@FunctionalInterface
public interface RatingSystem {

	/**
	 * Applies this rating algorithm to a given match.
	 * Exceptions aside, every application increases the match count of all players of all teams.
	 * <p/>
	 * A match with a score 0-0 in all sets has no effect, the match count does not increase.
	 * It is as if the match never happened.
	 *
	 * @param match a match, not {@code null}.
	 */
	void apply(Match match);
}
