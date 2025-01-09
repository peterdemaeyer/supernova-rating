package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerTest {

	@Test
	void constructionThrowsWhenNameIsNull() {
		final NullPointerException expected = assertThrows(NullPointerException.class, () -> new Player(null));
		assertEquals("name is null", expected.getMessage());
	}

	@Test
	void equalityAndHashCode() {
		final Player player = new Player("Kris Pynaert");
		assertTrue(player.equals(player));
		assertEquals(0, player.compareTo(player));
		assertFalse(player.equals(null));
		assertFalse(player.equals(this));
		final Player equalPlayer = new Player(player.id);
		assertTrue(player.equals(equalPlayer));
		assertEquals(0, player.compareTo(player));
		assertTrue(equalPlayer.equals(player));
		final Player playerWithDifferentRank = new Player(player.id);
		playerWithDifferentRank.rating = player.rating + 1.;
		assertFalse(player.equals(playerWithDifferentRank));
		assertFalse(player.compareTo(playerWithDifferentRank) == 0);
		final Player playerWithDifferentName = new Player("Kirsten Robberecht");
		assertFalse(player.equals(playerWithDifferentName));
		assertFalse(player.compareTo(playerWithDifferentName) == 0);
	}

	@Test
	void comparisonBasedOnRating() {
		final Player player = new Player("Stef Degens");
		player.rating = 5.;
		final Player higherRankedPlayer = new Player("Kristien Van Hoof");
		higherRankedPlayer.rating = 6.;
		final Player lowerRankedPlayer = new Player("Bart Van den Steen");
		lowerRankedPlayer.rating = 4.;
		assertEquals(-1, player.compareTo(higherRankedPlayer));
		assertEquals(1, player.compareTo(lowerRankedPlayer));
		assertEquals(1, higherRankedPlayer.compareTo(player));
		assertEquals(1, higherRankedPlayer.compareTo(lowerRankedPlayer));
		assertEquals(-1, lowerRankedPlayer.compareTo(player));
		assertEquals(-1, lowerRankedPlayer.compareTo(higherRankedPlayer));
	}

	@Test
	void comparisonBasedOnName() {
		final Player playerS = new Player("Stef Degens");
		final Player playerK = new Player("Kristien Van Hoof");
		final Player playerB = new Player("Bart Van den Steen");
		assertEquals(1, playerS.compareTo(playerK));
		assertEquals(1, playerS.compareTo(playerB));
		assertEquals(-1, playerK.compareTo(playerS));
		assertEquals(1, playerK.compareTo(playerB));
		assertEquals(-1, playerB.compareTo(playerS));
		assertEquals(-1, playerB.compareTo(playerK));
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
