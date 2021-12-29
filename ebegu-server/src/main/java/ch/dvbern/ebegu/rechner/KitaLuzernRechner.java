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
import ch.dvbern.ebegu.util.MathUtil;

public class KitaLuzernRechner extends AbstractRechner {

	private static final MathUtil EXACT = MathUtil.EXACT;


	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		AbstractKitaLuzernRecher rechner = verfuegungZeitabschnitt.getBgCalculationInputAsiv().isBabyTarif() ?
			new KitaLuzernBabyRechner(parameterDTO, verfuegungZeitabschnitt.getBgCalculationInputAsiv()) :
			new KitaLuzernKindRechner(parameterDTO, verfuegungZeitabschnitt.getBgCalculationInputAsiv());

		BGCalculationResult result = rechner.calculateResult();
		result.roundAllValues();
		verfuegungZeitabschnitt.setBgCalculationResultAsiv(result);

	}

	private abstract static class AbstractKitaLuzernRecher {

		private BGCalculationResult result;
		protected final BGRechnerParameterDTO inputParameter;

		private static final BigDecimal MAX_BETREUNGSTAGE_PRO_WOCHE = BigDecimal.valueOf(5);
		private static final BigDecimal WOCHEN_PRO_MONAT = BigDecimal.valueOf(4.1);

		private BigDecimal inputVollkosten;
		private BigDecimal inputBetreuungsPensum;
		private int inputAnspruchPensum;
		protected BigDecimal inputMassgebendesEinkommen;
		private BigDecimal inputZuschlagErhoeterBeterungsbedarf = BigDecimal.ZERO;
		private boolean inputKitaPlusZuschlag = false;
		private boolean inputGeschwisternBonus2Kind = false;
		private boolean inputGeschwisternBonus3Kind = false;

		protected BigDecimal selbstBehaltElternProzent;
		private BigDecimal geschwisternBonus2Kind;
		private BigDecimal geschwisternBonus3Kind;
		private BigDecimal betreuungsgutscheinPensumProzent;
		private BigDecimal effektiveBetreuungstageProWoche;
		private BigDecimal effektiveBetreuungstageProMonat;
		private BigDecimal z;

		AbstractKitaLuzernRecher(BGRechnerParameterDTO rechnerParameterDTO, BGCalculationInput input) {
			this.inputParameter = rechnerParameterDTO;
			initCalculationResult(input);
			initInputs(input);
		}

		private void initCalculationResult(BGCalculationInput input) {
			this.result = new BGCalculationResult();
			VerfuegungZeitabschnitt.initBGCalculationResult(input, this.result);
		}

		private void initInputs(BGCalculationInput input) {
			this.inputVollkosten = input.getMonatlicheBetreuungskosten();
			this.inputBetreuungsPensum = input.getBetreuungspensumProzent();
			this.inputAnspruchPensum = input.getAnspruchspensumProzent();
			this.inputMassgebendesEinkommen = input.getMassgebendesEinkommen();
			this.inputKitaPlusZuschlag = input.isKitaPlusZuschlag();
		}

		private BGCalculationResult calculateResult() {
			doCalculations();

			BigDecimal gutscheinProTagAufgrundEinkommen = calculateBGProTagByEinkommen();
			BigDecimal gutscheinProTagVorZuschlagUndSelbstbehalt = calculateGutscheinProTagVorZuschlagUndSelbstbehalt(gutscheinProTagAufgrundEinkommen);
			BigDecimal gutscheinProMonatVorZuschlagUndSelbstbehalt = EXACT.multiply(gutscheinProTagVorZuschlagUndSelbstbehalt, effektiveBetreuungstageProMonat);

			BigDecimal minimalerSelbstbehalt = EXACT.multiply(inputParameter.getMinVerguenstigungProTg(), effektiveBetreuungstageProMonat);
			BigDecimal selbstbehaltDerEltern = calculateSelbstbehaltEltern(gutscheinProMonatVorZuschlagUndSelbstbehalt, minimalerSelbstbehalt);

			BigDecimal gutscheinProMonatVorZuschlag = EXACT.subtract(gutscheinProMonatVorZuschlagUndSelbstbehalt, selbstbehaltDerEltern);
			BigDecimal gutscheinProMonat = calculateGutscheinProMonat(gutscheinProMonatVorZuschlag);

			this.result.setVollkosten(this.inputVollkosten);
			this.result.setMinimalerElternbeitrag(minimalerSelbstbehalt);
			this.result.setElternbeitrag(selbstbehaltDerEltern);
			this.result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(gutscheinProMonatVorZuschlagUndSelbstbehalt);
			this.result.setVerguenstigungOhneBeruecksichtigungVollkosten(gutscheinProMonatVorZuschlag);
			this.result.setVerguenstigung(gutscheinProMonat);
			this.result.setBetreuungspensumZeiteinheit(effektiveBetreuungstageProMonat);
			return this.result;
		}

		private BigDecimal calculateGutscheinProMonat(BigDecimal gutscheinProMonatVorZuschlag) {
			BigDecimal zuschlagProTag = inputZuschlagErhoeterBeterungsbedarf;
			BigDecimal zuschlagKitaPlus = inputKitaPlusZuschlag ? inputParameter.getKitaPlusZuschlag() : BigDecimal.ZERO;

			BigDecimal totalZuschlagProTag = EXACT.add(zuschlagProTag, zuschlagKitaPlus);
			BigDecimal totalZuschlagProMonat = EXACT.multiply(effektiveBetreuungstageProMonat, totalZuschlagProTag);
			return EXACT.add(gutscheinProMonatVorZuschlag, totalZuschlagProMonat);
		}

		private BigDecimal calculateSelbstbehaltEltern(BigDecimal gutscheinProMonatVorZuschlagUndSelbstbehalt, BigDecimal minimalerSelbstbehalt) {
			BigDecimal differenzVollkostenUndGutschein = EXACT.subtract(inputVollkosten,gutscheinProMonatVorZuschlagUndSelbstbehalt);

			//Wenn Differenz Vollkosten und Gutschein<Minimaler Selbstbehalt, wird zusätzlicher Selbstbehalt abgezogen
			BigDecimal zusaetzlicherSelbstbehalt = BigDecimal.ZERO;

			if(differenzVollkostenUndGutschein.compareTo(minimalerSelbstbehalt) < 0) {
				zusaetzlicherSelbstbehalt = EXACT.subtract(minimalerSelbstbehalt, differenzVollkostenUndGutschein);
			}

			return zusaetzlicherSelbstbehalt;
		}


		private BigDecimal calculateGutscheinProTagVorZuschlagUndSelbstbehalt(BigDecimal gutscheinProTagAufgrundEinkommen) {
			BigDecimal gutscheinProTagVorZuschlagUndSelbstbahalt = gutscheinProTagAufgrundEinkommen.add(BigDecimal.ZERO);

			if(inputGeschwisternBonus2Kind) {
				gutscheinProTagVorZuschlagUndSelbstbahalt.add(geschwisternBonus2Kind);
			}

			if(inputGeschwisternBonus3Kind) {
				gutscheinProTagVorZuschlagUndSelbstbahalt.add(geschwisternBonus3Kind);
			}

			return gutscheinProTagVorZuschlagUndSelbstbahalt;
		}

		private void doCalculations() {
			this.selbstBehaltElternProzent = calculateSelbstbehaltElternProzent();
			this.geschwisternBonus2Kind = calculateGeschwisternBonus2Kind();
			this.geschwisternBonus3Kind = calculateGeschwisternBonus3Kind();
			this.betreuungsgutscheinPensumProzent = inputBetreuungsPensum.min(BigDecimal.valueOf(inputAnspruchPensum));
			this.effektiveBetreuungstageProWoche = calculateEffektiveBetreuungstageProWoche();
			this.effektiveBetreuungstageProMonat = calculateEffektiveBetreuungstageProMonat();
		}

		private BigDecimal calculateGeschwisternBonus2Kind() {
			//SelbstbehaltElternProzent * 50% * VollkostenTarif
			return EXACT.multiplyNullSafe(this.selbstBehaltElternProzent, BigDecimal.valueOf(0.5), getVollkostenTarif());
		}

		private BigDecimal calculateGeschwisternBonus3Kind() {
			//SelbstbehaltElternProzent * 70% * VollkostenTarif
			return EXACT.multiplyNullSafe(this.selbstBehaltElternProzent, BigDecimal.valueOf(0.7), getVollkostenTarif());
		}

		private BigDecimal calculateEffektiveBetreuungstageProWoche() {
			return EXACT.multiplyNullSafe(MAX_BETREUNGSTAGE_PRO_WOCHE,betreuungsgutscheinPensumProzent,BigDecimal.valueOf(0.01));
		}

		private BigDecimal calculateEffektiveBetreuungstageProMonat() {
			return EXACT.multiply(WOCHEN_PRO_MONAT, effektiveBetreuungstageProWoche);
		}

		protected BigDecimal getZ() {
			if(z == null) {
				z = calculateZ();
			}

			return z;
		}

		protected BigDecimal calculateSelbstbehaltElternGemaessFormel() {
			BigDecimal diffMassgebendesEkMinEk = EXACT.divide(inputParameter.getMinVerguenstigungProTg(), getVollkostenTarif());
			BigDecimal massgebendesEkMinusMinEk = EXACT.subtract(inputMassgebendesEinkommen, inputParameter.getMinMassgebendesEinkommen());
			BigDecimal rateEinkommen = EXACT.multiply(getZ(), massgebendesEkMinusMinEk);
			return EXACT.add(diffMassgebendesEkMinEk, rateEinkommen);
		}

		/**
		 * Formel um z zu Berechnen =
		 * 1-(minimaltarif / Vollkostentarif) / (maxMassgebendesEinkommen - minMassgebendesEinkommen)
		 */
		private BigDecimal calculateZ() {
			BigDecimal rateTarife = EXACT.divide(inputParameter.getMinVerguenstigungProTg(), getVollkostenTarif());
			BigDecimal diffEinkommen = EXACT.subtract(inputParameter.getMaxMassgebendesEinkommen(), inputParameter.getMinMassgebendesEinkommen());

			BigDecimal diffTarife1 = EXACT.subtract(BigDecimal.ONE, rateTarife);

			return EXACT.divide(diffTarife1, diffEinkommen);
		}

		abstract BigDecimal getVollkostenTarif();
		abstract BigDecimal calculateSelbstbehaltElternProzent();
		abstract BigDecimal getMinBetreuungsgutschein();
		abstract BigDecimal calculateBGProTagByEinkommen();
	}

	private static class KitaLuzernBabyRechner extends AbstractKitaLuzernRecher {

		KitaLuzernBabyRechner(BGRechnerParameterDTO rechnerParameterDTO, BGCalculationInput input) {
			super(rechnerParameterDTO, input);
		}

		/**
		 * Berechnet den Selbstbehalt der Eltern
		 *
		 * returns (MinimalTarif / VollkostenTarif) + (z * (MassgebendesEinkommen-MinimalMassgebendesEinkommen))
		 * wenn MassgebendesEinkommen <= MaximalMasgebendesEinkommen,
		 *
		 * returns 101, wenn MassgebendesEinkommen > MaximalMasgebendesEinkommen:
		 */
		@Override
		BigDecimal calculateSelbstbehaltElternProzent() {
			if(inputMassgebendesEinkommen.compareTo(inputParameter.getMaxMassgebendesEinkommen()) > 0) {
				return BigDecimal.valueOf(101);
			}

			return calculateSelbstbehaltElternGemaessFormel();
		}

		/**
		 * Berechnet den Gutschein pro Tag aufgrund des Einkommens nach
		 *
		 * returns 0, wenn selbstBehaltElternProzent > 100
		 *
		 * returns bgProTag
		 * wenn bgProTag > minBetreuungsgutschein
		 * sonst minBetreuungsgutschein
		 *
		 * formel bgProTag = vollkostenTarif * (1-selbstBehaltElternProzent)
		 */
		@Override
		BigDecimal calculateBGProTagByEinkommen() {
			if(selbstBehaltElternProzent.compareTo(BigDecimal.valueOf(100)) > 0) {
				return BigDecimal.ZERO;
			}

			BigDecimal einsMinusSelbstbehalt = EXACT.subtract(BigDecimal.ONE, selbstBehaltElternProzent);
			BigDecimal bgProTag = EXACT.multiply(getVollkostenTarif(), einsMinusSelbstbehalt);

			if(bgProTag.compareTo(getMinBetreuungsgutschein()) > 0) {
				return bgProTag;
			}

			return getMinBetreuungsgutschein();
		}

		@Override
		BigDecimal getVollkostenTarif() {
			return this.inputParameter.getMaxVerguenstigungVorschuleBabyProTg();
		}

		@Override
		BigDecimal getMinBetreuungsgutschein() {
			return this.inputParameter.getMinBGBaby();
		}

	}

	private static class KitaLuzernKindRechner extends  AbstractKitaLuzernRecher {

		KitaLuzernKindRechner(BGRechnerParameterDTO rechnerParameterDTO, BGCalculationInput input) {
			super(rechnerParameterDTO, input);
		}

		@Override
		BigDecimal getVollkostenTarif() {
			return this.inputParameter.getMaxVerguenstigungVorschuleKindProTg();
		}

		@Override
		BigDecimal getMinBetreuungsgutschein() {
			return this.inputParameter.getMinBGKind();
		}

		/**
		 * Berechnet den Selbstbehalt der Eltern
		 *
		 * returns selbstbehaltDerEltern (gemässFormel)
		 * wenn selbstbehaltDerEltern (gemässFormel) <= 100%,
		 *
		 * returns 100, wenn selbstbehaltDerEltern (gemässFormel) > 100%:
		 */
		@Override
		BigDecimal calculateSelbstbehaltElternProzent() {
			BigDecimal selbstbehaltGemässFormel = calculateSelbstbehaltElternGemaessFormel();

			if(selbstbehaltGemässFormel.compareTo(BigDecimal.valueOf(100)) > 0) {
				return BigDecimal.valueOf(100);
			}

			return selbstbehaltGemässFormel;
		}


		/**
		 * Berechnet den Gutschein pro Tag aufgrund des Einkommens nach
		 *
		 * returns bgProTag, wenn bgProTag > minBetreuungsgutschein, sonst
		 * returns minBetreuungsgutschein, wenn massgebendes Einkomen <= maxMassgebendesEinkommen, sonst
		 * returns 0
		 *
		 * formel bgProTag = vollkostenTarif * (1-selbstBehaltElternProzent)
		 */
		@Override
		BigDecimal calculateBGProTagByEinkommen() {
			BigDecimal einsMinusSelbstbehalt = EXACT.subtract(BigDecimal.ONE, selbstBehaltElternProzent);
			BigDecimal bgProTag = EXACT.multiply(getVollkostenTarif(), einsMinusSelbstbehalt);

			if(bgProTag.compareTo(getMinBetreuungsgutschein()) > 0) {
				return bgProTag;
			}

			if(inputMassgebendesEinkommen.compareTo(inputParameter.getMaxMassgebendesEinkommen()) <= 0) {
				return getMinBetreuungsgutschein();
			}

			return BigDecimal.ZERO;
		}
	}
}
