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

import ch.dvbern.ebegu.betreuung.BetreuungEinstellungen;
import ch.dvbern.ebegu.betreuung.BetreuungEinstellungenService;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Eingewoehnung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSSTUNDEN_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.PENSUM_ANZEIGE_TYP;
import static ch.dvbern.ebegu.util.Constants.DEUTSCH_LOCALE;
import static java.util.Objects.requireNonNull;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(EasyMockExtension.class)
class MitteilungServiceBeanCreateMessageTest extends EasyMockSupport {

	@Mock
	private EinstellungService einstellungService;

	@Mock
	private BetreuungEinstellungenService betreuungEinstellungenService;


	@TestSubject
	private final MitteilungServiceBean mitteilungServiceBean = new MitteilungServiceBean();

	@Test
	void emptyWhenNoPensen() {
		BetreuungEinstellungen einstellungen = defaultEinstellungen().build();

		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, einstellungen);

		assertThat(result, emptyString());
	}

	@Test
	void concatWithNewline() {
		BetreuungEinstellungen einstellungen = defaultEinstellungen().build();

		String result =
			run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, einstellungen, createPensum(),
				createPensum());

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 75%, monatliche Betreuungskosten: CHF 1’230.35\n"
				+ "Pensum 2 von 01.01.2024 bis 29.08.2024: 75%, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@Test
	void percentage() {
		BetreuungsmitteilungPensum pensum = createPensum();
		BetreuungEinstellungen einstellungen = defaultEinstellungen().build();

		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, einstellungen, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 75%, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@Test
	void percentageWithMahlzeitenVerguenstigungEnabled() {
		BetreuungsmitteilungPensum pensum = createPensum();

		BetreuungEinstellungen einstellungen = BetreuungEinstellungen.builder()
			.mahlzeitenVerguenstigungEnabled(true)
			.build();

		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, einstellungen, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 75%, monatliche Betreuungskosten: CHF 1’230.35, monatliche "
				+ "Hauptmahlzeiten: 5 à CHF 9.75, monatliche Nebenmahlzeiten: 7 à CHF 0.35"));

	}

	@Test
	void stunden() {
		BetreuungsmitteilungPensum pensum = createPensum();
		BetreuungEinstellungen einstellungen = defaultEinstellungen().build();

		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_STUNDEN, einstellungen, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 137.5 Stunden, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@Test
	void stundenWithMahlzeitenVerguenstigungEnabled() {
		BetreuungsmitteilungPensum pensum = createPensum();

		BetreuungEinstellungen einstellungen = BetreuungEinstellungen.builder()
			.mahlzeitenVerguenstigungEnabled(true)
			.build();

		String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_STUNDEN, einstellungen, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 137.5 Stunden, monatliche Betreuungskosten: CHF 1’230.35, monatliche "
				+ "Hauptmahlzeiten: 5 à CHF 9.75, monatliche Nebenmahlzeiten: 7 à CHF 0.35"));
	}

	@ParameterizedTest
	@EnumSource(BetreuungspensumAnzeigeTyp.class)
	void mittagstisch_doesNotDependOnAnzeigeTyp(BetreuungspensumAnzeigeTyp anzeigeTyp) {
		BetreuungsmitteilungPensum pensum = createPensum();
		PensumUtil.transformMittagstischPensum(pensum);
		BetreuungEinstellungen einstellungen = defaultEinstellungen().build();

		String result = run(BetreuungsangebotTyp.MITTAGSTISCH, anzeigeTyp, einstellungen, pensum);

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
		BetreuungEinstellungen einstellungen = defaultEinstellungen().build();

		String result = run(BetreuungsangebotTyp.TAGESFAMILIEN, BetreuungspensumAnzeigeTyp.NUR_STUNDEN, einstellungen, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 165 Stunden, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@Test
	void tfoMitAnwesenheitstagen() {
		BetreuungsmitteilungPensum pensum = createPensum();
		pensum.setBetreuteTage(BigDecimal.valueOf(8));

		BetreuungEinstellungen einstellungen = defaultEinstellungen()
			.betreuteTageEnabled(true)
			.build();

		String result = run(BetreuungsangebotTyp.TAGESFAMILIEN, BetreuungspensumAnzeigeTyp.NUR_STUNDEN, einstellungen, pensum);

		assertThat(
			result,
			is("Pensum 1 von 01.01.2024 bis 29.08.2024: 165 Stunden, Anwesenheitstage: 8, monatliche Betreuungskosten: CHF 1’230.35"));
	}

	@ParameterizedTest
	@CsvSource({ "TAGESFAMILIEN, NUR_STUNDEN", "KITA, NUR_PROZENT" })
	void eingewoehnungKosten(BetreuungsangebotTyp angebotsTyp, BetreuungspensumAnzeigeTyp anzeigeTyp) {
		BetreuungsmitteilungPensum pensum = createPensum();
		pensum.setEingewoehnung(createEingewoehnung());

		BetreuungEinstellungen einstellungen = defaultEinstellungen().build();

		String result = run(angebotsTyp, anzeigeTyp, einstellungen, pensum);

		assertThat(
			result,
			stringContainsInOrder(
				"Pensum 1 von 01.01.2024 bis 29.08.2024: ",
				", monatliche Betreuungskosten: CHF 1’230.35, Eingewöhnung von 28.12.2023 bis 07.01.2024: Kosten: CHF 777"
			));
	}

	@Nested
	class SchulergaenzendeBetreuungMessageTest {

		@Test
		void noSchulergaenzendeBetreuungMessageWhenEinstellungDisabled() {
			BetreuungsmitteilungPensum pensum = createPensum();
			pensum.setBetreuungInFerienzeit(true);

			BetreuungEinstellungen einstellungen = defaultEinstellungen()
				.schulergaenzendeBetreuungEnabled(false)
				.build();

			String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, einstellungen, pensum);

			assertThat(result, not(containsString("während der schulfreien Zeit")));
		}

		@Test
		void noSchulergaenzendeBetreuungMessageWhenVorschule() {
			BetreuungsmitteilungPensum pensum = createPensum();
			pensum.setBetreuungInFerienzeit(true);
			Betreuungsmitteilung betreuungsmitteilung = createBetreuungsmitteilung(BetreuungsangebotTyp.KITA, pensum);
			requireNonNull(betreuungsmitteilung.getBetreuung()).getKind()
				.getKindJA()
				.setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);

			BetreuungEinstellungen einstellungen = defaultEinstellungen()
				.schulergaenzendeBetreuungEnabled(false)
				.build();

			String result = run(betreuungsmitteilung, BetreuungspensumAnzeigeTyp.NUR_PROZENT, einstellungen, pensum);

			assertThat(result, not(containsString("während der schulfreien Zeit")));
		}

		@ParameterizedTest
		@CsvSource({ "TAGESFAMILIEN, NUR_STUNDEN", "KITA, NUR_PROZENT" })
		void betreuungInFerienzeit(BetreuungsangebotTyp angebotsTyp, BetreuungspensumAnzeigeTyp anzeigeTyp) {
			BetreuungsmitteilungPensum pensum = createPensum();
			pensum.setBetreuungInFerienzeit(true);
			Betreuungsmitteilung betreuungsmitteilung = createBetreuungsmitteilung(angebotsTyp, pensum);
			requireNonNull(betreuungsmitteilung.getBetreuung()).getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE1);

			BetreuungEinstellungen einstellungen = defaultEinstellungen()
				.schulergaenzendeBetreuungEnabled(true)
				.build();

			String result = run(BetreuungsangebotTyp.KITA, BetreuungspensumAnzeigeTyp.NUR_PROZENT, einstellungen, pensum);

			assertThat(
				result,
				stringContainsInOrder(
					"Pensum 1 von 01.01.2024 bis 29.08.2024: ",
					", monatliche Betreuungskosten: CHF 1’230.35 (während der schulfreien Zeit)"
				));
		}

		@ParameterizedTest
		@CsvSource({ "TAGESFAMILIEN, NUR_STUNDEN", "KITA, NUR_PROZENT" })
		void betreuungNichtInFerienzeit(BetreuungsangebotTyp angebotsTyp, BetreuungspensumAnzeigeTyp anzeigeTyp) {
			BetreuungsmitteilungPensum pensum = createPensum();
			pensum.setBetreuungInFerienzeit(false);
			Betreuungsmitteilung betreuungsmitteilung = createBetreuungsmitteilung(angebotsTyp, pensum);
			requireNonNull(betreuungsmitteilung.getBetreuung()).getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE1);

			BetreuungEinstellungen einstellungen = defaultEinstellungen()
				.schulergaenzendeBetreuungEnabled(true)
				.build();

			String result = run(betreuungsmitteilung, anzeigeTyp, einstellungen, pensum);

			assertThat(
				result,
				stringContainsInOrder(
					"Pensum 1 von 01.01.2024 bis 29.08.2024: ",
					", monatliche Betreuungskosten: CHF 1’230.35 (während der Schulzeit)"
				));
		}
	}

	@Nonnull
	private BetreuungEinstellungen.BetreuungEinstellungenBuilder defaultEinstellungen() {
		return BetreuungEinstellungen.builder()
			.mahlzeitenVerguenstigungEnabled(false)
			.schulergaenzendeBetreuungEnabled(false)
			.betreuteTageEnabled(false);
	}

	private String run(
		@Nonnull BetreuungsangebotTyp angebotTyp,
		@Nonnull BetreuungspensumAnzeigeTyp anzeigeTyp,
		@Nonnull BetreuungEinstellungen einstellungen,
		@Nonnull BetreuungsmitteilungPensum... pensen
	) {
		return run(createBetreuungsmitteilung(angebotTyp, pensen), anzeigeTyp, einstellungen, pensen);
	}

	private String run(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull BetreuungspensumAnzeigeTyp anzeigeTyp,
		@Nonnull BetreuungEinstellungen einstellungen,
		@Nonnull BetreuungsmitteilungPensum... pensen
	) {
		Betreuung betreuung = requireNonNull(mitteilung.getBetreuung());

		expect(betreuungEinstellungenService.getEinstellungen(betreuung))
			.andReturn(einstellungen)
			.anyTimes();

		expect(einstellungService.findEinstellung(PENSUM_ANZEIGE_TYP, betreuung))
			.andReturn(new Einstellung(PENSUM_ANZEIGE_TYP, anzeigeTyp.name(), betreuung.extractGesuchsperiode()))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(OEFFNUNGSTAGE_KITA, betreuung))
			.andReturn(new BigDecimal("220"))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(OEFFNUNGSTAGE_TFO, betreuung))
			.andReturn(new BigDecimal("240"))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(OEFFNUNGSSTUNDEN_TFO, betreuung))
			.andReturn(new BigDecimal("11"))
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
	private Eingewoehnung createEingewoehnung() {
		Eingewoehnung pauschale = new Eingewoehnung();
		pauschale.setGueltigkeit(new DateRange(LocalDate.of(2023, 12, 28), LocalDate.of(2024, 1, 7)));
		pauschale.setKosten(BigDecimal.valueOf(777));

		return pauschale;
	}
}
