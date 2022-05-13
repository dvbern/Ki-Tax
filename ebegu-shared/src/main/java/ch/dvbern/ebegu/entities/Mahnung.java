/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

/**
 * Entitaet fuer Mahnungen
 */
@Audited
@Entity
public class Mahnung extends AbstractMutableEntity {

	private static final long serialVersionUID = -4210097012467874096L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_mahnung_gesuch_id"))
	private Gesuch gesuch;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MahnungTyp mahnungTyp;

	@NotNull
	@Column(nullable = false)
	private LocalDate datumFristablauf;

	@NotNull
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = false, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungen;

	@Column(nullable = true)
	private LocalDateTime timestampAbgeschlossen;

	/**
	 * This parameter must be set to true when the Mahnung has already been treated,
	 * so that we can distinguish between a Mahnung with a fristDatum in the past and a
	 * Mahnung that has really been set as expired.
	 */
	@NotNull
	@Column(nullable = false)
	private Boolean abgelaufen = false;

	public Gesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(Gesuch gesuch) {
		this.gesuch = gesuch;
	}

	@Nonnull
	public MahnungTyp getMahnungTyp() {
		return mahnungTyp;
	}

	public void setMahnungTyp(@Nonnull MahnungTyp mahnungTyp) {
		this.mahnungTyp = mahnungTyp;
	}

	@Nullable
	public LocalDate getDatumFristablauf() {
		return datumFristablauf;
	}

	public void setDatumFristablauf(@Nullable LocalDate datumFristablauf) {
		this.datumFristablauf = datumFristablauf;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Nullable
	public LocalDateTime getTimestampAbgeschlossen() {
		return timestampAbgeschlossen;
	}

	public void setTimestampAbgeschlossen(@Nullable LocalDateTime timestampBeendet) {
		this.timestampAbgeschlossen = timestampBeendet;
	}

	public Boolean getAbgelaufen() {
		return abgelaufen;
	}

	public void setAbgelaufen(Boolean abgelaufen) {
		this.abgelaufen = abgelaufen;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final Mahnung otherMahnung = (Mahnung) other;
		return Objects.equals(getMahnungTyp(), otherMahnung.getMahnungTyp()) &&
			Objects.equals(getDatumFristablauf(), otherMahnung.getDatumFristablauf()) &&
			Objects.equals(getBemerkungen(), otherMahnung.getBemerkungen()) &&
			Objects.equals(getTimestampAbgeschlossen(), otherMahnung.getTimestampAbgeschlossen());
	}
}
