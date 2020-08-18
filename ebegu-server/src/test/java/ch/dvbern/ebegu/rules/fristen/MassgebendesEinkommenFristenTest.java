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
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculateInklAllgemeineRegeln;

/**
 * Folgende Tatbestände beeinflussen das MassgebendeEinkommen oder die Familiengrösse
 * - Heirat / Scheidung (nur Mutation möglich)
 * - Geburt eines Kindes (Erstgesuch oder Mutation)
 * - Einkommensverschlechterung (Erstgesuch oder Mutation)
 * - Anpassung der Finanziellen Situation (nur Mutation möglich)
 */
@SuppressWarnings({ "Duplicates", "UnusedAssignment" })
public class MassgebendesEinkommenFristenTest {

	private static final LocalDate EINREICHUNG_RECHTZEITIG = TestDataUtil.START_PERIODE.minusMonths(3);
	private static final LocalDate EINREICHUNG_ZU_SPAET = TestDataUtil.START_PERIODE.plusMonths(1);
	private static final FinanzielleSituationRechner RECHNER = new FinanzielleSituationRechner();

	/**
	 * Zusätzliches Kind: Massgebendes Einkommen sinkt wegen höherem Familienabzug
	 * Geburt Kind: 16.11.2017
	 * Erstgesuch eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Familiengrösse und Massgebendes Einkommen per 01.12.2017 (Folgemonat Geburt)
	 * (Fall ist eigentlich gar nicht möglich, da kein Kind voraus erfasst werden kann. Ausser das Einreichungsdatum
	 * wird manuell überschrieben)
	 */
	@Test
	public void erstgesuchGeburtNeuesKindRechtzeitig() {
		Betreuung betreuung = createErstgesuch(EINREICHUNG_RECHTZEITIG, false, 50000);
		Gesuch gesuch = betreuung.extractGesuch();

		addKind(gesuch, LocalDate.of(2017, Month.NOVEMBER, 16));
		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 50000, 50000);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 3, 50000, 38600);
	}

	/**
	 * Zusätzliches Kind: Massgebendes Einkommen sinkt wegen höherem Familienabzug
	 * Geburt Kind: 16.11.2017
	 * Mutation eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Familiengrösse und Massgebendes Einkommen per 01.12.2017 (Folgemonat Geburt)
	 */
	@Test
	public void mutationGeburtNeuesKindRechtzeitig() {
		Betreuung mutationBetreuung = createMutationAlleine(EINREICHUNG_RECHTZEITIG);
		addKind(mutationBetreuung.extractGesuch(), LocalDate.of(2017, Month.NOVEMBER, 16));

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 50000, 50000);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 3, 50000, 38600);
	}

	/**
	 * Zusätzliches Kind: Massgebendes Einkommen sinkt wegen höherem Familienabzug
	 * Geburt Kind: 16.01.2017
	 * Mutation eingereicht: 30.09.2017 (zu spät)
	 * => Anpassung Familiengrösse und Massgebendes Einkommen per 01.05.2018 (Folgemonat Einreichung)
	 */
	@Test
	public void mutationGeburtNeuesKindZuSpaet() {
		Betreuung mutationBetreuung = createMutationAlleine(EINREICHUNG_ZU_SPAET);
		addKind(mutationBetreuung.extractGesuch(), LocalDate.of(2017, Month.JANUARY, 16));

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 3, 50000, 38600);
	}

	/**
	 * Heirat, Partner hat 20000 Einkommen: Massgebendes Einkommen steigt trotz höherem Abzug
	 * Heirat: 16.11.2017
	 * Mutation eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Familiengrösse und Massgebendes Einkommen per 01.12.2017 (Folgemonat Heirat)
	 */
	@Test
	public void mutationHeiratMitZweiteinkommenRechtzeitig() {
		Betreuung mutationBetreuung =
			createMutationHeirat(EINREICHUNG_RECHTZEITIG, LocalDate.of(2017, Month.NOVEMBER, 16), 20000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 50000, 50000);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 3, 70000, 58600);
	}

	/**
	 * Heirat, Partner hat kein Einkommen: Massgebendes Einkommen sinkt wegen höherem Familienabzug
	 * Heirat: 16.11.2017
	 * Mutation eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Familiengrösse und Massgebendes Einkommen per 01.12.2017 (Folgemonat Heirat)
	 */
	@Test
	public void mutationHeiratOhneZweiteinkommenRechtzeitig() {
		Betreuung mutationBetreuung =
			createMutationHeirat(EINREICHUNG_RECHTZEITIG, LocalDate.of(2017, Month.NOVEMBER, 16), 0);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 50000, 50000);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 3, 50000, 38600);
	}

	/**
	 * Heirat, Partner hat 20000 Einkommen: Massgebendes Einkommen steigt trotz höherem Abzug
	 * Heirat: 16.11.2017
	 * Mutation eingereicht: 30.04.2018 (zu spät)
	 * => Anpassung Familiengrösse und Massgebendes Einkommen per 01.12.2017 (Folgemonat Heirat, unabhängig von
	 * Einreichedatum!)
	 */
	@Test
	public void mutationHeiratMitZweiteinkommenZuSpaet() {
		Betreuung mutationBetreuung =
			createMutationHeirat(EINREICHUNG_ZU_SPAET, LocalDate.of(2017, Month.NOVEMBER, 16), 20000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 50000, 50000);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 3, 70000, 58600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 3, 70000, 58600);
	}

	/**
	 * Heirat, Partner hat kein Einkommen: Massgebendes Einkommen sinkt wegen höherem Familienabzug
	 * Heirat: 16.04.2017
	 * Mutation eingereicht:  30.09.2017 (zu spät)
	 * => Anpassung Familiengrösse und Massgebendes Einkommen per 01.05.2018 (Folgemonat Meldung)
	 */
	@Test
	public void mutationHeiratOhneZweiteinkommenZuSpaet() {
		Betreuung mutationBetreuung =
			createMutationHeirat(EINREICHUNG_ZU_SPAET, LocalDate.of(2017, Month.APRIL, 16), 0);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 3, 50000, 38600);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 3, 50000, 38600);
	}

	/**
	 * Mutation, höheres Einkommen für ganze Periode
	 * Mutation eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Massgebendes Einkommen gilt für die ganze Periode
	 */
	@Test
	public void mutationErhoehungFinanzelleSituationRechtzeitig() {
		Betreuung mutationBetreuung = createMutationFinanzielleSituation(EINREICHUNG_RECHTZEITIG, 70000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 70000, 70000);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 70000, 70000);
	}

	/**
	 * Mutation, höheres Einkommen für ganze Periode
	 * Mutation eingereicht: 30.04.2018 (zu spät)
	 * => Anpassung Massgebendes Einkommen gilt für die ganze Periode, unabhängig vom Einreichungsdatum
	 */
	@Test
	public void mutationErhoehungFinanzielleSituationZuSpaet() {
		Betreuung mutationBetreuung = createMutationFinanzielleSituation(EINREICHUNG_ZU_SPAET, 70000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 70000, 70000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 70000, 70000, true);
	}

	/**
	 * Mutation, tieferes Einkommen für ganze Periode
	 * Mutation eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Massgebendes Einkommen gilt für die ganze Periode
	 */
	@Test
	public void mutationVerringerungFinanzielleSituationRechtzeitig() {
		Betreuung mutationBetreuung = createMutationFinanzielleSituation(EINREICHUNG_RECHTZEITIG, 30000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 30000, 30000, true);
	}

	/**
	 * Mutation, tieferes Einkommen für ganze Periode
	 * Mutation eingereicht:  30.09.2017 (zu spät)
	 * => Anpassung Massgebendes Einkommen gilt per 01.05.2018 (Folgemonat Meldung)
	 */
	@Test
	public void mutationVerringerungFinanzielleSituationZuSpaet() {
		Betreuung mutationBetreuung = createMutationFinanzielleSituation(EINREICHUNG_ZU_SPAET, 30000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutationBetreuung);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000, true);

		// Anpassung FinSit zu spaet eingereicht: Gilt ab Oktober
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 30000, 30000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 30000, 30000, true);
	}

	/**
	 * Erstgesuch mit EKV
	 * EKV ab 16.11.2017
	 * Eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Massgebendes Einkommen gilt per 01.12.2017 (Folgemonat Ereignis)
	 */
	@Test
	public void erstgesuchEinkommensverschlechterungRechtzeitig() {
		Betreuung erstgesuch = createErstgesuch(EINREICHUNG_RECHTZEITIG, false, 50000);
		createEinkommensverschlechterung(erstgesuch.extractGesuch(), 20000, 0);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(erstgesuch);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 2, 20000, 20000, true);

		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 50000, 50000, true);
	}

	/**
	 * Erstgesuch mit EKV
	 * EKV ab 16.11.2017
	 * Eingereicht:  30.09.2017 (zu spät)
	 * => Gesamtanspruch beginnt per 01.05.2018 (Folgemonat Meldung), das Einkommen wird aber schon ab Ereignisdatum
	 * (15.12.). neu berechnet
	 */
	@Test
	public void erstgesuchEinkommensverschlechterungZuSpaet() {
		Betreuung erstgesuch = createErstgesuch(EINREICHUNG_ZU_SPAET, false, 50000);
		createEinkommensverschlechterung(erstgesuch.extractGesuch(), 20000, 0);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(erstgesuch);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 20000, 20000, false);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 20000, 20000, false);

		// EKV 1 zu spät eingereicht: Gilt ab Oktober
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 2, 20000, 20000, true);

		// Keine EKV 2 erfasst: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 2, 50000, 50000, true);

		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 50000, 50000, true);
	}

	/**
	 * Mutation mit EKV
	 * EKV ab 16.11.2017
	 * Eingereicht: 30.05.2017 (rechtzeitig)
	 * => Anpassung Massgebendes Einkommen gilt per 01.12.2017 (Folgemonat Ereignis)
	 */
	@Test
	public void mutationEinkommensverschlechterungRechtzeitig() {
		Betreuung mutation = createMutationEinkommensverschlechterung(EINREICHUNG_RECHTZEITIG, 20000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutation);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 2, 20000, 20000, true);

		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 50000, 50000, true);
	}

	/**
	 * Mutation mit EKV
	 * EKV ab 16.11.2017
	 * Eingereicht:  30.09.2017 (zu spät)
	 * => Anpassung Massgebendes Einkommen gilt per 01.05.2018 (Folgemonat Meldung)
	 */
	@Test
	public void mutationEinkommensverschlechterungZuSpaet() {
		Betreuung mutation = createMutationEinkommensverschlechterung(EINREICHUNG_ZU_SPAET, 20000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutation);
		Assert.assertEquals(12, result.size());
		int i = 0;

		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 2, 50000, 50000, true);

		// EkV 1: Eingereicht im September, gilt ab Oktober
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 2, 20000, 20000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 2, 20000, 20000, true);

		// Keine EKV 2 -> ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 50000, 50000, true);
		assertZeitabschnitt(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 50000, 50000, true);
	}

	/**
	 * Mutation verheiratet, der Partner verdient weniger, danach erfolgt aber die Scheidung
	 * => Bis zur Scheidung müsste die EKV gelten, danach nicht mehr, da GS1 keine Verschlechterung erfahren hat
	 */
	@Test
	public void mutationScheidungMitEinkommensverschlechterungGs2() {
		LocalDate scheidungsdatum = LocalDate.of(2018, Month.APRIL, 16);
		Betreuung erstgesuch = createErstgesuch(EINREICHUNG_RECHTZEITIG, true, 50000);
		Betreuung mutation = createMutationScheidungMitEkv(erstgesuch.extractGesuch(),
			EINREICHUNG_RECHTZEITIG,
			scheidungsdatum,
			50000,
			10000);

		List<VerfuegungZeitabschnitt> result = calculateInklAllgemeineRegeln(mutation);
		Assert.assertEquals(12, result.size());
		int i = 0;

		// EKV 1
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2017, Month.AUGUST, 1), 3, 60000, 2017);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2017, Month.SEPTEMBER, 1), 3, 60000, 2017);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2017, Month.OCTOBER, 1), 3, 60000, 2017);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2017, Month.NOVEMBER, 1), 3, 60000, 2017);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2017, Month.DECEMBER, 1), 3, 60000, 2017);

		// Keine EKV 2: Ab Januar gilt wieder das Einkommen des Basisjahres
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2018, Month.JANUARY, 1), 3, 100000, 2016);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2018, Month.FEBRUARY, 1), 3, 100000, 2016);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2018, Month.MARCH, 1), 3, 100000, 2016);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2018, Month.APRIL, 1), 3, 100000, 2016);
		// Scheidung
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2018, Month.MAY, 1), 2, 50000, 2016);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2018, Month.JUNE, 1), 2, 50000, 2016);
		assertZeitabschnittMitJahr(result.get(i++), LocalDate.of(2018, Month.JULY, 1), 2, 50000, 2016);
	}

	private void assertZeitabschnitt(
		@Nonnull VerfuegungZeitabschnitt abschnitt,
		@Nonnull LocalDate gueltigAb,
		int familiengroesse,
		int massgEinkVorAbzug,
		int massgEink) {
		Assert.assertEquals(gueltigAb, abschnitt.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(familiengroesse, abschnitt.getFamGroesse().intValue());
		Assert.assertEquals(massgEinkVorAbzug, abschnitt.getMassgebendesEinkommenVorAbzFamgr().intValue());
		Assert.assertEquals(massgEink, abschnitt.getMassgebendesEinkommen().intValue());
	}

	private void assertZeitabschnitt(
		@Nonnull VerfuegungZeitabschnitt abschnitt,
		@Nonnull LocalDate gueltigAb,
		int familiengroesse,
		int massgEinkVorAbzug,
		int massgEink,
		boolean hasAnspruch) {
		Assert.assertEquals(gueltigAb, abschnitt.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(familiengroesse, abschnitt.getFamGroesse().intValue());
		Assert.assertEquals(massgEinkVorAbzug, abschnitt.getMassgebendesEinkommenVorAbzFamgr().intValue());
		Assert.assertEquals(massgEink, abschnitt.getMassgebendesEinkommen().intValue());
		Assert.assertEquals(hasAnspruch, abschnitt.getAnspruchberechtigtesPensum() > 0);
	}

	private void assertZeitabschnittMitJahr(
		@Nonnull VerfuegungZeitabschnitt abschnitt, @Nonnull LocalDate gueltigAb, int familiengroesse,
		int massgEinkVorAbzug,
		int einkommensjahr) {
		Assert.assertEquals(gueltigAb, abschnitt.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(familiengroesse, abschnitt.getFamGroesse().intValue());
		Assert.assertEquals(massgEinkVorAbzug, abschnitt.getMassgebendesEinkommenVorAbzFamgr().intValue());
		Assert.assertEquals(einkommensjahr, abschnitt.getEinkommensjahr().intValue());
	}

	private Betreuung createErstgesuch(@Nonnull LocalDate eingangsdatum, boolean zuZweit, int einkommmenProGs) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA,
			100,
			BigDecimal.valueOf(2000));
		betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
		Gesuch gesuch = betreuung.extractGesuch();
		GesuchstellerContainer gs1 = gesuch.getGesuchsteller1();
		Assert.assertNotNull(gs1);
		Assert.assertNotNull(gs1.getFinanzielleSituationContainer());
		gs1.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			100));
		gs1.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettolohn(MathUtil.DEFAULT.from(einkommmenProGs));
		gesuch.setRegelnGueltigAb(eingangsdatum);
		if (zuZweit) {
			Familiensituation familiensituation = gesuch.extractFamiliensituation();
			Assert.assertNotNull(familiensituation);
			familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
			GesuchstellerContainer gs2 = new GesuchstellerContainer();
			Assert.assertNotNull(gs2);
			gesuch.setGesuchsteller2(gs2);
			gs2.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
				TestDataUtil.START_PERIODE,
				TestDataUtil.ENDE_PERIODE,
				100));
			gs2.setFinanzielleSituationContainer(new FinanzielleSituationContainer());
			Assert.assertNotNull(gs2.getFinanzielleSituationContainer());
			gs2.getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
			gs2.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setNettolohn(MathUtil.DEFAULT.from(einkommmenProGs));
		}
		return betreuung;
	}

	private Betreuung createMutation(@Nullable Gesuch erstgesuch, @Nonnull LocalDate eingangsdatum, boolean zuZweit) {
		if (erstgesuch == null) {
			erstgesuch = createErstgesuch(EINREICHUNG_RECHTZEITIG, zuZweit, 50000).extractGesuch();
		}
		List<VerfuegungZeitabschnitt> calculate =
			calculateInklAllgemeineRegeln(erstgesuch.extractAllBetreuungen().get(0));
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(calculate);
		Gesuch mutation = erstgesuch.copyForMutation(new Gesuch(), Eingangsart.ONLINE);
		addBetreuung(mutation.getKindContainers().iterator().next());
		mutation.extractAllBetreuungen().get(0).initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutation.setRegelnGueltigAb(eingangsdatum);
		return mutation.extractAllBetreuungen().get(0);
	}

	private Betreuung createMutationAlleine(@Nonnull LocalDate eingangsdatum) {
		Gesuch erstgesuch = createErstgesuch(EINREICHUNG_RECHTZEITIG, false, 50000).extractGesuch();
		addBetreuung(erstgesuch.getKindContainers().iterator().next());
		List<VerfuegungZeitabschnitt> calculate =
			calculateInklAllgemeineRegeln(erstgesuch.extractAllBetreuungen().get(0));
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(calculate);
		Gesuch mutation = erstgesuch.copyForMutation(new Gesuch(), Eingangsart.ONLINE);
		mutation.extractAllBetreuungen().get(0).initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutation.setRegelnGueltigAb(eingangsdatum);
		return mutation.extractAllBetreuungen().get(0);
	}

	private Betreuung createMutationHeirat(
		@Nonnull LocalDate eingangsdatum,
		@Nonnull LocalDate heiratsdatum,
		int einkommenPartner) {
		Betreuung mutationBetreuung = createMutationAlleine(eingangsdatum);
		Gesuch mutation = mutationBetreuung.extractGesuch();
		Familiensituation verheiratet = new Familiensituation();
		verheiratet.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		verheiratet.setAenderungPer(heiratsdatum);
		Assert.assertNotNull(mutation);
		Assert.assertNotNull(mutation.getFamiliensituationContainer());
		mutation.getFamiliensituationContainer().setFamiliensituationJA(verheiratet);
		if (einkommenPartner > 0) {
			mutation.setGesuchsteller2(new GesuchstellerContainer());
			Assert.assertNotNull(mutation.getGesuchsteller2());
			mutation.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
			Assert.assertNotNull(mutation.getGesuchsteller2().getFinanzielleSituationContainer());
			mutation.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.setFinanzielleSituationJA(new FinanzielleSituation());
			mutation.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setNettolohn(new BigDecimal(einkommenPartner));

		}
		return mutationBetreuung;
	}

	private Betreuung createMutationScheidungMitEkv(
		@Nullable Gesuch erstgesuch, @Nonnull LocalDate eingangsdatum, @Nonnull LocalDate scheidungsdatum,
		int einkommenGS1, int einkommenGS2) {
		Betreuung mutationBetreuung = createMutation(erstgesuch, eingangsdatum, true);
		Gesuch mutation = mutationBetreuung.extractGesuch();
		Familiensituation alleinerziehend = new Familiensituation();
		alleinerziehend.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		alleinerziehend.setAenderungPer(scheidungsdatum);
		Assert.assertNotNull(mutation);
		Assert.assertNotNull(mutation.getFamiliensituationContainer());
		mutation.getFamiliensituationContainer().setFamiliensituationJA(alleinerziehend);
		createEinkommensverschlechterung(mutation, einkommenGS1, einkommenGS2);
		return mutationBetreuung;
	}

	private Betreuung createMutationFinanzielleSituation(@Nonnull LocalDate eingangsdatum, int neuesEinkommen) {
		Betreuung mutationBetreuung = createMutationAlleine(eingangsdatum);
		Gesuch mutation = mutationBetreuung.extractGesuch();
		Assert.assertNotNull(mutation.getGesuchsteller1());
		Assert.assertNotNull(mutation.getGesuchsteller1().getFinanzielleSituationContainer());
		mutation.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettolohn(new BigDecimal(neuesEinkommen));
		return mutationBetreuung;
	}

	private Betreuung createMutationEinkommensverschlechterung(@Nonnull LocalDate eingangsdatum, int neuesEinkommen) {
		Betreuung mutationBetreuung = createMutationAlleine(eingangsdatum);
		Gesuch mutation = mutationBetreuung.extractGesuch();
		createEinkommensverschlechterung(mutation, neuesEinkommen, 0);
		return mutationBetreuung;
	}

	private void createEinkommensverschlechterung(
		@Nonnull Gesuch gesuch,
		int neuesEinkommenGS1,
		int neuesEinkommenGS2) {
		// GS1
		if (gesuch.getGesuchsteller1() != null) {
			createEinkommensverschlechterungForGesuchsteller(gesuch.getGesuchsteller1(), neuesEinkommenGS1);
		}
		// GS2
		if (gesuch.getGesuchsteller2() != null) {
			createEinkommensverschlechterungForGesuchsteller(gesuch.getGesuchsteller2(), neuesEinkommenGS2);
		}
		gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
		Assert.assertNotNull(gesuch.getEinkommensverschlechterungInfoContainer());
		gesuch.getEinkommensverschlechterungInfoContainer()
			.getEinkommensverschlechterungInfoJA()
			.setEinkommensverschlechterung(true);
		gesuch.getEinkommensverschlechterungInfoContainer()
			.getEinkommensverschlechterungInfoJA()
			.setEkvFuerBasisJahrPlus1(true);
		RECHNER.calculateFinanzDaten(gesuch, new BigDecimal(20));
	}

	private void createEinkommensverschlechterungForGesuchsteller(
		@Nonnull GesuchstellerContainer gesuchsteller,
		int neuesEinkommen) {
		Assert.assertNotNull(gesuchsteller);
		gesuchsteller.setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		Assert.assertNotNull(gesuchsteller.getEinkommensverschlechterungContainer());
		gesuchsteller.getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
		gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().setNettolohn(new BigDecimal(neuesEinkommen));
	}

	private void addBetreuung(@Nonnull KindContainer kind) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA,
			100,
			BigDecimal.valueOf(2000));
		kind.getBetreuungen().add(betreuung);
		betreuung.setKind(kind);
	}

	private void addKind(@Nonnull Gesuch gesuch, @Nonnull LocalDate geburtsdatum) {
		KindContainer neuesKind = TestDataUtil.createDefaultKindContainer();
		neuesKind.getKindJA().setGeburtsdatum(geburtsdatum);
		neuesKind.getKindJA().setFamilienErgaenzendeBetreuung(false);
		gesuch.getKindContainers().add(neuesKind);
	}
}

