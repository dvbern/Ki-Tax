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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern einer Durchführung des Lastenausgleichs. Der Lastenausgleich wird einmal pro Jahr
 * durchgeführt. Im Total können jedoch Beträge verschiedener Jahre vorhanden sein (Korrekturen)
 */
@Audited
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "jahr", name = "UK_Lastenausgleich_jahr"))
public class Lastenausgleich extends AbstractEntity {

	private static final long serialVersionUID = -5083436194821575595L;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Integer jahr = 0;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Min(0)
	private BigDecimal totalAlleGemeinden = BigDecimal.ZERO;

	@Nonnull
	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "lastenausgleich")
	private Set<LastenausgleichDetail> lastenausgleichDetails = new TreeSet<>();


	public Lastenausgleich() {
	}


	@Nonnull
	public Integer getJahr() {
		return jahr;
	}

	public void setJahr(@Nonnull Integer jahr) {
		this.jahr = jahr;
	}

	@Nonnull
	public BigDecimal getTotalAlleGemeinden() {
		return totalAlleGemeinden;
	}

	public void setTotalAlleGemeinden(@Nonnull BigDecimal totalAlleGemeinden) {
		this.totalAlleGemeinden = totalAlleGemeinden;
	}

	@Nonnull
	public Set<LastenausgleichDetail> getLastenausgleichDetails() {
		return lastenausgleichDetails;
	}

	public void setLastenausgleichDetails(@Nonnull Set<LastenausgleichDetail> lastenausgleichDetails) {
		this.lastenausgleichDetails = lastenausgleichDetails;
	}

	@Override
	public boolean isSame(@Nullable AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof Lastenausgleich)) {
			return false;
		}
		final Lastenausgleich otherLastenausgleich = (Lastenausgleich) other;
		return Objects.equals(getJahr(), otherLastenausgleich.getJahr()) &&
			MathUtil.isSame(this.getTotalAlleGemeinden(), otherLastenausgleich.getTotalAlleGemeinden());
	}
}
