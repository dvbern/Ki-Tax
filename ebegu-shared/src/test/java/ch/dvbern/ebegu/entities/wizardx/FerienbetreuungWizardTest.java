/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities.wizardx;

import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.FerienbetreuungWizard;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.FerienbetreuungWizardStepsEnum;
import org.junit.Assert;
import org.junit.Test;

public class FerienbetreuungWizardTest {

	private final FerienbetreuungAngabenContainer container = new FerienbetreuungAngabenContainer();

	@Test
	public void testGemeindeRole() {
		UserRole role = UserRole.ADMIN_GEMEINDE;
		container.setStatus(FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE);
		FerienbetreuungWizard wizard = new FerienbetreuungWizard(role, container);

		Assert.assertEquals(FerienbetreuungWizardStepsEnum.STAMMDATEN_GEMEINDE.name(), wizard.getStep().getWizardStepName());
		wizard.nextState();
		Assert.assertEquals(FerienbetreuungWizardStepsEnum.ANGEBOT.name(), wizard.getStep().getWizardStepName());
		wizard.nextState();
		Assert.assertEquals(FerienbetreuungWizardStepsEnum.NUTZUNG.name(), wizard.getStep().getWizardStepName());
		wizard.nextState();
		Assert.assertEquals(FerienbetreuungWizardStepsEnum.KOSTEN_EINNAHMEN.name(), wizard.getStep().getWizardStepName());
		wizard.nextState();
		Assert.assertEquals(FerienbetreuungWizardStepsEnum.UPLOAD.name(), wizard.getStep().getWizardStepName());
		wizard.nextState();
		Assert.assertEquals(FerienbetreuungWizardStepsEnum.FREIGABE.name(), wizard.getStep().getWizardStepName());
		Assert.assertTrue(wizard.getStep().isDisabled(wizard));
		wizard.nextState();
		Assert.assertEquals(FerienbetreuungWizardStepsEnum.VERFUEGUNG.name(), wizard.getStep().getWizardStepName());
		Assert.assertTrue(wizard.getStep().isDisabled(wizard));
	}
}
