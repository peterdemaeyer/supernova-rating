package su.pernova.rating;

import static java.lang.Double.compare;
import static java.util.Objects.requireNonNull;

public class Player implements Comparable<Player> {

	public final String id;

	public double rating;

	public long matchCount;

	public Player(String id) {
		this(id, 0., 0L);
	}

	/**
	 * @param id an ID, which must not be {@code null}.
	 * @param rating an initial rating. When {@code matchCount} is zero, this may be overridden by the rating system.
	 * @param matchCount an initial match count.
	 */
	public Player(String id, double rating, long matchCount) {
		this.id = requireNonNull(id, "ID is null");
		this.rating = rating;
		this.matchCount = matchCount;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Player player = (Player) o;
		return id.equals(player.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public int compareTo(Player p) {
		int result = compare(rating, p.rating);
		return result != 0 ? result : id.compareTo(p.id);
	}
}
