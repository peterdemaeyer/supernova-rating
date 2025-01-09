package su.pernova.rating;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MatchTest {

	private final Team team1 = new Team(new Player("David"), new Player("Bart"));

	private final Team team2 = new Team(new Player("Kirsten"), new Player("Karin"));

	private final Set[] sets = {new Set(6L, 2L), new Set(6L, 3L)};

	@Test
	void constructionThrowsWhenArrayOfTeamsIsNull() {
		assertEquals("array of teams is null",
				assertThrows(NullPointerException.class,
						() -> new Match(null, sets))
						.getMessage());
	}

	@Test
	void constructionThrowsWhenArrayOfSetsIsNullOrEmpty() {
		assertEquals("array of sets is null",
				assertThrows(NullPointerException.class,
						() -> new Match(new Team[]{ team1, team2 }, (Set[]) null)).getMessage());
		assertEquals("array of sets is empty",
				assertThrows(IllegalArgumentException.class,
						() -> new Match(new Team[] { team1, team2 } )).getMessage());
	}

	@Test
	void stringValue() {
		assertEquals("[David, Bart] - [Kirsten, Karin]=[6-2, 6-3]",
				new Match(new Team[] { team1, team2 }, sets).toString());
	}

	@Test
	void fields() {
		final Match match = new Match(new Team[] { team1, team2 } , sets);
		assertSame(team1, match.teams[0]);
		assertSame(team2, match.teams[1]);
		assertSame(sets, match.sets);
	}
}
