/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.MathUtil;
import lombok.Getter;
import lombok.Setter;

public class BGCalculationInput {

	@Nonnull
	private RuleValidity ruleValidity;

	private VerfuegungZeitabschnitt parent;

	// Wird benoetigt, um clientseitig "Identische Berechnung" anzuzeigen (betrifft nur Verfuegungsbetrag, nicht Mahlzeiten)
	private boolean sameVerfuegteVerfuegungsrelevanteDaten;

	// Dieser Wert wird gebraucht, um zu wissen ob die Korrektur relevant fuer die Zahlungen ist, da nur wenn die
	// Verguenstigung sich geaendert hat, muss man die Korrektur beruecksichtigen
	// Wird nur benoetigt, um clientseitig die frage nach ignorieren zu stellen, muss pro Zahlungslauftyp separat berechnet werden!
	private boolean sameAusbezahlterBetragInstitution;
	private boolean sameAusbezahlterBetragAntragsteller;
	// Dieser Wert wird gebraucht, um zu wissen ob die Korrektur relevant für den Verfügungsprozessist, da die Mutation nicht
	// ohne Verfügung geschlossen werden darf, wenn sich die MZV ändern. Ansonsten würden diese bei den Zahlungen ignoriert
	private boolean sameVerfuegteMahlzeitenVerguenstigung;

	@Nullable
	private Integer erwerbspensumGS1 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Nullable
	private Integer erwerbspensumGS2 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Nullable
	private Integer erwerbspensumZuschlag = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	private Set<Taetigkeit> taetigkeiten = new HashSet<>();

	private int fachstellenpensum;

	private IntegrationTyp integrationTypFachstellenPensum;

	private boolean betreuungspensumMustBeAtLeastFachstellenpensum = false;

	private int ausserordentlicherAnspruch;

	//es muss by default null sein um zu wissen, wann es nicht definiert wurde
	private Boolean wohnsitzNichtInGemeindeGS1 = null;

	// Wenn Vollkosten bezahlt werden muessen, werden die Vollkosten berechnet und als Elternbeitrag gesetzt
	// MaxEinkommen, FinSitAbgelehnt, KeineVerguenstigungGewuenscht: OHNE erweiterteBetreuung
	// Aber auch bei langer Abwesenheit!
	// Anteil am Monat, bei welchem die Eltern die gesamen Vollkosten bezahlen müssen (1 = 100%)
	@Nonnull
	private BigDecimal bezahltVollkostenMonatAnteil = BigDecimal.ZERO;

	// MaxEinkommen, FinSitAbgelehnt, KeineVerguenstigungGewuenscht (alle egal ob erweiterteBetreuung)
	private boolean keinAnspruchAufgrundEinkommen;

	private boolean longAbwesenheit;

	private int anspruchspensumRest;

	private int rueckwirkendReduziertesPensumRest;

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
	private BigDecimal anspruchspensumProzent = BigDecimal.ZERO;

	@NotNull
	@Nonnull
	private BigDecimal betreuungspensumProzent = BigDecimal.ZERO;

	@NotNull
	@Nonnull
	private BigDecimal massgebendesEinkommenVorAbzugFamgr = BigDecimal.ZERO;

	private boolean besondereBeduerfnisseBestaetigt;

	@Nullable
	private BigDecimal besondereBeduerfnisseZuschlag;

	@Nullable
	private BigDecimal abzugFamGroesse = null;

	@NotNull
	@Nonnull
	private Integer einkommensjahr;

	private boolean zuSpaetEingereicht;

	private boolean minimalesEwpUnterschritten;

	@Nullable
	private BigDecimal famGroesse = null;

	private boolean kitaPlusZuschlag = false;

	private boolean geschwisternBonusKind2 = false;

	private boolean geschwisternBonusKind3 = false;

	private boolean verguenstigungGewuenscht = true;

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

	private boolean verguenstigungMahlzeitenBeantragt = false;

	private int minimalErforderlichesPensum;

	private boolean isKesbPlatzierung;

	private PensumUnits pensumUnit = PensumUnits.PERCENTAGE;

	private boolean isAuszahlungAnEltern = false;

	//für TFO Luzern
	private BigDecimal stuendlicheVollkosten;

	private BigDecimal percentage;
	private Boolean partnerIdentischMitVorgesuch;

	private boolean isEkvAccepted = false;
	private boolean finsitAccepted = true;

	private boolean requiredAgeForAnspruchNotReached = false;
	private boolean gesuchBeendenKonkubinatMitZweiGS = false;
	private BigDecimal bgStundenFaktor = BigDecimal.ZERO;

	private boolean betreuungInFerienzeit = false;

	private BigDecimal eingewoehnungKosten = BigDecimal.ZERO;

	@Getter
	@Setter
	// Zu verstehen als "Anzahl anderer Kinder im gleichen Haushalt"
	private int anzahlGeschwister = 0;

	@Getter
	@Setter
	private BigDecimal anwesenheitsTageProMonat = BigDecimal.ZERO;

	@Getter
	@Setter
	private Bedarfsstufe bedarfsstufe;

