/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class TSCalculationResult extends AbstractEntity {

	private static final long serialVersionUID = 1351117672006189102L;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Integer betreuungszeitProWoche = 0;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal verpflegungskosten = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal gebuehrProStunde = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal totalKostenProWoche = BigDecimal.ZERO;

	@Valid
	@NotNull @Nonnull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_tsCalculationResult_bgCalculationResult"), nullable = false)
	private BGCalculationResult bgCalculationResult;


	public TSCalculationResult() {
	}

	public TSCalculationResult(@Nullable TSCalculationResult toCopy) {
		if (toCopy != null) {
			this.betreuungszeitProWoche = toCopy.betreuungszeitProWoche;
			this.verpflegungskosten = toCopy.verpflegungskosten;
			this.gebuehrProStunde = toCopy.gebuehrProStunde;
			this.bgCalculationResult = toCopy.bgCalculationResult;
		}
	}

	@Nonnull
	public Integer getBetreuungszeitProWoche() {
		return betreuungszeitProWoche;
	}

	public void setBetreuungszeitProWoche(@Nonnull Integer betreuungszeitProWoche) {
		this.betreuungszeitProWoche = betreuungszeitProWoche;
	}

	@Nonnull
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nonnull BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nonnull
	public BigDecimal getGebuehrProStunde() {
		return gebuehrProStunde;
	}

	public void setGebuehrProStunde(@Nonnull BigDecimal gebuehrProStunde) {
		this.gebuehrProStunde = gebuehrProStunde;
	}

	@Nonnull
	public BigDecimal getTotalKostenProWoche() {
		return totalKostenProWoche;
	}

	public void setTotalKostenProWoche(@Nonnull BigDecimal totalKostenProWoche) {
		this.totalKostenProWoche = totalKostenProWoche;
	}

	@Nonnull
	public BGCalculationResult getBgCalculationResult() {
		return bgCalculationResult;
	}

	public void setBgCalculationResult(@Nonnull BGCalculationResult bgCalculationResult) {
		this.bgCalculationResult = bgCalculationResult;
	}

	@Override
	public String toString() {
		String sb = "betreuungszeitProWoche=" + getBetreuungszeitProWocheFormatted()
			+ ", verpflegungskosten=" + verpflegungskosten
			+ ", gebuehrProStunde=" + gebuehrProStunde
			+ ", totalKostenProWoche=" + totalKostenProWoche;
		return sb;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TSCalculationResult)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		TSCalculationResult that = (TSCalculationResult) other;
		return Objects.equals(betreuungszeitProWoche, that.betreuungszeitProWoche) &&
			MathUtil.isSame(verpflegungskosten, that.verpflegungskosten) &&
			MathUtil.isSame(gebuehrProStunde, that.gebuehrProStunde)&&
			MathUtil.isSame(totalKostenProWoche, that.totalKostenProWoche);
	}

	@Nonnull
	public String getBetreuungszeitProWocheFormatted() {
		long hours = betreuungszeitProWoche.longValue() / 60;   // integer division gibt stunden
		long minutes = betreuungszeitProWoche.longValue() % 60; // rest minuten
		return StringUtils.leftPad(Long.toString(hours), 2, '0')
			+ ':' + StringUtils.leftPad(Long.toString(minutes), 2, '0');
	}

	public void add(@Nonnull TSCalculationResult other) {
		betreuungszeitProWoche = MathUtil.DEFAULT.addNullSafe(this.betreuungszeitProWoche, other.betreuungszeitProWoche);
		verpflegungskosten = MathUtil.DEFAULT.addNullSafe(this.verpflegungskosten, other.verpflegungskosten);
		gebuehrProStunde = MathUtil.DEFAULT.addNullSafe(this.gebuehrProStunde, other.gebuehrProStunde);
	}

	public static void add(@Nullable TSCalculationResult result, @Nullable TSCalculationResult other) {
		if (other == null) {
			return;
		}
		if (result == null) {
			result = new TSCalculationResult();
		}
		result.betreuungszeitProWoche = MathUtil.DEFAULT.addNullSafe(result.betreuungszeitProWoche, other.betreuungszeitProWoche);
		result.verpflegungskosten = MathUtil.DEFAULT.addNullSafe(result.verpflegungskosten, other.verpflegungskosten);
		result.gebuehrProStunde = MathUtil.DEFAULT.addNullSafe(result.gebuehrProStunde, other.gebuehrProStunde);
	}
}
