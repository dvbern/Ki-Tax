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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.MathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal verpflegungskostenVerguenstigt = BigDecimal.ZERO;


	public TSCalculationResult() {
	}

	public TSCalculationResult(@Nonnull TSCalculationResult toCopy) {
		this.betreuungszeitProWoche = toCopy.betreuungszeitProWoche;
		this.verpflegungskosten = toCopy.verpflegungskosten;
		this.gebuehrProStunde = toCopy.gebuehrProStunde;
		this.totalKostenProWoche = toCopy.totalKostenProWoche;
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
	public BigDecimal getVerpflegungskostenVerguenstigt() {
		return verpflegungskostenVerguenstigt;
	}

	public void setVerpflegungskostenVerguenstigt(@Nonnull BigDecimal verpflegungskostenVerguenstigt) {
		this.verpflegungskostenVerguenstigt = verpflegungskostenVerguenstigt;
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
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TSCalculationResult)) {
			return false;
		}

		TSCalculationResult that = (TSCalculationResult) other;
		return Objects.equals(betreuungszeitProWoche, that.betreuungszeitProWoche) &&
			MathUtil.isSame(verpflegungskosten, that.verpflegungskosten) &&
			MathUtil.isSame(gebuehrProStunde, that.gebuehrProStunde) &&
			MathUtil.isSame(totalKostenProWoche, that.totalKostenProWoche);
	}

	/**
	 * Im unterschied zu is same wird hier super equals nicht aufgerufen.
	 * @param thisEntity
	 * @param otherEntity
	 * @return
	 */
	public static boolean isSameSichtbareDaten(@Nullable TSCalculationResult thisEntity, @Nullable TSCalculationResult otherEntity) {
		return (thisEntity == null && otherEntity == null)
			|| (thisEntity != null && otherEntity != null && (
				Objects.equals(thisEntity.betreuungszeitProWoche, otherEntity.betreuungszeitProWoche) &&
				MathUtil.isSame(thisEntity.verpflegungskosten, otherEntity.verpflegungskosten) &&
				MathUtil.isSame(thisEntity.gebuehrProStunde, otherEntity.gebuehrProStunde) &&
				MathUtil.isSame(thisEntity.totalKostenProWoche, otherEntity.totalKostenProWoche)
		));
	}

	@Nonnull
	public String getBetreuungszeitProWocheFormatted() {
		long hours = betreuungszeitProWoche.longValue() / 60;   // integer division gibt stunden
		long minutes = betreuungszeitProWoche.longValue() % 60; // rest minuten
		return StringUtils.leftPad(Long.toString(hours), 2, '0')
			+ ':' + StringUtils.leftPad(Long.toString(minutes), 2, '0');
	}

	public void add(@Nonnull TSCalculationResult other) {
		betreuungszeitProWoche = this.betreuungszeitProWoche + other.betreuungszeitProWoche;
		verpflegungskosten = MathUtil.DEFAULT.addNullSafe(this.verpflegungskosten, other.verpflegungskosten);
	}
}
