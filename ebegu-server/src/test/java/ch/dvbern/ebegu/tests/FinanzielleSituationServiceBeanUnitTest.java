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

package ch.dvbern.ebegu.tests;

import java.math.BigDecimal;

import ch.dvbern.ebegu.dto.JaxFinanzielleSituationAufteilungDTO;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.FinanzielleSituationServiceBean;
import org.junit.Assert;
import org.junit.Test;

public class FinanzielleSituationServiceBeanUnitTest {

	private final FinanzielleSituationServiceBean finanzielleSituationService = new FinanzielleSituationServiceBean();

	@Test
	public void testUpdateFinSitFromAufteilung() {

		var dto = new JaxFinanzielleSituationAufteilungDTO();
		dto.setBruttoertraegeVermoegenGS1(BigDecimal.valueOf(1000));
		dto.setBruttoertraegeVermoegenGS2(BigDecimal.valueOf(2000));

		dto.setGeleisteteAlimenteGS1(BigDecimal.valueOf(500));
		dto.setGeleisteteAlimenteGS2(BigDecimal.valueOf(700));

		dto.setAbzugSchuldzinsenGS1(BigDecimal.valueOf(1));
		dto.setAbzugSchuldzinsenGS2(BigDecimal.valueOf(1));

		dto.setGewinnungskostenGS1(BigDecimal.valueOf(1));
		dto.setGewinnungskostenGS2(BigDecimal.valueOf(1));

		dto.setNettoertraegeErbengemeinschaftGS1(BigDecimal.valueOf(1));
		dto.setNettoertraegeErbengemeinschaftGS2(BigDecimal.valueOf(1));

		dto.setNettovermoegenGS1(BigDecimal.valueOf(1));
		dto.setNettovermoegenGS2(BigDecimal.valueOf(1));

		var finSitGS1 = new FinanzielleSituation();
		var finSitGS2 = new FinanzielleSituation();

		finSitGS1.setBruttoertraegeVermoegen(BigDecimal.valueOf(1500));
		finSitGS2.setBruttoertraegeVermoegen(BigDecimal.valueOf(1500));

		finSitGS1.setGeleisteteAlimente(BigDecimal.valueOf(600));
		finSitGS2.setGeleisteteAlimente(BigDecimal.valueOf(600));

		finSitGS1.setAbzugSchuldzinsen(BigDecimal.valueOf(2));
		finSitGS1.setGewinnungskosten(BigDecimal.valueOf(2));
		finSitGS1.setNettoertraegeErbengemeinschaft(BigDecimal.valueOf(2));
		finSitGS1.setNettoVermoegen(BigDecimal.valueOf(2));

		finSitGS2.setAbzugSchuldzinsen(BigDecimal.ZERO);
		finSitGS2.setGewinnungskosten(BigDecimal.ZERO);
		finSitGS2.setNettoertraegeErbengemeinschaft(BigDecimal.ZERO);
		finSitGS2.setNettoVermoegen(BigDecimal.ZERO);

		finanzielleSituationService.setValuesFromAufteilungDTO(finSitGS1, finSitGS2, dto);

		Assert.assertEquals(BigDecimal.valueOf(1000), finSitGS1.getBruttoertraegeVermoegen());
		Assert.assertEquals(BigDecimal.valueOf(2000), finSitGS2.getBruttoertraegeVermoegen());
		Assert.assertEquals(BigDecimal.valueOf(500), finSitGS1.getGeleisteteAlimente());
		Assert.assertEquals(BigDecimal.valueOf(700), finSitGS2.getGeleisteteAlimente());

		Assert.assertEquals(BigDecimal.valueOf(1), finSitGS1.getAbzugSchuldzinsen());
		Assert.assertEquals(BigDecimal.valueOf(1), finSitGS1.getGewinnungskosten());
		Assert.assertEquals(BigDecimal.valueOf(1), finSitGS1.getNettoertraegeErbengemeinschaft());
		Assert.assertEquals(BigDecimal.valueOf(1), finSitGS1.getNettoVermoegen());

		Assert.assertEquals(BigDecimal.valueOf(1), finSitGS2.getAbzugSchuldzinsen());
		Assert.assertEquals(BigDecimal.valueOf(1), finSitGS2.getGewinnungskosten());
		Assert.assertEquals(BigDecimal.valueOf(1), finSitGS2.getNettoertraegeErbengemeinschaft());
		Assert.assertEquals(BigDecimal.valueOf(1), finSitGS2.getNettoVermoegen());

	}

	@Test(expected = EbeguRuntimeException.class)
	public void testAufteilungWrongSum() {
		var dto = new JaxFinanzielleSituationAufteilungDTO();
		dto.setBruttoertraegeVermoegenGS1(BigDecimal.valueOf(1000));
		dto.setBruttoertraegeVermoegenGS2(BigDecimal.valueOf(2000));

		var finSitGS1 = new FinanzielleSituation();
		var finSitGS2 = new FinanzielleSituation();

		finSitGS1.setBruttoertraegeVermoegen(BigDecimal.valueOf(1500));
		finSitGS2.setBruttoertraegeVermoegen(BigDecimal.valueOf(1499));

		finanzielleSituationService.setValuesFromAufteilungDTO(finSitGS1, finSitGS2, dto);
	}
}
