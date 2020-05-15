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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_TARIF;
import static ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator.createInitialenRestanspruch;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;

/**
 * Hilfsklasse fuer Ebegu-Rule-Tests
 */
public final class EbeguRuleTestsHelper {

	private static Gesuchsperiode gesuchsperiodeOfAllTimes = null;
	private static Map<EinstellungKey, Einstellung> einstellungenGemaessAsiv = getEinstellungenRulesAsiv(getGesuchsperiodeOfAllTimes());
	private static BetreuungsgutscheinConfigurator ruleConfigurator = new BetreuungsgutscheinConfigurator();
	private static KitaxUebergangsloesungParameter kitaxParams = TestDataUtil.geKitaxUebergangsloesungParameter();


	private static Gesuchsperiode getGesuchsperiodeOfAllTimes() {
		if (gesuchsperiodeOfAllTimes == null) {
			gesuchsperiodeOfAllTimes = new Gesuchsperiode();
			gesuchsperiodeOfAllTimes.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		}
		return gesuchsperiodeOfAllTimes;
	}

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
		// wird eigentlich nur noch in Tests verwendet, welche nicht direkt den Rechner aufrufen
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : result) {
			verfuegungZeitabschnitt.initBGCalculationResult();
		}
		return result;
	}

	public static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung, @Nonnull Map<EinstellungKey, Einstellung> einstellungenGemeinde) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenGemeinde);
	}

	public static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenGemaessAsiv);
	}

	public static List<VerfuegungZeitabschnitt> calculateInklAllgemeineRegeln(Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculateInklAllgemeineRegeln(betreuung, initialenRestanspruchAbschnitte);
	}

	/**
	 * Testhilfsmethode die eine Betreuung so berechnet als haette es vorher schon eine Betreuung gegeben welche einen Teil des anspruchs
	 * aufgebraucht hat, es wird  als bestehnder Restnanspruch der Wert von existingRestanspruch genommen
	 */
	public static List<VerfuegungZeitabschnitt> calculateWithRemainingRestanspruch(Betreuung betreuung, int existingRestanspruch) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : initialenRestanspruchAbschnitte) {
			verfuegungZeitabschnitt.setAnspruchspensumRestForAsivAndGemeinde(existingRestanspruch);
		}
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenGemaessAsiv);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung, List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte, @Nonnull Map<EinstellungKey, Einstellung> einstellungenGemeinde) {
		return calculateAllRules(betreuung, einstellungenGemeinde, initialenRestanspruchAbschnitte, false);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculateInklAllgemeineRegeln(Betreuung betreuung, List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte) {
		return calculateAllRules(betreuung, einstellungenGemaessAsiv, initialenRestanspruchAbschnitte, true);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculateAllRules(
		@Nonnull AbstractPlatz platz,
		@Nonnull Map<EinstellungKey, Einstellung> einstellungenGemeinde,
		@Nonnull List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte,
		boolean doMonatsstueckelungen
	) {
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(
			platz.extractGemeinde(), einstellungenGemeinde, kitaxParams, Locale.GERMAN);

		List<VerfuegungZeitabschnitt> result = initialenRestanspruchAbschnitte;

		for (Rule rule : rules) {
			result = rule.calculate(platz, result);
		}

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

	public static Map<EinstellungKey, Einstellung> getEinstellungenRulesAsiv(@Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EinstellungKey, Einstellung> einstellungen = new EnumMap<>(EinstellungKey.class);

		Einstellung paramMaxEinkommen = new Einstellung(EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
			EinstellungenDefaultWerteAsiv.EINSTELLUNG_MAX_EINKOMMEN,
			gesuchsperiode);
		einstellungen.put(EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN, paramMaxEinkommen);

		Einstellung pmab3 = new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
			EinstellungenDefaultWerteAsiv.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
			gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3, pmab3);

		Einstellung pmab4 = new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
			gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4, pmab4);

		Einstellung pmab5 = new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
			gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5, pmab5);

		Einstellung pmab6 = new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
			gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6, pmab6);

		Einstellung paramAbwesenheit = new Einstellung(EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT, "30",
			gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT, paramAbwesenheit);

		Einstellung bgBisUndMitSchulstufe = new Einstellung(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
			EinschulungTyp.KINDERGARTEN2.name(),
			gesuchsperiode);
		einstellungen.put(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, bgBisUndMitSchulstufe);

		Einstellung minErwerbspensumEingeschult = new Einstellung(
			EinstellungKey.MIN_ERWERBSPENSUM_EINGESCHULT,
			EinstellungenDefaultWerteAsiv.EINSTELLUNG_MIN_ERWERBSPENSUM_EINGESCHULT, gesuchsperiode);
		einstellungen.put(EinstellungKey.MIN_ERWERBSPENSUM_EINGESCHULT, minErwerbspensumEingeschult);

		Einstellung minErwerbspensumNichtEingeschult = new Einstellung(
			EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
			EinstellungenDefaultWerteAsiv.EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, gesuchsperiode);
		einstellungen.put(EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, minErwerbspensumNichtEingeschult);

		// Gemaess ASIV: Wir nehmen eben den ASIV Wert!
		Einstellung gmdeMinEwpEingeschult = new Einstellung(
			GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT,
			EinstellungenDefaultWerteAsiv.EINSTELLUNG_MIN_ERWERBSPENSUM_EINGESCHULT, gesuchsperiode);
		einstellungen.put(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT, gmdeMinEwpEingeschult);

		// Gemaess ASIV: Wir nehmen eben den ASIV Wert!
		Einstellung gmdeMinEwpNichtEingeschult = new Einstellung(
			GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
			EinstellungenDefaultWerteAsiv.EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, gesuchsperiode);
		einstellungen.put(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, gmdeMinEwpNichtEingeschult);

		Einstellung erwerbspensumZuschlag = new Einstellung(
			EinstellungKey.ERWERBSPENSUM_ZUSCHLAG, "20", gesuchsperiode);
		einstellungen.put(EinstellungKey.ERWERBSPENSUM_ZUSCHLAG, erwerbspensumZuschlag);

		Einstellung gmdeFreiwilligenarbeitEnabled = new Einstellung(
			GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED, "false", gesuchsperiode);
		einstellungen.put(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED, gmdeFreiwilligenarbeitEnabled);

		Einstellung gmdeMaxFreiwilligenarbeit = new Einstellung(
			GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT, "0", gesuchsperiode);
		einstellungen.put(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT, gmdeMaxFreiwilligenarbeit);

		return einstellungen;
	}

	public static Map<EinstellungKey, Einstellung> getEinstellungenRulesParis(@Nonnull Gesuchsperiode gesuchsperiode) {
		final Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(getEinstellungenRulesAsiv(gesuchsperiode));
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED).setValue("true");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT).setValue("20");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT).setValue("15");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT).setValue("30");
		return einstellungenGemeinde;
	}

	public static Map<EinstellungKey, Einstellung> getEinstellungeRechnerAsiv(@Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EinstellungKey, Einstellung> einstellungen = new EnumMap<>(EinstellungKey.class);

		Einstellung maxTarifTsMitBetreuung = new Einstellung(
			MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG, "12.24", gesuchsperiode);
		einstellungen.put(MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG, maxTarifTsMitBetreuung);

		Einstellung maxTarifTsOhneBetreuung = new Einstellung(
			MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG, "6.11", gesuchsperiode);
		einstellungen.put(MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG, maxTarifTsOhneBetreuung);

		Einstellung minTarifTs = new Einstellung(
			MIN_TARIF, "0.78", gesuchsperiode);
		einstellungen.put(MIN_TARIF, minTarifTs);

		return einstellungen;
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
