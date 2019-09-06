/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.time.DayOfWeek;

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
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Entity for the Module of the Tageschulangebote.
 */
@Audited
@Entity
public class ModulTagesschule extends AbstractEntity implements Comparable<ModulTagesschule> {

	private static final long serialVersionUID = -8403411439182708718L;


	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_modul_tagesschule_modul_tagesschule_group_id"), nullable = false)
	private ModulTagesschuleGroup modulTagesschuleGroup;

	@Enumerated(value = EnumType.STRING)
	@NotNull @Nonnull
	@Column(nullable = false)
	private DayOfWeek wochentag;


	public ModulTagesschuleGroup getModulTagesschuleGroup() {
		return modulTagesschuleGroup;
	}

	public void setModulTagesschuleGroup(ModulTagesschuleGroup modulTagesschuleGroup) {
		this.modulTagesschuleGroup = modulTagesschuleGroup;
	}

	@Nonnull
	public DayOfWeek getWochentag() {
		return wochentag;
	}

	public void setWochentag(@Nonnull DayOfWeek wochentag) {
		this.wochentag = wochentag;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof ModulTagesschule)) {
			return false;
		}

		final ModulTagesschule otherModulTagesschule = (ModulTagesschule) other;
		return getModulTagesschuleGroup().isSame(otherModulTagesschule.getModulTagesschuleGroup()) &&
			getWochentag() == otherModulTagesschule.getWochentag();
	}

	@Override
	public int compareTo(@Nonnull ModulTagesschule o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getModulTagesschuleGroup(), o.getModulTagesschuleGroup());
		builder.append(this.getWochentag(), o.getWochentag());
		return builder.toComparison();
	}

	@Nonnull
	public ModulTagesschule copyModulTagesschule(@Nonnull ModulTagesschule target, @Nonnull AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
			target.setWochentag(getWochentag());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	public ModulTagesschule copyForGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		ModulTagesschule copy = new ModulTagesschule();
		copy.setModulTagesschuleGroup(this.getModulTagesschuleGroup().copyForGesuchsperiode(gesuchsperiode));
		copy.setWochentag(this.getWochentag());
		return copy;
	}
}