	public BGCalculationInput(@Nonnull VerfuegungZeitabschnitt parent, @Nonnull RuleValidity ruleValidity) {
		this.parent = parent;
		this.ruleValidity = ruleValidity;
	}

	public BGCalculationInput(@Nonnull VerfuegungZeitabschnitt parent, @Nonnull BGCalculationInput toCopy) {
		this.parent = parent;
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
		this.bezahltVollkostenMonatAnteil = toCopy.bezahltVollkostenMonatAnteil;
		this.keinAnspruchAufgrundEinkommen = toCopy.keinAnspruchAufgrundEinkommen;
		this.longAbwesenheit = toCopy.isLongAbwesenheit();
		this.anspruchspensumRest = toCopy.anspruchspensumRest;
		this.betreuungspensumMustBeAtLeastFachstellenpensum = toCopy.betreuungspensumMustBeAtLeastFachstellenpensum;
		this.monatlicheBetreuungskosten = toCopy.monatlicheBetreuungskosten;
		this.anzahlHauptmahlzeiten = toCopy.anzahlHauptmahlzeiten;
		this.anzahlNebenmahlzeiten = toCopy.anzahlNebenmahlzeiten;
		this.tarifHauptmahlzeit = toCopy.tarifHauptmahlzeit;
		this.tarifNebenmahlzeit = toCopy.tarifNebenmahlzeit;
		this.verguenstigungMahlzeitenBeantragt = toCopy.verguenstigungMahlzeitenBeantragt;
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
		this.minimalErforderlichesPensum = toCopy.minimalErforderlichesPensum;
		this.rueckwirkendReduziertesPensumRest = toCopy.rueckwirkendReduziertesPensumRest;
		this.kitaPlusZuschlag = toCopy.kitaPlusZuschlag;
		this.isKesbPlatzierung = toCopy.isKesbPlatzierung;
		this.geschwisternBonusKind2 = toCopy.geschwisternBonusKind2;
		this.geschwisternBonusKind3 = toCopy.geschwisternBonusKind3;
		this.besondereBeduerfnisseZuschlag = toCopy.besondereBeduerfnisseZuschlag;
		this.stuendlicheVollkosten = toCopy.stuendlicheVollkosten;
		this.isAuszahlungAnEltern = toCopy.isAuszahlungAnEltern;
		this.partnerIdentischMitVorgesuch = toCopy.partnerIdentischMitVorgesuch;
		this.isEkvAccepted = toCopy.isEkvAccepted;
		this.requiredAgeForAnspruchNotReached = toCopy.requiredAgeForAnspruchNotReached;
		this.gesuchBeendenKonkubinatMitZweiGS = toCopy.gesuchBeendenKonkubinatMitZweiGS;
		this.bgStundenFaktor = toCopy.bgStundenFaktor;
		this.integrationTypFachstellenPensum = toCopy.integrationTypFachstellenPensum;
		this.verguenstigungGewuenscht = toCopy.verguenstigungGewuenscht;
		this.finsitAccepted = toCopy.finsitAccepted;
		this.eingewoehnungKosten = toCopy.eingewoehnungKosten;
		this.betreuungInFerienzeit = toCopy.betreuungInFerienzeit;
		this.anzahlGeschwister = toCopy.anzahlGeschwister;
		this.anwesenheitsTageProMonat = toCopy.anwesenheitsTageProMonat;
		this.bedarfsstufe = toCopy.bedarfsstufe;
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

	public void setErwerbspensumZuschlag(@Nullable Integer erwerbspensumZuschlag) {
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

	// Gibt an, ob die Eltern während des ganzen Zeitabschnittes die gesamten Vollkosten bezahlen müssen
	public boolean isBezahltKompletteVollkosten() {
		return bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ONE) == 0;
	}

	public @NotNull BigDecimal getBezahltVollkostenMonatAnteil() {
		return bezahltVollkostenMonatAnteil;
	}

	public @NotNull BigDecimal getMonatAnteilVollkostenNichtBezahlt() {
		return MathUtil.EXACT.subtract(BigDecimal.ONE, bezahltVollkostenMonatAnteil);
	}

	// In den RechnerRules kann nur gesetzt werden, das die Eltern die Vollkosten während des kompleten Zeitabschnittes
	// also zu 100 % tragen. Einzig beim berchnen des prozentualen Anteils (@see
	// BgCalculationInput#calculateInputValuesProportionaly)
	// wird hier ein Wert zwischen 0 und 1 gesetzt
	public void setBezahltVollkostenKomplett() {
		this.bezahltVollkostenMonatAnteil = BigDecimal.ONE;
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

	public boolean isSameAusbezahlterBetragInstitution() {
		return sameAusbezahlterBetragInstitution;
	}

	public void setSameAusbezahlterBetragInstitution(boolean sameAusbezahlterBetragInstitution) {
		this.sameAusbezahlterBetragInstitution = sameAusbezahlterBetragInstitution;
	}

	public boolean isSameAusbezahlterBetragAntragsteller() {
		return sameAusbezahlterBetragAntragsteller;
	}

	public void setSameAusbezahlterBetragAntragsteller(boolean sameAusbezahlterBetragAntragsteller) {
		this.sameAusbezahlterBetragAntragsteller = sameAusbezahlterBetragAntragsteller;
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
		return MathUtil.GANZZAHL.from(anspruchspensumProzent).intValue();
	}

	public void setAnspruchspensumProzent(int anspruchspensumProzent) {
		this.anspruchspensumProzent = BigDecimal.valueOf(anspruchspensumProzent);
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

	public boolean getVerguenstigungMahlzeitenBeantragt() {
		return verguenstigungMahlzeitenBeantragt;
	}

	public void setVerguenstigungMahlzeitenBeantragt(boolean verguenstigungMahlzeitenBeantragt) {
		this.verguenstigungMahlzeitenBeantragt = verguenstigungMahlzeitenBeantragt;
	}

	public boolean isAuszahlungAnEltern() {
		return isAuszahlungAnEltern;
	}

	public void setAuszahlungAnEltern(boolean auszahlungAnEltern) {
		isAuszahlungAnEltern = auszahlungAnEltern;
	}

	public boolean isRequiredAgeForAnspruchNotReached() {
		return requiredAgeForAnspruchNotReached;
	}

	public void setRequiredAgeForAnspruchNotReached(boolean requiredAgeForAnspruchNotReached) {
		this.requiredAgeForAnspruchNotReached = requiredAgeForAnspruchNotReached;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("BGCalculationInput{");
		sb.append("ruleValidity=").append(ruleValidity);
		sb.append(", anspruchspensumProzent=").append(anspruchspensumProzent);
		sb.append(", betreuungspensumProzent=").append(betreuungspensumProzent);
		sb.append(", fachstellenpensum=").append(fachstellenpensum);
		sb.append(", anspruchspensumRest=").append(anspruchspensumRest);
		sb.append(", zuSpaetEingereicht=").append(zuSpaetEingereicht);
		sb.append('}');
		return sb.toString();
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	public void add(@Nonnull BGCalculationInput other) {
		this.handleAbwesenheitPensumAenderung(other);
		this.setBetreuungspensumMustBeAtLeastFachstellenpensum(this.isBetreuungspensumMustBeAtLeastFachstellenpensum()
			|| other.isBetreuungspensumMustBeAtLeastFachstellenpensum());
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

		this.monatlicheBetreuungskosten =
			add(this.getMonatlicheBetreuungskosten(), other.getMonatlicheBetreuungskosten());
		this.anzahlHauptmahlzeiten = add(this.getAnzahlHauptmahlzeiten(), other.getAnzahlHauptmahlzeiten());
		this.anzahlNebenmahlzeiten = add(this.getAnzahlNebenmahlzeiten(), other.getAnzahlNebenmahlzeiten());
		this.tarifHauptmahlzeit = add(this.getTarifHauptmahlzeit(), other.getTarifHauptmahlzeit());
		this.tarifNebenmahlzeit = add(this.getTarifNebenmahlzeit(), other.getTarifNebenmahlzeit());

		this.setVerguenstigungMahlzeitenBeantragt(this.verguenstigungMahlzeitenBeantragt
			|| other.verguenstigungMahlzeitenBeantragt);

		this.getTaetigkeiten().addAll(other.getTaetigkeiten());
		this.setWohnsitzNichtInGemeindeGS1(this.isWohnsitzNichtInGemeindeGS1() && other.isWohnsitzNichtInGemeindeGS1());

		this.bezahltVollkostenMonatAnteil = add(this.bezahltVollkostenMonatAnteil, other.bezahltVollkostenMonatAnteil);
		this.handleAbwesenheitKeinMehrBG(other);

		//Vollkostenanteil ist ein Prozentsatz und darf nach dem add nicht mehr als 1 (100 %) entsprechen
		if (this.bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ONE) > 0) {
			this.bezahltVollkostenMonatAnteil = BigDecimal.ONE;
		}

		this.setKeinAnspruchAufgrundEinkommen(this.isKeinAnspruchAufgrundEinkommen()
			|| other.isKeinAnspruchAufgrundEinkommen());

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
		this.betreuungsangebotTyp =
			this.betreuungsangebotTyp != null ? this.betreuungsangebotTyp : other.betreuungsangebotTyp;

		//Minimal erforderliches Pensum ist immer nur auf dem Vorgänger gesetzt und muss einfach übernommen werden.
		this.minimalErforderlichesPensum = other.minimalErforderlichesPensum;

		// Zusätzliche Felder aus Result
		this.betreuungspensumProzent = this.betreuungspensumProzent.add(other.betreuungspensumProzent);
		this.anspruchspensumProzent = add(this.anspruchspensumProzent, other.anspruchspensumProzent);

		//Beim add von zwei anspruchspensen kann das anspruchspensum steigen. Es muss geprüft werden, ob das minimal
		//erforderliche Pensum jetzt überschritten ist. Dies muss nur geprüft werden, wenn das minimalesEwpUnterschritten
		//zuvor in einem der beiden Zeitabschnitte unterschritten wurde
		if (this.minimalesEwpUnterschritten || other.minimalesEwpUnterschritten) {
			this.minimalesEwpUnterschritten = this.getAnspruchspensumProzent() < this.minimalErforderlichesPensum;
		}

		this.einkommensjahr = other.einkommensjahr;
		this.massgebendesEinkommenVorAbzugFamgr =
			this.massgebendesEinkommenVorAbzugFamgr.add(other.massgebendesEinkommenVorAbzugFamgr);
		this.zuSpaetEingereicht = this.zuSpaetEingereicht || other.zuSpaetEingereicht;
		this.besondereBeduerfnisseBestaetigt =
			this.besondereBeduerfnisseBestaetigt || other.besondereBeduerfnisseBestaetigt;
		this.tsInputMitBetreuung.add(other.tsInputMitBetreuung);
		this.tsInputOhneBetreuung.add(other.tsInputOhneBetreuung);
		this.sozialhilfeempfaenger = this.sozialhilfeempfaenger || other.sozialhilfeempfaenger;
		this.betreuungInGemeinde = this.betreuungInGemeinde || other.betreuungInGemeinde;
		this.isKesbPlatzierung = this.isKesbPlatzierung || other.isKesbPlatzierung;
		this.geschwisternBonusKind2 = this.geschwisternBonusKind2 || other.geschwisternBonusKind2;
		this.geschwisternBonusKind3 = this.geschwisternBonusKind3 || other.geschwisternBonusKind3;
		this.anzahlGeschwister = this.anzahlGeschwister + other.anzahlGeschwister;
		this.isAuszahlungAnEltern = this.isAuszahlungAnEltern || other.isAuszahlungAnEltern;
		if (null == this.partnerIdentischMitVorgesuch) {
			this.partnerIdentischMitVorgesuch = other.partnerIdentischMitVorgesuch;
		} else {
			this.partnerIdentischMitVorgesuch =
				this.partnerIdentischMitVorgesuch || other.partnerIdentischMitVorgesuch;
		}
		this.isEkvAccepted = this.isEkvAccepted || other.isEkvAccepted;

		// Die Felder betreffend Familienabzug können nicht linear addiert werden. Es darf also nie Überschneidungen geben!
		if (other.getAbzugFamGroesse() != null) {
			if (this.getAbzugFamGroesse() != null &&
				!MathUtil.isSame( this.getAbzugFamGroesse(), other.getAbzugFamGroesse())
			) {
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
		// Die stündlichen Vollkosten für TFOs können nicht linaer addiert werden, daher darf es keine Überschneidungen geben
		if (other.getStuendlicheVollkosten() != null) {
			if (this.getStuendlicheVollkosten() != null &&
				!MathUtil.isSame( this.getStuendlicheVollkosten(), other.getStuendlicheVollkosten())
			) {
				throw new IllegalArgumentException("Stuendliche Vollkosten können nicht gemerged werden");
			}
			this.setStuendlicheVollkosten(other.getStuendlicheVollkosten());
		}

		this.kitaPlusZuschlag = this.kitaPlusZuschlag || other.kitaPlusZuschlag;
		this.besondereBeduerfnisseZuschlag = add(this.getBesondereBeduerfnisseZuschlag(), other.getBesondereBeduerfnisseZuschlag());
		this.requiredAgeForAnspruchNotReached = this.requiredAgeForAnspruchNotReached || other.requiredAgeForAnspruchNotReached;
		this.gesuchBeendenKonkubinatMitZweiGS = this.gesuchBeendenKonkubinatMitZweiGS || other.gesuchBeendenKonkubinatMitZweiGS;
		if (MathUtil.isZero(this.bgStundenFaktor) && !MathUtil.isZero(other.getBgStundenFaktor())) {
			this.setBgStundenFaktor(other.getBgStundenFaktor());
		}

		//integrations Typ kann nicht ändern wenn zwei Fachstellen zusammengezählt werde
		this.integrationTypFachstellenPensum = this.integrationTypFachstellenPensum == null ?
			other.integrationTypFachstellenPensum : this.integrationTypFachstellenPensum;

		//das flag kann sich nicht ändern, wenn einmal false ist es für die ganze gp false
		if (!other.verguenstigungGewuenscht) {
			this.verguenstigungGewuenscht = false;
		}

		//das flag kann sich nicht ändern, wenn einmal false ist es für die ganze gp false
		if (!other.finsitAccepted) {
			this.finsitAccepted = false;
		}
		this.betreuungInFerienzeit = this.betreuungInFerienzeit || other.betreuungInFerienzeit;

		this.eingewoehnungKosten = add(this.eingewoehnungKosten, other.eingewoehnungKosten);
		this.anwesenheitsTageProMonat = add(this.anwesenheitsTageProMonat, other.anwesenheitsTageProMonat);
		if (other.bedarfsstufe != null) {
			if (this.bedarfsstufe != null && this.bedarfsstufe == other.bedarfsstufe) {
				throw new IllegalArgumentException("Bedarfsstufe können nicht gemerged werden");
			}
			this.bedarfsstufe = other.bedarfsstufe;
		}
	}

	/**
	 * Abwesenheit Special Case: Teil-Monate-Abwesenheit ohne weitere Betreuung
	 * Die abwesenheit eben in Percent gilt fuer die ganze betreute Periode
	 */
	private void handleAbwesenheitKeinMehrBG(BGCalculationInput other) {
		// dort sollte man keinen BG auszahlen, als
		if (this.bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ZERO) > 0 && this.percentage != null
			&& (other.getBetreuungspensumProzent().compareTo(BigDecimal.ZERO) <= 0
			|| this.getBetreuungspensumProzent().compareTo(BigDecimal.ZERO) <= 0)) {
			// wenn wir vollkosten bezahlen muessen aber eine zeitabschnitt hat keine BG Pensum
			// wir verlaengern dann die vollkostenMonatAnteil
			this.bezahltVollkostenMonatAnteil = add(this.percentage, other.percentage);
		}
	}

	/**
	 * Abwesenheit Special Case: Teil Abwesenheit mit unterschiedliche Pensum im Monat
	 * Kosten und Pensum adaptieren wenn höhe roder kleiner in der Abwesende Abschnitt
	 */
	private void handleAbwesenheitPensumAenderung(BGCalculationInput other) {
		if ((this.bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ZERO) > 0
			|| other.bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ZERO) > 0) && this.percentage != null &&
			other.getBetreuungspensumProzent().compareTo(BigDecimal.ZERO) > 0
			&& this.getBetreuungspensumProzent().compareTo(BigDecimal.ZERO) > 0 &&
			this.getBetreuungspensumProzent().compareTo(other.getBetreuungspensumProzent()) != 0) {

			if (this.bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ZERO) > 0
				&& other.bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ZERO) <= 0) {
				this.setBetreuungspensumProzent(calculatePercentage(calculatePercentageBackward(
					other.getBetreuungspensumProzent(),
					other.percentage.doubleValue()), this.percentage.doubleValue()));
				this.setMonatlicheBetreuungskosten(calculatePercentage(calculatePercentageBackward(
					other.getMonatlicheBetreuungskosten(),
					other.percentage.doubleValue()), this.percentage.doubleValue()));
				this.anspruchspensumProzent = calculatePercentage(calculatePercentageBackward(
					other.anspruchspensumProzent, other.percentage.doubleValue()), this.percentage.doubleValue());
			}
			if (other.bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ZERO) > 0
				&& this.bezahltVollkostenMonatAnteil.compareTo(BigDecimal.ZERO) <= 0) {
				other.setBetreuungspensumProzent(calculatePercentage(calculatePercentageBackward(
					this.getBetreuungspensumProzent(),
					this.percentage.doubleValue()), other.percentage.doubleValue()));
				other.setMonatlicheBetreuungskosten(calculatePercentage(calculatePercentageBackward(
					this.getMonatlicheBetreuungskosten(),
					this.percentage.doubleValue()), other.percentage.doubleValue()));
				other.anspruchspensumProzent = calculatePercentage(calculatePercentageBackward(
					this.anspruchspensumProzent, this.percentage.doubleValue()), other.percentage.doubleValue());

			}
		}
	}

	private BigDecimal add(@Nullable BigDecimal b1, @Nullable BigDecimal b2) {
		BigDecimal result = BigDecimal.ZERO;

		if (b1 != null) {
			result = result.add(b1);
		}

		if (b2 != null) {
			result = result.add(b2);
		}

		return result;
	}

	public void calculateInputValuesProportionaly(double percentage) {
		if (!isPercentCaluclable(percentage)) {
			throw new IllegalArgumentException(
				"Prozentualle Input Berechnung kann nicht durchgeführt werden mit einem Prozentuallen Wert von "
					+ percentage);
		}
		this.percentage = calculatePercentage(BigDecimal.ONE, percentage);
		this.erwerbspensumGS1 = calculatePercentage(this.erwerbspensumGS1, percentage);
		this.erwerbspensumGS2 = calculatePercentage(this.erwerbspensumGS2, percentage);
		this.betreuungspensumProzent = calculatePercentage(this.betreuungspensumProzent, percentage);
		this.anspruchspensumProzent = calculatePercentage(this.anspruchspensumProzent, percentage);
		this.anspruchspensumRest = calculatePercentageInt(this.anspruchspensumRest, percentage);
		this.fachstellenpensum = calculatePercentageInt(this.fachstellenpensum, percentage);
		this.ausserordentlicherAnspruch = calculatePercentageInt(this.ausserordentlicherAnspruch, percentage);
		this.monatlicheBetreuungskosten = calculatePercentage(this.monatlicheBetreuungskosten, percentage);
		this.tarifHauptmahlzeit = calculatePercentage(this.tarifHauptmahlzeit, percentage);
		this.tarifNebenmahlzeit = calculatePercentage(this.tarifNebenmahlzeit, percentage);
		this.massgebendesEinkommenVorAbzugFamgr =
			calculatePercentage(this.massgebendesEinkommenVorAbzugFamgr, percentage);
		this.anzahlHauptmahlzeiten = calculatePercentage(this.anzahlHauptmahlzeiten, percentage);
		this.anzahlNebenmahlzeiten = calculatePercentage(this.anzahlNebenmahlzeiten, percentage);
		this.tsInputMitBetreuung.calculatePercentage(percentage);
		this.tsInputOhneBetreuung.calculatePercentage(percentage);
		this.bezahltVollkostenMonatAnteil = calculatePercentage(this.bezahltVollkostenMonatAnteil, percentage);
		this.eingewoehnungKosten = calculatePercentage(this.eingewoehnungKosten, percentage);
		this.anwesenheitsTageProMonat = calculatePercentage(this.anwesenheitsTageProMonat, percentage);
	}

	public void roundValuesAfterCalculateProportinaly() {
		this.massgebendesEinkommenVorAbzugFamgr = MathUtil.GANZZAHL.from(this.massgebendesEinkommenVorAbzugFamgr);
	}

	private boolean isPercentCaluclable(double percent) {
		return percent > 0 && percent < 100;
	}

	@Nullable
	private Integer calculatePercentage(@Nullable Integer value, double percent) {
		if (value == null) {
			return value;
		}

		return calculatePercentageInt(value, percent);
	}

	private int calculatePercentageInt(int value, double percent) {
		return Math.toIntExact(Math.round(actualCalculatePercentage(value * 1.0, percent)));
	}

	@Nullable
	private BigDecimal calculatePercentage(@Nullable BigDecimal value, double percent) {
		if (value == null) {
			return null;
		}

		return BigDecimal.valueOf(actualCalculatePercentage(value.doubleValue(), percent));
	}

	private double actualCalculatePercentage(double value, double percent) {
		if (value == 0) {
			return 0;
		}

		return value / 100 * percent;
	}

	@Nullable
	private BigDecimal calculatePercentageBackward(@Nullable BigDecimal value, double percent) {
		if (value == null) {
			return null;
		}

		return BigDecimal.valueOf(actualCalculatePercentageBackward(value.doubleValue(), percent));
	}

	private double actualCalculatePercentageBackward(double value, double percent) {
		if (value == 0) {
			return 0;
		}

		return value * 100 / percent;
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
			MathUtil.isSame(this.bezahltVollkostenMonatAnteil, other.bezahltVollkostenMonatAnteil) &&
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
				verguenstigungMahlzeitenBeantragt == other.verguenstigungMahlzeitenBeantragt &&
			MathUtil.isSame(tarifHauptmahlzeit, other.tarifHauptmahlzeit) &&
			MathUtil.isSame(tarifNebenmahlzeit, other.tarifNebenmahlzeit) &&
			MathUtil.isSame(anzahlHauptmahlzeiten, other.anzahlHauptmahlzeiten) &&
			MathUtil.isSame(anzahlNebenmahlzeiten, other.anzahlNebenmahlzeiten) &&
			// Zusätzliche Felder aus Result
			MathUtil.isSame(betreuungspensumProzent, other.betreuungspensumProzent) &&
			this.anspruchspensumProzent.compareTo(other.anspruchspensumProzent) == 0 &&
			MathUtil.isSame(abzugFamGroesse, other.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, other.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, other.massgebendesEinkommenVorAbzugFamgr) &&
			zuSpaetEingereicht == other.zuSpaetEingereicht &&
			minimalesEwpUnterschritten == other.minimalesEwpUnterschritten &&
			Objects.equals(einkommensjahr, other.einkommensjahr) &&
			besondereBeduerfnisseBestaetigt == other.besondereBeduerfnisseBestaetigt &&
			MathUtil.isSame(this.besondereBeduerfnisseZuschlag, other.besondereBeduerfnisseZuschlag) &&
			this.tsInputMitBetreuung.isSame(other.tsInputMitBetreuung) &&
			this.tsInputOhneBetreuung.isSame(other.tsInputOhneBetreuung) &&
			this.sozialhilfeempfaenger == other.sozialhilfeempfaenger &&
			this.kitaPlusZuschlag == other.kitaPlusZuschlag &&
			this.isKesbPlatzierung == other.isKesbPlatzierung &&
			this.geschwisternBonusKind2 == other.geschwisternBonusKind2 &&
			this.geschwisternBonusKind3 == other.geschwisternBonusKind3 &&
			this.anzahlGeschwister == other.anzahlGeschwister &&
			MathUtil.isSame(this.stuendlicheVollkosten, other.stuendlicheVollkosten) &&
			this.isAuszahlungAnEltern == other.isAuszahlungAnEltern &&
			Objects.equals( this.partnerIdentischMitVorgesuch , other.partnerIdentischMitVorgesuch) &&
			this.isEkvAccepted == other.isEkvAccepted &&
			this.requiredAgeForAnspruchNotReached == other.requiredAgeForAnspruchNotReached &&
			this.gesuchBeendenKonkubinatMitZweiGS == other.gesuchBeendenKonkubinatMitZweiGS &&
			MathUtil.isSame(this.bgStundenFaktor, other.bgStundenFaktor) &&
			this.integrationTypFachstellenPensum == other.integrationTypFachstellenPensum &&
			this.verguenstigungGewuenscht == other.verguenstigungGewuenscht &&
			this.finsitAccepted == other.finsitAccepted &&
			MathUtil.isSame(this.eingewoehnungKosten, other.eingewoehnungKosten) &&
			this.betreuungInFerienzeit == other.betreuungInFerienzeit &&
			this.anwesenheitsTageProMonat == other.anwesenheitsTageProMonat &&
			Objects.equals(this.bedarfsstufe, other.bedarfsstufe);
	}

	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSameSichtbareDaten(BGCalculationInput that) {
		//noinspection ObjectEquality,SimplifiableIfStatement
		if (this == that) {
			return true;
		}
		return babyTarif == that.babyTarif &&
			einschulungTyp == that.einschulungTyp &&
			betreuungsangebotTyp == that.betreuungsangebotTyp &&
			MathUtil.isSame(monatlicheBetreuungskosten, that.monatlicheBetreuungskosten) &&
			verguenstigungMahlzeitenBeantragt == that.verguenstigungMahlzeitenBeantragt &&
			// Zusätzliche Felder aus Result
			MathUtil.isSame(this.betreuungspensumProzent, that.betreuungspensumProzent) &&
			this.anspruchspensumProzent.compareTo(that.anspruchspensumProzent) == 0 &&
			MathUtil.isSame(this.abzugFamGroesse, that.abzugFamGroesse) &&
			MathUtil.isSame(this.famGroesse, that.famGroesse) &&
			MathUtil.isSame(this.massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			Objects.equals(this.einkommensjahr, that.einkommensjahr) &&
			this.besondereBeduerfnisseBestaetigt == that.besondereBeduerfnisseBestaetigt &&
			MathUtil.isSame(this.besondereBeduerfnisseZuschlag, that.besondereBeduerfnisseZuschlag) &&
			this.minimalesEwpUnterschritten == that.minimalesEwpUnterschritten &&
			this.tsInputMitBetreuung.isSame(that.tsInputMitBetreuung) &&
			this.tsInputOhneBetreuung.isSame(that.tsInputOhneBetreuung) &&
			this.sozialhilfeempfaenger == that.sozialhilfeempfaenger &&
			this.isZuSpaetEingereicht() == that.isZuSpaetEingereicht() &&
			this.isAuszahlungAnEltern == that.isAuszahlungAnEltern &&
			MathUtil.isSame(tarifHauptmahlzeit, that.tarifHauptmahlzeit) &&
			MathUtil.isSame(tarifNebenmahlzeit, that.tarifNebenmahlzeit) &&
			MathUtil.isSame(anzahlHauptmahlzeiten, that.anzahlHauptmahlzeiten) &&
			MathUtil.isSame(anzahlNebenmahlzeiten, that.anzahlNebenmahlzeiten) &&
			MathUtil.isSame(eingewoehnungKosten, that.eingewoehnungKosten) &&
			MathUtil.isSame(anwesenheitsTageProMonat, that.anwesenheitsTageProMonat) &&
			betreuungInFerienzeit == that.betreuungInFerienzeit &&
			Objects.equals(this.bedarfsstufe, that.bedarfsstufe);
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
		this.getParent().getBemerkungenDTOList().addBemerkung(ruleValidity, msgKey, locale, args);
	}

	//Alle Rules welche Bemerkungen hinzufügen, aber nach der Monatsrule laufen,
	// müssen der Bemerkung eine Gültigkeit hinzufügen, vorallem dann wenn sie
	// andere Bemerkungen überschrieben (VerfuegungsBemerkungDTOList#removeNotRequiredBemerkungen)
	public void addBemerkungWithGueltigkeitOfAbschnitt(
		@Nonnull MsgKey msgKey,
		@Nonnull Locale locale,
		@Nullable Object... args)
	{
		VerfuegungsBemerkungDTO bemerkung = new VerfuegungsBemerkungDTO(ruleValidity, msgKey, locale, args);
		bemerkung.setGueltigkeit(getParent().getGueltigkeit());
		this.getParent().getBemerkungenDTOList().addBemerkung(bemerkung);
	}

	public PensumUnits getPensumUnit() {
		return pensumUnit;
	}

	public void setPensumUnit(PensumUnits pensumUnit) {
		this.pensumUnit = pensumUnit;
	}

	/**
	 * Setzt den Anspruch auf 0, der bisherige Anspruch wird als AnspruchspensumRest gespeichert, fuer
	 * eine eventuelle weitere Betreuung.
	 */
	public void setAnspruchZeroAndSaveRestanspruch() {
		int anspruchVorRegel = getAnspruchspensumProzent();
		setAnspruchspensumProzent(0);
		setAnspruchspensumRest(anspruchVorRegel);
	}

	public boolean isSameVerfuegteMahlzeitenVerguenstigung() {
		return sameVerfuegteMahlzeitenVerguenstigung;
	}

	public void setSameVerfuegteMahlzeitenVerguenstigung(boolean sameVerfuegteMahlzeitenVerguenstigung) {
		this.sameVerfuegteMahlzeitenVerguenstigung = sameVerfuegteMahlzeitenVerguenstigung;
	}

	public int getMinimalErforderlichesPensum() {
		return minimalErforderlichesPensum;
	}

	public void setMinimalErforderlichesPensum(int minimalErforderlichesPensum) {
		this.minimalErforderlichesPensum = minimalErforderlichesPensum;
	}

	public int getRueckwirkendReduziertesPensumRest() {
		return rueckwirkendReduziertesPensumRest;
	}

	public void setRueckwirkendReduziertesPensumRest(int rueckwirkendReduziertesPensumRest) {
		this.rueckwirkendReduziertesPensumRest = rueckwirkendReduziertesPensumRest;
	}

	public boolean isKesbPlatzierung() {
		return isKesbPlatzierung;
	}

	public void setKesbPlatzierung(boolean kesbPlatzierung) {
		isKesbPlatzierung = kesbPlatzierung;
	}

	public boolean isKitaPlusZuschlag() {
		return kitaPlusZuschlag;
	}

	public void setKitaPlusZuschlag(boolean kitaPlusZuschlag) {
		this.kitaPlusZuschlag = kitaPlusZuschlag;
	}

	@Nullable
	public BigDecimal getBesondereBeduerfnisseZuschlag() {
		return besondereBeduerfnisseZuschlag;
	}

	public void setBesondereBeduerfnisseZuschlag(@Nullable BigDecimal besondereBeduerfnisseZuschlag) {
		this.besondereBeduerfnisseZuschlag = besondereBeduerfnisseZuschlag;
	}

	public boolean isGeschwisternBonusKind3() {
		return geschwisternBonusKind3;
	}

	public void setGeschwisternBonusKind3(boolean geschwisternBonusKind3) {
		this.geschwisternBonusKind3 = geschwisternBonusKind3;
	}

	public boolean isGeschwisternBonusKind2() {
		return geschwisternBonusKind2;
	}

	public void setGeschwisternBonusKind2(boolean geschwisternBonusKind2) {
		this.geschwisternBonusKind2 = geschwisternBonusKind2;
	}

	public BigDecimal getStuendlicheVollkosten() {
		return stuendlicheVollkosten;
	}

	public void setStuendlicheVollkosten(BigDecimal stuendlicheVollkosten) {
		this.stuendlicheVollkosten = stuendlicheVollkosten;
	}

	public void setPartnerIdentischMitVorgesuch(@Nullable Boolean samePartner) {
		this.partnerIdentischMitVorgesuch = samePartner;
	}

	public Boolean getPartnerIdentischMitVorgesuch() {
		return partnerIdentischMitVorgesuch;
	}

	public boolean isEkvAccepted() {
		return isEkvAccepted;
	}

	public void setEkvAccepted(boolean ekvAccepted) {
		isEkvAccepted = ekvAccepted;
	}

	public void setGesuchBeendenKonkubinatMitZweiGS(boolean gesuchBeendenKonkubinatMitZweiGS) {
		this.gesuchBeendenKonkubinatMitZweiGS = gesuchBeendenKonkubinatMitZweiGS;
	}

	public boolean isGesuchBeendenKonkubinatMitZweiGS() {
		return gesuchBeendenKonkubinatMitZweiGS;
	}

	public void setBgStundenFaktor(BigDecimal bgStundenFaktor) {
		this.bgStundenFaktor = bgStundenFaktor;
	}

	public BigDecimal getBgStundenFaktor() {
		return bgStundenFaktor;
	}

	public IntegrationTyp getIntegrationTypFachstellenPensum() {
		return integrationTypFachstellenPensum;
	}

	public void setIntegrationTypFachstellenPensum(IntegrationTyp integrationTypFachstellenPensum) {
		this.integrationTypFachstellenPensum = integrationTypFachstellenPensum;
	}

	public boolean isVerguenstigungGewuenscht() {
		return verguenstigungGewuenscht;
	}

	public void setVerguenstigungGewuenscht(boolean verguenstigungGewuenscht) {
		this.verguenstigungGewuenscht = verguenstigungGewuenscht;
	}

	public boolean isFinsitAccepted() {
		return finsitAccepted;
	}

	public void setFinsitAccepted(boolean finsitAccepted) {
		this.finsitAccepted = finsitAccepted;
	}

	public BigDecimal getEingewoehnungKosten() {
		return eingewoehnungKosten;
	}

	public void setEingewoehnungKosten(BigDecimal eingewoehnungKosten) {
		this.eingewoehnungKosten = eingewoehnungKosten;
	}

	public boolean isBetreuungInFerienzeit() {
		return betreuungInFerienzeit;
	}

	public void setBetreuungInFerienzeit(boolean betreuungInFerienzeit) {
		this.betreuungInFerienzeit = betreuungInFerienzeit;
	}
}
