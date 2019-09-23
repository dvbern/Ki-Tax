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
import java.util.Objects;
import java.util.Optional;

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
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;

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

	@NotNull
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String kontoinhaber;

	@NotNull
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String bic;

	@NotNull
	@Column(nullable = false)
	@Embedded
	@Valid
	private IBAN iban;

	@NotNull
	@Column(nullable = false)
	private Boolean standardRechtsmittelbelehrung = true;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_rechtsmittelbelehrung_id"))
	private TextRessource rechtsmittelbelehrung;


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

	@SuppressFBWarnings("NM_CONFUSING")
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	public void setKontoinhaber(String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	public String getBic() {
		return bic;
	}

	public void setBic(String bic) {
		this.bic = bic;
	}

	public IBAN getIban() {
		return iban;
	}

	public void setIban(IBAN iban) {
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
		return StringUtils.isNotEmpty(kontoinhaber) && StringUtils.isNotEmpty(bic) && StringUtils.isNotEmpty(iban.getIban());
	}

	/**
	 * Fuer *reine* BG-Angebote verwenden wir die BG-Adresse (falls gesetzt), sonst die Allgemeine Adresse
	 * Fuer *reine* TS-Angebote verwenden wir die TS-Adresse (falls gesetzt), sonst die Allgemeine Adresse
	 * In allen anderen Faellen (inkl. gar keine Kinder oder Betreuungen) die Allgemeine Adresse
	 */
	@Nonnull
	public Adresse getAdresseForGesuch(@Nonnull Gesuch gesuch) {
		if (gesuch.hasOnlyBetreuungenOfJugendamt() && bgAdresse != null) {
			return bgAdresse;
		}
		if (gesuch.hasOnlyBetreuungenOfSchulamt() && tsAdresse != null) {
			return tsAdresse;
		}
		return adresse;
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
}
