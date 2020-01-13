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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkung;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Dieses Objekt repraesentiert einen Zeitabschnitt wahrend eines Betreeungsgutscheinantrags waehrend dem die Faktoren
 * die fuer die Berechnung des Gutscheins der Betreuung relevant sind konstant geblieben sind.
 */
@Entity
@Audited
public class VerfuegungZeitabschnitt extends AbstractDateRangedEntity implements Comparable<VerfuegungZeitabschnitt> {

	private static final long serialVersionUID = 7250339356897563374L;

	// Zwischenresulate aus DATA-Rules ("Abschnitt")

	@Transient
	private boolean sameVerfuegteVerfuegungsrelevanteDaten;

	// Dieser Wert wird gebraucht, um zu wissen ob die Korrektur relevant fuer die Zahlungen ist, da nur wenn die
	// Verguenstigung sich geaendert hat, muss man die Korrektur beruecksichtigen
	@Transient
	private boolean sameAusbezahlteVerguenstigung;

	@Transient
	@Nullable
	private Integer erwerbspensumGS1 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	@Nullable
	private Integer erwerbspensumGS2 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	private Set<Taetigkeit> taetigkeiten = new HashSet<>();

	@Transient
	private int fachstellenpensum;

	@Transient
	private int ausserordentlicherAnspruch;

	@Transient
	//es muss by default null sein um zu wissen, wann es nicht definiert wurde
	private Boolean wohnsitzNichtInGemeindeGS1 = null;

	@Transient
	// Wenn Vollkosten bezahlt werden muessen, werden die Vollkosten berechnet und als Elternbeitrag gesetzt
	private boolean bezahltVollkosten;

	@Transient
	private boolean longAbwesenheit;

	@Transient
	private int anspruchspensumRest;

	@Transient
	// Achtung, dieses Flag wird erst ab 1. des Folgemonats gesetzt, weil die Finanzielle Situation ab dann gilt. Für
	// Erwerbspensen zählt der GS2 ab sofort!
	private boolean hasSecondGesuchstellerForFinanzielleSituation;

	@Transient
	private boolean ekv1Alleine;

	@Transient
	private boolean ekv1ZuZweit;

	@Transient
	private boolean ekv2Alleine;

	@Transient
	private boolean ekv2ZuZweit;

	@Transient
	private boolean kategorieMaxEinkommen = false;

	@Transient
	private boolean kategorieKeinPensum = false;

	@Transient
	private boolean abschnittLiegtNachBEGUStartdatum = true;

	@Transient
	private BigDecimal monatlicheBetreuungskosten = BigDecimal.ZERO;

	@Column(nullable = false)
	private @Min(0) @NotNull BigDecimal betreuungspensumProzent = BigDecimal.ZERO;

	/**
	 * Anpsruch für diese Kita, bzw. Tageseltern Kleinkinder
	 */
	@Column(nullable = false)
	private @Max(100) @Min(0) @NotNull int anspruchberechtigtesPensum;

	@Nonnull
	@Column(nullable = true) // nullable, because migration is needed
	private @Min(0) BigDecimal verfuegteAnzahlZeiteinheiten = BigDecimal.ZERO;

	@Nonnull
	@Column(nullable = true) // nullable, because migration is needed
	private @Min(0) BigDecimal anspruchsberechtigteAnzahlZeiteinheiten = BigDecimal.ZERO;

	@Nonnull
	@Enumerated(EnumType.STRING)
	@Column(nullable = true, length = Constants.DB_DEFAULT_SHORT_LENGTH) // nullable, because migration is needed
	private PensumUnits zeiteinheit = PensumUnits.DAYS;

	@Column(nullable = true)
	private BigDecimal betreuungspensumZeiteinheit = BigDecimal.ZERO;

	@Column(nullable = true)
	private BigDecimal vollkosten = BigDecimal.ZERO;

	@Column(nullable = true)
	private BigDecimal verguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.ZERO;

	@Column(nullable = true)
	private BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag = BigDecimal.ZERO;

	@Column(nullable = true)
	private BigDecimal verguenstigung = BigDecimal.ZERO;

	@Column(nullable = true)
	private BigDecimal minimalerElternbeitrag = BigDecimal.ZERO;

	@Column(nullable = true)
	private BigDecimal elternbeitrag = BigDecimal.ZERO;

	@Column(nullable = true)
	private BigDecimal abzugFamGroesse = null;

	@Column(nullable = true)
	private BigDecimal famGroesse = null;

	@Column(nullable = true)
	@Nonnull
	private BigDecimal massgebendesEinkommenVorAbzugFamgr = BigDecimal.ZERO;

	@Column(nullable = false)
	private @NotNull Integer einkommensjahr;

