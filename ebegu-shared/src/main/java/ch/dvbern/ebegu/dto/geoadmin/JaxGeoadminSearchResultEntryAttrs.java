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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "geoadminSearchResultEntryAttrs")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JaxGeoadminSearchResultEntryAttrs implements Serializable {

	private static final long serialVersionUID = 955410499468586109L;

	@NotNull
	@Nonnull
	private Integer num = 0;

	@NotNull
	@Nonnull
	private Integer rank = 0;

	@NotNull
	@Nonnull
	private String detail = "";

	@NotNull
	@Nonnull
	@XmlElement(name = "geom_quadindex")
	private String geomQuadindex = "";

	@NotNull
	@Nonnull
	@XmlElement(name = "geom_st_box2d")
	private String geomStBox2d = "";

	// Beispiel: "ch.bfs.gebaeude_wohnungs_register"
	@NotNull
	@Nonnull
	private String layerBodId = "";

	@NotNull
	@Nonnull
	private String layer = "";

	// Beispiel (wenn layerBodId = ch.bfs.gebaeude_wohnungs_register): 190197872_0
	@NotNull
	@Nonnull
	private String featureId = "";

	@NotNull
	@Nonnull
	private String origin = "";

	@NotNull
	@Nonnull
	private String label = "";

	@Nullable
	private BigDecimal lat = null;

	@Nullable
	private BigDecimal lon = null;

	@Nullable
	private BigDecimal zoomlevel = null;

	@Nullable
	private BigDecimal x = null;

	@Nullable
	private BigDecimal y = null;

	@Nonnull
	public Integer getNum() {
		return num;
	}

	public void setNum(@Nonnull Integer num) {
		this.num = num;
	}

	@Nonnull
	public Integer getRank() {
		return rank;
	}

	public void setRank(@Nonnull Integer rank) {
		this.rank = rank;
	}

	@Nonnull
	public String getDetail() {
		return detail;
	}

	public void setDetail(@Nonnull String detail) {
		this.detail = detail;
	}

	@Nonnull
	public String getGeomQuadindex() {
		return geomQuadindex;
	}

	public void setGeomQuadindex(@Nonnull String geomQuadindex) {
		this.geomQuadindex = geomQuadindex;
	}

	@Nonnull
	public String getGeomStBox2d() {
		return geomStBox2d;
	}

	public void setGeomStBox2d(@Nonnull String geomStBox2d) {
		this.geomStBox2d = geomStBox2d;
	}

	@Nonnull
	public String getLayerBodId() {
		return layerBodId;
	}

	public void setLayerBodId(@Nonnull String layerBodId) {
		this.layerBodId = layerBodId;
	}

	@Nonnull
	public String getLayer() {
		return layer;
	}

	public void setLayer(@Nonnull String layer) {
		this.layer = layer;
	}

	@Nonnull
	public String getFeatureId() {
		return featureId;
	}

	public void setFeatureId(@Nonnull String featureId) {
		this.featureId = featureId;
	}

	@Nonnull
	public String getOrigin() {
		return origin;
	}

	public void setOrigin(@Nonnull String origin) {
		this.origin = origin;
	}

	@Nonnull
	public String getLabel() {
		return label;
	}

	public void setLabel(@Nonnull String label) {
		this.label = label;
	}

	@Nullable
	public BigDecimal getLat() {
		return lat;
	}

	public void setLat(@Nullable BigDecimal lat) {
		this.lat = lat;
	}

	@Nullable
	public BigDecimal getLon() {
		return lon;
	}

	public void setLon(@Nullable BigDecimal lon) {
		this.lon = lon;
	}

	@Nullable
	public BigDecimal getZoomlevel() {
		return zoomlevel;
	}

	public void setZoomlevel(@Nullable BigDecimal zoomlevel) {
		this.zoomlevel = zoomlevel;
	}

	@Nullable
	public BigDecimal getX() {
		return x;
	}

	public void setX(@Nullable BigDecimal x) {
		this.x = x;
	}

	@Nullable
	public BigDecimal getY() {
		return y;
	}

	public void setY(@Nullable BigDecimal y) {
		this.y = y;
	}
}
