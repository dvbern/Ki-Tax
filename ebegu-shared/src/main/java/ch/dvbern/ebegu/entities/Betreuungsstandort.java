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

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von Betreuungsstandorten in der Datenbank.
 */
@Audited
@Entity
public class Betreuungsstandort extends AbstractMutableEntity {

	private static final long serialVersionUID = -672064202442191630L;

	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuungsstandort_institution_stammdaten_betreuungsgutscheine_id"), nullable = false)
	@ManyToOne(optional = false)
	private @NotNull InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine;

	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_adresse_id"), nullable = false)
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@Nonnull
	private @NotNull Adresse adresse = new Adresse();

	@Column(nullable = true)
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	@Nullable
	private String mail;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Nullable
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{validator.constraints.phonenumber.message}")
	private String telefon;

	@Column(nullable = true)
	@Nullable
	private @Size(max = DB_DEFAULT_MAX_LENGTH) String webseite;

	public InstitutionStammdatenBetreuungsgutscheine getInstitutionStammdatenBetreuungsgutscheine() {
		return institutionStammdatenBetreuungsgutscheine;
	}

	public void setInstitutionStammdatenBetreuungsgutscheine(InstitutionStammdatenBetreuungsgutscheine institutionStammdaten) {
		this.institutionStammdatenBetreuungsgutscheine = institutionStammdaten;
	}

	@Nonnull
	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull Adresse adresse) {
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

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof Betreuungsstandort)) {
			return false;
		}
		final Betreuungsstandort otherBetreuungsstandort = (Betreuungsstandort) other;
		return Objects.equals(getInstitutionStammdatenBetreuungsgutscheine().getId(), otherBetreuungsstandort.getInstitutionStammdatenBetreuungsgutscheine().getId()) &&
			Objects.equals(getAdresse().getId(), otherBetreuungsstandort.getAdresse().getId());
	}
}
