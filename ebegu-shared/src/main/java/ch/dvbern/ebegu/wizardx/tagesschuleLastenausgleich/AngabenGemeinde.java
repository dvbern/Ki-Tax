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

public class AngabenGemeinde extends WizardStep<Gemeinde> {

	private LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainer;

	public AngabenGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainer
	) {
		this.lastenausgleichTagesschuleAngabenGemeindeContainer = lastenausgleichTagesschuleAngabenGemeindeContainer;
	};

	@Override
	public void next(
		Wizard tagesschuleWizard) {
		tagesschuleWizard.setStep(new AngabenTagesschule(lastenausgleichTagesschuleAngabenGemeindeContainer));
	}

	@Override
	public void prev(Wizard tagesschuleWizard) {
		//Nothing to do - initial State
	}

	@Override
	public WizardStateEnum getStatus(Gemeinde gemeinde) {
		return WizardStateEnum.OK;
	}

	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.LASTENAUSGLEICH_TS;
	}

	@Override
	public String getWizardStepName() {
		return TagesschuleWizardStepsEnum.ANGABEN_GEMEINDE.name();
	}
}
