/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Entity um eine History einer Benutzerberechtigung zu speichern.
 */
@Audited
@Entity
public class BerechtigungHistory extends AbstractDateRangedEntity implements Comparable<BerechtigungHistory> {

	private static final long serialVersionUID = -9032257320864372570L;

	@Column(nullable = false)
	@NotNull
	private String username = null;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private final UserRole role;

	@Nullable
	@ManyToOne
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_berechtigung_history_institution_id"))
	private Institution institution = null;

	@Nullable
	@ManyToOne
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_berechtigung_history_traegerschaft_id"))
	private Traegerschaft traegerschaft = null;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_berechtigung_history_sozialdienst_id"))
	private Sozialdienst sozialdienst = null;

	@Nullable
	@Column
	@Size(max = Constants.DB_DEFAULT_MAX_LENGTH)
	private String gemeinden = null;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private final BenutzerStatus status;

	@NotNull
	@Column(nullable = false)
	private Boolean geloescht = false;

	@SuppressWarnings("ConstantConditions")
	protected BerechtigungHistory() {
		this.status = null;
		this.role = null;
	}

	public BerechtigungHistory(@Nonnull Berechtigung berechtigung, boolean deleted) {
		this.role = berechtigung.getRole();
		this.setGueltigkeit(berechtigung.getGueltigkeit());
		this.institution = berechtigung.getInstitution();
		this.traegerschaft = berechtigung.getTraegerschaft();
		this.gemeinden = berechtigung.extractGemeindenForBerechtigungAsString();
		this.sozialdienst = berechtigung.getSozialdienst();

		Benutzer benutzer = berechtigung.getBenutzer();
		this.username = benutzer.getUsername();
		this.status = benutzer.getStatus();

		this.geloescht = deleted;
	}

	public String getUsername() {
		return username;
	}

	public UserRole getRole() {
		return role;
	}

	@Nullable
	public Institution getInstitution() {
		return institution;
	}

	@Nullable
	public Traegerschaft getTraegerschaft() {
		return traegerschaft;
	}

	@Nullable
	public String getGemeinden() {
		return gemeinden;
	}

	public BenutzerStatus getStatus() {
		return status;
	}

	public Boolean getGeloescht() {
		return geloescht;
	}

	@Override
	public int compareTo(BerechtigungHistory o) {
		CompareToBuilder cb = new CompareToBuilder();
		cb.append(this.getTimestampErstellt(), o.getTimestampErstellt())
			.append(this.getId(), o.getId());
		return cb.toComparison();
	}

	@Override
	public boolean isSame(AbstractEntity otherEntity) {
		if (!(otherEntity instanceof BerechtigungHistory)) {
			return false;
		}
		BerechtigungHistory other = (BerechtigungHistory) otherEntity;
		CompareToBuilder cb = new CompareToBuilder();
		cb.append(this.getUsername(), other.getUsername());
		cb.append(this.getRole(), other.getRole());
		cb.append(this.getGueltigkeit().getGueltigAb(), other.getGueltigkeit().getGueltigAb());
		cb.append(this.getGueltigkeit().getGueltigBis(), other.getGueltigkeit().getGueltigBis());
		cb.append(this.getInstitution(), other.getInstitution());
		cb.append(this.getTraegerschaft(), other.getTraegerschaft());
		cb.append(this.getGemeinden(), other.getGemeinden());
		cb.append(this.getStatus(), other.getStatus());
		cb.append(this.getGeloescht(), other.getGeloescht());
		cb.append(this.getSozialdienst(), other.getSozialdienst());
		return cb.toComparison() == 0;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("username", username)
			.append("role", role)
			.append("gueltigkeit", getGueltigkeit())
			.append("institution", institution)
			.append("traegerschaft", traegerschaft)
			.append("sozialdienst", sozialdienst)
			.append("gemeinden", gemeinden)
			.append("status", status)
			.append("geloescht", geloescht)
			.toString();
	}

	@Nullable
	public Sozialdienst getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(@Nullable Sozialdienst sozialdienst) {
		this.sozialdienst = sozialdienst;
	}
}
