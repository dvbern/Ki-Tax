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

package ch.dvbern.ebegu.rules.tagesschule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MaxTarifBisFolgendeMonatRuleTest {

	Gesuch gesuch;

	@Before
	public void setUp() {
		gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setEingangsdatum(LocalDate.of(2017, 10,10));
	}

	@Test
	public void testMaxTarifBisFolgendeMonatRule() {
		MaxTarifBisFolgendeMonatRule maxTarifRule = new MaxTarifBisFolgendeMonatRule();

		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(100000));
		zeitabschnitt.setGueltigkeit(gesuch.getGesuchsperiode().getGueltigkeit());
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts = new ArrayList<>();
		verfuegungZeitabschnitts.add(zeitabschnitt);
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittsAfterRule =
			maxTarifRule.executeVerfuegungZeitabschnittRule(gesuch,
			verfuegungZeitabschnitts);
		Assert.assertEquals(2, verfuegungZeitabschnittsAfterRule.size());
	}
}
