package su.pernova.rating;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Players implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<Object, Player> playersByKey;

	public Players(final Map<Object, Player> playersByKey) {
		this.playersByKey = requireNonNull(playersByKey, "map of players by key is null");
	}

	public Collection<Player> getPlayers() {
		return playersByKey.values();
	}

	public Player getPlayer(Object key) {
		return playersByKey.get(key);
	}

	public void registerAll(final URI uri) throws IOException {
		if (!uri.getPath().endsWith(".csv")) {
			throw new IllegalArgumentException("not a CSV resource");
		}
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream(), UTF_8))) {
			String line = reader.readLine();
			final Map<String, Integer> indexesByColumn = new LinkedHashMap<>();
			if (line != null) {
				int index = 0;
				for (final String column : line.split(",")) {
					indexesByColumn.put(column.trim().toLowerCase(), index++);
				}
			}
			final int lidnummerIndex = indexesByColumn.get("lidnummer");
			final int voornaamIndex = indexesByColumn.get("voornaam");
			final int naamIndex = indexesByColumn.get("naam");
			for (line = reader.readLine(); line != null; line = reader.readLine()) {
				final String[] columns = line.split(",");
				final String lidnummer = columns[lidnummerIndex];
				final Player[] newPlayer = new Player[1];
				final String[] namen = new String[2];
				playersByKey.computeIfAbsent(lidnummer, ignored -> newPlayer[0] = new Player(
						lidnummer,
						(namen[0] = columns[voornaamIndex].trim()) + " " + (namen[1] = columns[naamIndex].trim())
				));
				if (newPlayer[0] != null) {
					final String shortname = computeShortname(namen);
					System.out.println("Registering new player: " + newPlayer[0] + " with shortname: " + shortname);
					playersByKey.putIfAbsent(shortname, newPlayer[0]);
				}
			}
		}
	}

	private static String computeShortname(final String[] names) {
		final StringBuilder builder = new StringBuilder(names[0]);
		for (final String part : names[1].split(" ")) {
			builder.append(part.charAt(0));
		}
		return builder.toString();
	}
}
