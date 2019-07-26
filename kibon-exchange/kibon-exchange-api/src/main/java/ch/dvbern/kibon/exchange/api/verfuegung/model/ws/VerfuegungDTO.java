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

package ch.dvbern.kibon.exchange.api.verfuegung.model.ws;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.kibon.exchange.api.verfuegung.model.BetreuungsAngebot;
import ch.dvbern.kibon.exchange.api.verfuegung.model.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.api.verfuegung.model.KindDTO;
import ch.dvbern.kibon.exchange.api.verfuegung.model.ZeitabschnittDTO;

public class VerfuegungDTO implements Serializable {

	private static final long serialVersionUID = 1339970917808014561L;

	@Nonnull
	private @NotNull Long id;

	@Nonnull
	private @Size(min = 1) @NotNull String refnr;

	@Nonnull
	private @Size(min = 1) @NotNull String institutionId;

	@Nonnull
	private @NotNull LocalDate von;

	@Nonnull
	private @NotNull LocalDate bis;

	@Nonnull
	private @NotNull Integer version;

	@Nonnull
	private @NotNull LocalDateTime verfuegtAm;

	@Nonnull
	private @NotNull @Valid KindDTO kind;

	@Nonnull
	private @NotNull @Valid GesuchstellerDTO gesuchsteller;

	@Nonnull
	private final @NotNull BetreuungsAngebot betreuungsArt;

	@Nonnull
	private @NotNull @Valid List<ZeitabschnittDTO> zeitabschnitte = new ArrayList<>();

	@Nonnull
	private @NotNull @Valid List<ZeitabschnittDTO> ignorierteZeitabschnitte = new ArrayList<>();

	public VerfuegungDTO() {
		this.id = -1L;
		this.refnr = "";
		this.institutionId = "";
		this.von = LocalDate.MIN;
		this.bis = LocalDate.MIN;
		this.version = -1;
		this.verfuegtAm = LocalDateTime.MIN;
		this.kind = new KindDTO();
		this.gesuchsteller = new GesuchstellerDTO();
		this.betreuungsArt = BetreuungsAngebot.KITA;
	}

	public VerfuegungDTO(
		@Nonnull Long id,
		@Nonnull String refnr,
		@Nonnull String institutionId,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		@Nonnull Integer version,
		@Nonnull LocalDateTime verfuegtAm,
		@Nonnull KindDTO kind,
		@Nonnull GesuchstellerDTO gesuchsteller,
		@Nonnull BetreuungsAngebot betreuungsArt) {
		this.id = id;
		this.refnr = refnr;
		this.institutionId = institutionId;
		this.von = von;
		this.bis = bis;
		this.version = version;
		this.verfuegtAm = verfuegtAm;
		this.kind = kind;
		this.gesuchsteller = gesuchsteller;
		this.betreuungsArt = betreuungsArt;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		VerfuegungDTO
			that = (VerfuegungDTO) o;

		return getId().equals(that.getId()) &&
			getRefnr().equals(that.getRefnr()) &&
			getInstitutionId().equals(that.getInstitutionId()) &&
			getVon().equals(that.getVon()) &&
			getBis().equals(that.getBis()) &&
			getVersion().equals(that.getVersion()) &&
			getVerfuegtAm().equals(that.getVerfuegtAm()) &&
			getKind().equals(that.getKind()) &&
			getGesuchsteller().equals(that.getGesuchsteller()) &&
			getBetreuungsArt() == that.getBetreuungsArt() &&
			getZeitabschnitte().equals(that.getZeitabschnitte()) &&
			getIgnorierteZeitabschnitte().equals(that.getIgnorierteZeitabschnitte());
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			getId(),
			getRefnr(),
			getInstitutionId(),
			getVon(),
			getBis(),
			getVersion(),
			getVerfuegtAm(),
			getKind(),
			getGesuchsteller(),
			getBetreuungsArt(),
			getZeitabschnitte(),
			getIgnorierteZeitabschnitte());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", VerfuegungDTO.class.getSimpleName() + '[', "]")
			.add("id='" + id + '\'')
			.add("refnr='" + refnr + '\'')
			.add("institutionId='" + institutionId + '\'')
			.add("von=" + von)
			.add("bis=" + bis)
			.add("version=" + version)
			.add("verfuegtAm=" + verfuegtAm)
			.toString();
	}

	@Nonnull
	public Long getId() {
		return id;
	}

	public void setId(@Nonnull Long id) {
		this.id = id;
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

	@Nonnull
	public Integer getVersion() {
		return version;
	}

	public void setVersion(@Nonnull Integer version) {
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

	public void setKind(@Nonnull KindDTO kind) {
		this.kind = kind;
	}

	@Nonnull
	public GesuchstellerDTO getGesuchsteller() {
		return gesuchsteller;
	}

	public void setGesuchsteller(@Nonnull GesuchstellerDTO gesuchsteller) {
		this.gesuchsteller = gesuchsteller;
	}

	@Nonnull
	public BetreuungsAngebot getBetreuungsArt() {
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
