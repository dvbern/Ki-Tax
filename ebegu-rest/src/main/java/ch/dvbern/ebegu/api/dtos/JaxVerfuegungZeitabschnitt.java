/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.util.Constants;

/**
 * DTO fuer Verfuegung Zeitabschnitte. Gehoert immer zu einer Verfuegung welche weiderum zu einen Betreuung gehoert
 */
@XmlRootElement(name = "pensumFachstelle")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxVerfuegungZeitabschnitt extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = 5116358042804364490L;

	@Max(100)
	@Min(0)
	@NotNull
	private Integer erwerbspensumGS1;

	@Max(100)
	@Min(0)
	@NotNull
	private Integer erwerbspensumGS2;

	@Min(0)
	@NotNull
	private BigDecimal betreuungspensumProzent;

	@Max(100)
	@Min(0)
	@NotNull
	private int fachstellenpensum;

	@Max(100)
	@Min(0)
	@NotNull
	private int anspruchspensumRest;

	@Max(100)
	@Min(0)
	@NotNull
	private int anspruchberechtigtesPensum; // = Anpsruch für diese Kita, bzw. Tagesfamilien

	private boolean zuSpaetEingereicht;

	private boolean minimalesEwpUnterschritten;

	private BigDecimal bgPensum; //min von anspruchberechtigtesPensum und betreuungspensum

	private Integer einkommensjahr;

	@Nullable
	private BigDecimal verfuegteAnzahlZeiteinheiten = null;

	@Nullable
	private BigDecimal anspruchsberechtigteAnzahlZeiteinheiten = null;

	@Nullable
	private PensumUnits zeiteinheit = null;

	private BigDecimal betreuungspensumZeiteinheit;

	private BigDecimal vollkosten = BigDecimal.ZERO;

	private BigDecimal verguenstigungOhneBeruecksichtigungVollkosten;

	private BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag;

	private BigDecimal verguenstigung;

	private BigDecimal minimalerElternbeitrag;

	private BigDecimal minimalerElternbeitragGekuerzt;

	private BigDecimal elternbeitrag = BigDecimal.ZERO;

	private BigDecimal abzugFamGroesse = BigDecimal.ZERO;

	private BigDecimal famGroesse;

	private BigDecimal massgebendesEinkommenVorAbzugFamgr = BigDecimal.ZERO;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private String bemerkungen;

	@NotNull @Nonnull
	private VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus = VerfuegungsZeitabschnittZahlungsstatus.NEU;

	@NotNull @Nonnull
	private VerfuegungsZeitabschnittZahlungsstatus zahlungsstatusMahlzeitenverguenstigung = VerfuegungsZeitabschnittZahlungsstatus.NEU;

	private boolean kategorieMaxEinkommen = false;

	private boolean kategorieKeinPensum = false;

	private boolean sameVerfuegteVerfuegungsrelevanteDaten;

	private boolean sameAusbezahlteVerguenstigung;
	private boolean sameAusbezahlteMahlzeiten;
	private boolean sameVerfuegteMahlzeitenVerguenstigung;

	@Nullable
	private JaxTsCalculationResult tsCalculationResultMitPaedagogischerBetreuung;

	@Nullable
	private JaxTsCalculationResult tsCalculationResultOhnePaedagogischerBetreuung;

	private BigDecimal verguenstigungMahlzeitTotal;

	public Integer getErwerbspensumGS1() {
		return erwerbspensumGS1;
	}

	public void setErwerbspensumGS1(Integer erwerbspensumGS1) {
		this.erwerbspensumGS1 = erwerbspensumGS1;
	}

	public Integer getErwerbspensumGS2() {
		return erwerbspensumGS2;
	}

	public void setErwerbspensumGS2(Integer erwerbspensumGS2) {
		this.erwerbspensumGS2 = erwerbspensumGS2;
	}

	public BigDecimal getBetreuungspensumProzent() {
		return betreuungspensumProzent;
	}

	public void setBetreuungspensumProzent(BigDecimal betreuungspensumProzent) {
		this.betreuungspensumProzent = betreuungspensumProzent;
	}

	public int getFachstellenpensum() {
		return fachstellenpensum;
	}

	public void setFachstellenpensum(int fachstellenpensum) {
		this.fachstellenpensum = fachstellenpensum;
	}

	public int getAnspruchspensumRest() {
		return anspruchspensumRest;
	}

	public void setAnspruchspensumRest(int anspruchspensumRest) {
		this.anspruchspensumRest = anspruchspensumRest;
	}

	public int getAnspruchberechtigtesPensum() {
		return anspruchberechtigtesPensum;
	}

	public void setAnspruchspensumProzent(int anspruchberechtigtesPensum) {
		this.anspruchberechtigtesPensum = anspruchberechtigtesPensum;
	}

	public boolean isZuSpaetEingereicht() {
		return zuSpaetEingereicht;
	}

	public void setZuSpaetEingereicht(boolean zuSpaetEingereicht) {
		this.zuSpaetEingereicht = zuSpaetEingereicht;
	}

	public boolean isMinimalesEwpUnterschritten() {
		return minimalesEwpUnterschritten;
	}

	public void setMinimalesEwpUnterschritten(boolean minimalesEwpUnterschritten) {
		this.minimalesEwpUnterschritten = minimalesEwpUnterschritten;
	}

	public BigDecimal getBgPensum() {
		return bgPensum;
	}

	public void setBgPensum(BigDecimal bgPensum) {
		this.bgPensum = bgPensum;
	}

	public Integer getEinkommensjahr() {
		return einkommensjahr;
	}

	public void setEinkommensjahr(Integer einkommensjahr) {
		this.einkommensjahr = einkommensjahr;
	}

	@Nullable
	public BigDecimal getVerfuegteAnzahlZeiteinheiten() {
		return verfuegteAnzahlZeiteinheiten;
	}

	public void setVerfuegteAnzahlZeiteinheiten(@Nullable BigDecimal verfuegteAnzahlZeiteinheiten) {
		this.verfuegteAnzahlZeiteinheiten = verfuegteAnzahlZeiteinheiten;
	}

	@Nullable
	public BigDecimal getAnspruchsberechtigteAnzahlZeiteinheiten() {
		return anspruchsberechtigteAnzahlZeiteinheiten;
	}

	public void setAnspruchsberechtigteAnzahlZeiteinheiten(
		@Nullable BigDecimal anspruchsberechtigteAnzahlZeiteinheiten) {
		this.anspruchsberechtigteAnzahlZeiteinheiten = anspruchsberechtigteAnzahlZeiteinheiten;
	}

	@Nullable
	public PensumUnits getZeiteinheit() {
		return zeiteinheit;
	}

	public void setZeiteinheit(@Nullable PensumUnits zeiteinheit) {
		this.zeiteinheit = zeiteinheit;
	}

	public BigDecimal getBetreuungspensumZeiteinheit() {
		return betreuungspensumZeiteinheit;
	}

	public void setBetreuungspensumZeiteinheit(BigDecimal betreuungspensumZeiteinheit) {
		this.betreuungspensumZeiteinheit = betreuungspensumZeiteinheit;
	}

	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
	}

	public BigDecimal getVerguenstigungOhneBeruecksichtigungVollkosten() {
		return verguenstigungOhneBeruecksichtigungVollkosten;
	}

	public void setVerguenstigungOhneBeruecksichtigungVollkosten(BigDecimal verguenstigungOhneBeruecksichtigungVollkosten) {
		this.verguenstigungOhneBeruecksichtigungVollkosten = verguenstigungOhneBeruecksichtigungVollkosten;
	}

	public BigDecimal getVerguenstigungOhneBeruecksichtigungMinimalbeitrag() {
		return verguenstigungOhneBeruecksichtigungMinimalbeitrag;
	}

	public void setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag) {
		this.verguenstigungOhneBeruecksichtigungMinimalbeitrag = verguenstigungOhneBeruecksichtigungMinimalbeitrag;
	}

	public BigDecimal getVerguenstigung() {
		return verguenstigung;
	}

	public void setVerguenstigung(BigDecimal verguenstigung) {
		this.verguenstigung = verguenstigung;
	}

	public BigDecimal getMinimalerElternbeitrag() {
		return minimalerElternbeitrag;
	}

	public void setMinimalerElternbeitrag(BigDecimal minimalerElternbeitrag) {
		this.minimalerElternbeitrag = minimalerElternbeitrag;
	}

	public BigDecimal getElternbeitrag() {
		return elternbeitrag;
	}

	public void setElternbeitrag(BigDecimal elternbeitrag) {
		this.elternbeitrag = elternbeitrag;
	}

	public BigDecimal getAbzugFamGroesse() {
		return abzugFamGroesse;
	}

	public void setAbzugFamGroesse(BigDecimal abzugFamGroesse) {
		this.abzugFamGroesse = abzugFamGroesse;
	}

	public BigDecimal getFamGroesse() {
		return famGroesse;
	}

	public void setFamGroesse(BigDecimal famGroesse) {
		this.famGroesse = famGroesse;
	}

	public BigDecimal getMassgebendesEinkommenVorAbzugFamgr() {
		return massgebendesEinkommenVorAbzugFamgr;
	}

	public void setMassgebendesEinkommenVorAbzugFamgr(BigDecimal massgebendesEinkommenVorAbzugFamgr) {
		this.massgebendesEinkommenVorAbzugFamgr = massgebendesEinkommenVorAbzugFamgr;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Nonnull
	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatus() {
		return zahlungsstatus;
	}

	public void setZahlungsstatus(@Nonnull VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus) {
		this.zahlungsstatus = zahlungsstatus;
	}

	@Nonnull
	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatusMahlzeitenverguenstigung() {
		return zahlungsstatusMahlzeitenverguenstigung;
	}

	public void setZahlungsstatusMahlzeitenverguenstigung(@Nonnull VerfuegungsZeitabschnittZahlungsstatus zahlungsstatusMahlzeitenverguenstigung) {
		this.zahlungsstatusMahlzeitenverguenstigung = zahlungsstatusMahlzeitenverguenstigung;
	}

	public boolean isKategorieMaxEinkommen() {
		return kategorieMaxEinkommen;
	}

	public void setKategorieMaxEinkommen(boolean kategorieMaxEinkommen) {
		this.kategorieMaxEinkommen = kategorieMaxEinkommen;
	}

	public boolean isKategorieKeinPensum() {
		return kategorieKeinPensum;
	}

	public void setKategorieKeinPensum(boolean kategorieKeinPensum) {
		this.kategorieKeinPensum = kategorieKeinPensum;
	}

	public boolean isSameVerfuegteVerfuegungsrelevanteDaten() {
		return sameVerfuegteVerfuegungsrelevanteDaten;
	}

	public void setSameVerfuegteVerfuegungsrelevanteDaten(boolean sameVerfuegteVerfuegungsrelevanteDaten) {
		this.sameVerfuegteVerfuegungsrelevanteDaten = sameVerfuegteVerfuegungsrelevanteDaten;
	}

	public boolean isSameAusbezahlteVerguenstigung() {
		return sameAusbezahlteVerguenstigung;
	}

	public void setSameAusbezahlteVerguenstigung(boolean sameAusbezahlteVerguenstigung) {
		this.sameAusbezahlteVerguenstigung = sameAusbezahlteVerguenstigung;
	}

	public boolean isSameAusbezahlteMahlzeiten() {
		return sameAusbezahlteMahlzeiten;
	}

	public void setSameAusbezahlteMahlzeiten(boolean sameAusbezahlteMahlzeiten) {
		this.sameAusbezahlteMahlzeiten = sameAusbezahlteMahlzeiten;
	}

	public BigDecimal getMinimalerElternbeitragGekuerzt() {
		return minimalerElternbeitragGekuerzt;
	}

	public void setMinimalerElternbeitragGekuerzt(BigDecimal minimalerElternbeitragGekuerzt) {
		this.minimalerElternbeitragGekuerzt = minimalerElternbeitragGekuerzt;
	}

	@Nullable
	public JaxTsCalculationResult getTsCalculationResultMitPaedagogischerBetreuung() {
		return tsCalculationResultMitPaedagogischerBetreuung;
	}

	public void setTsCalculationResultMitPaedagogischerBetreuung(@Nullable JaxTsCalculationResult tsCalculationResultMitPaedagogischerBetreuung) {
		this.tsCalculationResultMitPaedagogischerBetreuung = tsCalculationResultMitPaedagogischerBetreuung;
	}

	@Nullable
	public JaxTsCalculationResult getTsCalculationResultOhnePaedagogischerBetreuung() {
		return tsCalculationResultOhnePaedagogischerBetreuung;
	}

	public void setTsCalculationResultOhnePaedagogischerBetreuung(@Nullable JaxTsCalculationResult tsCalculationResultOhnePaedagogischerBetreuung) {
		this.tsCalculationResultOhnePaedagogischerBetreuung = tsCalculationResultOhnePaedagogischerBetreuung;
	}

	public BigDecimal getVerguenstigungMahlzeitTotal() {
		return verguenstigungMahlzeitTotal;
	}

	public void setVerguenstigungMahlzeitTotal(BigDecimal verguenstigungMahlzeitTotal) {
		this.verguenstigungMahlzeitTotal = verguenstigungMahlzeitTotal;
	}

	public boolean isSameVerfuegteMahlzeitenVerguenstigung() {
		return sameVerfuegteMahlzeitenVerguenstigung;
	}

	public void setSameVerfuegteMahlzeitenVerguenstigung(boolean sameVerfuegteMahlzeitenVerguenstigung) {
		this.sameVerfuegteMahlzeitenVerguenstigung = sameVerfuegteMahlzeitenVerguenstigung;
	}
}
