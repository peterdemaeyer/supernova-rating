package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AverageRatingCombinerTest {

	private final Player player0 = new Player("0", 0., 10L);

	private final Player player1 = new Player("1", 1., 10L);

	@Test
	void combinedRatingIsAverageOfRatings() {
		final AverageRatingCombiner ratingCombiner = new AverageRatingCombiner();
		assertEquals(0., ratingCombiner.combineRatings(player0));
		assertEquals(.5, ratingCombiner.combineRatings(player0, player1));
		assertEquals(1. / 3., ratingCombiner.combineRatings(player0, player0, player1));
		assertEquals(1., ratingCombiner.combineRatings(player1, player1));
	}
}
