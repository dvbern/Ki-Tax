/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public class LastenausgleichTagesschulenDataRow {

	@Nullable
	private String gemeindeFallnummerTS;

	@Nullable
	private String tagesschuleName;

	@Nullable
	private String tagesschuleID;

	@Nullable
	private Boolean lehrbetrieb;

	@Nullable
	private BigDecimal kinderTotal;

	@Nullable
	private BigDecimal kinderKindergarten;

	@Nullable
	private BigDecimal kinderPrimar;

	@Nullable
	private BigDecimal kinderSek;

	@Nullable
	private BigDecimal kinderFaktor;

	@Nullable
	private BigDecimal kinderFrueh;

	@Nullable
	private BigDecimal kinderMittag;

	@Nullable
	private BigDecimal kinderNachmittag1;

	@Nullable
	private BigDecimal kinderNachmittag2;

	@Nullable
	private BigDecimal betreuungsstundenTagesschule;

	@Nullable
	private Boolean konzeptOrganisatorisch;

	@Nullable
	private Boolean konzeptPaedagogisch;

	@Nullable
	private Boolean raeumeGeeignet;

	@Nullable
	private Boolean betreuungsVerhaeltnis;

	@Nullable
	private Boolean ernaehrung;

	@Nullable
	private String bemerkungenTagesschule;

	@Nullable
	public String getGemeindeFallnummerTS() {
		return gemeindeFallnummerTS;
	}

	public void setGemeindeFallnummerTS(@Nullable String gemeindeFallnummerTS) {
		this.gemeindeFallnummerTS = gemeindeFallnummerTS;
	}

	@Nullable
	public String getTagesschuleName() {
		return tagesschuleName;
	}

	public void setTagesschuleName(@Nullable String tagesschuleName) {
		this.tagesschuleName = tagesschuleName;
	}

	@Nullable
	public String getTagesschuleID() {
		return tagesschuleID;
	}

	public void setTagesschuleID(@Nullable String tagesschuleID) {
		this.tagesschuleID = tagesschuleID;
	}

	@Nullable
	public Boolean getLehrbetrieb() {
		return lehrbetrieb;
	}

	public void setLehrbetrieb(@Nullable Boolean lehrbetrieb) {
		this.lehrbetrieb = lehrbetrieb;
	}

	@Nullable
	public BigDecimal getKinderTotal() {
		return kinderTotal;
	}

	public void setKinderTotal(@Nullable BigDecimal kinderTotal) {
		this.kinderTotal = kinderTotal;
	}

	@Nullable
	public BigDecimal getKinderKindergarten() {
		return kinderKindergarten;
	}

	public void setKinderKindergarten(@Nullable BigDecimal kinderKindergarten) {
		this.kinderKindergarten = kinderKindergarten;
	}

	@Nullable
	public BigDecimal getKinderPrimar() {
		return kinderPrimar;
	}

	public void setKinderPrimar(@Nullable BigDecimal kinderPrimar) {
		this.kinderPrimar = kinderPrimar;
	}

	@Nullable
	public BigDecimal getKinderSek() {
		return kinderSek;
	}

	public void setKinderSek(@Nullable BigDecimal kinderSek) {
		this.kinderSek = kinderSek;
	}

	@Nullable
	public BigDecimal getKinderFaktor() {
		return kinderFaktor;
	}

	public void setKinderFaktor(@Nullable BigDecimal kinderFaktor) {
		this.kinderFaktor = kinderFaktor;
	}

	@Nullable
	public BigDecimal getKinderFrueh() {
		return kinderFrueh;
	}

	public void setKinderFrueh(@Nullable BigDecimal kinderFrueh) {
		this.kinderFrueh = kinderFrueh;
	}

	@Nullable
	public BigDecimal getKinderMittag() {
		return kinderMittag;
	}

	public void setKinderMittag(@Nullable BigDecimal kinderMittag) {
		this.kinderMittag = kinderMittag;
	}

	@Nullable
	public BigDecimal getKinderNachmittag1() {
		return kinderNachmittag1;
	}

	public void setKinderNachmittag1(@Nullable BigDecimal kinderNachmittag1) {
		this.kinderNachmittag1 = kinderNachmittag1;
	}

	@Nullable
	public BigDecimal getKinderNachmittag2() {
		return kinderNachmittag2;
	}

	public void setKinderNachmittag2(@Nullable BigDecimal kinderNachmittag2) {
		this.kinderNachmittag2 = kinderNachmittag2;
	}

	@Nullable
	public BigDecimal getBetreuungsstundenTagesschule() {
		return betreuungsstundenTagesschule;
	}

	public void setBetreuungsstundenTagesschule(@Nullable BigDecimal betreuungsstundenTagesschule) {
		this.betreuungsstundenTagesschule = betreuungsstundenTagesschule;
	}

	@Nullable
	public Boolean getKonzeptOrganisatorisch() {
		return konzeptOrganisatorisch;
	}

	public void setKonzeptOrganisatorisch(@Nullable Boolean konzeptOrganisatorisch) {
		this.konzeptOrganisatorisch = konzeptOrganisatorisch;
	}

	@Nullable
	public Boolean getKonzeptPaedagogisch() {
		return konzeptPaedagogisch;
	}

	public void setKonzeptPaedagogisch(@Nullable Boolean konzeptPaedagogisch) {
		this.konzeptPaedagogisch = konzeptPaedagogisch;
	}

	@Nullable
	public Boolean getRaeumeGeeignet() {
		return raeumeGeeignet;
	}

	public void setRaeumeGeeignet(@Nullable Boolean raeumeGeeignet) {
		this.raeumeGeeignet = raeumeGeeignet;
	}

	@Nullable
	public Boolean getBetreuungsVerhaeltnis() {
		return betreuungsVerhaeltnis;
	}

	public void setBetreuungsVerhaeltnis(@Nullable Boolean betreuungsVerhaeltnis) {
		this.betreuungsVerhaeltnis = betreuungsVerhaeltnis;
	}

	@Nullable
	public Boolean getErnaehrung() {
		return ernaehrung;
	}

	public void setErnaehrung(@Nullable Boolean ernaehrung) {
		this.ernaehrung = ernaehrung;
	}

	@Nullable
	public String getBemerkungenTagesschule() {
		return bemerkungenTagesschule;
	}

	public void setBemerkungenTagesschule(@Nullable String bemerkungenTagesschule) {
		this.bemerkungenTagesschule = bemerkungenTagesschule;
	}
}
