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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FinanzielleSituationSchwyzRechnerTest {

	private final FinanzielleSituationSchwyzRechner finanzielleSituationSchwyzRechner = new FinanzielleSituationSchwyzRechner();

	@Nested
	class SingleGSTest {
		/**
		 * Steuerbares Einkommen								60'000
		 * <p>
		 * Steuerbares Vermögen	10'000 - 200'000, 10% =		   + 0
		 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
		 * Einkäufe in die berufliche Vorsorge Subtrahieren    + 1'000
		 * -------
		 * 62'000
		 */
		@Test
		void calculateForNichtQuellenBesteuerteTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(10000),
				new BigDecimal(1000),
				new BigDecimal(1000));
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
		}

		/**
		 * Steuerbares Einkommen								60'000
		 * <p>
		 * Steuerbares Vermögen	200'000 - 200'000, 10% =		   + 0
		 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
		 * Einkäufe in die berufliche Vorsorge Subtrahieren    + 1'000
		 * -------
		 * 62'000
		 */
		@Test
		void calculateForNichtQuellenBesteuerteMitSteuerbaresVermoegenAnGrenzeTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(200000),
				new BigDecimal(1000),
				new BigDecimal(1000));
			gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setSteuerbaresVermoegen(new BigDecimal(200000));
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
		}

		/**
		 * Steuerbares Einkommen								60'000
		 * <p>
		 * Steuerbares Vermögen	250'000 - 200'000, 10% =	   + 5'000
		 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
		 * Einkäufe in die berufliche Vorsorge Subtrahieren    + 1'000
		 * -------
		 * 67'000
		 */
		@Test
		void calculateForNichtQuellenBesteuerteMitSteuerbaresVermoegenTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(250000),
				new BigDecimal(1000),
				new BigDecimal(1000));
			gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setSteuerbaresVermoegen(new BigDecimal(250000));
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(67000)));
		}

		/**
		 * Brutto Einkommen									    60'000
		 * <p>
		 * Brutto Einkommen 20% =		   					  - 12'000
		 * -------
		 * 48'000
		 */
		@Test
		void calculateForQuellenBesteuerteTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000));
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(48000)));
		}

		@Test
		void quellenBesteuertAllesNullTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				null);
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(0)));
		}

		@Test
		void nichtQuellenBesteuertAllesNullTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				null,
				null,
				null,
				null);
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(0)));
		}

		private Gesuch prepareGesuch() {
			Gesuch gesuch = new Gesuch();
			gesuch.setGesuchsteller1(createGesuchstellerMitLeerenFinSit());
			return gesuch;
		}
	}

	@Nested
	class TwoGSTest {
		/**
		 * Steuerbares Einkommen								60'000
		 * <p>
		 * Steuerbares Vermögen	10'000 - 200'000, 10% =		   + 0
		 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
		 * Einkäufe in die berufliche Vorsorge Subtrahieren    + 1'000
		 * -------
		 * 62'000
		 * <p>
		 * GS2 gleich => 62'000 x 2 = 124'000
		 */
		@Test
		void calculateForNichtQuellenBesteuerteTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(200000),
				new BigDecimal(1000),
				new BigDecimal(1000));
			setFinSitValueForNichtQuellenbesteuert(gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(200000),
				new BigDecimal(1000),
				new BigDecimal(1000));
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
			assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(124000)));
		}


		/**
		 * Brutto Einkommen									    60'000
		 * <p>
		 * Brutto Einkommen 20% =		   					  - 12'000
		 * -------
		 * 48'000
		 * <p>
		 * GS 2 gleich => 48'000 x 2 = 96'000
		 *
		 */
		@Test
		void calculateForQuellenBesteuerteTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000));
			setFinSitValueForQuellenbesteuert(gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000));
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(48000)));
			assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(96000)));
		}

		@Test
		void quellenBesteuertZweiteGSNullTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000));
			gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(null);
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(48000)));
			assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(48000)));
		}

		@Test
		void nichtQuellenBesteuertZweiteGSNullTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(200000),
				new BigDecimal(1000),
				new BigDecimal(1000));
			gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(null);
			finanzielleSituationSchwyzRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
			assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(62000)));
		}

		private Gesuch prepareGesuch() {
			Gesuch gesuch = new Gesuch();
			gesuch.setGesuchsteller1(createGesuchstellerMitLeerenFinSit());
			gesuch.setGesuchsteller2(createGesuchstellerMitLeerenFinSit());
			return gesuch;
		}
	}

	private GesuchstellerContainer createGesuchstellerMitLeerenFinSit() {
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituationForTest = new FinanzielleSituation();
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationForTest);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}

	private void setFinSitValueForNichtQuellenbesteuert (@Nonnull FinanzielleSituation finanzielleSituationForTest,
		@Nonnull BigDecimal steuerbaresEinkommen,
		@Nonnull BigDecimal steuerbaresVermoegen,
		@Nonnull BigDecimal einkaeufeVorsorge,
		@Nonnull BigDecimal abzuegeLiegenschaft
	) {
		finanzielleSituationForTest.setQuellenbesteuert(false);
		finanzielleSituationForTest.setSteuerbaresEinkommen(steuerbaresEinkommen);
		finanzielleSituationForTest.setEinkaeufeVorsorge(einkaeufeVorsorge);
		finanzielleSituationForTest.setAbzuegeLiegenschaft(abzuegeLiegenschaft);
		finanzielleSituationForTest.setSteuerbaresVermoegen(steuerbaresVermoegen);
	}

	private void setFinSitValueForQuellenbesteuert (@Nonnull FinanzielleSituation finanzielleSituationForTest,
		@Nonnull BigDecimal bruttolohn) {
		finanzielleSituationForTest.setQuellenbesteuert(true);
		finanzielleSituationForTest.setBruttoLohn(bruttolohn);
	}
}
