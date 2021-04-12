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

import java.util.Set;

import javax.annotation.Nonnull;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.wizardx.WizardStateEnum;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;

public class AngabenTagesschuleStep implements WizardStep<TagesschuleWizard> {


	@Override
	public void next(
		@Nonnull TagesschuleWizard wizard) {
		if (wizard.getRole().isRoleGemeindeOrTS() || wizard.getRole().isRoleMandant() || wizard.getRole()
			.isSuperadmin()) {
			wizard.setStep(new FreigabeStep());
		}
	}

	@Override
	public void prev(
		@Nonnull TagesschuleWizard wizard) {
		if (wizard.getRole().isRoleGemeindeOrTS() || wizard.getRole().isRoleMandant() || wizard.getRole()
			.isSuperadmin()) {
			wizard.setStep(new AngabenGemeindeStep());
		}
	}

	@Override
	public WizardStateEnum getStatus(@Nonnull TagesschuleWizard wizard) {
		final Set<LastenausgleichTagesschuleAngabenInstitutionContainer> containerList =
			wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer().getAngabenInstitutionContainers();

		if (wizard.getRole().isInstitutionRole()) {
			boolean userInstitutionsAbgeschlossen =
				containerList.stream()
					.filter(container -> wizard.getReadableInstitutionsOfUser()
						.stream()
						.anyMatch(institution -> institution.equals(container.getInstitution())))
				.reduce(true, (prev, cur) -> prev && cur.isAntragAtLeastInPruefungGemeinde(), Boolean::logicalAnd);

			return userInstitutionsAbgeschlossen? WizardStateEnum.OK : WizardStateEnum.IN_BEARBEITUNG;
		}

		return wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer().allInstitutionenGeprueft() ?
			WizardStateEnum.OK :
			WizardStateEnum.IN_BEARBEITUNG;
	}

	@Override
	public String getWizardStepName() {
		return TagesschuleWizardStepsEnum.ANGABEN_TAGESSCHULEN.name();
	}

	@Override
	public boolean isDisabled(@Nonnull TagesschuleWizard wizard) {
		LastenausgleichTagesschuleAngabenGemeindeStatus status =
			wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer().getStatus();
		return status.equals(LastenausgleichTagesschuleAngabenGemeindeStatus.NEU);
	}

	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.LASTENAUSGLEICH_TAGESSCHULEN;
	}
}
