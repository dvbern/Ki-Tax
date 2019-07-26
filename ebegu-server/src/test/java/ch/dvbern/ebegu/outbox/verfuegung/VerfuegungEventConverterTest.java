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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.ObjectMapperUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.spotify.hamcrest.jackson.IsJsonObject;
import org.junit.Test;

import static com.spotify.hamcrest.jackson.JsonMatchers.jsonArray;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonBigDecimal;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonInt;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonObject;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonText;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class VerfuegungEventConverterTest {

	private static final LocalDateTime TIMESTAMP_ERSTELLT = LocalDateTime.of(2019, 1, 2, 3, 4, 5, 6);

	private static final LocalDate KIND_GEBURTSDATUM = LocalDate.of(2019, 1, 20);
	private static final String KIND_NACHNAME = "b";
	private static final String KIND_VORNAME = "a";

	private static final String GESUCHSTELLER_VORNAME = "A";
	private static final String GESUCHSTELLER_NACHNAME = "B";
	private static final String GESUCHSTELLER_MAIL = "foo@bar.com";

	@Nonnull
	private final VerfuegungEventConverter converter = new VerfuegungEventConverter();

	@Test
	public void testLocalDateTime() {
		assertThat(TIMESTAMP_ERSTELLT.toString(), is("2019-01-02T03:04:05.000000006"));
	}

	@Test
	public void testLocalDate() {
		assertThat(KIND_GEBURTSDATUM.toString(), is("2019-01-20"));
	}

	@Test
	public void testEventConversion() throws IOException {
		Verfuegung verfuegung = createVerfuegung();
		VerfuegungVerfuegtEvent event = converter.of(verfuegung);

		Betreuung betreuung = verfuegung.getBetreuung();
		Gesuchsperiode gesuchsperiode = verfuegung.getBetreuung().extractGesuchsperiode();
		String institutionId = betreuung.getInstitutionStammdaten().getInstitution().getId();
		String bgNummer = betreuung.getBGNummer();

		assertThat(event, is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(bgNummer))
			.where(ExportedEvent::getAggregateType, is("Verfuegung"))
			.where(ExportedEvent::getType, is("VerfuegungVerfuegt")))
		);

		JsonNode jsonNode = ObjectMapperUtil.MAPPER.readTree(event.getPayload());

		assertThat(jsonNode, is(jsonObject()
			.where("refnr", is(jsonText(bgNummer)))
			.where("institutionId", is(jsonText(institutionId)))
			.where("von", is(jsonText(gesuchsperiode.getGueltigkeit().getGueltigAb().toString())))
			.where("bis", is(jsonText(gesuchsperiode.getGueltigkeit().getGueltigBis().toString())))
			.where("version", is(jsonInt(0)))
			.where("verfuegtAm", is(jsonText(TIMESTAMP_ERSTELLT.toString())))
			.where("kind", is(jsonObject()
				.where("vorname", is(jsonText(KIND_VORNAME)))
				.where("nachname", is(jsonText(KIND_NACHNAME)))
				.where("geburtsdatum", is(jsonText(KIND_GEBURTSDATUM.toString())))
			))
			.where("gesuchsteller", is(jsonObject()
				.where("vorname", is(jsonText(GESUCHSTELLER_VORNAME)))
				.where("nachname", is(jsonText(GESUCHSTELLER_NACHNAME)))
				.where("email", is(jsonText(GESUCHSTELLER_MAIL)))
			))
			.where("betreuungsArt", is(jsonText("KITA")))
			.where("zeitabschnitte", is(jsonArray(contains(defaultZeitAbschnitt()))))
			.where("ignorierteZeitabschnitte", is(jsonArray(is(empty()))))
		));
	}

	@Nonnull
	private IsJsonObject defaultZeitAbschnitt() {
		return jsonObject()
			.where("von", is(jsonText(LocalDate.now().toString())))
			.where("bis", is(jsonText(Constants.END_OF_TIME.toString())))
			.where("verfuegungNr", is(jsonInt(0)))
			.where("effektiveBetreuungPct", is(jsonBigDecimal(comparesEqualTo(BigDecimal.TEN))))
			.where("anspruchPct", is(jsonInt(50)))
			.where("verguenstigtPct", is(jsonBigDecimal(comparesEqualTo(BigDecimal.TEN))))
			.where("vollkosten", is(jsonBigDecimal(comparesEqualTo(BigDecimal.ZERO))))
			.where("betreuungsgutschein", is(jsonBigDecimal(comparesEqualTo(BigDecimal.ZERO))))
			.where("minimalerElternbeitrag", is(jsonBigDecimal(comparesEqualTo(BigDecimal.ZERO))))
			.where("verguenstigung", is(jsonBigDecimal(comparesEqualTo(BigDecimal.ZERO))));
	}

	@Nonnull
	public static Verfuegung createVerfuegung() {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();

		GesuchstellerContainer gesuchsteller1 = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuchsteller1.getGesuchstellerJA().setVorname(GESUCHSTELLER_VORNAME);
		gesuchsteller1.getGesuchstellerJA().setNachname(GESUCHSTELLER_NACHNAME);
		gesuchsteller1.getGesuchstellerJA().setMail(GESUCHSTELLER_MAIL);
		gesuch.setGesuchsteller1(gesuchsteller1);

		KindContainer kind = betreuung.getKind();
		Kind kindJA = kind.getKindJA();

		kindJA.setVorname(KIND_VORNAME);
		kindJA.setNachname(KIND_NACHNAME);
		kindJA.setGeburtsdatum(KIND_GEBURTSDATUM);

		kind.setKindNummer(1);
		kind.setGesuch(gesuch);
		Verfuegung verfuegung = new Verfuegung(betreuung);

		VerfuegungZeitabschnitt defaultZeitabschnitt = TestDataUtil.createDefaultZeitabschnitt(verfuegung);
		verfuegung.getZeitabschnitte().add(defaultZeitabschnitt);
		verfuegung.setTimestampErstellt(TIMESTAMP_ERSTELLT);

		return verfuegung;
	}
}
