package su.pernova.rating;

import static java.lang.ClassLoader.getSystemResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

class PlayersTest {

	@Test
	void players() throws URISyntaxException {
		final Players players = new Players(getSystemResource("players.txt").toURI());
		Player player5 = players.getPlayer("peter");
		assertEquals("Peter De Maeyer", player5.id);
		Player player4 = players.getPlayer("pdm");
		assertEquals("Peter De Maeyer", player4.id);
		Player player3 = players.getPlayer("Peter De Maeyer");
		assertEquals("Peter De Maeyer", player3.id);
		Player player2 = players.getPlayer("peter de maeyer");
		assertEquals("Peter De Maeyer", player2.id);
		Player player1 = players.getPlayer("pede");
		assertEquals("Peter De Maeyer", player1.id);
		Player player = players.getPlayer("jedh");
		assertEquals("Jens D'Hoine", player.id);
	}
}
