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
package ch.dvbern.ebegu.dto.geoadmin;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "geoadminSearchResultEntry")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JaxGeoadminSearchResultEntry implements Serializable {

	private static final long serialVersionUID = -2736875261425297974L;

	@NotNull
	@Nonnull
	private Long id = 0L;

	@NotNull
	@Nonnull
	private Long weight = 0L;   // highest weight is best match

	@NotNull
	@Nonnull
	private JaxGeoadminSearchResultEntryAttrs attrs = new JaxGeoadminSearchResultEntryAttrs();

	@Nonnull
	public JaxGeoadminSearchResultEntryAttrs getAttrs() {
		return attrs;
	}

	public void setAttrs(@Nonnull JaxGeoadminSearchResultEntryAttrs attrs) {
		this.attrs = attrs;
	}

	@Nonnull
	public Long getId() {
		return id;
	}

	public void setId(@Nonnull Long id) {
		this.id = id;
	}

	@Nonnull
	public Long getWeight() {
		return weight;
	}

	public void setWeight(@Nonnull Long weight) {
		this.weight = weight;
	}
}
