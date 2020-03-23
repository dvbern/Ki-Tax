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
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator.createInitialenRestanspruch;
import static ch.dvbern.ebegu.util.Constants.DEFAULT_GUELTIGKEIT;
import static ch.dvbern.ebegu.util.Constants.DEFAULT_LOCALE;
import static ch.dvbern.ebegu.util.Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS;
import static ch.dvbern.ebegu.util.Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS;
import static ch.dvbern.ebegu.util.Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS;
import static ch.dvbern.ebegu.util.Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS;

/**
 * Hilfsklasse fuer Ebegu-Rule-Tests
 */
public final class EbeguRuleTestsHelper {

	private static final BigDecimal MAX_EINKOMMEN = new BigDecimal("159000");

	private static final ErwerbspensumAbschnittRule erwerbspensumAbschnittRule = new ErwerbspensumAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final ErwerbspensumCalcRule erwerbspensumCalcRule =
		new ErwerbspensumCalcRule(DEFAULT_GUELTIGKEIT, 20, 20, 40, DEFAULT_LOCALE);
	private static final FachstelleAbschnittRule fachstelleAbschnittRule = new FachstelleAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final FachstelleCalcRule fachstelleCalcRule = new FachstelleCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final AusserordentlicherAnspruchAbschnittRule ausserordentlicherAnspruchAbschnittRule =
		new AusserordentlicherAnspruchAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final AusserordentlicherAnspruchCalcRule ausserordentlicherAnspruchCalcRule =
		new AusserordentlicherAnspruchCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final KindTarifAbschnittRule kindTarifAbschnittRule = new KindTarifAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final BetreuungspensumAbschnittRule betreuungspensumAbschnittRule = new BetreuungspensumAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final TagesschuleBetreuungszeitAbschnittRule tagesschuleAbschnittRule = new TagesschuleBetreuungszeitAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final RestanspruchLimitCalcRule restanspruchLimitCalcRule = new RestanspruchLimitCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final EinkommenAbschnittRule einkommenAbschnittRule = new EinkommenAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final EinkommenCalcRule maximalesEinkommenCalcRule = new EinkommenCalcRule(DEFAULT_GUELTIGKEIT, MAX_EINKOMMEN, DEFAULT_LOCALE);
	private static final BetreuungsangebotTypAbschnittRule betreuungsangebotTypAbschnittRule = new BetreuungsangebotTypAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final BetreuungsangebotTypCalcRule betreuungsangebotTypCalcRule = new BetreuungsangebotTypCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final EinreichungsfristAbschnittRule einreichungsfristAbschnittRule = new EinreichungsfristAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final EinreichungsfristCalcRule einreichungsfristCalcRule = new EinreichungsfristCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final WohnsitzAbschnittRule wohnsitzAbschnittRule = new WohnsitzAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final AbwesenheitAbschnittRule abwesenheitAbschnittRule =
		new AbwesenheitAbschnittRule(DEFAULT_GUELTIGKEIT, TestDataUtil.ABWESENHEIT_DAYS_LIMIT, DEFAULT_LOCALE);
	private static final AbwesenheitCalcRule abwesenheitCalcRule = new AbwesenheitCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final UnbezahlterUrlaubAbschnittRule urlaubAbschnittRule = new UnbezahlterUrlaubAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule = new ZivilstandsaenderungAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final SchulstufeCalcRule schulstufeCalcRule = new SchulstufeCalcRule(DEFAULT_GUELTIGKEIT, EinschulungTyp.KINDERGARTEN2, DEFAULT_LOCALE);
	private static final KesbPlatzierungCalcRule kesbPlatzierungCalcRule = new KesbPlatzierungCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final FamilienabzugAbschnittRule familienabzugAbschnittRule =
		new FamilienabzugAbschnittRule(DEFAULT_GUELTIGKEIT,
			new BigDecimal(PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS),
			new BigDecimal(PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS),
			new BigDecimal(PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS),
			new BigDecimal(PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS),
			DEFAULT_LOCALE);
	private static final StorniertCalcRule storniertCalcRule = new StorniertCalcRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);
	private static final SozialhilfeAbschnittRule sozialhilfeAbschnittRule = new SozialhilfeAbschnittRule(DEFAULT_GUELTIGKEIT, DEFAULT_LOCALE);

	private static final AnspruchFristRule anspruchFristRule = new AnspruchFristRule();
	private static final AbschlussNormalizer abschlussNormalizerKeepMonate = new AbschlussNormalizer(true);
	private static final AbschlussNormalizer abschlussNormalizerDismissMonate = new AbschlussNormalizer(false);
	private static final MutationsMerger mutationsMerger = new MutationsMerger(Locale.GERMAN);
	private static final MonatsRule monatsRule = new MonatsRule();
	private static final RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer();

	private EbeguRuleTestsHelper() {
	}

	@Nonnull
	public static List<VerfuegungZeitabschnitt> runSingleAbschlussRule(
		@Nonnull AbstractAbschlussRule abschlussRule, @Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte
	) {
		List<VerfuegungZeitabschnitt> result = abschlussRule.executeIfApplicable(platz, zeitabschnitte);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : result) {
			verfuegungZeitabschnitt.initBGCalculationResult();
		}
		return result;
	}

	public static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung) {
		// Abschnitte
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculate(betreuung, initialenRestanspruchAbschnitte);
	}

	public static List<VerfuegungZeitabschnitt> calculateInklAllgemeineRegeln(Betreuung betreuung) {
		// Abschnitte
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculateInklAllgemeineRegeln(betreuung, initialenRestanspruchAbschnitte);
	}

	/**
	 * Testhilfsmethode die eine Betreuung so berechnet als haette es vorher schon eine Betreuung gegeben welche einen Teil des anspruchs
	 * aufgebraucht hat, es wird  als bestehnder Restnanspruch der Wert von existingRestanspruch genommen
	 */
	public static List<VerfuegungZeitabschnitt> calculateWithRemainingRestanspruch(Betreuung betreuung, int existingRestanspruch) {
		// Abschnitte
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : initialenRestanspruchAbschnitte) {
			verfuegungZeitabschnitt.setAnspruchspensumRestForAsivAndGemeinde(existingRestanspruch);
		}
		return calculate(betreuung, initialenRestanspruchAbschnitte);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung, List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte) {
		return calculateAllRules(betreuung, initialenRestanspruchAbschnitte, false);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculateInklAllgemeineRegeln(Betreuung betreuung, List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte) {
		return calculateAllRules(betreuung, initialenRestanspruchAbschnitte, true);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculateAllRules(AbstractPlatz platz, List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte,
		boolean doMonatsstueckelungen) {
		List<VerfuegungZeitabschnitt> result = initialenRestanspruchAbschnitte;
		result = erwerbspensumAbschnittRule.calculate(platz, result);
		result = urlaubAbschnittRule.calculate(platz, result);
		result = familienabzugAbschnittRule.calculate(platz, result);
		result = kindTarifAbschnittRule.calculate(platz, result);
		result = betreuungsangebotTypAbschnittRule.calculate(platz, result);
		result = betreuungspensumAbschnittRule.calculate(platz, result);
		result = tagesschuleAbschnittRule.calculate(platz, result);
		result = fachstelleAbschnittRule.calculate(platz, result);
		result = ausserordentlicherAnspruchAbschnittRule.calculate(platz, result);
		result = einkommenAbschnittRule.calculate(platz, result);
		result = wohnsitzAbschnittRule.calculate(platz, result);
		result = einreichungsfristAbschnittRule.calculate(platz, result);
		result = abwesenheitAbschnittRule.calculate(platz, result);
		result = zivilstandsaenderungAbschnittRule.calculate(platz, result);
		result = sozialhilfeAbschnittRule.calculate(platz, result);
		// Anspruch
		result = storniertCalcRule.calculate(platz, result);
		result = erwerbspensumCalcRule.calculate(platz, result);
		result = fachstelleCalcRule.calculate(platz, result);
		result = ausserordentlicherAnspruchCalcRule.calculate(platz, result);
		// Restanspruch
		// Reduktionen
		result = maximalesEinkommenCalcRule.calculate(platz, result);
		result = betreuungsangebotTypCalcRule.calculate(platz, result);
		result = wohnsitzCalcRule.calculate(platz, result);
		result = einreichungsfristCalcRule.calculate(platz, result);
		result = abwesenheitCalcRule.calculate(platz, result);
		result = schulstufeCalcRule.calculate(platz, result);
		result = kesbPlatzierungCalcRule.calculate(platz, result);
		result = restanspruchLimitCalcRule.calculate(platz, result);

		result = anspruchFristRule.executeIfApplicable(platz, result);
		// Der RestanspruchInitializer erstellt Restansprueche, darf nicht das Resultat ueberschreiben!
		restanspruchInitializer.executeIfApplicable(platz, result);
		result = abschlussNormalizerDismissMonate.executeIfApplicable(platz, result);
		if (doMonatsstueckelungen) {
			result = monatsRule.executeIfApplicable(platz, result);
		}
		result = mutationsMerger.executeIfApplicable(platz, result);
		result = abschlussNormalizerKeepMonate.executeIfApplicable(platz, result);
		BemerkungsMerger.prepareGeneratedBemerkungen(result);

		result.forEach(VerfuegungZeitabschnitt::initBGCalculationResult);
		return result;
	}

	public static List<VerfuegungZeitabschnitt> initializeRestanspruchForNextBetreuung(Betreuung currentBetreuung, List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return restanspruchInitializer.executeIfApplicable(currentBetreuung, zeitabschnitte);
	}

	public static Betreuung createBetreuungWithPensum(
		LocalDate von, LocalDate bis,
		BetreuungsangebotTyp angebot,
		int pensum,
		BigDecimal monatlicheBetreuungskosten
	) {
		return createBetreuungWithPensum(von, bis, angebot, pensum, monatlicheBetreuungskosten, 0, 0);
	}

	public static Betreuung createBetreuungWithPensum(
		LocalDate von, LocalDate bis,
		BetreuungsangebotTyp angebot,
		int pensum,
		BigDecimal monatlicheBetreuungskosten,
		Integer monatlicheHauptmahlzeiten,
		Integer monatlicheNebenmahlzeiten
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
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(monatlicheHauptmahlzeiten);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(monatlicheNebenmahlzeiten);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);

		ErweiterteBetreuungContainer container = TestDataUtil.createDefaultErweiterteBetreuungContainer();
		container.setBetreuung(betreuung);
		betreuung.setErweiterteBetreuungContainer(container);

		return betreuung;
	}
}
