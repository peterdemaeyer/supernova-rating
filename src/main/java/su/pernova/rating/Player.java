package su.pernova.rating;

import static java.lang.Double.compare;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;

public class Player implements Comparable<Player>, Serializable {

	private static final long serialVersionUID = 1L;

	public final String id;

	public final String name;

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
		this(id, null, rating, matchCount);
	}

	public Player(String id, String name, double rating, long matchCount) {
		this.id = requireNonNull(id, "ID is null");
		this.name = name;
		this.rating = rating;
		this.matchCount = matchCount;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(id);
		if (name != null) {
			builder.append(": ").append(name);
		}
		return builder.toString();
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
