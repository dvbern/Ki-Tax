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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.MathUtil;

public class BGCalculationInput {

	@Nonnull
	private RuleValidity ruleValidity;

	private VerfuegungZeitabschnitt parent;

	// Wird benoetigt, um clientseitig "Identische Berechnung" anzuzeigen (betrifft nur Verfuegungsbetrag, nicht Mahlzeiten)
	private boolean sameVerfuegteVerfuegungsrelevanteDaten;

	// Dieser Wert wird gebraucht, um zu wissen ob die Korrektur relevant fuer die Zahlungen ist, da nur wenn die
	// Verguenstigung sich geaendert hat, muss man die Korrektur beruecksichtigen
	// TODO (hefr) IGNORIEREN? Wir nur benoetigt, um clientseitig die frage nach ignorieren zu stellen, fuer Mahlzeiten nicht!
	private boolean sameAusbezahlteVerguenstigung;

	@Nullable
	private Integer erwerbspensumGS1 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Nullable
	private Integer erwerbspensumGS2 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Nullable
	private Integer erwerbspensumZuschlag = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	private Set<Taetigkeit> taetigkeiten = new HashSet<>();

	private int fachstellenpensum;

	private boolean betreuungspensumMustBeAtLeastFachstellenpensum = false;

	private int ausserordentlicherAnspruch;

	//es muss by default null sein um zu wissen, wann es nicht definiert wurde
	private Boolean wohnsitzNichtInGemeindeGS1 = null;

	// Wenn Vollkosten bezahlt werden muessen, werden die Vollkosten berechnet und als Elternbeitrag gesetzt
	// MaxEinkommen, FinSitAbgelehnt, KeineVerguenstigungGewuenscht: OHNE erweiterteBetreuung
	// Aber auch bei langer Abwesenheit!
	private boolean bezahltVollkosten;

	// MaxEinkommen, FinSitAbgelehnt, KeineVerguenstigungGewuenscht (alle egal ob erweiterteBetreuung)
	private boolean keinAnspruchAufgrundEinkommen;

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

	@Nullable
	private EinschulungTyp einschulungTyp;

	private BetreuungsangebotTyp betreuungsangebotTyp;

	// Zusätzliche Felder aus Result. Diese müssen nach Abschluss der Rules auf das Result kopiert werden
	private int anspruchspensumProzent;

	@NotNull @Nonnull
	private BigDecimal betreuungspensumProzent = BigDecimal.ZERO;

	@NotNull @Nonnull
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

	@Valid
	@NotNull
	@Nonnull
	private TSCalculationInput tsInputMitBetreuung = new TSCalculationInput();

	@Valid
	@NotNull
	@Nonnull
	private TSCalculationInput tsInputOhneBetreuung = new TSCalculationInput();

	private boolean sozialhilfeempfaenger = false;

	private boolean betreuungInGemeinde = false;

	private BigDecimal anzahlHauptmahlzeiten = BigDecimal.ZERO;

	private BigDecimal tarifHauptmahlzeit = BigDecimal.ZERO;

	private BigDecimal anzahlNebenmahlzeiten = BigDecimal.ZERO;

	private BigDecimal tarifNebenmahlzeit = BigDecimal.ZERO;

	private BigDecimal verguenstigungHauptmahlzeitenTotal = BigDecimal.ZERO;

	private BigDecimal verguenstigungNebenmahlzeitenTotal = BigDecimal.ZERO;

	private PensumUnits pensumUnit = PensumUnits.PERCENTAGE;

	public BGCalculationInput(@Nonnull VerfuegungZeitabschnitt parent, @Nonnull RuleValidity ruleValidity) {
		this.parent = parent;
		this.ruleValidity = ruleValidity;
	}

