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

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.math.BigDecimal;

/**
 * Superklasse fuer ein Pensum
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxEingewoehnungPauschale extends JaxAbstractDateRangedDTO {


	private static final long serialVersionUID = 5161971483109161443L;

	@Nonnull
	private BigDecimal pauschale;


	@Nonnull
	public BigDecimal getPauschale() {
		return pauschale;
	}

	public void setPauschale(@Nonnull BigDecimal pauschale) {
		this.pauschale = pauschale;
	}
}