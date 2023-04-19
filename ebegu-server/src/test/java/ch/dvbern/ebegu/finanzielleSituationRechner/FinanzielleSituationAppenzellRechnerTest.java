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

import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
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
		finanzielleSituationForTest.setFinSitZusatzangabenAppenzell(createFinanzielleVerhaeltnisse());
		finanzielleSituationForTest.setSteuerbaresEinkommen(BigDecimal.valueOf(60000));
		finanzielleSituationForTest.setSteuerbaresVermoegen(BigDecimal.valueOf(15000));
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationForTest);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}

	private FinSitZusatzangabenAppenzell createFinanzielleVerhaeltnisse() {
		FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell = new FinSitZusatzangabenAppenzell();
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
}
