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

package ch.dvbern.ebegu.rechner.rules;

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungParameter;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MahlzeitenverguenstigungBGRechnerRuleTest {

	private final BigDecimal einkommenStufe1 = MathUtil.DEFAULT.from(10000);
	private final BigDecimal einkommenStufe2 = MathUtil.DEFAULT.from(60000);

	private final AbstractPlatz platz = TestDataUtil.createTestgesuchDagmar(new FinanzielleSituationBernRechner()).extractAllBetreuungen().get(0);
	private MahlzeitenverguenstigungBGRechnerRule rule = null;
	private MahlzeitenverguenstigungParameter parameter;

	@Before
	public void setUp() {
		final Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		familiensituation.setKeineMahlzeitenverguenstigungBeantragt(false);
		this.parameter = new MahlzeitenverguenstigungParameter(
			true,
			false,
			BigDecimal.valueOf(51000),
			BigDecimal.valueOf(70000),
			BigDecimal.valueOf(6),
			BigDecimal.valueOf(3),
			BigDecimal.valueOf(0),
			BigDecimal.valueOf(2)
		);
		rule =
			new MahlzeitenverguenstigungBGRechnerRule(Locale.GERMAN);
	}

	@Test
	public void executeRule() {
		// Beispiel 1 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 8, 16, 10, 3),
			24
		);
		// Beispiel 2 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 8, 20, 10, 3),
			30
		);
		// Beispiel 3 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 10, 16, 10, 3),
			30
		);
		// Beispiel 4 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 8, 20, 10, 2),
			28
		);
		// Beispiel 5 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 10, 16, 10, 2),
			30
		);
		// Beispiel 6 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 8, 20, 6, 3),
			30
		);
		// Beispiel 7 aus Excel
		assertResults(
			createInputData(einkommenStufe1, 8, 0, 6, 3),
			32
		);
		// Beispiel 8 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 0, 4, 10, 3),
			6
		);
		// Beispiel 9 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 8, 17, 10, 3),
			25
		);
	}

	@Test
	public void ungeradeAnzahlNebenmahlzeiten() {
		assertResults(
			createInputData(einkommenStufe2, 10, 25, 10, 5),
			39
		);
		assertResults(
			createInputData(einkommenStufe2, 10, 24, 10, 5),
			36
		);
		assertResults(
			createInputData(einkommenStufe2, 10, 26, 10, 5),
			39
		);
	}

	@Test
	public void maximalAnzahlTageUeberschritten() {
		// Bei 100% Pensum duerfen max. 20 Tage beruecksichtigt werden
		// Beispiel 10 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 30, 60, 10, 6),
			60
		);
		// Beispiel 11 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 30, 90, 10, 6),
			60
		);
		// Beispiel 12 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 10, 90, 10, 6),
			60
		);

	}

	@Test
	public void teilPensum() {
		// Beispiel 13 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 30, 60, 10, 3, 60),
			36
		);
		// Beispiel 14 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 8, 20, 10, 3, 60),
			30
		);
		// Beispiel 15 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 8, 19, 10, 3, 60),
			28
		);
		// Beispiel 16 aus Excel
		assertResults(
			createInputData(einkommenStufe2, 8, 0, 10, 3, 60),
			24
		);
	}

	private void assertResults(@Nonnull BGCalculationInput inputData, int expectedVerguenstigungMahlzeitenTotal) {
		BGRechnerParameterDTO bgRechnerParameterDTO = new BGRechnerParameterDTO();
		bgRechnerParameterDTO.setMahlzeitenverguenstigungParameter(this.parameter);
		RechnerRuleParameterDTO rechnerRuleParameterDTO = new RechnerRuleParameterDTO();
		rule.prepareParameter(inputData, bgRechnerParameterDTO, rechnerRuleParameterDTO);
		final BigDecimal verguenstigungMahlzeitenTotal = rechnerRuleParameterDTO.getVerguenstigungMahlzeitenTotal();
		Assert.assertNotNull(verguenstigungMahlzeitenTotal);
		Assert.assertEquals(expectedVerguenstigungMahlzeitenTotal, verguenstigungMahlzeitenTotal.intValue());
	}

	@Nonnull
	private BGCalculationInput createInputData(
		BigDecimal einkommen,
		int anzahlHauptmahlzeiten,
		int anzahlNebenmahlzeiten,
		int kostenProHauptmahlzeit,
		int kostenProNebenmahlzeit
	) {
		return createInputData(
			einkommen,
			anzahlHauptmahlzeiten,
			anzahlNebenmahlzeiten,
			kostenProHauptmahlzeit,
			kostenProNebenmahlzeit,
			100);
	}

	@Nonnull
	private BGCalculationInput createInputData(
		BigDecimal einkommen,
		int anzahlHauptmahlzeiten,
		int anzahlNebenmahlzeiten,
		int kostenProHauptmahlzeit,
		int kostenProNebenmahlzeit,
		int anspruchsPensum
	) {
		VerfuegungZeitabschnitt abschnitt = new VerfuegungZeitabschnitt();
		BGCalculationInput input = abschnitt.getBgCalculationInputAsiv();
		input.setMassgebendesEinkommenVorAbzugFamgr(einkommen);
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setAnzahlHauptmahlzeiten(MathUtil.DEFAULT.from(anzahlHauptmahlzeiten));
		input.setAnzahlNebenmahlzeiten(MathUtil.DEFAULT.from(anzahlNebenmahlzeiten));
		input.setTarifHauptmahlzeit(MathUtil.DEFAULT.from(kostenProHauptmahlzeit));
		input.setTarifNebenmahlzeit(MathUtil.DEFAULT.from(kostenProNebenmahlzeit));
		input.setAnspruchspensumProzent(anspruchsPensum);
		input.setBetreuungspensumProzent(MathUtil.DEFAULT.from(anspruchsPensum));
		return input;
	}
}
