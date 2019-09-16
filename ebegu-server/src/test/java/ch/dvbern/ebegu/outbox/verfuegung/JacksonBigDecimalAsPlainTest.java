/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.outbox.verfuegung;

import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

import static com.spotify.hamcrest.jackson.JsonMatchers.jsonBigDecimal;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JacksonBigDecimalAsPlainTest {

	/**
	 * There is a Bug in Jackson, not properly serializing BigDecimals to plainText.
	 *
	 * @see
	 * <a href="https://github.com/FasterXML/jackson-databind/issues/2230">https://github.com/FasterXML/jackson-databind/issues/2230</a>
	 */
	@Ignore
	@Test
	public void testBigDecimal() {
		// should use BigDecimal::toPlainString
		ObjectMapper mapper = new ObjectMapper().configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

		Trial test = new Trial(BigDecimal.TEN);
		JsonNode actual = mapper.valueToTree(test);

		// verify that TEN to plainString is actually "10"
		assertThat(BigDecimal.TEN.toPlainString(), is("10"));

		// this fails with Jackson 2.9.9. value is "1E+1"
		assertThat(actual, jsonObject().where("value", jsonBigDecimal(BigDecimal.TEN)));
	}

	private static class Trial {
		private final BigDecimal value;

		public Trial(BigDecimal value) {
			this.value = value;
		}

		public BigDecimal getValue() {
			return value;
		}
	}
}
