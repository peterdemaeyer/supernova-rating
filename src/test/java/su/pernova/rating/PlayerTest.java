package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerTest {

	@Test
	void constructionThrowsWhenIdIsNull() {
		final NullPointerException expected = assertThrows(NullPointerException.class, () -> new Player(null));
		assertEquals("ID is null", expected.getMessage());
	}

	@Test
	void equalityAndHashCode() {
		final Player player = new Player("Kris Pynaert");
		assertTrue(player.equals(player));
		assertFalse(player.equals(null));
		assertFalse(player.equals(this));
		final Player equalPlayer = new Player(player.id);
		assertTrue(player.equals(equalPlayer));
		assertTrue(equalPlayer.equals(player));
		final Player playerWithDifferentRating = new Player(player.id);
		playerWithDifferentRating.rating = player.rating + 1.;
		assertTrue(player.equals(playerWithDifferentRating));
		final Player playerWithDifferentName = new Player("Kirsten Robberecht");
		assertFalse(player.equals(playerWithDifferentName));
	}

	@Test
	void defaultInitialProperties() {
		final Player player = new Player("Pieter-Jan De Boeck");
		assertEquals("Pieter-Jan De Boeck", player.id);
		assertEquals(0., player.rating);
		assertEquals(0L, player.matchCount);
	}

	@Test
	void initialProperties() {
		final Player player = new Player("Els De Sagher", 132., 6L);
		assertEquals("Els De Sagher", player.id);
		assertEquals(132., player.rating);
		assertEquals(6L, player.matchCount);
	}
}
