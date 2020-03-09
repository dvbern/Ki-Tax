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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * DTO fuer SozialhilfeZeitraum Container
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxSozialhilfeZeitraumContainer extends JaxAbstractDTO {

	private static final long serialVersionUID = 4879926292956257346L;

	@Valid
	private JaxSozialhilfeZeitraum sozialhilfeZeitraumGS;

	@Valid
	private JaxSozialhilfeZeitraum sozialhilfeZeitraumJA;

	public JaxSozialhilfeZeitraum getSozialhilfeZeitraumGS() {
		return sozialhilfeZeitraumGS;
	}

	public void setSozialhilfeZeitraumGS(JaxSozialhilfeZeitraum sozialhilfeZeitraumGS) {
		this.sozialhilfeZeitraumGS = sozialhilfeZeitraumGS;
	}

	public JaxSozialhilfeZeitraum getSozialhilfeZeitraumJA() {
		return sozialhilfeZeitraumJA;
	}

	public void setSozialhilfeZeitraumJA(JaxSozialhilfeZeitraum sozialhilfeZeitraumJA) {
		this.sozialhilfeZeitraumJA = sozialhilfeZeitraumJA;
	}
}
