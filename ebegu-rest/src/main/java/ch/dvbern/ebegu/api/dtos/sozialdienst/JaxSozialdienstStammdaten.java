/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos.sozialdienst;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxAdresse;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxSozialdienstStammdaten extends JaxAbstractDTO {

	@NotNull
	private JaxSozialdienst sozialdienst;

	@NotNull
	private JaxAdresse adresse;

	@NotNull
	private String mail;

	@NotNull
	private String telefon;

	@Nullable
	private String webseite;

	public JaxSozialdienst getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(JaxSozialdienst sozialdienst) {
		this.sozialdienst = sozialdienst;
	}

	public JaxAdresse getAdresse() {
		return adresse;
	}

	public void setAdresse(JaxAdresse adresse) {
		this.adresse = adresse;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

	@Nullable
	public String getWebseite() {
		return webseite;
	}

	public void setWebseite(@Nullable String webseite) {
		this.webseite = webseite;
	}
}
