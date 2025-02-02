package su.pernova.rating;

import static java.nio.file.Files.newInputStream;
import static java.util.Comparator.comparingDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ObjectInputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import su.pernova.racketerop.Racketerop;

class RacketeropTest {

	/**
	 * In 2023, it was spelled Racket-Erop.
	 * Since 2025, it is spelled Racketerop.
	 */
	@Test
	void racketErop2023(final @TempDir Path tempDir) throws Exception {
		final Path playersSer = tempDir.resolve("players.ser");
		final URI playersUri = playersSer.toUri();
		Racketerop.main(
				playersUri.toString(),
				"racket-erop-deelnemers-2023.csv",
				"racket-erop-uitslagen-2023-02-05.txt",
				"racket-erop-uitslagen-2023-02-12.txt",
				"racket-erop-uitslagen-2023-02-19.txt",
				"racket-erop-uitslagen-2023-02-26.txt",
				"racket-erop-uitslagen-2023-03-05.txt",
				"racket-erop-uitslagen-2023-03-12.txt",
				"racket-erop-uitslagen-2023-03-19.txt",
				"racket-erop-uitslagen-2023-03-26.txt"
		);
		final Players players = new Players(new LinkedHashMap<>());
		try (final ObjectInputStream objIn = new ObjectInputStream(newInputStream(playersSer))) {
			players.registerAll((Players) objIn.readObject());
		}
		Comparator<Player> sortedByRatingThenById = comparingDouble(player -> player.rating);
		sortedByRatingThenById = sortedByRatingThenById.thenComparing(player -> player.id);
		sortedByRatingThenById = sortedByRatingThenById.reversed();
		final SortedSet<Player> sortedPlayers = new TreeSet<>(sortedByRatingThenById);
		sortedPlayers.addAll(players.getPlayers());
		assertEquals("Hans Van den Brande", sortedPlayers.first().name);
		assertEquals("Maritsa Roesems", sortedPlayers.last().name);
	}
}
