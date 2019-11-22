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

package ch.dvbern.ebegu.api.dtos;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;

/**
 * DTO fuer Daten der Belegungen.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBelegungTagesschuleModul extends JaxAbstractDTO {

	private static final long serialVersionUID = -841132022111944954L;

	@NotNull @Nonnull
	private BelegungTagesschuleModulIntervall intervall;

	@NotNull @Nonnull
	private JaxModulTagesschule modulTagesschule;

	@Nonnull
	public BelegungTagesschuleModulIntervall getIntervall() {
		return intervall;
	}

	public void setIntervall(@Nonnull BelegungTagesschuleModulIntervall intervall) {
		this.intervall = intervall;
	}

	@Nonnull
	public JaxModulTagesschule getModulTagesschule() {
		return modulTagesschule;
	}

	public void setModulTagesschule(@Nonnull JaxModulTagesschule modulTagesschule) {
		this.modulTagesschule = modulTagesschule;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JaxBelegungTagesschuleModul)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		JaxBelegungTagesschuleModul that = (JaxBelegungTagesschuleModul) o;
		return this.getIntervall() == that.getIntervall()
			&& Objects.equals(this.getModulTagesschule(), that.getModulTagesschule());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getIntervall(), getModulTagesschule());
	}
}
