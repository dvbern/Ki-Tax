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

package ch.dvbern.ebegu.outbox;

import javax.xml.bind.DatatypeConverter;

import ch.dvbern.kibon.exchange.commons.verfuegung.VerfuegungEventDTO;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.Test;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.hasMessage;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.hasMessageThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class AvroConverterTest {

	private static final byte[] TEST_DATA_SCHEMA1 = DatatypeConverter.parseHexBinary(
		"0A53696D6F6E0C57C3A46C7469DAFC010C4461676D61720C57C3A46C7469023274657374406D61696C6275636B65742E64766265726E2E6368002232302E3030303232382E3030352E312E324831623666343736662D653066352D343338302D396566362D383336643638383835336133D6A002AEA60200B0D7FD8CADEDCB0518D6A00292A102000200280200020002000200020002000401900094A102CEA1020002002802000200020002000200020004019000D0A1028CA20200020028020002000200020002000200040190008EA202C8A2020002002802000200020002000200020004019000CAA20286A302000200280200020002000200020002000401900088A302C4A3020002002802000200020002000200020004019000C6A302FCA30200040FA0C801040FA006030D4006011B75020006011B750403200407D000FEA302BAA40200040FA0C801040FA006030D4006011B75020006011B750403200407D000BCA402F6A40200040FA0C801040FA006030D4006011B75020006011B750403200407D000F8A402B4A50200040FA0C801040FA006030D4006011B75020006011B750403200407D000B6A502F0A50200040FA0C801040FA006030D4006011B75020006011B750403200407D000F2A502AEA60200040FA0C801040FA006030D4006011B75020006011B750403200407D0000000");

	// VerfuegungEventDTO without gemeindeBfsNr nor gemeindeName
	private static final Schema SCHEMA1 =
		new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"VerfuegungEventDTO\",\"namespace\":\"ch.dvbern"
			+ ".kibon"
			+ ".exchange.commons.verfuegung\",\"fields\":[{\"name\":\"kind\",\"type\":{\"type\":\"record\","
			+ "\"name\":\"KindDTO\",\"fields\":[{\"name\":\"vorname\",\"type\":{\"type\":\"string\",\"avro.java"
			+ ".string\":\"String\"}},{\"name\":\"nachname\",\"type\":{\"type\":\"string\",\"avro.java"
			+ ".string\":\"String\"}},{\"name\":\"geburtsdatum\",\"type\":{\"type\":\"int\","
			+ "\"logicalType\":\"date\"}}]}},"
			+ "{\"name\":\"gesuchsteller\",\"type\":{\"type\":\"record\",\"name\":\"GesuchstellerDTO\","
			+ "\"fields\":[{\"name\":\"vorname\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},"
			+ "{\"name\":\"nachname\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},"
			+ "{\"name\":\"email\","
			+ "\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null}]}},"
			+ "{\"name\":\"betreuungsArt\",\"type\":{\"type\":\"enum\",\"name\":\"BetreuungsangebotTyp\","
			+ "\"namespace\":\"ch.dvbern.kibon.exchange.commons.types\",\"symbols\":[\"KITA\",\"TAGESSCHULE\","
			+ "\"TAGESFAMILIEN\",\"FERIENINSEL\"]}},{\"name\":\"refnr\",\"type\":{\"type\":\"string\",\"avro.java"
			+ ".string\":\"String\"}},{\"name\":\"institutionId\",\"type\":{\"type\":\"string\",\"avro.java"
			+ ".string\":\"String\"}},{\"name\":\"von\",\"type\":{\"type\":\"int\",\"logicalType\":\"date\"}},"
			+ "{\"name\":\"bis\",\"type\":{\"type\":\"int\",\"logicalType\":\"date\"}},{\"name\":\"version\","
			+ "\"type\":\"int\"},{\"name\":\"verfuegtAm\",\"type\":{\"type\":\"long\","
			+ "\"logicalType\":\"timestamp-micros\"}},{\"name\":\"zeitabschnitte\",\"type\":{\"type\":\"array\","
			+ "\"items\":{\"type\":\"record\",\"name\":\"ZeitabschnittDTO\",\"fields\":[{\"name\":\"von\","
			+ "\"type\":{\"type\":\"int\",\"logicalType\":\"date\"}},{\"name\":\"bis\",\"type\":{\"type\":\"int\","
			+ "\"logicalType\":\"date\"}},{\"name\":\"verfuegungNr\",\"type\":\"int\"},"
			+ "{\"name\":\"effektiveBetreuungPct\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\","
			+ "\"precision\":19,\"scale\":2}},{\"name\":\"anspruchPct\",\"type\":\"int\"},"
			+ "{\"name\":\"verguenstigtPct\","
			+ "\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},"
			+ "{\"name\":\"vollkosten\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":19,"
			+ "\"scale\":2}},{\"name\":\"betreuungsgutschein\",\"type\":{\"type\":\"bytes\","
			+ "\"logicalType\":\"decimal\","
			+ "\"precision\":19,\"scale\":2}},{\"name\":\"minimalerElternbeitrag\",\"type\":{\"type\":\"bytes\","
			+ "\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},{\"name\":\"verguenstigung\","
			+ "\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},"
			+ "{\"name\":\"verfuegteAnzahlZeiteinheiten\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\","
			+ "\"precision\":19,\"scale\":2}},{\"name\":\"anspruchsberechtigteAnzahlZeiteinheiten\","
			+ "\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},"
			+ "{\"name\":\"zeiteinheit\",\"type\":{\"type\":\"enum\",\"name\":\"Zeiteinheit\",\"namespace\":\"ch"
			+ ".dvbern"
			+ ".kibon.exchange.commons.types\",\"symbols\":[\"DAYS\",\"HOURS\"]}}]}},\"default\":[]},"
			+ "{\"name\":\"ignorierteZeitabschnitte\",\"type\":{\"type\":\"array\",\"items\":\"ZeitabschnittDTO\"},"
			+ "\"default\":[]}]}");

	// VerfuegungEventDTO with gemeindeBfsNr and gemeindeName default values (VerfuegungEventDTO.getClassSchema() does
	// not have defaults)
	private static final Schema SCHEMA2 = new Schema.Parser().parse(
		"{\"type\":\"record\",\"name\":\"VerfuegungEventDTO\",\"namespace\":\"ch.dvbern.kibon.exchange.commons"
			+ ".verfuegung\",\"fields\":[{\"name\":\"kind\",\"type\":{\"type\":\"record\",\"name\":\"KindDTO\","
			+ "\"fields\":[{\"name\":\"vorname\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},"
			+ "{\"name\":\"nachname\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},"
			+ "{\"name\":\"geburtsdatum\",\"type\":{\"type\":\"int\",\"logicalType\":\"date\"}}]}},"
			+ "{\"name\":\"gesuchsteller\",\"type\":{\"type\":\"record\",\"name\":\"GesuchstellerDTO\","
			+ "\"fields\":[{\"name\":\"vorname\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},"
			+ "{\"name\":\"nachname\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},"
			+ "{\"name\":\"email\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],"
			+ "\"default\":null}]}},{\"name\":\"betreuungsArt\",\"type\":{\"type\":\"enum\","
			+ "\"name\":\"BetreuungsangebotTyp\",\"namespace\":\"ch.dvbern.kibon.exchange.commons.types\","
			+ "\"symbols\":[\"KITA\",\"TAGESSCHULE\",\"TAGESFAMILIEN\",\"FERIENINSEL\"]}},{\"name\":\"refnr\","
			+ "\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"institutionId\","
			+ "\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"von\","
			+ "\"type\":{\"type\":\"int\",\"logicalType\":\"date\"}},{\"name\":\"bis\",\"type\":{\"type\":\"int\","
			+ "\"logicalType\":\"date\"}},{\"name\":\"version\",\"type\":\"int\"},{\"name\":\"verfuegtAm\","
			+ "\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-micros\"}},{\"name\":\"gemeindeBfsNr\","
			+ "\"type\":\"long\",\"default\":1},{\"name\":\"gemeindeName\",\"type\":{\"type\":\"string\",\"avro.java"
			+ ".string\":\"String\"},\"default\":\"TEST\"},{\"name\":\"zeitabschnitte\",\"type\":{\"type\":\"array\","
			+ "\"items\":{\"type\":\"record\",\"name\":\"ZeitabschnittDTO\",\"fields\":[{\"name\":\"von\","
			+ "\"type\":{\"type\":\"int\",\"logicalType\":\"date\"}},{\"name\":\"bis\",\"type\":{\"type\":\"int\","
			+ "\"logicalType\":\"date\"}},{\"name\":\"verfuegungNr\",\"type\":\"int\"},"
			+ "{\"name\":\"effektiveBetreuungPct\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\","
			+ "\"precision\":19,\"scale\":2}},{\"name\":\"anspruchPct\",\"type\":\"int\"},"
			+ "{\"name\":\"verguenstigtPct\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\","
			+ "\"precision\":19,\"scale\":2}},{\"name\":\"vollkosten\",\"type\":{\"type\":\"bytes\","
			+ "\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},{\"name\":\"betreuungsgutschein\","
			+ "\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},"
			+ "{\"name\":\"minimalerElternbeitrag\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\","
			+ "\"precision\":19,\"scale\":2}},{\"name\":\"verguenstigung\",\"type\":{\"type\":\"bytes\","
			+ "\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},{\"name\":\"verfuegteAnzahlZeiteinheiten\","
			+ "\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},"
			+ "{\"name\":\"anspruchsberechtigteAnzahlZeiteinheiten\",\"type\":{\"type\":\"bytes\","
			+ "\"logicalType\":\"decimal\",\"precision\":19,\"scale\":2}},{\"name\":\"zeiteinheit\","
			+ "\"type\":{\"type\":\"enum\",\"name\":\"Zeiteinheit\",\"namespace\":\"ch.dvbern.kibon.exchange.commons"
			+ ".types\",\"symbols\":[\"DAYS\",\"HOURS\"]}}]}},\"default\":[]},{\"name\":\"ignorierteZeitabschnitte\","
			+ "\"type\":{\"type\":\"array\",\"items\":\"ZeitabschnittDTO\"},\"default\":[]}]}");

	/**
	 * It should always be possible to create a generic record for a payload matching the writer's schema.
	 */
	@Test
	public void testGenericRecordCreationMatch() {
		GenericRecord record = AvroConverter.fromAvroBinaryGeneric(SCHEMA1, TEST_DATA_SCHEMA1);
		assertThat(record, is(notNullValue()));
	}

	/**
	 * Decoding with a non-matching schema fails, because the deserializer does not know how to map unknown fields.
	 */
	@Test
	public void testGenericRecordCreationMismatch() {
		//noinspection ResultOfMethodCallIgnored
		catchException(() -> AvroConverter.fromAvroBinaryGeneric(SCHEMA2, TEST_DATA_SCHEMA1));

		assertThat(caughtException(), allOf(
			instanceOf(IllegalStateException.class),
			hasMessage("failed converting from avro")
		));
	}

	/**
	 * This is a tricky one, and the original cause of a debugging session, leading to some important insights:
	 * when using SpecificRecords, the actual DTO class is eventually instanciated. Since we have an updated version
	 * of VerfuegungEventDTO in the classpath, which does not match SCHEMA1, the conversion fails, because avro assumes
	 * that writer and reader schema are identical (in which case everything works is perfectly fine).
	 */
	@Test
	public void testSpecificRecordWithMismatch() {
		//noinspection deprecation,ResultOfMethodCallIgnored
		catchException(() -> AvroConverter.fromAvroBinary(SCHEMA1, TEST_DATA_SCHEMA1));

		assertThat(caughtException(), allOf(
			instanceOf(IllegalStateException.class),
			hasMessage("failed converting from avro")
		));
	}

	/**
	 * This tast case simulates a consumer with a compatible schema.
	 */
	@Test
	public void testSpecificRecordWithDefaultValues() {
		SpecificRecordBase record = AvroConverter.fromAvroBinary(SCHEMA1, SCHEMA2, TEST_DATA_SCHEMA1);

		assertThat(record, is(instanceOf(VerfuegungEventDTO.class)));
	}

	/**
	 * This case fails, because the schema of VerfuegungEventDTO has no default values for the fields gemeindeName and
	 * gemeindeBfsNr. Thus, it does not know what to do when reading serialized data without these values.
	 */
	@Test
	public void testSpecificRecordWithoutDefaultValues() {
		Schema classSchema = VerfuegungEventDTO.getClassSchema();
		//noinspection unused,ResultOfMethodCallIgnored
		catchException(() -> AvroConverter.fromAvroBinary(SCHEMA1, classSchema, TEST_DATA_SCHEMA1));

		assertThat(caughtException(), allOf(
			instanceOf(IllegalStateException.class),
			hasMessage("failed converting from avro"),
			hasProperty(
				"cause",
				hasMessageThat(containsString(
					"Found ch.dvbern.kibon.exchange.commons.verfuegung.VerfuegungEventDTO, expecting ch.dvbern.kibon"
						+ ".exchange.commons.verfuegung.VerfuegungEventDTO, missing required field gemeinde")))
		));
	}
}
