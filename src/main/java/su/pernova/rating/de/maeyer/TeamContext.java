package su.pernova.rating.de.maeyer;

import java.util.stream.Stream;

import su.pernova.rating.Team;

public class TeamContext {

	double sumOfWeights;

	double sumOfWeighedRatings;

	final PlayerContext[] playerContexts;

	public TeamContext(Team team) {
		playerContexts = Stream.of(team.players).map(PlayerContext::new).toArray(PlayerContext[]::new);
	}
}
