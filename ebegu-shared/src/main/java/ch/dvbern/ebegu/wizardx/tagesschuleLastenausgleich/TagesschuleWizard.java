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

import java.util.Collection;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.wizardx.Wizard;

public class TagesschuleWizard extends Wizard {

	private final Collection<Institution> readableInstitutionsOfUser;
	private LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainer;

	public TagesschuleWizard(
		@Nonnull UserRole roleToUse,
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainer,
		@Nonnull Collection<Institution> readableInstitutionsOfUser) {
		this.role = roleToUse;
		this.lastenausgleichTagesschuleAngabenGemeindeContainer = lastenausgleichTagesschuleAngabenGemeindeContainer;
		this.readableInstitutionsOfUser = readableInstitutionsOfUser;
		if (role.isRoleGemeindeOrTS() || role.isRoleMandant() || role.isSuperadmin()) {
			this.setStep(new AngabenGemeindeStep());
		} else {
			this.setStep(new AngabenTagesschuleStep());
		}
	}

	public LastenausgleichTagesschuleAngabenGemeindeContainer getLastenausgleichTagesschuleAngabenGemeindeContainer() {
		return lastenausgleichTagesschuleAngabenGemeindeContainer;
	}

	public Collection<Institution> getReadableInstitutionsOfUser() {
		return readableInstitutionsOfUser;
	}
}
