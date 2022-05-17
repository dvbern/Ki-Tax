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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.RueckforderungDokumentTyp;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class RueckforderungDokument extends FileMetadata {

	private static final long serialVersionUID = -6261661007452419600L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_rueckforderungDokument_rueckforderungFormular_id"), nullable =
		false)
	private RueckforderungFormular rueckforderungFormular;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private RueckforderungDokumentTyp rueckforderungDokumentTyp;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private LocalDateTime timestampUpload;

	public RueckforderungFormular getRueckforderungFormular() {
		return rueckforderungFormular;
	}

	public void setRueckforderungFormular(RueckforderungFormular rueckforderungFormular) {
		this.rueckforderungFormular = rueckforderungFormular;
	}

	@Nonnull
	public RueckforderungDokumentTyp getRueckforderungDokumentTyp() {
		return rueckforderungDokumentTyp;
	}

	public void setRueckforderungDokumentTyp(@Nonnull RueckforderungDokumentTyp rueckforderungDokumentTyp) {
		this.rueckforderungDokumentTyp = rueckforderungDokumentTyp;
	}

	@Nonnull
	public LocalDateTime getTimestampUpload() {
		return timestampUpload;
	}

	public void setTimestampUpload(@Nonnull LocalDateTime timestampUpload) {
		this.timestampUpload = timestampUpload;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final RueckforderungDokument otherRueckforderungDokument = (RueckforderungDokument) other;
		return this.rueckforderungFormular.getId().equals(otherRueckforderungDokument.getRueckforderungFormular().getId())
			&& this.rueckforderungDokumentTyp.equals(otherRueckforderungDokument.getRueckforderungDokumentTyp())
			&& this.timestampUpload.isEqual(otherRueckforderungDokument.getTimestampUpload());

	}
}
