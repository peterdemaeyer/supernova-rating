package su.pernova.rating.de.maeyer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import su.pernova.rating.Player;

class MaxWeightPolicyTest implements WeightPolicyTest {

	@Override
	public MaxWeightPolicy newInstance() {
		return new MaxWeightPolicy(new AverageWeightPolicy(), new FixedWeightPolicy(.2));
	}

	@Test
	void weightIsMaxOfWeights() {
		final MaxWeightPolicy weightPolicy = new MaxWeightPolicy(new AverageWeightPolicy(), new FixedWeightPolicy(.2));
		assertEquals(1., weightPolicy.weight(new Player("Player 0", 0., 0L)));
		assertEquals(.25, weightPolicy.weight(new Player("Player 3", 0., 3L)));
		assertEquals(.2, weightPolicy.weight(new Player("Player 4", 0., 4L)));
		assertEquals(.2, weightPolicy.weight(new Player("Player N", 0., 374L)));	}
}
