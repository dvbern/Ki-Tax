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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.dto;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.util.MathUtil;

public class BGCalculationInput {

	@Transient
	private VerfuegungZeitabschnitt parent;

	private boolean sameVerfuegteVerfuegungsrelevanteDaten;

	// Dieser Wert wird gebraucht, um zu wissen ob die Korrektur relevant fuer die Zahlungen ist, da nur wenn die
	// Verguenstigung sich geaendert hat, muss man die Korrektur beruecksichtigen
	private boolean sameAusbezahlteVerguenstigung;

	@Nullable
	private Integer erwerbspensumGS1 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Nullable
	private Integer erwerbspensumGS2 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	private Set<Taetigkeit> taetigkeiten = new HashSet<>();

	private int fachstellenpensum;

	private int ausserordentlicherAnspruch;

	//es muss by default null sein um zu wissen, wann es nicht definiert wurde
	private Boolean wohnsitzNichtInGemeindeGS1 = null;

	// Wenn Vollkosten bezahlt werden muessen, werden die Vollkosten berechnet und als Elternbeitrag gesetzt
	private boolean bezahltVollkosten;

	private boolean longAbwesenheit;

	private int anspruchspensumRest;

	// Achtung, dieses Flag wird erst ab 1. des Folgemonats gesetzt, weil die Finanzielle Situation ab dann gilt. Für
	// Erwerbspensen zählt der GS2 ab sofort!
	private boolean hasSecondGesuchstellerForFinanzielleSituation;

	private boolean ekv1Alleine;

	private boolean ekv1ZuZweit;

	private boolean ekv2Alleine;

	private boolean ekv2ZuZweit;

	private boolean kategorieMaxEinkommen = false;

	private boolean kategorieKeinPensum = false;

	private boolean abschnittLiegtNachBEGUStartdatum = true;

	private BigDecimal monatlicheBetreuungskosten = BigDecimal.ZERO;

	private boolean babyTarif;

	private boolean eingeschult;


	// Zusätzliche Felder aus Result. Diese müssen nach Abschluss der Rules auf das Result kopiert werden
	// Start
	private int anspruchspensumProzent;

	@NotNull
	@Nonnull
	private BigDecimal betreuungspensumProzent = BigDecimal.ZERO;

	@NotNull
	@Nonnull
	private BigDecimal massgebendesEinkommenVorAbzugFamgr = BigDecimal.ZERO;

	private boolean besondereBeduerfnisseBestaetigt;

	@Nullable
	private BigDecimal abzugFamGroesse = null;

	@NotNull @Nonnull
	private Integer einkommensjahr;

	private boolean zuSpaetEingereicht;

	private boolean minimalesEwpUnterschritten;

	@Nullable
	private BigDecimal famGroesse = null;

	@NotNull @Nonnull
	private Integer tsBetreuungszeitProWocheMitBetreuung = 0;

	@NotNull @Nonnull
	private BigDecimal tsVerpflegungskostenMitBetreuung = BigDecimal.ZERO;

	@NotNull @Nonnull
	private BigDecimal tsGebuehrProStundeMitBetreuung = BigDecimal.ZERO;

	@NotNull @Nonnull
	private BigDecimal tsTotalKostenProWocheMitBetreuung = BigDecimal.ZERO;

	@NotNull @Nonnull
	private Integer tsBetreuungszeitProWocheOhneBetreuung = 0;

	@NotNull @Nonnull
	private BigDecimal tsVerpflegungskostenOhneBetreuung = BigDecimal.ZERO;

	@NotNull @Nonnull
	private BigDecimal tsGebuehrProStundeOhneBetreuung = BigDecimal.ZERO;

	@NotNull @Nonnull
	private BigDecimal tsTotalKostenProWocheOhneBetreuung = BigDecimal.ZERO;

	// Ende

	public BGCalculationInput(VerfuegungZeitabschnitt parent) {
		this.parent = parent;
	}

