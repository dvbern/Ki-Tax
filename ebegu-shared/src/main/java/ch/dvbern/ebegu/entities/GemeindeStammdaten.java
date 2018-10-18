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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.TEN_MEG;

@Audited
@Entity
@Table
public class GemeindeStammdaten extends AbstractEntity {

	private static final long serialVersionUID = -6627279554105679587L;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_defaultbenutzerbg_id"), nullable = true)
	private Benutzer defaultBenutzerBG;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
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
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_beschwerdeadresse_id"), nullable = false)
	private Adresse beschwerdeAdresse;

	// todo KIBON-245 braucht man das? koennte man es nicht direkt setzen wenn die adresse existiert?
	@NotNull
	@Column(nullable = false)
	private boolean keineBeschwerdeAdresse = true;

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
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	private String webseite;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private KorrespondenzSpracheTyp korrespondenzsprache = KorrespondenzSpracheTyp.DE;

	@Nullable
	@Column(nullable = false, length = TEN_MEG) //10 megabytes // todo KIBON-245 ist es nicht viel?
	@Lob
	private byte[] logoContent;


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

	public boolean isKeineBeschwerdeAdresse() {
		return keineBeschwerdeAdresse;
	}

	public void setKeineBeschwerdeAdresse(boolean keineBeschwerdeAdresse) {
		this.keineBeschwerdeAdresse = keineBeschwerdeAdresse;
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

	@Nullable
	public byte[] getLogoContent() {
		return logoContent;
	}

	public void setLogoContent(@Nullable byte[] logoContent) {
		if (logoContent == null) {
			this.logoContent = null;
		} else {
			this.logoContent = Arrays.copyOf(logoContent, logoContent.length);
		}
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

}
