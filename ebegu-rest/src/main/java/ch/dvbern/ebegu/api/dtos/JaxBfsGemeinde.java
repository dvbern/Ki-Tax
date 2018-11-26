/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer BFS-Gemeinden. Im Moment synchronisieren wir nur die BFS-Nummer und den Namen zum Client.
 */
@XmlRootElement(name = "bfs_gemeinde")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBfsGemeinde extends JaxAbstractDTO {

	private static final long serialVersionUID = 7980499854206395920L;

	@NotNull
	private String name;

	@Nonnull
	private Long bfsNummer;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Nonnull
	public Long getBfsNummer() {
		return bfsNummer;
	}

	public void setBfsNummer(@Nonnull Long bfsNummer) {
		this.bfsNummer = bfsNummer;
	}
}
