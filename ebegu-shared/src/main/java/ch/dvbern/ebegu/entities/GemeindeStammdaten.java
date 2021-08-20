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

package ch.dvbern.ebegu.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.validators.CheckIBANUppercase;
import ch.dvbern.ebegu.validators.CheckKontodatenGemeinde;
import ch.dvbern.ebegu.validators.ExternalClientOfType;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.enums.ExternalClientType.GEMEINDE_SCOLARIS_SERVICE;
import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.ONE_MB;

@Audited
@Entity
@Table (
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "gemeinde_id", name = "UK_gemeinde_stammdaten_gemeinde_id"),
		@UniqueConstraint(columnNames = "adresse_id", name = "UK_gemeinde_stammdaten_adresse_id"),
		@UniqueConstraint(columnNames = "rechtsmittelbelehrung_id", name = "UK_rechtsmittelbelehrung_id"),
		@UniqueConstraint(columnNames = "bg_adresse_id", name = "UK_gemeinde_stammdaten_bg_adresse_id"),
		@UniqueConstraint(columnNames = "ts_adresse_id", name = "UK_gemeinde_stammdaten_ts_adresse_id")
	}
)
@CheckKontodatenGemeinde
public class GemeindeStammdaten extends AbstractEntity {

	private static final long serialVersionUID = -6627279554105679587L;
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	@Nullable
	@OneToOne(optional = true, orphanRemoval = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_defaultbenutzer_id"), nullable = true)
	private Benutzer defaultBenutzer;

	@Nullable
	@OneToOne(optional = true, orphanRemoval = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_defaultbenutzerbg_id"), nullable = true)
	private Benutzer defaultBenutzerBG;

	@Nullable
	@OneToOne(optional = true, orphanRemoval = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_defaultbenutzerts_id"), nullable = true)
	private Benutzer defaultBenutzerTS;

	@NotNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_gemeinde_id"), nullable = false)
	private Gemeinde gemeinde;

	@NotNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_adresse_id"), nullable = false)
	private Adresse adresse;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_bg_adresse_id"), nullable = true)
	private Adresse bgAdresse;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_ts_adresse_id"), nullable = true)
	private Adresse tsAdresse;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_beschwerdeadresse_id"), nullable = true)
	private Adresse beschwerdeAdresse;

	@NotNull
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String mail;

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{validator.constraints.phonenumber.message}")
	private String telefon;

	@Nullable
	@Pattern(regexp = Constants.REGEX_URL, message = "{validator.constraints.url.message}")
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	private String webseite;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private KorrespondenzSpracheTyp korrespondenzsprache = KorrespondenzSpracheTyp.DE;

	@Nullable
	@Column(nullable = true, length = ONE_MB) // 1 megabytes
	@Lob
	private byte[] logoContent;

	@Nullable
	@Column(nullable = true)
	private String logoName;

	@Nullable
	@Column(nullable = true)
	private String logoType;

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String kontoinhaber; // TODO (team) evt. spaeter limitieren auf 70

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String bic;

	@Nullable
	@Column(nullable = true)
	@Embedded
	@CheckIBANUppercase
	@Valid
	private IBAN iban;

	@NotNull
	@Column(nullable = false)
	private Boolean standardRechtsmittelbelehrung = true;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_rechtsmittelbelehrung_id"))
	private TextRessource rechtsmittelbelehrung;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private Boolean benachrichtigungBgEmailAuto = true;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private Boolean benachrichtigungTsEmailAuto = true;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private Boolean standardDokSignature = true;

	@Nullable
	@Column(nullable = true)
	private String standardDokTitle;

	@Nullable
	@Column(nullable = true)
	private String standardDokUnterschriftTitel;

	@Nullable
	@Column(nullable = true)
	private String standardDokUnterschriftName;

	@Nullable
	@Column(nullable = true)
	private String standardDokUnterschriftTitel2;

	@Nullable
	@Column(nullable = true)
	private String standardDokUnterschriftName2;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private Boolean tsVerantwortlicherNachVerfuegungBenachrichtigen = false;

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String usernameScolaris;

	@Nullable
	@Column(nullable = true)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{validator.constraints.phonenumber.message}")
	private String bgTelefon;

	@Nullable
	@Column(nullable = true)
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.email.message}")
	private String bgEmail;

	@Nullable
	@Column(nullable = true)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{validator.constraints.phonenumber.message}")
	private String tsTelefon;

	@Nullable
	@Column(nullable = true)
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.email.message}")
	private String tsEmail;

	@Nonnull
	@Column(nullable = false)
	@NotNull
	private Boolean emailBeiGesuchsperiodeOeffnung = false;

	@Nonnull
	@Column(nullable = false)
	@NotNull
	private Boolean gutscheinSelberAusgestellt = true;


	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeinde_stammdaten_gemeinde_id"), nullable = true)
	private Gemeinde gemeindeAusgabestelle;

	@Nonnull
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "gemeinde_stammdaten_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "external_client_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_gemeinde_stammdaten_external_clients_gemeinde_stammdaten_id"),
		inverseForeignKey = @ForeignKey(name = "FK_gemeinde_stammdaten_external_clients_external_client_id")
	)
	private @Valid @NotNull Set<@ExternalClientOfType(type = GEMEINDE_SCOLARIS_SERVICE)ExternalClient> externalClients =
		new HashSet<>();

