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

package ch.dvbern.ebegu.dto.gemeindeantrag;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import ch.dvbern.ebegu.enums.gemeindeantrag.OeffnungszeitenTagesschuleTyp;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public class OeffnungszeitenTagesschuleDTO implements Serializable {

	private static final long serialVersionUID = -1277026655764135397L;

	private OeffnungszeitenTagesschuleTyp type;

	private boolean montag = false;
	private boolean dienstag = false;
	private boolean mittwoch = false;
	private boolean donnerstag = false;
	private boolean freitag = false;

	public OeffnungszeitenTagesschuleTyp getType() {
		return type;
	}

	public void setType(OeffnungszeitenTagesschuleTyp type) {
		this.type = type;
	}

	public boolean isMontag() {
		return montag;
	}

	public void setMontag(boolean montag) {
		this.montag = montag;
	}

	public boolean isDienstag() {
		return dienstag;
	}

	public void setDienstag(boolean dienstag) {
		this.dienstag = dienstag;
	}

	public boolean isMittwoch() {
		return mittwoch;
	}

	public void setMittwoch(boolean mittwoch) {
		this.mittwoch = mittwoch;
	}

	public boolean isDonnerstag() {
		return donnerstag;
	}

	public void setDonnerstag(boolean donnerstag) {
		this.donnerstag = donnerstag;
	}

	public boolean isFreitag() {
		return freitag;
	}

	public void setFreitag(boolean freitag) {
		this.freitag = freitag;
	}
}
