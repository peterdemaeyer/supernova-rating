package su.pernova.rating.de.maeyer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import su.pernova.rating.Player;

public class AverageWeightPolicyTest implements WeightPolicyTest {

	@Override
	public AverageWeightPolicy newInstance() {
		return new AverageWeightPolicy();
	}

	@Test
	void weightIsAverageOfMatchCount() {
		AverageWeightPolicy weightPolicy = new AverageWeightPolicy();
		assertEquals(1., weightPolicy.weight(new Player("Player 0", 0., 0L)));
		assertEquals(.5, weightPolicy.weight(new Player("Player 1", 0., 1L)));
		assertEquals(1. / 3., weightPolicy.weight(new Player("Player 2", 0., 2L)));
		assertEquals(.25, weightPolicy.weight(new Player("Player 3", 0., 3L)));
		assertEquals(1. / 375., weightPolicy.weight(new Player("Player N", 0., 374L)));
	}
}
