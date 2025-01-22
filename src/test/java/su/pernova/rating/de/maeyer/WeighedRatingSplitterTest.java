package su.pernova.rating.de.maeyer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import static su.pernova.rating.de.maeyer.RatingSplitterTest.newTeamContext;

import org.junit.jupiter.api.Test;

import su.pernova.rating.Player;
import su.pernova.rating.Team;

class WeighedRatingSplitterTest implements RatingSplitterTest {

	@Override
	public RatingSplitter newInstance() {
		return new WeighedRatingSplitter();
	}

	@Test
	void insufficientRatingQuotaWhenLossIsAbsorbedByPlayersThatHaveEnoughQuotaLeft() {
		final Player player1 = new Player("1", 1000., 10L);
		final Player player2 = new Player("2", 20., 10L);
		final Player player3 = new Player("3", 0., 10L);
		final Team team = new Team(player1, player2, player3);
		final TeamContext teamContext = newTeamContext(team, 1., 1., 1.);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		final double[] splitWeighedRatings = ratingSplitter.split(50., teamContext);
		assertArrayEquals(new double[] { 49.01, .98 , 0. }, splitWeighedRatings, .01);
	}

	@Test
	void playersWithHighStakesWinTheMost() {
		final Player player1 = new Player("1", 1000., 10L);
		final Player player2 = new Player("2", 1000., 10L);
		final Player player3 = new Player("3", 1000., 10L);
		final Team team = new Team(player1, player2, player3);
		// Player 1 antes 0.2 * 1000 = 200.
		// Player 2 antes 0.3 * 1000 = 300.
		// Player 3 antes 0.5 * 1000 = 500.
		final TeamContext teamContext = newTeamContext(team, .2, .3, .5);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		// The total ante is 200 + 300 + 500 = 1000.
		// The win is 100 more than the ante = 1100.
		final double[] splitWeighedRatings = ratingSplitter.split(1100., teamContext);
		// The player who contributed a higher ante takes more of the win.
		assertArrayEquals(new double[] { 220., 330., 550. }, splitWeighedRatings);
	}

	@Test
	void playersWithHighStakesLoseTheMost() {
		final Player player1 = new Player("1", 1000., 10L);
		final Player player2 = new Player("2", 1000., 10L);
		final Player player3 = new Player("3", 1000., 10L);
		final Team team = new Team(player1, player2, player3);
		// Player 1 antes 0.2 * 1000 = 200.
		// Player 2 antes 0.3 * 1000 = 300.
		// Player 3 antes 0.5 * 1000 = 500.
		final TeamContext teamContext = newTeamContext(team, .2, .3, .5);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		// The total ante is 200 + 300 + 500 = 1000.
		// The win is 100 more than the ante = 1100.
		final double[] splitWeighedRatings = ratingSplitter.split(900., teamContext);
		// The player who contributed a higher ante takes more of the win.
		assertArrayEquals(new double[] { 180., 270., 450. }, splitWeighedRatings);
	}

	@Test
	void noPlayersLoseMoreThanEverythingTheyHave() {
		final Player player1 = new Player("1", 1000., 10L);
		final Player player2 = new Player("2", 1000., 10L);
		final Player player3 = new Player("3", 10., 10L);
		final Team team = new Team(player1, player2, player3);
		// Player 1 antes 0.2 * 1000 = 200.
		// Player 2 antes 0.3 * 10 = 3.
		// Player 3 antes 0.5 * 1000 = 500.
		final TeamContext teamContext = newTeamContext(team, .2, .3, .5);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		// The total ante is 200 + 300 + 5 = 505.
		// The win is 50 less than the ante = 455.
		final double[] splitWeighedRatings = ratingSplitter.split(455, teamContext);
		// The player who contributed a higher ante takes more of the win.
		// According to their weight, player 3 has to take 50% of the loss = -25, but is broke after -5.
		// Players 1 and 2 take 20% and 30% of the loss respectively, which is -10 and -15 respectively.
		// Additionally, player 3's deficit of -20 has to be absorbed by players 1 and 2 as well, according to their relative weights 20% and 30%.
		// The 20% and 30% become 40% and 60% of the deficit, which is -8 and -12 respectively.
		// So player 1 finally ends up with their initial ante minus the loss, which is 200 - 10 - 8 = 182.
		// In a similar fashion, player 2 finally ends up with 300 - 15 - 12 = 273.
		assertArrayEquals(new double[] { 180.20, 270.30, 4.50 }, splitWeighedRatings, .01);
	}

	@Test
	void playersWinAccordingToTheirRelativeRating() {
		final Player player1 = new Player("1", 10., 10L);
		final Player player2 = new Player("2", 20., 10L);
		final Team team = new Team(player1, player2);
		final TeamContext teamContext = newTeamContext(team, .5, .5);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		final double[] splitWeighedRatings = ratingSplitter.split(21., teamContext);
		assertArrayEquals(new double[] { 7., 14. }, splitWeighedRatings);
	}

	@Test
	void noNaNWhenZeroRatings() {
		final Player player1 = new Player("1", 10., 10L);
		final Player player2 = new Player("2", 20., 10L);
		final Team team = new Team(player1, player2);
		final TeamContext teamContext = newTeamContext(team, 0., 0.);
		final WeighedRatingSplitter ratingSplitter = new WeighedRatingSplitter();
		final double[] splitWeighedRatings = ratingSplitter.split(21., teamContext);
		assertArrayEquals(new double[] { 10.5, 10.5 }, splitWeighedRatings);
	}
}
