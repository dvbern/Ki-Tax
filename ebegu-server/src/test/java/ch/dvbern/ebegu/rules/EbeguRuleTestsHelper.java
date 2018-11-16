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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator.createInitialenRestanspruch;
import static ch.dvbern.ebegu.util.Constants.DEFAULT_GUELTIGKEIT;

/**
 * Hilfsklasse fuer Ebegu-Rule-Tests
 */
public final class EbeguRuleTestsHelper {

	private static final BigDecimal MAX_EINKOMMEN = new BigDecimal("159000");

	private static final ErwerbspensumAbschnittRule erwerbspensumAbschnittRule = new ErwerbspensumAbschnittRule(DEFAULT_GUELTIGKEIT);
	private static final ErwerbspensumCalcRule erwerbspensumCalcRule =
		new ErwerbspensumCalcRule(DEFAULT_GUELTIGKEIT, 20, 20, 40);
	private static final FachstelleAbschnittRule fachstelleAbschnittRule = new FachstelleAbschnittRule(DEFAULT_GUELTIGKEIT);
	private static final FachstelleCalcRule fachstelleCalcRule = new FachstelleCalcRule(DEFAULT_GUELTIGKEIT);
	private static final BetreuungspensumAbschnittRule betreuungspensumAbschnittRule = new BetreuungspensumAbschnittRule(DEFAULT_GUELTIGKEIT);
	private static final RestanspruchLimitCalcRule restanspruchLimitCalcRule = new RestanspruchLimitCalcRule(DEFAULT_GUELTIGKEIT);
	private static final EinkommenAbschnittRule einkommenAbschnittRule = new EinkommenAbschnittRule(DEFAULT_GUELTIGKEIT);
	private static final EinkommenCalcRule maximalesEinkommenCalcRule = new EinkommenCalcRule(DEFAULT_GUELTIGKEIT, MAX_EINKOMMEN);
	private static final BetreuungsangebotTypCalcRule betreuungsangebotTypCalcRule = new BetreuungsangebotTypCalcRule(DEFAULT_GUELTIGKEIT);
	private static final EinreichungsfristAbschnittRule einreichungsfristAbschnittRule = new EinreichungsfristAbschnittRule(DEFAULT_GUELTIGKEIT);
	private static final EinreichungsfristCalcRule einreichungsfristCalcRule = new EinreichungsfristCalcRule(DEFAULT_GUELTIGKEIT);
	private static final WohnsitzAbschnittRule wohnsitzAbschnittRule = new WohnsitzAbschnittRule(DEFAULT_GUELTIGKEIT);
	private static final WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(DEFAULT_GUELTIGKEIT);
	private static final AbwesenheitAbschnittRule abwesenheitAbschnittRule =
		new AbwesenheitAbschnittRule(DEFAULT_GUELTIGKEIT, TestDataUtil.ABWESENHEIT_DAYS_LIMIT);
	private static final AbwesenheitCalcRule abwesenheitCalcRule = new AbwesenheitCalcRule(DEFAULT_GUELTIGKEIT);
	private static final UnbezahlterUrlaubAbschnittRule urlaubAbschnittRule = new UnbezahlterUrlaubAbschnittRule(DEFAULT_GUELTIGKEIT);
	private static final ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule = new ZivilstandsaenderungAbschnittRule(DEFAULT_GUELTIGKEIT);
	private static final SchulstufeCalcRule schulstufeCalcRule = new SchulstufeCalcRule(DEFAULT_GUELTIGKEIT, EinschulungTyp.KINDERGARTEN2);
	private static final KesbPlatzierungCalcRule kesbPlatzierungCalcRule = new KesbPlatzierungCalcRule(DEFAULT_GUELTIGKEIT);
	private static final RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer();

	private EbeguRuleTestsHelper() {
	}

	public static List<VerfuegungZeitabschnitt> calculate(Betreuung betreuung) {
		// Abschnitte
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode());
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculate(betreuung, initialenRestanspruchAbschnitte);
	}

	/**
	 * Testhilfsmethode die eine Betreuung so berechnet als haette es vorher schon eine Betreuung gegeben welche einen Teil des anspruchs
	 * aufgebraucht hat, es wird  als bestehnder Restnanspruch der Wert von existingRestanspruch genommen
	 */
	public static List<VerfuegungZeitabschnitt> calculateWithRemainingRestanspruch(Betreuung betreuung, int existingRestanspruch) {
		// Abschnitte
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode());
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : initialenRestanspruchAbschnitte) {
			verfuegungZeitabschnitt.setAnspruchspensumRest(existingRestanspruch);
		}
		return calculate(betreuung, initialenRestanspruchAbschnitte);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculate(Betreuung betreuung, List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte) {
		List<VerfuegungZeitabschnitt> result = initialenRestanspruchAbschnitte;
		result = erwerbspensumAbschnittRule.calculate(betreuung, result);
		result = urlaubAbschnittRule.calculate(betreuung, result);
		result = betreuungspensumAbschnittRule.calculate(betreuung, result);
		result = fachstelleAbschnittRule.calculate(betreuung, result);
		result = zivilstandsaenderungAbschnittRule.calculate(betreuung, result);
		result = einkommenAbschnittRule.calculate(betreuung, result);
		result = einreichungsfristAbschnittRule.calculate(betreuung, result);
		result = wohnsitzAbschnittRule.calculate(betreuung, result);
		result = abwesenheitAbschnittRule.calculate(betreuung, result);
		// Anspruch
		result = erwerbspensumCalcRule.calculate(betreuung, result);
		result = fachstelleCalcRule.calculate(betreuung, result);
		// Restanspruch
		// Reduktionen
		result = maximalesEinkommenCalcRule.calculate(betreuung, result);
		result = betreuungsangebotTypCalcRule.calculate(betreuung, result);
		result = einreichungsfristCalcRule.calculate(betreuung, result);
		result = wohnsitzCalcRule.calculate(betreuung, result);
		result = abwesenheitCalcRule.calculate(betreuung, result);
		result = schulstufeCalcRule.calculate(betreuung, result);
		result = kesbPlatzierungCalcRule.calculate(betreuung, result);
		result = restanspruchLimitCalcRule.calculate(betreuung, result);
		return result;
	}

	public static List<VerfuegungZeitabschnitt> initializeRestanspruchForNextBetreuung(Betreuung currentBetreuung, List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return restanspruchInitializer.createVerfuegungsZeitabschnitte(currentBetreuung, zeitabschnitte);
	}

	public static Betreuung createBetreuungWithPensum(
		LocalDate von, LocalDate bis,
		BetreuungsangebotTyp angebot,
		int pensum,
		BigDecimal monatlicheBetreuungskosten
	) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(angebot);
		betreuung.setBetreuungspensumContainers(new LinkedHashSet<>());
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuung(betreuung);
		DateRange gueltigkeit = new DateRange(von, bis);
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum(gueltigkeit));
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(MathUtil.DEFAULT.from(pensum));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);

		ErweiterteBetreuungContainer container = TestDataUtil.createDefaultErweiterteBetreuungContainer();
		container.setBetreuung(betreuung);
		betreuung.setErweiterteBetreuungContainer(container);

		return betreuung;
	}
}
