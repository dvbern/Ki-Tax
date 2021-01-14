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

package ch.dvbern.ebegu.api.converter;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;

import ch.dvbern.ebegu.api.dtos.JaxWizardStepX;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.wizardx.Wizard;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.AngabenGemeinde;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.AngabenTagesschule;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.Lastenausgleich;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.TagesschuleWizardStepsEnum;

@RequestScoped
public class JaxBWizardStepXConverter {

	public JaxWizardStepX convertStepToJax(WizardStep step, Wizard wizard) {
		JaxWizardStepX wizardStepX = new JaxWizardStepX();
		wizardStepX.setStepName(step.getWizardStepName());
		wizardStepX.setWizardTyp(step.getWizardTyp().name());
		wizardStepX.setDisabled(step.isDisabled(wizard));
		return wizardStepX;
	}

	public WizardStep convertTagesschuleWizardStepJaxToStep(
		String step
	) {
		switch (TagesschuleWizardStepsEnum.valueOf(step)) {
		case ANGABEN_GEMEINDE:
			return new AngabenGemeinde();
		case ANGABEN_TAGESSCHULEN:
			return new AngabenTagesschule();
		case FREIGABE:
			return new Lastenausgleich();
		}
		return null;
	}
}
