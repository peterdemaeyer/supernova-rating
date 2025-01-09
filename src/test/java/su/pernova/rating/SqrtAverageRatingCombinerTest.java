package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SqrtAverageRatingCombinerTest {

	private final Player player0 = new Player("0", 0., 10L);

	private final Player player1 = new Player("1", 1., 10L);

	private final Player player2 = new Player("-1", -2., 10L);

	@Test
	void combinedRatingFavorsWeakestPlayer() {
		final SqrtAverageRatingCombiner ratingCombiner = new SqrtAverageRatingCombiner();
		assertEquals(0., ratingCombiner.combineRatings(player0));
		assertEquals(1., ratingCombiner.combineRatings(player1, player1));
		assertEquals(.25, ratingCombiner.combineRatings(player0, player1));
	}

	@Test
	void combinedNegativeRating() {
		final SqrtAverageRatingCombiner ratingCombiner = new SqrtAverageRatingCombiner();
		assertEquals(.25, ratingCombiner.combineRatings(player1, player2));
	}
}
