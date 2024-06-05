/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos;

import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxAlwaysEditableProperties {

	private JaxId  gesuchId;

	@Email
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Nullable
	private String mailGS1;

	@Pattern(regexp = Constants.REGEX_TELEFON_MOBILE, message = "{error_invalid_mobilenummer}")
	@Nullable
	private String mobileGS1;

	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{error_invalid_mobilenummer}")
	@Nullable
	private String telefonGS1;

	@Nullable
	private String telefonAuslandGS1;

	@Email
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Nullable
	private String mailGS2;

	@Pattern(regexp = Constants.REGEX_TELEFON_MOBILE, message = "{error_invalid_mobilenummer}")
	@Nullable
	private String mobileGS2;

	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{error_invalid_mobilenummer}")
	@Nullable
	private String telefonGS2;

	@Nullable
	private String telefonAuslandGS2;

	private boolean keineMahlzeitenverguenstigungBeantragt;

	@Nullable
	private String iban;

	@Nullable
	private String kontoinhaber;

	private boolean abweichendeZahlungsadresse;

	@Nullable
	private JaxAdresse zahlungsadresse;

	public JaxId getGesuchId() {
		return gesuchId;
	}

	public void setGesuchId(JaxId gesuchId) {
		this.gesuchId = gesuchId;
	}

	@Nullable
	public String getMailGS1() {
		return mailGS1;
	}

	public void setMailGS1(@Nullable String mailGS1) {
		this.mailGS1 = mailGS1;
	}

	@Nullable
	public String getMobileGS1() {
		return mobileGS1;
	}

	public void setMobileGS1(@Nullable String mobileGS1) {
		this.mobileGS1 = mobileGS1;
	}

	@Nullable
	public String getTelefonGS1() {
		return telefonGS1;
	}

	public void setTelefonGS1(@Nullable String telefonGS1) {
		this.telefonGS1 = telefonGS1;
	}

	@Nullable
	public String getTelefonAuslandGS1() {
		return telefonAuslandGS1;
	}

	public void setTelefonAuslandGS1(@Nullable String telefonAuslandGS1) {
		this.telefonAuslandGS1 = telefonAuslandGS1;
	}

	@Nullable
	public String getMailGS2() {
		return mailGS2;
	}

	public void setMailGS2(@Nullable String mailGS2) {
		this.mailGS2 = mailGS2;
	}

	@Nullable
	public String getMobileGS2() {
		return mobileGS2;
	}

	public void setMobileGS2(@Nullable String mobileGS2) {
		this.mobileGS2 = mobileGS2;
	}

	@Nullable
	public String getTelefonGS2() {
		return telefonGS2;
	}

	public void setTelefonGS2(@Nullable String telefonGS2) {
		this.telefonGS2 = telefonGS2;
	}

	@Nullable
	public String getTelefonAuslandGS2() {
		return telefonAuslandGS2;
	}

	public void setTelefonAuslandGS2(@Nullable String telefonAuslandGS2) {
		this.telefonAuslandGS2 = telefonAuslandGS2;
	}

	public boolean isKeineMahlzeitenverguenstigungBeantragt() {
		return keineMahlzeitenverguenstigungBeantragt;
	}

	public void setKeineMahlzeitenverguenstigungBeantragt(boolean keineMahlzeitenverguenstigungBeantragt) {
		this.keineMahlzeitenverguenstigungBeantragt = keineMahlzeitenverguenstigungBeantragt;
	}

	@Nullable
	public String getIban() {
		return iban;
	}

	public void setIban(@Nullable String iban) {
		this.iban = iban;
	}

	@Nullable
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	public void setKontoinhaber(@Nullable String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	public boolean isAbweichendeZahlungsadresse() {
		return abweichendeZahlungsadresse;
	}

	public void setAbweichendeZahlungsadresse(boolean abweichendeZahlungsadresse) {
		this.abweichendeZahlungsadresse = abweichendeZahlungsadresse;
	}

	@Nullable
	public JaxAdresse getZahlungsadresse() {
		return zahlungsadresse;
	}

	public void setZahlungsadresse(@Nullable JaxAdresse zahlungsadresse) {
		this.zahlungsadresse = zahlungsadresse;
	}
}
