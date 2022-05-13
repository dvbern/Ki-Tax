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
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.ModulTagesschuleTyp;
import ch.dvbern.ebegu.util.Constants;
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

	private static final long serialVersionUID = -3095520370997020676L;

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

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String erlaeuterung;

	@NotNull
	@Column(nullable = false)
	private boolean tagi = false;

	public EinstellungenTagesschule() {
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
		return true;
	}

	@Override
	public int compareTo(EinstellungenTagesschule o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	@Nonnull
	public InstitutionStammdatenTagesschule getInstitutionStammdatenTagesschule() {
		return institutionStammdatenTagesschule;
	}

	public void setInstitutionStammdatenTagesschule(@Nonnull InstitutionStammdatenTagesschule institutionStammdatenTagesschule) {
		this.institutionStammdatenTagesschule = institutionStammdatenTagesschule;
	}

	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nonnull
	public Set<ModulTagesschuleGroup> getModulTagesschuleGroups() {
		return modulTagesschuleGroups;
	}

	public void setModulTagesschuleGroups(@Nonnull Set<ModulTagesschuleGroup> modulTagesschuleGroups) {
		this.modulTagesschuleGroups = modulTagesschuleGroups;
	}

	@Nonnull
	public ModulTagesschuleTyp getModulTagesschuleTyp() {
		return modulTagesschuleTyp;
	}

	public void setModulTagesschuleTyp(@Nonnull ModulTagesschuleTyp modulTagesschuleTyp) {
		this.modulTagesschuleTyp = modulTagesschuleTyp;
	}

	@Nullable
	public String getErlaeuterung() {
		return erlaeuterung;
	}

	public void setErlaeuterung(@Nullable String erlaeuterung) {
		this.erlaeuterung = erlaeuterung;
	}

	public boolean isTagi() {
		return tagi;
	}

	public void setTagi(boolean tagis) {
		this.tagi = tagis;
	}

	@Nonnull
	public EinstellungenTagesschule copyForGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		EinstellungenTagesschule copy = new EinstellungenTagesschule();
		copy.setInstitutionStammdatenTagesschule(this.getInstitutionStammdatenTagesschule());
		copy.setGesuchsperiode(gesuchsperiode);
		copy.setErlaeuterung(this.getErlaeuterung());
		copy.setTagi(this.isTagi());
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
