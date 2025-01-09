package su.pernova.rating;

import static java.lang.Long.parseLong;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Matches {

	public final List<Match> matches = new ArrayList<>();

	public Matches(URI uri, Players players) throws IOException {
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream(), UTF_8))) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				final String[] components = line.split("[+\\-=]");
				if (components.length != 6) {
					throw new IllegalArgumentException("# components is not 6: " + Arrays.toString(components));
				}
				matches.add(Padel.newMatch(
						getPlayer(players, components[0]), getPlayer(players, components[1]),
						getPlayer(players, components[2]), getPlayer(players, components[3]),
						parseLong(components[4]), parseLong(components[5])
				));
			}
		}
	}

	private static Player getPlayer(Players players, String key) {
		final Player player = players.getPlayer(key);
		if (player == null) {
			throw new NullPointerException("player is null for key: " + key);
		}
		return player;
	}
}
