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

package ch.dvbern.ebegu.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests fuer AbwesenheitAbschnittRule
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public class AbwesenheitAbschnittRuleTest {

	private final AbwesenheitAbschnittRule abwesenheitRule = new AbwesenheitAbschnittRule(
		Constants.DEFAULT_GUELTIGKEIT,
		TestDataUtil.ABWESENHEIT_DAYS_LIMIT);

	@Test
	public void testAbschnitteWithoutAbwesenheit() {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		final List<VerfuegungZeitabschnitt> zeitabschnitte = abwesenheitRule.createVerfuegungsZeitabschnitte(betreuung);

		assertNotNull(zeitabschnitte);
		assertEquals(0, zeitabschnitte.size());
	}

	@Test
	public void testAbschnitteShortAbwesenheit() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);

		final Set<AbwesenheitContainer> abwenseheitContList = new HashSet<>();
		abwenseheitContList.add(TestDataUtil.createShortAbwesenheitContainer(betreuung.extractGesuchsperiode()));
		betreuung.setAbwesenheitContainers(abwenseheitContList);

		final List<VerfuegungZeitabschnitt> zeitabschnitte = abwesenheitRule.createVerfuegungsZeitabschnitte(betreuung);

		assertNotNull(zeitabschnitte);
		assertEquals(0, zeitabschnitte.size());
	}

	@Test
	public void testAbschnitteLongAbwesenheit() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);

		final Set<AbwesenheitContainer> abwenseheitContList = new HashSet<>();
		final AbwesenheitContainer abwesenheit = TestDataUtil.createLongAbwesenheitContainer(betreuung.extractGesuchsperiode());
		abwenseheitContList.add(abwesenheit);
		betreuung.setAbwesenheitContainers(abwenseheitContList);

		final List<VerfuegungZeitabschnitt> zeitabschnitte = abwesenheitRule.createVerfuegungsZeitabschnitte(betreuung);


		assertNotNull(zeitabschnitte);
		assertEquals(1, zeitabschnitte.size());

		checkAbwesenheitAbschnitte(abwesenheit, null, zeitabschnitte);
	}

	@Test
	public void testAbschnitteSeveralLongAbw() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);

		final Set<AbwesenheitContainer> abwenseheitContList = new HashSet<>();
		final AbwesenheitContainer abwesenheit1 = TestDataUtil.createLongAbwesenheitContainer(betreuung.extractGesuchsperiode());
		abwenseheitContList.add(abwesenheit1);

		//neue lange Abwesenheit die 3 Monate spaeter stattfindet
		final AbwesenheitContainer secondAbwesenheit = TestDataUtil.createLongAbwesenheitContainer(betreuung.extractGesuchsperiode());
		secondAbwesenheit.getAbwesenheitJA().getGueltigkeit().setGueltigAb(
			secondAbwesenheit.getAbwesenheitJA().getGueltigkeit().getGueltigAb().plusMonths(3));
		secondAbwesenheit.getAbwesenheitJA().getGueltigkeit().setGueltigBis(
			secondAbwesenheit.getAbwesenheitJA().getGueltigkeit().getGueltigBis().plusMonths(3));
		abwenseheitContList.add(secondAbwesenheit);

		betreuung.setAbwesenheitContainers(abwenseheitContList);

		final List<VerfuegungZeitabschnitt> zeitabschnitte = abwesenheitRule.createVerfuegungsZeitabschnitte(betreuung);

		assertNotNull(zeitabschnitte);
		assertEquals("Es werden beide Abwesenheiten beruecksichtigt", 2, zeitabschnitte.size());

		checkAbwesenheitAbschnitte(abwesenheit1, secondAbwesenheit, zeitabschnitte);
	}

	@Test
	public void testAbschnitteShortLongAbw() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);

		final Set<AbwesenheitContainer> abwenseheitContList = new HashSet<>();
		final AbwesenheitContainer abwesenheit1 = TestDataUtil.createShortAbwesenheitContainer(betreuung.extractGesuchsperiode());
		abwenseheitContList.add(abwesenheit1);

		//neue lange Abwesenheit die 3 Monate spaeter stattfindet
		final AbwesenheitContainer lateAbwesenheit = TestDataUtil.createLongAbwesenheitContainer(betreuung.extractGesuchsperiode());
		lateAbwesenheit.getAbwesenheitJA().getGueltigkeit().setGueltigAb(
			lateAbwesenheit.getAbwesenheitJA().getGueltigkeit().getGueltigAb().plusMonths(3));
		lateAbwesenheit.getAbwesenheitJA().getGueltigkeit().setGueltigBis(
			lateAbwesenheit.getAbwesenheitJA().getGueltigkeit().getGueltigBis().plusMonths(3));
		abwenseheitContList.add(lateAbwesenheit);

		betreuung.setAbwesenheitContainers(abwenseheitContList);

		final List<VerfuegungZeitabschnitt> zeitabschnitte = abwesenheitRule.createVerfuegungsZeitabschnitte(betreuung);

		assertNotNull(zeitabschnitte);
		assertEquals("Die erste Abwesenheit wird nicht beruecksichtigt da sie kurz ist. Nur die 2 erstellt die Zeitabschnitte", 1, zeitabschnitte.size());

		checkAbwesenheitAbschnitte(lateAbwesenheit, null, zeitabschnitte);
	}

	private void checkAbwesenheitAbschnitte(AbwesenheitContainer abwesenheit, AbwesenheitContainer second, List<VerfuegungZeitabschnitt> zeitabschnitte) {
		assertTrue(zeitabschnitte.get(0).isLongAbwesenheit());
		assertEquals(abwesenheit.getAbwesenheitJA().getGueltigkeit().getGueltigAb().plusDays(TestDataUtil.ABWESENHEIT_DAYS_LIMIT),
			zeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		assertEquals(abwesenheit.getAbwesenheitJA().getGueltigkeit().getGueltigBis(),
			zeitabschnitte.get(0).getGueltigkeit().getGueltigBis());

		if (second != null) {
			assertTrue(zeitabschnitte.get(1).isLongAbwesenheit());
			assertEquals(second.getAbwesenheitJA().getGueltigkeit().getGueltigAb().plusDays(TestDataUtil.ABWESENHEIT_DAYS_LIMIT),
				zeitabschnitte.get(1).getGueltigkeit().getGueltigAb());
			assertEquals(second.getAbwesenheitJA().getGueltigkeit().getGueltigBis(),
				zeitabschnitte.get(1).getGueltigkeit().getGueltigBis());
		}

	}
}
