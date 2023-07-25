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
	private BigDecimal kinderFaktor15;

	@Nullable
	private BigDecimal kinderFaktor3;

	@Nullable
	private BigDecimal kinderFrueh;

	@Nullable
	private BigDecimal kinderMittag;

	@Nullable
	private BigDecimal kinderNachmittag1;

	@Nullable
	private BigDecimal kinderNachmittag2;

	@Nullable
	private BigDecimal kinderBasisstufe;

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
	private Boolean fruehBetMo;

	@Nullable
	private Boolean fruehBetDi;

	@Nullable
	private Boolean fruehBetMi;

	@Nullable
	private Boolean fruehBetDo;

	@Nullable
	private Boolean fruehBetFr;

	@Nullable
	private Boolean mittagsBetMo;

	@Nullable
	private Boolean mittagsBetDi;

	@Nullable
	private Boolean mittagsBetMi;

	@Nullable
	private Boolean mittagsBetDo;

	@Nullable
	private Boolean mittagsBetFr;

	@Nullable
	private Boolean nachmittags1BetMo;

	@Nullable
	private Boolean nachmittags1BetDi;

	@Nullable
	private Boolean nachmittags1BetMi;

	@Nullable
	private Boolean nachmittags1BetDo;

	@Nullable
	private Boolean nachmittags1BetFr;

	@Nullable
	private Boolean nachmittags2BetMo;

	@Nullable
	private Boolean nachmittags2BetDi;

	@Nullable
	private Boolean nachmittags2BetMi;

	@Nullable
	private Boolean nachmittags2BetDo;

	@Nullable
	private Boolean nachmittags2BetFr;

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
	public BigDecimal getKinderFaktor15() {
		return kinderFaktor15;
	}

	public void setKinderFaktor15(@Nullable BigDecimal kinderFaktor15) {
		this.kinderFaktor15 = kinderFaktor15;
	}

	@Nullable
	public BigDecimal getKinderFaktor3() {
		return kinderFaktor3;
	}

	public void setKinderFaktor3(@Nullable BigDecimal kinderFaktor3) {
		this.kinderFaktor3 = kinderFaktor3;
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
	public BigDecimal getKinderBasisstufe() {
		return kinderBasisstufe;
	}

	public void setKinderBasisstufe(@Nullable BigDecimal kinderBasisstufe) {
		this.kinderBasisstufe = kinderBasisstufe;
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

	@Nullable
	public Boolean getFruehBetMo() {
		return fruehBetMo;
	}

	public void setFruehBetMo(@Nullable Boolean fruehBetMo) {
		this.fruehBetMo = fruehBetMo;
	}

	@Nullable
	public Boolean getFruehBetDi() {
		return fruehBetDi;
	}

	public void setFruehBetDi(@Nullable Boolean fruehBetDi) {
		this.fruehBetDi = fruehBetDi;
	}

	@Nullable
	public Boolean getFruehBetMi() {
		return fruehBetMi;
	}

	public void setFruehBetMi(@Nullable Boolean fruehBetMi) {
		this.fruehBetMi = fruehBetMi;
	}

	@Nullable
	public Boolean getFruehBetDo() {
		return fruehBetDo;
	}

	public void setFruehBetDo(@Nullable Boolean fruehBetDo) {
		this.fruehBetDo = fruehBetDo;
	}

	@Nullable
	public Boolean getFruehBetFr() {
		return fruehBetFr;
	}

	public void setFruehBetFr(@Nullable Boolean fruehBetFr) {
		this.fruehBetFr = fruehBetFr;
	}

	@Nullable
	public Boolean getMittagsBetMo() {
		return mittagsBetMo;
	}

	public void setMittagsBetMo(@Nullable Boolean mittagsBetMo) {
		this.mittagsBetMo = mittagsBetMo;
	}

	@Nullable
	public Boolean getMittagsBetDi() {
		return mittagsBetDi;
	}

	public void setMittagsBetDi(@Nullable Boolean mittagsBetDi) {
		this.mittagsBetDi = mittagsBetDi;
	}

	@Nullable
	public Boolean getMittagsBetMi() {
		return mittagsBetMi;
	}

	public void setMittagsBetMi(@Nullable Boolean mittagsBetMi) {
		this.mittagsBetMi = mittagsBetMi;
	}

	@Nullable
	public Boolean getMittagsBetDo() {
		return mittagsBetDo;
	}

	public void setMittagsBetDo(@Nullable Boolean mittagsBetDo) {
		this.mittagsBetDo = mittagsBetDo;
	}

	@Nullable
	public Boolean getMittagsBetFr() {
		return mittagsBetFr;
	}

	public void setMittagsBetFr(@Nullable Boolean mittagsBetFr) {
		this.mittagsBetFr = mittagsBetFr;
	}

	@Nullable
	public Boolean getNachmittags1BetMo() {
		return nachmittags1BetMo;
	}

	public void setNachmittags1BetMo(@Nullable Boolean nachmittags1BetMo) {
		this.nachmittags1BetMo = nachmittags1BetMo;
	}

	@Nullable
	public Boolean getNachmittags1BetDi() {
		return nachmittags1BetDi;
	}

	public void setNachmittags1BetDi(@Nullable Boolean nachmittags1BetDi) {
		this.nachmittags1BetDi = nachmittags1BetDi;
	}

	@Nullable
	public Boolean getNachmittags1BetMi() {
		return nachmittags1BetMi;
	}

	public void setNachmittags1BetMi(@Nullable Boolean nachmittags1BetMi) {
		this.nachmittags1BetMi = nachmittags1BetMi;
	}

	@Nullable
	public Boolean getNachmittags1BetDo() {
		return nachmittags1BetDo;
	}

	public void setNachmittags1BetDo(@Nullable Boolean nachmittags1BetDo) {
		this.nachmittags1BetDo = nachmittags1BetDo;
	}

	@Nullable
	public Boolean getNachmittags1BetFr() {
		return nachmittags1BetFr;
	}

	public void setNachmittags1BetFr(@Nullable Boolean nachmittags1BetFr) {
		this.nachmittags1BetFr = nachmittags1BetFr;
	}

	@Nullable
	public Boolean getNachmittags2BetMo() {
		return nachmittags2BetMo;
	}

	public void setNachmittags2BetMo(@Nullable Boolean nachmittags2BetMo) {
		this.nachmittags2BetMo = nachmittags2BetMo;
	}

	@Nullable
	public Boolean getNachmittags2BetDi() {
		return nachmittags2BetDi;
	}

	public void setNachmittags2BetDi(@Nullable Boolean nachmittags2BetDi) {
		this.nachmittags2BetDi = nachmittags2BetDi;
	}

	@Nullable
	public Boolean getNachmittags2BetMi() {
		return nachmittags2BetMi;
	}

	public void setNachmittags2BetMi(@Nullable Boolean nachmittags2BetMi) {
		this.nachmittags2BetMi = nachmittags2BetMi;
	}

	@Nullable
	public Boolean getNachmittags2BetDo() {
		return nachmittags2BetDo;
	}

	public void setNachmittags2BetDo(@Nullable Boolean nachmittags2BetDo) {
		this.nachmittags2BetDo = nachmittags2BetDo;
	}

	@Nullable
	public Boolean getNachmittags2BetFr() {
		return nachmittags2BetFr;
	}

	public void setNachmittags2BetFr(@Nullable Boolean nachmittags2BetFr) {
		this.nachmittags2BetFr = nachmittags2BetFr;
	}
}
