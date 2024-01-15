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

package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.VerfuegungsBemerkungDTOList;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dieses Objekt repraesentiert einen Zeitabschnitt wahrend eines Betreeungsgutscheinantrags waehrend dem die Faktoren
 * die fuer die Berechnung des Gutscheins der Betreuung relevant sind konstant geblieben sind.
 */
@Entity
@Audited
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = "bg_calculation_result_asiv_id",
		name = "UK_verfuegung_zeitabschnitt_result_asiv")
)
public class VerfuegungZeitabschnitt extends AbstractDateRangedEntity implements Comparable<VerfuegungZeitabschnitt> {

	private static final long serialVersionUID = 7250339356897563374L;

	@Column(nullable = false)
	private boolean hasGemeindeSpezifischeBerechnung = false;

	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(nullable = false)
	private Regelwerk regelwerk = Regelwerk.ASIV;

	/**
	 * Input-Werte für die Rules. Berechnung nach ASIV (Standard)
	 */
	@Transient
	@Nonnull
	private BGCalculationInput bgCalculationInputAsiv = new BGCalculationInput(this, RuleValidity.ASIV);

	/**
	 * Input-Werte für die Rules. Berechnung nach Spezialwünschen der Gemeinde, optional
	 */
	@Transient
	@Nonnull
	private BGCalculationInput bgCalculationInputGemeinde = new BGCalculationInput(this, RuleValidity.GEMEINDE);

	/**
	 * Berechnungsresultate. Berechnung nach ASIV (Standard)
	 */
	@Valid
	@Nonnull
	@NotNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegungZeitabschnitt_resultatAsiv"), nullable = false)
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

	@NotNull
	@Nonnull
	@OneToMany(mappedBy = "verfuegungZeitabschnitt")
	private List<Zahlungsposition> zahlungsposition = new ArrayList<>();

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private VerfuegungsZeitabschnittZahlungsstatus zahlungsstatusInstitution = VerfuegungsZeitabschnittZahlungsstatus.NEU;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private VerfuegungsZeitabschnittZahlungsstatus zahlungsstatusAntragsteller = VerfuegungsZeitabschnittZahlungsstatus.NEU;

	// Die Bemerkungen werden vorerst in eine Map geschrieben, damit einzelne
	// Bemerkungen spaeter wieder zugreifbar sind. Am Ende des RuleSets werden sie ins persistente Object
	// "verfuegungZeitabschnittBemerkungList" geschrieben
	@Transient
	private final VerfuegungsBemerkungDTOList bemerkungenDTOList = new VerfuegungsBemerkungDTOList();


