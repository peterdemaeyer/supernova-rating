package su.pernova.rating.de.maeyer;

import static java.lang.Double.isNaN;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static com.google.gson.FormattingStyle.PRETTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static su.pernova.rating.RatingUtils.sumOfRatings;

import java.lang.reflect.Type;
import java.util.stream.Stream;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import org.junit.jupiter.api.Test;

import su.pernova.rating.AverageRatingCombiner;
import su.pernova.rating.Match;
import su.pernova.rating.Padel;
import su.pernova.rating.Player;
import su.pernova.rating.RatingCombiner;
import su.pernova.rating.RatingSystem;
import su.pernova.rating.RatingSystemTest;

class DeMaeyerRatingSystemTest implements RatingSystemTest {

	@Override
	public RatingSystem newInstance() {
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
		assertEquals(weakestPlayer.rating * 9. / 2., strongerPlayer.rating);
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
		assertEquals(.8, weakPlayer1.rating);
		assertEquals(101L, weakPlayer1.matchCount);
		assertEquals(1., weakPlayer2.rating);
		assertEquals(101L, weakPlayer2.matchCount);
		assertEquals(800., strongPlayer1.rating);
		assertEquals(101L, strongPlayer1.matchCount);
		assertEquals(1000., strongPlayer2.rating);
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
		assertEquals(50., unratedWeakPlayer1.rating);
		assertEquals(1L, unratedWeakPlayer1.matchCount);
		assertEquals(50., unratedWeakPlayer2.rating);
		assertEquals(1L, unratedWeakPlayer2.matchCount);
		assertEquals(800., strongPlayer1.rating);
		assertEquals(101L, strongPlayer1.matchCount);
		assertEquals(1000., strongPlayer2.rating);
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
		assertEquals(240.1657458563536, strongUnratedPlayer1.rating);
		assertEquals(1L, strongUnratedPlayer1.matchCount);
		assertEquals(240.1657458563536, strongUnratedPlayer2.rating);
		assertEquals(1L, strongUnratedPlayer2.matchCount);
		assertEquals(640., ratedPlayer1.rating);
		assertEquals(101L, ratedPlayer1.matchCount);
		assertEquals(802.6685082872928, ratedPlayer2.rating);
		assertEquals(101L, ratedPlayer2.matchCount);
		// 1923 = 800 + 1000 + 50 + 50 + (portion of rating pool excess = 23)
		assertEquals(1923., sumOfRatings(strongUnratedPlayer1, strongUnratedPlayer2, ratedPlayer1, ratedPlayer2));
	}

	@Test
	void serialization() {
		final Gson gson = new GsonBuilder()
				.setVersion(1.0)
				.setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
				.setFormattingStyle(PRETTY)
				.registerTypeAdapter(RatingCombiner.class, (InstanceCreator<RatingCombiner>) type -> {
					System.out.println(type.getTypeName());
					return new AverageRatingCombiner();
				})
				.create();
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder()
				.setInitialRatingExcess(12.)
				.setInitialRating(5.)
				.build();
		final String json = gson.toJson(ratingSystem);
		System.out.println(json);
		final RatingSystem clone = gson.fromJson(json, ratingSystem.getClass());
		System.out.println(clone);
	}
}
