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
import javax.annotation.Nullable;
import javax.persistence.AssociationOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Stammdaten für Ferieninseln
 */
@Entity
@Audited
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "adresse_id", name = "UK_institution_stammdaten_fi_adresse_id"),
		@UniqueConstraint(columnNames = "ferieninsel_id", name= "UK_institution_stammdaten_fi_ferieninsel_id")
	},
	indexes = {
		@Index(name = "IX_institution_stammdaten_fi_gueltig_ab", columnList = "gueltigAb"),
		@Index(name = "IX_institution_stammdaten_fi_gueltig_bis", columnList = "gueltigBis")
	}
)
// Der ForeignKey-Name wird leider nicht richtig generiert, muss von Hand angepasst werden!
@AssociationOverride(name="adresse", joinColumns=@JoinColumn(name="adresse_id"), foreignKey = @ForeignKey(name = "FK_institution_stammdaten_ferieninsel_adresse_id"))
// TODO (KIBON-616): Umbenennen
public class InstitutionStammdatenFerieninsel1 extends AbstractInstitutionStammdaten {

	private static final long serialVersionUID = -9037857320548372570L;

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_ferieninsel_id"), nullable = false)
	private Ferieninsel ferieninsel;

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


	public InstitutionStammdatenFerieninsel1() {
	}


	@NotNull
	public Ferieninsel getFerieninsel() {
		return ferieninsel;
	}

	public void setFerieninsel(@NotNull Ferieninsel ferieninsel) {
		this.ferieninsel = ferieninsel;
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

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof InstitutionStammdatenFerieninsel1)) {
			return false;
		}
		final InstitutionStammdatenFerieninsel1 otherInstStammdaten = (InstitutionStammdatenFerieninsel1) other;
		return EbeguUtil.isSameObject(getFerieninsel(), otherInstStammdaten.getFerieninsel());
	}

	@Nonnull
	@Override
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return BetreuungsangebotTyp.FERIENINSEL;
	}
}
