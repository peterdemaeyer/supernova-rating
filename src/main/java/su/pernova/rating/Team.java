package su.pernova.rating;

import static su.pernova.rating.RatingUtils.requireArrayNonNull;

import java.util.Arrays;

public class Team {

	public final Player[] players;

	public Team(final Player... players) {
		this.players = requireArrayNonNull(players, "players");
	}

	@Override
	public String toString() {
		return Arrays.toString(players);
	}
}
