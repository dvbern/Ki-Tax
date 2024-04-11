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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.BetreuungspensumAbweichungStatus;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

import static java.util.Objects.requireNonNull;

@Audited
@Entity
public class BetreuungspensumAbweichung extends AbstractMahlzeitenPensum implements Comparable<BetreuungspensumAbweichung>  {

	private static final long serialVersionUID = -8308660793880620086L;

	@NotNull @Nonnull
	@Enumerated(EnumType.STRING)
	private BetreuungspensumAbweichungStatus status = BetreuungspensumAbweichungStatus.NONE;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuungspensum_abweichung_betreuung_id"), nullable = false)
	private Betreuung betreuung;

	@Transient
	@Nullable
	private BigDecimal multiplier = BigDecimal.ZERO;

	// every Zeitabschnitt containing any date of this.gueltigkeit
	// merged by its part of the current Gueltigkeit into one monthly (=this.gueltigkeit) Betreuungspensum.
	@Transient
	@Nullable
	private BigDecimal vertraglichesPensum = null;

	@Transient
	@Nullable
	private BigDecimal vertraglicheKosten = null;

	@Transient
	@Nullable
	private BigDecimal vertraglicheHauptmahlzeiten = null;

	@Transient
	@Nullable
	private BigDecimal vertraglicheNebenmahlzeiten = null;

	@Transient
	@Nullable
	private BigDecimal vertraglicherTarifHauptmahlzeit = BigDecimal.ZERO;

	@Transient
	@Nullable
	private BigDecimal vertraglicherTarifNebenmahlzeit = BigDecimal.ZERO;

