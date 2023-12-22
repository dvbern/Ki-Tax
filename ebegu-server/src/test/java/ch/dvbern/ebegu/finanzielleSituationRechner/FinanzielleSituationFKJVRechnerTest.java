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

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FinanzielleSituationFKJVRechnerTest extends FinanzielleSituationBernRechnerTest {

	private static final BigDecimal NETTLOHON = new BigDecimal("60000");
	private static final BigDecimal STEUERBARES_VERMOEGEN = new BigDecimal("10000");

	@Before
	public void setUp() {
		finSitRechner = new FinanzielleSituationFKJVRechner();
	}

	/**
	 * Veranlagte Berechnung, entweder alleine oder zur zweit
	 *
	 * Nettlolohn
	 * das steuerpflichtige Ersatzeinkommen addieren
	 * die erhaltenen Unterhaltsbeiträge addieren
	 * fünf % des Nettovermögens addieren
	 * den ausgewiesenen Geschäftsgewinn addieren
	 * Bruttoerträge aus beweglichem und unbeweglichem Vermögen addieren
	 * Weitere steuerbare Einkünfte addieren
	 *
	 * Bezahlte Unterhaltsbeiträge subtrahieren
	 * Schuldzinsen subtrahieren
	 * Gewinnungskosten subtrahieren
	 *
	 */

	/**
	 * Steuerbares Einkommen								60'000
	 * Ersatzeinkomen									   +     0
	 * Erhaltene Unterhaltsbeiträge						   +	 0
	 * Steuerbares Vermögen	10'000, 5% =			       +   500
	 * Geschäftsgewinn									   +	 0
	 * Bruttoerträge aus Vermögen						   +	50
	 * Weitere steuerliche Einkünfte					   +	 0
	 * Einkommen im vereinfachten Verfahren abgerechnet    +    50
	 *
	 * Bezahlte Unterhaltsbeiträge						   -	 0
	 * Schuldzinsen subtrahieren						   - 	10
	 * Gewinnungskosten subtrahieren					   -	85
	 *
	 * -------
	 * 60'505
	 */
	@Test
	public void testAlleWertVorhanden() {
		Gesuch gesuch = prepareGesuch(false);
		finSitRechner.calculateFinanzDaten(gesuch, null);
		assertThat(
				gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(),
				is(BigDecimal.valueOf(60505)));

		//zwei Antragstellende, beides ueberpruefen
		gesuch = prepareGesuch(true);
		finSitRechner.calculateFinanzDaten(gesuch, null);
		assertThat(
				gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(),
				is(BigDecimal.valueOf(60505)));
		assertThat(
				gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(),
				is(BigDecimal.valueOf(60505 * 2)));
	}

	/**
	 * Steuerbares Einkommen								60'000
	 * Erhaltene Unterhaltsbeiträge						   + 1'135
	 * Steuerbares Vermögen	10'000, 5% =			       +   500
	 * Bruttoerträge aus Vermögen						   +	50
	 * Einkommen im vereinfachten Verfahren abgerechnet    +    50
	 *
	 *      Geschäftsgewinn BJ 					+ 5'000
	 * 	    Geschäftsgewinn BJ-1				+ 3'500
	 * 	    Geschäftsgewinn BJ-2				+ 1'840
	 * 	    Ersatzeinkommen BJ					+ 1'600
	 * 	    Ersatzeinkommen BJ-1				+ 3'208
	 * 	    Ersatzeinkommen BJ-2				+ 1'847
	 * 	    Total 								16'995
	 * 	Durchschnittlicher Geschäftsgwinn (16'995/3)       + 5'665
	 *
	 *		Ersatzeinkomen						10'000
	 *		Ersatzeinkommen BJ			       - 1'600
	 * 	    Ersatzeinkommen BJ-1		       - 3'208
	 * 	    Ersatzeinkommen BJ-2		       - 1'847
	 * 	Zu Berücksichtigendes Ersatzeinkommen  				+3'345
	 * Schuldzinsen subtrahieren						   - 	10
	 * Gewinnungskosten subtrahieren					   -	85
	 *
	 *
	 * -------
	 * 70650
	 */
	@Test
	public void calculateFinanzdatenWithErsatzeinkommenSelbsstaendig() {
		Gesuch gesuch = new Gesuch();
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finSit = new FinanzielleSituation();

		finSit.setNettolohn(NETTLOHON);
		finSit.setBruttovermoegen(STEUERBARES_VERMOEGEN);
		finSit.setSchulden(BigDecimal.ZERO);

		finSit.setFamilienzulage(BigDecimal.ZERO);
		finSit.setErsatzeinkommen(BigDecimal.valueOf(10000));
		finSit.setErhalteneAlimente(BigDecimal.valueOf(1135));

		finSit.setFamilienzulage(BigDecimal.ZERO);
		finSit.setBruttoertraegeVermoegen(BigDecimal.valueOf(50));
		finSit.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(5000));
		finSit.setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(3500));
		finSit.setGeschaeftsgewinnBasisjahrMinus2(BigDecimal.valueOf(1840));
		finSit.setErsatzeinkommenBasisjahr(BigDecimal.valueOf(1600));
		finSit.setErsatzeinkommenBasisjahrMinus1(BigDecimal.valueOf(3208));
		finSit.setErsatzeinkommenBasisjahrMinus2(BigDecimal.valueOf(1847));

		finSit.setAbzugSchuldzinsen(BigDecimal.valueOf(10));
		finSit.setGewinnungskosten(BigDecimal.valueOf(85));
		finSit.setEinkommenInVereinfachtemVerfahrenAbgerechnet(false);

		finSit.setEinkommenInVereinfachtemVerfahrenAbgerechnet(true);
		finSit.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(BigDecimal.valueOf(50));

		finanzielleSituationContainer.setFinanzielleSituationJA(finSit);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		gesuch.setGesuchsteller1(gesuchstellerContainer);

		finSitRechner.calculateFinanzDaten(gesuch, null);
		assertThat(
			gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(),
			is(BigDecimal.valueOf(70650)));
	}

	private Gesuch prepareGesuch(boolean secondGesuchsteller) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsteller1(createGesuchstellerMitFinSit());
		if (secondGesuchsteller) {
			gesuch.setGesuchsteller2(createGesuchstellerMitFinSit());
		}
		return gesuch;
	}

	private GesuchstellerContainer createGesuchstellerMitFinSit() {
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituationForTest = new FinanzielleSituation();
		finanzielleSituationForTest.setNettolohn(NETTLOHON);
		finanzielleSituationForTest.setBruttovermoegen(STEUERBARES_VERMOEGEN);
		finanzielleSituationForTest.setSchulden(BigDecimal.ZERO);

		finanzielleSituationForTest.setFamilienzulage(BigDecimal.ZERO);
		finanzielleSituationForTest.setErsatzeinkommen(BigDecimal.ZERO);
		finanzielleSituationForTest.setErhalteneAlimente(BigDecimal.ZERO);

		finanzielleSituationForTest.setFamilienzulage(BigDecimal.ZERO);
		finanzielleSituationForTest.setDurchschnittlicherGeschaeftsgewinn(BigDecimal.ZERO);
		finanzielleSituationForTest.setBruttoertraegeVermoegen(new BigDecimal(50));

		finanzielleSituationForTest.setAbzugSchuldzinsen(new BigDecimal(10));
		finanzielleSituationForTest.setGewinnungskosten(new BigDecimal(85));
		finanzielleSituationForTest.setEinkommenInVereinfachtemVerfahrenAbgerechnet(false);

		finanzielleSituationForTest.setEinkommenInVereinfachtemVerfahrenAbgerechnet(true);
		finanzielleSituationForTest.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(new BigDecimal(50));

		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationForTest);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}
}
