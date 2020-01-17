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
import java.util.Objects;

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
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
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
	private BGCalculationInput bgCalculationInputAsiv = new BGCalculationInput();

	/**
	 * Input-Werte für die Rules. Berechnung nach Spezialwünschen der Gemeinde, optional
	 */
	@Transient
	@Nonnull
	private BGCalculationInput bgCalculationInputGemeinde = new BGCalculationInput();

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

	@NotNull @Nonnull
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

	@Column(nullable = false)
	private @NotNull Integer einkommensjahr;

	@Column(nullable = true)
	private BigDecimal abzugFamGroesse = null;

	@Column(nullable = true)
	private BigDecimal famGroesse = null;

	@Column(nullable = true)
	@Nonnull
	private BigDecimal massgebendesEinkommenVorAbzugFamgr = BigDecimal.ZERO;

	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private @Size(max = Constants.DB_TEXTAREA_LENGTH) String bemerkungen = "";

	@Column(nullable = false)
	private @NotNull boolean zuSpaetEingereicht;

	@Column(nullable = false)
	private @NotNull boolean minimalesEwpUnterschritten;

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

		this.bgCalculationInputAsiv = new BGCalculationInput(toCopy.bgCalculationInputAsiv);
		this.bgCalculationInputGemeinde = new BGCalculationInput(toCopy.bgCalculationInputGemeinde);
		this.bgCalculationResultAsiv = new BGCalculationResult(toCopy.getBgCalculationResultAsiv());
		if (toCopy.getBgCalculationResultGemeinde() != null) {
			this.bgCalculationResultGemeinde = new BGCalculationResult(toCopy.getBgCalculationResultGemeinde());
		}

		this.zuSpaetEingereicht = toCopy.zuSpaetEingereicht;
		this.minimalesEwpUnterschritten = toCopy.minimalesEwpUnterschritten;
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

	/* Ende Delegator-Methoden */

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
			bgCalculationInputAsiv.isSame(otherVerfuegungZeitabschnitt.getBgCalculationInputAsiv()) &&
			bgCalculationInputGemeinde.isSame(((VerfuegungZeitabschnitt) other).getBgCalculationInputGemeinde()) &&
			EbeguUtil.isSameObject(bgCalculationResultAsiv, otherVerfuegungZeitabschnitt.bgCalculationResultAsiv) &&
			EbeguUtil.isSameObject(bgCalculationResultGemeinde, otherVerfuegungZeitabschnitt.bgCalculationResultGemeinde) &&

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
			this.bgCalculationInputAsiv.isSamePersistedValues(that.bgCalculationInputAsiv) &&
			this.bgCalculationInputGemeinde.isSamePersistedValues(that.bgCalculationInputGemeinde) &&
			BGCalculationResult.isSamePersistedValues(this.bgCalculationResultAsiv, that.bgCalculationResultAsiv) &&
			BGCalculationResult.isSamePersistedValues(this.bgCalculationResultGemeinde, that.bgCalculationResultGemeinde) &&

			MathUtil.isSame(abzugFamGroesse, that.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, that.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			getGueltigkeit().compareTo(that.getGueltigkeit()) == 0 &&
			minimalesEwpUnterschritten == that.minimalesEwpUnterschritten &&
			Objects.equals(this.einkommensjahr, that.einkommensjahr);
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

	@Override
	public int compareTo(@Nonnull VerfuegungZeitabschnitt other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getGueltigkeit(), other.getGueltigkeit());
		// wenn ids nicht gleich sind wollen wir auch compare to nicht gleich
		compareToBuilder.append(this.getId(), other.getId());
		return compareToBuilder.toComparison();
	}
}
