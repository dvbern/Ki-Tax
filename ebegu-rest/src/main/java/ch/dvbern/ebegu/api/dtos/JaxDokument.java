/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDateTime;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;

@XmlRootElement(name = "dokument")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxDokument extends JaxFile {

	private static final long serialVersionUID = 1118235796540488553L;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime timestampUpload;

	@Nullable
	private JaxBenutzer userUploaded;

	public LocalDateTime getTimestampUpload() {
		return timestampUpload;
	}

	public void setTimestampUpload(LocalDateTime timestampUpload) {
		this.timestampUpload = timestampUpload;
	}

	@Nullable
	public JaxBenutzer getUserUploaded() {
		return userUploaded;
	}

	public void setUserUploaded(@Nullable JaxBenutzer userUploaded) {
		this.userUploaded = userUploaded;
	}
}
