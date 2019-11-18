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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer InstitutionStammdatenTagesschule
 */
@XmlRootElement(name = "institutionStammdatenTagesschule")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxInstitutionStammdatenTagesschule extends JaxAbstractDTO {

	private static final long serialVersionUID = 6958211446966611467L;

	@NotNull @Nonnull
	private JaxGemeinde gemeinde;

	@NotNull @Nonnull
	private Set<JaxEinstellungenTagesschule> einstellungenTagesschule = new HashSet<>();


	@Nonnull
	public JaxGemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull JaxGemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	public Set<JaxEinstellungenTagesschule> getEinstellungenTagesschule() {
		return einstellungenTagesschule;
	}

	public void setEinstellungenTagesschule(Set<JaxEinstellungenTagesschule> einstellungenTagesschule) {
		this.einstellungenTagesschule = einstellungenTagesschule;
	}
}