	public BGCalculationInput(@Nonnull BGCalculationInput toCopy) {
		this.parent = toCopy.parent;
		this.erwerbspensumGS1 = toCopy.erwerbspensumGS1;
		this.erwerbspensumGS2 = toCopy.erwerbspensumGS2;
		this.taetigkeiten = toCopy.taetigkeiten;
		this.fachstellenpensum = toCopy.fachstellenpensum;
		this.ausserordentlicherAnspruch = toCopy.ausserordentlicherAnspruch;
		this.wohnsitzNichtInGemeindeGS1 = toCopy.wohnsitzNichtInGemeindeGS1;
		this.bezahltVollkosten = toCopy.bezahltVollkosten;
		this.longAbwesenheit = toCopy.isLongAbwesenheit();
		this.anspruchspensumRest = toCopy.anspruchspensumRest;
		this.monatlicheBetreuungskosten = toCopy.monatlicheBetreuungskosten;
		this.hasSecondGesuchstellerForFinanzielleSituation = toCopy.hasSecondGesuchstellerForFinanzielleSituation;
		this.ekv1Alleine = toCopy.ekv1Alleine;
		this.ekv1ZuZweit = toCopy.ekv1ZuZweit;
		this.ekv2Alleine = toCopy.ekv2Alleine;
		this.ekv2ZuZweit = toCopy.ekv2ZuZweit;
		this.kategorieMaxEinkommen = toCopy.kategorieMaxEinkommen;
		this.kategorieKeinPensum = toCopy.kategorieKeinPensum;
		this.abschnittLiegtNachBEGUStartdatum = toCopy.abschnittLiegtNachBEGUStartdatum;
		this.babyTarif = toCopy.babyTarif;
		this.eingeschult = toCopy.eingeschult;
		this.betreuungspensumProzent = toCopy.betreuungspensumProzent;
		this.anspruchspensumProzent = toCopy.anspruchspensumProzent;
		this.einkommensjahr = toCopy.einkommensjahr;
		this.abzugFamGroesse = toCopy.abzugFamGroesse;
		this.famGroesse = toCopy.famGroesse;
		this.massgebendesEinkommenVorAbzugFamgr = toCopy.massgebendesEinkommenVorAbzugFamgr;
		this.besondereBeduerfnisseBestaetigt = toCopy.besondereBeduerfnisseBestaetigt;
		this.zuSpaetEingereicht = toCopy.zuSpaetEingereicht;
		this.minimalesEwpUnterschritten = toCopy.minimalesEwpUnterschritten;
		this.tsBetreuungszeitProWocheMitBetreuung = toCopy.tsBetreuungszeitProWocheMitBetreuung;
		this.tsVerpflegungskostenMitBetreuung = toCopy.tsVerpflegungskostenMitBetreuung;
		this.tsGebuehrProStundeMitBetreuung = toCopy.tsGebuehrProStundeMitBetreuung;
		this.tsTotalKostenProWocheMitBetreuung = toCopy.tsTotalKostenProWocheMitBetreuung;
		this.tsBetreuungszeitProWocheOhneBetreuung = toCopy.tsBetreuungszeitProWocheOhneBetreuung;
		this.tsVerpflegungskostenOhneBetreuung = toCopy.tsVerpflegungskostenOhneBetreuung;
		this.tsGebuehrProStundeOhneBetreuung = toCopy.tsGebuehrProStundeOhneBetreuung;
		this.tsTotalKostenProWocheOhneBetreuung = toCopy.tsTotalKostenProWocheOhneBetreuung;
	}

	public VerfuegungZeitabschnitt getParent() {
		return parent;
	}

	public void setParent(VerfuegungZeitabschnitt parent) {
		this.parent = parent;
	}

	@Nullable
	public Integer getErwerbspensumGS1() {
		return erwerbspensumGS1;
	}

	public void setErwerbspensumGS1(@Nullable Integer erwerbspensumGS1) {
		this.erwerbspensumGS1 = erwerbspensumGS1;
	}

	@Nullable
	public Integer getErwerbspensumGS2() {
		return erwerbspensumGS2;
	}

	public void setErwerbspensumGS2(@Nullable Integer erwerbspensumGS2) {
		this.erwerbspensumGS2 = erwerbspensumGS2;
	}

