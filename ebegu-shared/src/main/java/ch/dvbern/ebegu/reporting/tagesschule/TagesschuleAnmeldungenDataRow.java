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
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.enums.Betreuungsstatus;

/**
 * DTO fuer die TagesschuleStatistik
 */
public class TagesschuleAnmeldungenDataRow {

	private @Nullable String nachnameKind;
	private @Nullable String vornameKind;
	private @Nullable LocalDate geburtsdatum;
	private @Nullable String nachnameAntragsteller1;
	private @Nullable String vornameAntragsteller1;
	private @Nullable String emailAntragsteller1;
	private @Nullable String nachnameAntragsteller2;
	private @Nullable String vornameAntragsteller2;
	private @Nullable String emailAntragsteller2;
	private @Nullable String referenznummer;
	private @Nullable LocalDate eintrittsdatum;
	private @Nonnull Betreuungsstatus status;
	private boolean isZweiwoechentlich;
	private @Nonnull AnmeldungTagesschule anmeldungTagesschule;

	public @Nullable String getNachnameKind() {
		return nachnameKind;
	}

	public void setNachnameKind(@Nullable String nachnameKind) {
		this.nachnameKind = nachnameKind;
	}

	public @Nullable String getVornameKind() {
		return vornameKind;
	}

	public void setVornameKind(@Nullable String vornameKind) {
		this.vornameKind = vornameKind;
	}

	public @Nullable LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public @Nullable String getNachnameAntragsteller1() {
		return nachnameAntragsteller1;
	}

	public void setNachnameAntragsteller1(@Nullable String nachnameAntragsteller1) {
		this.nachnameAntragsteller1 = nachnameAntragsteller1;
	}

	public @Nullable String getVornameAntragsteller1() {
		return vornameAntragsteller1;
	}

	public void setVornameAntragsteller1(@Nullable String vornameAntragsteller1) {
		this.vornameAntragsteller1 = vornameAntragsteller1;
	}

	public @Nullable String getEmailAntragsteller1() {
		return emailAntragsteller1;
	}

	public void setEmailAntragsteller1(@Nullable String emailAntragsteller1) {
		this.emailAntragsteller1 = emailAntragsteller1;
	}

	public @Nullable String getNachnameAntragsteller2() {
		return nachnameAntragsteller2;
	}

	public void setNachnameAntragsteller2(@Nullable String nachnameAntragsteller2) {
		this.nachnameAntragsteller2 = nachnameAntragsteller2;
	}

	public @Nullable String getVornameAntragsteller2() {
		return vornameAntragsteller2;
	}

	public void setVornameAntragsteller2(@Nullable String vornameAntragsteller2) {
		this.vornameAntragsteller2 = vornameAntragsteller2;
	}

	public @Nullable String getEmailAntragsteller2() {
		return emailAntragsteller2;
	}

	public void setEmailAntragsteller2(@Nullable String emailAntragsteller2) {
		this.emailAntragsteller2 = emailAntragsteller2;
	}

	public void setGeburtsdatum(@Nullable LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	public @Nullable String getReferenznummer() {
		return referenznummer;
	}

	public void setReferenznummer(@Nullable String referenznummer) {
		this.referenznummer = referenznummer;
	}

	public @Nullable LocalDate getEintrittsdatum() {
		return eintrittsdatum;
	}

	public void setEintrittsdatum(@Nullable LocalDate eintrittsdatum) {
		this.eintrittsdatum = eintrittsdatum;
	}

	@Nonnull
	public @NotNull Betreuungsstatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull @NotNull Betreuungsstatus status) {
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
