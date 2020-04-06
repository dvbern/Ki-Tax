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

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Entitaet zum Speichern von Verrechnungsdetails in der Datenbank.
 */
@Entity
public class VerrechnungKibonDetail extends AbstractEntity {

	private static final long serialVersionUID = -7687613920281069860L;

	@NotNull
	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verrechnungdetail_verrechnung_id"))
	private VerrechnungKibon verrechnungKibon;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verrechnungdetail_gemeinde_id"))
	private Gemeinde gemeinde;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verrechnungdetail_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Long totalBg;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Long totalTs;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Long totalBgTs;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Long totalKeinAngebot;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Long totalFi;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Long totalTagi;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Long totalFiTagi;

	public VerrechnungKibonDetail() {
	}

	@Nonnull
	public VerrechnungKibon getVerrechnungKibon() {
		return verrechnungKibon;
	}

	public void setVerrechnungKibon(@Nonnull VerrechnungKibon verrechnungKibon) {
		this.verrechnungKibon = verrechnungKibon;
	}

	@Nonnull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nonnull
	public Long getTotalBg() {
		return totalBg;
	}

	public void setTotalBg(@Nonnull Long totalBg) {
		this.totalBg = totalBg;
	}

	@Nonnull
	public Long getTotalTs() {
		return totalTs;
	}

	public void setTotalTs(@Nonnull Long totalTs) {
		this.totalTs = totalTs;
	}

	@Nonnull
	public Long getTotalBgTs() {
		return totalBgTs;
	}

	public void setTotalBgTs(@Nonnull Long totalBgTs) {
		this.totalBgTs = totalBgTs;
	}

	@Nonnull
	public Long getTotalKeinAngebot() {
		return totalKeinAngebot;
	}

	public void setTotalKeinAngebot(@Nonnull Long totalKeinAngebot) {
		this.totalKeinAngebot = totalKeinAngebot;
	}

	@Nonnull
	public Long getTotalFi() {
		return totalFi;
	}

	public void setTotalFi(@Nonnull Long totalFi) {
		this.totalFi = totalFi;
	}

	@Nonnull
	public Long getTotalTagi() {
		return totalTagi;
	}

	public void setTotalTagi(@Nonnull Long totalTagi) {
		this.totalTagi = totalTagi;
	}

	@Nonnull
	public Long getTotalFiTagi() {
		return totalFiTagi;
	}

	public void setTotalFiTagi(@Nonnull Long totalFiTagi) {
		this.totalFiTagi = totalFiTagi;
	}

	@Nonnull
	public Long getTotalBgAndBgTs() {
		return getTotalBg() + getTotalBgTs();
	}

	@Nonnull
	public Long getTotalTsAndBgTs() {
		return getTotalTs() + getTotalBgTs();
	}

	@Nonnull
	public Long getTotalFiAndFiTagi() {
		return getTotalFi() + getTotalFiTagi();
	}

	@Nonnull
	public Long getTotalTagiAndFiTagi() {
		return getTotalTagi() + getTotalFiTagi();
	}

	@Nonnull
	public Long getTotalKanton() {
		return getTotalBg() + getTotalTs() + getTotalBgTs()+ getTotalKeinAngebot();
	}

	@Nonnull
	public Long getTotalGemeinde() {
		return getTotalFi() + getTotalTagi() + getTotalFiTagi();
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}
}
