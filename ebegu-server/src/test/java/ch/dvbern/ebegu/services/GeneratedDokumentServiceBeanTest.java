/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.needle4j.annotation.ObjectUnderTest;
import org.needle4j.junit.NeedleRule;

@SuppressWarnings("unused")
public class GeneratedDokumentServiceBeanTest {

	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest
	private GeneratedDokumentServiceBean dokumentServiceBean;

	@Test
	public void ibanWithoutSpaces() {
		Assert.assertEquals("CH3909000000306638172", dokumentServiceBean.ibanToUnformattedString(new IBAN("CH39 0900 0000 3066 3817 2")));
		Assert.assertEquals("CH3909000000306638172", dokumentServiceBean.ibanToUnformattedString(new IBAN("CH3909000000306638172")));
	}
}
