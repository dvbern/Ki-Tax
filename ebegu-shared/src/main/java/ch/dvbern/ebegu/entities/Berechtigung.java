/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.listener.BerechtigungChangedEntityListener;
import ch.dvbern.ebegu.validators.CheckBerechtigungGemeinde;
import ch.dvbern.ebegu.validators.CheckBerechtigungInstitutionTraegerschaft;
import ch.dvbern.ebegu.validators.CheckBerechtigungSozialdienst;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Entity
@EntityListeners(BerechtigungChangedEntityListener.class)
@Audited
@CheckBerechtigungInstitutionTraegerschaft
@CheckBerechtigungGemeinde
@CheckBerechtigungSozialdienst
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Berechtigung extends AbstractDateRangedEntity implements Comparable<Berechtigung> {

	private static final long serialVersionUID = 6372688971894279665L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Berechtigung_benutzer_id"))
	private Benutzer benutzer = null;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private UserRole role = null;

	@NotNull
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "berechtigung_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "gemeinde_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_berechtigung_gemeinde_gemeinde_id"),
		inverseForeignKey = @ForeignKey(name = "FK_berechtigung_gemeinde_berechtigung_id"),
		indexes = {
			@Index(name = "IX_berechtigung_gemeinde_berechtigung_id", columnList = "berechtigung_id"),
			@Index(name = "IX_berechtigung_gemeinde_gemeinde_id", columnList = "gemeinde_id"),
		}
	)
	private Set<Gemeinde> gemeindeList = new TreeSet<>();

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Berechtigung_institution_id"))
	private Institution institution = null;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Berechtigung_traegerschaft_id"))
	private Traegerschaft traegerschaft = null;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_berechtigung_sozialdienst_id"))
	private Sozialdienst sozialdienst = null;

	public Benutzer getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(Benutzer benutzer) {
		this.benutzer = benutzer;
	}

	@Nonnull
	public UserRole getRole() {
		return role;
	}

	public void setRole(@Nonnull UserRole role) {
		this.role = role;
	}

	@Nonnull
	public Set<Gemeinde> getGemeindeList() {
		return gemeindeList;
	}

	public void setGemeindeList(@Nonnull Set<Gemeinde> gemeindeList) {
		this.gemeindeList = gemeindeList;
	}

	@Nullable
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nullable Institution institution) {
		this.institution = institution;
	}

	@Nullable
	public Traegerschaft getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable Traegerschaft traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.appendSuper(super.toString())
			.append("gueltigkeit", getGueltigkeit())
			.append("role", role)
			.toString();
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
		if (!(other instanceof Berechtigung)) {
			return false;
		}
		final Berechtigung otherBerechtigung = (Berechtigung) other;
		return Objects.equals(getBenutzer(), otherBerechtigung.getBenutzer())
			&& getRole() == otherBerechtigung.getRole()
			&& Objects.equals(getInstitution(), otherBerechtigung.getInstitution())
			&& Objects.equals(getTraegerschaft(), otherBerechtigung.getTraegerschaft())
			&& Objects.equals(getGueltigkeit(), otherBerechtigung.getGueltigkeit())
			&& Objects.equals(getSozialdienst(), otherBerechtigung.getSozialdienst());
	}

	@Override
	public int compareTo(@Nonnull Berechtigung o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getGueltigkeit().getGueltigAb(), o.getGueltigkeit().getGueltigAb());
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	public boolean isGueltig() {
		return getGueltigkeit().contains(LocalDate.now());
	}

	public boolean isAbgelaufen() {
		return getGueltigkeit().endsBefore(LocalDate.now());
	}

	@Nonnull
	public String extractGemeindenForBerechtigungAsString() {
		return getGemeindeList()
			.stream()
			.map(Gemeinde::getName)
			.sorted(String::compareToIgnoreCase)
			.collect(Collectors.joining(", "));
	}

	@Nullable
	public Sozialdienst getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(@Nullable Sozialdienst sozialdienst) {
		this.sozialdienst = sozialdienst;
	}
}
