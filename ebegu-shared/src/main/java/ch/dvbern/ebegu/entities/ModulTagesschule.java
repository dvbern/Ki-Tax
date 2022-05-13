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
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final ModulTagesschule otherModulTagesschule = (ModulTagesschule) other;
		return getModulTagesschuleGroup().isSame(otherModulTagesschule.getModulTagesschuleGroup()) &&
			getWochentag() == otherModulTagesschule.getWochentag();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModulTagesschule)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		ModulTagesschule that = (ModulTagesschule) o;
		return Objects.equals(this.getModulTagesschuleGroup(), that.getModulTagesschuleGroup()) &&
			this.getWochentag() == that.getWochentag();
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getModulTagesschuleGroup(), getWochentag());
	}

	@Override
	public int compareTo(@Nonnull ModulTagesschule o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getModulTagesschuleGroup(), o.getModulTagesschuleGroup());
		builder.append(this.getWochentag(), o.getWochentag());
		return builder.toComparison();
	}

	public ModulTagesschule copyForGesuchsperiode() {
		ModulTagesschule copy = new ModulTagesschule();
		copy.setWochentag(this.getWochentag());
		return copy;
	}
}
