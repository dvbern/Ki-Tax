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

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class BelegungTagesschuleModul extends AbstractEntity implements Comparable<BelegungTagesschuleModul> {

	private static final long serialVersionUID = -2101736417147986784L;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	// es darf nicht cascadeAll sein, da sonst die Module geloescht werden, wenn die Belegung geloescht wird, obwohl das Modul eigentlich zur Institutione gehoert
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_belegung_ts_modul_modul_ts"), nullable = false)
	private ModulTagesschule modulTagesschule;

	@NotNull @Nonnull
	@Enumerated(value = EnumType.STRING)
	@Column(nullable = false)
	private BelegungTagesschuleModulIntervall intervall;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_belegung_ts_modul_belegung_ts"), nullable = false)
	private BelegungTagesschule belegungTagesschule;


	@Override
	public boolean isSame(AbstractEntity other) {
		return false;
	}

	public ModulTagesschule getModulTagesschule() {
		return modulTagesschule;
	}

	public void setModulTagesschule(ModulTagesschule modulTagesschule) {
		this.modulTagesschule = modulTagesschule;
	}

	public BelegungTagesschuleModulIntervall getIntervall() {
		return intervall;
	}

	public void setIntervall(BelegungTagesschuleModulIntervall intervall) {
		this.intervall = intervall;
	}

	public BelegungTagesschule getBelegungTagesschule() {
		return belegungTagesschule;
	}

	public void setBelegungTagesschule(BelegungTagesschule belegungTagesschule) {
		this.belegungTagesschule = belegungTagesschule;
	}

	@Nonnull
	public BelegungTagesschuleModul copyBelegungTagesschuleModul(@Nonnull BelegungTagesschuleModul target,
		@Nonnull AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
			target.setIntervall(this.getIntervall());
			target.setModulTagesschule(this.getModulTagesschule());

			break;
		case ERNEUERUNG:
		case ERNEUERUNG_AR_2023:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	public int compareTo(BelegungTagesschuleModul o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getModulTagesschule(), o.getModulTagesschule());
		builder.append(this.getIntervall(), o.getIntervall());
		return builder.toComparison();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BelegungTagesschuleModul)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		BelegungTagesschuleModul that = (BelegungTagesschuleModul) o;
		return Objects.equals(this.getModulTagesschule(), that.getModulTagesschule()) &&
			Objects.equals(this.getModulTagesschule().getModulTagesschuleGroup(), that.getModulTagesschule().getModulTagesschuleGroup()) &&
			this.getIntervall() == that.getIntervall();
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getModulTagesschule(), getIntervall());
	}
}
