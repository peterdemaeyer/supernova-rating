package su.pernova.rating;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.isNaN;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;
import static java.util.Comparator.comparingDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public interface RatingSystemTest {

	Random RANDOM = new Random(351356847643654L);

	RatingSystem newInstance();

	static void warmUp(RatingSystem ratingSystem, List<Player> players, List<Set[]> scores) {
		final int playerCount = players.size();
		if (playerCount < 4) {
			throw new IllegalArgumentException("# players: " + playerCount + " < 4");
		}
		final int matchCombinationCount = computeMatchCombinationCount(playerCount);
		if (scores.size() != matchCombinationCount) {
			throw new IllegalArgumentException("# scores: " + scores.size() + " != # match combinations: " + matchCombinationCount);
		}
		double maxRatingAdjustment = 0.;
		while (true) {
			double ratingAdjustment = 0.;
			int matchCombinationIndex = 0;
			// j1 is the amount of places to "jump ahead" for the next player, starting from i1.
			for (int j1 = 1, m1 = playerCount - 1; j1 != m1; j1++) {
				for (int i1 = 0, n1 = playerCount - j1; i1 != n1; i1++) {
					Player player1a = players.get(i1);
					int i1b = i1 + j1;
					Player player1b = players.get(i1b);
					for (int j2 = 1, m2 = playerCount - 2; j2 != m2; j2++) {
						for (int i2 = i1 + 1, n2 = playerCount - j2; i2 != n2; i2++) {
							Player player2a = players.get(justify(i2, i1b));
							Player player2b = players.get(justify(i2 + j2, i1b));
							Match match = Padel.newMatch(player1a, player1b, player2a, player2b, scores.get(matchCombinationIndex++));
							final List<Player> selectedPlayers = asList(player1a, player1b, player2a, player2b);
							double sumOfAbsRatingsBefore = sumOfAbsRatings(selectedPlayers);
							ratingSystem.apply(match);
							double sumOfAbsRatingsAfter = sumOfAbsRatings(selectedPlayers);
							ratingAdjustment += abs(sumOfAbsRatingsAfter - sumOfAbsRatingsBefore);
						}
					}
				}
			}
			assertEquals(matchCombinationCount, matchCombinationIndex);
			if (ratingAdjustment > maxRatingAdjustment) {
				maxRatingAdjustment = ratingAdjustment;
			}
			if (ratingAdjustment < (.01 * maxRatingAdjustment)) {
				break;
			}
		}
	}

	static int justify(int i2, int i1b) {
		return i2 < i1b ? i2 : i2 + 1;
	}

	static int computeMatchCombinationCount(int playerCount) {
		return (factorial(playerCount, playerCount - 2) / 2
				* factorial(playerCount - 2, playerCount - 4) / 2)
				/ 2;
	}

	static int factorial(int factorialNumerator, int factorialDenominator) {
		int result = 1;
		for (; factorialNumerator > factorialDenominator; factorialNumerator--) {
			result *= factorialNumerator;
		}
		return result;
	}

	/**
	 * Players 1, 2, 3, 4 have ranking P100, P200, P300, P400 respectively.
	 * When match scores are logical according to ranking, players end up according to their ranking after 2 matches.
	 */
	@Test
	default void ratingRespectsRanking() {
		ratingRespectsRanking(
			new Player("Arend", 200., 1L),
			new Player("Amaryllis", 200., 1L),
			new Player("Karo", 200., 1L),
			new Player("Juul", 200., 1L)
		);
	}

	default void ratingRespectsRanking(Player player1, Player player2, Player player3, Player player4) {
		final RatingSystem ratingSystem = newInstance();

		final Match match1234 = Match.padel(player1, player2, player3, player4, 2L, 9L);
		ratingSystem.apply(match1234);
		printRatings(System.out, asList(player1, player2, player3, player4));

		final Match match1342 = Match.padel(player1, player3, player2, player4, 6L, 9L);
		ratingSystem.apply(match1342);
		printRatings(System.out, asList(player1, player2, player3, player4));

		Stream.of(player1, player2, player3, player4).forEach(player -> assertFalse(isNaN(player1.rating)));
		assertTrue(player1.rating < player2.rating);
		assertTrue(player2.rating < player3.rating);
		assertTrue(player3.rating < player4.rating);
	}

	@Test
	default void initialRatingRespectsRanking() {
		ratingRespectsRanking(
				new Player("Arend"),
				new Player("Amaryllis"),
				new Player("Karo"),
				new Player("Juul")
		);
	}

	@Test
	default void matchCountIncreasesWithEveryMatch() {
		RatingSystem ratingSystem = newInstance();
		Player player1 = new Player("P100", 0., 0L);
		Player player2 = new Player("P200", 0., 0L);
		Player player3 = new Player("P300", 0., 0L);
		Player player4 = new Player("P400", 0., 0L);
		Stream.of(player1, player2, player3, player4).forEach(player -> assertEquals(0L, player.matchCount));

		Match match1234 = Match.padel(player1, player2, player3, player4, 2L, 9L);
		ratingSystem.apply(match1234);
		Stream.of(player1, player2, player3, player4).forEach(player -> assertEquals(1L, player.matchCount));

		Match match1342 = Match.padel(player1, player3, player2, player4, 6L, 9L);
		ratingSystem.apply(match1342);
		Stream.of(player1, player2, player3, player4).forEach(player -> assertEquals(2L, player.matchCount));
	}

	/**
	 * Tests that after numerous iterations, the sum of all ratings across all players stays the same.
	 * In other words, that the total amount of ratings does not inflate nor deflate.
	 */
	@Test
	default void ratingDoesNotInflateNorDeflate() {
		List<Player> players = asList(
				new Player("Dirk", 200., 100L),
				new Player("Jan", 200., 100L),
				new Player("Jeroen", 200., 100L),
				new Player("Peter", 200., 100L),
				new Player("Piet", 200., 100L),
				new Player("Pieter-Jan", 200., 100L)
		);
		double initialSumOfRatings = sumOfAbsRatings(players);

		RatingSystem ratingSystem = newInstance();

		for (int i = 0, n = 100; i != n; i++) {
			shuffle(players, RANDOM);
			Match match = Match.padel(
					players.get(0),
					players.get(1),
					players.get(2),
					players.get(3),
					RANDOM.nextInt(9), 9L
			);
			ratingSystem.apply(match);
			printRatings(System.out, players);
			assertEquals(initialSumOfRatings, sumOfAbsRatings(players), 1e-3);
		}
	}

	@Test
	default void ratingConvergesWhenEqualInitialRating() {
		ratingConverges(200., 200., 200., 200., 5L, 9L);
	}

	@Test
	default void ratingConvergesWhenUnequalInitialRating() {
		ratingConverges(1., 299., 75., 25., 3L, 9L);
	}

	default void ratingConverges(double initialRating1, double initialRating2, double initialRating3, double initialRating4, long score1, long score2) {
		final Player player1 = new Player("Kirsten", initialRating1, 0L);
		final Player player2 = new Player("Karin", initialRating2, 0L);
		final Player player3 = new Player("Lut", initialRating3, 0L);
		final Player player4 = new Player("Liesbet", initialRating4, 0L);

		final RatingSystem ratingSystem = newInstance();

		double previousDifference1 = POSITIVE_INFINITY;
		double previousDifference2 = POSITIVE_INFINITY;
		double previousDifference3 = POSITIVE_INFINITY;
		double previousDifference4 = POSITIVE_INFINITY;

		for (int i = 0, n = 100; i != n; i++) {
			final Match match = Match.padel(player1, player2, player3, player4, score1, score2);
			final double previousRating1 = player1.rating;
			final double previousRating2 = player2.rating;
			final double previousRating3 = player3.rating;
			final double previousRating4 = player4.rating;
			ratingSystem.apply(match);
			final double difference1 = abs(player1.rating - previousRating1);
			final double difference2 = abs(player2.rating - previousRating2);
			final double difference3 = abs(player3.rating - previousRating3);
			final double difference4 = abs(player4.rating - previousRating4);
			assertTrue(difference1 < previousDifference1, format("%d: %s < %s", i, difference1, previousDifference1));
			assertTrue(difference2 < previousDifference2, format("%d: %s < %s", i, difference2, previousDifference2));
			assertTrue(difference3 < previousDifference3, format("%d: %s < %s", i, difference3, previousDifference3));
			assertTrue(difference4 < previousDifference4, format("%d: %s < %s", i, difference4, previousDifference4));
			if (difference1 < .1
					&& difference2 < .1
					&& difference3 < .1
					&& difference4 < .1) {
				break;
			}
			previousDifference1 = difference1;
			previousDifference2 = difference2;
			previousDifference3 = difference3;
			previousDifference4 = difference4;
		}
		printRatings(System.out, asList(player1, player2, player3, player4));
	}

	static double sumOfAbsRatings(Collection<Player> players) {
		double sumOfRatings = 0.;
		for (Player player : players) {
			sumOfRatings += abs(player.rating);
		}
		return sumOfRatings;
	}

	@Test
	default void matchWithZeroZeroScoreBehavesAsIfNoMatchPlayed() {
		final Player player1 = new Player("Jeroen", 200., 0L);
		final Player player2 = new Player("Piet", 200., 0L);
		final Match match = new Match(currentTimeMillis(), new Team[] { new Team(player1), new Team(player2) }, new Set(0L, 0L));
		final RatingSystem ratingSystem = newInstance();
		ratingSystem.apply(match);
		assertEquals(0L, player1.matchCount);
		assertEquals(200., player1.rating);
		assertEquals(0L, player2.matchCount);
		assertEquals(200., player2.rating);
	}
//
//	@Test
//	default void ratingConvergesX() throws Exception {
//		final Players players = new Players(getSystemResource("test-players.txt").toURI());
//		final Map<Player, Double> levels = readLevels(getSystemResource("test-levels.txt").toURI(), players);
//		RatingSystem ratingSystem = newInstance();
//		int iterationCount = 0;
//		outer:
//		while (iterationCount < 50) {
//			printRatings(System.out, players, iterationCount + " iterations");
//			final List<Match> matches = generateRandomMatches(players, levels, 24);
//			matches.forEach(ratingSystem::apply);
//			iterationCount++;
//			for (Entry<Player, Double> entry : levels.entrySet()) {
//				final double rank = entry.getKey().rating;
//				final double level = entry.getValue();
//				if (rank < level * .9 || rank > level * 1.1) {
//					continue outer;
//				}
//			}
//			break;
//		}
//		printRatings(System.out, players, iterationCount + " iterations");
//	}
//
//	static Map<Player, Double> readLevels(URI uri, Players players) throws IOException {
//		final Map<Player, Double> levels = new LinkedHashMap<>(players.getPlayers().size());
//		try (BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream()))) {
//			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
//				final String[] components = line.split("=");
//				levels.put(players.getPlayer(components[0].trim()), parseDouble(components[1].trim()));
//			}
//		}
//		return levels;
//	}
//
//	static List<Match> generateRandomMatches(Players players, Map<Player, Double> levels, int matchCount) {
//		final List<Match> matches = new ArrayList<>(matchCount);
//		final List<Player> playerList = players.getPlayers();
//		final List<Player> list = new ArrayList<>(playerList);
//		int playerIndex = RANDOM.nextInt(list.size());
//		for (int matchIndex = 0; matchIndex != matchCount; matchIndex++) {
//			final List<Player> party = new ArrayList<>(4);
//			for (int partyIndex = 0; partyIndex != 4; partyIndex++) {
//				if (playerIndex == list.size()) {
//					shuffle(list, RANDOM);
//					playerIndex = 0;
//				}
//				party.add(list.get(playerIndex++ % list.size()));
//			}
//			Player player1a = party.get(0);
//			Player player1b = party.get(1);
//			Player player2a = party.get(2);
//			Player player2b = party.get(3);
//			Match match = generateMatchResultBasedOnLevels(player1a, player1b, player2a, player2b, levels);
//			matches.add(match);
//		}
//		return matches;
//	}
//
//	static Match generateMatchResultBasedOnLevels(Player player1a, Player player1b, Player player2a, Player player2b, Map<Player, Double> levels) {
//		final double level1 = levels.get(player1a) + levels.get(player1b);
//		final double level2 = levels.get(player2a) + levels.get(player2b);
//		long games1 = 9L;
//		long games2 = 9L;
//		if (level1 == level2) {
//			if (RANDOM.nextBoolean()) {
//				games1 = 8L;
//			} else {
//				games1 = 8L;
//			}
//		} else if (level1 < level2) {
//			games1 = min(8L, round(9d * level1 / level2));
//		} else {
//			games2 = min(8L, round(9d * level2 / level1));
//		}
//		return Match.padel(player1a, player1b, player2a, player2b, games1, games2);
//	}
//
//	static void printRatings(PrintStream stream, Players players, Object subject) {
//		stream.println(format("=== rating after %s ===", subject));
//		Comparator<Player> orderByRating = comparingDouble(player -> player.rating);
//		orderByRating = orderByRating.thenComparing(player -> player.id);
//		orderByRating = orderByRating.reversed();
//		final SortedSet<Player> sortedPlayers = new TreeSet<>(orderByRating);
//		sortedPlayers.addAll(players.getPlayers());
//		sortedPlayers.forEach(player -> stream.printf("%-30s: %.2f (/%d)%n", player.id, player.rating, player.matchCount));
//	}

	static void printRatings(PrintStream stream, Collection<Player> players) {
		stream.println("=== rating ===");
		Comparator<Player> orderByRating = comparingDouble(player -> player.rating);
		orderByRating = orderByRating.thenComparing(player1 -> player1.id);
		orderByRating = orderByRating.reversed();
		final SortedSet<Player> sortedPlayers = new TreeSet<>(orderByRating);
		sortedPlayers.addAll(players);
		sortedPlayers.forEach(player -> stream.printf("%-30s: %.2f (/%d)%n", player.id, player.rating, player.matchCount));
	}
}
