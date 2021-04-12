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

package ch.dvbern.ebegu.api.dtos;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.wizardx.WizardStateEnum;

public class JaxWizardStepX {

	@NotNull
	private String wizardTyp;

	@NotNull
	private String stepName;

	@Nonnull
	private Boolean disabled;

	@Nonnull
	private WizardStateEnum status;

	public String getWizardTyp() {
		return wizardTyp;
	}

	public void setWizardTyp(String wizardTyp) {
		this.wizardTyp = wizardTyp;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	@Nonnull
	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(@Nonnull Boolean disabled) {
		this.disabled = disabled;
	}

	public void setStatus(WizardStateEnum status) {
		this.status = status;
	}

	public WizardStateEnum getStatus() {
		return status;
	}
}
