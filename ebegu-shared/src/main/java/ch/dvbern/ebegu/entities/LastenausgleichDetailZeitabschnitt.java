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
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

/**
 * Entitaet zum Verknüpfen eines LastenausgleichDetails mit den Zeitabschnitten
 */
@Audited
@Entity
public class LastenausgleichDetailZeitabschnitt extends AbstractEntity {

	private static final long serialVersionUID = 4243309916882090263L;

	@ManyToOne(optional = false)
	@Nonnull
	@NotNull
	@JoinColumn(nullable = false)
	private LastenausgleichDetail lastenausgleichDetail;

	@ManyToOne(optional = false)
	@Nonnull
	@NotNull
	@JoinColumn(nullable = false)
	private VerfuegungZeitabschnitt zeitabschnitt;

	public LastenausgleichDetailZeitabschnitt() {}

	@Nonnull
	public LastenausgleichDetail getLastenausgleichDetail() {
		return lastenausgleichDetail;
	}

	public void setLastenausgleichDetail(@Nonnull LastenausgleichDetail lastenausgleichDetail) {
		this.lastenausgleichDetail = lastenausgleichDetail;
	}

	public LastenausgleichDetailZeitabschnitt(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull LastenausgleichDetail lastenausgleichDetail
	) {
		this.zeitabschnitt = zeitabschnitt;
		this.lastenausgleichDetail = lastenausgleichDetail;
	}

	@Nonnull
	public VerfuegungZeitabschnitt getZeitabschnitt() {
		return zeitabschnitt;
	}

	public void setZeitabschnitte(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		this.zeitabschnitt = zeitabschnitt;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return this.getId().equals(other.getId());
	}
}
