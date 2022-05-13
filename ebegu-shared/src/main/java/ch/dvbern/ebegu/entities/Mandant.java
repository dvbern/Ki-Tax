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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von Mandant in der Datenbank.
 */
@Audited
@Entity
public class Mandant extends AbstractMutableEntity implements Displayable {

	private static final long serialVersionUID = -8433487433884700618L;
	public static final String MANDANT_PARAMETER = "mandant";

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	private String name;

	@Column(nullable = false)
	private boolean angebotTS = false;

	@Column(nullable = false)
	private boolean angebotFI = false;

	@Column()
	@Enumerated(EnumType.STRING)
	private MandantIdentifier mandantIdentifier;

	@Column(nullable = false)
	private boolean activated = false;

	public Mandant() {
	}

	@Override
	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	public boolean isAngebotTS() {
		return angebotTS;
	}

	public void setAngebotTS(boolean angebotTS) {
		this.angebotTS = angebotTS;
	}

	public boolean isAngebotFI() {
		return angebotFI;
	}

	public void setAngebotFI(boolean angebotFI) {
		this.angebotFI = angebotFI;
	}

	public MandantIdentifier getMandantIdentifier() {
		return mandantIdentifier;
	}

	public void setMandantIdentifier(MandantIdentifier mandantIdentifier) {
		this.mandantIdentifier = mandantIdentifier;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
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
		final Mandant otherMandant = (Mandant) other;
		return Objects.equals(getName(), otherMandant.getName());
	}
}
