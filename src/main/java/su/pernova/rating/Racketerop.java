package su.pernova.rating;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.util.Comparator.comparingDouble;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import su.pernova.rating.de.maeyer.DeMaeyerRatingSystem;

public class Racketerop {

	public static void main(final String... args) throws Exception {
		final Players players = new Players(new LinkedHashMap<>());
		Path playersSer = null;
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder()
				.setAbsorptionFactor(.1)
				.setNewPlayerRating(75.)
				.setNewPlayerPooledRating(125.)
				.build();
		for (final String arg : args) {
			final URL url = getSystemResource(arg);
			final URI uri = url != null ? url.toURI() : new URI(arg);
			if (arg.endsWith(".csv")) {
				players.registerAll(uri);
			} else if (arg.endsWith(".txt")) {
				for (Match match : new Matches(uri, players).matches) {
					System.out.println("Applying rating system to match: " + match);
					ratingSystem.apply(match);
				}
			} else if (arg.endsWith(".ser")) {
				playersSer = Path.of(uri);
				if (exists(playersSer)) {
					try (final ObjectInputStream objIn = new ObjectInputStream(newInputStream(playersSer))) {
						System.out.println("Loading players from: " + playersSer);
						players.registerAll((Players) objIn.readObject());
					}
				}
			}
		}
		try (final ObjectOutputStream objOut = new ObjectOutputStream(newOutputStream(playersSer))) {
			System.out.println("Writing players to: " + playersSer);
			objOut.writeObject(players);
		}
	}

	public static void printRatings(final PrintStream stream, final Players players, final Object subject) {
		stream.println(format("=== rating after %s ===", subject));
		Comparator<Player> orderByRating = comparingDouble(player -> player.rating);
		orderByRating = orderByRating.thenComparing(player -> player.id);
		orderByRating = orderByRating.reversed();
		final SortedSet<Player> sortedPlayers = new TreeSet<>(orderByRating);
		sortedPlayers.addAll(players.getPlayers());
		sortedPlayers.forEach(player -> stream.printf("%-30s: %.2f (/%d)%n", player.name, player.rating, player.matchCount));
		stream.println("================================================================================");
	}
}
