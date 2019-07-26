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
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class BetreuungspensumAbweichung extends AbstractDecimalPensum implements Comparable<BetreuungspensumAbweichung>  {

	private static final long serialVersionUID = -8308660793880620086L;

	@NotNull
	@Enumerated(EnumType.STRING)
	private BetreuungspensumAbweichungStatus status = BetreuungspensumAbweichungStatus.NONE;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuungspensum_abweichung_betreuung_id"), nullable = false)
	private Betreuung betreuung;

	// every Zeitabschnitt containing any date of this.gueltigkeit
	// merged by its part of the current Gueltigkeit into one monthly (=this.gueltigkeit) Betreuungspensum.
	@Transient
	@Nullable
	private BigDecimal originalPensumMerged = null;

	@Transient
	@Nullable
	private BigDecimal originalKostenMerged = null;

	public BetreuungspensumAbweichungStatus getStatus() {
		return status;
	}

	public void setStatus(BetreuungspensumAbweichungStatus status) {
		this.status = status;
	}

	public BigDecimal getOriginalPensumMerged() {
		return originalPensumMerged;
	}

	public void setOriginalPensumMerged(BigDecimal originalPensumMerged) {
		this.originalPensumMerged = originalPensumMerged;
	}

	public BigDecimal getOriginalKostenMerged() {
		return originalKostenMerged;
	}

	public void setOriginalKostenMerged(BigDecimal originalKostenMerged) {
		this.originalKostenMerged = originalKostenMerged;
	}

	public void addPensum(BigDecimal pensum) {
		originalPensumMerged = MathUtil.DEFAULT.addNullSafe(pensum, originalPensumMerged);
	}

	public void addKosten(BigDecimal kosten) {
		originalKostenMerged = MathUtil.DEFAULT.addNullSafe(MathUtil.DEFAULT.roundToFrankenRappen(kosten),
			originalKostenMerged);
	}

	public Betreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(Betreuung betreuung) {
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
}
