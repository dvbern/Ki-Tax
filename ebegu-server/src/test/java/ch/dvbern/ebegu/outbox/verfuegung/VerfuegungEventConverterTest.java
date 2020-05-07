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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
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
import ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp;
import ch.dvbern.kibon.exchange.commons.types.Regelwerk;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import ch.dvbern.kibon.exchange.commons.verfuegung.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.KindDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.VerfuegungEventDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.ZeitabschnittDTO;
import com.spotify.hamcrest.pojo.IsPojo;
import org.junit.Assert;
import org.junit.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
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
	public void testEventConversion() {
		Verfuegung verfuegung = createVerfuegung();
		VerfuegungVerfuegtEvent event = converter.of(verfuegung)
			.orElseThrow(() -> new IllegalStateException("Test setup broken"));

		Betreuung betreuung = verfuegung.getBetreuung();
		Assert.assertNotNull(betreuung);
		Gesuchsperiode gesuchsperiode = verfuegung.getBetreuung().extractGesuchsperiode();
		String institutionId = betreuung.getInstitutionStammdaten().getInstitution().getId();
		String bgNummer = betreuung.getBGNummer();
		Gemeinde gemeinde = betreuung.extractGesuch().extractGemeinde();

		assertThat(event, is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(bgNummer))
			.where(ExportedEvent::getAggregateType, is("Verfuegung"))
			.where(ExportedEvent::getType, is("VerfuegungVerfuegt")))
		);

		//noinspection deprecation
		VerfuegungEventDTO specificRecord = AvroConverter.fromAvroBinary(event.getSchema(), event.getPayload());

		// Avro only serializes Instant with microsecond precision (opposed to nano)
		long expectedVerfuegtAm = TIMESTAMP_ERSTELLT.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

		assertThat(specificRecord, is(pojo(VerfuegungEventDTO.class)
			.where(VerfuegungEventDTO::getRefnr, is(bgNummer))
			.where(VerfuegungEventDTO::getInstitutionId, is(institutionId))
			.where(VerfuegungEventDTO::getVon, is(gesuchsperiode.getGueltigkeit().getGueltigAb()))
			.where(VerfuegungEventDTO::getBis, is(gesuchsperiode.getGueltigkeit().getGueltigBis()))
			.where(VerfuegungEventDTO::getVersion, is(0))
			.where(VerfuegungEventDTO::getVerfuegtAm, is(pojo(Instant.class)
				.where(Instant::toEpochMilli, is(expectedVerfuegtAm))))
			.where(VerfuegungEventDTO::getKind, is(pojo(KindDTO.class)
				.where(KindDTO::getVorname, is(KIND_VORNAME))
				.where(KindDTO::getNachname, is(KIND_NACHNAME))
				.where(KindDTO::getGeburtsdatum, is(KIND_GEBURTSDATUM))
			))
			.where(VerfuegungEventDTO::getGesuchsteller, is(pojo(GesuchstellerDTO.class)
				.where(GesuchstellerDTO::getVorname, is(GESUCHSTELLER_VORNAME))
				.where(GesuchstellerDTO::getNachname, is(GESUCHSTELLER_NACHNAME))
				.where(GesuchstellerDTO::getEmail, is(GESUCHSTELLER_MAIL))
			))
			.where(VerfuegungEventDTO::getBetreuungsArt, is(BetreuungsangebotTyp.KITA))
			.where(VerfuegungEventDTO::getGemeindeName, is(gemeinde.getName()))
			.where(VerfuegungEventDTO::getGemeindeBfsNr, is(gemeinde.getBfsNummer()))
			.where(VerfuegungEventDTO::getZeitabschnitte, is(contains(defaultZeitAbschnitt())))
			.where(VerfuegungEventDTO::getIgnorierteZeitabschnitte, is(empty()))
		));
	}

	@Test
	public void testRegelwerkConversion() {
		Verfuegung verfuegung = createVerfuegung();

		// setting non-default value
		verfuegung.getZeitabschnitte()
			.forEach(z -> z.setRegelwerk(ch.dvbern.ebegu.enums.Regelwerk.FEBR));

		VerfuegungVerfuegtEvent event = converter.of(verfuegung)
			.orElseThrow(() -> new IllegalStateException("Test setup broken"));

		//noinspection deprecation
		VerfuegungEventDTO specificRecord = AvroConverter.fromAvroBinary(event.getSchema(), event.getPayload());

		// expecting value from verfuegung
		assertThat(specificRecord.getZeitabschnitte(), everyItem(hasProperty("regelwerk", equalTo(Regelwerk.FEBR))));
	}

	@Nonnull
	private IsPojo<ZeitabschnittDTO> defaultZeitAbschnitt() {
		return pojo(ZeitabschnittDTO.class)
			.where(ZeitabschnittDTO::getVon, is(LocalDate.now()))
			.where(ZeitabschnittDTO::getBis, is(Constants.END_OF_TIME))
			.where(ZeitabschnittDTO::getVerfuegungNr, is(0))
			.where(ZeitabschnittDTO::getEffektiveBetreuungPct, comparesEqualTo(BigDecimal.TEN))
			.where(ZeitabschnittDTO::getAnspruchPct, is(50))
			.where(ZeitabschnittDTO::getVerguenstigtPct, comparesEqualTo(BigDecimal.TEN))
			.where(ZeitabschnittDTO::getVollkosten, comparesEqualTo(BigDecimal.ZERO))
			.where(ZeitabschnittDTO::getBetreuungsgutschein, comparesEqualTo(BigDecimal.ZERO))
			.where(ZeitabschnittDTO::getMinimalerElternbeitrag, comparesEqualTo(BigDecimal.ZERO))
			.where(ZeitabschnittDTO::getVerguenstigung, comparesEqualTo(BigDecimal.ZERO))
			.where(ZeitabschnittDTO::getVerfuegteAnzahlZeiteinheiten, comparesEqualTo(BigDecimal.ZERO))
			.where(ZeitabschnittDTO::getAnspruchsberechtigteAnzahlZeiteinheiten, comparesEqualTo(BigDecimal.ZERO))
			.where(ZeitabschnittDTO::getZeiteinheit, is(Zeiteinheit.DAYS))
			.where(ZeitabschnittDTO::getRegelwerk, is(Regelwerk.ASIV));
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
		Verfuegung verfuegung = new Verfuegung();

		VerfuegungZeitabschnitt defaultZeitabschnitt = TestDataUtil.createDefaultZeitabschnitt(verfuegung);
		defaultZeitabschnitt.initBGCalculationResult();
		verfuegung.getZeitabschnitte().add(defaultZeitabschnitt);
		verfuegung.setTimestampErstellt(TIMESTAMP_ERSTELLT);
		verfuegung.setBetreuung(betreuung);

		betreuung.setVerfuegung(verfuegung);
		return verfuegung;
	}
}
