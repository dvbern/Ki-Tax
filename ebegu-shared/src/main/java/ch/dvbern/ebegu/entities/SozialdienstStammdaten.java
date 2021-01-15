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

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Audited
@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "sozialdienst_id", name = "UK_sozialdienst_stammdaten_sozialdienst_id"),
		@UniqueConstraint(columnNames = "adresse_id", name = "UK_sozialdienst_stammdaten_adresse_id")
	}
)
public class SozialdienstStammdaten extends AbstractEntity {

	private static final long serialVersionUID = -4083405024633687668L;

	@NotNull @Nonnull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_sozialdienststammdaten_sozialdienst_id"), nullable = false)
	private Sozialdienst sozialdienst;

	@NotNull @Nonnull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_sozialdienststammdaten_adresse_id"), nullable = false)
	private Adresse adresse;

	@NotNull @Nonnull
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String mail;

	@NotNull @Nonnull
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{validator.constraints.phonenumber.message}")
	private String telefon;

	@Nullable @Nonnull
	@Pattern(regexp = Constants.REGEX_URL, message = "{validator.constraints.url.message}")
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	private String webseite;

	@Nonnull
	public Sozialdienst getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(@Nonnull Sozialdienst sozialdienst) {
		this.sozialdienst = sozialdienst;
	}

	@Nonnull
	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull Adresse adresse) {
		this.adresse = adresse;
	}

	@Nonnull
	public String getMail() {
		return mail;
	}

	public void setMail(@Nonnull String mail) {
		this.mail = mail;
	}

	@Nonnull
	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(@Nonnull String telefon) {
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
		if (!(other instanceof SozialdienstStammdaten)){
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
	SozialdienstStammdaten sozialdienstStammdaten = (SozialdienstStammdaten) other;
		return Objects.equals(this.getSozialdienst(), sozialdienstStammdaten.getSozialdienst());
	}
}
