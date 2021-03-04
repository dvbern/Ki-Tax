/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.FileMetadata;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class FerienbetreuungDokument extends FileMetadata {

	private static final long serialVersionUID = -7562439691196495568L;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuungDokument_ferienbetreuungAngabenContainer_id"), nullable = false)
	private FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private LocalDateTime timestampUpload;

	@Nonnull
	public FerienbetreuungAngabenContainer getFerienbetreuungAngabenContainer() {
		return ferienbetreuungAngabenContainer;
	}

	public void setFerienbetreuungAngabenContainer(@Nonnull FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer) {
		this.ferienbetreuungAngabenContainer = ferienbetreuungAngabenContainer;
	}

	@Nonnull
	public LocalDateTime getTimestampUpload() {
		return timestampUpload;
	}

	public void setTimestampUpload(@Nonnull LocalDateTime timestampUpload) {
		this.timestampUpload = timestampUpload;
	}
}
