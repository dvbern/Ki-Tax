/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von Institution in der Datenbank.
 */
@Audited
@Entity
public class Institution extends AbstractMutableEntity implements HasMandant, Displayable {

	private static final long serialVersionUID = -8706487439884760618L;

	@Column(nullable = false)
	private @Size(min = 1, max = DB_DEFAULT_MAX_LENGTH) @NotNull String name;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_traegerschaft_id"))
	private Traegerschaft traegerschaft;

	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_mandant_id"))
	@ManyToOne(optional = false)
	private @NotNull Mandant mandant;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private @NotNull InstitutionStatus status = InstitutionStatus.EINGELADEN;

	@Column(nullable = false)
	private @NotNull boolean stammdatenCheckRequired = false;

	/**
	 * @deprecated used to convert all Institutionen from the database to InstitutionChangedEvents and publish them to
	 * Kafka. Thus, only required for one deployment.
	 */
	@Deprecated
	@Column(nullable = false)
	private @NotNull boolean eventPublished = true;

	@Valid
	@Nonnull
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "institution")
	private Set<InstitutionExternalClient> institutionExternalClients = new HashSet<>();

	public Institution() {
	}

	@Override
	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Nullable
	public Traegerschaft getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable Traegerschaft traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Override
	public @NotNull Mandant getMandant() {
		return mandant;
	}

	public void setMandant(@NotNull Mandant mandant) {
		this.mandant = mandant;
	}

	public InstitutionStatus getStatus() {
		return status;
	}

	public void setStatus(InstitutionStatus status) {
		this.status = status;
	}

	public boolean isStammdatenCheckRequired() {
		return stammdatenCheckRequired;
	}

	public void setStammdatenCheckRequired(boolean stammdatenCheckRequired) {
		this.stammdatenCheckRequired = stammdatenCheckRequired;
	}

	public boolean isEventPublished() {
		return eventPublished;
	}

	public void setEventPublished(boolean eventPublished) {
		this.eventPublished = eventPublished;
	}

	public boolean isUnknownInstitution() {
		return this.name.equals(Constants.UNKNOWN_INSTITUTION_NAME);
	}

	/**
	 * The data of this institution can be accessed by any ExternalClient in this set. E.g. via the exchange service
	 */
	@Nonnull
	public Set<InstitutionExternalClient> getInstitutionExternalClients() {
		return institutionExternalClients;
	}

	public void setInstitutionExternalClients(@Nonnull Set<InstitutionExternalClient> institutionExternalClients) {
		this.institutionExternalClients = institutionExternalClients;
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
		if (!(other instanceof Institution)) {
			return false;
		}
		final Institution otherInstitution = (Institution) other;
		return getStatus() == otherInstitution.getStatus() &&
			Objects.equals(getName(), otherInstitution.getName());
	}

	@Override
	public String getMessageForAccessException() {
		return "bgNummer: " + this.getName()
			+ ", status: " + this.getStatus();
	}
}
