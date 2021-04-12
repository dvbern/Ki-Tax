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

import javax.enterprise.context.RequestScoped;

import ch.dvbern.ebegu.api.dtos.JaxWizardStepX;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.wizardx.Wizard;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.AbschlussStep;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.AngebotStep;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.FerienbetreuungWizardStepsEnum;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.KostenEinnahmenStep;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.NutzungStep;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.StammdatenGemeindeStep;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.UploadStep;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.AngabenGemeindeStep;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.AngabenTagesschuleStep;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.LastenausgleichStep;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.TagesschuleWizardStepsEnum;

@RequestScoped
public class JaxBWizardStepXConverter {

	public JaxWizardStepX convertStepToJax(WizardStep step, Wizard wizard) {
		JaxWizardStepX wizardStepX = new JaxWizardStepX();
		wizardStepX.setStepName(step.getWizardStepName());
		wizardStepX.setWizardTyp(step.getWizardTyp().name());
		wizardStepX.setStatus(step.getStatus(wizard));
		wizardStepX.setDisabled(step.isDisabled(wizard));
		return wizardStepX;
	}

	public WizardStep convertTagesschuleWizardStepJaxToStep(
		String step
	) {
		switch (TagesschuleWizardStepsEnum.valueOf(step)) {
		case ANGABEN_GEMEINDE:
			return new AngabenGemeindeStep();
		case ANGABEN_TAGESSCHULEN:
			return new AngabenTagesschuleStep();
		case FREIGABE:
			return new LastenausgleichStep();
		}
		return null;
	}

	public WizardStep convertFerienbetreuungWizardStepJaxToStep(
		String step
	) {
		switch (FerienbetreuungWizardStepsEnum.valueOf(step)) {
		case STAMMDATEN_GEMEINDE:
			return new StammdatenGemeindeStep();
		case ANGEBOT:
			return new AngebotStep();
		case NUTZUNG:
			return new NutzungStep();
		case KOSTEN_EINNAHMEN:
			return new KostenEinnahmenStep();
		case UPLOAD:
			return new UploadStep();
		case ABSCHLUSS:
			return new AbschlussStep();
		}
		return null;
	}
}
