package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RatingUtilsTest {

	@Test
	void sumOfRatings() {
		assertEquals(0., RatingUtils.sumOfRatings());
		assertEquals(123., RatingUtils.sumOfRatings(
				new Player("Els", 123., 0L))
		);
		assertEquals(200., RatingUtils.sumOfRatings(
				new Player("Kris", 130., 5L),
				new Player("Gert", 70., 7L))
		);
	}
}
