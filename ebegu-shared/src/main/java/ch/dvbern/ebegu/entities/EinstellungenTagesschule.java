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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.ModulTagesschuleTyp;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von EinstellungenTagesschule in der Datenbank.
 */
@Audited
@Entity
public class EinstellungenTagesschule extends AbstractEntity implements Comparable<EinstellungenTagesschule> {

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_einstellungen_ts_inst_stammdaten_tagesschule_id"), nullable = false)
	private InstitutionStammdatenTagesschule institutionStammdatenTagesschule;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "FK_einstellungen_ts_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	@Nonnull
	@Valid
	@SortNatural
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "einstellungenTagesschule")
	private Set<ModulTagesschuleGroup> modulTagesschuleGroups = new TreeSet<>();

	@Enumerated(value = EnumType.STRING)
	@NotNull @Nonnull
	@Column(nullable = false)
	private ModulTagesschuleTyp modulTagesschuleTyp = ModulTagesschuleTyp.DYNAMISCH;


	public EinstellungenTagesschule() {
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
		if (!(other instanceof EinstellungenTagesschule)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(EinstellungenTagesschule o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	public InstitutionStammdatenTagesschule getInstitutionStammdatenTagesschule() {
		return institutionStammdatenTagesschule;
	}

	public void setInstitutionStammdatenTagesschule(InstitutionStammdatenTagesschule institutionStammdatenTagesschule) {
		this.institutionStammdatenTagesschule = institutionStammdatenTagesschule;
	}

	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nonnull
	public Set<ModulTagesschuleGroup> getModulTagesschuleGroups() {
		return modulTagesschuleGroups;
	}

	public void setModulTagesschuleGroups(@Nonnull Set<ModulTagesschuleGroup> modulTagesschuleGroups) {
		this.modulTagesschuleGroups = modulTagesschuleGroups;
	}


	public ModulTagesschuleTyp getModulTagesschuleTyp() {
		return modulTagesschuleTyp;
	}

	public void setModulTagesschuleTyp(ModulTagesschuleTyp modulTagesschuleTyp) {
		this.modulTagesschuleTyp = modulTagesschuleTyp;
	}

	@Nonnull
	public EinstellungenTagesschule copyForGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		EinstellungenTagesschule copy = new EinstellungenTagesschule();
		copy.setInstitutionStammdatenTagesschule(this.getInstitutionStammdatenTagesschule());
		copy.setGesuchsperiode(gesuchsperiode);
		copy.setModulTagesschuleTyp(this.getModulTagesschuleTyp());
		if (CollectionUtils.isNotEmpty(this.getModulTagesschuleGroups())) {
			copy.setModulTagesschuleGroups(new TreeSet<>());
			this.getModulTagesschuleGroups().forEach(group -> {
				ModulTagesschuleGroup newGroup = group.copyForGesuchsperiode();
				copy.getModulTagesschuleGroups().add(newGroup);
				newGroup.setEinstellungenTagesschule(copy);
			});
		}
		return copy;
	}
}
