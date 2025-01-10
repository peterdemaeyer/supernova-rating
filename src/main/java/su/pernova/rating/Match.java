package su.pernova.rating;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;

public class Match {

	public final long time;

	public final Team[] teams;

	public final Set[] sets;

	public Match(long time, final Team[] teams, final Set... sets) {
		this.time = time;
		this.teams = requireNonNull(teams, "array of teams is null");
//		for (Player player1 : team1.players) {
//			for (Player player2 : team2.players) {
//				if (player1.equals(player2)) {
//					throw new IllegalArgumentException("player on both teams: " + player1);
//				}
//			}
//		}
		this.sets = requireNonEmpty(requireNonNull(sets, "array of sets is null"), "array of sets is empty");
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		for (final Team team : teams) {
			if (builder.length() > 0) {
				builder.append(" - ");
			}
			builder.append(team);
		}
		return builder + " = " + Arrays.toString(sets);
	}

	private static Set[] requireNonEmpty(Set[] sets, String message) {
		if (sets.length == 0) {
			 throw new IllegalArgumentException(message);
		}
		return sets;
	}

	public static Match padel(Player player1a, Player player1b, Player player2a, Player player2b, long... scores) {
		if (scores.length % 2 == 1) {
			throw new IllegalArgumentException("odd # scores: " + scores.length);
		}
		final int n = scores.length / 2;
		final Set[] sets = new Set[n];
		for (int i = 0; i != n; i++) {
			sets[i] = new Set(scores[i * 2], scores[i * 2 + 1]);
		}
		return new Match(currentTimeMillis(), new Team[] { new Team(player1a, player1b), new Team(player2a, player2b) }, sets);
	}
}
