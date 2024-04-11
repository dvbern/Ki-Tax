/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FinanzielleSituationSchwyzRechnerTest {

	private FinanzielleSituationSchwyzRechner finanzielleSituationSchwyzRechner = new FinanzielleSituationSchwyzRechner();


	/**
	 * Steuerbares Einkommen								60'000
	 *
	 * Steuerbares Vermögen	10'000 - 200'000, 10% =		   + 0
	 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
	 * Einkäufe in die berufliche Vorsorge Subtrahieren    + 1'000
	 *                                                     -------
	 *                                                      62'000
	 *                                                     */
	@Test
	public void calculateForNichtQuellenBesteuerteTest() {
		Gesuch gesuch = prepareGesuch(false, false);
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));

		//zwei Antragstellende, beides ueberpruefen
		gesuch = prepareGesuch(true, false);
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(124000)));
	}

	/**
	 * Steuerbares Einkommen								60'000
	 *
	 * Steuerbares Vermögen	200'000 - 200'000, 10% =		   + 0
	 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
	 * Einkäufe in die berufliche Vorsorge Subtrahieren    + 1'000
	 *                                                     -------
	 *                                                      62'000
	 *                                                     */
	@Test
	public void calculateForNichtQuellenBesteuerteMitSteuerbaresVermoegenAnGrenzeTest() {
		Gesuch gesuch = prepareGesuch(false, false);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerbaresVermoegen(new BigDecimal(200000));
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
	}

	/**
	 * Steuerbares Einkommen								60'000
	 *
	 * Steuerbares Vermögen	250'000 - 200'000, 10% =	   + 5'000
	 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
	 * Einkäufe in die berufliche Vorsorge Subtrahieren    + 1'000
	 *                                                     -------
	 *                                                      67'000
	 *                                                     */
	@Test
	public void calculateForNichtQuellenBesteuerteMitSteuerbaresVermoegenTest() {
		Gesuch gesuch = prepareGesuch(false, false);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerbaresVermoegen(new BigDecimal(250000));
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(67000)));
	}

	/**
	 * Brutto Einkommen									    60'000
	 *
	 * Brutto Einkommen 20% =		   					  - 12'000
	 *                                                     -------
	 *                                                      48'000
	 *                                                     */
	@Test
	public void calculateForQuellenBesteuerteTest() {
		Gesuch gesuch = prepareGesuch(false, true);
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(48000)));

		//zwei Antragstellende, beides ueberpruefen
		gesuch = prepareGesuch(true, true);
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(48000)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(96000)));
	}

	@Test
	public void quellenBesteuertAllesNullTest(){
		Gesuch gesuch = prepareGesuch(false, true);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setBruttoLohn(null);
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(0)));
	}

	@Test
	public void nichtQuellenBesteuertAllesNullTest(){
		Gesuch gesuch = prepareGesuch(false, false);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerbaresEinkommen(null);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setEinkaeufeVorsorge(null);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setAbzuegeLiegenschaft(null);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerbaresVermoegen(null);
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(0)));
	}

	@Test
	public void quellenBesteuertZweiteGSNullTest() {
		Gesuch gesuch = prepareGesuch(true, true);
		gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(null);
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(48000)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(48000)));
	}

	@Test
	public void nichtQuellenBesteuertZweiteGSNullTest() {
		Gesuch gesuch = prepareGesuch(true, false);
		gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(null);
		finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
		assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
		assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
	}

	private Gesuch prepareGesuch(boolean secondGesuchsteller, boolean quellenbesteuert) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsteller1(quellenbesteuert ?
			createGesuchstellerMitFinSitQuellenbesteuert(): createGesuchstellerMitFinSit());
		if(secondGesuchsteller) {
			gesuch.setGesuchsteller2(quellenbesteuert ?
				createGesuchstellerMitFinSitQuellenbesteuert(): createGesuchstellerMitFinSit());
		}
		return gesuch;
	}

	private GesuchstellerContainer createGesuchstellerMitFinSit() {
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituationForTest = new FinanzielleSituation();
		finanzielleSituationForTest.setQuellenbesteuert(false);
		finanzielleSituationForTest.setSteuerbaresEinkommen(BigDecimal.valueOf(60000));
		finanzielleSituationForTest.setEinkaeufeVorsorge(BigDecimal.valueOf(1000));
		finanzielleSituationForTest.setAbzuegeLiegenschaft(BigDecimal.valueOf(1000));
		finanzielleSituationForTest.setSteuerbaresVermoegen(BigDecimal.valueOf(10000));
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationForTest);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}

	private GesuchstellerContainer createGesuchstellerMitFinSitQuellenbesteuert() {
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituationForTest = new FinanzielleSituation();
		finanzielleSituationForTest.setQuellenbesteuert(true);
		finanzielleSituationForTest.setBruttoLohn(BigDecimal.valueOf(60000));
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationForTest);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}

}
