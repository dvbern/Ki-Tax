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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests fuer KesbPlatzierungCalcRule
 */
public class KesbPlatzierungCalcRuleTest {

	@SuppressWarnings({ "InstanceVariableMayNotBeInitialized", "NullableProblems" })
	@Nonnull
	private Betreuung betreuung;

	@Before
	public void setUp() {
		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
			Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
			BetreuungsangebotTyp.KITA,
			60,
			new BigDecimal(2000));
		ErwerbspensumContainer erwerbspensumContainer = TestDataUtil.createErwerbspensumContainer();
		Assert.assertNotNull(erwerbspensumContainer.getErwerbspensumJA());
		erwerbspensumContainer.getErwerbspensumJA().setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		Assert.assertNotNull(betreuung.extractGesuch().getGesuchsteller1());
		Assert.assertNotNull(Objects.requireNonNull(betreuung.extractGesuch().getGesuchsteller1())
			.getErwerbspensenContainers());
		Objects.requireNonNull(betreuung.extractGesuch().getGesuchsteller1())
			.addErwerbspensumContainer(erwerbspensumContainer);
	}

	@Test
	public void testPensumWithKesbPlatzierung() {
		Assert.assertNotNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA());
		betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA().setKeineKesbPlatzierung(false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).getBemerkungenDTOList().isEmpty());
		Assert.assertEquals(2, result.get(0).getBemerkungenDTOList().uniqueSize());
		Assert.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.KESB_PLATZIERUNG_MSG));
	}

	@Test
	public void testPensumWithoutKesbPlatzierung() {
		Assert.assertNotNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA());
		betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA().setKeineKesbPlatzierung(true);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		Assert.assertEquals(1, result.size());
		Assert.assertNotEquals(0, result.get(0).getAnspruchberechtigtesPensum());
	}
}
