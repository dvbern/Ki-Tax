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

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.GruendeZusatzleistung;
import ch.dvbern.ebegu.enums.IntegrationTyp;

/**
 * DTO fuer Stammdaten der PensumFachstelle. Definiert ein bestimmtes Pensum und eine bestimmte Fachstelle und wird einem
 * Kind zugewiesen
 */
@XmlRootElement(name = "pensumFachstellen")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxPensumFachstelle extends JaxAbstractIntegerPensumDTO {

	private static final long serialVersionUID = -7997026881634137397L;

	@Nullable
	private JaxFachstelle fachstelle;

	private IntegrationTyp integrationTyp;

	@Nullable
	private GruendeZusatzleistung gruendeZusatzleistung;

	@Nullable
	public JaxFachstelle getFachstelle() {
		return fachstelle;
	}

	public void setFachstelle(@Nullable JaxFachstelle fachstelle) {
		this.fachstelle = fachstelle;
	}

	public IntegrationTyp getIntegrationTyp() {
		return integrationTyp;
	}

	public void setIntegrationTyp(IntegrationTyp integrationTyp) {
		this.integrationTyp = integrationTyp;
	}

	@Nullable
	public GruendeZusatzleistung getGruendeZusatzleistung() {
		return gruendeZusatzleistung;
	}

	public void setGruendeZusatzleistung(@Nullable GruendeZusatzleistung gruendeZusatzleistung) {
		this.gruendeZusatzleistung = gruendeZusatzleistung;
	}
}