	public Set<Taetigkeit> getTaetigkeiten() {
		return taetigkeiten;
	}

	public void setTaetigkeiten(Set<Taetigkeit> taetigkeiten) {
		this.taetigkeiten = taetigkeiten;
	}

	public int getFachstellenpensum() {
		return fachstellenpensum;
	}

	public void setFachstellenpensum(int fachstellenpensum) {
		this.fachstellenpensum = fachstellenpensum;
	}

	public int getAusserordentlicherAnspruch() {
		return ausserordentlicherAnspruch;
	}

	public void setAusserordentlicherAnspruch(int ausserordentlicherAnspruch) {
		this.ausserordentlicherAnspruch = ausserordentlicherAnspruch;
	}

	public int getAnspruchspensumRest() {
		return anspruchspensumRest;
	}

	public void setAnspruchspensumRest(int anspruchspensumRest) {
		this.anspruchspensumRest = anspruchspensumRest;
	}

	public boolean isHasSecondGesuchstellerForFinanzielleSituation() {
		return hasSecondGesuchstellerForFinanzielleSituation;
	}

	public void setHasSecondGesuchstellerForFinanzielleSituation(boolean hasSecondGesuchstellerForFinanzielleSituation) {
		this.hasSecondGesuchstellerForFinanzielleSituation = hasSecondGesuchstellerForFinanzielleSituation;
	}

	public boolean isBezahltVollkosten() {
		return bezahltVollkosten;
	}

	public void setBezahltVollkosten(boolean bezahltVollkosten) {
		this.bezahltVollkosten = bezahltVollkosten;
	}

	public boolean isLongAbwesenheit() {
		return longAbwesenheit;
	}

	public void setLongAbwesenheit(boolean longAbwesenheit) {
		this.longAbwesenheit = longAbwesenheit;
	}

	public Boolean isWohnsitzNichtInGemeindeGS1() {
		return wohnsitzNichtInGemeindeGS1 != null ? wohnsitzNichtInGemeindeGS1 : true;
	}

	public void setWohnsitzNichtInGemeindeGS1(Boolean wohnsitzNichtInGemeindeGS1) {
		this.wohnsitzNichtInGemeindeGS1 = wohnsitzNichtInGemeindeGS1;
	}

	public boolean isEkv1Alleine() {
		return ekv1Alleine;
	}

	public void setEkv1Alleine(boolean ekv1Alleine) {
		this.ekv1Alleine = ekv1Alleine;
	}

	public boolean isEkv1ZuZweit() {
		return ekv1ZuZweit;
	}

	public void setEkv1ZuZweit(boolean ekv1ZuZweit) {
		this.ekv1ZuZweit = ekv1ZuZweit;
	}

	public boolean isEkv2Alleine() {
		return ekv2Alleine;
	}

	public void setEkv2Alleine(boolean ekv2Alleine) {
		this.ekv2Alleine = ekv2Alleine;
	}

	public boolean isEkv2ZuZweit() {
		return ekv2ZuZweit;
	}

