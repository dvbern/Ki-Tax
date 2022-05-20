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
	protected BGCalculationInput input;


	private BigDecimal inputMassgebendesEinkommen;
	private BigDecimal inputZuschlagErhoeterBeterungsbedarf = BigDecimal.ZERO;
	private boolean inputIsGeschwisternBonus2Kind = false;
	private boolean inputIsGeschwisternBonus3Kind = false;
	private boolean inputIsKitaPlusZuschlag = false;

	private BigDecimal z;

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
		this.inputIsGeschwisternBonus2Kind = input.isGeschwisternBonusKind2();
		this.inputIsGeschwisternBonus3Kind = input.isGeschwisternBonusKind3();

		if(input.getBesondereBeduerfnisseZuschlag() != null) {
			this.inputZuschlagErhoeterBeterungsbedarf = input.getBesondereBeduerfnisseZuschlag();
		}

		this.z = calculateZ();

		this.selbstBehaltElternProzent = calculateSelbstbehaltElternProzent();
		this.geschwisternBonus2Kind = calculateGeschwisternBonus2Kind();
		this.geschwisternBonus3Kind = calculateGeschwisternBonus3Kind();

		this.verfuegteZeiteinheit = calculateAnzahlZeiteiteinheitenGemaessPensumUndAnteilMonat(input.getBgPensumProzent());
		BigDecimal anspruchsberechtigteZeiteinheiten = calculateAnzahlZeiteiteinheitenGemaessPensumUndAnteilMonat(BigDecimal.valueOf(input.getAnspruchspensumProzent()));
		BigDecimal betreuungsZeiteinheiten = calculateAnzahlZeiteiteinheitenGemaessPensumUndAnteilMonat(input.getBetreuungspensumProzent());

		BigDecimal gutscheinProZeieinheitAufgrundEinkommen = calculateBGProZeiteinheitByEinkommen();
		BigDecimal gutscheinProZeiteinheitVorZuschlagUndSelbstbehalt = calculateGutscheinProZeiteinheitVorZuschlagUndSelbstbehalt(gutscheinProZeieinheitAufgrundEinkommen);
		BigDecimal gutscheinProMonatVorZuschlagUndSelbstbehalt = EXACT.multiply(gutscheinProZeiteinheitVorZuschlagUndSelbstbehalt,
			verfuegteZeiteinheit);

		BigDecimal vollkostenGekuerzt = calculateVollkosten(this.verfuegteZeiteinheit);
		BigDecimal differenzVollkostenUndGutschein = EXACT.subtract(vollkostenGekuerzt,gutscheinProMonatVorZuschlagUndSelbstbehalt);
		BigDecimal minimalerSelbstbehalt = EXACT.multiply(getMinimalTarif(), verfuegteZeiteinheit);
		BigDecimal selbstbehaltDerEltern = calculateEffektiverSelbstbehaltEltern(differenzVollkostenUndGutschein, minimalerSelbstbehalt);

		BigDecimal gutscheinProMonatGekuerzt = calculateGutscheinProMonatGekuerzt(differenzVollkostenUndGutschein, gutscheinProMonatVorZuschlagUndSelbstbehalt);
		BigDecimal zuschlagProMonat = calculateZuschlagProMonat();
		BigDecimal guscheinProMonatGekuerztInklZuschlag = EXACT.add(gutscheinProMonatGekuerzt, zuschlagProMonat);
		BigDecimal gutscheinProMonatVorAbzugSelbstbehalt
			= EXACT.add(gutscheinProMonatVorZuschlagUndSelbstbehalt, zuschlagProMonat);
		BigDecimal gutscheinProMonat =  EXACT.subtract(gutscheinProMonatVorAbzugSelbstbehalt, selbstbehaltDerEltern);
		BigDecimal gutscheinProZeiteinheit = calculateGutscheinProZeiteinheit(gutscheinProMonat);

		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(this.input, result);

		result.setVollkosten(vollkostenGekuerzt);
		result.setMinimalerElternbeitrag(minimalerSelbstbehalt);
		result.setElternbeitrag(selbstbehaltDerEltern);
		result.setMinimalerElternbeitragGekuerzt(minimalerSelbstbehalt);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(guscheinProMonatGekuerztInklZuschlag);
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(gutscheinProMonatVorAbzugSelbstbehalt);
		result.setVerguenstigung(gutscheinProMonat);
		result.setZeiteinheit(getZeiteinheit());
		result.setBetreuungspensumZeiteinheit(betreuungsZeiteinheiten);
		result.setBgPensumZeiteinheit(verfuegteZeiteinheit);
		result.setAnspruchspensumZeiteinheit(anspruchsberechtigteZeiteinheiten);
		result.setVerguenstigungProZeiteinheit(gutscheinProZeiteinheit);

		result.roundAllValues();
		verfuegungZeitabschnitt.setBgCalculationResultAsiv(result);
		verfuegungZeitabschnitt.setBgCalculationResultGemeinde(result);
	}

	private BigDecimal calculateGutscheinProZeiteinheit(BigDecimal gutscheinProMonat) {
		if (gutscheinProMonat.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		return EXACT.divide(gutscheinProMonat, this.verfuegteZeiteinheit);
	}

	protected BigDecimal calculateGutscheinProMonatGekuerzt(BigDecimal differenzVollkostenUndGutschein, BigDecimal gutscheinProMonatVorZuschlagUndSelbstbehalt) {
		if(differenzVollkostenUndGutschein.compareTo(BigDecimal.ZERO) < 0) {
			return EXACT.add(gutscheinProMonatVorZuschlagUndSelbstbehalt, differenzVollkostenUndGutschein);
		}

		return gutscheinProMonatVorZuschlagUndSelbstbehalt;
	}

	protected abstract BigDecimal calculateVollkosten(BigDecimal verfuegteZeiteinheiten);

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

	private BigDecimal calculateGutscheinProZeiteinheitVorZuschlagUndSelbstbehalt(BigDecimal gutscheinProTagAufgrundEinkommen) {
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
	protected BigDecimal calculateEffektiverSelbstbehaltEltern(BigDecimal differenzVollkostenUndGutschein, BigDecimal minimalerSelbstbehalt) {
		//Wenn Differenz Vollkosten und Gutschein<Minimaler Selbstbehalt, wird zusätzlicher Selbstbehalt abgezogen
		BigDecimal zusaetzlicherSelbstbehalt = BigDecimal.ZERO;

		if (differenzVollkostenUndGutschein.compareTo(minimalerSelbstbehalt) < 0) {
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
	protected BigDecimal calculateBetreuungsgutscheinProZeiteinheitAufgrundEinkommenGemaessFormel() {
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

	protected BigDecimal getMaximalWertBGProTagAufgrundEinkommen() {
		return EXACT.subtract(getVollkostenTarif(), getMinimalTarif());
	}

	private BigDecimal calculateZuschlagProMonat() {
		BigDecimal zuschlagProTag = inputZuschlagErhoeterBeterungsbedarf;
		BigDecimal zuschlagKitaPlus = inputIsKitaPlusZuschlag ? getKitaPlusZuschlag() : BigDecimal.ZERO;

		BigDecimal totalZuschlagProTag = EXACT.add(zuschlagProTag, zuschlagKitaPlus);
		return EXACT.multiply(verfuegteZeiteinheit, totalZuschlagProTag);
	}

	protected BigDecimal getZ() {
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
	protected abstract BigDecimal calculateBGProZeiteinheitByEinkommen();
}
