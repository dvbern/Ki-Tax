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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.time.LocalTime;

import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import org.junit.Assert;
import org.junit.Test;

public class CalculationUnitTest {

	@Test()
	public void testIsFruebetreuung() {
		ModulTagesschuleGroup group = new ModulTagesschuleGroup();

		group.setZeitVon(LocalTime.of(8,0));
		Assert.assertTrue(group.isFruehbetreuung());

		group.setZeitVon(LocalTime.of(11,29));
		Assert.assertTrue(group.isFruehbetreuung());

		group.setZeitVon(LocalTime.of(11, 30));
		Assert.assertFalse(group.isFruehbetreuung());
	}

	@Test()
	public void testIsMittagsbetreuung() {
		ModulTagesschuleGroup group = new ModulTagesschuleGroup();

		group.setZeitVon(LocalTime.of(11,30));
		Assert.assertTrue(group.isMittagsbetreuung());

		group.setZeitVon(LocalTime.of(13,14));
		Assert.assertTrue(group.isMittagsbetreuung());

		group.setZeitVon(LocalTime.of(13, 15));
		Assert.assertFalse(group.isMittagsbetreuung());
	}

	@Test()
	public void testNachmittagsbetreuung1() {
		ModulTagesschuleGroup group = new ModulTagesschuleGroup();

		group.setZeitVon(LocalTime.of(13,15));
		Assert.assertTrue(group.isNachmittagbetreuung1());

		group.setZeitVon(LocalTime.of(14,59));
		Assert.assertTrue(group.isNachmittagbetreuung1());

		group.setZeitVon(LocalTime.of(15, 0));
		Assert.assertFalse(group.isNachmittagbetreuung1());
	}

	@Test()
	public void testNachmittagsbetreuung2() {
		ModulTagesschuleGroup group = new ModulTagesschuleGroup();

		group.setZeitVon(LocalTime.of(15,0));
		Assert.assertTrue(group.isNachmittagbetreuung2());

		group.setZeitVon(LocalTime.of(20,0));
		Assert.assertTrue(group.isNachmittagbetreuung2());
	}
}
