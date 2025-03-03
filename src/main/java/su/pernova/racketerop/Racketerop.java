package su.pernova.racketerop;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.requireNonNull;

import static su.pernova.rating.Players.computeShortname;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import su.pernova.rating.Match;
import su.pernova.rating.Matches;
import su.pernova.rating.Player;
import su.pernova.rating.Players;
import su.pernova.rating.de.maeyer.DeMaeyerRatingSystem;
import su.pernova.rating.de.maeyer.FixedWeightPolicy;

public class Racketerop {

	/**
	 * <ul>
	 *     <li>{@code --generate-matches}: generates matches given a {@code .csv} file.</li>
	 *     <li>{@code --update-ratings}: generates matches given a {@code .txt} file with match scores and a {@code .ser} file to load and save the serialized ratings from and to.</li>
	 *     <li>{@code *.csv}: at least one file with player data containing at least "Naam", "Voornaam", "Lidnummer". Player IDs are generated from "Voornaam" + initials of "Naam".</li>
	 *     <li>{@code *.txt}: at least one file with match scores in the format "[JohnD, JaneD] - [PerG, MarieF] = 5 - 9"</li>
	 *     <li>{@code *.ser}: a file containing the saved state of the population of players in the ranking system.</li>
	 * </ul>
	 * @param args various arguments in various order.
	 */
	public static void main(final String... args) throws Exception {
		final Players players = new Players(new LinkedHashMap<>());
		Path playersSer = null;
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder()
				.setAbsorptionFactor(.1)
				.setNewPlayerRating(75.)
				.setNewPlayerPooledRating(125.)
				.build();
		String option = null;
		for (final String arg : args) {
			if (arg.startsWith("--")) {
				option = arg;
			} else {
				final URL url = getSystemResource(arg);
				final URI uri = url != null ? url.toURI() : new URI(arg);
				if (arg.endsWith(".csv")) {
					if ("--generate-matches".equals(option)) {
						generateMatches(uri);
					} else {
						players.registerAll(uri);
					}
				} else if (arg.endsWith(".txt")) {
					for (Match match : new Matches(uri, players).matches) {
						System.out.println("Applying rating system to match: " + match);
						ratingSystem.apply(match);
					}
					printRatings(System.out, players, arg);
				} else if (arg.endsWith(".ser")) {
					playersSer = Path.of(uri);
					if (exists(playersSer)) {
						try (final ObjectInputStream objIn = new ObjectInputStream(newInputStream(playersSer))) {
							System.out.println("Loading players from: " + playersSer);
							players.registerAll((Players) objIn.readObject());
						}
					}
					printRatings(System.out, players, arg);
				}
			}
		}
		if ("--update-ratings".equals(option)) {
			try (final ObjectOutputStream objOut = new ObjectOutputStream(newOutputStream(playersSer))) {
				System.out.println("Writing players to: " + playersSer);
				objOut.writeObject(players);
			}
		}
	}

	private static void generateMatches(final URI uri) throws IOException {
		if (!uri.getPath().endsWith(".csv")) {
			throw new IllegalArgumentException("not a CSV resource");
		}
		final Map<Object, Player> playersByKey = new LinkedHashMap<>();
		final Map<String, List<Player>> playersByAvailability = new TreeMap<>();
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
			final int beschikbaarheidIndex = indexesByColumn.get("welke momenten ben je beschikbaar?");
			for (line = reader.readLine(); line != null; line = reader.readLine()) {
				final String[] columns = line.split(",");
				final String lidnummer = columns[lidnummerIndex];
				final Player[] newPlayer = new Player[1];
				final String[] namen = new String[2];
				final String beschikbaarheid = columns[beschikbaarheidIndex];
				final Player player = playersByKey.computeIfAbsent(lidnummer, ignored -> newPlayer[0] = new Player(
						lidnummer,
						(namen[0] = columns[voornaamIndex].trim()) + " " + (namen[1] = columns[naamIndex].trim())
				));
				playersByAvailability.computeIfAbsent(beschikbaarheid, availability -> new ArrayList<>()).add(player);
				if (newPlayer[0] != null) {
					final String shortname = computeShortname(namen);
					System.out.println("Registering new player: " + newPlayer[0] + " with shortname: " + shortname);
					playersByKey.putIfAbsent(shortname, newPlayer[0]);
				}
			}
		}
		for (Entry<String, List<Player>> entry : playersByAvailability.entrySet()) {
			System.out.println("==========");
			System.out.println(entry.getKey());
			System.out.println("==========");
			entry.getValue().forEach(player -> System.out.println(player.name));
		}
	}

	public static void printRatings(final PrintStream stream, final Players players, final Object subject) {
		stream.println(format("=== rating after %s ===", subject));
		Comparator<Player> orderByRating = comparingDouble(player -> player.rating);
		orderByRating = orderByRating.thenComparing(player -> player.id);
		orderByRating =
				orderByRating.reversed();
		final SortedSet<Player> sortedPlayers = new TreeSet<>(orderByRating);
		sortedPlayers.addAll(players.getPlayers());
		sortedPlayers.forEach(player -> stream.printf("%-30s: %.2f (/%d)%n", player.name, player.rating, player.matchCount));
		stream.println("================================================================================");
	}
}
