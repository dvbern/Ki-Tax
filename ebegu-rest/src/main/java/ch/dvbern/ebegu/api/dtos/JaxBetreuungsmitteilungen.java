/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "betreuungsmitteilungenResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBetreuungsmitteilungen implements Serializable {

	private static final long serialVersionUID = -5622686940054179426L;

	private List<JaxBetreuungsmitteilung> betreuungsmitteilungen;

	public List<JaxBetreuungsmitteilung> getBetreuungsmitteilungen() {
		return betreuungsmitteilungen;
	}

	public void setBetreuungsmitteilungen(List<JaxBetreuungsmitteilung> betreuungsmitteilungen) {
		this.betreuungsmitteilungen = betreuungsmitteilungen;
	}
}
