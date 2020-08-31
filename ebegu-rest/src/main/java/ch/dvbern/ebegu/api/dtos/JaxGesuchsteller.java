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

import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * DTO fuer Stammdaten der Gesuchsteller
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxGesuchsteller extends JaxAbstractPersonDTO {

	private static final long serialVersionUID = -1297026901664130397L;

	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Nullable
	private String mail;

	@Pattern(regexp = Constants.REGEX_TELEFON_MOBILE, message = "{error_invalid_mobilenummer}")
	@Nullable
	private String mobile;

	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{error_invalid_mobilenummer}")
	@Nullable
	private String telefon;

	@Nullable
	private String telefonAusland;

	private boolean diplomatenstatus;

	@Nullable
	private Sprache korrespondenzSprache;

	@Nullable
	public String getMail() {
		return mail;
	}

	public void setMail(@Nullable final String mail) {
		this.mail = mail;
	}

	@Nullable
	public String getMobile() {
		return mobile;
	}

	public void setMobile(@Nullable final String mobile) {
		this.mobile = mobile;
	}

	@Nullable
	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(@Nullable final String telefon) {
		this.telefon = telefon;
	}

	@Nullable
	public String getTelefonAusland() {
		return telefonAusland;
	}

	public void setTelefonAusland(@Nullable final String telefonAusland) {
		this.telefonAusland = telefonAusland;
	}

	public boolean isDiplomatenstatus() {
		return diplomatenstatus;
	}

	public void setDiplomatenstatus(final boolean diplomatenstatus) {
		this.diplomatenstatus = diplomatenstatus;
	}

	@Nullable
	public Sprache getKorrespondenzSprache() {
		return korrespondenzSprache;
	}

	public void setKorrespondenzSprache(@Nullable Sprache korrespondenzSprache) {
		this.korrespondenzSprache = korrespondenzSprache;
	}
}
