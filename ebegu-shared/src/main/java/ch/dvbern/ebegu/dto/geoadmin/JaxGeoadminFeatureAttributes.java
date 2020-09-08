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

@XmlRootElement(name = "geoadminFeatureAttributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JaxGeoadminFeatureAttributes implements Serializable {

	private static final long serialVersionUID = 207402652291559292L;

	@Nonnull
	private String gdename = "";
	@Nonnull
	private String strname1 = "";
	private long gstat;
	@Nonnull
	private String gdekt = "";
	@Nonnull
	private String label = "";
	@Nonnull
	private String ggbkr = "";
	private long egid;
	private long dstrid;
	private long gdenr;
	// Datum im Format "DD.MM.YYYY"
	@Nonnull
	@XmlElement(name = "bgdi_created")
	private String bgdiCreated = "";
	private long gkplaus;
	@Nonnull
	private String plz4 = "";
	@Nonnull
	private String plz6 = "";
	@Nonnull
	private String plzname = "";
	@Nonnull
	private String deinr = "";

	@Nonnull
	public String getGdename() {
		return gdename;
	}

	public void setGdename(@Nonnull String gdename) {
		this.gdename = gdename;
	}

	@Nonnull
	public String getStrname1() {
		return strname1;
	}

	public void setStrname1(@Nonnull String strname1) {
		this.strname1 = strname1;
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
	public String getGgbkr() {
		return ggbkr;
	}

	public void setGgbkr(@Nonnull String ggbkr) {
		this.ggbkr = ggbkr;
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

	public long getGdenr() {
		return gdenr;
	}

	public void setGdenr(long gdenr) {
		this.gdenr = gdenr;
	}

	@Nonnull
	public String getBgdiCreated() {
		return bgdiCreated;
	}

	public void setBgdiCreated(@Nonnull String bgdiCreated) {
		this.bgdiCreated = bgdiCreated;
	}

	public long getGkplaus() {
		return gkplaus;
	}

	public void setGkplaus(long gkplaus) {
		this.gkplaus = gkplaus;
	}

	@Nonnull
	public String getPlz4() {
		return plz4;
	}

	public void setPlz4(@Nonnull String plz4) {
		this.plz4 = plz4;
	}

	@Nonnull
	public String getPlz6() {
		return plz6;
	}

	public void setPlz6(@Nonnull String plz6) {
		this.plz6 = plz6;
	}

	@Nonnull
	public String getPlzname() {
		return plzname;
	}

	public void setPlzname(@Nonnull String plzname) {
		this.plzname = plzname;
	}

	@Nonnull
	public String getDeinr() {
		return deinr;
	}

	public void setDeinr(@Nonnull String deinr) {
		this.deinr = deinr;
	}
}
