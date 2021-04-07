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

import ch.dvbern.ebegu.enums.UserRole;

/**
 *  Abstract Class Wizard
 */
public abstract class Wizard {

	protected WizardStep step;
	protected UserRole role;

	public void previousState() {
		this.getStep().prev(this);
	}

	public void nextState() {
		this.getStep().next(this);
	}

	public WizardStep getStep() {
		return step;
	}

	public void setStep(WizardStep step) {
		this.step = step;
	}

	public UserRole getRole() {
		return role;
	}
}
