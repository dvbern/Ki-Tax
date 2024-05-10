/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Abstrakte Entitaet. Muss von Entitaeten erweitert werden, die ein Pensum (Prozent) als BigDecimal,
 * ein DateRange und ein PensumUnits beeinhalten.
 */
@MappedSuperclass
public abstract class AbstractDecimalPensum extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -7136083144964149528L;

	@Min(0)
	@NotNull
	@Nonnull
	@Column(nullable = false, columnDefinition = "DECIMAL(19,10)")
	private BigDecimal pensum = BigDecimal.ZERO;

	/**
	 * This parameter is used in the client to know in which units the amount must be displayed.
	 * In the database the amount will always be % so it must be task of the client to translate the value
	 * in the DB into the value needed by the user.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	@Nonnull
	private PensumUnits unitForDisplay = PensumUnits.PERCENTAGE;

	@NotNull
	@Column(nullable = false)
	private BigDecimal monatlicheBetreuungskosten = BigDecimal.ZERO;

	@Nullable
	@Column(nullable = true)
	private BigDecimal stuendlicheVollkosten;

	@Nullable
	@Column(nullable = true)
	@DecimalMin("0")
	private BigDecimal betreuteTage;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "eingewoehnungPauschale"), nullable = true)
	private EingewoehnungPauschale eingewoehnungPauschale;

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final AbstractDecimalPensum otherAbstDateRangedEntity = (AbstractDecimalPensum) other;
		return super.isSame(otherAbstDateRangedEntity)
			&& this.getPensum().compareTo(otherAbstDateRangedEntity.getPensum()) == 0
			&& this.getUnitForDisplay() == otherAbstDateRangedEntity.getUnitForDisplay()
			&& this.getMonatlicheBetreuungskosten().compareTo(otherAbstDateRangedEntity.getMonatlicheBetreuungskosten()) == 0
			&& MathUtil.isSame(this.getStuendlicheVollkosten(), otherAbstDateRangedEntity.getStuendlicheVollkosten())
			&& MathUtil.isSame(this.getBetreuteTage(), otherAbstDateRangedEntity.getBetreuteTage())
			&& EbeguUtil.isSame(this.getEingewoehnungPauschale(), otherAbstDateRangedEntity.getEingewoehnungPauschale());
	}

	public void copyAbstractBetreuungspensumEntity(
		@Nonnull AbstractDecimalPensum target,
		@Nonnull AntragCopyType copyType) {

		super.copyAbstractDateRangedEntity(target, copyType);
		target.setPensum(this.getPensum());
		target.setMonatlicheBetreuungskosten(this.getMonatlicheBetreuungskosten());
		target.setUnitForDisplay(this.getUnitForDisplay());
		target.setStuendlicheVollkosten(this.getStuendlicheVollkosten());
		target.setBetreuteTage(this.getBetreuteTage());
		if (this.getEingewoehnungPauschale() != null) {
			target.setEingewoehnungPauschale(this.getEingewoehnungPauschale()
				.copyEingewohnungEntity(new EingewoehnungPauschale(), copyType));
		}
	}

	public void applyPensumFromDays(@Nonnull BigDecimal days, @Nonnull BigDecimal maxTageProMonat) {
		pensum = MathUtil.EXACT.divide(MathUtil.HUNDRED.multiply(days), maxTageProMonat);
		unitForDisplay = PensumUnits.DAYS;
	}

	public void applyPensumFromHours(@Nonnull BigDecimal hours, @Nonnull BigDecimal maxStundenProMonat) {
		pensum = MathUtil.EXACT.divide(MathUtil.HUNDRED.multiply(hours), maxStundenProMonat);
		unitForDisplay = PensumUnits.HOURS;
	}

	public void applyPensumFromPercentage(@Nonnull BigDecimal percentage) {
		pensum = percentage;
		unitForDisplay = PensumUnits.PERCENTAGE;
	}

	@Nonnull
	public PensumUnits getUnitForDisplay() {
		return unitForDisplay;
	}

	public void setUnitForDisplay(@Nonnull PensumUnits unitForDisplay) {
		this.unitForDisplay = unitForDisplay;
	}

	@Nonnull
	public BigDecimal getPensum() {
		return pensum;
	}

	public void setPensum(@Nonnull BigDecimal pensum) {
		this.pensum = pensum;
	}

	@Nonnull
	public BigDecimal getMonatlicheBetreuungskosten() {
		return monatlicheBetreuungskosten;
	}

	public void setMonatlicheBetreuungskosten(@Nonnull BigDecimal monatlicheBetreuungskosten) {
		this.monatlicheBetreuungskosten = monatlicheBetreuungskosten;
	}

	/**
	 * In der Datenbank wird das Pensum mit 10 Nachkomastellen gespeichert,
	 * dem Benutzer soll es auf 2 Stellen gerunden angezeigt werden
	 */
	@Nonnull
	public BigDecimal getPensumRounded(){
		return MathUtil.DEFAULT.from(pensum);
	}

	@Nullable
	public BigDecimal getStuendlicheVollkosten() {
		return stuendlicheVollkosten;
	}

	public void setStuendlicheVollkosten(@Nullable BigDecimal tfoStuendlichVollkosten) {
		this.stuendlicheVollkosten = tfoStuendlichVollkosten;
	}

	@Nullable
	public EingewoehnungPauschale getEingewoehnungPauschale() {
		return eingewoehnungPauschale;
	}

	public void setEingewoehnungPauschale(@Nullable EingewoehnungPauschale eingewoehnungPauschale) {
		this.eingewoehnungPauschale = eingewoehnungPauschale;
	}

	@Nullable
	public BigDecimal getBetreuteTage() {
		return betreuteTage;
	}

	public void setBetreuteTage(@Nullable BigDecimal betreuteTage) {
		this.betreuteTage = betreuteTage;
	}
}
