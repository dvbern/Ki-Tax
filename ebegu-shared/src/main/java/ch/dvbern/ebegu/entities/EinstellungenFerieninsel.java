/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import javax.annotation.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von gesuchsperiode-abhängigen Ferieninsel-Einstellungen in der Datenbank.
 */
@Audited
@Entity
public class EinstellungenFerieninsel extends AbstractEntity implements Comparable<EinstellungenFerieninsel> {

	private static final long serialVersionUID = -495776045789299006L;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_einstellungen_fi_inst_stammdaten_ferieninsel_id"), nullable = false)
	private InstitutionStammdatenFerieninsel institutionStammdatenFerieninsel;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "FK_einstellungen_fi_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String ausweichstandortSommerferien;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String ausweichstandortHerbstferien;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String ausweichstandortSportferien;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String ausweichstandortFruehlingsferien;


	public EinstellungenFerieninsel() {
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
		return true;
	}

	@Override
	public int compareTo(EinstellungenFerieninsel o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	@Nonnull
	public InstitutionStammdatenFerieninsel getInstitutionStammdatenFerieninsel() {
		return institutionStammdatenFerieninsel;
	}

	public void setInstitutionStammdatenFerieninsel(@Nonnull InstitutionStammdatenFerieninsel institutionStammdatenFerieninsel) {
		this.institutionStammdatenFerieninsel = institutionStammdatenFerieninsel;
	}

	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public String getAusweichstandortSommerferien() {
		return ausweichstandortSommerferien;
	}

	public void setAusweichstandortSommerferien(@Nullable String ausweichstandortSommerferien) {
		this.ausweichstandortSommerferien = ausweichstandortSommerferien;
	}

	@Nullable
	public String getAusweichstandortHerbstferien() {
		return ausweichstandortHerbstferien;
	}

	public void setAusweichstandortHerbstferien(@Nullable String ausweichstandortHerbstferien) {
		this.ausweichstandortHerbstferien = ausweichstandortHerbstferien;
	}

	@Nullable
	public String getAusweichstandortSportferien() {
		return ausweichstandortSportferien;
	}

	public void setAusweichstandortSportferien(@Nullable String ausweichstandortSportferien) {
		this.ausweichstandortSportferien = ausweichstandortSportferien;
	}

	@Nullable
	public String getAusweichstandortFruehlingsferien() {
		return ausweichstandortFruehlingsferien;
	}

	public void setAusweichstandortFruehlingsferien(@Nullable String ausweichstandortFruehlingsferien) {
		this.ausweichstandortFruehlingsferien = ausweichstandortFruehlingsferien;
	}

	@Nonnull
	public EinstellungenFerieninsel copyForGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		EinstellungenFerieninsel copy = new EinstellungenFerieninsel();
		copy.setInstitutionStammdatenFerieninsel(this.getInstitutionStammdatenFerieninsel());
		copy.setGesuchsperiode(gesuchsperiode);
		copy.setAusweichstandortHerbstferien(this.getAusweichstandortHerbstferien());
		copy.setAusweichstandortFruehlingsferien(this.getAusweichstandortFruehlingsferien());
		copy.setAusweichstandortSommerferien(this.getAusweichstandortSommerferien());
		copy.setAusweichstandortSportferien(this.getAusweichstandortSportferien());
		return copy;
	}
}
