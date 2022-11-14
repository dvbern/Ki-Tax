/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.util.Constants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entity fuer gesuchstellerdaten
 */
@Audited
@Entity
public class Gesuchsteller extends AbstractPersonEntity {

	private static final long serialVersionUID = -9032257320578372570L;

	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Nullable
	@Column(nullable = true)
	private String mail;

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON_MOBILE, message = "{error_invalid_mobilenummer}")
	private String mobile;

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{error_invalid_mobilenummer}")
	private String telefon;

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String telefonAusland;

	@NotNull
	private boolean diplomatenstatus;

	@Nullable
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private Sprache korrespondenzSprache;

	@Nullable
	@Column
	private String zpvNummer = null;


	public Gesuchsteller() {
	}

	@Nullable
	public String getMail() {
		return mail;
	}

	public void setMail(@Nullable final String mail) {
		this.mail = mail;
	}

	@Nullable
	public String getMobile() {
		return mobile;
	}

	public void setMobile(@Nullable final String mobile) {
		this.mobile = mobile;
	}

	@Nullable
	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(@Nullable final String telefon) {
		this.telefon = telefon;
	}

	@Nullable
	public String getTelefonAusland() {
		return telefonAusland;
	}

	public void setTelefonAusland(@Nullable final String telefonAusland) {
		this.telefonAusland = telefonAusland;
	}

	public boolean isDiplomatenstatus() {
		return diplomatenstatus;
	}

	public void setDiplomatenstatus(final boolean diplomatenstatus) {
		this.diplomatenstatus = diplomatenstatus;
	}

	@Nullable
	public Sprache getKorrespondenzSprache() {
		return korrespondenzSprache;
	}

	public void setKorrespondenzSprache(@Nullable Sprache korrespondenzSprachen) {
		this.korrespondenzSprache = korrespondenzSprachen;
	}

	@Nonnull
	public Gesuchsteller copyGesuchsteller(@Nonnull Gesuchsteller target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractPersonEntity(target, copyType);
		target.setMail(this.getMail());
		target.setMobile(this.getMobile());
		target.setTelefon(this.getTelefon());
		target.setTelefonAusland(this.getTelefonAusland());
		target.setDiplomatenstatus(this.isDiplomatenstatus());
		target.setKorrespondenzSprache(this.getKorrespondenzSprache());
		target.setZpvNummer(this.getZpvNummer());
		return target;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		final Gesuchsteller otherGesuchsteller = (Gesuchsteller) other;
		return Objects.equals(getMail(), otherGesuchsteller.getMail()) &&
			Objects.equals(getMobile(), otherGesuchsteller.getMobile()) &&
			Objects.equals(getTelefon(), otherGesuchsteller.getTelefon()) &&
			Objects.equals(getTelefonAusland(), otherGesuchsteller.getTelefonAusland()) &&
			Objects.equals(isDiplomatenstatus(), otherGesuchsteller.isDiplomatenstatus()) &&
			getKorrespondenzSprache() == otherGesuchsteller.getKorrespondenzSprache();
	}

	@Nullable
	public String getZpvNummer() {
		return zpvNummer;
	}

	public void setZpvNummer(@Nullable String zpvNummer) {
		this.zpvNummer = zpvNummer;
	}
}
