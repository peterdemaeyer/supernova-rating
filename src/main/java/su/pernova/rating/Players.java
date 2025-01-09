package su.pernova.rating;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Players {

	private static final Pattern NAME_PATTERN = Pattern.compile("\\S+");

	private final List<Player> players = new ArrayList<>();

	private final Map<String, Player> playersByKey = new LinkedHashMap<>();

	public Players(URI uri) {
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream(), UTF_8))) {
			for (String name = reader.readLine(); name != null; name = reader.readLine()) {
				name = name.trim();
				if (name.isEmpty() || name.startsWith("#")) {
					// Skip empty lines and comments.
					continue;
				}
				final String nameKey = name.toLowerCase();
				final Player player = new Player(name);
				if (playersByKey.put(nameKey, player) != null) {
					throw new IllegalArgumentException("duplicate name key: " + nameKey);
				}
				players.add(player);
				final Matcher matcher = NAME_PATTERN.matcher(name);
				final StringBuilder initials = new StringBuilder();
				final StringBuilder doubleInitials = new StringBuilder();
				boolean firstName = true;
				while (matcher.find()) {
					final String group = matcher.group().replaceAll("['`-]", "");
					if (firstName) {
						final String firstNameKey = group.toLowerCase();
						final Player oldPlayer = playersByKey.put(firstNameKey, player);
						if (oldPlayer != null) {
							System.out.println(format("Omitting ambiguous first name key: \"%s\" [\"%s\", \"%s\"].", firstNameKey, oldPlayer, player));
							playersByKey.remove(firstNameKey);
						}
						firstName = false;
					}
					initials.append(group.charAt(0));
					if (doubleInitials.length() < 4) {
						doubleInitials.append(group.charAt(0)).append(group.charAt(1));
					}
				}
				final String initialsKey = initials.toString().toLowerCase();
				Player oldPlayer = playersByKey.put(initialsKey, player);
				if (oldPlayer != null) {
					System.out.println(format("Omitting ambiguous initials key: \"%s\" [\"%s\", \"%s\"].", initialsKey, oldPlayer, player));
					playersByKey.remove(initialsKey);
				}
				final String doubleInitialsKey = doubleInitials.toString().toLowerCase();
				oldPlayer = playersByKey.put(doubleInitialsKey, player);
				if (oldPlayer != null) {
					System.out.println(format("Omitting ambiguous double initials key: \"%s\" [\"%s\", \"%s\"].", doubleInitialsKey, oldPlayer, player));
					playersByKey.remove(doubleInitialsKey);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Player getPlayer(String key) {
		return playersByKey.get(key.toLowerCase());
	}
}
