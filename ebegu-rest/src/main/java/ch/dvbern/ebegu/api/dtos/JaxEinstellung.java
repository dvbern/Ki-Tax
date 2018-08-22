/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.EinstellungKey;

/**
 * DTO fuer Einstellungen
 */
@XmlRootElement(name = "einstellung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxEinstellung extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = 2539868697910194410L;

	@NotNull
	private EinstellungKey key;

	@NotNull
	private String value;

	// Gesuchsperiode, Mandant und Gemeinde werden aktuell nicht gemappt!

	public EinstellungKey getKey() {
		return key;
	}

	public void setKey(EinstellungKey key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
