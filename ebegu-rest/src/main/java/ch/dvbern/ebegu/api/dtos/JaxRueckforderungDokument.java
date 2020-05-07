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

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.enums.RueckforderungDokumentTyp;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxRueckforderungDokument extends JaxAbstractDTO {

	private static final long serialVersionUID = -924708642859396311L;

	@Nonnull
	private RueckforderungFormular rueckforderungFormular;

	@Nonnull
	private RueckforderungDokumentTyp rueckforderungDokumentTyp;

	@Nonnull
	private LocalDateTime timestampUpload;

	@Nonnull
	public RueckforderungFormular getRueckforderungFormular() {
		return rueckforderungFormular;
	}

	public void setRueckforderungFormular(@Nonnull RueckforderungFormular rueckforderungFormular) {
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
}
