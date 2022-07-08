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

package ch.dvbern.ebegu.wizardx.ferienbetreuung;

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.wizardx.WizardStateEnum;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;

public class AbschlussStep implements WizardStep<FerienbetreuungWizard> {

	@Override
	public void next(@Nonnull FerienbetreuungWizard wizard) {
		// last step, do nothing
	}

	@Override
	public void prev(@Nonnull FerienbetreuungWizard wizard) {
		wizard.setStep(new UploadStep());
	}

	@Override
	public WizardStateEnum getStatus(@Nonnull FerienbetreuungWizard wizard) {
		if (wizard.getFerienbetreuungAngabenContainer().isAbgeschlossen()) {
			return WizardStateEnum.OK;
		}
		if (wizard.getFerienbetreuungAngabenContainer().isGeprueft()) {
			if (wizard.getRole().isRoleGemeindeabhaengig()) {
				return WizardStateEnum.OK;
			}
			return WizardStateEnum.IN_BEARBEITUNG;
		}
		if (wizard.getFerienbetreuungAngabenContainer().isInPruefungKanton()) {
			if (wizard.getRole().isRoleGemeindeabhaengig()) {
				return WizardStateEnum.OK;
			}

			if (Objects.requireNonNull(wizard.getFerienbetreuungAngabenContainer().getAngabenKorrektur())
				.isReadyForFreigeben()) {
				return WizardStateEnum.IN_BEARBEITUNG;
			}
			// step should be disabled
			return WizardStateEnum.NONE;
		}
		// IN_BEARBEITUNG_GEMEINDE
		if (wizard.getFerienbetreuungAngabenContainer().getAngabenDeklaration().isReadyForFreigeben()) {
			return WizardStateEnum.IN_BEARBEITUNG;
		}
		// zurueck an Gemeinde
		// TODO change zurueckAnGemeinde
		if (wizard.getFerienbetreuungAngabenContainer().getZurueckAnGemeinde()) {
			Objects.requireNonNull(wizard.getFerienbetreuungAngabenContainer().getAngabenKorrektur());
			if (wizard.getFerienbetreuungAngabenContainer().getAngabenKorrektur().isReadyForFreigeben()) {
				return WizardStateEnum.IN_BEARBEITUNG;
			}
		}
		// step should be disabled
		return WizardStateEnum.NONE;

	}

	@Override
	public String getWizardStepName() {
		return FerienbetreuungWizardStepsEnum.ABSCHLUSS.name();
	}

	@Override
	public boolean isDisabled(@Nonnull FerienbetreuungWizard wizard) {
		if (wizard.getRole().isRoleMandant()) {
			return wizard.getFerienbetreuungAngabenContainer().isInBearbeitungGemeinde();
		}
		if (wizard.getRole().isRoleGemeindeabhaengig() ||
			(wizard.getRole().isSuperadmin() && wizard.getFerienbetreuungAngabenContainer()
				.isInBearbeitungGemeinde())) {
			return wizard.getFerienbetreuungAngabenContainer().getDokumente() == null ||
				wizard.getFerienbetreuungAngabenContainer().getDokumente().isEmpty() ||
				!isReadyForFreigeben(wizard);
		}
		return false;
	}

	private boolean isReadyForFreigeben(@Nonnull FerienbetreuungWizard wizard) {
		var container = wizard.getFerienbetreuungAngabenContainer();
		// TODO change zurueckAnGemeinde
		if (container.getZurueckAnGemeinde()) {
			Objects.requireNonNull(container.getAngabenKorrektur());
			return container.getAngabenKorrektur().isReadyForFreigeben();
		}
		return container.getAngabenDeklaration().isReadyForFreigeben();
	}

	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.FERIENBETREUUNG;
	}
}
