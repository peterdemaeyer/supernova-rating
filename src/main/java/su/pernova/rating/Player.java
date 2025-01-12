package su.pernova.rating;

import static java.lang.Double.compare;
import static java.lang.Long.compare;

import java.io.Serializable;

public class Player implements Comparable<Player>, Serializable {

	private static final long serialVersionUID = 1L;

	public final String id;

	public final String name;

	public double rating;

	public long matchCount;

	public long lastMatchTime;

	public Player(String id) {
		this(id, 0., 0L);
	}

	/**
	 * @param id an ID, which must not be {@code null}.
	 * @param rating an initial rating. When {@code matchCount} is zero, this may be overridden by the rating system.
	 * @param matchCount an initial match count.
	 */
	public Player(String id, double rating, long matchCount) {
		this(id, null, rating, matchCount, -1L);
	}

	public Player(String id, String name, double rating, long matchCount, long lastMatchTime) {
		this.id = id;
		this.name = name;
		this.rating = rating;
		this.matchCount = matchCount;
		this.lastMatchTime = lastMatchTime;
	}

	public Player(String id, String name) {
		this(id, name, 0., 0L, -1L);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(String.valueOf(id));
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
		return id == player.id;
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
