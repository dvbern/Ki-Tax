/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.Assert;

public class AbstractLuzernRechnerTest extends AbstractBGRechnerTest {

	protected final DateRange dateRangePartFebruary = new DateRange(
		LocalDate.of(2019, Month.FEBRUARY, 10),
		LocalDate.of(2019, Month.FEBRUARY, 28));

	protected final LocalDate geburtstagBaby = LocalDate.of(2018, Month.OCTOBER, 15);
	protected final LocalDate geburtstagKind = LocalDate.of(2016, Month.OCTOBER, 15);

	protected VerfuegungZeitabschnitt prepareVerfuegung(TestValues testValues) {

		LocalDate geburtstag = testValues.isBaby ? geburtstagBaby : geburtstagKind;

		Verfuegung verfuegung = prepareVerfuegungKita(
			geburtstag,
			testValues.gueltigkeit.getGueltigAb(),
			testValues.gueltigkeit.getGueltigBis(),
			EinschulungTyp.VORSCHULALTER,
			false,
			testValues.einkommen,
			testValues.monatlicheBetreuungsKosten);

		VerfuegungZeitabschnitt verfuegungZeitabschnitt = verfuegung.getZeitabschnitte().get(0);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv().setAnspruchspensumProzent(testValues.anspruchsPensum);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv().setBetreuungspensumProzent(testValues.betreuungsPensum);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv().setBesondereBeduerfnisseZuschlag(testValues.besondereBeduerfnisseZuschlag);
		verfuegungZeitabschnitt.setBabyTarifForAsivAndGemeinde(geburtstag.plusYears(1)
			.isAfter(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis()));
		return verfuegung.getZeitabschnitte().get(0);
	}

	protected void assertCalculationResultResult(BGCalculationResult result, TestValues testValues) {
		Assert.assertEquals("Vollkosten not equal", testValues.expectedVollkosten.stripTrailingZeros(), result.getVollkosten().stripTrailingZeros());
		Assert.assertEquals("Betreuungspensum not equal", testValues.betreuungsPensum.stripTrailingZeros(), result.getBetreuungspensumProzent().stripTrailingZeros());
		Assert.assertEquals("Anspruchspensum not equal", testValues.anspruchsPensum, result.getAnspruchspensumProzent());
		Assert.assertEquals("Elternebeitrag not equal", testValues.expectedElternbeitrag.stripTrailingZeros(), result.getElternbeitrag().stripTrailingZeros());
		Assert.assertEquals("Minimaler Elternbeitrag not equal", testValues.expectedMinimalerElternbeitrag.stripTrailingZeros(), result.getMinimalerElternbeitrag().stripTrailingZeros());
		Assert.assertEquals(
			"VerguenstigungOhneBeruckesichtigungMinimalbetrag not equal",
			testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag.stripTrailingZeros(),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag().stripTrailingZeros());
		Assert.assertEquals(
			"VerguenstigungOhneBeruecksichtigungVollkosten not equal",
			testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten.stripTrailingZeros(),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten().stripTrailingZeros());
		Assert.assertEquals(
			"Verguenstigung not equal",
			testValues.expectedVerguenstigung.stripTrailingZeros(),
			result.getVerguenstigung().stripTrailingZeros());
		Assert.assertEquals("Betreuungstage not equal", testValues.expectedBetreuungsTage.stripTrailingZeros(), result.getBetreuungspensumZeiteinheit().stripTrailingZeros());
		Assert.assertEquals("Anspruchstage not equal", testValues.expectedAnspruchsTage.stripTrailingZeros(), result.getAnspruchspensumZeiteinheit().stripTrailingZeros());
		Assert.assertEquals("BGTage not equal", testValues.expectedBgTage.stripTrailingZeros(), result.getBgPensumZeiteinheit().stripTrailingZeros());
		BigDecimal bgPensum =  testValues.betreuungsPensum.min(BigDecimal.valueOf(testValues.anspruchsPensum));
		Assert.assertEquals("BGPensum not equal", bgPensum.stripTrailingZeros(), result.getBgPensumProzent().stripTrailingZeros());
	}

	protected static class TestValues {
		protected BigDecimal monatlicheBetreuungsKosten = BigDecimal.ZERO;
		protected BigDecimal betreuungsPensum = BigDecimal.ZERO;
		protected int anspruchsPensum;
		protected BigDecimal einkommen = BigDecimal.ZERO;
		protected BigDecimal besondereBeduerfnisseZuschlag = BigDecimal.ZERO;
		protected boolean isBaby = false;

		protected BigDecimal expectedVollkosten = BigDecimal.ZERO;
		protected BigDecimal expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.ZERO;
		protected BigDecimal expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.ZERO;
		protected BigDecimal expectedVerguenstigung = BigDecimal.ZERO;
		protected BigDecimal expectedElternbeitrag = BigDecimal.ZERO;
		protected BigDecimal expectedMinimalerElternbeitrag = BigDecimal.ZERO;
		protected BigDecimal expectedBetreuungsTage = BigDecimal.ZERO;
		protected BigDecimal expectedAnspruchsTage = BigDecimal.ZERO;
		protected BigDecimal expectedBgTage = BigDecimal.ZERO;

		protected DateRange gueltigkeit = new DateRange(
			LocalDate.of(2019, Month.AUGUST, 1),
			LocalDate.of(2019, Month.AUGUST, 31));;
	}
}
