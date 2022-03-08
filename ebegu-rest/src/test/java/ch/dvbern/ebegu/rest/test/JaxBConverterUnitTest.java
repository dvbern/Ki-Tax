/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rest.test;

import java.math.BigDecimal;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxFinanzielleSituation;
import ch.dvbern.ebegu.api.dtos.JaxFinanzielleSituationContainer;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Assert;
import org.junit.Test;

public class JaxBConverterUnitTest {
	@Test
	public void overwriteFinSitWhenDataNotFromSteuern() {
		var converter = new JaxBConverter();
		var container = TestDataUtil.createFinanzielleSituationContainer();
		var jaxContainer = new JaxFinanzielleSituationContainer();
		var jaxFinSit = new JaxFinanzielleSituation();
		jaxContainer.setFinanzielleSituationJA(jaxFinSit);
		jaxFinSit.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(BigDecimal.TEN);
		jaxFinSit.setEinkommenInVereinfachtemVerfahrenAbgerechnet(true);
		jaxFinSit.setNettoVermoegen(BigDecimal.valueOf(1000));
		jaxFinSit.setNettolohn(BigDecimal.valueOf(100));
		converter.finanzielleSituationContainerToStorableEntity(jaxContainer, container);
		Assert.assertTrue(container.getFinanzielleSituationJA().getEinkommenInVereinfachtemVerfahrenAbgerechnet());
		Assert.assertEquals(BigDecimal.TEN, container.getFinanzielleSituationJA().getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet());
		Assert.assertEquals(BigDecimal.valueOf(1000), container.getFinanzielleSituationJA().getNettoVermoegen());
		Assert.assertEquals(BigDecimal.valueOf(100), container.getFinanzielleSituationJA().getNettolohn());
	}

	@Test
	public void dontOverwriteFinSitWhenDataFromSteuern() {
		var converter = new JaxBConverter();
		var container = TestDataUtil.createFinanzielleSituationContainer();
		var finSit = TestDataUtil.createDefaultFinanzielleSituation();
		finSit.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.PROVISORISCH);
		container.setFinanzielleSituationJA(finSit);
		var jaxContainer = new JaxFinanzielleSituationContainer();
		var jaxFinSit = new JaxFinanzielleSituation();
		jaxContainer.setFinanzielleSituationJA(jaxFinSit);
		jaxFinSit.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(BigDecimal.TEN);
		jaxFinSit.setEinkommenInVereinfachtemVerfahrenAbgerechnet(true);
		jaxFinSit.setNettoVermoegen(BigDecimal.valueOf(100));
		jaxFinSit.setNettolohn(BigDecimal.valueOf(9999));
		converter.finanzielleSituationContainerToStorableEntity(jaxContainer, container);
		Assert.assertTrue(container.getFinanzielleSituationJA().getEinkommenInVereinfachtemVerfahrenAbgerechnet());
		Assert.assertEquals(BigDecimal.TEN, container.getFinanzielleSituationJA().getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet());
		Assert.assertEquals(BigDecimal.valueOf(100000), container.getFinanzielleSituationJA().getNettolohn());
		Assert.assertNull(container.getFinanzielleSituationJA().getNettoVermoegen());
	}
}
