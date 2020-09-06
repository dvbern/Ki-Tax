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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer Betreuungsstandorte
 */
@XmlRootElement(name = "betreuungsstandort")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBetreuungsstandort extends JaxAbstractDTO {

	private static final long serialVersionUID = -6496353808356573936L;

	@Nonnull
	private JaxAdresse adresse = new JaxAdresse();

	@Nullable
	private String mail;

	@Nullable
	private String telefon;

	@Nullable
	private String webseite;

	@Nonnull
	public JaxAdresse getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull JaxAdresse adresse) {
		this.adresse = adresse;
	}

	@Nullable
	public String getMail() {
		return mail;
	}

	public void setMail(@Nullable String mail) {
		this.mail = mail;
	}

	@Nullable
	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(@Nullable String telefon) {
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
