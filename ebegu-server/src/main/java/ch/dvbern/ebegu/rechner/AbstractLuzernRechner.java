/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

public abstract class AbstractLuzernRechner extends AbstractRechner {

	protected static final MathUtil EXACT = MathUtil.EXACT;

	private BGRechnerParameterDTO inputParameter;
	private BGCalculationInput input;


	private BigDecimal inputMassgebendesEinkommen;
	private BigDecimal inputZuschlagErhoeterBeterungsbedarf = BigDecimal.ZERO;
	private boolean inputIsGeschwisternBonus2Kind = false;
	private boolean inputIsGeschwisternBonus3Kind = false;
	private boolean inputIsKitaPlusZuschlag = false;

	private BigDecimal z;

	private BigDecimal vollkosten;
	private BigDecimal selbstBehaltElternProzent;
	private BigDecimal geschwisternBonus2Kind;
	private BigDecimal geschwisternBonus3Kind;

	private BigDecimal verfuegteZeiteinheit; //Betreuungtage oder Betreuungsstunden pro Monat

	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		this.input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		this.inputParameter = parameterDTO;

		this.inputMassgebendesEinkommen = input.getMassgebendesEinkommen();
		this.inputIsKitaPlusZuschlag = input.isKitaPlusZuschlag();

		if(input.getBesondereBeduerfnisseZuschlag() != null) {
			this.inputZuschlagErhoeterBeterungsbedarf = input.getBesondereBeduerfnisseZuschlag();
		}

		this.vollkosten = calculateVollkosten();
		this.selbstBehaltElternProzent = calculateSelbstbehaltElternProzent();
		this.geschwisternBonus2Kind = calculateGeschwisternBonus2Kind();
		this.geschwisternBonus3Kind = calculateGeschwisternBonus3Kind();

		this.verfuegteZeiteinheit = calculateAnzahlZeiteiteinheitenGemaessPensumUndAnteilMonat(input.getBgPensumProzent());
		BigDecimal anspruchsberechtigteZeiteinheiten = calculateAnzahlZeiteiteinheitenGemaessPensumUndAnteilMonat(BigDecimal.valueOf(input.getAnspruchspensumProzent()));
		BigDecimal betreuungsZeiteinheiten = calculateAnzahlZeiteiteinheitenGemaessPensumUndAnteilMonat(input.getBetreuungspensumProzent());

		BigDecimal gutscheinProTagAufgrundEinkommen = calculateBGProTagByEinkommen();
		BigDecimal gutscheinProTagVorZuschlagUndSelbstbehalt = calculateGutscheinProTagVorZuschlagUndSelbstbehalt(gutscheinProTagAufgrundEinkommen);
		BigDecimal gutscheinProMonatVorZuschlagUndSelbstbehalt = EXACT.multiply(gutscheinProTagVorZuschlagUndSelbstbehalt,
			verfuegteZeiteinheit);

		BigDecimal minimalerSelbstbehalt = EXACT.multiply(getMinimalTarif(), verfuegteZeiteinheit);
		BigDecimal selbstbehaltDerEltern = calculateEffektiverSelbstbehaltEltern(gutscheinProMonatVorZuschlagUndSelbstbehalt, minimalerSelbstbehalt);

		BigDecimal gutscheinProMonatVorZuschlag = EXACT.subtract(gutscheinProMonatVorZuschlagUndSelbstbehalt, selbstbehaltDerEltern);
		BigDecimal gutscheinProMonat = calculateGutscheinProMonat(gutscheinProMonatVorZuschlag);

		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(this.input, result);

		//TODO Werte und Naming passen Teilweise nicht zu bestehendem Result. Es wird noch abgeklärt wie Luzern das genau haben will
		result.setVollkosten(this.vollkosten);
		result.setMinimalerElternbeitrag(minimalerSelbstbehalt);
		result.setElternbeitrag(selbstbehaltDerEltern);
		result.setMinimalerElternbeitragGekuerzt(selbstbehaltDerEltern);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(gutscheinProMonatVorZuschlagUndSelbstbehalt);
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(gutscheinProMonatVorZuschlag);
		result.setVerguenstigung(gutscheinProMonat);
		result.setZeiteinheit(getZeiteinheit());
		result.setBetreuungspensumZeiteinheit(betreuungsZeiteinheiten);
		result.setBgPensumZeiteinheit(verfuegteZeiteinheit);
		result.setAnspruchspensumZeiteinheit(anspruchsberechtigteZeiteinheiten);

