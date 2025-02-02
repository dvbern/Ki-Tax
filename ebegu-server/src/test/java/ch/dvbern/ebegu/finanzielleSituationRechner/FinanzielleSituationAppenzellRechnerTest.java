/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FinanzielleSituationAppenzellRechnerTest {

	private FinanzielleSituationAppenzellRechner finSitRechner = new FinanzielleSituationAppenzellRechner();

	/**
	 * Steuerbares Einkommen										 60'000
	 * Steuerbares Vermögen	15'000, 15% =			       			+ 2'250
	 * Säule 3a, sofern hauptberuflich BVG versichert	   			+ 1'000
	 * Säule 3a von Personen, die keiner BVG angehören	   			+ 1'000
	 * Einkaufsbeiträge berufliche Vorsorge				   			+ 2'000
	 * Liegenschaftsaufwand 							   			+ 2'000
	 * Einkünfte BGSA, vereinfacht abgerechnet			   			+ 3'000
	 * Vorjahresverluste								   			+ 3'000
	 * Mitgliederbeiträge und Zuwendungen an politische Parteien	+ 4'000
	 * Leistungen an juristische Personen in der Schweiz			+ 4'000
	 *                                                     			-------
	 *                                                      		82'250
	 *                                                     					*/
	@Test
	public void testAlleWertVorhanden() {
		Gesuch gesuch = prepareGesuch(false);
		finSitRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250)));

		//zwei Antragstellende, beides ueberpruefen
		gesuch = prepareGesuch(true);
		finSitRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(164500)));
	}

	/**
	 * In case der Rechner ist angerufen bevor der FinSit bekannt ist sollte keinen Fehler verursachen
	 */
	@Test
	public void testNullableWertVorhanden() {
		Gesuch gesuch = prepareGesuch(false);
		assert gesuch.getGesuchsteller1() != null;
		assert gesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null;
		FinanzielleSituation emptyFinanzielleSituationForTest = new FinanzielleSituation();
		emptyFinanzielleSituationForTest.setFinSitZusatzangabenAppenzell(new FinSitZusatzangabenAppenzell());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(emptyFinanzielleSituationForTest);
		finSitRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(0)));
	}

	@Test
	public void testSpezialFallNichtGeteilteObhut() {
		Gesuch gesuch = prepareGesuch(false);
		setSpezialFallNichtGeteilteObhut(gesuch);

		setARDatenPartnerHalfed(gesuch);
		finSitRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250 + 82250 / 2)));

	}

	@Test
	public void testSpezialFallGeteilteObhut() {
		Gesuch gesuch = prepareGesuch(false);
		setSpezialfallGeteilteObhut(gesuch);

		setARDatenPartnerHalfed(gesuch);
		finSitRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250 + 82250 / 2)));

	}

	@Test
	public void testEKVAlleine() {
		Gesuch gesuch = prepareGesuch(false);
		assert gesuch.getGesuchsteller1() != null;
		TestUtils.prepareEKVInfoTwoYears(gesuch);
		TestUtils.prepareEKVTwoYears(gesuch.getGesuchsteller1());
		setEkvARDatenGS(gesuch.getGesuchsteller1());

		finSitRechner.calculateFinanzDaten(gesuch, BigDecimal.valueOf(20));
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250)));
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjP1VorAbzFamGr(), is(BigDecimal.valueOf(82250 / 2)));
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjP2VorAbzFamGr(), is(BigDecimal.valueOf(82250 / 2)));
	}

	@Test
	public void testEKVZweiGS() {
		Gesuch gesuch = prepareGesuch(true);
		assert gesuch.getGesuchsteller1() != null;
		assert gesuch.getGesuchsteller2() != null;
		TestUtils.prepareEKVInfoTwoYears(gesuch);
		TestUtils.prepareEKVTwoYears(gesuch.getGesuchsteller1());
		TestUtils.prepareEKVTwoYears(gesuch.getGesuchsteller2());
		setEkvARDatenGS(gesuch.getGesuchsteller1());
		setEkvARDatenGS(gesuch.getGesuchsteller2());

		finSitRechner.calculateFinanzDaten(gesuch, BigDecimal.valueOf(20));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250 * 2)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjP1VorAbzFamGr(), is(BigDecimal.valueOf(82250)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjP2VorAbzFamGr(), is(BigDecimal.valueOf(82250)));
	}

	@Test
	public void testEKVSpezialfallGeteilteObhut() {
		Gesuch gesuch = prepareGesuch(false);
		assert gesuch.getGesuchsteller1() != null;
		setSpezialfallGeteilteObhut(gesuch);
		TestUtils.prepareEKVInfoTwoYears(gesuch);
		TestUtils.prepareEKVTwoYears(gesuch.getGesuchsteller1());
		setARDatenPartnerHalfed(gesuch);
		setEkvARDatenGS(gesuch.getGesuchsteller1());
		setEkvARDatenPartner(gesuch.getGesuchsteller1());

		finSitRechner.calculateFinanzDaten(gesuch, BigDecimal.valueOf(20));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(82250 + 82250 / 2)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjP1VorAbzFamGr(), is(BigDecimal.valueOf(82250)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjP2VorAbzFamGr(), is(BigDecimal.valueOf(82250)));
	}

	private static void setSpezialfallGeteilteObhut(Gesuch gesuch) {
		var familiensituation = gesuch.extractFamiliensituation();
		assert familiensituation != null;
		familiensituation.setFamilienstatus(EnumFamilienstatus.APPENZELL);
		familiensituation.setGeteilteObhut(true);
		familiensituation.setGemeinsamerHaushaltMitObhutsberechtigterPerson(false);
		familiensituation.setGemeinsamerHaushaltMitPartner(true);
	}

	private static void setSpezialFallNichtGeteilteObhut(Gesuch gesuch) {
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		assert familiensituation != null;
		familiensituation.setFamilienstatus(EnumFamilienstatus.APPENZELL);
		familiensituation.setGeteilteObhut(false);
		familiensituation.setGemeinsamerHaushaltMitPartner(true);
	}

	private void setARDatenPartnerHalfed(Gesuch gesuch) {
		assert gesuch.getGesuchsteller1() != null;
		assert gesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null;
		assert gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getFinSitZusatzangabenAppenzell() != null;
		gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getFinSitZusatzangabenAppenzell()
				.setZusatzangabenPartner(createFinanzielleVerhaeltnisseHalfed());
	}

	private void setEkvARDatenGS(GesuchstellerContainer gesuchstellerContainer) {
		assert gesuchstellerContainer.getEinkommensverschlechterungContainer() != null;
		gesuchstellerContainer.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus1()
				.setFinSitZusatzangabenAppenzell(createFinanzielleVerhaeltnisseHalfed());
		gesuchstellerContainer.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus2()
				.setFinSitZusatzangabenAppenzell(createFinanzielleVerhaeltnisseHalfed());
	}

	private void setEkvARDatenPartner(GesuchstellerContainer gesuch) {
		assert gesuch.getEinkommensverschlechterungContainer() != null;
		assert gesuch.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus1()
				.getFinSitZusatzangabenAppenzell() != null;
		assert gesuch.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus2()
				.getFinSitZusatzangabenAppenzell() != null;

		gesuch.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus1()
				.getFinSitZusatzangabenAppenzell()
				.setZusatzangabenPartner(createFinanzielleVerhaeltnisseHalfed());
		gesuch.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus2()
				.getFinSitZusatzangabenAppenzell()
				.setZusatzangabenPartner(createFinanzielleVerhaeltnisseHalfed());
	}

	private Gesuch prepareGesuch(boolean secondGesuchsteller) {
		Gesuch gesuch = new Gesuch();
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		assert gesuch.getFamiliensituationContainer() != null;
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(new Familiensituation());
		gesuch.setGesuchsteller1(createGesuchstellerMitFinSit());
		if(secondGesuchsteller) {
			gesuch.setGesuchsteller2(createGesuchstellerMitFinSit());
		}
		return gesuch;
	}

	private GesuchstellerContainer createGesuchstellerMitFinSit() {
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituationForTest = new FinanzielleSituation();
		finanzielleSituationForTest.setFinSitZusatzangabenAppenzell(createFinanzielleVerhaeltnisse());
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationForTest);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}

	private FinSitZusatzangabenAppenzell createFinanzielleVerhaeltnisse() {
		FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell = new FinSitZusatzangabenAppenzell();
		finSitZusatzangabenAppenzell.setSteuerbaresEinkommen(BigDecimal.valueOf(60000));
		finSitZusatzangabenAppenzell.setSteuerbaresVermoegen(BigDecimal.valueOf(15000));
		finSitZusatzangabenAppenzell.setSaeule3a(BigDecimal.valueOf(1000));
		finSitZusatzangabenAppenzell.setSaeule3aNichtBvg(BigDecimal.valueOf(1000));
		finSitZusatzangabenAppenzell.setBeruflicheVorsorge(BigDecimal.valueOf(2000));
		finSitZusatzangabenAppenzell.setLiegenschaftsaufwand(BigDecimal.valueOf(2000));
		finSitZusatzangabenAppenzell.setEinkuenfteBgsa(BigDecimal.valueOf(3000));
		finSitZusatzangabenAppenzell.setVorjahresverluste(BigDecimal.valueOf(3000));
		finSitZusatzangabenAppenzell.setPolitischeParteiSpende(BigDecimal.valueOf(4000));
		finSitZusatzangabenAppenzell.setLeistungAnJuristischePersonen(BigDecimal.valueOf(4000));
		return finSitZusatzangabenAppenzell;
	}

	private FinSitZusatzangabenAppenzell createFinanzielleVerhaeltnisseHalfed() {
		FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell = new FinSitZusatzangabenAppenzell();
		finSitZusatzangabenAppenzell.setSteuerbaresEinkommen(BigDecimal.valueOf(30000));
		finSitZusatzangabenAppenzell.setSteuerbaresVermoegen(BigDecimal.valueOf(7500));
		finSitZusatzangabenAppenzell.setSaeule3a(BigDecimal.valueOf(500));
		finSitZusatzangabenAppenzell.setSaeule3aNichtBvg(BigDecimal.valueOf(500));
		finSitZusatzangabenAppenzell.setBeruflicheVorsorge(BigDecimal.valueOf(1000));
		finSitZusatzangabenAppenzell.setLiegenschaftsaufwand(BigDecimal.valueOf(1000));
		finSitZusatzangabenAppenzell.setEinkuenfteBgsa(BigDecimal.valueOf(1500));
		finSitZusatzangabenAppenzell.setVorjahresverluste(BigDecimal.valueOf(1500));
		finSitZusatzangabenAppenzell.setPolitischeParteiSpende(BigDecimal.valueOf(2000));
		finSitZusatzangabenAppenzell.setLeistungAnJuristischePersonen(BigDecimal.valueOf(2000));
		return finSitZusatzangabenAppenzell;
	}
}
