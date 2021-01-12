/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.fristen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculateInklAllgemeineRegeln;

/**
 * Folgende Tatbestände beeinflussen den Anspruch
 * - Erwerbspensum
 * - Unbezahlter Urlaub
 * - Fachstelle
 * - Ausserordentlicher Anspruch
 * - Verspätetes Einreichen
 * - Zuzug/Wegzug
 * - Abwesenheit
 *
 * Grundeinstellungen für die Tests:
 * - Betreuung Kita 75%
 * - Einkommen 50'000
 * - Erwerbspensum 60%
 */
@SuppressWarnings({ "Duplicates", "UnusedAssignment" })
public class AnspruchFristenTest extends AbstractBGRechnerTest {

	private static final LocalDate EINREICHUNG_RECHTZEITIG = TestDataUtil.START_PERIODE.minusMonths(3);
	private static final LocalDate EINREICHUNG_ZU_SPAET = TestDataUtil.ENDE_PERIODE.minusMonths(3);

	/**
	 * Erstgesuch
	 * Pensumserhöhung per: 16.11.2017
	 * Erstgesuch eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Anspruch per Ereignisdatum
	 */
	@Test
	public void erstgesuchErwerbspensumErhoehtRechzeitig() {
		Betreuung betreuung = createErstgesuch(EINREICHUNG_RECHTZEITIG);
		Gesuch gesuch = betreuung.extractGesuch();
		changeErwerbspensum(gesuch, LocalDate.of(2017,  Month.NOVEMBER, 16), 80);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(13, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 75, 80, 75);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 16), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 75, 100, 75);
	}

	/**
	 * Erstgesuch
	 * Pensumserhöhung per: 16.11.2017
	 * Erstgesuch eingereicht: 30.04.2018 (zu spät)
	 * => Kein Anspruch bis Folgemonat der Einreichung, Anpassung Anspruch per 01.05.2018 (Folgemonat der Einreichung)
	 */
	@Test
	public void erstgesuchErwerbspensumErhoehtZuSpaet() {
		Betreuung betreuung = createErstgesuch(EINREICHUNG_ZU_SPAET);
		Gesuch gesuch = betreuung.extractGesuch();
		changeErwerbspensum(gesuch, LocalDate.of(2017,  Month.NOVEMBER, 16), 80);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 75, 0, 0);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 75, 100, 75);
	}

	/**
	 * Erstgesuch
	 * Pensumsreduktion per: 16.11.2017
	 * Erstgesuch eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Anspruch per 01.12. (Folgemonat der Reduktion)
	 */
	@Test
	public void erstgesuchErwerbspensumReduziertRechtzeitig() {
		Betreuung betreuung = createErstgesuch(EINREICHUNG_RECHTZEITIG);
		Gesuch gesuch = betreuung.extractGesuch();
		changeErwerbspensum(gesuch, LocalDate.of(2017,  Month.NOVEMBER, 16), 40);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 75, 80, 75);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 75, 60, 60);
	}

	/**
	 * Erstgesuch
	 * Pensumsreduktion per: 16.11.2017
	 * Erstgesuch eingereicht: 30.04.2018 (zu spät)
	 * => Kein Anspruch bis Folgemonat der Einreichung, Anpassung Anspruch per 01.05.2018 (Folgemonat der Einreichung)
	 */
	@Test
	public void erstgesuchErwerbspensumReduziertZuSpaet() {
		Betreuung betreuung = createErstgesuch(EINREICHUNG_ZU_SPAET);
		Gesuch gesuch = betreuung.extractGesuch();
		changeErwerbspensum(gesuch, LocalDate.of(2017,  Month.NOVEMBER, 16), 40);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 75, 0, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 75, 0, 0);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 75, 60, 60);
	}

	/**
	 * Mutation
	 * Pensumserhöhung per: 16.11.2017
	 * Mutation eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Anspruch Stichtag 16.11.2017
	 */
	@Test
	public void mutationErwerbspensumErhoehtRechzeitig() {
		Betreuung betreuung = createMutation(EINREICHUNG_RECHTZEITIG);
		Gesuch mutation = betreuung.extractGesuch();
		changeErwerbspensum(mutation, LocalDate.of(2017,  Month.NOVEMBER, 16), 80);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(13, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 75, 80, 75);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 16), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 75, 100, 75);
	}

	/**
	 * Mutation
	 * Pensumserhöhung per: 16.11.2017
	 * Mutation eingereicht: 30.04.2018 (zu spät)
	 * => Anpassung Anspruch per 01.05.2018 (Folgemonat der Einreichung)
	 */
	@Test
	public void mutationErwerbspensumErhoehtZuSpaet() {
		Betreuung betreuung = createMutation(EINREICHUNG_ZU_SPAET);
		Gesuch mutation = betreuung.extractGesuch();
		changeErwerbspensum(mutation, LocalDate.of(2017,  Month.NOVEMBER, 16), 80);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(13, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 16), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 75, 80, 75);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 75, 100, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 75, 100, 75);
	}

	/**
	 * Mutation
	 * Pensumsreduktion per: 16.11.2017
	 * Mutation eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Anspruch per 01.12. (Folgemonat der Reduktion)
	 */
	@Test
	public void mutationErwerbspensumReduziertRechtzeitig() {
		Betreuung betreuung = createMutation(EINREICHUNG_RECHTZEITIG);
		Gesuch mutation = betreuung.extractGesuch();
		changeErwerbspensum(mutation, LocalDate.of(2017,  Month.NOVEMBER, 16), 40);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 75, 80, 75);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 75, 60, 60);
	}

	/**
	 * Mutation
	 * Pensumsreduktion per: 16.11.2017
	 * Mutation eingereicht: 30.04.2018 (zu spät)
	 * => Anpassung Anspruch per 01.12.2017 (Folgemonat der Reduktion)
	 */
	@Test
	public void mutationErwerbspensumReduziertZuSpaet() {
		Betreuung betreuung = createMutation(EINREICHUNG_ZU_SPAET);
		Gesuch mutation = betreuung.extractGesuch();
		changeErwerbspensum(mutation, LocalDate.of(2017,  Month.NOVEMBER, 16), 40);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 75, 80, 75);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 75, 80, 75);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 75, 60, 60);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 75, 60, 60);
	}

	private Betreuung createErstgesuch(@Nonnull LocalDate eingangsdatum) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 75,  BigDecimal.valueOf(2000));
		betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		Gesuch gesuch = betreuung.extractGesuch();
		GesuchstellerContainer gs1 = gesuch.getGesuchsteller1();
		Assert.assertNotNull(gs1);
		Assert.assertNotNull(gs1.getFinanzielleSituationContainer());
		gs1.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		gs1.getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(50000L));
		gesuch.setRegelnGueltigAb(eingangsdatum);
		return betreuung;
	}

	private Betreuung createMutation(@Nonnull LocalDate eingangsdatum) {
		Gesuch erstgesuch = createErstgesuch(EINREICHUNG_RECHTZEITIG).extractGesuch();
		List<VerfuegungZeitabschnitt> calculate = calculateInklAllgemeineRegeln(erstgesuch.extractAllBetreuungen().get(0));
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(calculate);
		Gesuch mutation = erstgesuch.copyForMutation(new Gesuch(), Eingangsart.ONLINE, eingangsdatum);
		mutation.extractAllBetreuungen().get(0).initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutation.setRegelnGueltigAb(eingangsdatum);
		return mutation.extractAllBetreuungen().get(0);
	}

	private void changeErwerbspensum(@Nonnull Gesuch gesuch, @Nonnull LocalDate stichtag, int neuesPensum) {
		// Das alte beenden
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		ErwerbspensumContainer altesEWP = gesuch.getGesuchsteller1().getErwerbspensenContainers().iterator().next();
		Assert.assertNotNull(altesEWP.getErwerbspensumJA());
		altesEWP.getErwerbspensumJA().getGueltigkeit().setGueltigBis(stichtag.minusDays(1));
		// Une ein neues mit dem neuen Pensum erstellen
		ErwerbspensumContainer neuesEWP = new ErwerbspensumContainer();
		neuesEWP.setErwerbspensumJA(new Erwerbspensum());
		Assert.assertNotNull(neuesEWP.getErwerbspensumJA());
		neuesEWP.getErwerbspensumJA().setPensum(neuesPensum);
		neuesEWP.getErwerbspensumJA().setTaetigkeit(Taetigkeit.ANGESTELLT);
		neuesEWP.getErwerbspensumJA().getGueltigkeit().setGueltigAb(stichtag);
		gesuch.getGesuchsteller1().getErwerbspensenContainers().add(neuesEWP);
	}
}

