/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.mocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.needle4j.annotation.InjectIntoMany;

public class PrincipalBeanMock extends PrincipalBean {

	@Nullable
	private Benutzer benutzer;

	@InjectIntoMany
	private MandantServiceMock mandantService = new MandantServiceMock();


	@Nonnull
	@Override
	public Benutzer getBenutzer() {
		return benutzer != null ? benutzer : TestDataUtil.createDefaultBenutzer();
	}

	public void setBenutzer(Benutzer benutzer) {
		this.benutzer = benutzer;
	}

	@Override
	public boolean isCallerInRole(@Nonnull UserRole role) {
		return benutzer != null && benutzer.getRole() == role;
	}

	@Nonnull
	@Override
	public Mandant getMandant() {
		return mandantService.getMandantBern();
	}
}
