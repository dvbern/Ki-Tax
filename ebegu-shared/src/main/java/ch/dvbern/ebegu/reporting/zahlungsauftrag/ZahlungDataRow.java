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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.reporting.zahlungsauftrag;

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Zahlung;

public class ZahlungDataRow implements Comparable<ZahlungDataRow> {

	@Nonnull
	private Zahlung zahlung;

	@Nonnull
	private InstitutionStammdaten institutionStammdaten;

	public ZahlungDataRow(@Nonnull Zahlung zahlung, @Nonnull InstitutionStammdaten institutionStammdaten) {
		this.zahlung = zahlung;
		this.institutionStammdaten = institutionStammdaten;
	}

	@Nonnull
	public Zahlung getZahlung() {
		return zahlung;
	}

	public void setZahlung(@Nonnull Zahlung zahlung) {
		this.zahlung = zahlung;
	}

	@Nonnull
	public InstitutionStammdaten getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(@Nonnull InstitutionStammdaten institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	@Override
	public int compareTo(@Nonnull ZahlungDataRow o) {
		return this.getZahlung().compareTo(o.getZahlung());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ZahlungDataRow)) {
			return false;
		}
		ZahlungDataRow dataRow = (ZahlungDataRow) o;
		return Objects.equals(getZahlung(), dataRow.getZahlung());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getZahlung());
	}
}
