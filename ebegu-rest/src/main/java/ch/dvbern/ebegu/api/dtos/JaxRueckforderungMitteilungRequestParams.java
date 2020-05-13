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

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.RueckforderungStatus;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxRueckforderungMitteilungRequestParams extends JaxAbstractDTO {

	private static final long serialVersionUID = -8553194968646128553L;

	@Nonnull
	private JaxRueckforderungMitteilung mitteilung;

	@Nonnull
	private List<RueckforderungStatus> statusList;

	@Nonnull
	public JaxRueckforderungMitteilung getMitteilung() {
		return mitteilung;
	}

	public void setMitteilung(@Nonnull JaxRueckforderungMitteilung mitteilung) {
		this.mitteilung = mitteilung;
	}

	@Nonnull
	public List<RueckforderungStatus> getStatusList() {
		return statusList;
	}

	public void setStatusList(@Nonnull List<RueckforderungStatus> statusList) {
		this.statusList = statusList;
	}
}
