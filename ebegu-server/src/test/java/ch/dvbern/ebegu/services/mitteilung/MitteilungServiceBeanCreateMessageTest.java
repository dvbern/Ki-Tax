/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.mitteilung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSSTUNDEN_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.PENSUM_ANZEIGE_TYP;
import static ch.dvbern.ebegu.util.Constants.DEUTSCH_LOCALE;
import static java.util.Objects.requireNonNull;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(EasyMockExtension.class)
class MitteilungServiceBeanCreateMessageTest extends EasyMockSupport {

	@Mock
	private EinstellungService einstellungService;

	@TestSubject
	private final MitteilungServiceBean mitteilungServiceBean = new MitteilungServiceBean();

	@Test
	void emptyWhenNoPensen() {
		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT);

		assertThat(result, Matchers.emptyString());
	}

	@Test
	void concatWithNewline() {
		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, createPensum(), createPensum());

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 75%, monatliche Betreuungskosten: CHF 1’230.35\n"
				+ "Pensum 2 von 01.01.2024 bis 29.08.2024: 75%, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@Test
	void percentage() {
		BetreuungsmitteilungPensum pensum = createPensum();
		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 75%, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@Test
	void percentageWithMahlzeitenVerguenstigungEnabled() {
		BetreuungsmitteilungPensum pensum = createPensum();
		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, true, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 75%, monatliche Betreuungskosten: CHF 1’230.35, monatliche "
				+ "Hauptmahlzeiten: 5 à CHF 7, monatliche Nebenmahlzeiten: 9.75 à CHF 0.35"));

	}

	@Test
	void stunden() {
		BetreuungsmitteilungPensum pensum = createPensum();
		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_STUNDEN, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 137.5 Stunden, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@Test
	void stundenWithMahlzeitenVerguenstigungEnabled() {
		BetreuungsmitteilungPensum pensum = createPensum();
		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_STUNDEN, true, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 137.5 Stunden, monatliche Betreuungskosten: CHF 1’230.35, monatliche "
				+ "Hauptmahlzeiten: 5 à CHF 7, monatliche Nebenmahlzeiten: 9.75 à CHF 0.35"));
	}

	@ParameterizedTest
	@EnumSource(BetreuungspensumAnzeigeTyp.class)
	void mittagstisch_doesNotDependOnAnzeigeTyp(BetreuungspensumAnzeigeTyp anzeigeTyp) {
		BetreuungsmitteilungPensum pensum = createPensum();
		PensumUtil.transformMittagstischPensum(pensum);
		String result = run(BetreuungsangebotTyp.MITTAGSTISCH, anzeigeTyp, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: Anzahl Mahlzeiten pro Monat: "
				+ pensum.getMonatlicheHauptmahlzeiten()
				+ ", Kosten pro Mahlzeit: CHF "
				+ pensum.getTarifProHauptmahlzeit()));
	}

	@Test
	void tfo() {
		BetreuungsmitteilungPensum pensum = createPensum();
		String result = run(BetreuungsangebotTyp.TAGESFAMILIEN, BetreuungspensumAnzeigeTyp.NUR_STUNDEN, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 165 Stunden, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@ParameterizedTest
	@CsvSource({ "TAGESFAMILIEN, NUR_STUNDEN", "KITA, NUR_PROZENT" })
	void eingewoehnungPauschale(BetreuungsangebotTyp angebotsTyp, BetreuungspensumAnzeigeTyp anzeigeTyp) {
		BetreuungsmitteilungPensum pensum = createPensum();
		pensum.setEingewoehnungPauschale(createEingewoehnungPauschale());

		String result = run(angebotsTyp, anzeigeTyp, pensum);

		assertThat(
			result,
			stringContainsInOrder(
				"Pensum 1 von 01.01.2024 bis 29.08.2024: ",
				", Eingewöhnung von 28.12.2023 bis 07.01.2024: Pauschale: CHF 777"
			));
	}

	@Nonnull
	private String run(
		@Nonnull BetreuungsangebotTyp angebotTyp,
		@Nonnull BetreuungspensumAnzeigeTyp anzeigeTyp,
		@Nonnull BetreuungsmitteilungPensum... pensen
	) {
		return run(angebotTyp, anzeigeTyp, false, pensen);
	}

	@Nonnull
	private String run(
		@Nonnull BetreuungsangebotTyp angebotTyp,
		@Nonnull BetreuungspensumAnzeigeTyp anzeigeTyp,
		@Nonnull Boolean mahlzeitenVerguenstigungEnabled,
		@Nonnull BetreuungsmitteilungPensum... pensen
	) {
		Betreuungsmitteilung mitteilung = createBetreuungsmitteilung(angebotTyp, pensen);
		Betreuung betreuung = requireNonNull(mitteilung.getBetreuung());
		Gemeinde gemeinde = betreuung.extractGemeinde();
		Gesuchsperiode periode = betreuung.extractGesuchsperiode();

		String mahlzeitenVerguenstigung = mahlzeitenVerguenstigungEnabled.toString();
		expect(einstellungService.findEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED, gemeinde, periode))
			.andReturn(new Einstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED, mahlzeitenVerguenstigung, periode))
			.anyTimes();

		expect(einstellungService.findEinstellung(PENSUM_ANZEIGE_TYP, gemeinde, periode))
			.andReturn(new Einstellung(PENSUM_ANZEIGE_TYP, anzeigeTyp.name(), periode))
			.anyTimes();

		expect(einstellungService.findEinstellung(OEFFNUNGSTAGE_KITA, gemeinde, periode))
			.andReturn(new Einstellung(OEFFNUNGSTAGE_KITA, "220", periode))
			.anyTimes();

		expect(einstellungService.findEinstellung(OEFFNUNGSTAGE_TFO, gemeinde, periode))
			.andReturn(new Einstellung(OEFFNUNGSTAGE_TFO, "240", periode))
			.anyTimes();

		expect(einstellungService.findEinstellung(OEFFNUNGSSTUNDEN_TFO, gemeinde, periode))
			.andReturn(new Einstellung(OEFFNUNGSTAGE_TFO, "11", periode))
			.anyTimes();

		replayAll();

		String result = mitteilungServiceBean.createNachrichtForMutationsmeldung(mitteilung, Set.of(pensen), DEUTSCH_LOCALE);

		verifyAll();

		return result;
	}

	@Nonnull
	private BetreuungsmitteilungPensum createPensum() {
		BetreuungsmitteilungPensum pensum = new BetreuungsmitteilungPensum();
		pensum.setGueltigkeit(new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 8, 29)));
		pensum.setPensum(BigDecimal.valueOf(75));
		pensum.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1230.35));
		pensum.setUnitForDisplay(PensumUnits.PERCENTAGE);
		pensum.setMonatlicheHauptmahlzeiten(BigDecimal.valueOf(5));
		pensum.setMonatlicheNebenmahlzeiten(BigDecimal.valueOf(7));
		pensum.setTarifProHauptmahlzeit(BigDecimal.valueOf(9.75));
		pensum.setTarifProNebenmahlzeit(BigDecimal.valueOf(0.35));
		pensum.setStuendlicheVollkosten(BigDecimal.valueOf(13.30));

		return pensum;
	}

	@Nonnull
	private Betreuung createBetreuung(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());

		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getKind().setGesuch(gesuch);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(betreuungsangebotTyp);

		return betreuung;
	}

	@Nonnull
	private Betreuungsmitteilung createBetreuungsmitteilung(
		@Nonnull BetreuungsangebotTyp betreuungsangebotTyp,
		@Nonnull BetreuungsmitteilungPensum... pensen
	) {
		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setBetreuung(createBetreuung(betreuungsangebotTyp));
		betreuungsmitteilung.setBetreuungspensen(Set.of(pensen));

		return betreuungsmitteilung;
	}

	@Nonnull
	private EingewoehnungPauschale createEingewoehnungPauschale() {
		EingewoehnungPauschale pauschale = new EingewoehnungPauschale();
		pauschale.setGueltigkeit(new DateRange(LocalDate.of(2023, 12, 28), LocalDate.of(2024, 1, 7)));
		pauschale.setPauschale(BigDecimal.valueOf(777));

		return pauschale;
	}
}