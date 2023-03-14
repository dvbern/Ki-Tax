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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPersonEntity;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinConfigurator;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.testfaelle.Testfall03_PerreiraMarcia;
import ch.dvbern.ebegu.testfaelle.Testfall04_WaltherLaura;
import ch.dvbern.ebegu.testfaelle.Testfall05_LuethiMeret;
import ch.dvbern.ebegu.testfaelle.Testfall06_BeckerNora;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_01;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_02;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_03;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_04;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_05;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_06;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_07;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_08;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_09;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_10;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.RuleParameterUtil;
import ch.dvbern.ebegu.util.TestUtils;
import org.junit.Before;

import static ch.dvbern.ebegu.testfaelle.AbstractTestfall.ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA;
import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Superklasse für BG-Rechner-Tests
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public abstract class AbstractBGRechnerTest {

	protected BetreuungsgutscheinEvaluator evaluator;

	private static final MathUtil MATH = MathUtil.DEFAULT;
	private static final long VOLLKOSTEN_DEFAULT = 2000;
	private static final long VOLLKOSTEN_NULL = 0;
	protected static final int BASISJAHR = 2016;
	protected static final int BASISJAHR_PLUS_1 = 2017;
	protected static final int BASISJAHR_PLUS_2 = 2018;

	// Wir sollten fuer Tests nicht gerade Paris als Beispiel nehmen, sondern eine
	// Gemeinde, die moeglichst wenig Spezialfaelle hat
	protected Gemeinde gemeindeOfEvaluator = TestDataUtil.createGemeindeLondon();
	protected Gesuchsperiode gesuchsperiodeOfEvaluator = TestDataUtil.createGesuchsperiode1718();


	@Before
	public void setUpCalcuator() {
		evaluator = createEvaluator(gesuchsperiodeOfEvaluator, gemeindeOfEvaluator);
	}

	public static BetreuungsgutscheinEvaluator createEvaluator(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde bern
	) {
		Map<EinstellungKey, Einstellung> einstellungen = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(gesuchsperiode);
		RuleParameterUtil ruleParameterUtil = new RuleParameterUtil(einstellungen, TestDataUtil.geKitaxUebergangsloesungParameter());

		BetreuungsgutscheinConfigurator configurator = new BetreuungsgutscheinConfigurator();
		List<Rule> rules = configurator.configureRulesForMandant(bern, ruleParameterUtil);
		return new BetreuungsgutscheinEvaluator(rules, einstellungen);
	}

	public static void assertZeitabschnitt(
		VerfuegungZeitabschnitt abschnitt,
		BigDecimal betreuungspensum,
		int anspruchsberechtigtesPensum,
		BigDecimal bgPensum) {
		assertEquals("Beantragtes Pensum " + betreuungspensum + " entspricht nicht " + abschnitt,
			MathUtil.DEFAULT.from(betreuungspensum), MathUtil.DEFAULT.from(abschnitt.getBetreuungspensumProzent()));
		assertEquals(anspruchsberechtigtesPensum, abschnitt.getAnspruchberechtigtesPensum());
		assertEquals(MathUtil.DEFAULT.from(bgPensum), MathUtil.DEFAULT.from(abschnitt.getBgPensum()));
	}

	public static void assertZeitabschnitt(
		VerfuegungZeitabschnitt abschnitt,
		int betreuungspensum,
		int anspruchsberechtigtesPensum,
		int bgPensum,
		int anspruchspensumRest) {
		assertEquals("Beantragtes Pensum " + betreuungspensum + " entspricht nicht " + abschnitt,
			MathUtil.DEFAULT.from(betreuungspensum), MathUtil.DEFAULT.from(abschnitt.getBetreuungspensumProzent()));
		assertEquals(anspruchsberechtigtesPensum, abschnitt.getAnspruchberechtigtesPensum());
		assertEquals(MathUtil.DEFAULT.from(bgPensum), MathUtil.DEFAULT.from(abschnitt.getBgPensum()));
		assertEquals(MathUtil.DEFAULT.from(anspruchspensumRest),
			MathUtil.DEFAULT.from(abschnitt.getRelevantBgCalculationInput().getAnspruchspensumRest()));
	}

	protected void assertZeitabschnitt(
		@Nonnull VerfuegungZeitabschnitt abschnitt,
		@Nonnull LocalDate gueltigAb,
		int betreuungspensum,
		int anspruch,
		int bgPensum
	) {
		assertEquals(gueltigAb, abschnitt.getGueltigkeit().getGueltigAb());
		assertEquals(betreuungspensum, abschnitt.getBetreuungspensumProzent().intValue());
		assertEquals(anspruch, abschnitt.getAnspruchberechtigtesPensum());
		assertEquals(bgPensum, abschnitt.getBgPensum().intValue());
	}

	public static void assertZeitabschnitt(
		VerfuegungZeitabschnitt abschnitt,
		BigDecimal betreuungspensum,
		int anspruchsberechtigtesPensum,
		BigDecimal bgPensum,
		double vollkosten,
		double verguenstigung,
		double elternbeitrag) {

		assertEquals("Beantragtes Pensum " + betreuungspensum + " entspricht nicht " + abschnitt,
			MathUtil.DEFAULT.from(betreuungspensum), MathUtil.DEFAULT.from(abschnitt.getBetreuungspensumProzent()));
		assertEquals(anspruchsberechtigtesPensum, abschnitt.getAnspruchberechtigtesPensum());
		assertEquals(MathUtil.DEFAULT.from(bgPensum), MathUtil.DEFAULT.from(abschnitt.getBgPensum()));
		assertEquals(MATH.from(vollkosten), abschnitt.getVollkosten());
		assertEquals(MATH.from(verguenstigung), abschnitt.getVerguenstigung());
		assertEquals(MATH.from(elternbeitrag), abschnitt.getElternbeitrag());
	}

	public static void assertZeitabschnittFinanzdaten(
		VerfuegungZeitabschnitt abschnitt, LocalDate gueltigAb, double massgebendesEinkVorFamAbz,
		int einkommensjahr, double abzugFam, double massgebendesEinkommen,
		double famGroesse) {

		assertEquals(gueltigAb, abschnitt.getGueltigkeit().getGueltigAb());
		assertEquals(einkommensjahr, abschnitt.getEinkommensjahr().intValue());
		assertEquals(MATH.from(famGroesse), MATH.from(abschnitt.getFamGroesse()));
		assertEquals(MATH.from(massgebendesEinkVorFamAbz),
			MATH.from(abschnitt.getMassgebendesEinkommenVorAbzFamgr()));
		assertEquals(MATH.from(abzugFam), MATH.from(abschnitt.getAbzugFamGroesse()));
		assertEquals(MATH.from(massgebendesEinkommen), MATH.from(abschnitt.getMassgebendesEinkommen()));
	}

	private static BGRechnerParameterDTO createParameterDTO() {
		BGRechnerParameterDTO dto = new BGRechnerParameterDTO();
		dto.setMaxVerguenstigungVorschuleBabyProTg(MathUtil.GANZZAHL.from(150));
		dto.setMaxVerguenstigungVorschuleKindProTg(MathUtil.GANZZAHL.from(100));
		dto.setMaxVerguenstigungKindergartenKindProTg(MathUtil.GANZZAHL.from(75));
		dto.setMaxVerguenstigungVorschuleBabyProStd(MathUtil.DEFAULT.from(12.75));
		dto.setMaxVerguenstigungVorschuleKindProStd(MathUtil.DEFAULT.from(8.50));
		dto.setMaxVerguenstigungKindergartenKindProStd(MathUtil.DEFAULT.from(8.50));
		dto.setMaxVerguenstigungPrimarschuleKindProStd(MathUtil.DEFAULT.from(8.50));
		dto.setMaxMassgebendesEinkommen(MathUtil.GANZZAHL.from(160000));
		dto.setMinMassgebendesEinkommen(MathUtil.GANZZAHL.from(43000));
		dto.setOeffnungstageKita(MathUtil.GANZZAHL.from(240));
		dto.setOeffnungstageTFO(MathUtil.GANZZAHL.from(240));
		dto.setOeffnungsstundenTFO(MathUtil.GANZZAHL.from(11));
		dto.setZuschlagBehinderungProTg(MathUtil.GANZZAHL.from(50));
		dto.setZuschlagBehinderungProStd(MathUtil.DEFAULT.from(4.25));
		dto.setMinVerguenstigungProTg(MathUtil.GANZZAHL.from(7));
		dto.setMinVerguenstigungProStd(MathUtil.DEFAULT.from(0.70));
		dto.setMaxTarifTagesschuleMitPaedagogischerBetreuung(MathUtil.DEFAULT.from(12.24));
		dto.setMaxTarifTagesschuleOhnePaedagogischerBetreuung(MathUtil.DEFAULT.from(6.11));
		dto.setMinTarifTagesschule(MathUtil.DEFAULT.from(0.78));
		dto.getGemeindeParameter().setGemeindeZusaetzlicherGutscheinEnabled(false);
		dto.getMahlzeitenverguenstigungParameter().setEnabled(false);
		dto.getGemeindeParameter().setGemeindePauschalbetragEnabled(false);
		return dto;
	}

	/**
	 * Stellt alle für die Berechnung benötigten Parameter zusammen
	 */
	public static BGRechnerParameterDTO getParameter() {
		return TestUtils.getParameter();
	}

	/**
	 * Stellt alle für die Berechnung benötigten Parameter für Luzern zusammen
	 */
	public static BGRechnerParameterDTO getRechnerParameterLuzern() {
		BGRechnerParameterDTO defaultParameter = getParameter();

		//SET Parameters for LU
		defaultParameter.setMinVerguenstigungProTg(BigDecimal.valueOf(15));
		defaultParameter.setMinMassgebendesEinkommen(BigDecimal.valueOf(48000));
		defaultParameter.setMaxMassgebendesEinkommen(BigDecimal.valueOf(125000));
		defaultParameter.setMinVerguenstigungProStd(BigDecimal.valueOf(0.7));
		defaultParameter.setOeffnungstageKita(BigDecimal.valueOf(246));
		defaultParameter.setOeffnungstageTFO(BigDecimal.valueOf(246));
		defaultParameter.setOeffnungsstundenTFO(BigDecimal.valueOf(11));
		return defaultParameter;
	}

	public static  BGRechnerParameterDTO getRechnerParamterAppenzell() {
		BGRechnerParameterDTO defaultParameter = getParameter();

		//SET Parameters for AR
		defaultParameter.setMaxVerguenstigungVorschuleBabyProStd(BigDecimal.valueOf(13.50));
		defaultParameter.setMaxVerguenstigungVorschuleKindProStd(BigDecimal.valueOf(11.50));

		return defaultParameter;
	}

	/**
	 * Erstellt eine Verfügung mit einem einzelnen Zeitabschnitt und den für Kita notwendigen Parametern zusammen
	 */
	protected Verfuegung prepareVerfuegungKita(
		@Nonnull LocalDate geburtsdatumKind,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		@Nonnull EinschulungTyp einschulungTyp,
		boolean besondereBeduerfnisse,
		@Nonnull BigDecimal massgebendesEinkommen,
		@Nonnull BigDecimal monatlicheBetreuungskosten) {

		Betreuung betreuung = new Betreuung();
		ErweiterteBetreuungContainer erweiterteBetreuungContainer = new ErweiterteBetreuungContainer();
		ErweiterteBetreuung erweiterteBetreuung = new ErweiterteBetreuung();
		erweiterteBetreuung.setErweiterteBeduerfnisse(besondereBeduerfnisse);
		erweiterteBetreuungContainer.setErweiterteBetreuungJA(erweiterteBetreuung);
		betreuung.setErweiterteBetreuungContainer(erweiterteBetreuungContainer);
		Kind kind = new Kind();
		kind.setGeburtsdatum(geburtsdatumKind);
		kind.setEinschulungTyp(einschulungTyp);
		KindContainer kindContainer = new KindContainer();
		kindContainer.setKindJA(kind);
		Gesuch gesuch = new Gesuch();
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		boolean isSecondHalbjahr = LocalDate.now().isAfter(LocalDate.of(LocalDate.now().getYear(), Month.JULY, 31));
		int startyear = isSecondHalbjahr ? LocalDate.now().getYear() : LocalDate.now().getYear() - 1;
		LocalDate start = LocalDate.of(startyear, Month.AUGUST, 1);
		LocalDate end = LocalDate.of(startyear + 1, Month.JULY, 31);
		gesuchsperiode.setGueltigkeit(new DateRange(start, end));
		gesuch.setGesuchsperiode(gesuchsperiode);
		kindContainer.setGesuch(gesuch);
		betreuung.setKind(kindContainer);

		Verfuegung verfuegung = createVerfuegung(von, bis, massgebendesEinkommen, monatlicheBetreuungskosten);
		verfuegung.setBetreuung(betreuung);
		return verfuegung;
	}

	/**
	 * Erstellt eine Verfügung mit den übergebenen Parametern
	 */
	private Verfuegung createVerfuegung(
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		@Nonnull BigDecimal massgebendesEinkommen,
		@Nonnull BigDecimal monatlicheBetreuungskosten) {

		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(new DateRange(von, bis));
		zeitabschnitt.getBgCalculationInputAsiv().setAnspruchspensumProzent(20);
		zeitabschnitt.getBgCalculationInputAsiv().setBetreuungspensumProzent(BigDecimal.valueOf(20));
		zeitabschnitt.getBgCalculationInputAsiv().setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
		zeitabschnitt.getBgCalculationInputAsiv().setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
		List<VerfuegungZeitabschnitt> zeitabschnittList = new ArrayList<>();
		zeitabschnittList.add(zeitabschnitt);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitabschnittList);
		return verfuegung;
	}

	/**
	 * hilfsmethode um den {@link Testfall01_WaeltiDagmar} auf korrekte berechnung zu
	 * pruefen
	 */
	public static void checkTestfall01WaeltiDagmar(Gesuch gesuch) {

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				if (betreuung.getInstitutionStammdaten().getId().equals(ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA)) {
					Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
					assertNotNull(verfuegung);
					assertEquals(12, verfuegung.getZeitabschnitte().size());
					assertEquals(53872,
						verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen().intValue());
					// Erster Monat
					VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
					assertZeitabschnitt(august, new BigDecimal("80.00"), 80 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("80.00"), VOLLKOSTEN_DEFAULT, 1451.30, 548.70);
					// Letzter Monat
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, new BigDecimal("80.00"), 80 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("80.00"), VOLLKOSTEN_DEFAULT, 1451.30, 548.70);
					// Kein Anspruch mehr ab Februar
					VerfuegungZeitabschnitt februar = verfuegung.getZeitabschnitte().get(6);
					assertZeitabschnitt(februar, MathUtil.DEFAULT.from(0.00), 80 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS,MathUtil.DEFAULT.from(0.00), VOLLKOSTEN_NULL, 0, 0);
				} else {     //KITA Bruennen
					Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
					assertNotNull(verfuegung);
					assertEquals(12, verfuegung.getZeitabschnitte().size());
					assertEquals(53872,
						verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen().intValue());
					// Noch kein Anspruch im Januar 2017, Kind geht erst ab Feb 2017 in Kita, Anspruch muss ausserdem
					// 0 sein im Januar weil das Kind in die andere Kita geht
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, MathUtil.DEFAULT.from(0.00), 20, MathUtil.DEFAULT.from(0.00), VOLLKOSTEN_NULL, 0, 0);
					// Erster Monat
					VerfuegungZeitabschnitt februar = verfuegung.getZeitabschnitte().get(6);
					assertZeitabschnitt(februar, new BigDecimal("40.00"), 100, new BigDecimal("40.00"), VOLLKOSTEN_DEFAULT, 725.65, 1274.35);
					// Letzter Monat
					VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
					assertZeitabschnitt(juli, new BigDecimal("40.00"), 100, new BigDecimal("40.00"), VOLLKOSTEN_DEFAULT, 725.65, 1274.35);
				}
			}
		}
	}

	/**
	 * hilfsmethode um den {@link Testfall02_FeutzYvonne} auf
	 * korrekte berechnung zu pruefen
	 */
	public static void checkTestfall02FeutzYvonne(Gesuch gesuch) {
		Set<KindContainer> kinderList = gesuch.getKindContainers();
		assertEquals(2, kinderList.size());
		assertEquals(2,
			kinderList.stream().map(KindContainer::getKindJA).map(AbstractPersonEntity::getVorname).filter(s -> s.equals("Leonard") || s.equals(
			"Tamara")).count());
		for (KindContainer kindContainer : kinderList) {
			if ("Leonard".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();
				Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
				assertNotNull(verfuegung);

				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(113346,
					verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen().intValue());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, new BigDecimal("40.00"), 40 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("40.00"), VOLLKOSTEN_DEFAULT, 319.00, 1681.00);

				// Letzter Monat
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, new BigDecimal("40.00"), 40 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("40.00"), VOLLKOSTEN_DEFAULT, 319.00, 1681.00);
			}
			if ("Tamara".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();
				Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
				assertNotNull(verfuegung);

				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(113346,
					verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen().intValue());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, new BigDecimal("60.00"), 40 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal(40.00 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS), VOLLKOSTEN_DEFAULT, 478.50, 1521.50);
				// Letzter Monat
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, new BigDecimal("60.00"), 40 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal(40.00 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS), VOLLKOSTEN_DEFAULT, 478.50, 1521.50);
			}
		}
	}

	/**
	 * hilfsmethode um den {@link Testfall03_PerreiraMarcia} auf
	 * korrekte berechnung zu pruefen
	 */
	public static void checkTestfall03PerreiraMarcia(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if ("Jose".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

				Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
				assertNotNull(verfuegung);
				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(68678,
					verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen().intValue());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, new BigDecimal("50.00"), 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("50.00"), VOLLKOSTEN_DEFAULT, 780.55, 1219.45);
				// Letzter Monat
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, new BigDecimal("50.00"), 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("50.00"), VOLLKOSTEN_DEFAULT, 780.55, 1219.45);
			}
		}
	}

	/**
	 * hilfsmethode um den {@link Testfall04_WaltherLaura} auf
	 * korrekte berechnung zu pruefen
	 */
	public static void checkTestfall04WaltherLaura(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if ("Lorenz".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

				Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
				assertNotNull(verfuegung);
				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(MathUtil.DEFAULT.from(162126.00),
					verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
				// Das Einkommen ist zu hoch. Es werden alle Beträge auf 0 gesetzt, die Familie bezahlt die Vollkosten
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, new BigDecimal("50.00"), 0, MathUtil.DEFAULT.from(0.00), 0, 0, 0);
				// Letzter Monat
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, new BigDecimal("50.00"), 0, MathUtil.DEFAULT.from(0.00), 0, 0, 0);
			} else  {
				fail("Nur ein Kind names Lorenz sollte vorhanden sein!");
			}
		}
	}

	/**
	 * hilfsmethode um den {@link Testfall05_LuethiMeret} auf korrekte berechnung zu
	 * pruefen
	 */
	public static void checkTestfall05LuethiMeret(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if ("Tanja".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

				Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
				assertNotNull(verfuegung);
				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(98830,
					verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen().intValue());
				// Erster Monat 50%
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, new BigDecimal("50.00"), 70 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("50.00"), VOLLKOSTEN_DEFAULT, 522.80, 1477.20);
				// Letzter Monat 50%
				VerfuegungZeitabschnitt dezember = verfuegung.getZeitabschnitte().get(4);
				assertZeitabschnitt(dezember, new BigDecimal("50.00"), 70 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("50.00"), VOLLKOSTEN_DEFAULT, 522.80, 1477.20);
				// Erster Monat 60 %
				VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
				assertZeitabschnitt(januar, new BigDecimal("60.00"), 70 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("60.00"), VOLLKOSTEN_DEFAULT, 627.40, 1372.60);
				// Letzter Monat 60 %
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, new BigDecimal("60.00"), 70 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal("60.00"), VOLLKOSTEN_DEFAULT, 627.40, 1372.60);
			}
		}
	}

	/**
	 * hilfsmethode um den {@link Testfall06_BeckerNora} auf
	 * korrekte berechnung zu pruefen
	 */
	public static void checkTestfall06BeckerNora(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			checkKindOfNora(kindContainer, "Timon");
			checkKindOfNora(kindContainer, "Yasmin");
		}
	}

	private static void checkKindOfNora(KindContainer kindContainer, String kindName) {
		if (kindName.equals(kindContainer.getKindJA().getVorname())) {
			assertEquals(1, kindContainer.getBetreuungen().size());
			Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

			Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
			assertNotNull(verfuegung);
			assertEquals(12, verfuegung.getZeitabschnitte().size());
			assertEquals(-7600, verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen().intValue());
			// Erster Monat
			VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
			assertZeitabschnitt(august, new BigDecimal("100.00"), 60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, new BigDecimal(60.00 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS), 1600, 1488, 112);
		}
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_01} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_01(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 70000, BASISJAHR, 0, 70000, 2);
		// Heirat
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 100000, BASISJAHR, 11400, 88600, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_02} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_02(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 100000, BASISJAHR, 11400, 88600, 3);
		// Scheidung
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 70000, BASISJAHR, 0, 70000, 2);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_03} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_03(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 70000, BASISJAHR, 0, 70000, 2);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_04} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_04(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 49000, BASISJAHR_PLUS_1, 11400, 37600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 49000, BASISJAHR_PLUS_1, 11400, 37600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 49000, BASISJAHR_PLUS_1, 11400, 37600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 49000, BASISJAHR_PLUS_1, 11400, 37600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 49000, BASISJAHR_PLUS_1, 11400, 37600, 3);
		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 100000, BASISJAHR, 11400, 88600, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_05} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_05(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 70000, BASISJAHR, 0, 70000, 2);
		// Heirat: 15.01.
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 100000, BASISJAHR, 11400, 88600, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_06} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_06(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 49000, BASISJAHR_PLUS_1, 0, 49000, 2);
		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 70000, BASISJAHR, 0, 70000, 2);
		// Heirat: 15.01.
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 120000, BASISJAHR, 11400, 108600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 120000, BASISJAHR, 11400, 108600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 120000, BASISJAHR, 11400, 108600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 120000, BASISJAHR, 11400, 108600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 120000, BASISJAHR, 11400, 108600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 120000, BASISJAHR, 11400, 108600, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_07} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_07(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 71000, BASISJAHR_PLUS_1, 11400, 59600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 71000, BASISJAHR_PLUS_1, 11400, 59600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 71000, BASISJAHR_PLUS_1, 11400, 59600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 71000, BASISJAHR_PLUS_1, 11400, 59600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 71000, BASISJAHR_PLUS_1, 11400, 59600, 3);
		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		// Scheidung: 15.01.
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 70000, BASISJAHR, 0, 70000, 2);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_08} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_08(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 79000, BASISJAHR_PLUS_1, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 79000, BASISJAHR_PLUS_1, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 79000, BASISJAHR_PLUS_1, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 79000, BASISJAHR_PLUS_1, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 79000, BASISJAHR_PLUS_1, 11400, 67600, 3);
		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 100000, BASISJAHR, 11400, 88600, 3);
		// Scheidung: 15.01.
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 70000, BASISJAHR, 0, 70000, 2);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_09} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_09(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(13, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 70000, BASISJAHR, 0, 70000, 2);
		// Betreuungsbeginn
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 15), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 70000, BASISJAHR, 0, 70000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 70000, BASISJAHR, 0, 70000, 2);
		// EKV (gilt für das ganze Jahr)
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 50000, BASISJAHR_PLUS_2, 0, 50000, 2);
		// Heirat: 15.01.
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 79000, BASISJAHR_PLUS_2, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 79000, BASISJAHR_PLUS_2, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 79000, BASISJAHR_PLUS_2, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 79000, BASISJAHR_PLUS_2, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 79000, BASISJAHR_PLUS_2, 11400, 67600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 79000, BASISJAHR_PLUS_2, 11400, 67600, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_10} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_10(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(13, result.size());
		int i = 0;

		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 100000, BASISJAHR, 11400, 88600, 3);
		// Betreuungsstart: 15.08.
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 15), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 100000, BASISJAHR, 11400, 88600, 3);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 100000, BASISJAHR, 11400, 88600, 3);
		// EKV (gilt für das ganze Jahr)
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 50000, BASISJAHR_PLUS_2, 11400, 38600, 3);
		// Scheidung: 15.01.
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 50000, BASISJAHR_PLUS_2, 0, 50000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 50000, BASISJAHR_PLUS_2, 0, 50000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 50000, BASISJAHR_PLUS_2, 0, 50000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 50000, BASISJAHR_PLUS_2, 0, 50000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 50000, BASISJAHR_PLUS_2, 0, 50000, 2);
		assertZeitabschnittFinanzdaten(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 50000, BASISJAHR_PLUS_2, 0, 50000, 2);
	}

	public static void checkTestfall_ASIV_11_MZV(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();
		assertNotNull(result);
		assertEquals(12, result.size());
		for(int i = 0; i < 12; i++){
			final BGCalculationResult bgResult = result.get(i).getBgCalculationResultGemeinde();
			assertNotNull(bgResult);
			assertEquals(bgResult.getVerguenstigungMahlzeitenTotal(), BigDecimal.valueOf(60));
		}
	}

	public static void checkTestfall_ASIV_11_MZV_NICHT_EINTRETEN(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(12, result.size());
		// wegen die Mutation die gilt ab September und die Mahlzeitkosten ab August veraendert sollte trotzdem noch
		// keine MZV in ersten Abschnitt sein
		final BGCalculationResult bgResult0 = result.get(0).getBgCalculationResultGemeinde();
		assertNotNull(bgResult0);
		assertEquals(bgResult0.getVerguenstigungMahlzeitenTotal(), BigDecimal.valueOf(0));
		// aber ab September sollte wie normal die MVZ gegeben werden
		for(int i = 1; i < 12; i++){
			final BGCalculationResult bgResult = result.get(i).getBgCalculationResultGemeinde();
			assertNotNull(bgResult);
			assertEquals(bgResult.getVerguenstigungMahlzeitenTotal(), BigDecimal.valueOf(60));
		}
	}

	public static void checkTestfall_ASIV_12_MZV_Untermonatlich(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
		List<VerfuegungZeitabschnitt> result = betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte();

		assertNotNull(result);
		assertEquals(13, result.size());
		// erstes Abschnitt noch keine Betreuung
		final BGCalculationResult bgResult0 = result.get(0).getBgCalculationResultGemeinde();
		assertNotNull(bgResult0);
		assertEquals(bgResult0.getVerguenstigungMahlzeitenTotal(), BigDecimal.valueOf(0));
		// wegen die Mutation die gilt ab September soll in der letzten Zeitabschnitt von August noch keine Mahlzeitverguenstigung geben
		final BGCalculationResult bgResult1 = result.get(1).getBgCalculationResultGemeinde();
		assertNotNull(bgResult1);
		assertEquals(bgResult1.getVerguenstigungMahlzeitenTotal(), BigDecimal.valueOf(0));
		// aber ab September sollte wie normal die MVZ gegeben werden
		for(int i = 2; i < 13; i++){
			final BGCalculationResult bgResult = result.get(i).getBgCalculationResultGemeinde();
			assertNotNull(bgResult);
			assertEquals(bgResult.getVerguenstigungMahlzeitenTotal(), BigDecimal.valueOf(60));
		}
	}
}
