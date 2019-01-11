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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

import static java.math.BigDecimal.ZERO;

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
	private boolean sameVerfuegungsdaten;

	// Dieser Wert wird gebraucht, um zu wissen ob die Korrektur relevant fuer die Zahlungen ist, da nur wenn die
	// Verguenstigung sich geaendert hat, muss man die Korrektur beruecksichtigen
	@Transient
	private boolean sameVerguenstigung;

	@Transient
	@Nullable
	private Integer erwerbspensumGS1 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	@Nullable
	private Integer erwerbspensumGS2 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	private int fachstellenpensum;

	@Transient
	private int ausserordentlicherAnspruch;

	@Transient
	private Boolean wohnsitzNichtInGemeindeGS1 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	private Boolean wohnsitzNichtInGemeindeGS2 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	// Wenn Vollkosten bezahlt werden muessen, werden die Vollkosten berechnet und als Elternbeitrag gesetzt
	private boolean bezahltVollkosten;

	@Transient
	private boolean longAbwesenheit;

	@Transient
	private int anspruchspensumRest;

	@Transient
	// Achtung, dieses Flag wird erst ab 1. des Folgemonats gesetzt, weil die Finanzielle Situation ab dann gilt. Für Erwerbspensen zählt der GS2 ab sofort!
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
	private boolean ekv1NotExisting;

	@Transient
	private boolean kategorieMaxEinkommen = false;

	@Transient
	private boolean kategorieKeinPensum = false;

	@Transient
	private boolean abschnittLiegtNachBEGUStartdatum = true;

	@Transient
	private BigDecimal monatlicheBetreuungskosten = BigDecimal.ZERO;

	@Min(0)
	@NotNull
	@Column(nullable = false)
	private BigDecimal betreuungspensum = BigDecimal.ZERO;

	@Max(100)
	@Min(0)
	@NotNull
	@Column(nullable = false)
	private int anspruchberechtigtesPensum; // = Anpsruch für diese Kita, bzw. Tageseltern Kleinkinder

	@Column(nullable = true)
	private BigDecimal betreuungsstunden;

	@Column(nullable = true)
	private BigDecimal vollkosten = ZERO;

	@Column(nullable = true)
	private BigDecimal elternbeitrag = ZERO;

	@Column(nullable = true)
	private BigDecimal abzugFamGroesse = null;

	@Column(nullable = true)
	private BigDecimal famGroesse = null;

	@Column(nullable = true)
	@Nonnull
	private BigDecimal massgebendesEinkommenVorAbzugFamgr = ZERO;

	@NotNull
	@Column(nullable = false)
	private Integer einkommensjahr;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungen = "";

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegung_zeitabschnitt_verfuegung_id"), nullable = false)
	private Verfuegung verfuegung;

	@NotNull
	@Column(nullable = false)
	private boolean zuSpaetEingereicht;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus = VerfuegungsZeitabschnittZahlungsstatus.NEU;

	@NotNull
	@OneToMany(mappedBy = "verfuegungZeitabschnitt")
	private List<Zahlungsposition> zahlungsposition = new ArrayList<>();

	@Transient
	private boolean babyTarif;

	@Transient
	private boolean eingeschult;

	@Transient
	private boolean besondereBeduerfnisse;


	public VerfuegungZeitabschnitt() {
	}

	/**
	 * copy Konstruktor
	 */
	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
	public VerfuegungZeitabschnitt(VerfuegungZeitabschnitt toCopy) {
		this.setGueltigkeit(new DateRange(toCopy.getGueltigkeit()));
		this.erwerbspensumGS1 = toCopy.erwerbspensumGS1;
		this.erwerbspensumGS2 = toCopy.erwerbspensumGS2;
		this.fachstellenpensum = toCopy.fachstellenpensum;
		this.ausserordentlicherAnspruch = toCopy.ausserordentlicherAnspruch;
		this.zuSpaetEingereicht = toCopy.zuSpaetEingereicht;
		this.wohnsitzNichtInGemeindeGS1 = toCopy.wohnsitzNichtInGemeindeGS1;
		this.wohnsitzNichtInGemeindeGS2 = toCopy.wohnsitzNichtInGemeindeGS2;
		this.bezahltVollkosten = toCopy.bezahltVollkosten;
		this.longAbwesenheit = toCopy.isLongAbwesenheit();
		this.anspruchspensumRest = toCopy.anspruchspensumRest;
		this.betreuungspensum = toCopy.betreuungspensum;
		this.monatlicheBetreuungskosten = toCopy.monatlicheBetreuungskosten;
		this.anspruchberechtigtesPensum = toCopy.anspruchberechtigtesPensum;
		this.betreuungsstunden = toCopy.betreuungsstunden;
		this.vollkosten = toCopy.vollkosten;
		this.elternbeitrag = toCopy.elternbeitrag;
		this.abzugFamGroesse = toCopy.abzugFamGroesse;
		this.famGroesse = toCopy.famGroesse;
		this.massgebendesEinkommenVorAbzugFamgr = toCopy.massgebendesEinkommenVorAbzugFamgr;
		this.hasSecondGesuchstellerForFinanzielleSituation = toCopy.hasSecondGesuchstellerForFinanzielleSituation;
		this.einkommensjahr = toCopy.einkommensjahr;
		this.ekv1Alleine = toCopy.ekv1Alleine;
		this.ekv1ZuZweit = toCopy.ekv1ZuZweit;
		this.ekv2Alleine = toCopy.ekv2Alleine;
		this.ekv2ZuZweit = toCopy.ekv2ZuZweit;
		this.ekv1NotExisting = toCopy.ekv1NotExisting;
		this.bemerkungen = toCopy.bemerkungen;
		this.verfuegung = null;
		this.kategorieMaxEinkommen = toCopy.kategorieMaxEinkommen;
		this.kategorieKeinPensum = toCopy.kategorieKeinPensum;
		this.zahlungsstatus = toCopy.zahlungsstatus;
		this.abschnittLiegtNachBEGUStartdatum = toCopy.abschnittLiegtNachBEGUStartdatum;
		this.babyTarif = toCopy.babyTarif;
		this.eingeschult = toCopy.eingeschult;
		this.besondereBeduerfnisse = toCopy.besondereBeduerfnisse;
	}

	/**
	 * Erstellt einen Zeitabschnitt mit der gegebenen gueltigkeitsdauer
	 */
	public VerfuegungZeitabschnitt(DateRange gueltigkeit) {
		this.setGueltigkeit(new DateRange(gueltigkeit));
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public void setVorgaengerId(String vorgaengerId) {
		// nop -> Diese Methode darf eingentlich nicht verwendet werden, da ein VerfuegungZeitabschnitt keinen Vorgaenger hat
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public String getVorgaengerId() {
		return null; // Diese Methode darf eingentlich nicht verwendet werden, da ein VerfuegungZeitabschnitt keinen Vorgaenger hat
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

	public BigDecimal getBetreuungspensum() {
		return betreuungspensum;
	}

	public void setBetreuungspensum(BigDecimal betreuungspensum) {
		this.betreuungspensum = betreuungspensum;
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

	public BigDecimal getBetreuungsstunden() {
		return betreuungsstunden;
	}

	public void setBetreuungsstunden(BigDecimal betreuungsstunden) {
		this.betreuungsstunden = betreuungsstunden;
	}

	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
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

	/**
	 * @return berechneter Wert. Zieht vom massgebenenEinkommenVorAbzug den Familiengroessen Abzug ab
	 */
	@Nonnull
	public BigDecimal getMassgebendesEinkommen() {
		BigDecimal abzugFamSize = this.abzugFamGroesse == null ? BigDecimal.ZERO : this.abzugFamGroesse;
		return MathUtil.GANZZAHL.subtractNullSafe(this.massgebendesEinkommenVorAbzugFamgr, abzugFamSize);
	}

	@Nonnull
	public BigDecimal getMassgebendesEinkommenVorAbzFamgr() {
		return massgebendesEinkommenVorAbzugFamgr;
	}

	public void setMassgebendesEinkommenVorAbzugFamgr(@Nonnull BigDecimal massgebendesEinkommenVorAbzugFamgr) {
		this.massgebendesEinkommenVorAbzugFamgr = massgebendesEinkommenVorAbzugFamgr;
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

	public Verfuegung getVerfuegung() {
		return verfuegung;
	}

	public void setVerfuegung(Verfuegung verfuegung) {
		this.verfuegung = verfuegung;
	}

	public boolean isZuSpaetEingereicht() {
		return zuSpaetEingereicht;
	}

	public void setZuSpaetEingereicht(boolean zuSpaetEingereicht) {
		this.zuSpaetEingereicht = zuSpaetEingereicht;
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

	public boolean isWohnsitzNichtInGemeindeGS2() {
		return wohnsitzNichtInGemeindeGS2 != null ? wohnsitzNichtInGemeindeGS2 : true;
	}

	public void setWohnsitzNichtInGemeindeGS2(Boolean wohnsitzNichtInGemeindeGS2) {
		this.wohnsitzNichtInGemeindeGS2 = wohnsitzNichtInGemeindeGS2;
	}

	public BigDecimal getFamGroesse() {
		return famGroesse;
	}

	public void setFamGroesse(BigDecimal famGroesse) {
		this.famGroesse = famGroesse;
	}

	public Integer getEinkommensjahr() {
		return einkommensjahr;
	}

	public void setEinkommensjahr(Integer einkommensjahr) {
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

	public boolean isEkv1NotExisting() {
		return ekv1NotExisting;
	}

	public void setEkv1NotExisting(boolean ekv1NotExisting) {
		this.ekv1NotExisting = ekv1NotExisting;
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

	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatus() {
		return zahlungsstatus;
	}

	public void setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus) {
		this.zahlungsstatus = zahlungsstatus;
	}

	public List<Zahlungsposition> getZahlungsposition() {
		return zahlungsposition;
	}

	public void setZahlungsposition(List<Zahlungsposition> zahlungsposition) {
		this.zahlungsposition = zahlungsposition;
	}

	public boolean isSameVerfuegungsdaten() {
		return sameVerfuegungsdaten;
	}

	public void setSameVerfuegungsdaten(boolean sameVerfuegungsdaten) {
		this.sameVerfuegungsdaten = sameVerfuegungsdaten;
	}

	public boolean isSameVerguenstigung() {
		return sameVerguenstigung;
	}

	public void setSameVerguenstigung(boolean sameVerguenstigung) {
		this.sameVerguenstigung = sameVerguenstigung;
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

	public boolean isBesondereBeduerfnisse() {
		return besondereBeduerfnisse;
	}

	public void setBesondereBeduerfnisse(boolean besondereBeduerfnisse) {
		this.besondereBeduerfnisse = besondereBeduerfnisse;
	}

	/**
	 * Addiert die Daten von "other" zu diesem VerfuegungsZeitabschnitt
	 */
	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "PMD.NcssMethodCount"})
	public void add(VerfuegungZeitabschnitt other) {
		this.setBetreuungspensum(this.getBetreuungspensum().add(other.getBetreuungspensum()));
		this.setFachstellenpensum(this.getFachstellenpensum() + other.getFachstellenpensum());
		this.setAusserordentlicherAnspruch(this.getAusserordentlicherAnspruch() + other.getAusserordentlicherAnspruch());
		this.setAnspruchspensumRest(this.getAnspruchspensumRest() + other.getAnspruchspensumRest());
		this.setAnspruchberechtigtesPensum(this.getAnspruchberechtigtesPensum() + other.getAnspruchberechtigtesPensum());

		BigDecimal newMonatlicheBetreuungskosten = BigDecimal.ZERO;
		if (this.getMonatlicheBetreuungskosten() != null) {
			newMonatlicheBetreuungskosten = newMonatlicheBetreuungskosten.add(this.getMonatlicheBetreuungskosten());
		}
		if (other.getMonatlicheBetreuungskosten() != null) {
			newMonatlicheBetreuungskosten = newMonatlicheBetreuungskosten.add(other.getMonatlicheBetreuungskosten());
		}
		this.setMonatlicheBetreuungskosten(newMonatlicheBetreuungskosten);


		BigDecimal newBetreuungsstunden = ZERO;
		if (this.getBetreuungsstunden() != null) {
			newBetreuungsstunden = newBetreuungsstunden.add(this.getBetreuungsstunden());
		}
		if (other.getBetreuungsstunden() != null) {
			newBetreuungsstunden = newBetreuungsstunden.add(other.getBetreuungsstunden());
		}
		this.setBetreuungsstunden(newBetreuungsstunden);

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

		this.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.addNullSafe(this.getMassgebendesEinkommenVorAbzFamgr(), other.getMassgebendesEinkommenVorAbzFamgr()));

		this.addBemerkung(other.getBemerkungen());
		this.setZuSpaetEingereicht(this.isZuSpaetEingereicht() || other.isZuSpaetEingereicht());

		this.setWohnsitzNichtInGemeindeGS1(this.isWohnsitzNichtInGemeindeGS1() && other.isWohnsitzNichtInGemeindeGS1());
		this.setWohnsitzNichtInGemeindeGS2(this.isWohnsitzNichtInGemeindeGS2() && other.isWohnsitzNichtInGemeindeGS2());

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
		if (other.getEinkommensjahr() != null) {
			this.setEinkommensjahr(other.getEinkommensjahr());
		}
		this.setHasSecondGesuchstellerForFinanzielleSituation(this.isHasSecondGesuchstellerForFinanzielleSituation() || other.isHasSecondGesuchstellerForFinanzielleSituation());

		this.ekv1Alleine = (this.ekv1Alleine || other.ekv1Alleine);
		this.ekv1ZuZweit = (this.ekv1ZuZweit || other.ekv1ZuZweit);
		this.ekv2Alleine = (this.ekv2Alleine || other.ekv2Alleine);
		this.ekv2ZuZweit = (this.ekv2ZuZweit || other.ekv2ZuZweit);
		this.ekv1NotExisting = (this.ekv1NotExisting || other.ekv1NotExisting);

		this.setKategorieKeinPensum(this.kategorieKeinPensum || other.kategorieKeinPensum);
		this.setKategorieMaxEinkommen(this.kategorieMaxEinkommen || other.kategorieMaxEinkommen);
		this.setAbschnittLiegtNachBEGUStartdatum(this.abschnittLiegtNachBEGUStartdatum && other.abschnittLiegtNachBEGUStartdatum);

		this.setBabyTarif(this.babyTarif || other.babyTarif);
		this.setEingeschult(this.eingeschult || other.eingeschult);
		this.setBesondereBeduerfnisse(this.besondereBeduerfnisse || other.besondereBeduerfnisse);
	}

	public void addBemerkung(VerfuegungsBemerkung bemerkungContainer, @Nonnull Locale locale) {
		this.addBemerkung(bemerkungContainer.getRuleKey(), bemerkungContainer.getMsgKey(), locale);
	}

	public void addBemerkung(RuleKey ruleKey, MsgKey msgKey, @Nonnull Locale locale) {
		String bemerkungsText = ServerMessageUtil.translateEnumValue(msgKey, locale);
		this.addBemerkung(ruleKey.name() + ": " + bemerkungsText);

	}

	public void addBemerkung(RuleKey ruleKey, MsgKey msgKey, @Nonnull Locale locale, Object... args) {
		String bemerkungsText = ServerMessageUtil.translateEnumValue(msgKey, locale, args);
		this.addBemerkung(ruleKey.name() + ": " + bemerkungsText);
	}

	/**
	 * Fügt eine Bemerkung zur Liste hinzu
	 */
	public void addBemerkung(@Nullable String bem) {
		this.bemerkungen = Joiner.on("\n").skipNulls().join(
			StringUtils.defaultIfBlank(this.bemerkungen, null),
			StringUtils.defaultIfBlank(bem, null)
		);
	}

	/**
	 * Fügt mehrere Bemerkungen zur Liste hinzu
	 */
	public void addAllBemerkungen(@Nonnull List<String> bemerkungenList) {
		List<String> listOfStrings = new ArrayList<>();
		listOfStrings.add(this.bemerkungen);
		listOfStrings.addAll(bemerkungenList);
		this.bemerkungen = String.join(";", listOfStrings);
	}

	/**
	 * Fügt otherBemerkungen zur Liste hinzu, falls sie noch nicht vorhanden sind
	 */
	public void mergeBemerkungen(String otherBemerkungen) {
		String[] otherBemerkungenList = StringUtils.split(otherBemerkungen, "\n");
		for (String otherBemerkung : otherBemerkungenList) {
			if (!StringUtils.contains(getBemerkungen(), otherBemerkung)) {
				addBemerkung(otherBemerkung);
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

	/**
	 * Das BG-Pensum (Pensum des Gutscheins) wird zum BG-Tarif berechnet und kann höchstens so gross sein, wie das Betreuungspensum.
	 * Falls das anspruchsberechtigte Pensum unter dem Betreuungspensum liegt, entspricht das BG-Pensum dem
	 * anspruchsberechtigten Pensum.
	 * <p>
	 * Ein Kind mit einem Betreuungspensum von 60% und einem anspruchsberechtigten Pensum von 40% hat ein BG-Pensum von 40%.
	 * Ein Kind mit einem Betreuungspensum von 40% und einem anspruchsberechtigten Pensum von 60% hat ein BG-Pensum von 40%.
	 */
	@Transient
	public BigDecimal getBgPensum() {
		return getBetreuungspensum().min(MathUtil.DEFAULT.from(getAnspruchberechtigtesPensum()));
	}

	@Override
	public String toString() {
		String sb = '[' + Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb()) + " - " + Constants.DATE_FORMATTER.format(getGueltigkeit()
			.getGueltigBis()) + "] "
			+ " Status: " + zahlungsstatus + '\t'
			+ " EP GS1: " + erwerbspensumGS1 + '\t'
			+ " EP GS2: " + erwerbspensumGS2 + '\t'
			+ " BetrPensum: " + betreuungspensum + '\t'
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
		String sb = '[' + Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb()) + " - " + Constants.DATE_FORMATTER.format(getGueltigkeit()
			.getGueltigBis()) + "] "
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
			MathUtil.isSame(betreuungspensum, otherVerfuegungZeitabschnitt.betreuungspensum) &&
			fachstellenpensum == otherVerfuegungZeitabschnitt.fachstellenpensum &&
			ausserordentlicherAnspruch == otherVerfuegungZeitabschnitt.ausserordentlicherAnspruch &&
			anspruchspensumRest == otherVerfuegungZeitabschnitt.anspruchspensumRest &&
			anspruchberechtigtesPensum == otherVerfuegungZeitabschnitt.anspruchberechtigtesPensum &&
			hasSecondGesuchstellerForFinanzielleSituation == otherVerfuegungZeitabschnitt.hasSecondGesuchstellerForFinanzielleSituation &&
			Objects.equals(abzugFamGroesse, otherVerfuegungZeitabschnitt.abzugFamGroesse) &&
			Objects.equals(famGroesse, otherVerfuegungZeitabschnitt.famGroesse) &&
			Objects.equals(massgebendesEinkommenVorAbzugFamgr, otherVerfuegungZeitabschnitt.massgebendesEinkommenVorAbzugFamgr) &&
			(isWohnsitzNichtInGemeindeGS1() && isWohnsitzNichtInGemeindeGS2()) == (otherVerfuegungZeitabschnitt.isWohnsitzNichtInGemeindeGS1() && otherVerfuegungZeitabschnitt.isWohnsitzNichtInGemeindeGS2()) &&
			zuSpaetEingereicht == otherVerfuegungZeitabschnitt.zuSpaetEingereicht &&
			bezahltVollkosten == otherVerfuegungZeitabschnitt.bezahltVollkosten &&
			longAbwesenheit == otherVerfuegungZeitabschnitt.longAbwesenheit &&
			Objects.equals(einkommensjahr, otherVerfuegungZeitabschnitt.einkommensjahr) &&
			ekv1Alleine == otherVerfuegungZeitabschnitt.ekv1Alleine &&
			ekv1ZuZweit == otherVerfuegungZeitabschnitt.ekv1ZuZweit &&
			ekv2Alleine == otherVerfuegungZeitabschnitt.ekv2Alleine &&
			ekv2ZuZweit == otherVerfuegungZeitabschnitt.ekv2ZuZweit &&
			ekv1NotExisting == otherVerfuegungZeitabschnitt.ekv1NotExisting &&
			abschnittLiegtNachBEGUStartdatum == otherVerfuegungZeitabschnitt.abschnittLiegtNachBEGUStartdatum &&
			babyTarif == otherVerfuegungZeitabschnitt.babyTarif &&
			eingeschult == otherVerfuegungZeitabschnitt.eingeschult &&
			besondereBeduerfnisse == otherVerfuegungZeitabschnitt.besondereBeduerfnisse &&
			zahlungsstatus == otherVerfuegungZeitabschnitt.zahlungsstatus &&
			Objects.equals(this.bemerkungen, otherVerfuegungZeitabschnitt.bemerkungen);
	}

	public boolean isSameSichtbareDaten(VerfuegungZeitabschnitt that) {
		//noinspection ObjectEquality,SimplifiableIfStatement
		if (this == that) {
			return true;
		}

		return MathUtil.isSame(betreuungspensum, that.betreuungspensum) &&
			anspruchberechtigtesPensum == that.anspruchberechtigtesPensum &&
			MathUtil.isSame(betreuungsstunden, that.betreuungsstunden) &&
			MathUtil.isSame(vollkosten, that.vollkosten) &&
			MathUtil.isSame(elternbeitrag, that.elternbeitrag) &&
			MathUtil.isSame(abzugFamGroesse, that.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, that.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			babyTarif == that.babyTarif &&
			eingeschult == that.eingeschult &&
			besondereBeduerfnisse == that.besondereBeduerfnisse &&
			Objects.equals(this.einkommensjahr, that.einkommensjahr) &&
			Objects.equals(this.bemerkungen, that.bemerkungen);
	}

	private boolean isSameErwerbspensum(@Nullable Integer thisErwerbspensumGS, @Nullable Integer thatErwerbspensumGS) {
		return thisErwerbspensumGS == null && thatErwerbspensumGS == null
			|| !(thisErwerbspensumGS == null || thatErwerbspensumGS == null)
			&& thisErwerbspensumGS.equals(thatErwerbspensumGS);
	}

	/**
	 * Aller persistierten Daten ohne Kommentar
	 */
	@SuppressWarnings({ "OverlyComplexBooleanExpression", "AccessingNonPublicFieldOfAnotherObject", "QuestionableName" })
	public boolean isSamePersistedValues(VerfuegungZeitabschnitt that) {
		// zuSpaetEingereicht und zahlungsstatus sind hier nicht aufgefuehrt, weil;
		// Es sollen die Resultate der Verfuegung verglichen werden und nicht der Weg, wie wir zu diesem Resultat gelangt sind
		return MathUtil.isSame(betreuungspensum, that.betreuungspensum) &&
			anspruchberechtigtesPensum == that.anspruchberechtigtesPensum &&
			MathUtil.isSame(betreuungsstunden, that.betreuungsstunden) &&
			MathUtil.isSame(vollkosten, that.vollkosten) &&
			MathUtil.isSame(elternbeitrag, that.elternbeitrag) &&
			MathUtil.isSame(abzugFamGroesse, that.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, that.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			getGueltigkeit().compareTo(that.getGueltigkeit()) == 0 &&
			Objects.equals(this.einkommensjahr, that.einkommensjahr);
	}

	/**
	 * Vergleich nur die relevanten Daten fuer die Berechnung einer Verfuegung.
	 */
	public boolean isSameBerechnung(VerfuegungZeitabschnitt that) {
		return MathUtil.isSame(betreuungspensum, that.betreuungspensum) &&
			anspruchberechtigtesPensum == that.anspruchberechtigtesPensum &&
			MathUtil.isSame(betreuungsstunden, that.betreuungsstunden) &&
			MathUtil.isSame(vollkosten, that.vollkosten) &&
			MathUtil.isSame(elternbeitrag, that.elternbeitrag) &&
			(getGueltigkeit().compareTo(that.getGueltigkeit()) == 0);
	}

	/**
	 * Gibt den Betrag des Gutscheins zurück.
	 */
	@Nonnull
	public BigDecimal getVerguenstigung() {
		if (vollkosten != null && elternbeitrag != null) {
			return vollkosten.subtract(elternbeitrag);
		}
		return ZERO;
	}

	@Override
	public int compareTo(@Nonnull VerfuegungZeitabschnitt other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getGueltigkeit(), other.getGueltigkeit());
		compareToBuilder.append(this.getId(), other.getId());  // wenn ids nicht gleich sind wollen wir auch compare to nicht gleich
		return compareToBuilder.toComparison();
	}

	public BigDecimal getMonatlicheBetreuungskosten() {
		return monatlicheBetreuungskosten;
	}

	public void setMonatlicheBetreuungskosten(BigDecimal monatlicheBetreuungskosten) {
		this.monatlicheBetreuungskosten = monatlicheBetreuungskosten;
	}
}
