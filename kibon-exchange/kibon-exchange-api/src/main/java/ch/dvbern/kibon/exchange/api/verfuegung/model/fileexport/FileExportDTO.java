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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FileExportDTO implements Serializable {

	private static final long serialVersionUID = -5782035964234678821L;

	@Nonnull
	private static final String SCHEMA_VERSION = "2.0";

	@Nonnull
	private @Size(min = 1) @NotNull String schemaVersion = SCHEMA_VERSION;

	@Nonnull
	private @NotNull @Valid List<FileVerfuegungDTO> verfuegungen = new ArrayList<>();

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		FileExportDTO that = (FileExportDTO) o;

		return getSchemaVersion().equals(that.getSchemaVersion()) &&
			getVerfuegungen().equals(that.getVerfuegungen());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSchemaVersion(), getVerfuegungen());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", FileExportDTO.class.getSimpleName() + '[', "]")
			.add("schemaVersion='" + schemaVersion + '\'')
			.toString();
	}

	@Nonnull
	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(@Nonnull String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	@Nonnull
	public List<FileVerfuegungDTO> getVerfuegungen() {
		return verfuegungen;
	}

	public void setVerfuegungen(@Nonnull List<FileVerfuegungDTO> verfuegungen) {
		this.verfuegungen = verfuegungen;
	}
}
