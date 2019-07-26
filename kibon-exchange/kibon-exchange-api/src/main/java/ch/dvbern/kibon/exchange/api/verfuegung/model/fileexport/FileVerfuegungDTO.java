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

package ch.dvbern.kibon.exchange.api.verfuegung.model.fileexport;

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

import ch.dvbern.kibon.exchange.api.verfuegung.model.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.api.verfuegung.model.KindDTO;
import ch.dvbern.kibon.exchange.api.verfuegung.model.ZeitabschnittDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

public class FileVerfuegungDTO implements Serializable {

	private static final long serialVersionUID = 1339970917808014561L;

	@Nonnull
	private @Size(min = 1) @NotNull String refnr;

	@Nonnull
	private @NotNull LocalDate von;

	@Nonnull
	private @NotNull LocalDate bis;

	@Nonnull
	private @NotNull Integer version;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
	@Nonnull
	private @NotNull LocalDateTime verfuegtAm;

	@Nonnull
	private @NotNull @Valid KindDTO kind;

	@Nonnull
	private @NotNull @Valid GesuchstellerDTO gesuchsteller;

	@Nonnull
	private @NotNull @Valid BetreuungDTO betreuung;

	@Nonnull
	private @NotNull @Valid List<ZeitabschnittDTO> zeitabschnitte = new ArrayList<>();

	@Nonnull
	private @NotNull @Valid List<ZeitabschnittDTO> ignorierteZeitabschnitte = new ArrayList<>();

	public FileVerfuegungDTO() {
		this.refnr = "";
		this.von = LocalDate.MIN;
		this.bis = LocalDate.MIN;
		this.version = -1;
		this.verfuegtAm = LocalDateTime.MIN;
		this.kind = new KindDTO();
		this.gesuchsteller = new GesuchstellerDTO();
		this.betreuung = new BetreuungDTO();
	}

	public FileVerfuegungDTO(
		@Nonnull String refnr,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		@Nonnull Integer version,
		@Nonnull LocalDateTime verfuegtAm,
		@Nonnull KindDTO kind,
		@Nonnull GesuchstellerDTO gesuchsteller,
		@Nonnull BetreuungDTO betreuung) {
		this.refnr = refnr;
		this.von = von;
		this.bis = bis;
		this.version = version;
		this.verfuegtAm = verfuegtAm;
		this.kind = kind;
		this.gesuchsteller = gesuchsteller;
		this.betreuung = betreuung;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		FileVerfuegungDTO that = (FileVerfuegungDTO) o;

		return getRefnr().equals(that.getRefnr()) &&
			getVon().equals(that.getVon()) &&
			getBis().equals(that.getBis()) &&
			getVersion().equals(that.getVersion()) &&
			getVerfuegtAm().equals(that.getVerfuegtAm()) &&
			getKind().equals(that.getKind()) &&
			getGesuchsteller().equals(that.getGesuchsteller()) &&
			getBetreuung().equals(that.getBetreuung()) &&
			getZeitabschnitte().equals(that.getZeitabschnitte()) &&
			getIgnorierteZeitabschnitte().equals(that.getIgnorierteZeitabschnitte());
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			getRefnr(),
			getVon(),
			getBis(),
			getVersion(),
			getVerfuegtAm(),
			getKind(),
			getGesuchsteller(),
			getBetreuung(),
			getZeitabschnitte(),
			getIgnorierteZeitabschnitte());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", FileVerfuegungDTO.class.getSimpleName() + '[', "]")
			.add("refnr='" + refnr + '\'')
			.add("von=" + von)
			.add("bis=" + bis)
			.add("version=" + version)
			.add("verfuegtAm=" + verfuegtAm)
			.toString();
	}

	@Nonnull
	public String getRefnr() {
		return refnr;
	}

	public void setRefnr(@Nonnull String refnr) {
		this.refnr = refnr;
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
	public BetreuungDTO getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(@Nonnull BetreuungDTO betreuung) {
		this.betreuung = betreuung;
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
