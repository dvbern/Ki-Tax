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

package ch.dvbern.ebegu.reporting.tagesschule;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.enums.Betreuungsstatus;

/**
 * DTO fuer die TagesschuleStatistik
 */
public class TagesschuleDataRow {

	private String nachnameKind;
	private String vornameKind;
	private LocalDate geburtsdatum;
	private String referenznummer;
	private LocalDate ab;
	private @NotNull Betreuungsstatus status;
	private AnmeldungTagesschule anmeldungTagesschule;

	public String getNachnameKind() {
		return nachnameKind;
	}

	public void setNachnameKind(String nachnameKind) {
		this.nachnameKind = nachnameKind;
	}

	public String getVornameKind() {
		return vornameKind;
	}

	public void setVornameKind(String vornameKind) {
		this.vornameKind = vornameKind;
	}

	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	public String getReferenznummer() {
		return referenznummer;
	}

	public void setReferenznummer(String referenznummer) {
		this.referenznummer = referenznummer;
	}

	public LocalDate getAb() {
		return ab;
	}

	public void setAb(LocalDate ab) {
		this.ab = ab;
	}

	public @NotNull Betreuungsstatus getStatus() {
		return status;
	}

	public void setStatus(@NotNull Betreuungsstatus status) {
		this.status = status;
	}

	public AnmeldungTagesschule getAnmeldungTagesschule() {
		return anmeldungTagesschule;
	}

	public void setAnmeldungTagesschule(AnmeldungTagesschule anmeldungTagesschule) {
		this.anmeldungTagesschule = anmeldungTagesschule;
	}
}
