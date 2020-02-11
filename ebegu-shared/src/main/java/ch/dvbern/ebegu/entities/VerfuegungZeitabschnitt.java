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
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.PensumUnits;
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

		this.bgCalculationInputAsiv = new BGCalculationInput(toCopy.bgCalculationInputAsiv);
		this.bgCalculationInputGemeinde = new BGCalculationInput(toCopy.bgCalculationInputGemeinde);
		this.bgCalculationResultAsiv = new BGCalculationResult(toCopy.getBgCalculationResultAsiv());
		if (toCopy.getBgCalculationResultGemeinde() != null) {
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
		return getBgCalculationResultAsiv().getVollkosten();
	}

	@Nonnull
	public BigDecimal getElternbeitrag() {
		return getBgCalculationResultAsiv().getElternbeitrag();
	}

	@Nonnull
	public BigDecimal getVerguenstigungOhneBeruecksichtigungVollkosten() {
		return getBgCalculationResultAsiv().getVerguenstigungOhneBeruecksichtigungVollkosten();
	}

	@Nonnull
	public BigDecimal getVerguenstigungOhneBeruecksichtigungMinimalbeitrag() {
		return getBgCalculationResultAsiv().getVerguenstigungOhneBeruecksichtigungMinimalbeitrag();
	}

	@Nonnull
	public BigDecimal getVerguenstigung() {
		return getBgCalculationResultAsiv().getVerguenstigung();
	}

	@Nonnull
	public BigDecimal getMinimalerElternbeitrag() {
		return getBgCalculationResultAsiv().getMinimalerElternbeitrag();
	}

	@Nonnull
	public BigDecimal getMinimalerElternbeitragGekuerzt() {
		return getBgCalculationResultAsiv().getMinimalerElternbeitragGekuerztNullSafe();
	}

	@Nonnull
	public BigDecimal getVerfuegteAnzahlZeiteinheiten() {
		return getBgCalculationResultAsiv().getBgPensumZeiteinheit();
	}

	@Nonnull
	public BigDecimal getAnspruchsberechtigteAnzahlZeiteinheiten() {
		return getBgCalculationResultAsiv().getAnspruchspensumZeiteinheit();
	}

	@Nonnull
	public int getAnspruchberechtigtesPensum() {
		return getBgCalculationResultAsiv().getAnspruchspensumProzent();
	}

	@Nonnull
	public PensumUnits getZeiteinheit() {
		return getBgCalculationResultAsiv().getZeiteinheit();
	}

	@Nonnull
	public BigDecimal getBetreuungspensumProzent() {
		return getBgCalculationResultAsiv().getBetreuungspensumProzent();
	}

	@Nonnull
	public BigDecimal getBgPensum() {
		return getBgCalculationResultAsiv().getBgPensumProzent();
	}

	@Nonnull
	public BigDecimal getBetreuungspensumZeiteinheit() {
		return getBgCalculationResultAsiv().getBetreuungspensumZeiteinheit();
	}

	@Nullable
	public BigDecimal getAbzugFamGroesse() {
		return getBgCalculationResultAsiv().getAbzugFamGroesse();
	}

	@Nonnull
	public BigDecimal getMassgebendesEinkommen() {
		return getBgCalculationResultAsiv().getMassgebendesEinkommen();
	}

	@Nonnull
	public BigDecimal getMassgebendesEinkommenVorAbzFamgr() {
		return getBgCalculationResultAsiv().getMassgebendesEinkommenVorAbzugFamgr();
	}

	public boolean isZuSpaetEingereicht() {
		return getBgCalculationResultAsiv().isZuSpaetEingereicht();
	}

	public boolean isMinimalesEwpUnterschritten() {
		return getBgCalculationResultAsiv().isMinimalesEwpUnterschritten();
	}

	@Nullable
	public BigDecimal getFamGroesse() {
		return getBgCalculationResultAsiv().getFamGroesse();
	}

	@Nonnull
	public Integer getEinkommensjahr() {
		return getBgCalculationResultAsiv().getEinkommensjahr();
	}

	public boolean isBesondereBeduerfnisseBestaetigt() {
		return getBgCalculationResultAsiv().isBesondereBeduerfnisseBestaetigt();
	}

	/* Ende Delegator-Methoden */

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
		this.bgCalculationInputAsiv.add(other.bgCalculationInputAsiv);
		this.bgCalculationInputGemeinde.add(other.bgCalculationInputGemeinde);
		this.bgCalculationResultAsiv.add(other.bgCalculationResultAsiv);
		if (other.getBgCalculationResultGemeinde() != null) {
			if (this.bgCalculationResultGemeinde == null) {
				this.bgCalculationResultGemeinde = new BGCalculationResult();
			}
			this.bgCalculationResultGemeinde.add(other.getBgCalculationResultGemeinde());
		}
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

	public String toStringTagesschuleInfos() {
		String sb = '[' + Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb()) + " - "
			+ Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigBis()) + "] "
			+ " massgebendesEinkommen: " + bgCalculationResultAsiv.getMassgebendesEinkommen() + '\n'
			+ " mitBetreuung: " + bgCalculationResultAsiv.getTsCalculationResultMitPaedagogischerBetreuung() + '\n'
			+ " ohneBetreuung: " + bgCalculationResultAsiv.getTsCalculationResultOhnePaedagogischerBetreuung();
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
			bgCalculationInputGemeinde.isSame(((VerfuegungZeitabschnitt) other).getBgCalculationInputGemeinde()) &&
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
			this.bgCalculationInputGemeinde.isSameSichtbareDaten(that.bgCalculationInputGemeinde) &&
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
			BGCalculationResult.isSamePersistedValues(this.bgCalculationResultGemeinde, that.bgCalculationResultGemeinde) &&
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
		result.setAbzugFamGroesse(input.getAbzugFamGroesse());
		result.setEinkommensjahr(input.getEinkommensjahr());
		result.setZuSpaetEingereicht(input.isZuSpaetEingereicht());
		result.setMinimalesEwpUnterschritten(input.isMinimalesEwpUnterschritten());
		result.setFamGroesse(input.getFamGroesse());
		if (input.getTsBetreuungszeitProWocheMitBetreuung() > 0) {
			TSCalculationResult tsResultMitBetreuung = new TSCalculationResult();
			tsResultMitBetreuung.setBetreuungszeitProWoche(input.getTsBetreuungszeitProWocheMitBetreuung());
			tsResultMitBetreuung.setVerpflegungskosten(input.getTsVerpflegungskostenMitBetreuung());
			tsResultMitBetreuung.setGebuehrProStunde(input.getTsGebuehrProStundeMitBetreuung());
			tsResultMitBetreuung.setTotalKostenProWoche(input.getTsTotalKostenProWocheMitBetreuung());
			result.setTsCalculationResultMitPaedagogischerBetreuung(tsResultMitBetreuung);
		}
		if (input.getTsBetreuungszeitProWocheOhneBetreuung() > 0) {
			TSCalculationResult tsResultOhneBetreuung = new TSCalculationResult();
			tsResultOhneBetreuung.setBetreuungszeitProWoche(input.getTsBetreuungszeitProWocheOhneBetreuung());
			tsResultOhneBetreuung.setVerpflegungskosten(input.getTsVerpflegungskostenOhneBetreuung());
			tsResultOhneBetreuung.setGebuehrProStunde(input.getTsGebuehrProStundeOhneBetreuung());
			tsResultOhneBetreuung.setTotalKostenProWoche(input.getTsTotalKostenProWocheOhneBetreuung());
			result.setTsCalculationResultOhnePaedagogischerBetreuung(tsResultOhneBetreuung);
		}
	}
}
