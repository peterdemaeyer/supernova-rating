package su.pernova.rating.de.maeyer;

import java.io.Serializable;

import su.pernova.rating.Player;

public interface WeightPolicy extends Serializable {

	double weight(Player player);
}
