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
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.wizardx.WizardStateEnum;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;

public class AngabenGemeindeStep implements WizardStep<TagesschuleWizard> {

	@Override
	public void next(
		TagesschuleWizard wizard) {
		wizard.setStep(new AngabenTagesschuleStep());
	}

	@Override
	public void prev(@Nonnull TagesschuleWizard wizard) {
		//Nothing to do - initial State
	}

	@Override
	public WizardStateEnum getStatus(@Nonnull TagesschuleWizard wizard) {
		final LastenausgleichTagesschuleAngabenGemeindeContainer container =
			wizard.getLastenausgleichTagesschuleAngabenGemeindeContainer();
		final LastenausgleichTagesschuleAngabenGemeindeStatus containerStatus = container.getStatus();

			switch (containerStatus) {
		case NEU:
			return WizardStateEnum.IN_BEARBEITUNG;
		case IN_BEARBEITUNG_GEMEINDE:
			return container.isAngabenDeklarationAbgeschlossen() ?
				WizardStateEnum.OK :
				WizardStateEnum.IN_BEARBEITUNG;
		case ZURUECK_AN_GEMEINDE:
			return container.isAngabenKorrekturAbgeschlossen() ?
				WizardStateEnum.OK :
				WizardStateEnum.IN_BEARBEITUNG;
		case IN_PRUEFUNG_KANTON:
		case ZWEITPRUEFUNG:
			if (wizard.getRole().isRoleGemeindeabhaengig()) {
				return WizardStateEnum.OK;
			}
			return container.isAngabenKorrekturAbgeschlossen() ?
				WizardStateEnum.OK :
				WizardStateEnum.IN_BEARBEITUNG;
		default:
			return WizardStateEnum.OK;
		}
	}

	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.LASTENAUSGLEICH_TAGESSCHULEN;
	}

	@Override
	public String getWizardStepName() {
		return TagesschuleWizardStepsEnum.ANGABEN_GEMEINDE.name();
	}

	@Override
	public boolean isDisabled(@Nonnull TagesschuleWizard wizard) {

		return false;
	}
}