	public BGCalculationInput(@Nonnull BGCalculationInput toCopy) {
		this.parent = toCopy.parent;
		this.erwerbspensumGS1 = toCopy.erwerbspensumGS1;
		this.erwerbspensumGS2 = toCopy.erwerbspensumGS2;
		this.erwerbspensumZuschlag = toCopy.erwerbspensumZuschlag;
		HashSet<Taetigkeit> mergedTaetigkeiten = new HashSet<>();
		mergedTaetigkeiten.addAll(this.taetigkeiten);
		mergedTaetigkeiten.addAll(toCopy.taetigkeiten);
		this.taetigkeiten = mergedTaetigkeiten;
		this.fachstellenpensum = toCopy.fachstellenpensum;
		this.ausserordentlicherAnspruch = toCopy.ausserordentlicherAnspruch;
		this.wohnsitzNichtInGemeindeGS1 = toCopy.wohnsitzNichtInGemeindeGS1;
		this.bezahltVollkosten = toCopy.bezahltVollkosten;
		this.keinAnspruchAufgrundEinkommen = toCopy.keinAnspruchAufgrundEinkommen;
		this.longAbwesenheit = toCopy.isLongAbwesenheit();
		this.anspruchspensumRest = toCopy.anspruchspensumRest;
		this.betreuungspensumMustBeAtLeastFachstellenpensum = toCopy.betreuungspensumMustBeAtLeastFachstellenpensum;
		this.monatlicheBetreuungskosten = toCopy.monatlicheBetreuungskosten;
		this.anzahlHauptmahlzeiten = toCopy.anzahlHauptmahlzeiten;
		this.anzahlNebenmahlzeiten = toCopy.anzahlNebenmahlzeiten;
		this.tarifHauptmahlzeit = toCopy.tarifHauptmahlzeit;
		this.tarifNebenmahlzeit = toCopy.tarifNebenmahlzeit;
		this.verguenstigungHauptmahlzeitenTotal = toCopy.verguenstigungHauptmahlzeitenTotal;
		this.verguenstigungNebenmahlzeitenTotal = toCopy.getVerguenstigungNebenmahlzeitenTotal();
		this.hasSecondGesuchstellerForFinanzielleSituation = toCopy.hasSecondGesuchstellerForFinanzielleSituation;
		this.ekv1Alleine = toCopy.ekv1Alleine;
		this.ekv1ZuZweit = toCopy.ekv1ZuZweit;
		this.ekv2Alleine = toCopy.ekv2Alleine;
		this.ekv2ZuZweit = toCopy.ekv2ZuZweit;
		this.kategorieMaxEinkommen = toCopy.kategorieMaxEinkommen;
		this.kategorieKeinPensum = toCopy.kategorieKeinPensum;
		this.abschnittLiegtNachBEGUStartdatum = toCopy.abschnittLiegtNachBEGUStartdatum;
		this.babyTarif = toCopy.babyTarif;
		this.einschulungTyp = toCopy.einschulungTyp;
		this.betreuungsangebotTyp = toCopy.betreuungsangebotTyp;
		this.betreuungspensumProzent = toCopy.betreuungspensumProzent;
		this.anspruchspensumProzent = toCopy.anspruchspensumProzent;
		this.einkommensjahr = toCopy.einkommensjahr;
		this.abzugFamGroesse = toCopy.abzugFamGroesse;
		this.famGroesse = toCopy.famGroesse;
		this.massgebendesEinkommenVorAbzugFamgr = toCopy.massgebendesEinkommenVorAbzugFamgr;
		this.besondereBeduerfnisseBestaetigt = toCopy.besondereBeduerfnisseBestaetigt;
		this.zuSpaetEingereicht = toCopy.zuSpaetEingereicht;
		this.minimalesEwpUnterschritten = toCopy.minimalesEwpUnterschritten;
		this.tsInputMitBetreuung = toCopy.tsInputMitBetreuung.copy();
		this.tsInputOhneBetreuung = toCopy.tsInputOhneBetreuung.copy();
		this.sozialhilfeempfaenger = toCopy.sozialhilfeempfaenger;
		this.betreuungInGemeinde = toCopy.betreuungInGemeinde;
		this.ruleValidity = toCopy.ruleValidity;
		this.pensumUnit = toCopy.pensumUnit;
	}

	@Nonnull
	public RuleValidity getRuleValidity() {
		return ruleValidity;
	}