		result.roundAllValues();
		verfuegungZeitabschnitt.setBgCalculationResultAsiv(result);
		verfuegungZeitabschnitt.setBgCalculationResultGemeinde(result);
	}

	private BigDecimal calculateVollkosten() {
		BigDecimal betreuungspensum = input.getBetreuungspensumProzent();
		BigDecimal anspruchsPensum = BigDecimal.valueOf(input.getAnspruchspensumProzent());

		//wenn anspruchspensum < betreuungspensum, dann anspruchspensum/betreuungspensum * monatlicheBetreuungskosten
		if(anspruchsPensum.compareTo(betreuungspensum) < 0) {
			BigDecimal anspruchsPensumDevidedByBetreuungspensum = EXACT.divide(anspruchsPensum, betreuungspensum);
			return EXACT.multiply(anspruchsPensumDevidedByBetreuungspensum, input.getMonatlicheBetreuungskosten());
		}

		return input.getMonatlicheBetreuungskosten();
	}

	private BigDecimal calculateGeschwisternBonus2Kind() {
		//SelbstbehaltElternProzent * 50% * VollkostenTarif
		return EXACT.multiplyNullSafe(this.selbstBehaltElternProzent, BigDecimal.valueOf(0.5), getVollkostenTarif());
	}

	private BigDecimal calculateGeschwisternBonus3Kind() {
		//SelbstbehaltElternProzent * 70% * VollkostenTarif
		return EXACT.multiplyNullSafe(this.selbstBehaltElternProzent, BigDecimal.valueOf(0.7), getVollkostenTarif());
	}

	private BigDecimal calculateAnzahlZeiteiteinheitenGemaessPensumUndAnteilMonat(BigDecimal pensum) {
		BigDecimal anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(
			this.input.getParent().getGueltigkeit().getGueltigAb(),
			this.input.getParent().getGueltigkeit().getGueltigBis());
		return EXACT.multiply(getAnzahlZeiteinheitenProMonat(), BigDecimal.valueOf(0.01), pensum, anteilMonat);
	}

	private BigDecimal calculateGutscheinProTagVorZuschlagUndSelbstbehalt(BigDecimal gutscheinProTagAufgrundEinkommen) {
		BigDecimal gutscheinProTagVorZuschlagUndSelbstbahalt = gutscheinProTagAufgrundEinkommen.add(BigDecimal.ZERO);

		if(inputIsGeschwisternBonus2Kind) {
			gutscheinProTagVorZuschlagUndSelbstbahalt = gutscheinProTagVorZuschlagUndSelbstbahalt.add(geschwisternBonus2Kind);
		}

		if(inputIsGeschwisternBonus3Kind) {
			gutscheinProTagVorZuschlagUndSelbstbahalt = gutscheinProTagVorZuschlagUndSelbstbahalt.add(geschwisternBonus3Kind);
		}

		return gutscheinProTagVorZuschlagUndSelbstbahalt;
	}

	/**
	 * Berechnet den prozentualen Selbstbehalt der Eltern gemäss der Formel:
	 * returns (MinimalTarif / VollkostenTarif) + (z * (MassgebendesEinkommen-MinimalMassgebendesEinkommen))
	 */

	protected BigDecimal calculateSelbstbehaltProzentenGemaessFormel() {
		BigDecimal diffMassgebendesEkMinEk = EXACT.divide(getMinimalTarif(), getVollkostenTarif());
		BigDecimal massgebendesEkMinusMinEk = EXACT.subtract(inputMassgebendesEinkommen, inputParameter.getMinMassgebendesEinkommen());
		BigDecimal rateEinkommen = EXACT.multiply(getZ(), massgebendesEkMinusMinEk);
		return EXACT.add(diffMassgebendesEkMinEk, rateEinkommen);
	}

	/**
	 * Berechnet den effektiven Selbstbehalt der Eltern in Franken.
	 *
	 * @param gutscheinProMonatVorZuschlagUndSelbstbehalt
	 * @param minimalerSelbstbehalt
	 * @return
	 */
	protected BigDecimal calculateEffektiverSelbstbehaltEltern(BigDecimal gutscheinProMonatVorZuschlagUndSelbstbehalt, BigDecimal minimalerSelbstbehalt) {
		BigDecimal differenzVollkostenUndGutschein = EXACT.subtract(vollkosten,gutscheinProMonatVorZuschlagUndSelbstbehalt);

		//Wenn Differenz Vollkosten und Gutschein<Minimaler Selbstbehalt, wird zusätzlicher Selbstbehalt abgezogen
		BigDecimal zusaetzlicherSelbstbehalt = BigDecimal.ZERO;

		if(differenzVollkostenUndGutschein.compareTo(minimalerSelbstbehalt) < 0) {
			zusaetzlicherSelbstbehalt = EXACT.subtract(minimalerSelbstbehalt, differenzVollkostenUndGutschein);
		}

		return zusaetzlicherSelbstbehalt;
	}

	/**
	 * Berechnet den Gutschein pro Tag aufgrund des Einkommens nach
	 *
	 * returns 0, wenn selbstBehaltElternProzent > 100 %
	 *
	 * returns bgProTag
	 * wenn bgProTag > minBetreuungsgutschein
	 * sonst minBetreuungsgutschein
	 *
	 * formel bgProTag = vollkostenTarif * (1-selbstBehaltElternProzent)
	 */
	protected BigDecimal calculateBetreuungsgutscheinProTagAuftrungEinkommenGemaessFormel() {
		if(selbstBehaltElternProzent.compareTo(BigDecimal.ONE) > 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal einsMinusSelbstbehalt = EXACT.subtract(BigDecimal.ONE, selbstBehaltElternProzent);
		BigDecimal bgProTag = EXACT.multiply(getVollkostenTarif(), einsMinusSelbstbehalt);

		if(bgProTag.compareTo(getMinBetreuungsgutschein()) > 0) {
			return bgProTag;
		}

		return getMinBetreuungsgutschein();
	}

	private BigDecimal calculateGutscheinProMonat(BigDecimal gutscheinProMonatVorZuschlag) {
		BigDecimal zuschlagProTag = inputZuschlagErhoeterBeterungsbedarf;
		BigDecimal zuschlagKitaPlus = inputIsKitaPlusZuschlag ? getKitaPlusZuschlag() : BigDecimal.ZERO;

		BigDecimal totalZuschlagProTag = EXACT.add(zuschlagProTag, zuschlagKitaPlus);
		BigDecimal totalZuschlagProMonat = EXACT.multiply(verfuegteZeiteinheit, totalZuschlagProTag);
		return EXACT.add(gutscheinProMonatVorZuschlag, totalZuschlagProMonat);
	}

	protected BigDecimal getZ() {
		if(z == null) {
			z = calculateZ();
		}

		return z;
	}

	/**
	 * Formel um z zu Berechnen =
	 * 1-(minimaltarif / Vollkostentarif) / (maxMassgebendesEinkommen - minMassgebendesEinkommen)
	 */
	protected BigDecimal calculateZ() {
		BigDecimal rateTarife = EXACT.divide(getMinimalTarif(), getVollkostenTarif());
		BigDecimal diffEinkommen = EXACT.subtract(inputParameter.getMaxMassgebendesEinkommen(), inputParameter.getMinMassgebendesEinkommen());

		BigDecimal diffTarife1 = EXACT.subtract(BigDecimal.ONE, rateTarife);

		return EXACT.divide(diffTarife1, diffEinkommen);
	}

	protected BGRechnerParameterDTO getInputParameter() {
		return this.inputParameter;
	}

	protected BigDecimal getInputMassgebendesEinkommen() {
		return this.inputMassgebendesEinkommen;
	}

	protected BigDecimal getSelbstBehaltElternProzent() {
		return this.selbstBehaltElternProzent;
	}

	protected abstract BigDecimal getMinimalTarif();
	protected abstract PensumUnits getZeiteinheit();
	protected abstract BigDecimal getVollkostenTarif();
	protected abstract BigDecimal getKitaPlusZuschlag();
	protected abstract BigDecimal getMinBetreuungsgutschein();
	protected abstract BigDecimal getAnzahlZeiteinheitenProMonat();
	protected abstract BigDecimal calculateSelbstbehaltElternProzent();
	protected abstract BigDecimal calculateBGProTagByEinkommen();
}
