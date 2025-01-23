package su.pernova.rating;

import static java.nio.file.Files.newInputStream;

import java.io.ObjectInputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
		Racketerop.printRatings(System.out, players, "Racket-Erop 2023");
	}
}
