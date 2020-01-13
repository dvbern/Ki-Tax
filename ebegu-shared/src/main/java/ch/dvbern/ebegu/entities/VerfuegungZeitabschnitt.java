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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.VerfuegungsBemerkung;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
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

	@Transient
	private BGCalculationInput bgCalculationInput;

	public BGCalculationInput getBgCalculationInput() {
		return bgCalculationInput;
	}

	public void setBgCalculationInput(BGCalculationInput bgCalculationInput) {
		this.bgCalculationInput = bgCalculationInput;
	}

	// Zwischenresulate aus DATA-Rules ("Abschnitt")


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
		this.bgCalculationInput = new BGCalculationInput(toCopy.bgCalculationInput);
		this.zuSpaetEingereicht = toCopy.zuSpaetEingereicht;
		this.minimalesEwpUnterschritten = toCopy.minimalesEwpUnterschritten;
		this.betreuungspensumProzent = toCopy.betreuungspensumProzent;
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
		this.einkommensjahr = toCopy.einkommensjahr;
		this.bemerkungen = toCopy.bemerkungen;
		//noinspection ConstantConditions: Muss erst beim Speichern gesetzt sein
		this.verfuegung = null;
		this.zahlungsstatus = toCopy.zahlungsstatus;
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

	@Nonnull
	public BigDecimal getBetreuungspensumProzent() {
		return betreuungspensumProzent;
	}

	public void setBetreuungspensumProzent(@Nonnull BigDecimal betreuungspensumProzent) {
		this.betreuungspensumProzent = betreuungspensumProzent;
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

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
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

	/* START Delegation der Attribute auf dem Input-Objekt */

	@Deprecated
	@Nullable
	public Integer getErwerbspensumGS1() {
		return getBgCalculationInput().getErwerbspensumGS1();
	}

	@Deprecated
	@Nullable
	public Integer getErwerbspensumGS2() {
		return getBgCalculationInput().getErwerbspensumGS2();
	}

	@Deprecated
	public Set<Taetigkeit> getTaetigkeiten() {
		return getBgCalculationInput().getTaetigkeiten();
	}

	@Deprecated
	public int getFachstellenpensum() {
		return getBgCalculationInput().getFachstellenpensum();
	}

	@Deprecated
	public int getAusserordentlicherAnspruch() {
		return getBgCalculationInput().getAusserordentlicherAnspruch();
	}

	@Deprecated
	public int getAnspruchspensumRest() {
		return getBgCalculationInput().getAnspruchspensumRest();
	}

	@Deprecated
	public boolean isHasSecondGesuchstellerForFinanzielleSituation() {
		return getBgCalculationInput().isHasSecondGesuchstellerForFinanzielleSituation();
	}

	@Deprecated
	public Map<MsgKey, VerfuegungsBemerkung> getBemerkungenMap() {
		return getBgCalculationInput().getBemerkungenMap();
	}

	@Deprecated
	public boolean isBezahltVollkosten() {
		return getBgCalculationInput().isBezahltVollkosten();
	}

	@Deprecated
	public boolean isLongAbwesenheit() {
		return getBgCalculationInput().isLongAbwesenheit();
	}

	@Deprecated
	public boolean isWohnsitzNichtInGemeindeGS1() {
		return (getBgCalculationInput().isWohnsitzNichtInGemeindeGS1() != null) ? getBgCalculationInput().isWohnsitzNichtInGemeindeGS1() : true;
	}

	@Deprecated
	public boolean isEkv1Alleine() {
		return getBgCalculationInput().isEkv1Alleine();
	}

	@Deprecated
	public boolean isEkv1ZuZweit() {
		return getBgCalculationInput().isEkv1ZuZweit();
	}

	@Deprecated
	public boolean isEkv2Alleine() {
		return getBgCalculationInput().isEkv2Alleine();
	}

	@Deprecated
	public boolean isEkv2ZuZweit() {
		return getBgCalculationInput().isEkv2ZuZweit();
	}

	@Deprecated
	public boolean isKategorieMaxEinkommen() {
		return getBgCalculationInput().isKategorieMaxEinkommen();
	}

	@Deprecated
	public boolean isKategorieKeinPensum() {
		return getBgCalculationInput().isKategorieKeinPensum();
	}

	@Deprecated
	public boolean isSameVerfuegteVerfuegungsrelevanteDaten() {
		return getBgCalculationInput().isSameVerfuegteVerfuegungsrelevanteDaten();
	}

	@Deprecated
	public boolean isSameAusbezahlteVerguenstigung() {
		return getBgCalculationInput().isSameAusbezahlteVerguenstigung();
	}

	@Deprecated
	public boolean isAbschnittLiegtNachBEGUStartdatum() {
		return getBgCalculationInput().isAbschnittLiegtNachBEGUStartdatum();
	}

	@Deprecated
	public BigDecimal getMonatlicheBetreuungskosten() {
		return getBgCalculationInput().getMonatlicheBetreuungskosten();
	}

	/* ENDE Delegation der Attribute auf dem Input-Objekt */

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
		this.bgCalculationInput.add(other.bgCalculationInput);
		this.setBetreuungspensumProzent(this.getBetreuungspensumProzent().add(other.getBetreuungspensumProzent()));

		this.setAnspruchberechtigtesPensum(this.getAnspruchberechtigtesPensum()
			+ other.getAnspruchberechtigtesPensum());

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

		this.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.addNullSafe(
			this.getMassgebendesEinkommenVorAbzFamgr(),
			other.getMassgebendesEinkommenVorAbzFamgr()));

		this.setZuSpaetEingereicht(this.isZuSpaetEingereicht() || other.isZuSpaetEingereicht());

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

		this.setBabyTarif(this.babyTarif || other.babyTarif);
		this.setEingeschult(this.eingeschult || other.eingeschult);
		this.setBesondereBeduerfnisseBestaetigt(this.besondereBeduerfnisseBestaetigt
			|| other.besondereBeduerfnisseBestaetigt);
		this.setMinimalesEwpUnterschritten(this.minimalesEwpUnterschritten || other.minimalesEwpUnterschritten);
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
			+ " EP GS1: " + bgCalculationInput.getErwerbspensumGS1() + '\t'
			+ " EP GS2: " + bgCalculationInput.getErwerbspensumGS2() + '\t'
			+ " BetrPensum: " + betreuungspensumProzent + '\t'
			+ " Anspruch: " + anspruchberechtigtesPensum + '\t'
			+ " Restanspruch: " + bgCalculationInput.getAnspruchspensumRest() + '\t'
			+ " BG-Pensum: " + getBgPensum() + '\t'
			+ " Vollkosten: " + vollkosten + '\t'
			+ " Elternbeitrag: " + elternbeitrag + '\t'
			+ " Bemerkungen: " + bemerkungen + '\t'
			+ " Einkommensjahr: " + einkommensjahr + '\t'
			+ " Einkommen: " + massgebendesEinkommenVorAbzugFamgr + '\t'
			+ " Abzug Fam: " + abzugFamGroesse;
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
		return
			bgCalculationInput.isSame(((VerfuegungZeitabschnitt) other).getBgCalculationInput()) &&
			MathUtil.isSame(betreuungspensumProzent, otherVerfuegungZeitabschnitt.betreuungspensumProzent) &&
			anspruchberechtigtesPensum == otherVerfuegungZeitabschnitt.anspruchberechtigtesPensum &&
			MathUtil.isSame(abzugFamGroesse, otherVerfuegungZeitabschnitt.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, otherVerfuegungZeitabschnitt.famGroesse) &&
			MathUtil.isSame(
				massgebendesEinkommenVorAbzugFamgr,
				otherVerfuegungZeitabschnitt.massgebendesEinkommenVorAbzugFamgr) &&
			zuSpaetEingereicht == otherVerfuegungZeitabschnitt.zuSpaetEingereicht &&
			minimalesEwpUnterschritten == otherVerfuegungZeitabschnitt.minimalesEwpUnterschritten &&
			Objects.equals(einkommensjahr, otherVerfuegungZeitabschnitt.einkommensjahr) &&
			babyTarif == otherVerfuegungZeitabschnitt.babyTarif &&
			eingeschult == otherVerfuegungZeitabschnitt.eingeschult &&
			besondereBeduerfnisseBestaetigt == otherVerfuegungZeitabschnitt.besondereBeduerfnisseBestaetigt &&
			zahlungsstatus == otherVerfuegungZeitabschnitt.zahlungsstatus &&
			Objects.equals(bemerkungen, otherVerfuegungZeitabschnitt.bemerkungen) &&
			isSameZeiteinheiten(otherVerfuegungZeitabschnitt);
	}

	public boolean isSameSichtbareDaten(VerfuegungZeitabschnitt that) {
		//noinspection ObjectEquality,SimplifiableIfStatement
		if (this == that) {
			return true;
		}
		return
			bgCalculationInput.isSameSichtbareDaten(that.getBgCalculationInput()) &&
			MathUtil.isSame(betreuungspensumProzent, that.betreuungspensumProzent) &&
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
			Objects.equals(bemerkungen, that.bemerkungen);
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
		return
			bgCalculationInput.isSamePersistedValues(that.getBgCalculationInput()) &&
			MathUtil.isSame(betreuungspensumProzent, that.betreuungspensumProzent) &&
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

	public BigDecimal getBetreuungspensumZeiteinheit() {
		return betreuungspensumZeiteinheit;
	}

	public void setBetreuungspensumZeiteinheit(BigDecimal betreuungspensumZeiteinheit) {
		this.betreuungspensumZeiteinheit = betreuungspensumZeiteinheit;
	}
}
