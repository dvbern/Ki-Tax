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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.spotify.hamcrest.pojo.IsPojo;
import org.hamcrest.Matcher;
import org.hamcrest.number.BigDecimalCloseTo;
import org.junit.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Nutzt zum Vergleich den ASIV 2019 BG-Rechern 0.8.xls für Tageseltern mit Eingaben:
 * Gebursttag Kind 23.07.217
 * Eingeschult FALSCH
 * Besondere Bedürfnisse FALSCH
 * Von 01.11.2019
 * Bis 01.11.2019
 * Monatliches Pensum in % 100%
 * Massgebendes Einkommen 88600
 * Monatliche Vollkosten 2000
 */
public class VerfuegungZeitabschnittRundungTest extends AbstractBGRechnerTest {

	private final BGRechnerParameterDTO parameterDTO = getParameter();
	private final TageselternBernRechner tageselternRechner = new TageselternBernRechner(Collections.emptyList());

	@Test
	public void testVerguenstigungProZeiteinheit() {
		BigDecimal verguenstigungProZeiteinheit = tageselternRechner.getVerguenstigungProZeiteinheit(
			parameterDTO,
			false,
			false,
			false,
			false,
			BigDecimal.valueOf(88600),
			false
		);

		assertThat(verguenstigungProZeiteinheit, of("5.1871794872"));
	}

	@Test
	public void exactBGCalculationResult() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 11, 1), LocalDate.of(2019, 11, 1));

		BGCalculationResult result = tageselternRechner.calculateAsiv(createZeitabschnitt(gueltigkeit), parameterDTO);

		assertThat(result, pojo(BGCalculationResult.class)
			// Minimalbetrag
			.where(BGCalculationResult::getMinimalerElternbeitrag, of("5.1333333333"))
			.where(BGCalculationResult::getVerguenstigungOhneBeruecksichtigungMinimalbeitrag, of("38.0393162393"))
			// Vergünstigung vor Berücksichtigung Vollkosten
			.where(BGCalculationResult::getVerguenstigungOhneBeruecksichtigungVollkosten, of("38.0393162393"))
			// Vollkosten minus Minimaltarif
			.where(BGCalculationResult::getVerguenstigung, of("38.0393162393"))
			// Anteil der Vollkosten
			.where(BGCalculationResult::getVollkosten, of("66.6666666"))
			.where(BGCalculationResult::getElternbeitrag, of("28.6273504274"))
			// Stunden gemäss Pensum und Anteil Monat
			.where(BGCalculationResult::getBgPensumZeiteinheit, of("7.333333333"))
			.where(BGCalculationResult::getAnspruchspensumZeiteinheit, of("7.333333333"))
			.where(BGCalculationResult::getBetreuungspensumZeiteinheit, of("7.333333333"))
		);
	}

	@Test
	public void testBGCalculation_toVerfuegungZeitabschnitt() {
		BGCalculationResult result = createCalculationResult();
		result.roundAllValues();

		// Default: Stunden mit 2 Kommastellen
		assertThat(result, calculationResultMatcher()
			.where(BGCalculationResult::getBgPensumZeiteinheit, twoDecimalsOf("7.23"))
			.where(BGCalculationResult::getAnspruchspensumZeiteinheit, twoDecimalsOf("8.23"))
			.where(BGCalculationResult::getBetreuungspensumZeiteinheit, twoDecimalsOf("9.23"))
		);
	}

	@Test
	public void testBGCalculation_toVerfuegungZeitabschnitt_zeiteinheitenStrategy() {
		BGCalculationResult result = createCalculationResult();
		result.setZeiteinheitenRoundingStrategy(MathUtil::roundToNearestQuarter);
		result.roundAllValues();

		// Stunden in in Viertel
		assertThat(result, calculationResultMatcher()
			.where(BGCalculationResult::getBgPensumZeiteinheit, twoDecimalsOf("7.25"))
			.where(BGCalculationResult::getAnspruchspensumZeiteinheit, twoDecimalsOf("8.25"))
			.where(BGCalculationResult::getBetreuungspensumZeiteinheit, twoDecimalsOf("9.25"))
		);
	}

	@Nonnull
	private IsPojo<BGCalculationResult> calculationResultMatcher() {
		return pojo(BGCalculationResult.class)
			// Rappen
			.where(BGCalculationResult::getMinimalerElternbeitrag, twoDecimalsOf("1.25"))
			// Rappen
			.where(
				BGCalculationResult::getVerguenstigungOhneBeruecksichtigungMinimalbeitrag,
				twoDecimalsOf("2.25"))
			// 2 Kommastellen
			.where(BGCalculationResult::getVerguenstigungOhneBeruecksichtigungVollkosten, twoDecimalsOf("3.25"))
			// 2 Kommastellen
			.where(BGCalculationResult::getVerguenstigung, twoDecimalsOf("4.25"))
			// 2 Kommastellen
			.where(BGCalculationResult::getVollkosten, twoDecimalsOf("5.25"))
			// Rappen
			.where(BGCalculationResult::getElternbeitrag, twoDecimalsOf("6.25"));
	}

	@Nonnull
	private BGCalculationResult createCalculationResult() {
		BGCalculationResult result = new BGCalculationResult();
		result.setMinimalerElternbeitrag(BigDecimal.valueOf(1.2345));
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(BigDecimal.valueOf(2.2345));
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(BigDecimal.valueOf(3.2345));
		result.setVerguenstigung(BigDecimal.valueOf(4.2345));
		result.setVollkosten(BigDecimal.valueOf(5.2345));
		result.setElternbeitrag(BigDecimal.valueOf(6.2345));
		result.setBgPensumZeiteinheit(BigDecimal.valueOf(7.2345));
		result.setAnspruchspensumZeiteinheit(BigDecimal.valueOf(8.2345));
		result.setBetreuungspensumZeiteinheit(BigDecimal.valueOf(9.2345));

		return result;
	}

	@Nonnull
	private BGCalculationInput createZeitabschnitt(@Nonnull DateRange gueltigkeit) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
		BGCalculationInput inputAsiv = zeitabschnitt.getBgCalculationInputAsiv();
		inputAsiv.setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
		inputAsiv.setAnspruchspensumProzent(100);
		inputAsiv.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(88600));
		inputAsiv.setBetreuungspensumProzent(BigDecimal.valueOf(100));
		inputAsiv.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		inputAsiv.setKostenAnteilMonat(calculateKostenAnteilMonat(inputAsiv, gueltigkeit));
		return inputAsiv;
	}

	@Nonnull
	private Matcher<BigDecimal> of(@Nonnull String value) {
		return BigDecimalCloseTo.closeTo(MathUtil.EXACT.from(value), new BigDecimal("1e-7"));
	}

	@Nonnull
	private Matcher<BigDecimal> twoDecimalsOf(@Nonnull String value) {
		return is(MathUtil.ZWEI_NACHKOMMASTELLE.from(value));
	}
}
