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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "geoadminFeature")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JaxGeoadminFeature implements Serializable {

	private static final long serialVersionUID = 5376679830957318824L;

	@NotNull
	@Nonnull
	private String featureId = "";

	@NotNull
	@Nonnull
	private JaxGeoadminFeatureAttributes attributes = new JaxGeoadminFeatureAttributes();

	@NotNull
	@Nonnull
	private String layerBodId = "";

	@NotNull
	@Nonnull
	private String layerName = "";

	@NotNull
	@Nonnull
	private String id = "";

	@Nonnull
	@NotNull
	private List<BigDecimal> bbox = new ArrayList<>();

	@Nonnull
	public String getFeatureId() {
		return featureId;
	}

	public void setFeatureId(@Nonnull String featureId) {
		this.featureId = featureId;
	}

	@Nonnull
	public JaxGeoadminFeatureAttributes getAttributes() {
		return attributes;
	}

	public void setAttributes(@Nonnull JaxGeoadminFeatureAttributes attributes) {
		this.attributes = attributes;
	}

	@Nonnull
	public String getLayerBodId() {
		return layerBodId;
	}

	public void setLayerBodId(@Nonnull String layerBodId) {
		this.layerBodId = layerBodId;
	}

	@Nonnull
	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(@Nonnull String layerName) {
		this.layerName = layerName;
	}

	@Nonnull
	public String getId() {
		return id;
	}

	public void setId(@Nonnull String id) {
		this.id = id;
	}

	@Nonnull
	public List<BigDecimal> getBbox() {
		return bbox;
	}

	public void setBbox(@Nonnull List<BigDecimal> bbox) {
		this.bbox = bbox;
	}
}
