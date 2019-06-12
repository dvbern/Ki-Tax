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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;

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

	@NotNull
	@Column(nullable = false)
	private Long totalKinderVerrechnet;

	public VerrechnungKibonDetail() {
	}

	@NotNull
	public VerrechnungKibon getVerrechnungKibon() {
		return verrechnungKibon;
	}

	public void setVerrechnungKibon(@NotNull VerrechnungKibon verrechnungKibon) {
		this.verrechnungKibon = verrechnungKibon;
	}

	@NotNull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@NotNull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@NotNull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@NotNull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@NotNull
	public Long getTotalKinderVerrechnet() {
		return totalKinderVerrechnet;
	}

	public void setTotalKinderVerrechnet(@NotNull Long totalKinderVerrechnet) {
		this.totalKinderVerrechnet = totalKinderVerrechnet;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}
}
