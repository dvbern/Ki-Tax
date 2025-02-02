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

package ch.dvbern.ebegu.entities.sozialdienst;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.util.Constants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

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
	@Email
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String mail;

	@Nullable
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{validator.constraints.phonenumber.message}")
	private String telefon;

	@Nullable
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
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
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
