package su.pernova.rating.de.maeyer;

class FixedWeightPolicyTest implements WeightPolicyTest {

	@Override
	public FixedWeightPolicy newInstance() {
		return new FixedWeightPolicy(.2);
	}
}
