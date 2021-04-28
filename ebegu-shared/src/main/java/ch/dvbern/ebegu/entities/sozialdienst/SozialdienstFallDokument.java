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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.sozialdienst;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.FileMetadata;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class SozialdienstFallDokument extends FileMetadata {

	private static final long serialVersionUID = -6261661007452419600L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_sozialdienstFallDokument_sozialdienstFall_id"), nullable =
		false)
	private SozialdienstFall sozialdienstFall;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private LocalDateTime timestampUpload;

	public SozialdienstFall getSozialdienstFall() {
		return sozialdienstFall;
	}

	public void setSozialdienstFall(SozialdienstFall sozialdienstFall) {
		this.sozialdienstFall = sozialdienstFall;
	}

	@Nonnull
	public LocalDateTime getTimestampUpload() {
		return timestampUpload;
	}

	public void setTimestampUpload(@Nonnull LocalDateTime timestampUpload) {
		this.timestampUpload = timestampUpload;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof SozialdienstFallDokument)) {
			return false;
		}
		final SozialdienstFallDokument
			otherSozialdienstFallDokument = (SozialdienstFallDokument) other;
		return this.sozialdienstFall.getId().equals(otherSozialdienstFallDokument.getSozialdienstFall().getId())
			&& this.timestampUpload.isEqual(otherSozialdienstFallDokument.getTimestampUpload());

	}
}
