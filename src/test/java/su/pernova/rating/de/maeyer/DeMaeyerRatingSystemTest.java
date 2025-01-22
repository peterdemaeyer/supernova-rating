package su.pernova.rating.de.maeyer;

import static java.lang.Double.isNaN;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static su.pernova.rating.RatingUtils.sumOfRatings;
import static su.pernova.rating.de.maeyer.DeMaeyerRatingSystem.computeWinRating1;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import su.pernova.io.SerializableTest;
import su.pernova.rating.Match;
import su.pernova.rating.Padel;
import su.pernova.rating.Player;
import su.pernova.rating.RatingSystemTest;

class DeMaeyerRatingSystemTest implements RatingSystemTest, SerializableTest {

	@Override
	public DeMaeyerRatingSystem newInstance() {
		return new DeMaeyerRatingSystem.Builder().build();
	}

	@Test
	public void ratingRespectsRanking() {
		final Player weakestPlayer = new Player("Weakest Player", 0., 0L);
		final Player weakerPlayer = new Player("Weaker Player", 0., 0L);
		final Player strongerPlayer = new Player("Stronger Player", 0., 0L);
		final Player strongestPlayer = new Player("Strongest Player", 0., 0L);
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder().build();

		final Match match1234 = Match.padel(weakestPlayer, weakerPlayer, strongerPlayer, strongestPlayer, 2L, 9L);
		ratingSystem.apply(match1234);
		assertEquals(weakestPlayer.rating, weakerPlayer.rating);
		assertEquals(strongerPlayer.rating, strongestPlayer.rating);
		assertEquals(weakestPlayer.rating * 9. / 2., strongerPlayer.rating, .01);
		printRatings(match1234);

		final Match match1342 = Match.padel(weakestPlayer, strongerPlayer, weakerPlayer, strongestPlayer, 6L, 9L);
		ratingSystem.apply(match1342);
		printRatings(match1342);

		Stream.of(weakestPlayer, weakerPlayer, strongerPlayer, strongestPlayer).forEach(player -> assertFalse(isNaN(weakestPlayer.rating)));
		assertTrue(weakestPlayer.rating < weakerPlayer.rating);
		assertTrue(weakerPlayer.rating < strongerPlayer.rating);
		assertTrue(strongerPlayer.rating < strongestPlayer.rating);
		assertTrue(weakestPlayer.rating * 9. / 2. < strongestPlayer.rating);
	}

	@Test
	void ratingDoesNotChangeWhenWeakPlayersLoseFromStrongPlayersByWalkover() {
		final Player weakPlayer1 = new Player("Weak Player 1", .8, 100L);
		final Player weakPlayer2 = new Player("Weak Player 2", 1., 100L);
		final Player strongPlayer1 = new Player("Strong Player 1", 800., 100L);
		final Player strongPlayer2 = new Player("Strong Player 2", 1000., 100L);
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder().build();
		final Match match = Padel.newMatch(weakPlayer1, weakPlayer2, strongPlayer1, strongPlayer2, 0L, 9L);
		ratingSystem.apply(match);
		printRatings(match);
		assertEquals(.8, weakPlayer1.rating, .01);
		assertEquals(101L, weakPlayer1.matchCount);
		assertEquals(1., weakPlayer2.rating, .01);
		assertEquals(101L, weakPlayer2.matchCount);
		assertEquals(800., strongPlayer1.rating, .01);
		assertEquals(101L, strongPlayer1.matchCount);
		assertEquals(1000., strongPlayer2.rating, .01);
		assertEquals(101L, strongPlayer2.matchCount);
	}

	private static void printRatings(final Match match) {
		RatingSystemTest.printRatings(System.out, concat(Stream.of(match.teams[0].players), Stream.of(match.teams[1].players)).collect(toList()));
	}

	@Test
	void ratingOfStrongPlayersDoesNotChangeWhenWinningFromUnratedPlayers() {
		final Player unratedWeakPlayer1 = new Player("Unrated Player 1", 0., 0L);
		final Player unratedWeakPlayer2 = new Player("Unrated Player 2", 0., 0L);
		final Player strongPlayer1 = new Player("Strong Player 1", 800., 100L);
		final Player strongPlayer2 = new Player("Strong Player 2", 1000., 100L);
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder().build();
		final Match match = Padel.newMatch(unratedWeakPlayer1, unratedWeakPlayer2, strongPlayer1, strongPlayer2, 0L, 9L);
		ratingSystem.apply(match);
		printRatings(match);
		assertEquals(52.50, unratedWeakPlayer1.rating, .01);
		assertEquals(1L, unratedWeakPlayer1.matchCount);
		assertEquals(52.50, unratedWeakPlayer2.rating, .01);
		assertEquals(1L, unratedWeakPlayer2.matchCount);
		assertEquals(808.00, strongPlayer1.rating, .01);
		assertEquals(101L, strongPlayer1.matchCount);
		assertEquals(1010.00, strongPlayer2.rating, .01);
		assertEquals(101L, strongPlayer2.matchCount);
	}

