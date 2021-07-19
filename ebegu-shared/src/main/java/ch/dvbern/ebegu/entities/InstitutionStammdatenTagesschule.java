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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von InstitutionStammdatenTagesschule in der Datenbank.
 */
@Audited
@Entity
public class InstitutionStammdatenTagesschule extends AbstractEntity implements Comparable<InstitutionStammdatenTagesschule> {

	private static final long serialVersionUID = 3991623541799163623L;

	@Nonnull
	@Valid
	@SortNatural
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "institutionStammdatenTagesschule")
	private Set<EinstellungenTagesschule> einstellungenTagesschule = new TreeSet<>();

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_ts_gemeinde_id"))
	private Gemeinde gemeinde;


	public InstitutionStammdatenTagesschule() {
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
		if (!(other instanceof InstitutionStammdatenTagesschule)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(InstitutionStammdatenTagesschule o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	@Nonnull
	public Set<EinstellungenTagesschule> getEinstellungenTagesschule() {
		return einstellungenTagesschule;
	}

	public void setEinstellungenTagesschule(@Nonnull Set<EinstellungenTagesschule> einstellungenTagesschule) {
		this.einstellungenTagesschule = einstellungenTagesschule;
	}

	@Nonnull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public List<ModulTagesschuleGroup> extractAllModulTagesschuleGroup() {
		final List<ModulTagesschuleGroup> list = new ArrayList<>();
		for (final EinstellungenTagesschule einstellungenTagesschule : getEinstellungenTagesschule()) {
			Set<ModulTagesschuleGroup> modulTagesschuleGroups = einstellungenTagesschule.getModulTagesschuleGroups();
			if (CollectionUtils.isNotEmpty(modulTagesschuleGroups)) {
				list.addAll(modulTagesschuleGroups);
			}
		}
		return list;
	}
}