	public void setEkv2ZuZweit(boolean ekv2ZuZweit) {
		this.ekv2ZuZweit = ekv2ZuZweit;
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

	public boolean isAbschnittLiegtNachBEGUStartdatum() {
		return abschnittLiegtNachBEGUStartdatum;
	}

	public void setAbschnittLiegtNachBEGUStartdatum(boolean abschnittLiegtNachBEGUStartdatum) {
		this.abschnittLiegtNachBEGUStartdatum = abschnittLiegtNachBEGUStartdatum;
	}

	public BigDecimal getMonatlicheBetreuungskosten() {
		return monatlicheBetreuungskosten;
	}

	public void setMonatlicheBetreuungskosten(BigDecimal monatlicheBetreuungskosten) {
		this.monatlicheBetreuungskosten = monatlicheBetreuungskosten;
	}

	public boolean isBabyTarif() {
		return babyTarif;
	}

	public void setBabyTarif(boolean babyTarif) {
		this.babyTarif = babyTarif;
	}

	public boolean isEingeschult() {
		return eingeschult;
	}

	public void setEingeschult(boolean eingeschult) {
		this.eingeschult = eingeschult;
	}

	public int getAnspruchspensumProzent() {
		return anspruchspensumProzent;
	}

	public void setAnspruchspensumProzent(int anspruchspensumProzent) {
		this.anspruchspensumProzent = anspruchspensumProzent;
	}

	@Nonnull
	public BigDecimal getBetreuungspensumProzent() {
		return betreuungspensumProzent;
	}

	public void setBetreuungspensumProzent(@Nonnull BigDecimal betreuungspensumProzent) {
		this.betreuungspensumProzent = betreuungspensumProzent;
	}

	@Nonnull
	public BigDecimal getMassgebendesEinkommenVorAbzugFamgr() {
		return massgebendesEinkommenVorAbzugFamgr;
	}

	public void setMassgebendesEinkommenVorAbzugFamgr(@Nonnull BigDecimal massgebendesEinkommenVorAbzugFamgr) {
		this.massgebendesEinkommenVorAbzugFamgr = massgebendesEinkommenVorAbzugFamgr;
	}

	public boolean isBesondereBeduerfnisseBestaetigt() {
		return besondereBeduerfnisseBestaetigt;
	}

	public void setBesondereBeduerfnisseBestaetigt(boolean besondereBeduerfnisseBestaetigt) {
		this.besondereBeduerfnisseBestaetigt = besondereBeduerfnisseBestaetigt;
	}

	@Nullable
	public BigDecimal getAbzugFamGroesse() {
		return abzugFamGroesse;
	}

	public void setAbzugFamGroesse(@Nullable BigDecimal abzugFamGroesse) {
		this.abzugFamGroesse = abzugFamGroesse;
	}

	@Nonnull
	public Integer getEinkommensjahr() {
		return einkommensjahr;
	}

	public void setEinkommensjahr(@Nonnull Integer einkommensjahr) {
		this.einkommensjahr = einkommensjahr;
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

	@Nullable
	public BigDecimal getFamGroesse() {
		return famGroesse;
	}

	public void setFamGroesse(@Nullable BigDecimal famGroesse) {
		this.famGroesse = famGroesse;
	}

	@Nonnull
	public Integer getTsBetreuungszeitProWocheMitBetreuung() {
		return tsBetreuungszeitProWocheMitBetreuung;
	}

	public void setTsBetreuungszeitProWocheMitBetreuung(@Nonnull Integer tsBetreuungszeitProWocheMitBetreuung) {
		this.tsBetreuungszeitProWocheMitBetreuung = tsBetreuungszeitProWocheMitBetreuung;
	}

	@Nonnull
	public BigDecimal getTsVerpflegungskostenMitBetreuung() {
		return tsVerpflegungskostenMitBetreuung;
	}

	public void setTsVerpflegungskostenMitBetreuung(@Nonnull BigDecimal tsVerpflegungskostenMitBetreuung) {
		this.tsVerpflegungskostenMitBetreuung = tsVerpflegungskostenMitBetreuung;
	}

	@Nonnull
	public BigDecimal getTsGebuehrProStundeMitBetreuung() {
		return tsGebuehrProStundeMitBetreuung;
	}

	public void setTsGebuehrProStundeMitBetreuung(@Nonnull BigDecimal tsGebuehrProStundeMitBetreuung) {
		this.tsGebuehrProStundeMitBetreuung = tsGebuehrProStundeMitBetreuung;
	}

	@Nonnull
	public BigDecimal getTsTotalKostenProWocheMitBetreuung() {
		return tsTotalKostenProWocheMitBetreuung;
	}

	public void setTsTotalKostenProWocheMitBetreuung(@Nonnull BigDecimal tsTotalKostenProWocheMitBetreuung) {
		this.tsTotalKostenProWocheMitBetreuung = tsTotalKostenProWocheMitBetreuung;
	}

	@Nonnull
	public Integer getTsBetreuungszeitProWocheOhneBetreuung() {
		return tsBetreuungszeitProWocheOhneBetreuung;
	}

	public void setTsBetreuungszeitProWocheOhneBetreuung(@Nonnull Integer tsBetreuungszeitProWocheOhneBetreuung) {
		this.tsBetreuungszeitProWocheOhneBetreuung = tsBetreuungszeitProWocheOhneBetreuung;
	}

	@Nonnull
	public BigDecimal getTsVerpflegungskostenOhneBetreuung() {
		return tsVerpflegungskostenOhneBetreuung;
	}

	public void setTsVerpflegungskostenOhneBetreuung(@Nonnull BigDecimal tsVerpflegungskostenOhneBetreuung) {
		this.tsVerpflegungskostenOhneBetreuung = tsVerpflegungskostenOhneBetreuung;
	}

	@Nonnull
	public BigDecimal getTsGebuehrProStundeOhneBetreuung() {
		return tsGebuehrProStundeOhneBetreuung;
	}

	public void setTsGebuehrProStundeOhneBetreuung(@Nonnull BigDecimal tsGebuehrProStundeOhneBetreuung) {
		this.tsGebuehrProStundeOhneBetreuung = tsGebuehrProStundeOhneBetreuung;
	}

	@Nonnull
	public BigDecimal getTsTotalKostenProWocheOhneBetreuung() {
		return tsTotalKostenProWocheOhneBetreuung;
	}

	public void setTsTotalKostenProWocheOhneBetreuung(@Nonnull BigDecimal tsTotalKostenProWocheOhneBetreuung) {
		this.tsTotalKostenProWocheOhneBetreuung = tsTotalKostenProWocheOhneBetreuung;
	}

	@Override
	public String toString() {
		String sb = "EP GS1: " + getErwerbspensumGS1() + '\t'
			+ " EP GS2: " + getErwerbspensumGS2() + '\t'
			+ " Restanspruch: " + getAnspruchspensumRest();
		return sb;
	}

	public void add(@Nonnull BGCalculationInput other) {
		this.setFachstellenpensum(this.getFachstellenpensum() + other.getFachstellenpensum());
		this.setAusserordentlicherAnspruch(this.getAusserordentlicherAnspruch()
			+ other.getAusserordentlicherAnspruch());
		this.setAnspruchspensumRest(this.getAnspruchspensumRest() + other.getAnspruchspensumRest());
		if (this.getErwerbspensumGS1() == null && other.getErwerbspensumGS1() == null) {
			this.setErwerbspensumGS1(null);
		} else {
			this.setErwerbspensumGS1((this.getErwerbspensumGS1() != null ? this.getErwerbspensumGS1() : 0)
				+ (other.getErwerbspensumGS1() != null ? other.getErwerbspensumGS1() : 0));
		}

		if (this.getErwerbspensumGS2() == null && other.getErwerbspensumGS2() == null) {
			this.setErwerbspensumGS2(null);
		} else {
			this.setErwerbspensumGS2((this.getErwerbspensumGS2() != null ? this.getErwerbspensumGS2() : 0) +
				(other.getErwerbspensumGS2() != null ? other.getErwerbspensumGS2() : 0));
		}

		BigDecimal newMonatlicheBetreuungskosten = BigDecimal.ZERO;
		if (this.getMonatlicheBetreuungskosten() != null) {
			newMonatlicheBetreuungskosten = newMonatlicheBetreuungskosten.add(this.getMonatlicheBetreuungskosten());
		}
		if (other.getMonatlicheBetreuungskosten() != null) {
			newMonatlicheBetreuungskosten = newMonatlicheBetreuungskosten.add(other.getMonatlicheBetreuungskosten());
		}
		this.setMonatlicheBetreuungskosten(newMonatlicheBetreuungskosten);

		this.getTaetigkeiten().addAll(other.getTaetigkeiten());
		this.setWohnsitzNichtInGemeindeGS1(this.isWohnsitzNichtInGemeindeGS1() && other.isWohnsitzNichtInGemeindeGS1());

		this.setBezahltVollkosten(this.isBezahltVollkosten() || other.isBezahltVollkosten());

		this.setLongAbwesenheit(this.isLongAbwesenheit() || other.isLongAbwesenheit());
		this.setHasSecondGesuchstellerForFinanzielleSituation(this.isHasSecondGesuchstellerForFinanzielleSituation()
			|| other.isHasSecondGesuchstellerForFinanzielleSituation());

		this.ekv1Alleine = (this.ekv1Alleine || other.ekv1Alleine);
		this.ekv1ZuZweit = (this.ekv1ZuZweit || other.ekv1ZuZweit);
		this.ekv2Alleine = (this.ekv2Alleine || other.ekv2Alleine);
		this.ekv2ZuZweit = (this.ekv2ZuZweit || other.ekv2ZuZweit);

		this.setKategorieKeinPensum(this.kategorieKeinPensum || other.kategorieKeinPensum);
		this.setKategorieMaxEinkommen(this.kategorieMaxEinkommen || other.kategorieMaxEinkommen);
		this.setAbschnittLiegtNachBEGUStartdatum(this.abschnittLiegtNachBEGUStartdatum
			&& other.abschnittLiegtNachBEGUStartdatum);

		this.setBabyTarif(this.babyTarif || other.babyTarif);
		this.setEingeschult(this.eingeschult || other.eingeschult);

		// Zusätzliche Felder aus Result
		this.betreuungspensumProzent = this.betreuungspensumProzent.add(other.betreuungspensumProzent);
		this.anspruchspensumProzent = this.anspruchspensumProzent + other.anspruchspensumProzent;
		this.einkommensjahr = other.einkommensjahr;
		this.massgebendesEinkommenVorAbzugFamgr = this.massgebendesEinkommenVorAbzugFamgr.add(other.massgebendesEinkommenVorAbzugFamgr);
		this.zuSpaetEingereicht = this.zuSpaetEingereicht || other.zuSpaetEingereicht;
		this.besondereBeduerfnisseBestaetigt = this.besondereBeduerfnisseBestaetigt || other.besondereBeduerfnisseBestaetigt;
		this.minimalesEwpUnterschritten = this.minimalesEwpUnterschritten || other.minimalesEwpUnterschritten;
		this.tsBetreuungszeitProWocheMitBetreuung = this.tsBetreuungszeitProWocheMitBetreuung + other.tsBetreuungszeitProWocheMitBetreuung;
		this.tsVerpflegungskostenMitBetreuung = MathUtil.DEFAULT.addNullSafe(this.tsVerpflegungskostenMitBetreuung, other.tsVerpflegungskostenMitBetreuung);
		this.tsBetreuungszeitProWocheOhneBetreuung = this.tsBetreuungszeitProWocheOhneBetreuung + other.tsBetreuungszeitProWocheOhneBetreuung;
		this.tsVerpflegungskostenOhneBetreuung = MathUtil.DEFAULT.addNullSafe(this.tsVerpflegungskostenOhneBetreuung, other.tsVerpflegungskostenOhneBetreuung);
	}

	public boolean isSame(BGCalculationInput other) {
		return
			isSameErwerbspensum(erwerbspensumGS1, other.erwerbspensumGS1) &&
			isSameErwerbspensum(erwerbspensumGS2, other.erwerbspensumGS2) &&
			fachstellenpensum == other.fachstellenpensum &&
			ausserordentlicherAnspruch == other.ausserordentlicherAnspruch &&
			anspruchspensumRest == other.anspruchspensumRest &&
			hasSecondGesuchstellerForFinanzielleSituation
				== other.hasSecondGesuchstellerForFinanzielleSituation &&
			bezahltVollkosten == other.bezahltVollkosten &&
			longAbwesenheit == other.longAbwesenheit &&
			ekv1Alleine == other.ekv1Alleine &&
			ekv1ZuZweit == other.ekv1ZuZweit &&
			ekv2Alleine == other.ekv2Alleine &&
			ekv2ZuZweit == other.ekv2ZuZweit &&
			abschnittLiegtNachBEGUStartdatum == other.abschnittLiegtNachBEGUStartdatum &&
			Objects.equals(wohnsitzNichtInGemeindeGS1, other.wohnsitzNichtInGemeindeGS1) &&
			babyTarif == other.babyTarif &&
			eingeschult == other.eingeschult &&
			MathUtil.isSame(monatlicheBetreuungskosten, other.monatlicheBetreuungskosten) &&
			// Zusätzliche Felder aus Result
			MathUtil.isSame(betreuungspensumProzent, other.betreuungspensumProzent) &&
			this.anspruchspensumProzent == other.anspruchspensumProzent &&
			MathUtil.isSame(abzugFamGroesse, other.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, other.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, other.massgebendesEinkommenVorAbzugFamgr) &&
			zuSpaetEingereicht == other.zuSpaetEingereicht &&
			minimalesEwpUnterschritten == other.minimalesEwpUnterschritten &&
			Objects.equals(einkommensjahr, other.einkommensjahr) &&
			besondereBeduerfnisseBestaetigt == other.besondereBeduerfnisseBestaetigt;
	}

	public boolean isSameSichtbareDaten(BGCalculationInput that) {
		//noinspection ObjectEquality,SimplifiableIfStatement
		if (this == that) {
			return true;
		}
		return babyTarif == that.babyTarif &&
			eingeschult == that.eingeschult &&
			// Zusätzliche Felder aus Result
			MathUtil.isSame(this.betreuungspensumProzent, that.betreuungspensumProzent) &&
			this.anspruchspensumProzent == that.anspruchspensumProzent &&
			MathUtil.isSame(this.abzugFamGroesse, that.abzugFamGroesse) &&
			MathUtil.isSame(this.famGroesse, that.famGroesse) &&
			MathUtil.isSame(this.massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			Objects.equals(this.einkommensjahr, that.einkommensjahr) &&
			(this.besondereBeduerfnisseBestaetigt == that.besondereBeduerfnisseBestaetigt) &&
			(this.minimalesEwpUnterschritten == that.minimalesEwpUnterschritten) &&
			Objects.equals(this.tsBetreuungszeitProWocheMitBetreuung, that.tsBetreuungszeitProWocheMitBetreuung) &&
			MathUtil.isSame(this.tsVerpflegungskostenMitBetreuung, that.tsVerpflegungskostenMitBetreuung) &&
			MathUtil.isSame(this.tsGebuehrProStundeMitBetreuung, that.tsGebuehrProStundeMitBetreuung) &&
			MathUtil.isSame(this.tsTotalKostenProWocheMitBetreuung, that.tsTotalKostenProWocheMitBetreuung) &&
			Objects.equals(this.tsBetreuungszeitProWocheOhneBetreuung, that.tsBetreuungszeitProWocheOhneBetreuung) &&
			MathUtil.isSame(this.tsVerpflegungskostenOhneBetreuung, that.tsVerpflegungskostenOhneBetreuung) &&
			MathUtil.isSame(this.tsGebuehrProStundeOhneBetreuung, that.tsGebuehrProStundeOhneBetreuung) &&
			MathUtil.isSame(this.tsTotalKostenProWocheOhneBetreuung, that.tsTotalKostenProWocheOhneBetreuung);
	}

	private boolean isSameErwerbspensum(@Nullable Integer thisErwerbspensumGS, @Nullable Integer thatErwerbspensumGS) {
		return thisErwerbspensumGS == null && thatErwerbspensumGS == null
			|| !(thisErwerbspensumGS == null || thatErwerbspensumGS == null)
			&& thisErwerbspensumGS.equals(thatErwerbspensumGS);
	}

	/**
	 * @return berechneter Wert. Zieht vom massgebenenEinkommenVorAbzug den Familiengroessen Abzug ab
	 */
	@Nonnull
	public BigDecimal getMassgebendesEinkommen() {
		BigDecimal abzugFamSize = this.abzugFamGroesse;
		return MathUtil.DEFAULT.subtractNullSafe(this.massgebendesEinkommenVorAbzugFamgr, abzugFamSize);
	}
}
