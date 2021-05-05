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

package ch.dvbern.ebegu.reporting.tagesschule;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.enums.Betreuungsstatus;

/**
 * DTO fuer die TagesschuleStatistik
 */
public class TagesschuleAnmeldungenDataRow {

	@Nullable
	private String nachnameKind;
	@Nullable
	private String vornameKind;
	@Nullable
	private LocalDate geburtsdatum;
	@Nullable
	private String nachnameAntragsteller1;
	@Nullable
	private String vornameAntragsteller1;
	@Nullable
	private String emailAntragsteller1;
	@Nullable
	private String nachnameAntragsteller2;
	@Nullable
	private String vornameAntragsteller2;
	@Nullable
	private String emailAntragsteller2;
	@Nullable
	private String referenznummer;
	@Nullable
	private LocalDate eintrittsdatum;
	@Nonnull
	private Betreuungsstatus status;
	private boolean isZweiwoechentlich;
	@Nonnull
	private AnmeldungTagesschule anmeldungTagesschule;

	@Nullable
	public String getNachnameKind() {
		return nachnameKind;
	}

	public void setNachnameKind(@Nullable String nachnameKind) {
		this.nachnameKind = nachnameKind;
	}

	@Nullable
	public String getVornameKind() {
		return vornameKind;
	}

	public void setVornameKind(@Nullable String vornameKind) {
		this.vornameKind = vornameKind;
	}

	@Nullable
	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	@Nullable
	public String getNachnameAntragsteller1() {
		return nachnameAntragsteller1;
	}

	public void setNachnameAntragsteller1(@Nullable String nachnameAntragsteller1) {
		this.nachnameAntragsteller1 = nachnameAntragsteller1;
	}

	@Nullable
	public String getVornameAntragsteller1() {
		return vornameAntragsteller1;
	}

	public void setVornameAntragsteller1(@Nullable String vornameAntragsteller1) {
		this.vornameAntragsteller1 = vornameAntragsteller1;
	}

	@Nullable
	public String getEmailAntragsteller1() {
		return emailAntragsteller1;
	}

	public void setEmailAntragsteller1(@Nullable String emailAntragsteller1) {
		this.emailAntragsteller1 = emailAntragsteller1;
	}

	@Nullable
	public String getNachnameAntragsteller2() {
		return nachnameAntragsteller2;
	}

	public void setNachnameAntragsteller2(@Nullable String nachnameAntragsteller2) {
		this.nachnameAntragsteller2 = nachnameAntragsteller2;
	}

	@Nullable
	public String getVornameAntragsteller2() {
		return vornameAntragsteller2;
	}

	public void setVornameAntragsteller2(@Nullable String vornameAntragsteller2) {
		this.vornameAntragsteller2 = vornameAntragsteller2;
	}

	@Nullable
	public String getEmailAntragsteller2() {
		return emailAntragsteller2;
	}

	public void setEmailAntragsteller2(@Nullable String emailAntragsteller2) {
		this.emailAntragsteller2 = emailAntragsteller2;
	}

	public void setGeburtsdatum(@Nullable LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	@Nullable
	public String getReferenznummer() {
		return referenznummer;
	}

	public void setReferenznummer(@Nullable String referenznummer) {
		this.referenznummer = referenznummer;
	}

	@Nullable
	public LocalDate getEintrittsdatum() {
		return eintrittsdatum;
	}

	public void setEintrittsdatum(@Nullable LocalDate eintrittsdatum) {
		this.eintrittsdatum = eintrittsdatum;
	}

	@Nonnull
	public Betreuungsstatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull Betreuungsstatus status) {
		this.status = status;
	}

	@Nonnull
	public AnmeldungTagesschule getAnmeldungTagesschule() {
		return anmeldungTagesschule;
	}

	public boolean isZweiwoechentlich() {
		return isZweiwoechentlich;
	}

	public void setZweiwoechentlich(boolean zweiwoechentlich) {
		isZweiwoechentlich = zweiwoechentlich;
	}

	public void setAnmeldungTagesschule(@Nonnull AnmeldungTagesschule anmeldungTagesschule) {
		this.anmeldungTagesschule = anmeldungTagesschule;
	}
}
