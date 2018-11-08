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

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.Audited;

/**
 * Entity fuer Gesuchsperiode.
 */
@Audited
@Entity
public class Gesuchsperiode extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -9132257370971574570L;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private GesuchsperiodeStatus status = GesuchsperiodeStatus.ENTWURF;

	// Wir merken uns, wann die Periode aktiv geschalten wurde, damit z.B. die Mails nicht 2 mal verschickt werden
	@Column(nullable = true)
	private LocalDate datumAktiviert;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumFreischaltungTagesschule;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumErsterSchultag;


	public GesuchsperiodeStatus getStatus() {
		return status;
	}

	public void setStatus(GesuchsperiodeStatus status) {
		this.status = status;
	}

	public int getBasisJahr() {
		return getGueltigkeit().getGueltigAb().getYear() - 1;
	}

	public int getBasisJahrPlus1() {
		return getBasisJahr() + 1;
	}

	public int getBasisJahrPlus2() {
		return getBasisJahr() + 2;
	}

	public LocalDate getDatumAktiviert() {
		return datumAktiviert;
	}

	public void setDatumAktiviert(LocalDate datumAktiviert) {
		this.datumAktiviert = datumAktiviert;
	}

	@Nullable
	public LocalDate getDatumFreischaltungTagesschule() {
		return datumFreischaltungTagesschule;
	}

	public void setDatumFreischaltungTagesschule(@Nullable LocalDate datumFreischaltungTagesschule) {
		this.datumFreischaltungTagesschule = datumFreischaltungTagesschule;
	}

	@Nullable
	public LocalDate getDatumErsterSchultag() {
		return datumErsterSchultag;
	}

	public void setDatumErsterSchultag(@Nullable LocalDate datumErsterSchultag) {
		this.datumErsterSchultag = datumErsterSchultag;
	}

	@SuppressWarnings({ "OverlyComplexBooleanExpression" })
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
		if (!(other instanceof Gesuchsperiode)) {
			return false;
		}
		final Gesuchsperiode otherGesuchsperiode = (Gesuchsperiode) other;
		return this.getStatus() == otherGesuchsperiode.getStatus();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("gueltigkeit", getGueltigkeit().toString())
			.append("status", status.name())
			.toString();
	}

	public String getGesuchsperiodeString() {
		DateRange gueltigkeit = this.getGueltigkeit();
		return gueltigkeit.getGueltigAb().getYear() + "/"
			+ gueltigkeit.getGueltigBis().getYear();
	}

	public String getGesuchsperiodeDisplayName() {
		DateRange gueltigkeit = this.getGueltigkeit();

		return Constants.DATE_FORMATTER.format(gueltigkeit.getGueltigAb()) + " - "
			+ Constants.DATE_FORMATTER.format(gueltigkeit.getGueltigBis())
			+ " " + ServerMessageUtil.getMessage("Gesuchsperiode_KONFIGURATION")
			+ " (" + ServerMessageUtil.translateEnumValue(status) + ")";
	}

	public boolean hasTagesschulenAnmeldung() {
		return getDatumFreischaltungTagesschule() != null;
	}
}
