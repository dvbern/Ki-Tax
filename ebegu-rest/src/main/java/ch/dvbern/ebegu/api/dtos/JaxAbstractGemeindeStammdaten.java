/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxAbstractGemeindeStammdaten extends JaxAbstractDTO {

	private static final long serialVersionUID = -4638153440787545634L;

	@NotNull
	private JaxAdresse adresse;
	@NotNull
	private String mail;
	@Nullable
	private String telefon;
	@Nullable
	private String webseite;
	@NotNull
	private boolean korrespondenzspracheDe;
	@NotNull
	private boolean korrespondenzspracheFr;
	@Nonnull
	private Boolean hasAltGemeindeKontakt;
	@Nullable
	private String altGemeindeKontaktText;

	// ---------- Konfiguration ----------
	@NotNull
	private List<JaxGemeindeKonfiguration> konfigurationsListe = new ArrayList<>();

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

	public boolean isKorrespondenzspracheDe() {
		return korrespondenzspracheDe;
	}

	public void setKorrespondenzspracheDe(boolean korrespondenzspracheDe) {
		this.korrespondenzspracheDe = korrespondenzspracheDe;
	}

	public boolean isKorrespondenzspracheFr() {
		return korrespondenzspracheFr;
	}

	public void setKorrespondenzspracheFr(boolean korrespondenzspracheFr) {
		this.korrespondenzspracheFr = korrespondenzspracheFr;
	}

	public List<JaxGemeindeKonfiguration> getKonfigurationsListe() {
		return konfigurationsListe;
	}

	public void setKonfigurationsListe(List<JaxGemeindeKonfiguration> konfigurationsListe) {
		this.konfigurationsListe = konfigurationsListe;
	}

	@Nullable
	public String getAltGemeindeKontaktText() {
		return altGemeindeKontaktText;
	}

	public void setAltGemeindeKontaktText(@Nullable String altGemeindeKontaktText) {
		this.altGemeindeKontaktText = altGemeindeKontaktText;
	}

	@Nonnull
	public Boolean getHasAltGemeindeKontakt() {
		return hasAltGemeindeKontakt;
	}

	public void setHasAltGemeindeKontakt(@Nonnull Boolean hasAltGemeindeKontakt) {
		this.hasAltGemeindeKontakt = hasAltGemeindeKontakt;
	}
}
