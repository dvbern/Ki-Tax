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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ArrayUtils;

@XmlRootElement(name = "geoadminFeatureAttributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JaxGeoadminFeatureAttributes implements Serializable {

	private static final long serialVersionUID = 207402652291559292L;

	@Nonnull
	private String ggdename = "";
	@Nonnull
	private String[] strname = new String[0];
	private long gstat;
	@Nonnull
	private String gdekt = "";
	@Nonnull
	private String label = "";
	@Nonnull
	private String lgbkr = "";
	private long egid;
	private long dstrid;
	private long ggdenr;
	@Nonnull
	private String dplz4 = "";

	@XmlElement(name = "plz_plz6")
	@Nonnull
	private String plzPlz6 = "";
	@Nonnull
	private String dplzname = "";
	@Nonnull
	private String deinr = "";

	@Nonnull
	public String getGgdename() {
		return ggdename;
	}

	public void setGgdename(@Nonnull String ggdename) {
		this.ggdename = ggdename;
	}

	@Nonnull
	public String[] getStrname() {
		return ArrayUtils.clone(strname);
	}

	public void setStrname(@Nonnull String[] strname) {
		this.strname = ArrayUtils.clone(strname);
	}

	public long getGstat() {
		return gstat;
	}

	public void setGstat(long gstat) {
		this.gstat = gstat;
	}

	@Nonnull
	public String getGdekt() {
		return gdekt;
	}

	public void setGdekt(@Nonnull String gdekt) {
		this.gdekt = gdekt;
	}

	@Nonnull
	public String getLabel() {
		return label;
	}

	public void setLabel(@Nonnull String label) {
		this.label = label;
	}

	@Nonnull
	public String getLgbkr() {
		return lgbkr;
	}

	public void setLgbkr(@Nonnull String lgbkr) {
		this.lgbkr = lgbkr;
	}

	public long getEgid() {
		return egid;
	}

	public void setEgid(long egid) {
		this.egid = egid;
	}

	public long getDstrid() {
		return dstrid;
	}

	public void setDstrid(long dstrid) {
		this.dstrid = dstrid;
	}

	public long getGgdenr() {
		return ggdenr;
	}

	public void setGgdenr(long ggdenr) {
		this.ggdenr = ggdenr;
	}

	@Nonnull
	public String getDplz4() {
		return dplz4;
	}

	public void setDplz4(@Nonnull String dplz4) {
		this.dplz4 = dplz4;
	}

	@Nonnull
	public String getPlzPlz6() {
		return plzPlz6;
	}

	public void setPlzPlz6(@Nonnull String plzPlz6) {
		this.plzPlz6 = plzPlz6;
	}

	@Nonnull
	public String getDplzname() {
		return dplzname;
	}

	public void setDplzname(@Nonnull String dplzname) {
		this.dplzname = dplzname;
	}

	@Nonnull
	public String getDeinr() {
		return deinr;
	}

	public void setDeinr(@Nonnull String deinr) {
		this.deinr = deinr;
	}
}
