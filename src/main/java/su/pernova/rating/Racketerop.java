package su.pernova.rating;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.newOutputStream;
import static java.util.Comparator.comparingDouble;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import su.pernova.rating.de.maeyer.DeMaeyerRatingSystem;

public class Racketerop {

	public static void main(String[] args) throws Exception {
		URL url = getSystemResource("players.ser");
		Players players;
		if (url != null) {
			try (final ObjectInputStream objIn = new ObjectInputStream(url.openStream())) {
				System.out.println("Loading players from: " + url);
				players = (Players) objIn.readObject();
			}
		} else {
			url = Paths.get("players.ser").toUri().toURL();
			players = new Players(new LinkedHashMap<>());
		}
		final DeMaeyerRatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder()
				.setAbsorptionFactor(.1)
				.setInitialRating(75.)
				.setInitialRatingExcess(125.)
				.build();
		for (final String arg : args) {
			final URI uri = getSystemResource(arg).toURI();
			if (arg.endsWith(".csv")) {
				players.registerAll(uri);
			} else if (arg.endsWith(".txt")) {
				for (Match match : new Matches(uri, players).matches) {
					System.out.println("Applying rating system to match: " + match);
					ratingSystem.apply(match);
				}
				printRatings(System.out, players, uri);
			}
		}
		try (final ObjectOutputStream objOut = new ObjectOutputStream(newOutputStream(Paths.get(url.toURI())))) {
			System.out.println("Writing players to: " + url);
			objOut.writeObject(players);
		}
	}

	private static double computeRatingPool(final DeMaeyerRatingSystem ratingSystem, final Players players) {
		double ratingPool = ratingSystem.ratingPoolExcess;
		for (final Player player : players.getPlayers()) {
			if (player.matchCount > 0L) {
				ratingPool += player.rating;
			}
		}
		return ratingPool;
	}

	private static void printRatings(PrintStream stream, Players players, Object subject) {
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
