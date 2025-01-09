package su.pernova.rating.de.maeyer;

import su.pernova.rating.Player;

public interface WeightPolicy {

	double weight(Player player);
}