	public void setRuleValidity(@Nonnull RuleValidity ruleValidity) {
		this.ruleValidity = ruleValidity;
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

	@Nullable
	public Integer getErwerbspensumZuschlag() {
		return erwerbspensumZuschlag;
	}

	public void setErwerbspensumZuschlag (@Nullable Integer erwerbspensumZuschlag) {
		this.erwerbspensumZuschlag = erwerbspensumZuschlag;
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

	public boolean isBetreuungspensumMustBeAtLeastFachstellenpensum() {
		return this.betreuungspensumMustBeAtLeastFachstellenpensum;
	}

	public void setBetreuungspensumMustBeAtLeastFachstellenpensum(boolean betreuungspensumMustBeAtLeastFachstellenpensum) {
		this.betreuungspensumMustBeAtLeastFachstellenpensum = betreuungspensumMustBeAtLeastFachstellenpensum;
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

	public boolean isKeinAnspruchAufgrundEinkommen() {
		return keinAnspruchAufgrundEinkommen;
	}

	public void setKeinAnspruchAufgrundEinkommen(boolean keinAnspruchAufgrundEinkommen) {
		this.keinAnspruchAufgrundEinkommen = keinAnspruchAufgrundEinkommen;
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

	@Nullable
	public EinschulungTyp getEinschulungTyp() {
		return einschulungTyp;
	}

	public void setEinschulungTyp(@Nullable EinschulungTyp einschulungTyp) {
		this.einschulungTyp = einschulungTyp;
	}

	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
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

	@Nonnull
	public BigDecimal getAbzugFamGroesseNonNull() {
		return abzugFamGroesse != null ? abzugFamGroesse : MathUtil.DEFAULT.from(0);
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

	@Nonnull
	public BigDecimal getFamGroesseNonNull() {
		return famGroesse != null ? famGroesse : MathUtil.DEFAULT.from(0);
	}

	public void setFamGroesse(@Nullable BigDecimal famGroesse) {
		this.famGroesse = famGroesse;
	}

	public void setTsBetreuungszeitProWocheMitBetreuung(@Nonnull Integer tsBetreuungszeitProWocheMitBetreuung) {
		this.tsInputMitBetreuung.setBetreuungszeitProWoche(tsBetreuungszeitProWocheMitBetreuung);
	}

	public void setTsVerpflegungskostenMitBetreuung(@Nonnull BigDecimal tsVerpflegungskostenMitBetreuung) {
		this.tsInputMitBetreuung.setVerpflegungskosten(tsVerpflegungskostenMitBetreuung);
	}

	public void setTsVerpflegungskostenVerguenstigtMitBetreuung(@Nonnull BigDecimal tsVerpflegungskostenVerguenstigtMitBetreuung) {
		this.tsInputMitBetreuung.setVerpflegungskostenVerguenstigt(tsVerpflegungskostenVerguenstigtMitBetreuung);
	}

	public void setVerpflegungskostenUndMahlzeitenMitBetreuung(Map<BigDecimal, Integer> verpflegungskostenUndMahlzeiten) {
		this.tsInputMitBetreuung.setVerpflegungskostenUndMahlzeiten(verpflegungskostenUndMahlzeiten);
	}

	public void setVerpflegungskostenUndMahlzeitenMitBetreuungZweiWochen(Map<BigDecimal, Integer> verpflegungskostenUndMahlzeiten) {
		this.tsInputMitBetreuung.setVerpflegungskostenUndMahlzeitenZweiWochen(verpflegungskostenUndMahlzeiten);
	}

	public void setVerpflegungskostenUndMahlzeitenOhneBetreuung(Map<BigDecimal, Integer> verpflegungskostenUndMahlzeiten) {
		this.tsInputOhneBetreuung.setVerpflegungskostenUndMahlzeiten(verpflegungskostenUndMahlzeiten);
	}

	public void setVerpflegungskostenUndMahlzeitenOhneBetreuungZweiWochen(Map<BigDecimal, Integer> verpflegungskostenUndMahlzeiten) {
		this.tsInputOhneBetreuung.setVerpflegungskostenUndMahlzeitenZweiWochen(verpflegungskostenUndMahlzeiten);
	}

	public void setTsBetreuungszeitProWocheOhneBetreuung(@Nonnull Integer tsBetreuungszeitProWocheOhneBetreuung) {
		this.tsInputOhneBetreuung.setBetreuungszeitProWoche(tsBetreuungszeitProWocheOhneBetreuung);
	}

	public void setTsVerpflegungskostenOhneBetreuung(@Nonnull BigDecimal tsVerpflegungskostenOhneBetreuung) {
		this.tsInputOhneBetreuung.setVerpflegungskosten(tsVerpflegungskostenOhneBetreuung);
	}

	public void setTsVerpflegungskostenVerguenstigtOhneBetreuung(@Nonnull BigDecimal tsVerpflegungskostenVerguenstigtOhneBetreuung) {
		this.tsInputOhneBetreuung.setVerpflegungskostenVerguenstigt(tsVerpflegungskostenVerguenstigtOhneBetreuung);
	}

	@Nonnull
	public TSCalculationInput getTsInputMitBetreuung() {
		return tsInputMitBetreuung;
	}

	@Nonnull
	public TSCalculationInput getTsInputOhneBetreuung() {
		return tsInputOhneBetreuung;
	}

	public boolean isSozialhilfeempfaenger() {
		return sozialhilfeempfaenger;
	}

	public void setSozialhilfeempfaenger(boolean sozialhilfeempfaenger) {
		this.sozialhilfeempfaenger = sozialhilfeempfaenger;
	}

	public boolean isBetreuungInGemeinde() {
		return betreuungInGemeinde;
	}

	public void setBetreuungInGemeinde(boolean betreuungInGemeinde) {
		this.betreuungInGemeinde = betreuungInGemeinde;
	}

	public BigDecimal getAnzahlHauptmahlzeiten() {
		return anzahlHauptmahlzeiten;
	}

	public void setAnzahlHauptmahlzeiten(BigDecimal anzahlHauptmahlzeiten) {
		this.anzahlHauptmahlzeiten = anzahlHauptmahlzeiten;
	}

	public BigDecimal getAnzahlNebenmahlzeiten() {
		return anzahlNebenmahlzeiten;
	}

	public void setAnzahlNebenmahlzeiten(BigDecimal anzahlNebenmahlzeiten) {
		this.anzahlNebenmahlzeiten = anzahlNebenmahlzeiten;
	}

	public BigDecimal getTarifHauptmahlzeit() {
		return tarifHauptmahlzeit;
	}

	public void setTarifHauptmahlzeit(BigDecimal tarifHauptmahlzeit) {
		this.tarifHauptmahlzeit = tarifHauptmahlzeit;
	}

	public BigDecimal getTarifNebenmahlzeit() {
		return tarifNebenmahlzeit;
	}

	public void setTarifNebenmahlzeit(BigDecimal tarifNebenmahlzeit) {
		this.tarifNebenmahlzeit = tarifNebenmahlzeit;
	}

	public BigDecimal getVerguenstigungHauptmahlzeitenTotal() {
		return verguenstigungHauptmahlzeitenTotal;
	}

	public void setVerguenstigungHauptmahlzeitenTotal(BigDecimal verguenstigungHauptmahlzeitenTotal) {
		this.verguenstigungHauptmahlzeitenTotal = verguenstigungHauptmahlzeitenTotal;
	}

	public BigDecimal getVerguenstigungNebenmahlzeitenTotal() {
		return verguenstigungNebenmahlzeitenTotal;
	}

	public void setVerguenstigungNebenmahlzeitenTotal(BigDecimal verguenstigungNebenmahlzeitenTotal) {
		this.verguenstigungNebenmahlzeitenTotal = verguenstigungNebenmahlzeitenTotal;
	}

	@Override
	public String toString() {
		String sb = "EP GS1: " + getErwerbspensumGS1() + '\t'
			+ " EP GS2: " + getErwerbspensumGS2() + '\t'
			+ " Restanspruch: " + getAnspruchspensumRest();
		return sb;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	public void add(@Nonnull BGCalculationInput other) {
		this.setBetreuungspensumMustBeAtLeastFachstellenpensum(this.isBetreuungspensumMustBeAtLeastFachstellenpensum() || other.isBetreuungspensumMustBeAtLeastFachstellenpensum());
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

		// Die Felder betreffend EWP Zuschlag können nicht linear addiert werden. Es darf also nie Überschneidungen geben!
		if (other.getErwerbspensumZuschlag() != null) {
			this.setErwerbspensumZuschlag(other.getErwerbspensumZuschlag());
		}

		BigDecimal newMonatlicheBetreuungskosten = BigDecimal.ZERO;
		if (this.getMonatlicheBetreuungskosten() != null) {
			newMonatlicheBetreuungskosten = newMonatlicheBetreuungskosten.add(this.getMonatlicheBetreuungskosten());
		}
		if (other.getMonatlicheBetreuungskosten() != null) {
			newMonatlicheBetreuungskosten = newMonatlicheBetreuungskosten.add(other.getMonatlicheBetreuungskosten());
		}
		this.setMonatlicheBetreuungskosten(newMonatlicheBetreuungskosten);

		BigDecimal newMonatlicheHauptmahlzeiten = BigDecimal.ZERO;
		if (this.getAnzahlHauptmahlzeiten() != null) {
			newMonatlicheHauptmahlzeiten = newMonatlicheHauptmahlzeiten.add(this.getAnzahlHauptmahlzeiten());
		}
		if (other.getAnzahlHauptmahlzeiten() != null) {
			newMonatlicheHauptmahlzeiten = newMonatlicheHauptmahlzeiten.add(other.getAnzahlHauptmahlzeiten());
		}
		this.setAnzahlHauptmahlzeiten(newMonatlicheHauptmahlzeiten);

		BigDecimal newMonatlicheNebenmahlzeiten = BigDecimal.ZERO;
		if (this.getAnzahlNebenmahlzeiten() != null) {
			newMonatlicheNebenmahlzeiten = newMonatlicheNebenmahlzeiten.add(this.getAnzahlNebenmahlzeiten());
		}
		if (other.getAnzahlNebenmahlzeiten() != null) {
			newMonatlicheNebenmahlzeiten = newMonatlicheNebenmahlzeiten.add(other.getAnzahlNebenmahlzeiten());
		}
		this.setAnzahlNebenmahlzeiten(newMonatlicheNebenmahlzeiten);

		BigDecimal newTarifHauptmhalzeit = BigDecimal.ZERO;
		if (this.getTarifHauptmahlzeit() != null) {
			newTarifHauptmhalzeit = newTarifHauptmhalzeit.add(this.getTarifHauptmahlzeit());
		}
		if (other.getTarifHauptmahlzeit() != null) {
			newTarifHauptmhalzeit = newTarifHauptmhalzeit.add(other.getTarifHauptmahlzeit());
		}
		this.setTarifHauptmahlzeit(newTarifHauptmhalzeit);

		BigDecimal newTarifNebenmahlzeit = BigDecimal.ZERO;
		if (this.getTarifNebenmahlzeit() != null) {
			newTarifNebenmahlzeit = newTarifNebenmahlzeit.add(this.getTarifNebenmahlzeit());
		}
		if (other.getTarifNebenmahlzeit() != null) {
			newTarifNebenmahlzeit = newTarifNebenmahlzeit.add(other.getTarifNebenmahlzeit());
		}
		this.setTarifNebenmahlzeit(newTarifNebenmahlzeit);

		BigDecimal newVerguenstigungHaupt = BigDecimal.ZERO;
		if (this.getVerguenstigungHauptmahlzeitenTotal() != null) {
			newVerguenstigungHaupt = newVerguenstigungHaupt.add(this.getVerguenstigungHauptmahlzeitenTotal());
		}
		if (other.getTarifNebenmahlzeit() != null) {
			newVerguenstigungHaupt = newVerguenstigungHaupt.add(other.getVerguenstigungHauptmahlzeitenTotal());
		}
		this.setVerguenstigungHauptmahlzeitenTotal(newVerguenstigungHaupt);

		BigDecimal newVerguenstigungNeben = BigDecimal.ZERO;
		if (this.getVerguenstigungNebenmahlzeitenTotal() != null) {
			newVerguenstigungNeben = newVerguenstigungNeben.add(this.getVerguenstigungNebenmahlzeitenTotal());
		}
		if (other.getVerguenstigungNebenmahlzeitenTotal() != null) {
			newVerguenstigungNeben = newVerguenstigungNeben.add(other.getVerguenstigungNebenmahlzeitenTotal());
		}
		this.setVerguenstigungNebenmahlzeitenTotal(newVerguenstigungNeben);


		this.getTaetigkeiten().addAll(other.getTaetigkeiten());
		this.setWohnsitzNichtInGemeindeGS1(this.isWohnsitzNichtInGemeindeGS1() && other.isWohnsitzNichtInGemeindeGS1());

		this.setBezahltVollkosten(this.isBezahltVollkosten() || other.isBezahltVollkosten());
		this.setKeinAnspruchAufgrundEinkommen(this.isKeinAnspruchAufgrundEinkommen() || other.isKeinAnspruchAufgrundEinkommen());

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
		this.einschulungTyp = this.einschulungTyp != null ? this.einschulungTyp : other.einschulungTyp;
		this.betreuungsangebotTyp = this.betreuungsangebotTyp != null ? this.betreuungsangebotTyp : other.betreuungsangebotTyp;

		// Zusätzliche Felder aus Result
		this.betreuungspensumProzent = this.betreuungspensumProzent.add(other.betreuungspensumProzent);
		this.anspruchspensumProzent = this.anspruchspensumProzent + other.anspruchspensumProzent;
		this.einkommensjahr = other.einkommensjahr;
		this.massgebendesEinkommenVorAbzugFamgr = this.massgebendesEinkommenVorAbzugFamgr.add(other.massgebendesEinkommenVorAbzugFamgr);
		this.zuSpaetEingereicht = this.zuSpaetEingereicht || other.zuSpaetEingereicht;
		this.besondereBeduerfnisseBestaetigt = this.besondereBeduerfnisseBestaetigt || other.besondereBeduerfnisseBestaetigt;
		this.minimalesEwpUnterschritten = this.minimalesEwpUnterschritten || other.minimalesEwpUnterschritten;
		this.tsInputMitBetreuung.add(other.tsInputMitBetreuung);
		this.tsInputOhneBetreuung.add(other.tsInputOhneBetreuung);
		this.sozialhilfeempfaenger = this.sozialhilfeempfaenger || other.sozialhilfeempfaenger;
		this.betreuungInGemeinde = this.betreuungInGemeinde || other.betreuungInGemeinde;

		// Die Felder betreffend Familienabzug können nicht linear addiert werden. Es darf also nie Überschneidungen geben!
		if (other.getAbzugFamGroesse() != null) {
			if (this.getAbzugFamGroesse() != null && !MathUtil.isSame(this.getAbzugFamGroesse(), other.getAbzugFamGroesse())) {
				throw new IllegalArgumentException("Familiengoressenabzug kann nicht gemerged werden");
			}
			this.setAbzugFamGroesse(other.getAbzugFamGroesse());
		}
		// Die Familiengroesse kann nicht linear addiert werden, daher darf es hier nie uebschneidungen geben
		if (other.getFamGroesse() != null) {
			if (this.getFamGroesse() != null && !MathUtil.isSame(this.getFamGroesse(), other.getFamGroesse())) {
				throw new IllegalArgumentException("Familiengoressen kann nicht gemerged werden");
			}
			this.setFamGroesse(other.getFamGroesse());
		}
	}

	public boolean isSame(BGCalculationInput other) {
		return
			isSameErwerbspensum(erwerbspensumGS1, other.erwerbspensumGS1) &&
			isSameErwerbspensum(erwerbspensumGS2, other.erwerbspensumGS2) &&
			isSameErwerbspensum(erwerbspensumZuschlag, other.erwerbspensumZuschlag) &&
			fachstellenpensum == other.fachstellenpensum &&
			ausserordentlicherAnspruch == other.ausserordentlicherAnspruch &&
			anspruchspensumRest == other.anspruchspensumRest &&
			hasSecondGesuchstellerForFinanzielleSituation
				== other.hasSecondGesuchstellerForFinanzielleSituation &&
			bezahltVollkosten == other.bezahltVollkosten &&
			keinAnspruchAufgrundEinkommen == other.keinAnspruchAufgrundEinkommen &&
			longAbwesenheit == other.longAbwesenheit &&
			ekv1Alleine == other.ekv1Alleine &&
			ekv1ZuZweit == other.ekv1ZuZweit &&
			ekv2Alleine == other.ekv2Alleine &&
			ekv2ZuZweit == other.ekv2ZuZweit &&
			abschnittLiegtNachBEGUStartdatum == other.abschnittLiegtNachBEGUStartdatum &&
			Objects.equals(wohnsitzNichtInGemeindeGS1, other.wohnsitzNichtInGemeindeGS1) &&
			babyTarif == other.babyTarif &&
			einschulungTyp == other.einschulungTyp &&
			betreuungsangebotTyp == other.betreuungsangebotTyp &&
			MathUtil.isSame(monatlicheBetreuungskosten, other.monatlicheBetreuungskosten) &&
			MathUtil.isSame(verguenstigungHauptmahlzeitenTotal, other.verguenstigungHauptmahlzeitenTotal) &&
			MathUtil.isSame(verguenstigungNebenmahlzeitenTotal, other.verguenstigungNebenmahlzeitenTotal) &&
			MathUtil.isSame(tarifHauptmahlzeit, other.tarifHauptmahlzeit) &&
			MathUtil.isSame(tarifNebenmahlzeit, other.tarifNebenmahlzeit) &&
			MathUtil.isSame(anzahlHauptmahlzeiten, other.anzahlHauptmahlzeiten) &&
			MathUtil.isSame(anzahlNebenmahlzeiten, other.anzahlNebenmahlzeiten) &&
			// Zusätzliche Felder aus Result
			MathUtil.isSame(betreuungspensumProzent, other.betreuungspensumProzent) &&
			this.anspruchspensumProzent == other.anspruchspensumProzent &&
			MathUtil.isSame(abzugFamGroesse, other.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, other.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, other.massgebendesEinkommenVorAbzugFamgr) &&
			zuSpaetEingereicht == other.zuSpaetEingereicht &&
			minimalesEwpUnterschritten == other.minimalesEwpUnterschritten &&
			Objects.equals(einkommensjahr, other.einkommensjahr) &&
			besondereBeduerfnisseBestaetigt == other.besondereBeduerfnisseBestaetigt &&
			this.tsInputMitBetreuung.isSame(other.tsInputMitBetreuung) &&
			this.tsInputOhneBetreuung.isSame(other.tsInputOhneBetreuung);
	}

	public boolean isSameSichtbareDaten(BGCalculationInput that) {
		//noinspection ObjectEquality,SimplifiableIfStatement
		if (this == that) {
			return true;
		}
		return babyTarif == that.babyTarif &&
			einschulungTyp == that.einschulungTyp &&
			betreuungsangebotTyp == that.betreuungsangebotTyp &&
			MathUtil.isSame(monatlicheBetreuungskosten, that.monatlicheBetreuungskosten) &&
			MathUtil.isSame(verguenstigungHauptmahlzeitenTotal, that.verguenstigungHauptmahlzeitenTotal) &&
			MathUtil.isSame(verguenstigungNebenmahlzeitenTotal, that.verguenstigungNebenmahlzeitenTotal) &&
			// Zusätzliche Felder aus Result
			MathUtil.isSame(this.betreuungspensumProzent, that.betreuungspensumProzent) &&
			this.anspruchspensumProzent == that.anspruchspensumProzent &&
			MathUtil.isSame(this.abzugFamGroesse, that.abzugFamGroesse) &&
			MathUtil.isSame(this.famGroesse, that.famGroesse) &&
			MathUtil.isSame(this.massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			Objects.equals(this.einkommensjahr, that.einkommensjahr) &&
			this.besondereBeduerfnisseBestaetigt == that.besondereBeduerfnisseBestaetigt &&
			this.minimalesEwpUnterschritten == that.minimalesEwpUnterschritten &&
			this.tsInputMitBetreuung.isSame(that.tsInputMitBetreuung) &&
			this.tsInputOhneBetreuung.isSame(that.tsInputOhneBetreuung);
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

	@Nonnull
	public BigDecimal getBgPensumProzent() {
		return getBetreuungspensumProzent().min(MathUtil.DEFAULT.from(getAnspruchspensumProzent()));
	}

	public void addBemerkung(@Nonnull MsgKey msgKey, @Nonnull Locale locale, @Nullable Object... args) {
		VerfuegungsBemerkung bemerkung = new VerfuegungsBemerkung(ruleValidity, msgKey, locale, args);
		this.getParent().getBemerkungenList().addBemerkung(bemerkung);
	}

	public PensumUnits getPensumUnit() {
		return pensumUnit;
	}

	public void setPensumUnit(PensumUnits pensumUnit) {
		this.pensumUnit = pensumUnit;
	}
}
