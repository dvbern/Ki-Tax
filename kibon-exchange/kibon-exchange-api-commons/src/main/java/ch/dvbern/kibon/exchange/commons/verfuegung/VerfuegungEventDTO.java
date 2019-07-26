/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.kibon.exchange.commons.verfuegung;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp;

public class VerfuegungEventDTO implements Serializable {

	private static final long serialVersionUID = -1855780956653292131L;

	@Nonnull
	private @NotEmpty String refnr = "";

	@Nonnull
	private @NotEmpty String institutionId = "";

	@Nonnull
	private @NotNull LocalDate von = LocalDate.MIN;

	@Nonnull
	private @NotNull LocalDate bis = LocalDate.MIN;

	private @Min(0) @NotNull Integer version = -1;

	@Nonnull
	private @NotNull LocalDateTime verfuegtAm = LocalDateTime.MIN;

	@Nonnull
	private final @NotNull @Valid KindDTO kind;

	@Nonnull
	private final @NotNull @Valid GesuchstellerDTO gesuchsteller;

	@Nonnull
	private final @NotNull BetreuungsangebotTyp betreuungsArt;

	@Nonnull
	private @NotNull @Valid List<ZeitabschnittDTO> zeitabschnitte = new ArrayList<>();

	@Nonnull
	private @NotNull @Valid List<ZeitabschnittDTO> ignorierteZeitabschnitte = new ArrayList<>();

	public VerfuegungEventDTO() {
		this(new KindDTO(), new GesuchstellerDTO(), BetreuungsangebotTyp.KITA);
	}

	public VerfuegungEventDTO(
		@Nonnull KindDTO kind,
		@Nonnull GesuchstellerDTO gesuchsteller,
		@Nonnull BetreuungsangebotTyp betreuungsArt) {
		this.kind = kind;
		this.gesuchsteller = gesuchsteller;
		this.betreuungsArt = betreuungsArt;
	}

	@Nonnull
	public String getRefnr() {
		return refnr;
	}

	public void setRefnr(@Nonnull String refnr) {
		this.refnr = refnr;
	}

	@Nonnull
	public String getInstitutionId() {
		return institutionId;
	}

	public void setInstitutionId(@Nonnull String institutionId) {
		this.institutionId = institutionId;
	}

	@Nonnull
	public LocalDate getVon() {
		return von;
	}

	public void setVon(@Nonnull LocalDate von) {
		this.von = von;
	}

	@Nonnull
	public LocalDate getBis() {
		return bis;
	}

	public void setBis(@Nonnull LocalDate bis) {
		this.bis = bis;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Nonnull
	public LocalDateTime getVerfuegtAm() {
		return verfuegtAm;
	}

	public void setVerfuegtAm(@Nonnull LocalDateTime verfuegtAm) {
		this.verfuegtAm = verfuegtAm;
	}

	@Nonnull
	public KindDTO getKind() {
		return kind;
	}

	@Nonnull
	public GesuchstellerDTO getGesuchsteller() {
		return gesuchsteller;
	}

	@Nonnull
	public BetreuungsangebotTyp getBetreuungsArt() {
		return betreuungsArt;
	}

	@Nonnull
	public List<ZeitabschnittDTO> getZeitabschnitte() {
		return zeitabschnitte;
	}

	public void setZeitabschnitte(@Nonnull List<ZeitabschnittDTO> zeitabschnitte) {
		this.zeitabschnitte = zeitabschnitte;
	}

	@Nonnull
	public List<ZeitabschnittDTO> getIgnorierteZeitabschnitte() {
		return ignorierteZeitabschnitte;
	}

	public void setIgnorierteZeitabschnitte(@Nonnull List<ZeitabschnittDTO> ignorierteZeitabschnitte) {
		this.ignorierteZeitabschnitte = ignorierteZeitabschnitte;
	}
}
