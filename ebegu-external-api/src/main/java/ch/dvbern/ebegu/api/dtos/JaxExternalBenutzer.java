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

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This Transfer Object is used to pass on Info about an external Benutzer from an external Login
 * to E-BEGU
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxExternalBenutzer implements Serializable {
	private static final long serialVersionUID = -2418503680503363364L;

	private String username = null;
	@Nullable
	private String externalUUID = null;
	private String vorname = null;
	private String nachname = null;
	private String email = null;
	private String institutionId = null;
	private String traegerschaftId = null;
	private String mandantId = null;
	private String role = null;

	private String commonName = null;

	private String zpvNummer = null;

	@Nullable
	private String telephoneNumber = null;
	private String mobile = null;
	private String preferredLang = null;
	private String postalAddress = null;
	private String street = null;
	private String postalCode = null;
	private String state = null;
	private String countryCode = null;
	private String country = null;
	private String invitationLink = null;
	private boolean invitationPending;
	private boolean gesperrt;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Nullable
	public String getExternalUUID() {
		return externalUUID;
	}

	public void setExternalUUID(@Nullable String externalUUID) {
		this.externalUUID = externalUUID;
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

	public String getMandantId() {
		return mandantId;
	}

	public void setMandantId(String mandantId) {
		this.mandantId = mandantId;
	}

	public String getInstitutionId() {
		return institutionId;
	}

	public void setInstitutionId(String institutionId) {
		this.institutionId = institutionId;
	}

	public String getTraegerschaftId() {
		return traegerschaftId;
	}

	public void setTraegerschaftId(String traegerschaftId) {
		this.traegerschaftId = traegerschaftId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	//unused attributes

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	@Nullable
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(@Nullable String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPreferredLang() {
		return preferredLang;
	}

	public void setPreferredLang(String preferredLang) {
		this.preferredLang = preferredLang;
	}

	public String getPostalAddress() {
		return postalAddress;
	}

	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public boolean isGesperrt() {
		return gesperrt;
	}

	public void setGesperrt(boolean gesperrt) {
		this.gesperrt = gesperrt;
	}

	public String getInvitationLink() {
		return invitationLink;
	}

	public void setInvitationLink(String invitationLink) {
		this.invitationLink = invitationLink;
	}

	public boolean isInvitationPending() {
		return invitationPending;
	}

	public void setInvitationPending(boolean invitationPending) {
		this.invitationPending = invitationPending;
	}

	public String getZpvNummer() {
		return zpvNummer;
	}

	public void setZpvNummer(String zpvNummer) {
		this.zpvNummer = zpvNummer;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("JaxExternalBenutzer{");
		sb.append("username='").append(username).append('\'');
		sb.append(", externalUUID='").append(externalUUID).append('\'');
		sb.append(", vorname='").append(vorname).append('\'');
		sb.append(", nachname='").append(nachname).append('\'');
		sb.append(", email='").append(email).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
