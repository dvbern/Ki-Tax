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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.VerfuegungsBemerkung;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Dieses Objekt repraesentiert einen Zeitabschnitt wahrend eines Betreeungsgutscheinantrags waehrend dem die Faktoren
 * die fuer die Berechnung des Gutscheins der Betreuung relevant sind konstant geblieben sind.
 */
@Entity
@Audited
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = "bg_calculation_result_asiv_id", name = "UK_verfuegung_zeitabschnitt_result_asiv")
)
public class VerfuegungZeitabschnitt extends AbstractDateRangedEntity implements Comparable<VerfuegungZeitabschnitt> {

	private static final long serialVersionUID = 7250339356897563374L;

	@Transient
	private boolean hasGemeindeSpezifischeBerechnung = false;

	/**
	 * Input-Werte für die Rules. Berechnung nach ASIV (Standard)
	 */
	@Transient
	@Nonnull
	private BGCalculationInput bgCalculationInputAsiv = new BGCalculationInput(this);

	/**
	 * Input-Werte für die Rules. Berechnung nach Spezialwünschen der Gemeinde, optional
	 */
	@Transient
	@Nonnull
	private BGCalculationInput bgCalculationInputGemeinde = new BGCalculationInput(this);

	/**
	 * Berechnungsresultate. Berechnung nach ASIV (Standard)
	 */
	@Valid
	@Nonnull @NotNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegungZeitabschnitt_resultatAsiv"), nullable = true)
	private BGCalculationResult bgCalculationResultAsiv = new BGCalculationResult();

	/**
	 * Berechnungsresultate. Berechnung nach Spezialwünschen der Gemeinde, optional
	 */
	@Valid
	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegungZeitabschnitt_resultatGemeinde"), nullable = true)
	private BGCalculationResult bgCalculationResultGemeinde;

	@NotNull
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegung_zeitabschnitt_verfuegung_id"), nullable = false)
	@ManyToOne(optional = false)
	private Verfuegung verfuegung;

	@NotNull @Nonnull
	@OneToMany(mappedBy = "verfuegungZeitabschnitt")
	private List<Zahlungsposition> zahlungsposition = new ArrayList<>();

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus = VerfuegungsZeitabschnittZahlungsstatus.NEU;

	// Die Bemerkungen werden vorerst in eine Map geschrieben, damit einzelne
	// Bemerkungen spaeter wieder zugreifbar sind. Am Ende des RuleSets werden sie ins persistente Feld
	// "bemerkungen" geschrieben
	@Transient
	private final Map<MsgKey, VerfuegungsBemerkung> bemerkungenMap = new TreeMap<>();

	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private @Size(max = Constants.DB_TEXTAREA_LENGTH) String bemerkungen = "";

	public VerfuegungZeitabschnitt() {
	}

