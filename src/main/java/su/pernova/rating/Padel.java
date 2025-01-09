package su.pernova.rating;

public final class Padel {

	private Padel() {
	}

	public static Match newMatch(Player player1a, Player player1b, Player player2a, Player player2b, long... scores) {
		return newMatch(player1a, player1b, player2a, player2b, newSets(scores));
	}

	private static Set[] newSets(long... scores) {
		final Set[] sets = new Set[scores.length / 2];
		for (int scoreIndex = 0; scoreIndex != scores.length; ) {
			sets[scoreIndex / 2] = new Set(scores[scoreIndex++], scores[scoreIndex++]);
		}
		return sets;
	}

	public static Match newMatch(Player player1a, Player player1b, Player player2a, Player player2b, Set... sets) {
		return new Match(new Team[] { new Team(player1a, player1b), new Team(player2a, player2b) }, sets);
	}
}
