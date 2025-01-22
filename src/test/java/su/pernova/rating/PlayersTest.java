package su.pernova.rating;

import static java.lang.ClassLoader.getSystemResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URISyntaxException;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

class PlayersTest {

	@Test
	void players() throws Exception {
		final Players players = new Players(new LinkedHashMap<>());
		players.registerAll(getSystemResource("test-players.csv").toURI());
		Player player1 = players.getPlayer("1");
		assertEquals("1", player1.id);
		assertEquals("Peter De Maeyer", player1.name);
		Player player2 = players.getPlayer("KirstenR");
		assertEquals("2", player2.id);
		assertEquals("Kirsten Robberecht", player2.name);
		Player player5 = players.getPlayer("5");
		assertEquals("5", player5.id);
		assertEquals("Peter De Maeyer", player5.name);
	}
}
