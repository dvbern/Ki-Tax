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

package ch.dvbern.ebegu.dto.suchfilter.smarttable;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.UserRole;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Klasse zum deserialisieren/serialisieren des SmartTable Filter Objekts fuer suchfilter in Java
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public class BenutzerPredicateObjectDTO implements Serializable {

	private static final long serialVersionUID = -2248051428962150142L;

	@Nullable
	private String username = null;
	@Nullable
	private String vorname = null;
	@Nullable
	private String nachname = null;
	@Nullable
	private String email = null;
	@Nullable
	private UserRole role = null;
	@Nullable
	private String roleGueltigBis = null;
	@Nullable
	private String gemeinde = null;
	@Nullable
	private String institution = null;
	@Nullable
	private String traegerschaft = null;
	@Nullable
	private String sozialdienst = null;
	@Nullable
	private BenutzerStatus status = null;

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("username", username)
			.append("vorname", vorname)
			.append("nachname", nachname)
			.append("email", email)
			.append("role", role)
			.append("roleGueltigBis", roleGueltigBis)
			.append("gemeinde", gemeinde)
			.append("institution", institution)
			.append("traegerschaft", traegerschaft)
			.append("sozialdienst", sozialdienst)
			.append("status", status)
			.toString();
	}

	@Nullable
	public String getUsername() {
		return username;
	}

	public void setUsername(@Nullable String username) {
		this.username = username;
	}

	@Nullable
	public String getVorname() {
		return vorname;
	}

	public void setVorname(@Nullable String vorname) {
		this.vorname = vorname;
	}

	@Nullable
	public String getNachname() {
		return nachname;
	}

	public void setNachname(@Nullable String nachname) {
		this.nachname = nachname;
	}

	@Nullable
	public String getEmail() {
		return email;
	}

	public void setEmail(@Nullable String email) {
		this.email = email;
	}

	@Nullable
	public UserRole getRole() {
		return role;
	}

	public void setRole(@Nullable UserRole role) {
		this.role = role;
	}

	@Nullable
	public String getRoleGueltigBis() {
		return roleGueltigBis;
	}

	public void setRoleGueltigBis(@Nullable String roleGueltigBis) {
		this.roleGueltigBis = roleGueltigBis;
	}

	@Nullable
	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable String gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nullable
	public String getInstitution() {
		return institution;
	}

	public void setInstitution(@Nullable String institution) {
		this.institution = institution;
	}

	@Nullable
	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Nullable
	public BenutzerStatus getStatus() {
		return status;
	}

	public void setStatus(@Nullable BenutzerStatus status) {
		this.status = status;
	}

	@Nullable
	public String getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(@Nullable String sozialdienst) {
		this.sozialdienst = sozialdienst;
	}
}