	@Test
	void ratingOfStrongUnratedPlayersWhenWinningFromRatedPlayers() {
		final Player strongUnratedPlayer1 = new Player("Strong Unrated Player 1", 0., 0L);
		final Player strongUnratedPlayer2 = new Player("Strong Unrated Player 2", 0., 0L);
		final Player ratedPlayer1 = new Player("Rated Player 1", 800., 100L);
		final Player ratedPlayer2 = new Player("Rated Player 2", 1000., 100L);
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder().build();
		final Match match = Padel.newMatch(strongUnratedPlayer1, strongUnratedPlayer2, ratedPlayer1, ratedPlayer2, 9L, 0L);
		ratingSystem.apply(match);
		printRatings(match);
		assertEquals(240.45, strongUnratedPlayer1.rating, .01);
		assertEquals(1L, strongUnratedPlayer1.matchCount);
		assertEquals(240.45, strongUnratedPlayer2.rating, .01);
		assertEquals(1L, strongUnratedPlayer2.matchCount);
		assertEquals(640.93, ratedPlayer1.rating, .01);
		assertEquals(101L, ratedPlayer1.matchCount);
		assertEquals(801.17, ratedPlayer2.rating, .01);
		assertEquals(101L, ratedPlayer2.matchCount);
		// 1923 = 800 + 1000 + 50 + 50 + (portion of rating pool excess = 23)
		assertEquals(1923., sumOfRatings(strongUnratedPlayer1, strongUnratedPlayer2, ratedPlayer1, ratedPlayer2));
	}

	@Test
	void oneUnderratedPlayer() {
		final Player underratedPlayer1 = new Player("Underrated Player 1", 10., 10L);
		final Player player2 = new Player("Player 2", 190., 10L);
		final Player player3 = new Player("Player 3", 180., 10L);
		final Player player4 = new Player("Player 4", 220., 10L);
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder().build();
		final Match match1234 = Padel.newMatch(underratedPlayer1, player2, player3, player4, 7L, 9L);
		ratingSystem.apply(match1234);
		assertEquals(11.43, underratedPlayer1.rating, .01);
		final Match match1324 = Padel.newMatch(underratedPlayer1, player3, player2, player4, 9L, 6L);
		ratingSystem.apply(match1324);
		assertEquals(15.30, underratedPlayer1.rating, .01);
		final Match match1423 = Padel.newMatch(underratedPlayer1, player4, player2, player3, 9L, 8L);
		ratingSystem.apply(match1423);
		assertEquals(19.17, underratedPlayer1.rating, .01);
	}

	@Test
	void ratingPoolAlsoAntesWhenScoreBelowThreshold() {
		final Player player1 = new Player("Player 1", 10., 10L);
		final Player player2 = new Player("Player 2", 20., 10L);
		final Player player3 = new Player("Player 3", 100., 10L);
		final Player player4 = new Player("Player 4", 120., 10L);
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder()
				.setSystemPooledRating(1000.)
				.setWeightPolicy(new FixedWeightPolicy(.5))
				.build();
		final Match match = Padel.newMatch(player1, player2, player3, player4, 0L, 5L);
		ratingSystem.apply(match);
		assertTrue(ratingSystem.systemPooledRating < 1000.);
		assertTrue(player1.rating > 10.);
		assertTrue(player2.rating > 20.);
		assertEquals(2. * player1.rating, player2.rating);
		assertTrue(player3.rating > 100.);
		assertEquals(10. * player1.rating, player3.rating);
		assertTrue(player4.rating > 120.);
		assertEquals(12. * player1.rating, player4.rating);
	}

	@Test
	void computationOfWinRating1() {
		assertEquals(7., computeWinRating1(10., 10., 7., 5., 12.));
		assertEquals(2.5, computeWinRating1(5., 10., 5., 7., 12.));
		assertEquals(9.5, computeWinRating1(10., 5., 7., 5., 12.));
	}
}
