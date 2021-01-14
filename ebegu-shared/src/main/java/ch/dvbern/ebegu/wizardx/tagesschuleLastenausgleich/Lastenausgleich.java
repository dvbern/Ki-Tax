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

package ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.wizardx.Wizard;
import ch.dvbern.ebegu.wizardx.WizardStateEnum;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;

public class Lastenausgleich implements WizardStep<TagesschuleWizard> {

	@Override
	public void next(
		@Nonnull TagesschuleWizard wizard) {
		//Last Step nothing to do
	}

	@Override
	public void prev(
		@Nonnull TagesschuleWizard wizard) {
		wizard.setStep(new AngabenTagesschule());
	}

	@Override
	public WizardStateEnum getStatus(@Nonnull TagesschuleWizard wizard) {
		return WizardStateEnum.OK;
	}

	@Override
	public String getWizardStepName() {
		return TagesschuleWizardStepsEnum.LASTENAUSGLEICH.name();
	}

	@Override
	public boolean isDisabled(@Nonnull TagesschuleWizard wizard) {
		return false;
	}

	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.LASTENAUSGLEICH_TS;
	}
}