	// Die Bemerkungen werden vorerst in eine Map geschrieben, damit einzelne
	// Bemerkungen spaeter wieder zugreifbar sind. Am Ende des RuleSets werden sie ins persistente Feld
	// "bemerkungen" geschrieben
	@Transient
	private final Map<MsgKey, VerfuegungsBemerkung> bemerkungenMap = new TreeMap<>();

	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private @Size(max = Constants.DB_TEXTAREA_LENGTH) String bemerkungen = "";

	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegung_zeitabschnitt_verfuegung_id"), nullable = false)
	@ManyToOne(optional = false)
	private @NotNull
	Verfuegung verfuegung;

	@Column(nullable = false)
	private @NotNull boolean zuSpaetEingereicht;

	@Column(nullable = false)
	private @NotNull boolean minimalesEwpUnterschritten;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private @NotNull
	VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus =
		VerfuegungsZeitabschnittZahlungsstatus.NEU;

	@OneToMany(mappedBy = "verfuegungZeitabschnitt")
	private @NotNull
	List<Zahlungsposition> zahlungsposition = new ArrayList<>();

	@Transient
	private boolean babyTarif;

	@Transient
	private boolean eingeschult;

	@Column(nullable = false)
	private boolean besondereBeduerfnisseBestaetigt;

	public VerfuegungZeitabschnitt() {
	}

