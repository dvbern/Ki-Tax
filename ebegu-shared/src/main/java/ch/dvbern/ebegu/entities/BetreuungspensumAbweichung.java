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

@Audited
@Entity
public class BetreuungspensumAbweichung extends AbstractDecimalPensum implements Comparable<BetreuungspensumAbweichung>  {

	private static final long serialVersionUID = -8308660793880620086L;

	@NotNull @Nonnull
	@Enumerated(EnumType.STRING)
	private BetreuungspensumAbweichungStatus status = BetreuungspensumAbweichungStatus.NONE;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuungspensum_abweichung_betreuung_id"), nullable = false)
	private Betreuung betreuung;

	// every Zeitabschnitt containing any date of this.gueltigkeit
	// merged by its part of the current Gueltigkeit into one monthly (=this.gueltigkeit) Betreuungspensum.
	@Transient
	@Nullable
	private BigDecimal vertraglichesPensum = null;

	@Transient
	@Nullable
	private BigDecimal vertraglicheKosten = null;

	@Nonnull
	public BetreuungspensumAbweichungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull BetreuungspensumAbweichungStatus status) {
		this.status = status;
	}

	public BigDecimal getVertraglichesPensum() {
		return vertraglichesPensum;
	}

	public void setVertraglichesPensum(BigDecimal vertraglichesPensum) {
		this.vertraglichesPensum = vertraglichesPensum;
	}

	public BigDecimal getVertraglicheKosten() {
		return vertraglicheKosten;
	}

	public void setVertraglicheKosten(BigDecimal vertraglicheKosten) {
		this.vertraglicheKosten = vertraglicheKosten;
	}

	public void addPensum(BigDecimal pensum) {
		vertraglichesPensum = MathUtil.DEFAULT.addNullSafe(pensum, vertraglichesPensum);
	}

	public void addKosten(BigDecimal kosten) {
		vertraglicheKosten = MathUtil.DEFAULT.addNullSafe(MathUtil.roundToFrankenRappen(kosten),
			vertraglicheKosten);
	}

	@Nonnull
	public Betreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(@Nonnull Betreuung betreuung) {
		this.betreuung = betreuung;
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
		super.copyAbstractBetreuungspensumEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setBetreuung(targetBetreuung);
			target.setStatus(getStatus());
			break;
		case ERNEUERUNG:
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

		BigDecimal pensum = getStatus() == BetreuungspensumAbweichungStatus.NONE
			? getVertraglichesPensum() : getPensum();

		BigDecimal kosten = getStatus() == BetreuungspensumAbweichungStatus.NONE
			? getVertraglicheKosten() : getMonatlicheBetreuungskosten();

		Objects.requireNonNull(pensum);
		Objects.requireNonNull(kosten);

		mitteilungPensum.setUnitForDisplay(getUnitForDisplay());
		mitteilungPensum.setPensum(pensum);
		mitteilungPensum.setMonatlicheBetreuungskosten(kosten);

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
}
