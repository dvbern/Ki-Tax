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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class JaxGeoadminFeatureResult implements Serializable {

	private static final long serialVersionUID = -6370886656772109580L;

	@NotNull
	@Valid
	@Nonnull
	private JaxGeoadminFeature feature = new JaxGeoadminFeature();

	@Nonnull
	public JaxGeoadminFeature getFeature() {
		return feature;
	}

	public void setFeature(@Nonnull JaxGeoadminFeature feature) {
		this.feature = feature;
	}
}