	@Nonnull
	public BetreuungspensumAbweichungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull BetreuungspensumAbweichungStatus status) {
		this.status = status;
	}

	@Nullable
	public BigDecimal getVertraglichesPensum() {
		return vertraglichesPensum;
	}

	public void setVertraglichesPensum(@Nullable BigDecimal vertraglichesPensum) {
		this.vertraglichesPensum = vertraglichesPensum;
	}

	@Nullable
	public BigDecimal getVertraglicheKosten() {
		return vertraglicheKosten;
	}

	public void setVertraglicheKosten(@Nullable BigDecimal vertraglicheKosten) {
		this.vertraglicheKosten = vertraglicheKosten;
	}

	@Nullable
	public BigDecimal getVertraglicheHauptmahlzeiten() {
		return vertraglicheHauptmahlzeiten;
	}

	public void setVertraglicheHauptmahlzeiten(@Nullable BigDecimal vertraglicheHauptmahlzeiten) {
		this.vertraglicheHauptmahlzeiten = vertraglicheHauptmahlzeiten;
	}

	@Nullable
	public BigDecimal getVertraglicherTarifHauptmahlzeit() {
		return vertraglicherTarifHauptmahlzeit;
	}

	public void setVertraglicherTarifHauptmahlzeit(@Nullable BigDecimal vertraglicherTarifHauptmahlzeit) {
		this.vertraglicherTarifHauptmahlzeit = vertraglicherTarifHauptmahlzeit;
	}

	@Nullable
	public BigDecimal getVertraglicherTarifNebenmahlzeit() {
		return vertraglicherTarifNebenmahlzeit;
	}

	public void setVertraglicherTarifNebenmahlzeit(@Nullable BigDecimal vertraglicherTarifNebenmahlzeit) {
		this.vertraglicherTarifNebenmahlzeit = vertraglicherTarifNebenmahlzeit;
	}

	@Nullable
	public BigDecimal getVertraglicheNebenmahlzeiten() {
		return vertraglicheNebenmahlzeiten;
	}

	public void setVertraglicheNebenmahlzeiten(@Nullable BigDecimal vertraglicheNebenmahlzeiten) {
		this.vertraglicheNebenmahlzeiten = vertraglicheNebenmahlzeiten;
	}

	public void addPensum(BigDecimal pensum) {
		vertraglichesPensum = MathUtil.DEFAULT.addNullSafe(pensum, vertraglichesPensum);
	}

	public void addKosten(BigDecimal kosten) {
		vertraglicheKosten = MathUtil.DEFAULT.addNullSafe(MathUtil.roundToFrankenRappen(kosten),
			vertraglicheKosten);
	}

	public void addHauptmahlzeiten(@Nonnull BigDecimal amount) {
		vertraglicheHauptmahlzeiten = MathUtil.DEFAULT.addNullSafe(amount, vertraglicheHauptmahlzeiten);
	}

	public void addNebenmahlzeiten(@Nonnull BigDecimal amount) {
		vertraglicheNebenmahlzeiten = MathUtil.DEFAULT.addNullSafe(amount, vertraglicheNebenmahlzeiten);
	}

	public void addTarifHaupt(BigDecimal tarif) {
		vertraglicherTarifHauptmahlzeit = MathUtil.DEFAULT.addNullSafe(MathUtil.roundToFrankenRappen(tarif),
			vertraglicherTarifHauptmahlzeit);
	}

	public void addTarifNeben(BigDecimal tarif) {
		vertraglicherTarifNebenmahlzeit = MathUtil.DEFAULT.addNullSafe(MathUtil.roundToFrankenRappen(tarif),
			vertraglicherTarifNebenmahlzeit);
	}

	@Nonnull
	public Betreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(@Nonnull Betreuung betreuung) {
		this.betreuung = betreuung;
	}

	@Nullable
	public BigDecimal getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(@Nonnull BigDecimal multiplier) {
		this.multiplier = multiplier;
	}

	@Override
	public int compareTo(@Nonnull BetreuungspensumAbweichung other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getGueltigkeit(), other.getGueltigkeit());
		compareToBuilder.append(this.getId(), other.getId());
		return compareToBuilder.toComparison();
	}

	@Nonnull
	public BetreuungspensumAbweichung copyBetreuungspensumAbweichung(
		@Nonnull BetreuungspensumAbweichung target, @Nonnull AntragCopyType copyType, @Nonnull Betreuung targetBetreuung) {
		super.copyAbstractBetreuungspensumMahlzeitenEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setBetreuung(targetBetreuung);
			target.setStatus(getStatus());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_AR_2023:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	public BetreuungsmitteilungPensum convertAbweichungToMitteilungPensum(@Nonnull Betreuungsmitteilung mitteilung) {
		BetreuungsmitteilungPensum mitteilungPensum = new BetreuungsmitteilungPensum();
		mitteilungPensum.setBetreuungsmitteilung(mitteilung);
		mitteilungPensum.setGueltigkeit(getGueltigkeit());

		BigDecimal pensum = vertraglichWhenStatusNone(getPensum(), getVertraglichesPensum());
		BigDecimal kosten = vertraglichWhenStatusNone(getMonatlicheBetreuungskosten(), getVertraglicheKosten());
		BigDecimal hauptmahlzeiten = vertraglichWhenStatusNone(getMonatlicheHauptmahlzeiten(), getVertraglicheHauptmahlzeiten());
		BigDecimal nebenmahlzeiten = vertraglichWhenStatusNone(getMonatlicheNebenmahlzeiten(), getVertraglicheNebenmahlzeiten());

		mitteilungPensum.setUnitForDisplay(getUnitForDisplay());
		mitteilungPensum.setPensum(pensum);
		mitteilungPensum.setMonatlicheBetreuungskosten(kosten);
		//
		mitteilungPensum.setMonatlicheHauptmahlzeiten(hauptmahlzeiten);
		mitteilungPensum.setMonatlicheNebenmahlzeiten(nebenmahlzeiten);

		// really not sure why tarife for Mahlzeiten are different than everything else...
		// Tarif is immutable at this point and we just copy the old value
		if (requireNonNull(mitteilung.getBetreuung()).isAngebotMittagstisch()) {
			BigDecimal kostenHauptmahlzeit = vertraglichWhenStatusNone(getTarifProHauptmahlzeit(), getVertraglicherTarifHauptmahlzeit());
			BigDecimal kostenNebenmahlzeit = vertraglichWhenStatusNone(getTarifProNebenmahlzeit(), getVertraglicherTarifNebenmahlzeit());
			mitteilungPensum.setTarifProHauptmahlzeit(kostenHauptmahlzeit);
			mitteilungPensum.setTarifProNebenmahlzeit(kostenNebenmahlzeit);
		} else {
			mitteilungPensum.setTarifProHauptmahlzeit(requireNonNull(vertraglicherTarifHauptmahlzeit));
			mitteilungPensum.setTarifProNebenmahlzeit(requireNonNull(vertraglicherTarifNebenmahlzeit));
		}
		// not sure why this is different either...
		mitteilungPensum.setStuendlicheVollkosten(getStuendlicheVollkosten());

		// as soon as we created a Mitteilung out of the Abweichung we set the state to verrechnet (freigegeben) and
		// attach it to the BetreuungsmitteilungPensum
		if (!isNew()) {
			mitteilungPensum.setBetreuungspensumAbweichung(this);
			if (getStatus() != BetreuungspensumAbweichungStatus.UEBERNOMMEN) {
				setStatus(BetreuungspensumAbweichungStatus.VERRECHNET);
			}
		}

		return mitteilungPensum;
	}

	@Nonnull
	private BigDecimal vertraglichWhenStatusNone(BigDecimal value, @Nullable BigDecimal vertraglicherValue) {
		return requireNonNull(status == BetreuungspensumAbweichungStatus.NONE ? vertraglicherValue : value);
	}
}
