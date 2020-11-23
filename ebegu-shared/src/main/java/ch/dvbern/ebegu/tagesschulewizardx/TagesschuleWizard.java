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

package ch.dvbern.ebegu.tagesschulewizardx;


import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.UserRole;

public class TagesschuleWizard {

	private TagesschuleWizardStep step;
	private UserRole role;

	public void initTagesschuleWizard(@Nonnull UserRole roleToUse){
		this.role = roleToUse;
		if(role.isRoleGemeindeOrTS() || role.isRoleMandant() || role.isSuperadmin()){
			this.step = new AngabenGemeinde();
		}
	}


	public void previousState(@Nonnull UserRole role){
		this.step.next(role, this);
	}

	public void nextState(@Nonnull UserRole role) {
		this.step.prev(role, this);
	}
}
