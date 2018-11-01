/*
 * AGPL File-Header
 *
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by imanol on 17.03.16.
 * DTO fuer InstitutionStammdaten
 */
@XmlRootElement(name = "gemeindeStammdaten")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxGemeindeStammdaten extends JaxAbstractDTO {

	private static final long serialVersionUID = -1893677808323618626L;
	@Nullable
	private String administratoren;
	@Nullable
	private String sachbearbeiter;
	@Nullable
	private JaxBenutzer defaultBenutzerBG; // Der Standardverantwortliche
	@Nullable
	private JaxBenutzer defaultBenutzerTS;
	@NotNull
	private JaxGemeinde gemeinde;
	@NotNull
	private JaxAdresse adresse;
	@Nullable
	private JaxAdresse beschwerdeAdresse;
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
	@Nullable
	private List<JaxBenutzer> benutzerListeBG; // Für die ComboBox Standardverantwortliche BG
	@Nullable
	private List<JaxBenutzer> benutzerListeTS; // Für die ComboBox Standardverantwortliche TS
	// ---------- Konfiguration ----------
	@NotNull
	private List<JaxGemeindeKonfiguration> konfigurationsListe = new ArrayList<>();


	@Nullable
	public String getAdministratoren() {
		return administratoren;
	}

	public void setAdministratoren(@Nullable String administratoren) {
		this.administratoren = administratoren;
	}

	@Nullable
	public String getSachbearbeiter() {
		return sachbearbeiter;
	}

	public void setSachbearbeiter(@Nullable String sachbearbeiter) {
		this.sachbearbeiter = sachbearbeiter;
	}

	@Nullable
	public JaxBenutzer getDefaultBenutzerBG() {
		return defaultBenutzerBG;
	}

	public void setDefaultBenutzerBG(@Nullable JaxBenutzer defaultBenutzerBG) {
		this.defaultBenutzerBG = defaultBenutzerBG;
	}

	@Nullable
	public JaxBenutzer getDefaultBenutzerTS() {
		return defaultBenutzerTS;
	}

	public void setDefaultBenutzerTS(@Nullable JaxBenutzer defaultBenutzerTS) {
		this.defaultBenutzerTS = defaultBenutzerTS;
	}

	public JaxGemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(JaxGemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	public JaxAdresse getAdresse() {
		return adresse;
	}

	public void setAdresse(JaxAdresse adresse) {
		this.adresse = adresse;
	}

	@Nullable
	public JaxAdresse getBeschwerdeAdresse() {
		return beschwerdeAdresse;
	}

	public void setBeschwerdeAdresse(@Nullable JaxAdresse beschwerdeAdresse) {
		this.beschwerdeAdresse = beschwerdeAdresse;
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

	@Nullable
	public List<JaxBenutzer> getBenutzerListeBG() {
		return benutzerListeBG;
	}

	public void setBenutzerListeBG(@Nullable List<JaxBenutzer> benutzerListeBG) {
		this.benutzerListeBG = benutzerListeBG;
	}

	@Nullable
	public List<JaxBenutzer> getBenutzerListeTS() {
		return benutzerListeTS;
	}

	public void setBenutzerListeTS(@Nullable List<JaxBenutzer> benutzerListeTS) {
		this.benutzerListeTS = benutzerListeTS;
	}

	public List<JaxGemeindeKonfiguration> getKonfigurationsListe() {
		return konfigurationsListe;
	}

	public void setKonfigurationsListe(List<JaxGemeindeKonfiguration> konfigurationsListe) {
		this.konfigurationsListe = konfigurationsListe;
	}

}
