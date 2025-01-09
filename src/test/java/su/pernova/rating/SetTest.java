package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SetTest {

	@Test
	void constructionThrowsWhenGamesIsLessThanZero() {
		assertEquals("games1 < 0",
				assertThrows(IllegalArgumentException.class, () -> new Set(-1L, 0L)).getMessage());
		assertEquals("games2 < 0",
				assertThrows(IllegalArgumentException.class, () -> new Set(0L, -5L)).getMessage());
	}

	@Test
	void stringValue() {
		assertEquals("7-9", new Set(7L, 9L).toString());
	}
}
