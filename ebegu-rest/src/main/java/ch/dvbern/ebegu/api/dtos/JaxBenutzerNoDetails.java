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
package ch.dvbern.ebegu.api.dtos;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "benutzerNoDetails")
public class JaxBenutzerNoDetails implements Serializable {

	private static final long serialVersionUID = 2769899329796452129L;

	@Nonnull
	private String nachname = "";

	@Nonnull
	private String vorname = "";

	@Nonnull
	private Set<String> gemeindeIds = new HashSet<>();


	@Nonnull
	public String getNachname() {
		return nachname;
	}

	public void setNachname(@Nonnull String nachname) {
		this.nachname = nachname;
	}

	@Nonnull
	public String getVorname() {
		return vorname;
	}

	public void setVorname(@Nonnull String vorname) {
		this.vorname = vorname;
	}

	@Nonnull
	public Set<String> getGemeindeIds() {
		return gemeindeIds;
	}

	public void setGemeindeIds(@Nonnull Set<String> gemeindeIds) {
		this.gemeindeIds = gemeindeIds;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JaxBenutzerNoDetails)) {
			return false;
		}
		JaxBenutzerNoDetails other = (JaxBenutzerNoDetails) o;
		//noinspection NonFinalFieldReferenceInEquals
		return Objects.equals(this.getVorname(), other.getVorname())
			&& Objects.equals(this.getNachname(), other.getNachname());
	}

	@Override
	public int hashCode() {
		return super.hashCode()
			+ Objects.hashCode(this.getVorname())
			+ Objects.hashCode(this.getNachname());
	}
}
