package su.pernova.rating.de.maeyer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import su.pernova.rating.Player;

interface WeightPolicyTest {

	WeightPolicy newInstance();

	@ParameterizedTest
	@ArgumentsSource(PlayerArgumentsProvider.class)
	default void weightIsInNormalRange(Player player) {
		final double weight = newInstance().weight(player);
		assertTrue(weight >= 0.);
		assertTrue(weight <= 1.);
	}

	class PlayerArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					Arguments.of(new Player("Player 0/0", 0., 0L)),
					Arguments.of(new Player("Player 200/1", 200., 1L))
			);
		}
	}
}
