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
import ch.dvbern.ebegu.util.MathUtil;

public abstract class AbstractLuzernRechner extends AbstractRechner {

	protected static final MathUtil EXACT = MathUtil.EXACT;

	private static final BigDecimal MAX_BETREUNGSTAGE_PRO_WOCHE = BigDecimal.valueOf(5);
	protected static final BigDecimal WOCHEN_PRO_MONAT = BigDecimal.valueOf(4.1);

	protected BGRechnerParameterDTO inputParameter;
	private BGCalculationInput input;


	private BigDecimal inputVollkosten;
	private BigDecimal inputBetreuungsPensum;
	private int inputAnspruchPensum;
	protected BigDecimal inputMassgebendesEinkommen;
	private BigDecimal inputZuschlagErhoeterBeterungsbedarf = BigDecimal.ZERO;
	private boolean inputGeschwisternBonus2Kind = false;
	private boolean inputGeschwisternBonus3Kind = false;
	private boolean inputKitaPlusZuschlag = false;

	private BigDecimal z;

	protected BigDecimal selbstBehaltElternProzent;
	private BigDecimal geschwisternBonus2Kind;
	private BigDecimal geschwisternBonus3Kind;
	private BigDecimal betreuungsgutscheinPensumProzent;
	private BigDecimal effektiveBetreuungstageProWoche;
	private BigDecimal effektiveBetreuungZeiteinheitProMonat; //Betreuungtage oder Betreuungsstunden pro Monat

	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		this.input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		this.inputParameter = parameterDTO;

		this.inputVollkosten = input.getMonatlicheBetreuungskosten();
		this.inputBetreuungsPensum = input.getBetreuungspensumProzent();
		this.inputAnspruchPensum = input.getAnspruchspensumProzent();
		this.inputMassgebendesEinkommen = input.getMassgebendesEinkommen();
		this.inputKitaPlusZuschlag = input.isKitaPlusZuschlag();

		this.selbstBehaltElternProzent = calculateSelbstbehaltElternProzent();
		this.geschwisternBonus2Kind = calculateGeschwisternBonus2Kind();
		this.geschwisternBonus3Kind = calculateGeschwisternBonus3Kind();
		this.betreuungsgutscheinPensumProzent = inputBetreuungsPensum.min(BigDecimal.valueOf(inputAnspruchPensum));
		this.effektiveBetreuungstageProWoche = calculateEffektiveBetreuungstageProWoche();
		this.effektiveBetreuungZeiteinheitProMonat = calculateEffektiveBetreuungszeiteinheitenProMonat();

		BigDecimal gutscheinProTagAufgrundEinkommen = calculateBGProTagByEinkommen();
		BigDecimal gutscheinProTagVorZuschlagUndSelbstbehalt = calculateGutscheinProTagVorZuschlagUndSelbstbehalt(gutscheinProTagAufgrundEinkommen);
		BigDecimal gutscheinProMonatVorZuschlagUndSelbstbehalt = EXACT.multiply(gutscheinProTagVorZuschlagUndSelbstbehalt,
			effektiveBetreuungZeiteinheitProMonat);

		BigDecimal minimalerSelbstbehalt = EXACT.multiply(getMinimalTarif(), effektiveBetreuungZeiteinheitProMonat);
		BigDecimal selbstbehaltDerEltern = calculateEffektiverSelbstbehaltEltern(gutscheinProMonatVorZuschlagUndSelbstbehalt, minimalerSelbstbehalt);

		BigDecimal gutscheinProMonatVorZuschlag = EXACT.subtract(gutscheinProMonatVorZuschlagUndSelbstbehalt, selbstbehaltDerEltern);
		BigDecimal gutscheinProMonat = calculateGutscheinProMonat(gutscheinProMonatVorZuschlag);

		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(this.input, result);

		result.setVollkosten(this.inputVollkosten);
		result.setMinimalerElternbeitrag(minimalerSelbstbehalt);
		result.setElternbeitrag(selbstbehaltDerEltern);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(gutscheinProMonatVorZuschlagUndSelbstbehalt);
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(gutscheinProMonatVorZuschlag);
		result.setVerguenstigung(gutscheinProMonat);
		result.setZeiteinheit(getZeiteinheit());
		result.setBetreuungspensumZeiteinheit(effektiveBetreuungZeiteinheitProMonat);

		result.roundAllValues();
		verfuegungZeitabschnitt.setBgCalculationResultAsiv(result);

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

	private BigDecimal calculateEffektiveBetreuungszeiteinheitenProMonat() {
		return EXACT.multiply(getAnzahlZeiteinheitenProMonat(), effektiveBetreuungstageProWoche);
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
		BigDecimal differenzVollkostenUndGutschein = EXACT.subtract(inputVollkosten,gutscheinProMonatVorZuschlagUndSelbstbehalt);

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
		BigDecimal zuschlagKitaPlus = inputKitaPlusZuschlag ? inputParameter.getKitaPlusZuschlag() : BigDecimal.ZERO;

		BigDecimal totalZuschlagProTag = EXACT.add(zuschlagProTag, zuschlagKitaPlus);
		BigDecimal totalZuschlagProMonat = EXACT.multiply(effektiveBetreuungZeiteinheitProMonat, totalZuschlagProTag);
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

	protected abstract BigDecimal getMinimalTarif();
	protected abstract PensumUnits getZeiteinheit();
	protected abstract BigDecimal getVollkostenTarif();
	protected abstract BigDecimal getMinBetreuungsgutschein();
	protected abstract BigDecimal getAnzahlZeiteinheitenProMonat();
	protected abstract BigDecimal calculateSelbstbehaltElternProzent();
	protected abstract BigDecimal calculateBGProTagByEinkommen();

}
