package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TeamTest {

	@Test
	void constructionThrowsWhenPlayerIsNull() {
		assertEquals("array of players contains null",
				assertThrows(NullPointerException.class,
						() -> new Team(null, new Player("1a")))
						.getMessage());
		assertEquals("array of players is null",
				assertThrows(NullPointerException.class,
						() -> new Team((Player[]) null))
						.getMessage());
	}
}
