/*
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
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

	private static final long serialVersionUID = -1893677816323618626L;
	@Nullable
	private String administratoren;
	@Nullable
	private String sachbearbeiter;
	@Nullable
	private JaxBenutzer defaultBenutzer;
	@Nullable
	private JaxBenutzer defaultBenutzerBG; // Der Standardverantwortliche
	@Nullable
	private JaxBenutzer defaultBenutzerTS;
	@NotNull
	private JaxGemeinde gemeinde;
	@NotNull
	private JaxAdresse adresse;
	@Nullable
	private JaxAdresse bgAdresse;
	@Nullable
	private JaxAdresse tsAdresse;
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
	@Nullable
	private String kontoinhaber;
	@Nullable
	private String bic;
	@Nullable
	private String iban;
	@NotNull
	private Boolean standardRechtsmittelbelehrung;
	@Nullable
	private JaxTextRessource rechtsmittelbelehrung;
	@NotNull
	private Boolean benachrichtigungBgEmailAuto;
	@NotNull
	private Boolean benachrichtigungTsEmailAuto;
	@NotNull
	private Boolean standardDokSignature;
	@Nullable
	private String standardDokTitle;
	@Nullable
	private String standardDokUnterschriftTitel;
	@Nullable
	private String standardDokUnterschriftName;
	@Nullable
	private String standardDokUnterschriftTitel2;
	@Nullable
	private String standardDokUnterschriftName2;
	@Nullable
	private @Valid List<String> externalClients = null;
	@Nullable
	private String usernameScolaris;
	@Nullable
	private String bgEmail;
	@Nullable
	private String bgTelefon;
	@Nullable
	private String tsEmail;
	@Nullable
	private String tsTelefon;
	@NotNull
	private Boolean tsVerantwortlicherNachVerfuegungBenachrichtigen;
	@Nonnull
	private Boolean emailBeiGesuchsperiodeOeffnung;

	// ---------- Konfiguration ----------
	@NotNull
	private List<JaxGemeindeKonfiguration> konfigurationsListe = new ArrayList<>();

	public JaxGemeindeStammdaten() {
	}

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

	@Nullable
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	public void setKontoinhaber(@Nullable String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	@Nullable
	public String getBic() {
		return bic;
	}

	public void setBic(@Nullable String bic) {
		this.bic = bic;
	}

	@Nullable
	public String getIban() {
		return iban;
	}

	public void setIban(@Nullable String iban) {
		this.iban = iban;
	}

	@Nonnull
	public Boolean getStandardRechtsmittelbelehrung() {
		return standardRechtsmittelbelehrung;
	}

	public void setStandardRechtsmittelbelehrung(@Nonnull Boolean standardRechtsmittelbelehrung) {
		this.standardRechtsmittelbelehrung = standardRechtsmittelbelehrung;
	}

	@Nullable
	public JaxTextRessource getRechtsmittelbelehrung() {
		return rechtsmittelbelehrung;
	}

	public void setRechtsmittelbelehrung(@Nullable JaxTextRessource rechtsmittelbelehrung) {
		this.rechtsmittelbelehrung = rechtsmittelbelehrung;
	}

	@Nullable
	public JaxAdresse getBgAdresse() {
		return bgAdresse;
	}

	public void setBgAdresse(@Nullable JaxAdresse bgAdresse) {
		this.bgAdresse = bgAdresse;
	}

	@Nullable
	public JaxAdresse getTsAdresse() {
		return tsAdresse;
	}

	public void setTsAdresse(@Nullable JaxAdresse tsAdresse) {
		this.tsAdresse = tsAdresse;
	}

	@Nullable
	public JaxBenutzer getDefaultBenutzer() {
		return defaultBenutzer;
	}

	public void setDefaultBenutzer(@Nullable JaxBenutzer defaultBenutzer) {
		this.defaultBenutzer = defaultBenutzer;
	}

	@Nonnull
	public Boolean getBenachrichtigungBgEmailAuto() {
		return benachrichtigungBgEmailAuto;
	}

	public void setBenachrichtigungBgEmailAuto(@Nonnull Boolean benachrichtigungBgEmailAuto) {
		this.benachrichtigungBgEmailAuto = benachrichtigungBgEmailAuto;
	}

	@Nonnull
	public Boolean getBenachrichtigungTsEmailAuto() {
		return benachrichtigungTsEmailAuto;
	}

	public void setBenachrichtigungTsEmailAuto(@Nonnull Boolean benachrichtigungTsEmailAuto) {
		this.benachrichtigungTsEmailAuto = benachrichtigungTsEmailAuto;
	}

	public Boolean getStandardDokSignature() {
		return standardDokSignature;
	}

	public void setStandardDokSignature(Boolean standardDokSignature) {
		this.standardDokSignature = standardDokSignature;
	}

	@Nullable
	public String getStandardDokTitle() {
		return standardDokTitle;
	}

	public void setStandardDokTitle(@Nullable String standardDokTitle) {
		this.standardDokTitle = standardDokTitle;
	}

	@Nullable
	public String getStandardDokUnterschriftTitel() {
		return standardDokUnterschriftTitel;
	}

	public void setStandardDokUnterschriftTitel(@Nullable String standardDokUnterschriftTitel) {
		this.standardDokUnterschriftTitel = standardDokUnterschriftTitel;
	}

	@Nullable
	public String getStandardDokUnterschriftName() {
		return standardDokUnterschriftName;
	}

	public void setStandardDokUnterschriftName(@Nullable String standardDokUnterschriftName) {
		this.standardDokUnterschriftName = standardDokUnterschriftName;
	}

	@Nullable
	public String getStandardDokUnterschriftTitel2() {
		return standardDokUnterschriftTitel2;
	}

	public void setStandardDokUnterschriftTitel2(@Nullable String standardDokUnterschriftTitel2) {
		this.standardDokUnterschriftTitel2 = standardDokUnterschriftTitel2;
	}

	@Nullable
	public String getStandardDokUnterschriftName2() {
		return standardDokUnterschriftName2;
	}

	public void setStandardDokUnterschriftName2(@Nullable String standardDokUnterschriftName2) {
		this.standardDokUnterschriftName2 = standardDokUnterschriftName2;
	}

	public Boolean getTsVerantwortlicherNachVerfuegungBenachrichtigen() {
		return tsVerantwortlicherNachVerfuegungBenachrichtigen;
	}

	public void setTsVerantwortlicherNachVerfuegungBenachrichtigen(Boolean tsVerantwortlicherNachVerfuegungBenachrichtigen) {
		this.tsVerantwortlicherNachVerfuegungBenachrichtigen = tsVerantwortlicherNachVerfuegungBenachrichtigen;
	}

	@Nullable
	public List<String> getExternalClients() {
		return externalClients;
	}

	public void setExternalClients(@Nullable List<String> externalClients) {
		this.externalClients = externalClients;
	}

	@Nullable
	public String getUsernameScolaris() {
		return usernameScolaris;
	}

	public void setUsernameScolaris(@Nullable String usernameScolaris) {
		this.usernameScolaris = usernameScolaris;
	}

	@Nullable
	public String getBgEmail() {
		return bgEmail;
	}

	public void setBgEmail(@Nullable String bgEmail) {
		this.bgEmail = bgEmail;
	}

	@Nullable
	public String getBgTelefon() {
		return bgTelefon;
	}

	public void setBgTelefon(@Nullable String bgTelefon) {
		this.bgTelefon = bgTelefon;
	}

	@Nullable
	public String getTsEmail() {
		return tsEmail;
	}

	public void setTsEmail(@Nullable String tsEmail) {
		this.tsEmail = tsEmail;
	}

	@Nullable
	public String getTsTelefon() {
		return tsTelefon;
	}

	public void setTsTelefon(@Nullable String tsTelefon) {
		this.tsTelefon = tsTelefon;
	}

	@Nonnull
	public Boolean getEmailBeiGesuchsperiodeOeffnung() {
		return emailBeiGesuchsperiodeOeffnung;
	}

	public void setEmailBeiGesuchsperiodeOeffnung(@Nonnull Boolean emailBeiGesuchsperiodeOeffnung) {
		this.emailBeiGesuchsperiodeOeffnung = emailBeiGesuchsperiodeOeffnung;
	}
}
