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

import java.time.LocalDate;
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
import ch.dvbern.ebegu.util.EbeguUtil;
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

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON_MOBILE, message = "{error_invalid_mobilenummer}")
	private String mobile;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{error_invalid_mobilenummer}")
	private String telefon;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String telefonAusland;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String ewkPersonId;

	@Column(nullable = true)
	private LocalDate ewkAbfrageDatum;

	@NotNull
	private boolean diplomatenstatus;

	@Nullable
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private Sprache korrespondenzSprache;


	public Gesuchsteller() {
	}

	@Nullable
	public String getMail() {
		return mail;
	}

	public void setMail(@Nullable final String mail) {
		this.mail = mail;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(final String mobile) {
		this.mobile = mobile;
	}

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(final String telefon) {
		this.telefon = telefon;
	}

	public String getTelefonAusland() {
		return telefonAusland;
	}

	public void setTelefonAusland(final String telefonAusland) {
		this.telefonAusland = telefonAusland;
	}

	public String getEwkPersonId() {
		return ewkPersonId;
	}

	public void setEwkPersonId(final String ewkPersonId) {
		this.ewkPersonId = ewkPersonId;
	}

	public LocalDate getEwkAbfrageDatum() {
		return ewkAbfrageDatum;
	}

	public void setEwkAbfrageDatum(LocalDate ewkAbfrageDatum) {
		this.ewkAbfrageDatum = ewkAbfrageDatum;
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
		target.setEwkPersonId(this.getEwkPersonId());
		target.setEwkAbfrageDatum(this.getEwkAbfrageDatum());
		target.setDiplomatenstatus(this.isDiplomatenstatus());
		target.setKorrespondenzSprache(this.getKorrespondenzSprache());
		return target;
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
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof Gesuchsteller)) {
			return false;
		}
		final Gesuchsteller otherGesuchsteller = (Gesuchsteller) other;
		return Objects.equals(getMail(), otherGesuchsteller.getMail()) &&
			Objects.equals(getMobile(), otherGesuchsteller.getMobile()) &&
			Objects.equals(getTelefon(), otherGesuchsteller.getTelefon()) &&
			Objects.equals(getTelefonAusland(), otherGesuchsteller.getTelefonAusland()) &&
			EbeguUtil.isSameOrNullStrings(getEwkPersonId(), otherGesuchsteller.getEwkPersonId()) &&
			Objects.equals(isDiplomatenstatus(), otherGesuchsteller.isDiplomatenstatus()) &&
			getKorrespondenzSprache() == otherGesuchsteller.getKorrespondenzSprache();
	}
}
