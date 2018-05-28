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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Klasse zum deserialisieren/serialisieren des SmartTable Filter Objekts fuer suchfilter in Java
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public class BenutzerPredicateObjectDTO implements Serializable {

	private static final long serialVersionUID = -2248051428962150142L;

	private String username;
	private String vorname;
	private String nachname;
	private String email;
	private String role;
	private String roleGueltigBis;
	private String institution;
	private String traegerschaft;
	private Boolean gesperrt;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRoleGueltigBis() {
		return roleGueltigBis;
	}

	public void setRoleGueltigBis(String roleGueltigBis) {
		this.roleGueltigBis = roleGueltigBis;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	public Boolean getGesperrt() {
		return gesperrt;
	}

	public void setGesperrt(Boolean gesperrt) {
		this.gesperrt = gesperrt;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("username", username)
			.append("vorname", vorname)
			.append("nachname", nachname)
			.append("email", email)
			.append("role", role)
			.append("roleGueltigBis", roleGueltigBis)
			.append("institution", institution)
			.append("traegerschaft", traegerschaft)
			.append("gesperrt", gesperrt)
			.toString();
	}
}
