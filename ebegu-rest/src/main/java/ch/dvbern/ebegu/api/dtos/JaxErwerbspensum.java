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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.Taetigkeit;

/**
 * DTO fuer Erwerbspensum
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxErwerbspensum extends JaxAbstractIntegerPensumDTO {

	private static final long serialVersionUID = -2495737706808699744L;

	@NotNull
	@Enumerated(EnumType.STRING)
	private Taetigkeit taetigkeit;

	@Nullable
	private String erwerbspensumInstitution;

	@Nullable
	private String bezeichnung;

	@Nullable
	private JaxUnbezahlterUrlaub unbezahlterUrlaub;

	@Nullable
	private Boolean unregelmaessigeArbeitszeiten;

	@Nullable
	private String wegzeit;


	public Taetigkeit getTaetigkeit() {
		return taetigkeit;
	}

	public void setTaetigkeit(Taetigkeit taetigkeit) {
		this.taetigkeit = taetigkeit;
	}

	@Nullable
	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(@Nullable String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	@Nullable
	public JaxUnbezahlterUrlaub getUnbezahlterUrlaub() {
		return unbezahlterUrlaub;
	}

	public void setUnbezahlterUrlaub(@Nullable JaxUnbezahlterUrlaub unbezahlterUrlaub) {
		this.unbezahlterUrlaub = unbezahlterUrlaub;
	}

	@Nullable
	public Boolean isUnregelmaessigeArbeitszeiten() {
		return unregelmaessigeArbeitszeiten;
	}

	public void setUnregelmaessigeArbeitszeiten(@Nullable Boolean unregelmaessigeArbeitszeiten) {
		this.unregelmaessigeArbeitszeiten = unregelmaessigeArbeitszeiten;
	}

	@Nullable
	public String getErwerbspensumInstitution() {
		return erwerbspensumInstitution;
	}

	public void setErwerbspensumInstitution(@Nullable String erwerbspensumInstitution) {
		this.erwerbspensumInstitution = erwerbspensumInstitution;
	}

	@Nullable
	public String getWegzeit() {
		return wegzeit;
	}

	public void setWegzeit(@Nullable String wegzeit) {
		this.wegzeit = wegzeit;
	}
}
