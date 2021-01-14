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

package ch.dvbern.ebegu.wizardx;

import javax.annotation.Nonnull;

/**
 * Abstract class for Wizardsteps, take an entity as parameter
 * Routing should be wizardTyp.wizardStepName
 * Can be extended with other methods who should be shared through all wizards
 * @param <T>
 */
public abstract class WizardStep<T> {

	private boolean disabled = false;

	/**
	 * Next Step of the wizard - can be the same if none
	 * @param wizard
	 */
	abstract public void next(@Nonnull Wizard wizard);

	/**
	 * Previous Step of the wizard - can be the same if first
	 * @param wizard
	 */
	abstract public void prev(@Nonnull Wizard wizard);

	/**
	 * Status of the wizard
	 * @param t
	 * @return
	 */
	abstract public WizardStateEnum getStatus(T t);

	/**
	 * Typ of Wizard, used for routing with Step Name
	 * @return
	 */
	abstract public WizardTyp getWizardTyp();

	/**
	 * Name of the wizard step, used for routing
	 * @return
	 */
	abstract public String getWizardStepName();

	public boolean getDisabled() {
		return this.disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
