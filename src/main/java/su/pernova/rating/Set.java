package su.pernova.rating;

import static java.util.Objects.requireNonNull;

public final class Set {

	public final long[] score;

	/**
	 * @param score a score as an array # won games, one entry per team, not {@code null}.
	 */
	public Set(long... score) {
		this.score = requireNonNull(score, "score is null");
		for (final long games : score) {
			if (games < 0L) {
				throw new IllegalArgumentException("# won games < 0");
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		for (final long games : score) {
			if (builder.length() > 0) {
				builder.append(" - ");
			}
			builder.append(games);
		}
		return builder.toString();
	}
}
