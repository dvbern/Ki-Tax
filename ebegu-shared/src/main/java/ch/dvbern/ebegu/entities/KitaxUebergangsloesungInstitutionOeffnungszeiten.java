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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * Kapselung fuer die Oeffnungs-Parameter einer Kita
 */
@Entity
public final class KitaxUebergangsloesungInstitutionOeffnungszeiten extends AbstractEntity {

	private static final long serialVersionUID = 6906112225260019153L;

	@Transient
	private boolean dummyParams = false;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal oeffnungstage = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal oeffnungsstunden = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private String nameKitax;

	@NotNull @Nonnull
	@Column(nullable = false, unique = true)
	private String nameKibon;


	@Nonnull
	public BigDecimal getOeffnungstage() {
		return oeffnungstage;
	}

	public void setOeffnungstage(@Nonnull BigDecimal oeffnungstage) {
		this.oeffnungstage = oeffnungstage;
	}

	@Nonnull
	public BigDecimal getOeffnungsstunden() {
		return oeffnungsstunden;
	}

	public void setOeffnungsstunden(@Nonnull BigDecimal oeffnungsstunden) {
		this.oeffnungsstunden = oeffnungsstunden;
	}

	@Nonnull
	public String getNameKitax() {
		return nameKitax;
	}

	public void setNameKitax(@Nonnull String nameKitax) {
		this.nameKitax = nameKitax;
	}

	@Nonnull
	public String getNameKibon() {
		return nameKibon;
	}

	public void setNameKibon(@Nonnull String nameKibon) {
		this.nameKibon = nameKibon;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return false;
	}

	public boolean isDummyParams() {
		return dummyParams;
	}

	public void setDummyParams(boolean dummyParams) {
		this.dummyParams = dummyParams;
	}
}
