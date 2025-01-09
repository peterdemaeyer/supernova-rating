package su.pernova.rating;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static com.google.gson.FormattingStyle.PRETTY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

class AverageRatingCombinerTest {

	private final Player player0 = new Player("0", 0., 10L);

	private final Player player1 = new Player("1", 1., 10L);

	@Test
	void combinedRatingIsAverageOfRatings() {
		final AverageRatingCombiner ratingCombiner = new AverageRatingCombiner();
		assertEquals(0., ratingCombiner.combineRatings(player0));
		assertEquals(.5, ratingCombiner.combineRatings(player0, player1));
		assertEquals(1. / 3., ratingCombiner.combineRatings(player0, player0, player1));
		assertEquals(1., ratingCombiner.combineRatings(player1, player1));
	}

	@Test
	void serialization() {
		final Gson gson = new GsonBuilder()
				.setVersion(1.0)
				.setFormattingStyle(PRETTY)
				.setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
				.create();
		final AverageRatingCombiner ratingCombiner = new AverageRatingCombiner();
		final String json = gson.toJson(ratingCombiner);
		System.out.println(json);
		final AverageRatingCombiner clone = gson.fromJson(json, ratingCombiner.getClass());
		System.out.println(clone);
	}
}
