package su.pernova.rating.elo;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparingDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import su.pernova.rating.AverageRatingCombiner;
import su.pernova.rating.Match;
import su.pernova.rating.Player;
import su.pernova.rating.RatingSystemTest;
import su.pernova.rating.WeighedAverageRatingCombiner;

class EloRatingSystemTest implements RatingSystemTest {

	@Override
	public EloRatingSystem newInstance() {
		return new EloRatingSystem();
	}

	final Player player1a = new Player("Player 1a");

	final Player player1b = new Player("Player 1b");

	final Player player2a = new Player("Player 2a");

	final Player player2b = new Player("Player 2b");

	@Test
	void ratingDoesNotChangeIfScoreIsAsExpected() {
		final Player player1a = new Player("1a", 100., 100L);
		final Player player1b = new Player("1b", 100., 100L);
		final Player player2a = new Player("2a", 200., 100L);
		final Player player2b = new Player("2b", 200., 100L);
		final Match match = Match.padel(player1a, player1b, player2a, player2b, 3L, 6L);
		// Use an AverageRatingCombiner to weight the ratings of both players of a team equally.
		// It doesn't matter so much since the ratings of both players of a team are the same anyway.
		// The initial rating doesn't matter. All players are already rated.
		// Use a base of 2 so that every scale increase means being a factor 2 better.
		// Use a scale of 100.
		// Use a K-factor of 0.4. It doesn't matter much.
		final EloRatingSystem ratingSystem = new EloRatingSystem(new AverageRatingCombiner(), 150., 2., 100., 4.);
		ratingSystem.apply(match);
		assertEquals(100., player1a.rating);
		assertEquals(100., player1b.rating);
		assertEquals(200., player2a.rating);
		assertEquals(200., player2b.rating);
	}

	@Test
	void ratingChangesIfScoreIsNotAsExpected() {
		final Player player1a = new Player("1a", 100., 100L);
		final Player player1b = new Player("1b", 100., 100L);
		final Player player2a = new Player("2a", 200., 100L);
		final Player player2b = new Player("2b", 200., 100L);
		final Match match = Match.padel(player1a, player1b, player2a, player2b, 3L, 6L);
		final EloRatingSystem ratingSystem = new EloRatingSystem(new AverageRatingCombiner(), 150., 10., 40., 4.);
		ratingSystem.apply(match);
		assertTrue(player2a.rating - player1a.rating < 100.);
	}

	@Test
	void ratingOfUnbalancedMatchDoesNotChange() {
		final double base = 2.;
		final double alpha = 20.;
		final double r1 = 100. - alpha / 2.;
		final Player player1a = new Player("1a", r1, 1L);
		final Player player1b = new Player("1b", r1, 1L);
		final double r2 = 100. + alpha / 2.;
		final Player player2a = new Player("2a", r2, 1L);
		final Player player2b = new Player("2b", r2, 1L);
		final Match match = Match.padel(player1a, player1b, player2a, player2b, 3L, 6L);
		final EloRatingSystem ratingSystem = new EloRatingSystem(new AverageRatingCombiner(), 100., base, alpha, 20.);
		ratingSystem.apply(match);
		assertEquals(r1, player1a.rating, 1e-12);
		assertEquals(r1, player1b.rating, 1e-12);
		assertEquals(r2, player2a.rating, 1e-12);
		assertEquals(r2, player2b.rating, 1e-12);
	}

	@Test
	void ratingOfUnbalancedMatchChangesByNoMoreThanOneSeventh() {
		final Player player1a = new Player("Player 1a");
		final Player player1b = new Player("Player 1b");
		final Player player2a = new Player("Player 2a");
		final Player player2b = new Player("Player 2b");
		final Match match = Match.padel(player1a, player1b, player2a, player2b, 0L, 10L);
		final EloRatingSystem ratingSystem = new EloRatingSystem(new WeighedAverageRatingCombiner(.5), 100., 10., 10., 100.);
		ratingSystem.apply(match);
		printRatings(System.out, asList(player1a, player1b, player2a, player2b));
		assertEquals(25., player1a.rating, 1e-12);
		assertEquals(25., player1b.rating, 1e-12);
		assertEquals(175., player2a.rating, 1e-12);
		assertEquals(175., player2b.rating, 1e-12);
	}

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
