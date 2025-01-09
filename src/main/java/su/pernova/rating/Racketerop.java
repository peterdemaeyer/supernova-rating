package su.pernova.rating;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.util.Comparator.comparingDouble;

import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import su.pernova.rating.de.maeyer.DeMaeyerRatingSystem;
import su.pernova.rating.elo.EloRatingSystem;

public class Racketerop {

	public static void main(String[] args) throws Exception {
		final Players players = new Players(getSystemResource("players.txt").toURI());
		final List<URI> tournaments = Arrays.asList(
				getSystemResource("racket-erop-2023-02-05.txt").toURI(),
				getSystemResource("racket-erop-2023-02-12.txt").toURI(),
				getSystemResource("racket-erop-2023-02-19.txt").toURI(),
				getSystemResource("racket-erop-2023-02-26.txt").toURI(),
				getSystemResource("racket-erop-2023-03-05.txt").toURI(),
				getSystemResource("racket-erop-2023-03-12.txt").toURI(),
				getSystemResource("racket-erop-2023-03-19.txt").toURI(),
				getSystemResource("racket-erop-2023-03-26.txt").toURI()
		);
		final RatingSystem ratingSystem = new DeMaeyerRatingSystem.Builder()
				.setAbsorptionFactor(.1)
				.setInitialRating(100.)
				.setInitialRatingExcess(100.)
				.build();
		for (URI tournament : tournaments) {
			for (Match match : new Matches(tournament, players).matches) {
				ratingSystem.apply(match);
			}
			printRatings(System.out, players, Paths.get(tournament.getPath()).getFileName());
			System.out.println(ratingSystem);
		}
	}

	private static void printRatings(PrintStream stream, Players players, Object subject) {
		stream.println(format("=== rating after %s ===", subject));
		Comparator<Player> orderByRating = comparingDouble(player -> player.rating);
		orderByRating = orderByRating.thenComparing(player -> player.id);
		orderByRating = orderByRating.reversed();
		final SortedSet<Player> sortedPlayers = new TreeSet<>(orderByRating);
		sortedPlayers.addAll(players.getPlayers());
		sortedPlayers.forEach(player -> stream.printf("%-30s: %.2f (/%d)%n", player.id, player.rating, player.matchCount));
	}
}
