package su.pernova.rating;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

public class Team {

	public final Player[] players;

	public Team(Player... players) {
		this.players = requireNonNull(players, "players is null");
	}

	@Override
	public String toString() {
		return Arrays.toString(players);
	}
}
