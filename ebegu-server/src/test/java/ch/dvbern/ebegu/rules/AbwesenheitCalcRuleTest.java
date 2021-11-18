/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.Month;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests fuer AbwesenheitCalcRule
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class AbwesenheitCalcRuleTest {

	private final LocalDate START_PERIODE = LocalDate.of(2016, Month.AUGUST, 1);
	private final LocalDate ENDE_PERIODE = LocalDate.of(2017, Month.JULY, 31);
	private final DateRange PERIODE = new DateRange(START_PERIODE, ENDE_PERIODE);

	@Test
	public void testSchulamtBetreuungWithAbwesenheit() {
		final AbwesenheitCalcRule rule = new AbwesenheitCalcRule(PERIODE, Constants.DEFAULT_LOCALE);
		final VerfuegungZeitabschnitt zeitAbschnitt = createZeitabschnitt(true);
		final Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);

		rule.executeRuleIfApplicable(betreuung, zeitAbschnitt);
		BemerkungsMerger.prepareGeneratedBemerkungen(zeitAbschnitt);

		Assert.assertFalse(zeitAbschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		Assert.assertTrue(zeitAbschnitt.getVerfuegungZeitabschnittBemerkungList().isEmpty());
	}

	@Test
	public void testJABetreuungWithAbwesenheit() {
		final AbwesenheitCalcRule rule = new AbwesenheitCalcRule(PERIODE, Constants.DEFAULT_LOCALE);
		final VerfuegungZeitabschnitt zeitAbschnitt = createZeitabschnitt(true);
		final Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);

		rule.executeRuleIfApplicable(betreuung, zeitAbschnitt);
		BemerkungsMerger.prepareGeneratedBemerkungen(zeitAbschnitt);

		Assert.assertTrue(zeitAbschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		Assert.assertEquals(1, zeitAbschnitt.getVerfuegungZeitabschnittBemerkungList().size());
		Assert.assertEquals(
			"Das Kind wird länger als 30 aufeinanderfolgende Kalendertage nicht familienergänzend betreut"
				+ " (z.B. aufgrund einer längeren Reise). Deshalb wird kein Betreuungsgutschein mehr ausbezahlt (Art. 34u Abs. 1).",
			zeitAbschnitt.getVerfuegungZeitabschnittBemerkungList().get(0).getBemerkung());

	}

	@Test
	public void testJABetreuungWithoutAbwesenheit() {
		final AbwesenheitCalcRule rule = new AbwesenheitCalcRule(PERIODE, Constants.DEFAULT_LOCALE);
		final VerfuegungZeitabschnitt zeitAbschnitt = createZeitabschnitt(false);
		final Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);

		rule.executeRuleIfApplicable(betreuung, zeitAbschnitt);
		BemerkungsMerger.prepareGeneratedBemerkungen(zeitAbschnitt);

		Assert.assertFalse(zeitAbschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		Assert.assertTrue(zeitAbschnitt.getVerfuegungZeitabschnittBemerkungList().isEmpty());
	}

	// HELP METHODS

	@Nonnull
	private VerfuegungZeitabschnitt createZeitabschnitt(boolean abwesend) {
		final VerfuegungZeitabschnitt zeitAbschnitt = new VerfuegungZeitabschnitt();
		zeitAbschnitt.setGueltigkeit(PERIODE);
		zeitAbschnitt.getBgCalculationInputAsiv().setLongAbwesenheit(abwesend);
		return zeitAbschnitt;
	}
}
