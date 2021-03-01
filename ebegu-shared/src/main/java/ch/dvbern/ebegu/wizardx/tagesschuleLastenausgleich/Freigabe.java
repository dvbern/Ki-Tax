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

import ch.dvbern.ebegu.wizardx.WizardStateEnum;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;

public class Freigabe implements WizardStep<TagesschuleWizard> {

	@Override
	public void next(@Nonnull TagesschuleWizard tagesschuleWizard) {
		if (tagesschuleWizard.getRole().isRoleGemeindeOrTS()
			|| tagesschuleWizard.getRole().isRoleMandant()
			|| tagesschuleWizard.getRole().isSuperadmin()) {
			tagesschuleWizard.setStep(new Lastenausgleich());
		}
	}

	@Override
	public void prev(@Nonnull TagesschuleWizard tagesschuleWizard) {
		if (tagesschuleWizard.getRole().isRoleGemeindeOrTS()
			|| tagesschuleWizard.getRole().isRoleMandant()
			|| tagesschuleWizard.getRole().isSuperadmin()) {
			tagesschuleWizard.setStep(new AngabenTagesschule());
		}
	}

	@Override
	public WizardStateEnum getStatus(@Nonnull TagesschuleWizard wizard) {
		// IF ALL DATA Filled RETURN OK
		// IF NOT KO
		return WizardStateEnum.OK;
	}

	@Override
	public String getWizardStepName() {
		return TagesschuleWizardStepsEnum.FREIGABE.name();
	}

	@Override
	public boolean isDisabled(@Nonnull TagesschuleWizard wizard) {
		switch (wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer().getStatus()) {
		case IN_BEARBEITUNG_GEMEINDE:
			return !(wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer().allInstitutionenGeprueft());
		case NEU:
		default:
			return true;
		}

	}

	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.LASTENAUSGLEICH_TS;
	}
}