	@OneToMany(mappedBy = "verfuegungZeitabschnitt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<VerfuegungZeitabschnittBemerkung> verfuegungZeitabschnittBemerkungList = Collections.emptyList();

	public VerfuegungZeitabschnitt() {
	}

	/**
	 * copy Konstruktor
	 */
	@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "PMD.ConstructorCallsOverridableMethod" })
	public VerfuegungZeitabschnitt(VerfuegungZeitabschnitt toCopy) {
		this.setGueltigkeit(new DateRange(toCopy.getGueltigkeit()));
		this.regelwerk = toCopy.regelwerk;
		this.hasGemeindeSpezifischeBerechnung = toCopy.hasGemeindeSpezifischeBerechnung;
		this.bgCalculationInputAsiv = new BGCalculationInput(this, toCopy.bgCalculationInputAsiv);
		this.bgCalculationInputGemeinde = new BGCalculationInput(this, toCopy.bgCalculationInputGemeinde);
		this.bgCalculationResultAsiv = new BGCalculationResult(toCopy.getBgCalculationResultAsiv());
		if (this.hasGemeindeSpezifischeBerechnung && toCopy.getBgCalculationResultGemeinde() != null) {
			this.bgCalculationResultGemeinde = new BGCalculationResult(toCopy.getBgCalculationResultGemeinde());
		}
		//noinspection ConstantConditions: Muss erst beim Speichern gesetzt sein
		this.verfuegung = null;
		this.bemerkungenDTOList.mergeBemerkungenMap(toCopy.bemerkungenDTOList);
		this.verfuegungZeitabschnittBemerkungList = toCopy.verfuegungZeitabschnittBemerkungList;
		this.zahlungsstatusInstitution = toCopy.zahlungsstatusInstitution;
		this.zahlungsstatusAntragsteller = toCopy.zahlungsstatusAntragsteller;
	}

	/**
	 * Erstellt einen Zeitabschnitt mit der gegebenen gueltigkeitsdauer
	 */
	public VerfuegungZeitabschnitt(DateRange gueltigkeit) {
		this.setGueltigkeit(new DateRange(gueltigkeit));
	}

	@Nonnull
	public Regelwerk getRegelwerk() {
		return regelwerk;
	}

	public void setRegelwerk(@Nonnull Regelwerk regelwerk) {
		this.regelwerk = regelwerk;
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

	@Nonnull
	public List<VerfuegungZeitabschnittBemerkung> getVerfuegungZeitabschnittBemerkungList() {
		return verfuegungZeitabschnittBemerkungList;
	}

	public void setVerfuegungZeitabschnittBemerkungList(
		@Nonnull List<VerfuegungZeitabschnittBemerkung> verfuegungZeitabschnittBemerkungList) {
		this.verfuegungZeitabschnittBemerkungList = verfuegungZeitabschnittBemerkungList;
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

	@Nullable
	public BigDecimal getVerguenstigungProZeiteinheit() {
		return getRelevantBgCalculationResult().getVerguenstigungProZeiteinheit();
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

	@Nullable
	public BigDecimal getAnspruchspensumRest() {
		return getRelevantBgCalculationResult().getAnspruchspensumRest();
	}

	@Nonnull
	public BigDecimal getBetreuungspensumZeiteinheit() {
		return getRelevantBgCalculationResult().getBetreuungspensumZeiteinheit();
	}

	@Nonnull
	public BigDecimal getBgPensumZeiteinheit() {
		return getRelevantBgCalculationResult().getBgPensumZeiteinheit();
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

	@Nullable
	public TSCalculationResult getTsCalculationResultMitPaedagogischerBetreuung() {
		return getRelevantBgCalculationResult().getTsCalculationResultMitPaedagogischerBetreuung();
	}

	@Nullable
	public TSCalculationResult getTsCalculationResultOhnePaedagogischerBetreuung() {
		return getRelevantBgCalculationResult().getTsCalculationResultOhnePaedagogischerBetreuung();
	}

	public boolean isAuszahlungAnEltern() {
		return getRelevantBgCalculationResult().isAuszahlungAnEltern();
	}

	public Integer getBeitraghoheProzent() {
		return getRelevantBgCalculationResult().getBeitragshoeheProzent();
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

	public void setBesondereBeduerfnisseBetragForAsivAndGemeinde(@Nullable BigDecimal betrag) {
		this.getBgCalculationInputAsiv().setBesondereBeduerfnisseZuschlag(betrag);
		this.getBgCalculationInputGemeinde().setBesondereBeduerfnisseZuschlag(betrag);
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

	public void setErwerbspensumZuschlagForAsivAndGemeinde(int zuschlag) {
		this.getBgCalculationInputAsiv().setErwerbspensumZuschlag(zuschlag);
		this.getBgCalculationInputGemeinde().setErwerbspensumZuschlag(zuschlag);
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

	public void setFachstelleSprachlicheIntegrationBestaetigtForAsivAndGemeinde(boolean fachstelleSprachlicheIntegrationBestaetigt) {
		this.getBgCalculationInputAsiv().setFachstelleSprachlicheIntegrationBestaetigt(fachstelleSprachlicheIntegrationBestaetigt);
		this.getBgCalculationInputGemeinde().setFachstelleSprachlicheIntegrationBestaetigt(fachstelleSprachlicheIntegrationBestaetigt);
	}

	public void setIntegrationTypFachstellenPensumForAsivAndGemeinde(IntegrationTyp integrationTyp) {
		this.getBgCalculationInputAsiv().setIntegrationTypFachstellenPensum(integrationTyp);
		this.getBgCalculationInputGemeinde().setIntegrationTypFachstellenPensum(integrationTyp);
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

	public void setVerpflegungskostenUndMahlzeitenMitBetreuungForAsivAndGemeinde(Map<BigDecimal, Integer> kostenMahlzeitMap) {
		this.getBgCalculationInputAsiv().setVerpflegungskostenUndMahlzeitenMitBetreuung(kostenMahlzeitMap);
		this.getBgCalculationInputGemeinde().setVerpflegungskostenUndMahlzeitenMitBetreuung(kostenMahlzeitMap);
	}

	public void setVerpflegungskostenUndMahlzeitenMitBetreuungZweiWochenForAsivAndGemeinde(Map<BigDecimal, Integer> kostenMahlzeitMap) {
		this.getBgCalculationInputAsiv().setVerpflegungskostenUndMahlzeitenMitBetreuungZweiWochen(kostenMahlzeitMap);
		this.getBgCalculationInputGemeinde().setVerpflegungskostenUndMahlzeitenMitBetreuungZweiWochen(kostenMahlzeitMap);
	}

	public void setVerpflegungskostenUndMahlzeitenOhneBetreuungForAsivAndGemeinde(Map<BigDecimal, Integer> kostenMahlzeitMap) {
		this.getBgCalculationInputAsiv().setVerpflegungskostenUndMahlzeitenOhneBetreuung(kostenMahlzeitMap);
		this.getBgCalculationInputGemeinde().setVerpflegungskostenUndMahlzeitenOhneBetreuung(kostenMahlzeitMap);
	}

	public void setVerpflegungskostenUndMahlzeitenOhneBetreuungZweiWochenForAsivAndGemeinde(Map<BigDecimal, Integer> kostenMahlzeitMap) {
		this.getBgCalculationInputAsiv().setVerpflegungskostenUndMahlzeitenOhneBetreuungZweiWochen(kostenMahlzeitMap);
		this.getBgCalculationInputGemeinde().setVerpflegungskostenUndMahlzeitenOhneBetreuungZweiWochen(kostenMahlzeitMap);
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

	public void setSozialhilfeempfaengerForAsivAndGemeinde(boolean sozialhilfe) {
		this.getBgCalculationInputAsiv().setSozialhilfeempfaenger(sozialhilfe);
		this.getBgCalculationInputGemeinde().setSozialhilfeempfaenger(sozialhilfe);
	}

	public void setBetreuungInGemeindeForAsivAndGemeinde(boolean inGemeinde) {
		this.getBgCalculationInputAsiv().setBetreuungInGemeinde(inGemeinde);
		this.getBgCalculationInputGemeinde().setBetreuungInGemeinde(inGemeinde);
	}

	public void setMonatlicheHauptmahlzeitenForAsivAndGemeinde(BigDecimal monatlicheHauptmahlzeiten) {
		this.getBgCalculationInputAsiv().setAnzahlHauptmahlzeiten(monatlicheHauptmahlzeiten);
		this.getBgCalculationInputGemeinde().setAnzahlHauptmahlzeiten(monatlicheHauptmahlzeiten);
	}

	public void setMonatlicheNebenmahlzeitenForAsivAndGemeinde(BigDecimal monatlicheNebenmahlzeiten) {
		this.getBgCalculationInputAsiv().setAnzahlNebenmahlzeiten(monatlicheNebenmahlzeiten);
		this.getBgCalculationInputGemeinde().setAnzahlNebenmahlzeiten(monatlicheNebenmahlzeiten);
	}

	public void setTarifHauptmahlzeitForAsivAndGemeinde(BigDecimal tarifHauptmahlzeit) {
		this.getBgCalculationInputAsiv().setTarifHauptmahlzeit(tarifHauptmahlzeit);
		this.getBgCalculationInputGemeinde().setTarifHauptmahlzeit(tarifHauptmahlzeit);
	}

	public void setTarifNebenmahlzeitForAsivAndGemeinde(BigDecimal tarifNebenmahlzeit) {
		this.getBgCalculationInputAsiv().setTarifNebenmahlzeit(tarifNebenmahlzeit);
		this.getBgCalculationInputGemeinde().setTarifNebenmahlzeit(tarifNebenmahlzeit);
	}

	public void setVerguenstigungMahlzeitenBeantragtForAsivAndGemeinde(boolean verguenstigungMahlzeitenBeantragt) {
		this.getBgCalculationInputAsiv().setVerguenstigungMahlzeitenBeantragt(verguenstigungMahlzeitenBeantragt);
		this.getBgCalculationInputGemeinde().setVerguenstigungMahlzeitenBeantragt(verguenstigungMahlzeitenBeantragt);
	}

	public void setPensumUnitForAsivAndGemeinde(PensumUnits unit) {
		this.getBgCalculationInputAsiv().setPensumUnit(unit);
		this.getBgCalculationInputGemeinde().setPensumUnit(unit);

	}

	public void calculateInputValuesProportionaly(double percentag) {
		this.getBgCalculationInputAsiv().calculateInputValuesProportionaly(percentag);
		this.getBgCalculationInputGemeinde().calculateInputValuesProportionaly(percentag);
	}

	public void roundValuesAfterCalculateProportinaly() {
		this.getBgCalculationInputAsiv().roundValuesAfterCalculateProportinaly();
		this.getBgCalculationInputGemeinde().roundValuesAfterCalculateProportinaly();
	}

	public void setStuendlicheVollkosten(BigDecimal stuendlicheVollkosten) {
		this.getBgCalculationInputAsiv().setStuendlicheVollkosten(stuendlicheVollkosten);
		this.getBgCalculationInputGemeinde().setStuendlicheVollkosten(stuendlicheVollkosten);
	}

	public void setAuszahlungAnEltern(boolean auszahlungAnEltern) {
		this.getBgCalculationInputAsiv().setAuszahlungAnEltern(auszahlungAnEltern);
		this.getBgCalculationInputGemeinde().setAuszahlungAnEltern(auszahlungAnEltern);
	}

	public boolean hasBetreuungspensum() {
		return !MathUtil.isZero(this.getRelevantBgCalculationResult().getBetreuungspensumProzent());
	}

	public void setPartnerIdentischMitVorgesuch(boolean partnerIdentischMitVorgesuch){
		this.getBgCalculationInputAsiv().setPartnerIdentischMitVorgesuch(partnerIdentischMitVorgesuch);
		this.getBgCalculationInputGemeinde().setPartnerIdentischMitVorgesuch(partnerIdentischMitVorgesuch);
	}

	public void setBgStundenFaktor(BigDecimal bgStundenFaktor){
		this.getBgCalculationInputAsiv().setBgStundenFaktor(bgStundenFaktor);
		this.getBgCalculationInputGemeinde().setBgStundenFaktor(bgStundenFaktor);
	}

	/* Ende Delegator Setter-Methoden: Setzen die Werte auf BEIDEN inputs */

	@Nonnull
	public Verfuegung getVerfuegung() {
		return verfuegung;
	}

	public void setVerfuegung(@Nonnull Verfuegung verfuegung) {
		this.verfuegung = verfuegung;
	}

	@Nonnull
	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatusInstitution() {
		return zahlungsstatusInstitution;
	}

	public void setZahlungsstatusInstitution(@Nonnull VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus) {
		this.zahlungsstatusInstitution = zahlungsstatus;
	}

	@Nonnull
	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatusAntragsteller() {
		return zahlungsstatusAntragsteller;
	}

	public void setZahlungsstatusAntragsteller(@Nonnull VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus) {
		this.zahlungsstatusAntragsteller = zahlungsstatus;
	}

	@Nonnull
	public List<Zahlungsposition> getZahlungsposition() {
		return zahlungsposition;
	}

	public void setZahlungsposition(@Nonnull List<Zahlungsposition> zahlungsposition) {
		this.zahlungsposition = zahlungsposition;
	}

	public VerfuegungsBemerkungDTOList getBemerkungenDTOList() {
		return bemerkungenDTOList;
	}

	/**
	 * Addiert die Daten von "other" zu diesem VerfuegungsZeitabschnitt
	 */
	@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "PMD.NcssMethodCount" })
	public void add(VerfuegungZeitabschnitt other) {
		this.hasGemeindeSpezifischeBerechnung = (this.hasGemeindeSpezifischeBerechnung || other.hasGemeindeSpezifischeBerechnung);
		this.bgCalculationInputAsiv.add(other.bgCalculationInputAsiv);
		this.bgCalculationInputGemeinde.add(other.bgCalculationInputGemeinde);
		this.bemerkungenDTOList.addAllBemerkungen(other.bemerkungenDTOList);
	}

	@Override
	public String toString() {
		String sb = '\t' + this.toStringWithoutBemerkung()
			+ " Bemerkungen: " + getVerfuegungenZeitabschnittBemerkungenAsString();
		return sb;
	}

	public String toStringWithoutBemerkung() {
		String sb = '[' + Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb()) + " - "
			+ Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigBis()) + "] "
			+ " bgCalculationInputAsiv: " + bgCalculationInputAsiv + '\t'
			+ " bgCalculationInputGemeinde: " + bgCalculationInputGemeinde + '\t'
			+ " bgCalculationResultAsiv: " + bgCalculationResultAsiv+ '\t'
			+ " bgCalculationResultGemeinde: " + bgCalculationResultGemeinde + '\t'
			+ " Regelwerk: " + regelwerk + '\t'
			+ " Zahlungsstatus Institution: " + zahlungsstatusInstitution + '\t'
			+ " Zahlungsstatus Antragsteller: " + zahlungsstatusAntragsteller;
		return sb;
	}

	@Override
	@SuppressWarnings({ "OverlyComplexBooleanExpression", "AccessingNonPublicFieldOfAnotherObject",
		"OverlyComplexMethod", "PMD.CompareObjectsWithEquals" })
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		//noinspection ConstantConditions: Sonst motzt PMD
		if (!(other instanceof VerfuegungZeitabschnitt)) {
			return false;
		}
		final VerfuegungZeitabschnitt otherVerfuegungZeitabschnitt = (VerfuegungZeitabschnitt) other;
		return
			bgCalculationInputAsiv.isSame(otherVerfuegungZeitabschnitt.getBgCalculationInputAsiv()) &&
			(!this.isHasGemeindeSpezifischeBerechnung() || bgCalculationInputGemeinde.isSame(((VerfuegungZeitabschnitt) other).getBgCalculationInputGemeinde())) &&
			EbeguUtil.isSame(bgCalculationResultAsiv, otherVerfuegungZeitabschnitt.bgCalculationResultAsiv) &&
			EbeguUtil.isSame(bgCalculationResultGemeinde, otherVerfuegungZeitabschnitt.bgCalculationResultGemeinde) &&
			zahlungsstatusInstitution == otherVerfuegungZeitabschnitt.zahlungsstatusInstitution &&
			zahlungsstatusAntragsteller == otherVerfuegungZeitabschnitt.zahlungsstatusAntragsteller &&
			this.bemerkungenDTOList.isSame(((VerfuegungZeitabschnitt) other).bemerkungenDTOList) &&
			Objects.equals(verfuegungZeitabschnittBemerkungList, otherVerfuegungZeitabschnitt.verfuegungZeitabschnittBemerkungList);
	}

	@SuppressWarnings("PMD.CompareObjectsWithEquals")
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
			this.bemerkungenDTOList.isSame(that.bemerkungenDTOList) &&
			Objects.equals(verfuegungZeitabschnittBemerkungList, that.verfuegungZeitabschnittBemerkungList);
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
		boolean asivCloseTo = bgCalculationResultAsiv.isCloseTo(that.getBgCalculationResultAsiv());
		boolean gemeindeCloseTo = true;
		if (hasGemeindeSpezifischeBerechnung) {
			Objects.requireNonNull(this.getBgCalculationResultGemeinde());
			if (that.hasGemeindeSpezifischeBerechnung) {
				Objects.requireNonNull(that.getBgCalculationResultGemeinde());
				gemeindeCloseTo =
					this.getBgCalculationResultGemeinde().isCloseTo(that.getBgCalculationResultGemeinde());
			} else {
				gemeindeCloseTo = false;
			}
		}
		return asivCloseTo && gemeindeCloseTo;
	}

	public void copyCalculationResult(@Nonnull VerfuegungZeitabschnitt that) {
		this.bgCalculationResultAsiv.copyCalculationResult(that.bgCalculationResultAsiv);
		if (this.bgCalculationResultGemeinde != null) {
			this.bgCalculationResultGemeinde.copyCalculationResult(that.bgCalculationResultGemeinde);
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

	public void initBGCalculationResult() {
		initBGCalculationResult(getBgCalculationInputAsiv(), getBgCalculationResultAsiv());
		if (getBgCalculationResultGemeinde() != null) {
			initBGCalculationResult(getBgCalculationInputGemeinde(), getBgCalculationResultGemeinde());
		}
	}

	public static void initBGCalculationResult(@Nonnull BGCalculationInput input, @Nonnull BGCalculationResult result) {
		result.setAnspruchspensumProzent(input.getAnspruchspensumProzent());
		if (input.getAnspruchspensumRest() > -1) {
			result.setAnspruchspensumRest(MathUtil.DEFAULT.from(input.getAnspruchspensumRest()));
		}
		result.setBetreuungspensumProzent(input.getBetreuungspensumProzent());
		result.setMassgebendesEinkommenVorAbzugFamgr(input.getMassgebendesEinkommenVorAbzugFamgr());
		result.setBesondereBeduerfnisseBestaetigt(input.isBesondereBeduerfnisseBestaetigt());
		result.setAbzugFamGroesse(input.getAbzugFamGroesseNonNull());
		result.setEinkommensjahr(input.getEinkommensjahr());
		result.setZuSpaetEingereicht(input.isZuSpaetEingereicht());
		result.setMinimalesEwpUnterschritten(input.isMinimalesEwpUnterschritten());
		result.setFamGroesse(input.getFamGroesseNonNull());
		result.setAuszahlungAnEltern(input.isAuszahlungAnEltern());
		result.setVerguenstigungGewuenscht(input.isVerguenstigungGewuenscht());
	}

	public boolean isSameMZV(VerfuegungZeitabschnitt other) {
		return BGCalculationResult.isSameMZVResult(this.bgCalculationResultAsiv, other.bgCalculationResultAsiv) &&
				BGCalculationResult.isSameMZVResult(this.bgCalculationResultGemeinde, other.bgCalculationResultGemeinde);
	}

	public void setSameVerfuegteMahlzeitenVerguenstigungForAsivAndGemeinde(boolean same) {
		this.bgCalculationInputAsiv.setSameVerfuegteMahlzeitenVerguenstigung(same);
		this.bgCalculationInputGemeinde.setSameVerfuegteMahlzeitenVerguenstigung(same);
	}

	public String getVerfuegungenZeitabschnittBemerkungenAsString() {
		return this.getVerfuegungZeitabschnittBemerkungList().stream()
			.map(VerfuegungZeitabschnittBemerkung::getBemerkung)
			.collect(Collectors.joining("\n"));
	}

	public void setGeschwisternBonusKind2ForAsivAndGemeinde(boolean geschwisternBonusKind2) {
		this.bgCalculationInputAsiv.setGeschwisternBonusKind2(geschwisternBonusKind2);
		this.bgCalculationInputGemeinde.setGeschwisternBonusKind2(geschwisternBonusKind2);
	}

	public void setGeschwisternBonusKind3ForAsivAndGemeinde(boolean geschwisternBonusKind3) {
		this.bgCalculationInputAsiv.setGeschwisternBonusKind3(geschwisternBonusKind3);
		this.bgCalculationInputGemeinde.setGeschwisternBonusKind3(geschwisternBonusKind3);
	}

	public void setRequiredAgeForAnspruchNotReached(boolean requiredAgeForAnspruchNotReached) {
		this.bgCalculationInputAsiv.setRequiredAgeForAnspruchNotReached(requiredAgeForAnspruchNotReached);
		this.bgCalculationInputGemeinde.setRequiredAgeForAnspruchNotReached(requiredAgeForAnspruchNotReached);
	}

	public void setGesuchBeendenKonkubinatMitZweiGS(boolean gesuchBeenden) {
		this.bgCalculationInputAsiv.setGesuchBeendenKonkubinatMitZweiGS(gesuchBeenden);
		this.bgCalculationInputGemeinde.setGesuchBeendenKonkubinatMitZweiGS(gesuchBeenden);
	}
}
