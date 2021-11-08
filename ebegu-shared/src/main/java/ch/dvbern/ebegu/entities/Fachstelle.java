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

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.FachstelleName;
import ch.dvbern.ebegu.types.DateRange;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von Fachstellen in der Datenbank.
 */
@Audited
@Entity
public class Fachstelle extends AbstractDateRangedEntity implements HasMandant {

	private static final long serialVersionUID = -7687613920281069860L;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private FachstelleName name;

	@Column(nullable = false)
	private boolean fachstelleAnspruch;

	@Column(nullable = false)
	private boolean fachstelleErweiterteBetreuung;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_fachstelle_mandant_id"))
	private Mandant mandant;


	public Fachstelle() {
	}

	@Nonnull
	public FachstelleName getName() {
		return name;
	}

	public void setName(@Nonnull FachstelleName name) {
		this.name = name;
	}

	public boolean isFachstelleAnspruch() {
		return fachstelleAnspruch;
	}

	public void setFachstelleAnspruch(boolean fachstelleAnspruch) {
		this.fachstelleAnspruch = fachstelleAnspruch;
	}

	public boolean isFachstelleErweiterteBetreuung() {
		return fachstelleErweiterteBetreuung;
	}

	public void setFachstelleErweiterteBetreuung(boolean fachstelleErweiterteBetreuung) {
		this.fachstelleErweiterteBetreuung = fachstelleErweiterteBetreuung;
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
		if (!(other instanceof Fachstelle)) {
			return false;
		}
		final Fachstelle otherGesuchsteller = (Fachstelle) other;
		return getName() == otherGesuchsteller.getName();
	}

	@NotNull
	@Override
	public Mandant getMandant() {
		return mandant;
	}

	@Override
	public void setMandant(Mandant mandant) {
		this.mandant = mandant;
	}
}
