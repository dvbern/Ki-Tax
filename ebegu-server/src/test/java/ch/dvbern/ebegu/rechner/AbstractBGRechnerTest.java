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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
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
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.AbstractTestfall;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_05;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_06;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_07;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_08;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_09;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_10;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

/**
 * Superklasse für BG-Rechner-Tests
 */
public class AbstractBGRechnerTest {

	protected BetreuungsgutscheinEvaluator evaluator;

	private static final MathUtil MATH = MathUtil.DEFAULT;

	@Before
	public void setUpCalcuator() {
		evaluator = createEvaluator(TestDataUtil.createGesuchsperiode1718(), TestDataUtil.createGemeindeBern());
	}

	public static BetreuungsgutscheinEvaluator createEvaluator(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull Gemeinde bern) {
		Map<EinstellungKey, Einstellung> einstellungen = new HashMap<>();

		Einstellung paramMaxEinkommen = new Einstellung(EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN, "160000", gesuchsperiode);
		einstellungen.put(EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN, paramMaxEinkommen);

		Einstellung pmab3 = new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3, "3760", gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3, pmab3);

		Einstellung pmab4 = new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4, "5900", gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4, pmab4);

		Einstellung pmab5 = new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5, "6970", gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5, pmab5);

		Einstellung pmab6 = new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6, "7500", gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6, pmab6);

		Einstellung paramZuschlag = new Einstellung(EinstellungKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM, "20", gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM, paramZuschlag);

		Einstellung paramAbwesenheit = new Einstellung(EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT, "30", gesuchsperiode);
		einstellungen.put(EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT, paramAbwesenheit);

		Einstellung bgBisUndMitSchulstufe = new Einstellung(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, EinschulungTyp.VORSCHULALTER.name(), gesuchsperiode);
		einstellungen.put(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, bgBisUndMitSchulstufe);

		BetreuungsgutscheinConfigurator configurator = new BetreuungsgutscheinConfigurator();
		List<Rule> rules = configurator.configureRulesForMandant(bern, einstellungen);
		return new BetreuungsgutscheinEvaluator(rules);
	}

	public static void assertZeitabschnitt(VerfuegungZeitabschnitt abschnitt, int betreuungspensum, int anspruchsberechtigtesPensum, int bgPensum) {
		assertEquals("Beantragtes Pensum " + betreuungspensum + " entspricht nicht " + abschnitt,
			BigDecimal.valueOf(betreuungspensum), abschnitt.getBetreuungspensum());
		assertEquals(anspruchsberechtigtesPensum, abschnitt.getAnspruchberechtigtesPensum());
		assertEquals(BigDecimal.valueOf(bgPensum), abschnitt.getBgPensum());
	}

	public static void assertZeitabschnitt(VerfuegungZeitabschnitt abschnitt, int betreuungspensum, int anspruchsberechtigtesPensum, int bgPensum, double vollkosten,
		double verguenstigung, double elternbeitrag) {

		assertEquals("Beantragtes Pensum " + betreuungspensum + " entspricht nicht " + abschnitt,
			BigDecimal.valueOf(betreuungspensum), abschnitt.getBetreuungspensum());
		assertEquals(anspruchsberechtigtesPensum, abschnitt.getAnspruchberechtigtesPensum());
		assertEquals(BigDecimal.valueOf(bgPensum), abschnitt.getBgPensum());
		assertEquals(MATH.from(vollkosten), abschnitt.getVollkosten());
		assertEquals(MATH.from(verguenstigung), abschnitt.getVerguenstigung());
		assertEquals(MATH.from(elternbeitrag), abschnitt.getElternbeitrag());
	}

	public static void assertZeitabschnittFinanzdaten(VerfuegungZeitabschnitt abschnitt, double massgebendesEinkVorFamAbz,
		int einkommensjahr, double abzugFam, double massgebendesEinkommen,
		double famGroesse) {

		Assert.assertTrue(Objects.equals(einkommensjahr, abschnitt.getEinkommensjahr()));
		assertEquals(MATH.from(famGroesse), MATH.from(abschnitt.getFamGroesse()));
		assertEquals(MATH.from(massgebendesEinkVorFamAbz), MATH.from(abschnitt.getMassgebendesEinkommenVorAbzFamgr()));
		assertEquals(MATH.from(abzugFam), MATH.from(abschnitt.getAbzugFamGroesse()));
		assertEquals(MATH.from(massgebendesEinkommen), MATH.from(abschnitt.getMassgebendesEinkommen()));
	}

	/**
	 * Stellt alle für die Berechnung benötigten Parameter zusammen
	 */
	public static BGRechnerParameterDTO getParameter() {
		BGRechnerParameterDTO parameterDTO = new BGRechnerParameterDTO();
		parameterDTO.setMaxVerguenstigungVorschuleBabyProTg(MathUtil.GANZZAHL.from(140));
		parameterDTO.setMaxVerguenstigungVorschuleKindProTg(MathUtil.GANZZAHL.from(100));
		parameterDTO.setMaxVerguenstigungSchuleKindProTg(MathUtil.GANZZAHL.from(75));
		parameterDTO.setMaxVerguenstigungVorschuleBabyProStd(MathUtil.DEFAULT.from(11.90));
		parameterDTO.setMaxVerguenstigungVorschuleKindProStd(MathUtil.DEFAULT.from(8.50));
		parameterDTO.setMaxVerguenstigungSchuleKindProStd(MathUtil.DEFAULT.from(8.50));
		parameterDTO.setMaxMassgebendesEinkommen(MathUtil.GANZZAHL.from(160000));
		parameterDTO.setMinMassgebendesEinkommen(MathUtil.GANZZAHL.from(43000));
		parameterDTO.setOeffnungstageKita(MathUtil.GANZZAHL.from(240));
		parameterDTO.setOeffnungstageTFO(MathUtil.GANZZAHL.from(240));
		parameterDTO.setOeffnungsstundenTFO(MathUtil.GANZZAHL.from(11));
		parameterDTO.setZuschlagBehinderungProTg(MathUtil.GANZZAHL.from(50));
		parameterDTO.setZuschlagBehinderungProStd(MathUtil.DEFAULT.from(4.25));
		parameterDTO.setMinVerguenstigungProTg(MathUtil.GANZZAHL.from(7));
		parameterDTO.setMinVerguenstigungProStd(MathUtil.DEFAULT.from(0.70));
		return parameterDTO;
	}

	/**
	 * Erstellt eine Verfügung mit einem einzelnen Zeitabschnitt und den für Kita notwendigen Parametern zusammen
	 */
	protected Verfuegung prepareVerfuegungKita(
		@Nonnull LocalDate geburtsdatumKind,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		boolean eingeschult,
		boolean besondereBeduerfnisse,
		@Nonnull BigDecimal massgebendesEinkommen,
		@Nonnull BigDecimal monatlicheBetreuungskosten) {

		Betreuung betreuung = new Betreuung();
		betreuung.setErweiterteBeduerfnisse(besondereBeduerfnisse);
		Kind kind = new Kind();
		kind.setGeburtsdatum(geburtsdatumKind);
		kind.setEinschulungTyp(eingeschult ? EinschulungTyp.KLASSE1 : EinschulungTyp.VORSCHULALTER);
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
		zeitabschnitt.setAnspruchberechtigtesPensum(20);
		zeitabschnitt.setBetreuungspensum(BigDecimal.valueOf(20));
		zeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
		zeitabschnitt.setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
		List<VerfuegungZeitabschnitt> zeitabschnittList = new ArrayList<>();
		zeitabschnittList.add(zeitabschnitt);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitabschnittList);
		return verfuegung;
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall01WaeltiDagmar(Gesuch gesuch) {

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				if (betreuung.getInstitutionStammdaten().getInstitution().getId().equals(AbstractTestfall.ID_INSTITUTION_WEISSENSTEIN)) {
					Verfuegung verfuegung = betreuung.getVerfuegung();
					Assert.assertNotNull(verfuegung);
					assertEquals(12, verfuegung.getZeitabschnitte().size());
					assertEquals(MathUtil.GANZZAHL.from(MathUtil.DEFAULT.from(53872.35)), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
					// Erster Monat
					VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
					assertZeitabschnitt(august, 80, 80, 80, 1827.05, 1562.25, 264.80);
					// Letzter Monat
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, 80, 80, 80, 1827.05, 1562.25, 264.80);
					// Kein Anspruch mehr ab Februar
					VerfuegungZeitabschnitt februar = verfuegung.getZeitabschnitte().get(6);
					assertZeitabschnitt(februar, 0, 80, 0, 0, 0, 0);
				} else {     //KITA Bruennen
					Verfuegung verfuegung = betreuung.getVerfuegung();
					Assert.assertNotNull(verfuegung);
					assertEquals(12, verfuegung.getZeitabschnitte().size());
					assertEquals(MathUtil.GANZZAHL.from(MathUtil.DEFAULT.from(53872.35)), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
					// Noch kein Anspruch im Januar 2017, Kind geht erst ab Feb 2017 in Kita, Anspruch muss ausserdem 0 sein im Januar weil das Kind in die andere Kita geht
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, 0, 0, 0, 0, 0, 0);
					// Erster Monat
					VerfuegungZeitabschnitt februar = verfuegung.getZeitabschnitte().get(6);
					assertZeitabschnitt(februar, 40, 80, 40, 913.50, 781.10, 132.40);
					// Letzter Monat
					VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
					assertZeitabschnitt(juli, 40, 80, 40, 913.50, 781.10, 132.40);
				}
			}
		}
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne} auf
	 * korrekte berechnung zu pruefen
	 */
	public static void checkTestfall02FeutzYvonne(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if ("Leonard".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();
				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);

				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(MathUtil.GANZZAHL.from(MathUtil.DEFAULT.from(113745.70)), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, 40, 40, 40, 913.50, 366.90, 546.60);

				// Letzter Monat
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, 40, 40, 40, 913.50, 366.90, 546.60);
			}
			if ("Tamara".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();
				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);

				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(MathUtil.GANZZAHL.from(MathUtil.DEFAULT.from(113745.70)), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, 60, 60, 60, 1000.45, 362.75, 637.70);
				// Letzter Monat
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, 60, 60, 60, 1000.45, 362.75, 637.70);
			}
		}
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall03_PerreiraMarcia} auf
	 * korrekte berechnung zu pruefen
	 */
	public static void checkTestfall03PerreiraMarcia(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if ("Jose".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);
				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(MathUtil.GANZZAHL.from(69078.00), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, 50, 50, 50, 1141.90, 844.90, 297.00);
				// Letzter Monat
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, 50, 50, 50, 1141.90, 844.90, 297.00);
			}
		}
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall03_PerreiraMarcia} auf
	 * korrekte berechnung zu pruefen
	 */
	public static void checkTestfall04WaltherLaura(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if ("Jose".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);
				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(MathUtil.DEFAULT.from(162245.90), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, 50, 0, 0, 1141.90, 0, 1141.90);
				// Letzter Monat
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, 50, 0, 0, 1141.90, 0, 1141.90);
			}
		}
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall05LuethiMeret(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if ("Tanja".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);
				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(MathUtil.GANZZAHL.from(MathUtil.DEFAULT.from(98949.85)), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
				// Erster Monat 50%
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, 50, 70, 50, 1141.90, 586.60, 555.30);
				// Letzter Monat 50%
				VerfuegungZeitabschnitt dezember = verfuegung.getZeitabschnitte().get(4);
				assertZeitabschnitt(dezember, 50, 70, 50, 1141.90, 586.60, 555.30);
				// Erster Monat 60 %
				VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
				assertZeitabschnitt(januar, 60, 70, 60, 1370.30, 703.95, 666.35);
				// Letzter Monat 60 %
				VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
				assertZeitabschnitt(juli, 60, 70, 60, 1370.30, 703.95, 666.35);
			}
		}
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall03_PerreiraMarcia} auf
	 * korrekte berechnung zu pruefen
	 */
	public static void checkTestfall06BeckerNora(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if ("Timon".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);
				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(MathUtil.GANZZAHL.from(MathUtil.DEFAULT.from(-7520)), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, 100, 100, 100, 1667.40, 1562.40, 105.00);
			}
			if ("Yasmin".equals(kindContainer.getKindJA().getVorname())) {
				assertEquals(1, kindContainer.getBetreuungen().size());
				Betreuung betreuung = kindContainer.getBetreuungen().iterator().next();

				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);
				assertEquals(12, verfuegung.getZeitabschnitte().size());
				assertEquals(MathUtil.GANZZAHL.from(MathUtil.DEFAULT.from(-7520)), verfuegung.getZeitabschnitte().get(0).getMassgebendesEinkommen());
				// Erster Monat
				VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
				assertZeitabschnitt(august, 100, 60, 60, 1370.30, 1289.30, 81.00);
			}
		}
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall_ASIV_01} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_01(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Erster Monat
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 70000.00, 2015, 0, 70000, 2);
		// Letzter Monat vor Mutation
		VerfuegungZeitabschnitt oktober = verfuegung.getZeitabschnitte().get(3);
		assertZeitabschnittFinanzdaten(oktober, 70000.00, 2015, 0, 70000, 2);
		// Erster Monat nach Mutation
		VerfuegungZeitabschnitt november = verfuegung.getZeitabschnitte().get(5);
		assertZeitabschnittFinanzdaten(november, 100000, 2015, 11280, 88720, 3);
		// Letzter Monat
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(12);
		assertZeitabschnittFinanzdaten(juli, 100000, 2015, 11280, 88720, 3);
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall_ASIV_02} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_02(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Erster Monat
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 100000, 2015, 11280, 88720, 3);
		// Letzter Monat vor Mutation
		VerfuegungZeitabschnitt oktober = verfuegung.getZeitabschnitte().get(3);
		assertZeitabschnittFinanzdaten(oktober, 100000, 2015, 11280, 88720, 3);
		// Erster Monat nach Mutation
		VerfuegungZeitabschnitt november = verfuegung.getZeitabschnitte().get(5);
		assertZeitabschnittFinanzdaten(november, 70000.00, 2015, 0, 70000, 2);
		// Letzter Monat
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(12);
		assertZeitabschnittFinanzdaten(juli, 70000.00, 2015, 0, 70000, 2);
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall_ASIV_03} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_03(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Erster Monat
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 70000.00, 2015, 0, 70000, 2);
		// Letzter Monat vor EKV
		VerfuegungZeitabschnitt november = verfuegung.getZeitabschnitte().get(2);
		assertZeitabschnittFinanzdaten(november, 70000.00, 2015, 0, 70000, 2);
		// Erster Monat nach EKV
		VerfuegungZeitabschnitt dezember = verfuegung.getZeitabschnitte().get(3);
		assertZeitabschnittFinanzdaten(dezember, 49000, 2016, 0, 49000, 2);
		// Letzter Monat
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
		assertZeitabschnittFinanzdaten(juli, 49000, 2016, 0, 49000, 2);
	}

	/**
	 * hilfsmethode um den {@link ch.dvbern.ebegu.testfaelle.Testfall_ASIV_04} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_04(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Erster Monat
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 100000, 2015, 11280, 88720, 3);
		// Letzter Monat vor EKV
		VerfuegungZeitabschnitt november = verfuegung.getZeitabschnitte().get(2);
		assertZeitabschnittFinanzdaten(november, 100000, 2015, 11280, 88720, 3);
		// Erster Monat nach EKV
		VerfuegungZeitabschnitt dezember = verfuegung.getZeitabschnitte().get(3);
		assertZeitabschnittFinanzdaten(dezember, 49000, 2016, 11280, 37720, 3);
		// Letzter Monat
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(11);
		assertZeitabschnittFinanzdaten(juli, 49000, 2016, 11280, 37720, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_05} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_05(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Vor EKV
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 70000.00, 2015, 0, 70000, 2);
		// EKV
		VerfuegungZeitabschnitt oktober = verfuegung.getZeitabschnitte().get(3);
		assertZeitabschnittFinanzdaten(oktober, 49000, 2016, 0, 49000, 2);
		// Heirat
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(12);
		assertZeitabschnittFinanzdaten(juli, 79000, 2016, 11280, 67720, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_06} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_06(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Vor EKV
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 70000.00, 2015, 0, 70000, 2);
		// EKV
		VerfuegungZeitabschnitt oktober = verfuegung.getZeitabschnitte().get(3);
		assertZeitabschnittFinanzdaten(oktober, 49000, 2016, 0, 49000, 2);
		// Heirat
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(12);
		assertZeitabschnittFinanzdaten(juli, 120000, 2015, 11280, 108720, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_07} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_07(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Vor EKV
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 100000, 2015, 11280, 88720, 3);
		// EKV
		VerfuegungZeitabschnitt oktober = verfuegung.getZeitabschnitte().get(3);
		assertZeitabschnittFinanzdaten(oktober, 71000, 2016, 11280, 59720, 3);
		// Trennung
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(12);
		assertZeitabschnittFinanzdaten(juli, 49000, 2016, 0, 49000, 2);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_08} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_08(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Vor EKV
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 100000, 2015, 11280, 88720, 3);
		// EKV
		VerfuegungZeitabschnitt oktober = verfuegung.getZeitabschnitte().get(3);
		assertZeitabschnittFinanzdaten(oktober, 79000, 2016, 11280, 67720, 3);
		// Trennung
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(12);
		assertZeitabschnittFinanzdaten(juli, 70000, 2015, 0, 70000, 2);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_09} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_09(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Vor EKV
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 70000, 2015, 0, 70000, 2);
		// EKV
		VerfuegungZeitabschnitt oktober = verfuegung.getZeitabschnitte().get(7);
		assertZeitabschnittFinanzdaten(oktober, 100000, 2015, 11280, 88720, 3);
		// Heirat
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(12);
		assertZeitabschnittFinanzdaten(juli, 79000, 2017, 11280, 67720, 3);
	}

	/**
	 * hilfsmethode um den {@link Testfall_ASIV_10} auf korrekte berechnung zu pruefen
	 */
	public static void checkTestfall_ASIV_10(Gesuch gesuch) {
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegung();
		Assert.assertNotNull(verfuegung);
		// Vor EKV
		VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
		assertZeitabschnittFinanzdaten(august, 100000, 2015, 11280, 88720, 3);
		// EKV
		VerfuegungZeitabschnitt oktober = verfuegung.getZeitabschnitte().get(7);
		assertZeitabschnittFinanzdaten(oktober, 70000, 2015, 0, 70000, 2);
		// Trennung
		VerfuegungZeitabschnitt juli = verfuegung.getZeitabschnitte().get(12);
		assertZeitabschnittFinanzdaten(juli, 50000, 2017, 0, 50000, 2);
	}
}
