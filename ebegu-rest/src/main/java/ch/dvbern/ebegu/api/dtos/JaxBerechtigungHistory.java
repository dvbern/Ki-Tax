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
package ch.dvbern.ebegu.api.dtos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.UserRole;

/**
 * Wrapper DTO fuer eine BerechtigungHistory
 */
@XmlRootElement(name = "berechtigungHistory")
public class JaxBerechtigungHistory extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = 2769899329796452129L;

	@Nonnull
	private String userErstellt;

	@Nonnull
	private String username;

	@Nonnull
	private UserRole role;

	@Nullable
	private JaxTraegerschaft traegerschaft;

	@Nullable
	private JaxInstitution institution;

	private boolean gesperrt;

	private boolean geloescht;


	@Nonnull
	public String getUserErstellt() {
		return userErstellt;
	}

	public void setUserErstellt(@Nonnull String userErstellt) {
		this.userErstellt = userErstellt;
	}

	@Nonnull
	public String getUsername() {
		return username;
	}

	public void setUsername(@Nonnull String username) {
		this.username = username;
	}

	@Nonnull
	public UserRole getRole() {
		return role;
	}

	public void setRole(@Nonnull UserRole role) {
		this.role = role;
	}

	@Nullable
	public JaxTraegerschaft getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable JaxTraegerschaft traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Nullable
	public JaxInstitution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nullable JaxInstitution institution) {
		this.institution = institution;
	}

	public boolean isGesperrt() {
		return gesperrt;
	}

	public void setGesperrt(boolean gesperrt) {
		this.gesperrt = gesperrt;
	}

	public boolean isGeloescht() {
		return geloescht;
	}

	public void setGeloescht(boolean geloescht) {
		this.geloescht = geloescht;
	}
}
