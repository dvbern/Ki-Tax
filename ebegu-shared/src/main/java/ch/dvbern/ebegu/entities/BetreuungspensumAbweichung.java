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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.BetreuungspensumAbweichungStatus;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class BetreuungspensumAbweichung extends AbstractDecimalPensum {

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
	private BigDecimal originalPensumMerged = null;

	@Transient
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
		if (originalPensumMerged == null) {
			this.originalPensumMerged = pensum;
		} else {
			this.originalPensumMerged.add(pensum);
		}
	}

	public void addKosten(BigDecimal kosten) {
		if (originalKostenMerged == null) {
			this.originalKostenMerged = kosten;
		} else {
			this.originalKostenMerged.add(kosten);
		}
	}

	public Betreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(Betreuung betreuung) {
		this.betreuung = betreuung;
	}
}
