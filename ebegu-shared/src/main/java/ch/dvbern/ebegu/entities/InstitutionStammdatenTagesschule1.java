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

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AssociationOverride;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;

/**
 * Stammdaten für Tagesschulen
 */
@Entity
@Audited
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "adresse_id", name = "UK_institution_stammdaten_ts_adresse_id"),
		@UniqueConstraint(columnNames = "tagesschule_id", name= "UK_institution_stammdaten_ts_tagesschule_id")
	},
	indexes = {
		@Index(name = "IX_institution_stammdaten_ts_gueltig_ab", columnList = "gueltigAb"),
		@Index(name = "IX_institution_stammdaten_ts_gueltig_bis", columnList = "gueltigBis")
	}
)
// Der ForeignKey-Name wird leider nicht richtig generiert, muss von Hand angepasst werden!
@AssociationOverride(name="adresse", joinColumns=@JoinColumn(name="adresse_id"), foreignKey = @ForeignKey(name = "FK_institution_stammdaten_tagesschule_adresse_id"))
// TODO (KIBON-616): Umbenennen
public class InstitutionStammdatenTagesschule1 extends AbstractInstitutionStammdaten {

	private static final long serialVersionUID = -9037857320548372570L;

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_tagesschule_id"), nullable = false)
	private Tagesschule tagesschule;

	@Nullable
	@Valid
	@SortNatural
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "institutionStammdatenTagesschule")
	private Set<ModulTagesschule> moduleTagesschule = new TreeSet<>();


	public InstitutionStammdatenTagesschule1() {
	}


	@NotNull
	public Tagesschule getTagesschule() {
		return tagesschule;
	}

	public void setTagesschule(@NotNull Tagesschule tagesschule) {
		this.tagesschule = tagesschule;
	}

	@Nullable
	public Set<ModulTagesschule> getModuleTagesschule() {
		return moduleTagesschule;
	}

	public void setModuleTagesschule(@Nullable Set<ModulTagesschule> moduleTagesschule) {
		this.moduleTagesschule = moduleTagesschule;
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
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof InstitutionStammdatenTagesschule1)) {
			return false;
		}
		final InstitutionStammdatenTagesschule1 otherInstStammdaten = (InstitutionStammdatenTagesschule1) other;
		return EbeguUtil.isSameObject(getTagesschule(), otherInstStammdaten.getTagesschule());
	}

	@Nonnull
	@Override
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return BetreuungsangebotTyp.TAGESSCHULE;
	}
}
