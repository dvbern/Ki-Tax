/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.wizardx.gemeindekennzahlen;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.wizardx.WizardStateEnum;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;
import org.jetbrains.annotations.NotNull;

public class FormularStep implements WizardStep<GemeindeKennzahlenWizard> {

	@Override
	public void next(@Nonnull GemeindeKennzahlenWizard wizard) {
		// only step
	}

	@Override
	public void prev(@Nonnull GemeindeKennzahlenWizard wizard) {
		// only step
	}

	@Override
	public WizardStateEnum getStatus(@Nonnull GemeindeKennzahlenWizard wizard) {
		return wizard.getGemeindeKennzahlen()
				.isAntragAbgeschlossen() ?
				WizardStateEnum.OK :
				WizardStateEnum.IN_BEARBEITUNG;
	}


	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.GEMEINDE_KENNZAHLEN;
	}

	@Override
	public String getWizardStepName() {
		return GemeindeKennzahlenWizardStepsEnum.FORMULAR.name();
	}

	@Override
	public boolean isDisabled(@NotNull GemeindeKennzahlenWizard wizard) {
		return false;
	}
}
