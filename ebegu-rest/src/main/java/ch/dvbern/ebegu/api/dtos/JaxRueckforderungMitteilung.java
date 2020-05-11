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
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.RueckforderungStatus;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxRueckforderungMitteilung extends JaxAbstractDTO {

	private static final long serialVersionUID = -8553194968646128553L;

	@Nonnull
	private JaxBenutzer absender;

	@Nonnull
	private String betreff;

	@Nonnull
	private String inhalt;

	@Nonnull
	private LocalDateTime sendeDatum;

	@Nonnull
	private ArrayList<RueckforderungStatus> gesendetAnStatus;

	@Nonnull
	public JaxBenutzer getAbsender() {
		return absender;
	}

	public void setAbsender(@Nonnull JaxBenutzer absender) {
		this.absender = absender;
	}

	@Nonnull
	public String getBetreff() {
		return betreff;
	}

	public void setBetreff(@Nonnull String betreff) {
		this.betreff = betreff;
	}

	@Nonnull
	public String getInhalt() {
		return inhalt;
	}

	public void setInhalt(@Nonnull String inhalt) {
		this.inhalt = inhalt;
	}

	@Nonnull
	public LocalDateTime getSendeDatum() {
		return sendeDatum;
	}

	public void setSendeDatum(@Nonnull LocalDateTime sendeDatum) {
		this.sendeDatum = sendeDatum;
	}

	@Nonnull
	public ArrayList<RueckforderungStatus> getGesendetAnStatus() {
		return gesendetAnStatus;
	}

	public void setGesendetAnStatus(@Nonnull ArrayList<RueckforderungStatus> gesendetAnStatus) {
		this.gesendetAnStatus = gesendetAnStatus;
	}
}