	/**
	 * copy Konstruktor
	 */
	@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "PMD.ConstructorCallsOverridableMethod" })
	public VerfuegungZeitabschnitt(VerfuegungZeitabschnitt toCopy) {
		this.setGueltigkeit(new DateRange(toCopy.getGueltigkeit()));
		this.hasGemeindeSpezifischeBerechnung = toCopy.hasGemeindeSpezifischeBerechnung;
		this.bgCalculationInputAsiv = new BGCalculationInput(toCopy.bgCalculationInputAsiv);
		this.bgCalculationInputGemeinde = new BGCalculationInput(toCopy.bgCalculationInputGemeinde);
		this.bgCalculationResultAsiv = new BGCalculationResult(toCopy.getBgCalculationResultAsiv());
		if (this.hasGemeindeSpezifischeBerechnung && toCopy.getBgCalculationResultGemeinde() != null) {
			this.bgCalculationResultGemeinde = new BGCalculationResult(toCopy.getBgCalculationResultGemeinde());
		}
		//noinspection ConstantConditions: Muss erst beim Speichern gesetzt sein
		this.verfuegung = null;
		this.mergeBemerkungenMap(toCopy.getBemerkungenMap());
		this.bemerkungen = toCopy.bemerkungen;
		this.zahlungsstatus = toCopy.zahlungsstatus;
	}

	/**
	 * Erstellt einen Zeitabschnitt mit der gegebenen gueltigkeitsdauer
	 */
	public VerfuegungZeitabschnitt(DateRange gueltigkeit) {
		this.setGueltigkeit(new DateRange(gueltigkeit));
	}

	public boolean isHasGemeindeSpezifischeBerechnung() {
		return hasGemeindeSpezifischeBerechnung;
	}

	public void setHasGemeindeSpezifischeBerechnung(boolean hasGemeindeSpezifischeBerechnung) {
		this.hasGemeindeSpezifischeBerechnung = hasGemeindeSpezifischeBerechnung;
	}

	@Nonnull
	public BGCalculationInput getBgCalculationInputAsiv() {
		return bgCalculationInputAsiv;
	}

	@Nonnull
	public BGCalculationInput getBgCalculationInputGemeinde() {
		return bgCalculationInputGemeinde;
	}

	@Nonnull
	public BGCalculationResult getBgCalculationResultAsiv() {
		return bgCalculationResultAsiv;
	}

	public void setBgCalculationResultAsiv(@Nonnull BGCalculationResult bgCalculationResultAsiv) {
		this.bgCalculationResultAsiv = bgCalculationResultAsiv;
	}

	@Nullable
	public BGCalculationResult getBgCalculationResultGemeinde() {
		return bgCalculationResultGemeinde;
	}

	public void setBgCalculationResultGemeinde(@Nullable BGCalculationResult bgCalculationResultGemeinde) {
		this.bgCalculationResultGemeinde = bgCalculationResultGemeinde;
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


	/* Start Delegator-Methoden */

	@Nonnull
	public BigDecimal getVollkosten() {
		return getRelevantBgCalculationResult().getVollkosten();
	}

	@Nonnull
	public BigDecimal getElternbeitrag() {
		return getRelevantBgCalculationResult().getElternbeitrag();
	}

	@Nonnull
	public BigDecimal getVerguenstigungOhneBeruecksichtigungVollkosten() {
		return getRelevantBgCalculationResult().getVerguenstigungOhneBeruecksichtigungVollkosten();
	}

	@Nonnull
	public BigDecimal getVerguenstigungOhneBeruecksichtigungMinimalbeitrag() {
		return getRelevantBgCalculationResult().getVerguenstigungOhneBeruecksichtigungMinimalbeitrag();
	}

	@Nonnull
	public BigDecimal getVerguenstigung() {
		return getRelevantBgCalculationResult().getVerguenstigung();
	}

	@Nonnull
	public BigDecimal getMinimalerElternbeitrag() {
		return getRelevantBgCalculationResult().getMinimalerElternbeitrag();
	}

	@Nonnull
	public BigDecimal getMinimalerElternbeitragGekuerzt() {
		return getRelevantBgCalculationResult().getMinimalerElternbeitragGekuerztNullSafe();
	}

	@Nonnull
	public BigDecimal getVerfuegteAnzahlZeiteinheiten() {
		return getRelevantBgCalculationResult().getBgPensumZeiteinheit();
	}

	@Nonnull
	public BigDecimal getAnspruchsberechtigteAnzahlZeiteinheiten() {
		return getRelevantBgCalculationResult().getAnspruchspensumZeiteinheit();
	}

	public int getAnspruchberechtigtesPensum() {
		return getRelevantBgCalculationResult().getAnspruchspensumProzent();
	}

	@Nonnull
	public PensumUnits getZeiteinheit() {
		return getRelevantBgCalculationResult().getZeiteinheit();
	}

	@Nonnull
	public BigDecimal getBetreuungspensumProzent() {
		return getRelevantBgCalculationResult().getBetreuungspensumProzent();
	}

	@Nonnull
	public BigDecimal getBgPensum() {
		return getRelevantBgCalculationResult().getBgPensumProzent();
	}

	@Nonnull
	public BigDecimal getBetreuungspensumZeiteinheit() {
		return getRelevantBgCalculationResult().getBetreuungspensumZeiteinheit();
	}

	@Nullable
	public BigDecimal getAbzugFamGroesse() {
		return getRelevantBgCalculationResult().getAbzugFamGroesse();
	}

	@Nonnull
	public BigDecimal getMassgebendesEinkommen() {
		return getRelevantBgCalculationResult().getMassgebendesEinkommen();
	}

	@Nonnull
	public BigDecimal getMassgebendesEinkommenVorAbzFamgr() {
		return getRelevantBgCalculationResult().getMassgebendesEinkommenVorAbzugFamgr();
	}

	public boolean isZuSpaetEingereicht() {
		return getRelevantBgCalculationResult().isZuSpaetEingereicht();
	}

	public boolean isMinimalesEwpUnterschritten() {
		return getRelevantBgCalculationResult().isMinimalesEwpUnterschritten();
	}

	@Nullable
	public BigDecimal getFamGroesse() {
		return getRelevantBgCalculationResult().getFamGroesse();
	}

	@Nonnull
	public Integer getEinkommensjahr() {
		return getRelevantBgCalculationResult().getEinkommensjahr();
	}

	public boolean isBesondereBeduerfnisseBestaetigt() {
		return getRelevantBgCalculationResult().isBesondereBeduerfnisseBestaetigt();
	}

	/* Ende Delegator-Methoden */

	/* Start Delegator Setter-Methoden: Setzen die Werte auf BEIDEN inputs */

	public void setLongAbwesenheitForAsivAndGemeinde(boolean longAbwesenheit) {
		this.getBgCalculationInputAsiv().setLongAbwesenheit(longAbwesenheit);
		this.getBgCalculationInputGemeinde().setLongAbwesenheit(longAbwesenheit);
	}

	public void setAnspruchspensumProzentForAsivAndGemeinde(int anspruchspensumProzent) {
		this.getBgCalculationInputAsiv().setAnspruchspensumProzent(anspruchspensumProzent);
		this.getBgCalculationInputGemeinde().setAnspruchspensumProzent(anspruchspensumProzent);
	}

	public void setAusserordentlicherAnspruchForAsivAndGemeinde(int ausserordentlicherAnspruch) {
		this.getBgCalculationInputAsiv().setAusserordentlicherAnspruch(ausserordentlicherAnspruch);
		this.getBgCalculationInputGemeinde().setAusserordentlicherAnspruch(ausserordentlicherAnspruch);
	}

	public void setBetreuungspensumProzentForAsivAndGemeinde(@Nonnull BigDecimal betreuungspensumProzent) {
		this.getBgCalculationInputAsiv().setBetreuungspensumProzent(betreuungspensumProzent);
		this.getBgCalculationInputGemeinde().setBetreuungspensumProzent(betreuungspensumProzent);
	}

	public void setMonatlicheBetreuungskostenForAsivAndGemeinde(BigDecimal monatlicheBetreuungskosten) {
		this.getBgCalculationInputAsiv().setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
		this.getBgCalculationInputGemeinde().setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
	}

	public void setAnspruchspensumRestForAsivAndGemeinde(int anspruchspensumRest) {
		this.getBgCalculationInputAsiv().setAnspruchspensumRest(anspruchspensumRest);
		this.getBgCalculationInputGemeinde().setAnspruchspensumRest(anspruchspensumRest);
	}

	public void setBesondereBeduerfnisseBestaetigtForAsivAndGemeinde(boolean besondereBeduerfnisseBestaetigt) {
		this.getBgCalculationInputAsiv().setBesondereBeduerfnisseBestaetigt(besondereBeduerfnisseBestaetigt);
		this.getBgCalculationInputGemeinde().setBesondereBeduerfnisseBestaetigt(besondereBeduerfnisseBestaetigt);
	}

	public void setEkv1AlleineForAsivAndGemeinde(boolean ekv1Alleine) {
		this.getBgCalculationInputAsiv().setEkv1Alleine(ekv1Alleine);
		this.getBgCalculationInputGemeinde().setEkv1Alleine(ekv1Alleine);
	}

	public void setEkv1ZuZweitForAsivAndGemeinde(boolean ekv1Alleine) {
		this.getBgCalculationInputAsiv().setEkv1ZuZweit(ekv1Alleine);
		this.getBgCalculationInputGemeinde().setEkv1ZuZweit(ekv1Alleine);
	}

	public void setEkv2AlleineForAsivAndGemeinde(boolean ekv1Alleine) {
		this.getBgCalculationInputAsiv().setEkv2Alleine(ekv1Alleine);
		this.getBgCalculationInputGemeinde().setEkv2Alleine(ekv1Alleine);
	}

	public void setEkv2ZuZweitForAsivAndGemeinde(boolean ekv1Alleine) {
		this.getBgCalculationInputAsiv().setEkv2ZuZweit(ekv1Alleine);
		this.getBgCalculationInputGemeinde().setEkv2ZuZweit(ekv1Alleine);
	}

	public void setZuSpaetEingereichtForAsivAndGemeinde(boolean zuSpaetEingereicht) {
		this.getBgCalculationInputAsiv().setZuSpaetEingereicht(zuSpaetEingereicht);
		this.getBgCalculationInputGemeinde().setZuSpaetEingereicht(zuSpaetEingereicht);
	}

	public void setErwerbspensumGS1ForAsivAndGemeinde(@Nullable Integer erwerbspensumGS1) {
		this.getBgCalculationInputAsiv().setErwerbspensumGS1(erwerbspensumGS1);
		this.getBgCalculationInputGemeinde().setErwerbspensumGS1(erwerbspensumGS1);
	}

	public void setErwerbspensumGS2ForAsivAndGemeinde(@Nullable Integer erwerbspensumGS1) {
		this.getBgCalculationInputAsiv().setErwerbspensumGS2(erwerbspensumGS1);
		this.getBgCalculationInputGemeinde().setErwerbspensumGS2(erwerbspensumGS1);
	}

	public void addTaetigkeitForAsivAndGemeinde(@Nullable Taetigkeit taetigkeit) {
		this.getBgCalculationInputAsiv().getTaetigkeiten().add(taetigkeit);
		this.getBgCalculationInputGemeinde().getTaetigkeiten().add(taetigkeit);
	}

	public void setFachstellenpensumForAsivAndGemeinde(int fachstellenpensum) {
		this.getBgCalculationInputAsiv().setFachstellenpensum(fachstellenpensum);
		this.getBgCalculationInputGemeinde().setFachstellenpensum(fachstellenpensum);
	}

	public void setBetreuungspensumMustBeAtLeastFachstellenpensumForAsivAndGemeinde(boolean atLeastFachstellenpensum) {
		this.getBgCalculationInputAsiv().setBetreuungspensumMustBeAtLeastFachstellenpensum(atLeastFachstellenpensum);
		this.getBgCalculationInputGemeinde().setBetreuungspensumMustBeAtLeastFachstellenpensum(atLeastFachstellenpensum);
	}

	public void setAbschnittLiegtNachBEGUStartdatumForAsivAndGemeinde(boolean abschnittLiegtNachBEGUStartdatum) {
		this.getBgCalculationInputAsiv().setAbschnittLiegtNachBEGUStartdatum(abschnittLiegtNachBEGUStartdatum);
		this.getBgCalculationInputGemeinde().setAbschnittLiegtNachBEGUStartdatum(abschnittLiegtNachBEGUStartdatum);
	}

	public void setBabyTarifForAsivAndGemeinde(boolean babyTarif) {
		this.getBgCalculationInputAsiv().setBabyTarif(babyTarif);
		this.getBgCalculationInputGemeinde().setBabyTarif(babyTarif);
	}

	public void setEinschulungTypForAsivAndGemeinde(@Nonnull EinschulungTyp einschulungTyp) {
		this.getBgCalculationInputAsiv().setEinschulungTyp(einschulungTyp);
		this.getBgCalculationInputGemeinde().setEinschulungTyp(einschulungTyp);
	}

	public void setBetreuungsangebotTypForAsivAndGemeinde(@Nonnull BetreuungsangebotTyp typ) {
		this.getBgCalculationInputAsiv().setBetreuungsangebotTyp(typ);
		this.getBgCalculationInputGemeinde().setBetreuungsangebotTyp(typ);
	}

	public void setHasSecondGesuchstellerForFinanzielleSituationForAsivAndGemeinde(boolean hasSecondGesuchstellerForFinanzielleSituation) {
		this.getBgCalculationInputAsiv().setHasSecondGesuchstellerForFinanzielleSituation(hasSecondGesuchstellerForFinanzielleSituation);
		this.getBgCalculationInputGemeinde().setHasSecondGesuchstellerForFinanzielleSituation(hasSecondGesuchstellerForFinanzielleSituation);
	}

	public void setWohnsitzNichtInGemeindeGS1ForAsivAndGemeinde(Boolean wohnsitzNichtInGemeindeGS1) {
		this.getBgCalculationInputAsiv().setWohnsitzNichtInGemeindeGS1(wohnsitzNichtInGemeindeGS1);
		this.getBgCalculationInputGemeinde().setWohnsitzNichtInGemeindeGS1(wohnsitzNichtInGemeindeGS1);
	}

	public void setTsBetreuungszeitProWocheMitBetreuungForAsivAndGemeinde(@Nonnull Integer tsBetreuungszeitProWocheMitBetreuung) {
		this.getBgCalculationInputAsiv().setTsBetreuungszeitProWocheMitBetreuung(tsBetreuungszeitProWocheMitBetreuung);
		this.getBgCalculationInputGemeinde().setTsBetreuungszeitProWocheMitBetreuung(tsBetreuungszeitProWocheMitBetreuung);
	}

	public void setTsVerpflegungskostenMitBetreuungForAsivAndGemeinde(@Nonnull BigDecimal tsVerpflegungskostenMitBetreuung) {
		this.getBgCalculationInputAsiv().setTsVerpflegungskostenMitBetreuung(tsVerpflegungskostenMitBetreuung);
		this.getBgCalculationInputGemeinde().setTsVerpflegungskostenMitBetreuung(tsVerpflegungskostenMitBetreuung);
	}

	public void setTsBetreuungszeitProWocheOhneBetreuungForAsivAndGemeinde(@Nonnull Integer tsBetreuungszeitProWocheOhneBetreuung) {
		this.getBgCalculationInputAsiv().setTsBetreuungszeitProWocheOhneBetreuung(tsBetreuungszeitProWocheOhneBetreuung);
		this.getBgCalculationInputGemeinde().setTsBetreuungszeitProWocheOhneBetreuung(tsBetreuungszeitProWocheOhneBetreuung);
	}

	public void setTsVerpflegungskostenOhneBetreuungForAsivAndGemeinde(@Nonnull BigDecimal tsVerpflegungskostenOhneBetreuung) {
		this.getBgCalculationInputAsiv().setTsVerpflegungskostenOhneBetreuung(tsVerpflegungskostenOhneBetreuung);
		this.getBgCalculationInputGemeinde().setTsVerpflegungskostenOhneBetreuung(tsVerpflegungskostenOhneBetreuung);
	}

	public void setEinkommensjahrForAsivAndGemeinde(@Nonnull Integer einkommensjahr) {
		this.getBgCalculationInputAsiv().setEinkommensjahr(einkommensjahr);
		this.getBgCalculationInputGemeinde().setEinkommensjahr(einkommensjahr);
	}

	public void setAbzugFamGroesseForAsivAndGemeinde(@Nullable BigDecimal abzugFamGroesse) {
		this.getBgCalculationInputAsiv().setAbzugFamGroesse(abzugFamGroesse);
		this.getBgCalculationInputGemeinde().setAbzugFamGroesse(abzugFamGroesse);
	}

	public void setFamGroesseForAsivAndGemeinde(@Nullable BigDecimal famGroesse) {
		this.getBgCalculationInputAsiv().setFamGroesse(famGroesse);
		this.getBgCalculationInputGemeinde().setFamGroesse(famGroesse);
	}

	public void setSameVerfuegteVerfuegungsrelevanteDatenForAsivAndGemeinde(boolean sameVerfuegteVerfuegungsrelevanteDaten) {
		this.getBgCalculationInputAsiv().setSameVerfuegteVerfuegungsrelevanteDaten(sameVerfuegteVerfuegungsrelevanteDaten);
		this.getBgCalculationInputGemeinde().setSameVerfuegteVerfuegungsrelevanteDaten(sameVerfuegteVerfuegungsrelevanteDaten);
	}

	public void setSameAusbezahlteVerguenstigungForAsivAndGemeinde(boolean sameAusbezahlteVerguenstigung) {
		this.getBgCalculationInputAsiv().setSameAusbezahlteVerguenstigung(sameAusbezahlteVerguenstigung);
		this.getBgCalculationInputGemeinde().setSameAusbezahlteVerguenstigung(sameAusbezahlteVerguenstigung);
	}

	public void setSozialhilfeempfaengerForAsivAndGemeinde(boolean sozialhilfe) {
		this.getBgCalculationInputAsiv().setSozialhilfeempfaenger(sozialhilfe);
		this.getBgCalculationInputGemeinde().setSozialhilfeempfaenger(sozialhilfe);
	}
	
	public void setBetreuungInGemeindeForAsivAndGemeinde(boolean inGemeinde) {
		this.getBgCalculationInputAsiv().setBetreuungInGemeinde(inGemeinde);
		this.getBgCalculationInputGemeinde().setBetreuungInGemeinde(inGemeinde);
	}

	/* Ende Delegator Setter-Methoden: Setzen die Werte auf BEIDEN inputs */


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

	public Map<MsgKey, VerfuegungsBemerkung> getBemerkungenMap() {
		return bemerkungenMap;
	}

	/**
	 * Addiert die Daten von "other" zu diesem VerfuegungsZeitabschnitt
	 */
	@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "PMD.NcssMethodCount" })
	public void add(VerfuegungZeitabschnitt other) {
		this.hasGemeindeSpezifischeBerechnung = (this.hasGemeindeSpezifischeBerechnung || other.hasGemeindeSpezifischeBerechnung);
		this.bgCalculationInputAsiv.add(other.bgCalculationInputAsiv);
		this.bgCalculationInputGemeinde.add(other.bgCalculationInputGemeinde);
		this.addAllBemerkungen(other.getBemerkungenMap());
	}

	@Override
	public String toString() {
		String sb = '[' + Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb()) + " - "
			+ Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigBis()) + "] "
			+ " bgCalculationInputAsiv: " + bgCalculationInputAsiv + '\t'
			+ " bgCalculationInputGemeinde: " + bgCalculationInputGemeinde + '\t'
			+ " bgCalculationResultAsiv: " + bgCalculationResultAsiv+ '\t'
			+ " bgCalculationResultGemeinde: " + bgCalculationResultGemeinde + '\t'
			+ " Status: " + zahlungsstatus + '\t'
			+ " Status: " + zahlungsstatus + '\t'
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
		return
			bgCalculationInputAsiv.isSame(otherVerfuegungZeitabschnitt.getBgCalculationInputAsiv()) &&
			(!this.isHasGemeindeSpezifischeBerechnung() || bgCalculationInputGemeinde.isSame(((VerfuegungZeitabschnitt) other).getBgCalculationInputGemeinde())) &&
			EbeguUtil.isSameObject(bgCalculationResultAsiv, otherVerfuegungZeitabschnitt.bgCalculationResultAsiv) &&
			EbeguUtil.isSameObject(bgCalculationResultGemeinde, otherVerfuegungZeitabschnitt.bgCalculationResultGemeinde) &&
			zahlungsstatus == otherVerfuegungZeitabschnitt.zahlungsstatus &&
			Objects.equals(bemerkungenMap, otherVerfuegungZeitabschnitt.bemerkungenMap) &&
			Objects.equals(bemerkungen, otherVerfuegungZeitabschnitt.bemerkungen);
	}

	public boolean isSameSichtbareDaten(VerfuegungZeitabschnitt that) {
		//noinspection ObjectEquality,SimplifiableIfStatement
		if (this == that) {
			return true;
		}
		return
			this.bgCalculationInputAsiv.isSameSichtbareDaten(that.bgCalculationInputAsiv) &&
			(!this.isHasGemeindeSpezifischeBerechnung() || this.bgCalculationInputGemeinde.isSameSichtbareDaten(that.bgCalculationInputGemeinde)) &&
			BGCalculationResult.isSameSichtbareDaten(this.bgCalculationResultAsiv, that.bgCalculationResultAsiv) &&
			BGCalculationResult.isSameSichtbareDaten(this.bgCalculationResultGemeinde, that.bgCalculationResultGemeinde) &&
			Objects.equals(bemerkungenMap, that.bemerkungenMap) &&
			Objects.equals(bemerkungen, that.bemerkungen);
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
			BGCalculationResult.isSamePersistedValues(this.bgCalculationResultAsiv, that.bgCalculationResultAsiv) &&
			(!this.isHasGemeindeSpezifischeBerechnung() ||
				BGCalculationResult.isSamePersistedValues(this.bgCalculationResultGemeinde, that.bgCalculationResultGemeinde)) &&
			getGueltigkeit().compareTo(that.getGueltigkeit()) == 0;
	}

	/**
	 * Vergleich nur die relevanten Daten fuer die Berechnung einer Verfuegung.
	 */
	public boolean isSameBerechnung(VerfuegungZeitabschnitt that) {
		return
			BGCalculationResult.isSameBerechnung(this.bgCalculationResultAsiv, that.bgCalculationResultAsiv) &&
			BGCalculationResult.isSameBerechnung(this.bgCalculationResultGemeinde, that.bgCalculationResultGemeinde) &&
			(getGueltigkeit().compareTo(that.getGueltigkeit()) == 0);
	}

	public boolean isCloseTo(@Nonnull VerfuegungZeitabschnitt that) {
		// Folgende Attribute sollen bei einer "kleinen" Änderung nicht zu einer Neuberechnung führen:
		// (explizit wird nur ASIV verglichen, da es sich um einen "alten" Rundungsfehler handelt)
		return bgCalculationResultAsiv.isCloseTo(that.getBgCalculationResultAsiv());
	}

	public void copyCalculationResult(@Nonnull VerfuegungZeitabschnitt that) {
		this.bgCalculationResultAsiv.copyCalculationResult(that.bgCalculationResultAsiv);
		if (this.bgCalculationResultGemeinde != null) {
			this.bgCalculationResultGemeinde.copyCalculationResult(that.bgCalculationResultGemeinde);
		}
	}

	public void addAllBemerkungen(Map<MsgKey, VerfuegungsBemerkung> otherBemerkungenMap) {
		this.bemerkungenMap.putAll(otherBemerkungenMap);
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

	@Override
	public int compareTo(@Nonnull VerfuegungZeitabschnitt other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getGueltigkeit(), other.getGueltigkeit());
		// wenn ids nicht gleich sind wollen wir auch compare to nicht gleich
		compareToBuilder.append(this.getId(), other.getId());
		return compareToBuilder.toComparison();
	}

	@Nonnull
	public BGCalculationResult getRelevantBgCalculationResult() {
		if (isHasGemeindeSpezifischeBerechnung()) {
			Objects.requireNonNull(this.getBgCalculationResultGemeinde());
			return this.getBgCalculationResultGemeinde();
		}
		return this.getBgCalculationResultAsiv();
	}

	@Nonnull
	public BGCalculationInput getRelevantBgCalculationInput() {
		if (isHasGemeindeSpezifischeBerechnung()) {
			Objects.requireNonNull(this.getBgCalculationInputGemeinde());
			return this.getBgCalculationInputGemeinde();
		}
		return this.getBgCalculationInputAsiv();
	}

	public void copyValuesToResult() {
		copyValuesToResult(getBgCalculationInputAsiv(), getBgCalculationResultAsiv());
		if (getBgCalculationResultGemeinde() != null) {
			copyValuesToResult(getBgCalculationInputGemeinde(), getBgCalculationResultGemeinde());
		}
	}

	private void copyValuesToResult(@Nonnull BGCalculationInput input, @Nonnull BGCalculationResult result) {
		result.setAnspruchspensumProzent(input.getAnspruchspensumProzent());
		result.setBetreuungspensumProzent(input.getBetreuungspensumProzent());
		result.setMassgebendesEinkommenVorAbzugFamgr(input.getMassgebendesEinkommenVorAbzugFamgr());
		result.setBesondereBeduerfnisseBestaetigt(input.isBesondereBeduerfnisseBestaetigt());
		result.setAbzugFamGroesse(input.getAbzugFamGroesseNonNull());
		result.setEinkommensjahr(input.getEinkommensjahr());
		result.setZuSpaetEingereicht(input.isZuSpaetEingereicht());
		result.setMinimalesEwpUnterschritten(input.isMinimalesEwpUnterschritten());
		result.setFamGroesse(input.getFamGroesseNonNull());
		if (input.getTsBetreuungszeitProWocheMitBetreuung() > 0) {
			TSCalculationResult tsResultMitBetreuung = new TSCalculationResult();
			tsResultMitBetreuung.setBetreuungszeitProWoche(input.getTsBetreuungszeitProWocheMitBetreuung());
			tsResultMitBetreuung.setVerpflegungskosten(input.getTsVerpflegungskostenMitBetreuung());
			result.setTsCalculationResultMitPaedagogischerBetreuung(tsResultMitBetreuung);
		}
		if (input.getTsBetreuungszeitProWocheOhneBetreuung() > 0) {
			TSCalculationResult tsResultOhneBetreuung = new TSCalculationResult();
			tsResultOhneBetreuung.setBetreuungszeitProWoche(input.getTsBetreuungszeitProWocheOhneBetreuung());
			tsResultOhneBetreuung.setVerpflegungskosten(input.getTsVerpflegungskostenOhneBetreuung());
			result.setTsCalculationResultOhnePaedagogischerBetreuung(tsResultOhneBetreuung);
		}
	}
}
