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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FinanzielleSituationLuzernRechnerTest {

	private FinanzielleSituationLuzernRechner finSitRechner = new FinanzielleSituationLuzernRechner();

	/**
	 * Veranlagte Berechnung, entweder alleine oder zur zweit
	 *
	 * Steuerbares Einkommen
	 * Steuerbares Vermögen	10% von diesem addieren
	 * Abzüge für den effektiven Liegenschaftsunterhalt...	Subtrahieren
	 * Verrechenbare Geschäftsverluste aus den Vorjahren..	Subtrahieren
	 * Einkäufe in die berufliche Vorsorge Subtrahieren
	 *
	 * Einkommensverschlechterung sind noch nicht implementiert
	 */
	@Nested
	class VeranlagtTest {

		/**
		 * Steuerbares Einkommen								60'000
		 * Steuerbares Vermögen	10'000, 10% =			       + 1'000
		 * Abzüge für den effektiven Liegenschaftsunterhalt... - 1'000
		 * Verrechenbare Geschäftsverluste aus den Vorjahren.. - 1'000
		 * Einkäufe in die berufliche Vorsorge Subtrahieren    - 1'000
		 *                                                     -------
		 *                                                      58'000
		 *                                                     */
		@Test
		public void testAlleWertVorhanden() {
			Gesuch gesuch = prepareGesuch(false);
			finSitRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(58000)));

			//zwei Antragstellende, beides ueberpruefen
			gesuch = prepareGesuch(true);
			finSitRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(58000)));
			assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(116000)));
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
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(emptyFinanzielleSituationForTest);
			finSitRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(0)));
		}
	}


	private Gesuch prepareGesuch(boolean secondGesuchsteller) {
		Gesuch gesuch = new Gesuch();
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
		finanzielleSituationForTest.setSteuerbaresEinkommen(BigDecimal.valueOf(60000));
		finanzielleSituationForTest.setSteuerbaresVermoegen(BigDecimal.valueOf(10000));
		finanzielleSituationForTest.setGeschaeftsverlust(BigDecimal.valueOf(1000));
		finanzielleSituationForTest.setAbzuegeLiegenschaft(BigDecimal.valueOf(1000));
		finanzielleSituationForTest.setEinkaeufeVorsorge(BigDecimal.valueOf(1000));
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationForTest);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}
}
