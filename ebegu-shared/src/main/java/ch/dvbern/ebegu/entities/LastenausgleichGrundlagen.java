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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern des jeweiligen (vom Kanton bestätigten) Kosten pro 100% Platz eines Jahres
 */
@Audited
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "jahr", name = "UK_LastenausgleichGrundlagen_jahr"))
public class LastenausgleichGrundlagen extends AbstractEntity {

	private static final long serialVersionUID = -3717533583464831412L;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Integer jahr = 0;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Min(0)
	private BigDecimal selbstbehaltPro100ProzentPlatz = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Min(0)
	private BigDecimal kostenPro100ProzentPlatz = BigDecimal.ZERO;


	public LastenausgleichGrundlagen() {
	}


	@Nonnull
	public Integer getJahr() {
		return jahr;
	}

	public void setJahr(@Nonnull Integer jahr) {
		this.jahr = jahr;
	}

	@Nonnull
	public BigDecimal getSelbstbehaltPro100ProzentPlatz() {
		return selbstbehaltPro100ProzentPlatz;
	}

	public void setSelbstbehaltPro100ProzentPlatz(@Nonnull BigDecimal selbstbehaltPro100ProzentPlatz) {
		this.selbstbehaltPro100ProzentPlatz = selbstbehaltPro100ProzentPlatz;
	}

	@Nonnull
	public BigDecimal getKostenPro100ProzentPlatz() {
		return kostenPro100ProzentPlatz;
	}

	public void setKostenPro100ProzentPlatz(@Nonnull BigDecimal kostenPro100ProzentPlatz) {
		this.kostenPro100ProzentPlatz = kostenPro100ProzentPlatz;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(@Nullable AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final LastenausgleichGrundlagen otherGrundlagen = (LastenausgleichGrundlagen) other;
		return Objects.equals(getJahr(), otherGrundlagen.getJahr()) &&
			MathUtil.isSame(this.getKostenPro100ProzentPlatz(), otherGrundlagen.getKostenPro100ProzentPlatz());
	}
}
