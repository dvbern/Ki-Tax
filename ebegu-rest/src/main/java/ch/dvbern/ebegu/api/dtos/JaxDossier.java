/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer Dossiers
 */
@XmlRootElement(name = "dossier")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxDossier extends JaxAbstractDTO {

	private static final long serialVersionUID = -4522142577271287413L;

	private JaxFall fall;

	private JaxGemeinde gemeinde;

	private long dossierNummer;

	private JaxAuthLoginElement verantwortlicherBG;

	private JaxAuthLoginElement verantwortlicherTS;


	public JaxFall getFall() {
		return fall;
	}

	public void setFall(JaxFall fall) {
		this.fall = fall;
	}

	public JaxGemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(JaxGemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	public long getDossierNummer() {
		return dossierNummer;
	}

	public void setDossierNummer(long dossierNummer) {
		this.dossierNummer = dossierNummer;
	}

	public JaxAuthLoginElement getVerantwortlicherBG() {
		return verantwortlicherBG;
	}

	public void setVerantwortlicherBG(JaxAuthLoginElement verantwortlicherBG) {
		this.verantwortlicherBG = verantwortlicherBG;
	}

	public JaxAuthLoginElement getVerantwortlicherTS() {
		return verantwortlicherTS;
	}

	public void setVerantwortlicherTS(JaxAuthLoginElement verantwortlicherTS) {
		this.verantwortlicherTS = verantwortlicherTS;
	}
}
