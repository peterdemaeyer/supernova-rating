package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WeighedAverageRatingCombinerTest {

	private final Player player1 = new Player("Player 1", 0., 10L);

	private final Player player2 = new Player("Player 2", 1., 10L);

	@Test
	void combinedRatingIsLowestOfAllRatings() {
		final WeighedAverageRatingCombiner ratingCombiner = new WeighedAverageRatingCombiner();
		assertEquals(0., ratingCombiner.combineRatings(player1, player2));
		assertEquals(1., ratingCombiner.combineRatings(player2, player2));
	}
}
