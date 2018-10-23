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
package ch.dvbern.ebegu.api.dtos;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.UserRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@XmlRootElement(name = "benutzer")
public class JaxBenutzer implements Serializable {

	private static final long serialVersionUID = 2769899329796452129L;

	@Nonnull
	private String username = "";

	@Nullable
	private String externalUUID = "";

	@Nonnull
	private String password = "";

	@Nonnull
	private String nachname = "";

	@Nonnull
	private String vorname = "";

	@Nonnull
	private String email = "";

	private JaxMandant mandant;

	@Nonnull
	private BenutzerStatus status = BenutzerStatus.GESPERRT;

	@Nullable
	private JaxBerechtigung currentBerechtigung;

	@Nonnull
	private Set<JaxBerechtigung> berechtigungen = new LinkedHashSet<>();


	@SuppressFBWarnings(value = "NM_CONFUSING", justification = "Other method is external interface, cant change that")
	@Nonnull
	public String getUsername() {
		return username;
	}

	public void setUsername(@Nonnull String username) {
		this.username = username;
	}

	@Nullable
	public String getExternalUUID() {
		return externalUUID;
	}

	public void setExternalUUID(@Nullable String externalUUID) {
		this.externalUUID = externalUUID;
	}

	@Nonnull
	public String getPassword() {
		return password;
	}

	public void setPassword(@Nonnull String password) {
		this.password = password;
	}

	@Nonnull
	public String getNachname() {
		return nachname;
	}

	public void setNachname(@Nonnull String nachname) {
		this.nachname = nachname;
	}

	@Nonnull
	public String getVorname() {
		return vorname;
	}

	public void setVorname(@Nonnull String vorname) {
		this.vorname = vorname;
	}

	@Nonnull
	public String getEmail() {
		return email;
	}

	public void setEmail(@Nonnull String email) {
		this.email = email;
	}

	public JaxMandant getMandant() {
		return mandant;
	}

	public void setMandant(JaxMandant mandant) {
		this.mandant = mandant;
	}

	@Nonnull
	public BenutzerStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull BenutzerStatus status) {
		this.status = status;
	}

	@Nullable
	public JaxBerechtigung getCurrentBerechtigung() {
		return currentBerechtigung;
	}

	public void setCurrentBerechtigung(@Nonnull JaxBerechtigung currentBerechtigung) {
		this.currentBerechtigung = currentBerechtigung;
	}

	public Set<JaxBerechtigung> getBerechtigungen() {
		return berechtigungen;
	}

	public void setBerechtigungen(Set<JaxBerechtigung> berechtigungen) {
		this.berechtigungen = berechtigungen;
	}

	@Nonnull
	public UserRole getRole() {
		return getCurrentBerechtigung().getRole();
	}

	@Nullable
	public JaxInstitution getInstitution() {
		return getCurrentBerechtigung().getInstitution();
	}

	@Nullable
	public JaxTraegerschaft getTraegerschaft() {
		return getCurrentBerechtigung().getTraegerschaft();
	}

	/**
	 * evaluates current berechtigung and sets it to the object
	 */
	public void evaluateCurrentBerechtigung() {
		if (getCurrentBerechtigung() == null) {
			for (JaxBerechtigung berechtigung : getBerechtigungen()) {
				if (berechtigung.isGueltig()) {
					setCurrentBerechtigung(berechtigung);
					return;
				}
			}
		}
	}
}
