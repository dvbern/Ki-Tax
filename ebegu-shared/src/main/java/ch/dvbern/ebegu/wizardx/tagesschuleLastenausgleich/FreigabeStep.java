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

import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.wizardx.WizardStateEnum;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;

public class FreigabeStep implements WizardStep<TagesschuleWizard> {

	@Override
	public void next(@Nonnull TagesschuleWizard tagesschuleWizard) {
		if (tagesschuleWizard.getRole().isRoleMandant()
			|| tagesschuleWizard.getRole().isSuperadmin()) {
			tagesschuleWizard.setStep(new LastenausgleichStep());
		}
	}

	@Override
	public void prev(@Nonnull TagesschuleWizard tagesschuleWizard) {
		if (tagesschuleWizard.getRole().isRoleGemeindeOrTS()
			|| tagesschuleWizard.getRole().isRoleMandant()
			|| tagesschuleWizard.getRole().isSuperadmin()) {
			tagesschuleWizard.setStep(new AngabenTagesschuleStep());
		}
	}

	@Override
	public WizardStateEnum getStatus(@Nonnull TagesschuleWizard wizard) {
		// IF ALL DATA Filled RETURN OK
		// IF NOT KO
		if (wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer().isAntragAbgeschlossen()) {
			return WizardStateEnum.OK;
		}
		if (wizard.getRole().isRoleGemeindeabhaengig()) {
			return wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer().isAtLeastInBearbeitungKanton() ?
				WizardStateEnum.OK :
				WizardStateEnum.IN_BEARBEITUNG;
		}
		return wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer().isAntragGeprueft() ?
			WizardStateEnum.OK :
			WizardStateEnum.IN_BEARBEITUNG;
	}

	@Override
	public String getWizardStepName() {
		return TagesschuleWizardStepsEnum.FREIGABE.name();
	}

	@Override
	public boolean isDisabled(@Nonnull TagesschuleWizard wizard) {
		LastenausgleichTagesschuleAngabenGemeindeContainer container =
			wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer();
		UserRole role = wizard.getRole();

		if (container.isInStatusNeu()) {
			return true;
		}
		if (container.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE) {
			if (role.isRoleMandant()) {
				return true;
			}
			return !(container.isAngabenDeklarationAbgeschlossen()
				&& container.allInstitutionenGeprueft());
		}
		if (container.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.ZURUECK_AN_GEMEINDE) {
			if (role.isRoleMandant()) {
				return true;
			}
			return !(container.isAngabenKorrekturAbgeschlossen()
				&& container.allInstitutionenGeprueft());
		}
		return false;
	}

	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.LASTENAUSGLEICH_TAGESSCHULEN;
	}
}
