package su.pernova.rating.de.maeyer;

import su.pernova.rating.Player;

public class PlayerContext {

	final Player player;

	double weight;

	double weighedRating;

	public PlayerContext(Player player) {
		this.player = player;
	}
}
