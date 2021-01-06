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
	private BigDecimal totalBelegungenMitSelbstbehalt = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal totalAnrechenbar = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal totalBetragGutscheineMitSelbstbehalt = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal selbstbehaltGemeinde = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal betragLastenausgleich = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal totalBelegungenOhneSelbstbehalt = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal totalBetragGutscheineOhneSelbstbehalt = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal kostenFuerSelbstbehalt = BigDecimal.ZERO;

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
	public BigDecimal getTotalAnrechenbar() {
		return totalAnrechenbar;
	}

	public void setTotalAnrechenbar(@Nonnull BigDecimal totalAnrechenbar) {
		this.totalAnrechenbar = totalAnrechenbar;
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

	@Nonnull
	public BigDecimal getTotalBelegungenMitSelbstbehalt() {
		return totalBelegungenMitSelbstbehalt;
	}

	public void setTotalBelegungenMitSelbstbehalt(@Nonnull BigDecimal totalBelegungenMitSelbstbehalt) {
		this.totalBelegungenMitSelbstbehalt = totalBelegungenMitSelbstbehalt;
	}

	@Nonnull
	public BigDecimal getTotalBetragGutscheineMitSelbstbehalt() {
		return totalBetragGutscheineMitSelbstbehalt;
	}

	public void setTotalBetragGutscheineMitSelbstbehalt(@Nonnull BigDecimal totalBetragGutscheineMitSelbstbehalt) {
		this.totalBetragGutscheineMitSelbstbehalt = totalBetragGutscheineMitSelbstbehalt;
	}

	@Nonnull
	public BigDecimal getTotalBelegungenOhneSelbstbehalt() {
		return totalBelegungenOhneSelbstbehalt;
	}

	public void setTotalBelegungenOhneSelbstbehalt(@Nonnull BigDecimal totalBelegungenOhneSelbstbehalt) {
		this.totalBelegungenOhneSelbstbehalt = totalBelegungenOhneSelbstbehalt;
	}

	@Nonnull
	public BigDecimal getTotalBetragGutscheineOhneSelbstbehalt() {
		return totalBetragGutscheineOhneSelbstbehalt;
	}

	public void setTotalBetragGutscheineOhneSelbstbehalt(@Nonnull BigDecimal totalBetragGutscheineOhneSelbstbehalt) {
		this.totalBetragGutscheineOhneSelbstbehalt = totalBetragGutscheineOhneSelbstbehalt;
	}

	@Nonnull
	public BigDecimal getKostenFuerSelbstbehalt() {
		return kostenFuerSelbstbehalt;
	}

	public void setKostenFuerSelbstbehalt(@Nonnull BigDecimal kostenFuerSelbstbehalt) {
		this.kostenFuerSelbstbehalt = kostenFuerSelbstbehalt;
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
		sb.append(", totalBelegungenMitSelbstbehalt=").append(totalBelegungenMitSelbstbehalt);
		sb.append(", totalBetragGutscheineMitSelbstbehalt=").append(totalBetragGutscheineMitSelbstbehalt);
		sb.append(", selbstbehaltGemeinde=").append(selbstbehaltGemeinde);
		sb.append(", betragLastenausgleich=").append(betragLastenausgleich);
		sb.append(", korrektur=").append(korrektur);
		sb.append(", totalBelegungenOhneSelbstbehalt=").append(totalBelegungenOhneSelbstbehalt);
		sb.append(", totalBetragGutscheineOhneSelbstbehalt=").append(totalBetragGutscheineOhneSelbstbehalt);
		sb.append('}');
		return sb.toString();
	}

	public void add(@Nonnull LastenausgleichDetail other) {
		this.setGemeinde(other.getGemeinde());
		this.setJahr(other.getJahr());
		this.setLastenausgleich(other.getLastenausgleich());
		this.setTotalBelegungenMitSelbstbehalt(MathUtil.DEFAULT.addNullSafe(this.getTotalBelegungenMitSelbstbehalt(), other.getTotalBelegungenMitSelbstbehalt()));
		this.setTotalBetragGutscheineMitSelbstbehalt(MathUtil.DEFAULT.addNullSafe(this.getTotalBetragGutscheineMitSelbstbehalt(), other.getTotalBetragGutscheineMitSelbstbehalt()));
		this.setSelbstbehaltGemeinde(MathUtil.DEFAULT.addNullSafe(this.getSelbstbehaltGemeinde(), other.getSelbstbehaltGemeinde()));
		this.setBetragLastenausgleich(MathUtil.DEFAULT.addNullSafe(this.getBetragLastenausgleich(), other.getBetragLastenausgleich()));
		this.setTotalBelegungenOhneSelbstbehalt(MathUtil.DEFAULT.addNullSafe(this.getTotalBelegungenOhneSelbstbehalt(), other.getTotalBelegungenOhneSelbstbehalt()));
		this.setTotalBetragGutscheineOhneSelbstbehalt(MathUtil.DEFAULT.addNullSafe(this.getTotalBetragGutscheineOhneSelbstbehalt(), other.getTotalBetragGutscheineOhneSelbstbehalt()));
		this.setKostenFuerSelbstbehalt(MathUtil.DEFAULT.addNullSafe(this.getKostenFuerSelbstbehalt(), other.getKostenFuerSelbstbehalt()));
	}

	/**
	 * Prüft, ob sich der Lastenausgleich verändert hat. Dazu wird für die "Belegungen mit Selbstbehalt für Gemeinde"
	 * auf den BetragLastenausgleich geschaut und für die "Belegungen ohne Selbstbehalt für Gemeinde" auf den
	 * totalBetragGutscheineOhneSelbstbehalt
	 */
	public boolean hasChanged(@Nonnull LastenausgleichDetail detail) {
		return this.getBetragLastenausgleich().compareTo(detail.getBetragLastenausgleich()) != 0
			|| this.getTotalBetragGutscheineOhneSelbstbehalt().compareTo(detail.getTotalBetragGutscheineOhneSelbstbehalt()) != 0;
	}
}
