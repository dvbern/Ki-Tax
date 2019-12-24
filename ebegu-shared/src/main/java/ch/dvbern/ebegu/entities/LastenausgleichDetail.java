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
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern eines Details eines Lastenausgleichs: Die Details zu einer Gemeinde in einem Jahr.
 * In einem weiteren Lastenausgleich kann für dieselbe Gemeinde und dasselbe Jahr jedoch ein weiterer Eintrag
 * vorhanden sein.
 */
@Audited
@Entity
public class LastenausgleichDetail extends AbstractEntity implements Comparable<LastenausgleichDetail> {

	private static final long serialVersionUID = 5266890248557491091L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Lastenausgleich_detail_lastenausgleich_id"), nullable = false)
	private Lastenausgleich lastenausgleich;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Integer jahr = 0;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lastenausgleich_detail_gemeinde_id"))
	private Gemeinde gemeinde;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal totalBelegungen = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal totalBetragGutscheine = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal selbstbehaltGemeinde = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal betragLastenausgleich = BigDecimal.ZERO;

	@Column(nullable = false)
	private boolean korrektur = false;


	public LastenausgleichDetail() {
	}

	@Nonnull
	public Lastenausgleich getLastenausgleich() {
		return lastenausgleich;
	}

	public void setLastenausgleich(@Nonnull Lastenausgleich lastenausgleich) {
		this.lastenausgleich = lastenausgleich;
	}

	@Nonnull
	public Integer getJahr() {
		return jahr;
	}

	public void setJahr(@Nonnull Integer jahr) {
		this.jahr = jahr;
	}

	@Nonnull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public BigDecimal getTotalBelegungen() {
		return totalBelegungen;
	}

	public void setTotalBelegungen(@Nonnull BigDecimal totalBelegungen) {
		this.totalBelegungen = totalBelegungen;
	}

	@Nonnull
	public BigDecimal getTotalBetragGutscheine() {
		return totalBetragGutscheine;
	}

	public void setTotalBetragGutscheine(@Nonnull BigDecimal totalBetragGutscheine) {
		this.totalBetragGutscheine = totalBetragGutscheine;
	}

	@Nonnull
	public BigDecimal getSelbstbehaltGemeinde() {
		return selbstbehaltGemeinde;
	}

	public void setSelbstbehaltGemeinde(@Nonnull BigDecimal selbstbehaltGemeinde) {
		this.selbstbehaltGemeinde = selbstbehaltGemeinde;
	}

	@Nonnull
	public BigDecimal getBetragLastenausgleich() {
		return betragLastenausgleich;
	}

	public void setBetragLastenausgleich(@Nonnull BigDecimal betragLastenausgleich) {
		this.betragLastenausgleich = betragLastenausgleich;
	}

	public boolean isKorrektur() {
		return korrektur;
	}

	public void setKorrektur(boolean korrektur) {
		this.korrektur = korrektur;
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
		if (!(other instanceof LastenausgleichDetail)) {
			return false;
		}
		final LastenausgleichDetail otherDetail = (LastenausgleichDetail) other;
		return Objects.equals(getJahr(), otherDetail.getJahr()) &&
			Objects.equals(getGemeinde(), otherDetail.getGemeinde());
	}

	@Override
	public int compareTo(@Nonnull  LastenausgleichDetail other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getJahr(), other.getJahr());
		compareToBuilder.append(this.getGemeinde(), other.getGemeinde());
		compareToBuilder.append(this.getLastenausgleich(), other.getLastenausgleich());
		return compareToBuilder.toComparison();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("LastenausgleichDetail{");
		sb.append("lastenausgleich=").append(lastenausgleich.getJahr());
		sb.append(", jahr=").append(jahr);
		sb.append(", gemeinde=").append(gemeinde.getName());
		sb.append(", totalBelegungen=").append(totalBelegungen);
		sb.append(", totalBetragGutscheine=").append(totalBetragGutscheine);
		sb.append(", selbstbehaltGemeinde=").append(selbstbehaltGemeinde);
		sb.append(", betragLastenausgleich=").append(betragLastenausgleich);
		sb.append(", korrektur=").append(korrektur);
		sb.append('}');
		return sb.toString();
	}

	public void add(@Nonnull LastenausgleichDetail other) {
		this.setGemeinde(other.getGemeinde());
		this.setJahr(other.getJahr());
		this.setLastenausgleich(other.getLastenausgleich());
		this.setTotalBelegungen(MathUtil.DEFAULT.addNullSafe(this.getTotalBelegungen(), other.getTotalBelegungen()));
		this.setTotalBetragGutscheine(MathUtil.DEFAULT.addNullSafe(this.getTotalBetragGutscheine(), other.getTotalBetragGutscheine()));
		this.setSelbstbehaltGemeinde(MathUtil.DEFAULT.addNullSafe(this.getSelbstbehaltGemeinde(), other.getSelbstbehaltGemeinde()));
		this.setBetragLastenausgleich(MathUtil.DEFAULT.addNullSafe(this.getBetragLastenausgleich(), other.getBetragLastenausgleich()));
	}

	public boolean hasChanged(@Nonnull LastenausgleichDetail detail) {
		return this.getBetragLastenausgleich().compareTo(detail.getBetragLastenausgleich()) != 0;
	}
}
