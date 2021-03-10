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

package ch.dvbern.ebegu.reporting.benutzer;

import java.time.LocalDate;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.BenutzerStatus;

public class BenutzerDataRow {

	private String username = null;
	private String vorname = null;
	private String nachname = null;
	private String email = null;
	private String role = null;
	@Nullable
	private LocalDate roleGueltigAb = null;
	@Nullable
	private LocalDate roleGueltigBis = null;
	@Nullable
	private String gemeinden = null;
	@Nullable
	private String angebotGemeinden = null;
	@Nullable
	private String institution = null;
	@Nullable
	private String traegerschaft = null;
	@Nullable
	private BenutzerStatus status = null;
	private Boolean isKita = null;
	private Boolean isTagesfamilien = null;
	private Boolean isTagesschule = null;
	private Boolean isFerieninsel = null;

	public BenutzerDataRow() {
	}

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

	@Nullable
	public LocalDate getRoleGueltigAb() {
		return roleGueltigAb;
	}

	public void setRoleGueltigAb(@Nullable LocalDate roleGueltigAb) {
		this.roleGueltigAb = roleGueltigAb;
	}

	@Nullable
	public LocalDate getRoleGueltigBis() {
		return roleGueltigBis;
	}

	public void setRoleGueltigBis(@Nullable LocalDate roleGueltigBis) {
		this.roleGueltigBis = roleGueltigBis;
	}

	@Nullable
	public String getGemeinden() {
		return gemeinden;
	}

	public void setGemeinden(@Nullable String gemeinden) {
		this.gemeinden = gemeinden;
	}

	@Nullable
	public String getAngebotGemeinden() {
		return angebotGemeinden;
	}

	public void setAngebotGemeinden(@Nullable String angebotGemeinden) {
		this.angebotGemeinden = angebotGemeinden;
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

	public Boolean isKita() {
		return isKita;
	}

	public void setKita(Boolean isKita) {
		this.isKita = isKita;
	}

	public Boolean isTagesschule() {
		return isTagesschule;
	}

	public void setTagesschule(Boolean tagesschule) {
		isTagesschule = tagesschule;
	}

	public Boolean isFerieninsel() {
		return isFerieninsel;
	}

	public void setFerieninsel(Boolean ferieninsel) {
		isFerieninsel = ferieninsel;
	}

	public Boolean isTagesfamilien() {
		return isTagesfamilien;
	}

	public void setTagesfamilien(Boolean tagesfamilien) {
		isTagesfamilien = tagesfamilien;
	}

	public Boolean isJugendamt() {
		return (this.isKita != null && this.isKita)
			|| (this.isTagesfamilien != null && this.isTagesfamilien);
	}

	public Boolean isSchulamt() {
		return (this.isTagesschule != null && this.isTagesschule)
			|| (this.isFerieninsel != null && this.isFerieninsel);
	}
}
