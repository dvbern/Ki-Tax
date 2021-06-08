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

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer Monitoren Aenderung der Betreuungen,
 */
@XmlRootElement(name = "betreuungMonitoring")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBetreuungMonitoring extends JaxAbstractDTO {

	@Nonnull
	private String refNummer;

	@Nonnull
	private String benutzer;

	@Nonnull
	private String infoText;

	@Nonnull
	private LocalDateTime timestamp;

	@Nonnull
	public String getRefNummer() {
		return refNummer;
	}

	public void setRefNummer(@Nonnull String refNummer) {
		this.refNummer = refNummer;
	}

	@Nonnull
	public String getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(@Nonnull String benutzer) {
		this.benutzer = benutzer;
	}

	@Nonnull
	public String getInfoText() {
		return infoText;
	}

	public void setInfoText(@Nonnull String infoText) {
		this.infoText = infoText;
	}

	@Nonnull
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(@Nonnull LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
}
