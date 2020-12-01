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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionStatus;

public class JaxLastenausgleichTagesschuleAngabenInstitutionContainer extends JaxAbstractDTO {

	private static final long serialVersionUID = -4016185471589625587L;

	@NotNull @Nonnull
	private LastenausgleichTagesschuleAngabenInstitutionStatus status = LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN;

	@NotNull @Nonnull
	private JaxInstitution institution;

	@Nullable
	private JaxLastenausgleichTagesschuleAngabenInstitution angabenDeklaration;

	@Nullable
	private JaxLastenausgleichTagesschuleAngabenInstitution angabenKorrektur;


	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull LastenausgleichTagesschuleAngabenInstitutionStatus status) {
		this.status = status;
	}

	@Nonnull
	public JaxInstitution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull JaxInstitution institution) {
		this.institution = institution;
	}

	@Nullable
	public JaxLastenausgleichTagesschuleAngabenInstitution getAngabenDeklaration() {
		return angabenDeklaration;
	}

	public void setAngabenDeklaration(@Nullable JaxLastenausgleichTagesschuleAngabenInstitution angabenDeklaration) {
		this.angabenDeklaration = angabenDeklaration;
	}

	@Nullable
	public JaxLastenausgleichTagesschuleAngabenInstitution getAngabenKorrektur() {
		return angabenKorrektur;
	}

	public void setAngabenKorrektur(@Nullable JaxLastenausgleichTagesschuleAngabenInstitution angabenKorrektur) {
		this.angabenKorrektur = angabenKorrektur;
	}
}
