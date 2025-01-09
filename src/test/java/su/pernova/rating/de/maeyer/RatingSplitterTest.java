package su.pernova.rating.de.maeyer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import su.pernova.rating.Player;
import su.pernova.rating.Team;

public interface RatingSplitterTest {

	RatingSplitter newInstance();

	@Test
	default void splitIsEmptyWhenNoPlayers() {
		final TeamContext teamContext = new TeamContext(new Team());
		final RatingSplitter ratingSplitter = newInstance();
		assertArrayEquals(new double[0], ratingSplitter.split(354., teamContext));
	}

	@Test
	default void splitLossWhenOnePlayer() {
		final Player player = new Player("One", 751., 15L);
		final Team team = new Team(player);
		final TeamContext teamContext = newTeamContext(team, .21);
		final RatingSplitter ratingSplitter = newInstance();
		assertArrayEquals(new double[] { 123. }, ratingSplitter.split(123., teamContext));
	}

	@Test
	default void splitLossWhenOnePlayerHasZeroRating() {
		final Player player1 = new Player("One", 300., 15L);
		final Player player2 = new Player("Two", 0., 10L);
		final Player player3 = new Player("Three", 700., 56L);
		final Team team = new Team(player1, player2, player3);
		final TeamContext teamContext = newTeamContext(team, .2, .3, .5);
		final RatingSplitter ratingSplitter = newInstance();
		final double[] splitWeighedRatings = ratingSplitter.split(20., teamContext);
		for (double splitWeighedRating : splitWeighedRatings) {
			assertTrue(splitWeighedRating >= 0.);
		}
	}

	@Test
	default void noPlayerLosesWhenTeamWins() {
		final Player player1 = new Player("One", 100., 1L);
		final Player player2 = new Player("Two", 200., 100L);
		final Player player3 = new Player("Three", 300., 513L);
		final Team team = new Team(player1, player2, player3);
		final TeamContext teamContext = newTeamContext(team, .5, .1, .1);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		// No player loses rating when the team wins.
		// Only the increment is split.
		final double combinedWeighedRating = teamContext.sumOfWeighedRatings + 1.;
		assertTrue(combinedWeighedRating > teamContext.sumOfWeighedRatings);
		final double[] splitWeighedRatings = ratingSplitter.split(combinedWeighedRating, teamContext);
		assertTrue(100. < (1. - .5) * 100. + splitWeighedRatings[0]);
		assertTrue(200. < (1. - .1) * 200. + splitWeighedRatings[1]);
		assertTrue(300. < (1. - .1) * 300. + splitWeighedRatings[2]);
	}

	@Test
	default void noPlayerWinsWhenTeamLoses() {
		final Player player1 = new Player("One", 100., 1L);
		final Player player2 = new Player("Two", 200., 100L);
		final Player player3 = new Player("Three", 300., 513L);
		final Team team = new Team(player1, player2, player3);
		final TeamContext teamContext = newTeamContext(team, .5, .1, .1);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		// No player loses rating when the team wins.
		// Only the increment is split.
		final double combinedWeighedRating = teamContext.sumOfWeighedRatings - 1.;
		assertTrue(combinedWeighedRating > 0.);
		assertTrue(combinedWeighedRating < teamContext.sumOfWeighedRatings);
		final double[] splitWeighedRatings = ratingSplitter.split(combinedWeighedRating, teamContext);
		assertTrue(100. > (1. - .5) * 100. + splitWeighedRatings[0]);
		assertTrue(200. > (1. - .1) * 200. + splitWeighedRatings[1]);
		assertTrue(300. > (1. - .1) * 300. + splitWeighedRatings[2]);
	}

	@Test
	default void sumOfSplitRatingsIsCombinedRatingWhenWin() {
		final Player player1 = new Player("1", 1000., 10L);
		final Player player2 = new Player("2", 1000., 10L);
		final Team team = new Team(player1, player2);
		final TeamContext teamContext = newTeamContext(team, .3, .7);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		final double[] splitWeighedRatings = ratingSplitter.split(1100., teamContext);
		assertEquals(1100., sum(splitWeighedRatings), 1e-12);
	}

	static double sum(double... values) {
		double sum = 0.;
		for (double value : values) {
			sum += value;
		}
		return sum;
	}

	static TeamContext newTeamContext(Team team, double... weights) {
		final int playerCount = weights.length;
		final TeamContext teamContext = new TeamContext(team);
		for (int playerIndex = 0; playerIndex != playerCount; playerIndex++) {
			final PlayerContext playerContext = teamContext.playerContexts[playerIndex];
			playerContext.weight = weights[playerIndex];
			teamContext.sumOfWeights += playerContext.weight;
			playerContext.weighedRating = playerContext.weight * playerContext.player.rating;
			teamContext.sumOfWeighedRatings += playerContext.weighedRating;
		}
		return teamContext;
	}
}
