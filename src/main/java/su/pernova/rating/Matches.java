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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Matches {

	public static void main(String[] args) {
		final Matcher matcher = MATCH_PATTERN.matcher(" [ DirkR, PeterDM    ] -  [ KirstenR, MarleenDC]= 51 - 32132514  ");
		if (matcher.matches()) {
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
			System.out.println(matcher.group(3));
			System.out.println(matcher.group(4));
			System.out.println(matcher.group(5));
			System.out.println(matcher.group(6));
		}
	}

	private static final Pattern MATCH_PATTERN = Pattern.compile("\\s*\\[\\s*(.*?)\\s*,\\s*(.*?)\\s*]\\s*-\\s*\\[\\s*(.*?)\\s*,\\s*(.*?)\\s*]\\s*=\\s*(\\d*?)\\s*-\\s*(\\d*?)\\s*");

	public final List<Match> matches = new ArrayList<>();

	public Matches(final URI uri, final Players players) throws IOException {
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream(), UTF_8))) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				final Matcher matcher = MATCH_PATTERN.matcher(line);
				if (matcher.matches()) {
					matches.add(Padel.newMatch(
							getPlayer(players, matcher.group(1)), getPlayer(players, matcher.group(2)),
							getPlayer(players, matcher.group(3)), getPlayer(players, matcher.group(4)),
							parseLong(matcher.group(5)), parseLong(matcher.group(6))
					));
				}
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