	@Nullable
	public Benutzer getDefaultBenutzerBG() {
		return defaultBenutzerBG;
	}

	public void setDefaultBenutzerBG(@Nullable Benutzer defaultBenutzerBG) {
		this.defaultBenutzerBG = defaultBenutzerBG;
	}

	@Nullable
	public Benutzer getDefaultBenutzerTS() {
		return defaultBenutzerTS;
	}

	public void setDefaultBenutzerTS(@Nullable Benutzer defaultBenutzerTS) {
		this.defaultBenutzerTS = defaultBenutzerTS;
	}

	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull Adresse adresse) {
		this.adresse = adresse;
	}

	@Nullable
	public Adresse getBeschwerdeAdresse() {
		return beschwerdeAdresse;
	}

	public void setBeschwerdeAdresse(@Nullable Adresse beschwerdeAdresse) {
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

	@Nonnull
	public KorrespondenzSpracheTyp getKorrespondenzsprache() {
		return korrespondenzsprache;
	}

	public void setKorrespondenzsprache(@Nonnull KorrespondenzSpracheTyp korrespondenzsprache) {
		this.korrespondenzsprache = korrespondenzsprache;
	}

	@Nonnull
	public byte[] getLogoContent() {
		if (logoContent == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return Arrays.copyOf(logoContent, logoContent.length);
	}

	public void setLogoContent(@Nullable byte[] logoContent) {
		if (logoContent == null) {
			this.logoContent = null;
		} else {
			this.logoContent = Arrays.copyOf(logoContent, logoContent.length);
		}
	}

	@Nullable
	public String getLogoName() {
		return logoName;
	}

	public void setLogoName(@Nullable String logoName) {
		this.logoName = logoName;
	}

	@Nullable
	public String getLogoType() {
		return logoType;
	}

	public void setLogoType(@Nullable String logoType) {
		this.logoType = logoType;
	}

	@Nullable
	@SuppressFBWarnings("NM_CONFUSING")
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	@SuppressFBWarnings("NM_CONFUSING")
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
	public IBAN getIban() {
		return iban;
	}

	public void setIban(@Nullable IBAN iban) {
		this.iban = iban;
	}

	@Nonnull
	public Boolean getStandardRechtsmittelbelehrung() {
		return standardRechtsmittelbelehrung;
	}

	public void setStandardRechtsmittelbelehrung(@Nonnull Boolean beschwerdeStandardtext) {
		this.standardRechtsmittelbelehrung = beschwerdeStandardtext;
	}

	@Nullable
	public TextRessource getRechtsmittelbelehrung() {
		return rechtsmittelbelehrung;
	}

	public void setRechtsmittelbelehrung(@Nullable TextRessource rechtsmittelbelehrung) {
		this.rechtsmittelbelehrung = rechtsmittelbelehrung;
	}

	@Nullable
	public Benutzer getDefaultBenutzer() {
		return defaultBenutzer;
	}

	public void setDefaultBenutzer(@Nullable Benutzer defaultBenutzer) {
		this.defaultBenutzer = defaultBenutzer;
	}

	@Nullable
	public Adresse getBgAdresse() {
		return bgAdresse;
	}

	public void setBgAdresse(@Nullable Adresse bgAdresse) {
		this.bgAdresse = bgAdresse;
	}

	@Nullable
	public Adresse getTsAdresse() {
		return tsAdresse;
	}

	public void setTsAdresse(@Nullable Adresse tsAdresse) {
		this.tsAdresse = tsAdresse;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof GemeindeStammdaten)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		GemeindeStammdaten gemeindeStammdaten = (GemeindeStammdaten) other;
		return Objects.equals(this.getGemeinde(), gemeindeStammdaten.getGemeinde());
	}

	public boolean isZahlungsinformationValid() {
		return StringUtils.isNotEmpty(kontoinhaber)
			&& StringUtils.isNotEmpty(bic)
			&& iban != null
			&& StringUtils.isNotEmpty(iban.getIban());
	}

	/**
	 * Fuer *reine* BG-Angebote verwenden wir die BG-Adresse (falls gesetzt), sonst die Allgemeine Adresse
	 * Fuer *reine* TS-Angebote verwenden wir die TS-Adresse (falls gesetzt), sonst die Allgemeine Adresse
	 * In allen anderen Faellen (inkl. gar keine Kinder oder Betreuungen) die Allgemeine Adresse
	 */
	@Nonnull
	public Adresse getAdresseForGesuch(@Nonnull Gesuch gesuch) {
		if (bgAdresse != null && gesuch.hasOnlyBetreuungenOfJugendamt()) {
			return bgAdresse;
		}
		if (tsAdresse != null && gesuch.hasOnlyBetreuungenOfSchulamt()) {
			return tsAdresse;
		}
		return adresse;
	}

	/**
	 * Fuer *reine* BG-Angebote verwenden wir die BG Email (falls gesetzt), sonst die allgemeinen Angaben
	 * Fuer *reine* TS-Angebote verwenden wir die TS Email (falls gesetzt), sonst die allgemeinen Angaben
	 * In allen anderen Faellen (inkl. gar keine Kinder oder Betreuungen) die allgemeinen Angaben
	 */
	public String getEmailForGesuch(Gesuch gesuch) {
		if (bgEmail != null && !bgEmail.equals("") && gesuch.hasOnlyBetreuungenOfJugendamt()) {
			return bgEmail;
		}
		if (tsEmail != null && !tsEmail.equals("") && gesuch.hasOnlyBetreuungenOfSchulamt()) {
			return tsEmail;
		}
		return mail;
	}

	/**
	 * Fuer *reine* BG-Angebote verwenden wir die BG Telefonnummer (falls gesetzt), sonst die allgemeinen Angaben
	 * Fuer *reine* TS-Angebote verwenden wir die TS Telefonnummer (falls gesetzt), sonst die allgemeinen Angaben
	 * In allen anderen Faellen (inkl. gar keine Kinder oder Betreuungen) die allgemeinen Angaben
	 */
	public String getTelefonForGesuch(Gesuch gesuch) {
		if (bgTelefon != null && !bgTelefon.equals("") && gesuch.hasOnlyBetreuungenOfJugendamt()) {
			return bgTelefon;
		}
		if (tsTelefon != null && !tsTelefon.equals("") && gesuch.hasOnlyBetreuungenOfSchulamt()) {
			return tsTelefon;
		}
		return telefon;
	}

	/**
	 * Wir suchen einen Benutzer aufgrund der Betreuungen des übergebenen Gesuchs.
	 * Falls *reines* BG Gesuch verwenden wir den BG-Benutzer, falls dieser die richtige Rolle hat
	 * Falls *reines* TS Gesuch verwenden wir den TS-Benutzer, falls dieser die richtige Rolle hat
	 * In allen anderen Fällen den Allgemeinen Benutzer
	 */
	public Optional<Benutzer> getDefaultBenutzerForGesuch(@Nonnull Gesuch gesuch) {
		if (gesuch.hasOnlyBetreuungenOfJugendamt()
				&& defaultBenutzerBG != null && defaultBenutzerBG.getRole().isRoleGemeindeOrBG()) {
			return Optional.of(defaultBenutzerBG);
		}
		if (gesuch.hasOnlyBetreuungenOfSchulamt()
				&& defaultBenutzerTS != null && defaultBenutzerTS.getRole().isRoleGemeindeOrTS()) {
			return Optional.of(defaultBenutzerTS);
		}
		return Optional.ofNullable(defaultBenutzer);
	}

	/**
	 * Wir suchen einen Defaultbenutzer mit der Rolle BG oder GEMEINDE, falls ein spezifischer gesetzt ist
	 * in defaultBenutzerBG, so verwenden wir diesen, sonst pruefen wir, ob der allgemeine Defaultbenutzer
	 * zufaellig die gewuenschte Rolle hat.
	 * Achtung: Diese Methode ist aehnlich auch auf dem Client vorhanden
	 */
	@Nonnull
	public Optional<Benutzer> getDefaultBenutzerWithRoleBG() {
		if (defaultBenutzerBG != null && defaultBenutzerBG.getRole().isRoleGemeindeOrBG()) {
			return Optional.ofNullable(defaultBenutzerBG);
		}
		if (defaultBenutzer != null && defaultBenutzer.getRole().isRoleGemeindeOrBG()) {
			return Optional.ofNullable(defaultBenutzer);
		}
		return Optional.empty();
	}

	/**
	 * Wir suchen einen Defaultbenutzer mit der Rolle TS oder GEMEINDE, falls ein spezifischer gesetzt ist
	 * in defaultBenutzerTS, so verwenden wir diesen, sonst pruefen wir, ob der allgemeine Defaultbenutzer
	 * zufaellig die gewuenschte Rolle hat.
	 * Achtung: Diese Methode ist aehnlich auch auf dem Client vorhanden
	 */
	@Nonnull
	public Optional<Benutzer> getDefaultBenutzerWithRoleTS() {
		if (defaultBenutzerTS != null && defaultBenutzerTS.getRole().isRoleGemeindeOrTS()) {
			return Optional.ofNullable(defaultBenutzerTS);
		}
		if (defaultBenutzer != null && defaultBenutzer.getRole().isRoleGemeindeOrTS()) {
			return Optional.ofNullable(defaultBenutzer);
		}
		return Optional.empty();
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

	@Nonnull
	public Boolean getStandardDokSignature() {
		return standardDokSignature;
	}

	public void setStandardDokSignature(@Nonnull Boolean standardDokSignature) {
		this.standardDokSignature = standardDokSignature;
	}

	@Nonnull
	public Boolean getTsVerantwortlicherNachVerfuegungBenachrichtigen() {
		return tsVerantwortlicherNachVerfuegungBenachrichtigen;
	}

	public void setTsVerantwortlicherNachVerfuegungBenachrichtigen(@Nonnull Boolean tsVerantwortlicherNachVerfuegungBenachrichtigen) {
		this.tsVerantwortlicherNachVerfuegungBenachrichtigen = tsVerantwortlicherNachVerfuegungBenachrichtigen;
	}

	@Nullable
	public String getUsernameScolaris() {
		return usernameScolaris;
	}

	public void setUsernameScolaris(@Nullable String usernameScolaris) {
		this.usernameScolaris = usernameScolaris;
	}

	/**
	 * The data of this institution can be accessed by any ExternalClient in this set. E.g. via the exchange service
	 */
	@Nonnull
	public Set<ExternalClient> getExternalClients() {
		return externalClients;
	}

	public void setExternalClients(@Nonnull Set<ExternalClient> externalClients) {
		this.externalClients = externalClients;
	}

	@Nullable
	public String getBgTelefon() {
		return bgTelefon;
	}

	public void setBgTelefon(@Nullable String bgTelefon) {
		this.bgTelefon = bgTelefon;
	}

	@Nullable
	public String getBgEmail() {
		return bgEmail;
	}

	public void setBgEmail(@Nullable String bgEmail) {
		this.bgEmail = bgEmail;
	}

	@Nullable
	public String getTsTelefon() {
		return tsTelefon;
	}

	public void setTsTelefon(@Nullable String tsTelefon) {
		this.tsTelefon = tsTelefon;
	}

	@Nullable
	public String getTsEmail() {
		return tsEmail;
	}

	public void setTsEmail(@Nullable String tsEmail) {
		this.tsEmail = tsEmail;
	}

	@Nonnull
	public Boolean getEmailBeiGesuchsperiodeOeffnung() {
		return emailBeiGesuchsperiodeOeffnung;
	}

	public void setEmailBeiGesuchsperiodeOeffnung(@Nonnull Boolean emailBeiGesuchsperiodeOeffnung) {
		this.emailBeiGesuchsperiodeOeffnung = emailBeiGesuchsperiodeOeffnung;
	}

	@Nonnull
	public Boolean getGutscheinSelberAusgestellt() {
		return gutscheinSelberAusgestellt;
	}

	public void setGutscheinSelberAusgestellt(@Nonnull Boolean gutscheinSelberAusgestellt) {
		this.gutscheinSelberAusgestellt = gutscheinSelberAusgestellt;
	}

	@Nullable
	public Gemeinde getGemeindeAusgabestelle() {
		return gemeindeAusgabestelle;
	}

	public void setGemeindeAusgabestelle(@Nullable Gemeinde gemeindeAusgabestelle) {
		this.gemeindeAusgabestelle = gemeindeAusgabestelle;
	}
}