	/**
	 * copy Konstruktor
	 */
	@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "PMD.ConstructorCallsOverridableMethod" })
	public VerfuegungZeitabschnitt(VerfuegungZeitabschnitt toCopy) {
		this.setGueltigkeit(new DateRange(toCopy.getGueltigkeit()));
		this.erwerbspensumGS1 = toCopy.erwerbspensumGS1;
		this.erwerbspensumGS2 = toCopy.erwerbspensumGS2;
		this.taetigkeiten = toCopy.taetigkeiten;
		this.fachstellenpensum = toCopy.fachstellenpensum;
		this.ausserordentlicherAnspruch = toCopy.ausserordentlicherAnspruch;
		this.zuSpaetEingereicht = toCopy.zuSpaetEingereicht;
		this.minimalesEwpUnterschritten = toCopy.minimalesEwpUnterschritten;
		this.wohnsitzNichtInGemeindeGS1 = toCopy.wohnsitzNichtInGemeindeGS1;
		this.bezahltVollkosten = toCopy.bezahltVollkosten;
		this.longAbwesenheit = toCopy.isLongAbwesenheit();
		this.anspruchspensumRest = toCopy.anspruchspensumRest;
		this.betreuungspensumProzent = toCopy.betreuungspensumProzent;
		this.monatlicheBetreuungskosten = toCopy.monatlicheBetreuungskosten;
		this.anspruchberechtigtesPensum = toCopy.anspruchberechtigtesPensum;
		this.verfuegteAnzahlZeiteinheiten = toCopy.verfuegteAnzahlZeiteinheiten;
		this.anspruchsberechtigteAnzahlZeiteinheiten = toCopy.anspruchsberechtigteAnzahlZeiteinheiten;
		this.zeiteinheit = toCopy.zeiteinheit;
		this.setBetreuungspensumZeiteinheit(toCopy.getBetreuungspensumZeiteinheit());
		this.setVollkosten(toCopy.getVollkosten());
		this.setElternbeitrag(toCopy.getElternbeitrag());
		this.setVerguenstigungOhneBeruecksichtigungVollkosten(toCopy.getVerguenstigungOhneBeruecksichtigungVollkosten());
		this.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(toCopy.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		this.setVerguenstigung(toCopy.getVerguenstigung());
		this.setMinimalerElternbeitrag(toCopy.getMinimalerElternbeitrag());
		this.setAbzugFamGroesse(toCopy.getAbzugFamGroesse());
		this.setFamGroesse(toCopy.getFamGroesse());
		this.setMassgebendesEinkommenVorAbzugFamgr(toCopy.getMassgebendesEinkommenVorAbzFamgr());
		this.hasSecondGesuchstellerForFinanzielleSituation = toCopy.hasSecondGesuchstellerForFinanzielleSituation;
		this.einkommensjahr = toCopy.einkommensjahr;
		this.ekv1Alleine = toCopy.ekv1Alleine;
		this.ekv1ZuZweit = toCopy.ekv1ZuZweit;
		this.ekv2Alleine = toCopy.ekv2Alleine;
		this.ekv2ZuZweit = toCopy.ekv2ZuZweit;
		this.bemerkungen = toCopy.bemerkungen;
		this.mergeBemerkungenMap(toCopy.getBemerkungenMap());
		//noinspection ConstantConditions: Muss erst beim Speichern gesetzt sein
		this.verfuegung = null;
		this.kategorieMaxEinkommen = toCopy.kategorieMaxEinkommen;
		this.kategorieKeinPensum = toCopy.kategorieKeinPensum;
		this.zahlungsstatus = toCopy.zahlungsstatus;
		this.abschnittLiegtNachBEGUStartdatum = toCopy.abschnittLiegtNachBEGUStartdatum;
		this.babyTarif = toCopy.babyTarif;
		this.eingeschult = toCopy.eingeschult;
		this.besondereBeduerfnisseBestaetigt = toCopy.besondereBeduerfnisseBestaetigt;
	}

	/**
	 * Erstellt einen Zeitabschnitt mit der gegebenen gueltigkeitsdauer
	 */
	public VerfuegungZeitabschnitt(DateRange gueltigkeit) {
		this.setGueltigkeit(new DateRange(gueltigkeit));
	}

	@Override
	public void setVorgaengerId(String vorgaengerId) {
		// nop -> Diese Methode darf eingentlich nicht verwendet werden, da ein VerfuegungZeitabschnitt keinen
		// Vorgaenger hat
	}

	@Override
	public String getVorgaengerId() {
		return null; // Diese Methode darf eingentlich nicht verwendet werden, da ein VerfuegungZeitabschnitt keinen
		// Vorgaenger hat
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

	@Nonnull
	public BigDecimal getBetreuungspensumProzent() {
		return betreuungspensumProzent;
	}

	public void setBetreuungspensumProzent(@Nonnull BigDecimal betreuungspensumProzent) {
		this.betreuungspensumProzent = betreuungspensumProzent;
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

	public void setAnspruchberechtigtesPensum(int anspruchberechtigtesPensum) {
		this.anspruchberechtigtesPensum = anspruchberechtigtesPensum;
	}

	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(BigDecimal vollkosten) {
		// Wir stellen direkt im setter sicher, dass wir die Beträge mit 2 Nachkommastelle speichern
		this.vollkosten = MathUtil.toTwoKommastelle(vollkosten);
	}

	public BigDecimal getElternbeitrag() {
		return elternbeitrag;
	}

	public void setElternbeitrag(BigDecimal elternbeitrag) {
		// Wir stellen direkt im setter sicher, dass wir die Beträge mit 2 Nachkommastelle speichern
		this.elternbeitrag = MathUtil.toTwoKommastelle(elternbeitrag);
	}

	public BigDecimal getAbzugFamGroesse() {
		return abzugFamGroesse;
	}

	public void setAbzugFamGroesse(BigDecimal abzugFamGroesse) {
		// Wir stellen direkt im setter sicher, dass wir die Beträge mit 2 Nachkommastelle speichern
		this.abzugFamGroesse = MathUtil.toTwoKommastelle(abzugFamGroesse);
	}

	/**
	 * @return berechneter Wert. Zieht vom massgebenenEinkommenVorAbzug den Familiengroessen Abzug ab
	 */
	@Nonnull
	public BigDecimal getMassgebendesEinkommen() {
		BigDecimal abzugFamSize = this.abzugFamGroesse == null ? BigDecimal.ZERO : this.abzugFamGroesse;
		return MathUtil.DEFAULT.subtractNullSafe(this.massgebendesEinkommenVorAbzugFamgr, abzugFamSize);
	}

	@Nonnull
	public BigDecimal getMassgebendesEinkommenVorAbzFamgr() {
		return massgebendesEinkommenVorAbzugFamgr;
	}

	public void setMassgebendesEinkommenVorAbzugFamgr(@Nonnull BigDecimal massgebendesEinkommenVorAbzugFamgr) {
		// Wir stellen direkt im setter sicher, dass wir die Beträge mit 2 Nachkommastelle speichern
		this.massgebendesEinkommenVorAbzugFamgr = MathUtil.toTwoKommastelle(massgebendesEinkommenVorAbzugFamgr);
	}

	public boolean isHasSecondGesuchstellerForFinanzielleSituation() {
		return hasSecondGesuchstellerForFinanzielleSituation;
	}

	public void setHasSecondGesuchstellerForFinanzielleSituation(boolean hasSecondGesuchstellerForFinanzielleSituation) {
		this.hasSecondGesuchstellerForFinanzielleSituation = hasSecondGesuchstellerForFinanzielleSituation;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	public Map<MsgKey, VerfuegungsBemerkung> getBemerkungenMap() {
		return bemerkungenMap;
	}

	@Nonnull
	public Verfuegung getVerfuegung() {
		return verfuegung;
	}

	public void setVerfuegung(@Nonnull Verfuegung verfuegung) {
		this.verfuegung = verfuegung;
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

	public boolean isWohnsitzNichtInGemeindeGS1() {
		return wohnsitzNichtInGemeindeGS1 != null ? wohnsitzNichtInGemeindeGS1 : true;
	}

	public void setWohnsitzNichtInGemeindeGS1(Boolean wohnsitzNichtInGemeindeGS1) {
		this.wohnsitzNichtInGemeindeGS1 = wohnsitzNichtInGemeindeGS1;
	}

	public BigDecimal getFamGroesse() {
		return famGroesse;
	}

	public void setFamGroesse(BigDecimal famGroesse) {
		// Wir stellen direkt im setter sicher, dass wir die FamGroesse mit 1 Nachkommastelle speichern
		this.famGroesse = MathUtil.toOneKommastelle(famGroesse);
	}

	@Nonnull
	public Integer getEinkommensjahr() {
		return einkommensjahr;
	}

	public void setEinkommensjahr(@Nonnull Integer einkommensjahr) {
		this.einkommensjahr = einkommensjahr;
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

	@Nonnull
	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatus() {
		return zahlungsstatus;
	}

	public void setZahlungsstatus(@Nonnull VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus) {
		this.zahlungsstatus = zahlungsstatus;
	}

	@Nonnull
	public List<Zahlungsposition> getZahlungsposition() {
		return zahlungsposition;
	}

	public void setZahlungsposition(@Nonnull List<Zahlungsposition> zahlungsposition) {
		this.zahlungsposition = zahlungsposition;
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

	public boolean isBesondereBeduerfnisseBestaetigt() {
		return besondereBeduerfnisseBestaetigt;
	}

	public void setBesondereBeduerfnisseBestaetigt(boolean besondereBeduerfnisseBestaetigt) {
		this.besondereBeduerfnisseBestaetigt = besondereBeduerfnisseBestaetigt;
	}

	public BigDecimal getVerguenstigungOhneBeruecksichtigungVollkosten() {
		return verguenstigungOhneBeruecksichtigungVollkosten;
	}

	public void setVerguenstigungOhneBeruecksichtigungVollkosten(
		BigDecimal verguenstigungOhneBeruecksichtigungVollkosten) {

		// Wir stellen direkt im setter sicher, dass wir die Beträge mit 2 Nachkommastelle speichern
		this.verguenstigungOhneBeruecksichtigungVollkosten =
			MathUtil.toTwoKommastelle(verguenstigungOhneBeruecksichtigungVollkosten);
	}

	public BigDecimal getVerguenstigungOhneBeruecksichtigungMinimalbeitrag() {
		return verguenstigungOhneBeruecksichtigungMinimalbeitrag;
	}

	public void setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(
		BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag
	) {
		// Wir stellen direkt im setter sicher, dass wir die Beträge mit 2 Nachkommastelle speichern
		this.verguenstigungOhneBeruecksichtigungMinimalbeitrag =
			MathUtil.toTwoKommastelle(verguenstigungOhneBeruecksichtigungMinimalbeitrag);
	}

	public BigDecimal getVerguenstigung() {
		return verguenstigung;
	}

	public void setVerguenstigung(BigDecimal verguenstigung) {
		// Wir stellen direkt im setter sicher, dass wir die Beträge mit 2 Nachkommastelle speichern
		this.verguenstigung = MathUtil.toTwoKommastelle(verguenstigung);
	}

	public BigDecimal getMinimalerElternbeitrag() {
		return minimalerElternbeitrag;
	}

	public void setMinimalerElternbeitrag(BigDecimal minimalerElternbeitrag) {
		// Wir stellen direkt im setter sicher, dass wir die Beträge mit 2 Nachkommastelle speichern
		this.minimalerElternbeitrag = MathUtil.toTwoKommastelle(minimalerElternbeitrag);
	}

	@Nonnull
	public BigDecimal getMinimalerElternbeitragGekuerzt() {
		BigDecimal vollkostenMinusVerguenstigung = MathUtil.DEFAULT
			.subtract(getVollkosten(), getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		if (vollkostenMinusVerguenstigung.compareTo(getMinimalerElternbeitrag()) > 0) {
			return MathUtil.DEFAULT.from(0);
		}
		return MathUtil.DEFAULT.subtract(getMinimalerElternbeitrag(), vollkostenMinusVerguenstigung);
	}

	/**
	 * Addiert die Daten von "other" zu diesem VerfuegungsZeitabschnitt
	 */
	@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "PMD.NcssMethodCount" })
	public void add(VerfuegungZeitabschnitt other) {
		this.setBetreuungspensumProzent(this.getBetreuungspensumProzent().add(other.getBetreuungspensumProzent()));
		this.setFachstellenpensum(this.getFachstellenpensum() + other.getFachstellenpensum());
		this.setAusserordentlicherAnspruch(this.getAusserordentlicherAnspruch()
			+ other.getAusserordentlicherAnspruch());
		this.setAnspruchspensumRest(this.getAnspruchspensumRest() + other.getAnspruchspensumRest());
		this.setAnspruchberechtigtesPensum(this.getAnspruchberechtigtesPensum()
			+ other.getAnspruchberechtigtesPensum());

		BigDecimal newMonatlicheBetreuungskosten = BigDecimal.ZERO;
		if (this.getMonatlicheBetreuungskosten() != null) {
			newMonatlicheBetreuungskosten = newMonatlicheBetreuungskosten.add(this.getMonatlicheBetreuungskosten());
		}
		if (other.getMonatlicheBetreuungskosten() != null) {
			newMonatlicheBetreuungskosten = newMonatlicheBetreuungskosten.add(other.getMonatlicheBetreuungskosten());
		}
		this.setMonatlicheBetreuungskosten(newMonatlicheBetreuungskosten);

		this.setVerfuegteAnzahlZeiteinheiten(other.getVerfuegteAnzahlZeiteinheiten()
			.add(this.verfuegteAnzahlZeiteinheiten));
		this.setAnspruchsberechtigteAnzahlZeiteinheiten(other.getAnspruchsberechtigteAnzahlZeiteinheiten()
			.add(this.anspruchsberechtigteAnzahlZeiteinheiten));

		BigDecimal newBetreuungsstunden = BigDecimal.ZERO;
		if (this.getBetreuungspensumZeiteinheit() != null) {
			newBetreuungsstunden = newBetreuungsstunden.add(this.getBetreuungspensumZeiteinheit());
		}
		if (other.getBetreuungspensumZeiteinheit() != null) {
			newBetreuungsstunden = newBetreuungsstunden.add(other.getBetreuungspensumZeiteinheit());
		}
		this.setBetreuungspensumZeiteinheit(newBetreuungsstunden);

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

		this.getTaetigkeiten().addAll(other.getTaetigkeiten());

		this.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.addNullSafe(
			this.getMassgebendesEinkommenVorAbzFamgr(),
			other.getMassgebendesEinkommenVorAbzFamgr()));

		this.addAllBemerkungen(other.getBemerkungenMap());
		this.setZuSpaetEingereicht(this.isZuSpaetEingereicht() || other.isZuSpaetEingereicht());

		this.setWohnsitzNichtInGemeindeGS1(this.isWohnsitzNichtInGemeindeGS1() && other.isWohnsitzNichtInGemeindeGS1());

		this.setBezahltVollkosten(this.isBezahltVollkosten() || other.isBezahltVollkosten());

		this.setLongAbwesenheit(this.isLongAbwesenheit() || other.isLongAbwesenheit());

		// Der Familiengroessen Abzug kann nicht linear addiert werden, daher darf es hier nie uebschneidungen geben
		if (other.getAbzugFamGroesse() != null) {
			Validate.isTrue(this.getAbzugFamGroesse() == null, "Familiengoressenabzug kann nicht gemerged werden");
			this.setAbzugFamGroesse(other.getAbzugFamGroesse());
		}
		// Die Familiengroesse kann nicht linear addiert werden, daher darf es hier nie uebschneidungen geben
		if (other.getFamGroesse() != null) {
			Validate.isTrue(this.getFamGroesse() == null, "Familiengoressen kann nicht gemerged werden");
			this.setFamGroesse(other.getFamGroesse());
		}
		this.setEinkommensjahr(other.getEinkommensjahr());
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
		this.setBesondereBeduerfnisseBestaetigt(this.besondereBeduerfnisseBestaetigt
			|| other.besondereBeduerfnisseBestaetigt);
		this.setMinimalesEwpUnterschritten(this.minimalesEwpUnterschritten || other.minimalesEwpUnterschritten);
	}

	public void addBemerkung(@Nonnull RuleKey ruleKey, @Nonnull MsgKey msgKey, @Nonnull Locale locale) {
		bemerkungenMap.put(msgKey, new VerfuegungsBemerkung(ruleKey, msgKey, locale));
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	public void addBemerkung(
		@Nonnull RuleKey ruleKey,
		@Nonnull MsgKey msgKey,
		@Nonnull Locale locale,
		@Nonnull Object... args) {
		bemerkungenMap.put(msgKey, new VerfuegungsBemerkung(ruleKey, msgKey, locale, args));
	}

	public void addAllBemerkungen(Map<MsgKey, VerfuegungsBemerkung> otherBemerkungenMap) {
		this.bemerkungenMap.putAll(otherBemerkungenMap);
	}

	/**
	 * Fügt otherBemerkungen zur Liste hinzu, falls sie noch nicht vorhanden sind
	 */
	public final void mergeBemerkungenMap(Map<MsgKey, VerfuegungsBemerkung> otherBemerkungenMap) {
		for (Entry<MsgKey, VerfuegungsBemerkung> msgKeyVerfuegungsBemerkungEntry : otherBemerkungenMap.entrySet()) {
			if (!getBemerkungenMap().containsKey(msgKeyVerfuegungsBemerkungEntry.getKey())) {
				this.bemerkungenMap.put(
					msgKeyVerfuegungsBemerkungEntry.getKey(),
					msgKeyVerfuegungsBemerkungEntry.getValue());
			}
		}
	}

	/**
	 * Dieses Pensum ist abhängig vom Erwerbspensum der Eltern respektive von dem durch die Fachstelle definierten
	 * Pensum.
	 * <p>
	 * Dieses Pensum kann grösser oder kleiner als das Betreuungspensum sein.
	 * <p>
	 * Beispiel: Zwei Eltern arbeiten zusammen 140%. In diesem Fall ist das anspruchsberechtigte Pensum 40%.
	 */
	public int getAnspruchberechtigtesPensum() {
		return anspruchberechtigtesPensum;
	}

	@Nonnull
	public BigDecimal getVerfuegteAnzahlZeiteinheiten() {
		return verfuegteAnzahlZeiteinheiten;
	}

	public void setVerfuegteAnzahlZeiteinheiten(@Nonnull BigDecimal verfuegteAnzahlZeiteinheiten) {
		this.verfuegteAnzahlZeiteinheiten = verfuegteAnzahlZeiteinheiten;
	}

	@Nonnull
	public BigDecimal getAnspruchsberechtigteAnzahlZeiteinheiten() {
		return anspruchsberechtigteAnzahlZeiteinheiten;
	}

	public void setAnspruchsberechtigteAnzahlZeiteinheiten(@Nonnull BigDecimal zeiteinheiten) {
		this.anspruchsberechtigteAnzahlZeiteinheiten = zeiteinheiten;
	}

	@Nonnull
	public PensumUnits getZeiteinheit() {
		return zeiteinheit;
	}

	public void setZeiteinheit(@Nonnull PensumUnits zeiteinheit) {
		this.zeiteinheit = zeiteinheit;
	}

	/**
	 * Das BG-Pensum (Pensum des Gutscheins) wird zum BG-Tarif berechnet und kann höchstens so gross sein, wie das
	 * Betreuungspensum.
	 * Falls das anspruchsberechtigte Pensum unter dem Betreuungspensum liegt, entspricht das BG-Pensum dem
	 * anspruchsberechtigten Pensum.
	 * <p>
	 * Ein Kind mit einem Betreuungspensum von 60% und einem anspruchsberechtigten Pensum von 40% hat ein BG-Pensum
	 * von 40%.
	 * Ein Kind mit einem Betreuungspensum von 40% und einem anspruchsberechtigten Pensum von 60% hat ein BG-Pensum
	 * von 40%.
	 */
	@Transient
	public BigDecimal getBgPensum() {
		return getBetreuungspensumProzent().min(MathUtil.DEFAULT.from(getAnspruchberechtigtesPensum()));
	}

	@Override
	public String toString() {
		String sb = '[' + Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb()) + " - "
			+ Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigBis()) + "] "
			+ " Status: " + zahlungsstatus + '\t'
			+ " EP GS1: " + erwerbspensumGS1 + '\t'
			+ " EP GS2: " + erwerbspensumGS2 + '\t'
			+ " BetrPensum: " + betreuungspensumProzent + '\t'
			+ " Anspruch: " + anspruchberechtigtesPensum + '\t'
			+ " Restanspruch: " + anspruchspensumRest + '\t'
			+ " BG-Pensum: " + getBgPensum() + '\t'
			+ " Vollkosten: " + vollkosten + '\t'
			+ " Elternbeitrag: " + elternbeitrag + '\t'
			+ " Bemerkungen: " + bemerkungen + '\t'
			+ " Einkommensjahr: " + einkommensjahr + '\t'
			+ " Einkommen: " + massgebendesEinkommenVorAbzugFamgr + '\t'
			+ " Abzug Fam: " + abzugFamGroesse;
		return sb;
	}

	public String toStringFinanzielleSituation() {
		String sb = '[' + Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb()) + " - "
			+ Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigBis()) + "] "
			+ " MassgebendesEinkommenVorAbzugFamiliengroesse: " + massgebendesEinkommenVorAbzugFamgr + '\t'
			+ " AbzugFamiliengroesse: " + abzugFamGroesse + '\t'
			+ " MassgebendesEinkommen: " + getMassgebendesEinkommen() + '\t'
			+ " Einkommensjahr: " + einkommensjahr + '\t'
			+ " Familiengroesse: " + famGroesse + '\t'
			+ " Bemerkungen: " + bemerkungen;
		return sb;
	}

	@SuppressWarnings({ "OverlyComplexBooleanExpression", "AccessingNonPublicFieldOfAnotherObject",
		"OverlyComplexMethod" })
	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof VerfuegungZeitabschnitt)) {
			return false;
		}
		final VerfuegungZeitabschnitt otherVerfuegungZeitabschnitt = (VerfuegungZeitabschnitt) other;
		return isSameErwerbspensum(erwerbspensumGS1, otherVerfuegungZeitabschnitt.erwerbspensumGS1) &&
			isSameErwerbspensum(erwerbspensumGS2, otherVerfuegungZeitabschnitt.erwerbspensumGS2) &&
			MathUtil.isSame(betreuungspensumProzent, otherVerfuegungZeitabschnitt.betreuungspensumProzent) &&
			fachstellenpensum == otherVerfuegungZeitabschnitt.fachstellenpensum &&
			ausserordentlicherAnspruch == otherVerfuegungZeitabschnitt.ausserordentlicherAnspruch &&
			anspruchspensumRest == otherVerfuegungZeitabschnitt.anspruchspensumRest &&
			anspruchberechtigtesPensum == otherVerfuegungZeitabschnitt.anspruchberechtigtesPensum &&
			hasSecondGesuchstellerForFinanzielleSituation
				== otherVerfuegungZeitabschnitt.hasSecondGesuchstellerForFinanzielleSituation &&
			MathUtil.isSame(abzugFamGroesse, otherVerfuegungZeitabschnitt.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, otherVerfuegungZeitabschnitt.famGroesse) &&
			MathUtil.isSame(
				massgebendesEinkommenVorAbzugFamgr,
				otherVerfuegungZeitabschnitt.massgebendesEinkommenVorAbzugFamgr) &&
			zuSpaetEingereicht == otherVerfuegungZeitabschnitt.zuSpaetEingereicht &&
			minimalesEwpUnterschritten == otherVerfuegungZeitabschnitt.minimalesEwpUnterschritten &&
			bezahltVollkosten == otherVerfuegungZeitabschnitt.bezahltVollkosten &&
			longAbwesenheit == otherVerfuegungZeitabschnitt.longAbwesenheit &&
			Objects.equals(einkommensjahr, otherVerfuegungZeitabschnitt.einkommensjahr) &&
			ekv1Alleine == otherVerfuegungZeitabschnitt.ekv1Alleine &&
			ekv1ZuZweit == otherVerfuegungZeitabschnitt.ekv1ZuZweit &&
			ekv2Alleine == otherVerfuegungZeitabschnitt.ekv2Alleine &&
			ekv2ZuZweit == otherVerfuegungZeitabschnitt.ekv2ZuZweit &&
			abschnittLiegtNachBEGUStartdatum == otherVerfuegungZeitabschnitt.abschnittLiegtNachBEGUStartdatum &&
			babyTarif == otherVerfuegungZeitabschnitt.babyTarif &&
			eingeschult == otherVerfuegungZeitabschnitt.eingeschult &&
			besondereBeduerfnisseBestaetigt == otherVerfuegungZeitabschnitt.besondereBeduerfnisseBestaetigt &&
			zahlungsstatus == otherVerfuegungZeitabschnitt.zahlungsstatus &&
			Objects.equals(wohnsitzNichtInGemeindeGS1, otherVerfuegungZeitabschnitt.wohnsitzNichtInGemeindeGS1) &&
			Objects.equals(bemerkungen, otherVerfuegungZeitabschnitt.bemerkungen) &&
			Objects.equals(bemerkungenMap, otherVerfuegungZeitabschnitt.bemerkungenMap) &&
			MathUtil.isSame(monatlicheBetreuungskosten, otherVerfuegungZeitabschnitt.monatlicheBetreuungskosten) &&
			isSameZeiteinheiten(otherVerfuegungZeitabschnitt);
	}

	public boolean isSameSichtbareDaten(VerfuegungZeitabschnitt that) {
		//noinspection ObjectEquality,SimplifiableIfStatement
		if (this == that) {
			return true;
		}

		return MathUtil.isSame(betreuungspensumProzent, that.betreuungspensumProzent) &&
			anspruchberechtigtesPensum == that.anspruchberechtigtesPensum &&
			MathUtil.isSame(betreuungspensumZeiteinheit, that.betreuungspensumZeiteinheit) &&
			MathUtil.isSame(vollkosten, that.vollkosten) &&
			MathUtil.isSame(elternbeitrag, that.elternbeitrag) &&
			MathUtil.isSame(abzugFamGroesse, that.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, that.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			babyTarif == that.babyTarif &&
			eingeschult == that.eingeschult &&
			besondereBeduerfnisseBestaetigt == that.besondereBeduerfnisseBestaetigt &&
			Objects.equals(einkommensjahr, that.einkommensjahr) &&
			minimalesEwpUnterschritten == that.minimalesEwpUnterschritten &&
			Objects.equals(bemerkungen, that.bemerkungen) &&
			Objects.equals(bemerkungenMap, that.bemerkungenMap);
	}

	private boolean isSameErwerbspensum(@Nullable Integer thisErwerbspensumGS, @Nullable Integer thatErwerbspensumGS) {
		return thisErwerbspensumGS == null && thatErwerbspensumGS == null
			|| !(thisErwerbspensumGS == null || thatErwerbspensumGS == null)
			&& thisErwerbspensumGS.equals(thatErwerbspensumGS);
	}

	private boolean isSameZeiteinheiten(@Nonnull VerfuegungZeitabschnitt other) {
		return MathUtil.isSame(verfuegteAnzahlZeiteinheiten, other.verfuegteAnzahlZeiteinheiten) &&
			MathUtil.isSame(anspruchsberechtigteAnzahlZeiteinheiten, other.anspruchsberechtigteAnzahlZeiteinheiten) &&
			zeiteinheit == other.zeiteinheit;
	}

	/**
	 * Aller persistierten Daten ohne Kommentar
	 */
	@SuppressWarnings({ "OverlyComplexBooleanExpression", "AccessingNonPublicFieldOfAnotherObject",
		"QuestionableName" })
	public boolean isSamePersistedValues(VerfuegungZeitabschnitt that) {
		// zuSpaetEingereicht und zahlungsstatus sind hier nicht aufgefuehrt, weil;
		// Es sollen die Resultate der Verfuegung verglichen werden und nicht der Weg, wie wir zu diesem Resultat
		// gelangt sind
		return MathUtil.isSame(betreuungspensumProzent, that.betreuungspensumProzent) &&
			anspruchberechtigtesPensum == that.anspruchberechtigtesPensum &&
			MathUtil.isSame(betreuungspensumZeiteinheit, that.betreuungspensumZeiteinheit) &&
			MathUtil.isSame(vollkosten, that.vollkosten) &&
			MathUtil.isSame(elternbeitrag, that.elternbeitrag) &&
			MathUtil.isSame(abzugFamGroesse, that.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, that.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			getGueltigkeit().compareTo(that.getGueltigkeit()) == 0 &&
			minimalesEwpUnterschritten == that.minimalesEwpUnterschritten &&
			Objects.equals(this.einkommensjahr, that.einkommensjahr) &&
			MathUtil.isSame(this.monatlicheBetreuungskosten, that.monatlicheBetreuungskosten) &&
			isSameZeiteinheiten(that);
	}

	/**
	 * Vergleich nur die relevanten Daten fuer die Berechnung einer Verfuegung.
	 */
	public boolean isSameBerechnung(VerfuegungZeitabschnitt that) {
		return MathUtil.isSame(getBgPensum(), that.getBgPensum()) &&
			anspruchberechtigtesPensum == that.anspruchberechtigtesPensum &&
			MathUtil.isSame(verguenstigung, that.verguenstigung) &&
			MathUtil.isSame(getMinimalerElternbeitragGekuerzt(), that.getMinimalerElternbeitragGekuerzt()) &&
			(getGueltigkeit().compareTo(that.getGueltigkeit()) == 0);
	}

	public boolean isCloseTo(@Nonnull VerfuegungZeitabschnitt that) {
		BigDecimal rapenError = BigDecimal.valueOf(0.20);
		// Folgende Attribute sollen bei einer "kleinen" Änderung nicht zu einer Neuberechnung führen:
		return MathUtil.isSame(vollkosten, that.vollkosten)
			&& MathUtil.isClose(getBgPensum(), that.getBgPensum(), BigDecimal.valueOf(0.01))
			&& MathUtil.isClose(elternbeitrag, that.getElternbeitrag(), rapenError)
			&& MathUtil.isClose(minimalerElternbeitrag, that.getMinimalerElternbeitrag(), rapenError)
			&& MathUtil.isClose(verguenstigungOhneBeruecksichtigungVollkosten, that.getVerguenstigungOhneBeruecksichtigungVollkosten(), rapenError)
			&& MathUtil.isClose(verguenstigungOhneBeruecksichtigungMinimalbeitrag, that.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag(), rapenError)
			&& MathUtil.isClose(verguenstigung, that.getVerguenstigung(), rapenError);
	}

	public void copyCalculationResult(@Nonnull VerfuegungZeitabschnitt that) {
		elternbeitrag = that.elternbeitrag;
		minimalerElternbeitrag = that.minimalerElternbeitrag;
		verguenstigungOhneBeruecksichtigungVollkosten = that.verguenstigungOhneBeruecksichtigungVollkosten;
		verguenstigungOhneBeruecksichtigungMinimalbeitrag = that.verguenstigungOhneBeruecksichtigungMinimalbeitrag;
		verguenstigung = that.verguenstigung;
	}

	@Override
	public int compareTo(@Nonnull VerfuegungZeitabschnitt other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getGueltigkeit(), other.getGueltigkeit());
		// wenn ids nicht gleich sind wollen wir auch compare to nicht gleich
		compareToBuilder.append(this.getId(), other.getId());
		return compareToBuilder.toComparison();
	}

	public BigDecimal getMonatlicheBetreuungskosten() {
		return monatlicheBetreuungskosten;
	}

	public void setMonatlicheBetreuungskosten(BigDecimal monatlicheBetreuungskosten) {
		this.monatlicheBetreuungskosten = monatlicheBetreuungskosten;
	}

	public BigDecimal getBetreuungspensumZeiteinheit() {
		return betreuungspensumZeiteinheit;
	}

	public void setBetreuungspensumZeiteinheit(BigDecimal betreuungspensumZeiteinheit) {
		this.betreuungspensumZeiteinheit = betreuungspensumZeiteinheit;
	}
}
