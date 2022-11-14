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
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.MathUtil;

public class KitaLuzernRechner extends AbstractLuzernRechner {

	private boolean isBaby = false;

	//Die Tarife werden im Moment als Konstante gespeichert. Dies wird in Zukunft evtl noch konfigurierbar gemacht.
	private static final BigDecimal VOLLKOSTEN_TARIF_BABY = BigDecimal.valueOf(160);
	private static final BigDecimal VOLLKOSTEN_TARIF_KIND = BigDecimal.valueOf(130);
	private static final BigDecimal MIN_BETREUUNGSGUTSCHEIN_BABY = BigDecimal.valueOf(12.60);
	private static final BigDecimal MIN_BETREUUNGSGUTSCHEIN_KIND = BigDecimal.valueOf(10);

	private static final BigDecimal KITA_PLUS_ZUSCHLAG = BigDecimal.valueOf(32);

	protected KitaLuzernRechner(List<RechnerRule> rechnerRulesForGemeinde) {
		super(rechnerRulesForGemeinde);
	}

	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		BGCalculationInput bgCalculationInput = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		this.isBaby = bgCalculationInput.isBabyTarif();

		super.calculate(verfuegungZeitabschnitt, parameterDTO);
	}

	@Override
	protected BigDecimal calculateVollkostenProZeitabschnitt(BigDecimal vollkostenGekuerzt) {
		//Bei KITA Rechner wurden die Vollkosten bereits pro Zeitabschnitt berechnet
		return vollkostenGekuerzt;
	}

	@Override
	protected BigDecimal calculateGutscheinProZeitabschnitt(BigDecimal gutschein) {
		//Bei KITA Rechner wird der Gutschein schon pro Zeitabschnitt berechnet
		return gutschein;
	}

	@Override
	protected BigDecimal calculateGutscheinVorZuschlagUndSelbstbehalt() {
		BigDecimal gutscheinProTagAufgrundEinkommen = calculateBGProZeiteinheitByEinkommen();
		BigDecimal gutscheinProTagVorZuschlagUndSelbstbehalt = calculateGutscheinProZeiteinheitVorZuschlagUndSelbstbehalt(gutscheinProTagAufgrundEinkommen);
		return EXACT.multiply(gutscheinProTagVorZuschlagUndSelbstbehalt, verfuegteZeiteinheit);
	}

	@Override
	protected BigDecimal calculateMinimalerSelbstbehalt() {
		return EXACT.multiply(getMinimalTarif(), verfuegteZeiteinheit);
	}

	@Override
	protected BigDecimal calculateVollkosten() {
		BigDecimal betreuungspensum = input.getBetreuungspensumProzent();
		BigDecimal anspruchsPensum = BigDecimal.valueOf(input.getAnspruchspensumProzent());

		BigDecimal vollKostenProMonat = input.getMonatlicheBetreuungskosten();
		//wenn anspruchspensum < betreuungspensum, dann anspruchspensum/betreuungspensum * monatlicheBetreuungskosten
		if(anspruchsPensum.compareTo(betreuungspensum) < 0) {
			BigDecimal anspruchsPensumDevidedByBetreuungspensum = EXACT.divide(anspruchsPensum, betreuungspensum);
			vollKostenProMonat = EXACT.multiply(anspruchsPensumDevidedByBetreuungspensum, input.getMonatlicheBetreuungskosten());
		}

		return EXACT.multiply(vollKostenProMonat, anteilMonat);
	}

	@Override
	protected BigDecimal calculateZuschlag() {
		return EXACT.multiply(verfuegteZeiteinheit, calculateZuschlagProZeiteinheit());
	}

	@Override
	protected BigDecimal getMinimalTarif() {
		return getInputParameter().getMinVerguenstigungProTg();
	}

	@Override
	protected BigDecimal getVollkostenTarif() {
		return isBaby ? VOLLKOSTEN_TARIF_BABY: VOLLKOSTEN_TARIF_KIND;
	}

	@Override
	protected BigDecimal getKitaPlusZuschlag() {
		return KITA_PLUS_ZUSCHLAG;
	}

	@Override
	protected BigDecimal getMinBetreuungsgutschein() {
		return isBaby ? MIN_BETREUUNGSGUTSCHEIN_BABY : MIN_BETREUUNGSGUTSCHEIN_KIND;
	}

	@Override
	protected BigDecimal calculateSelbstbehaltElternProzent() {
		return isBaby ? calculateSelbstbehaltElternProzentBaby() : calculateSelbstbehaltElternProzentKind();
	}

	@Override
	protected BigDecimal calculateBGProZeiteinheitByEinkommen() {
		BigDecimal bgProTag = isBaby ? calculateBetreuungsgutscheinProZeiteinheitAufgrundEinkommenGemaessFormel() : calculateBGProTagByEinkommenKind();
		return MathUtil.maximum(bgProTag, getMaximalWertBGProTagAufgrundEinkommen());
	}

	@Override
	protected BigDecimal getAnzahlZeiteinheitenProMonat() {
		return EXACT.divide(getInputParameter().getOeffnungstageKita(), BigDecimal.valueOf(12));
	}

	@Override
	protected PensumUnits getZeiteinheit() {
		return PensumUnits.DAYS;
	}

	/**
	 * Berechnet den Selbstbehalt der Eltern
	 *
	 * returns selbstbehaltDerEltern (gemässFormel)
	 * wenn selbstbehaltDerEltern (gemässFormel) <= 100%,
	 *
	 * returns 100, wenn selbstbehaltDerEltern (gemässFormel) > 100%:
	 */
	private BigDecimal calculateSelbstbehaltElternProzentKind() {
		BigDecimal prozentuallerSelbstbehaltGemaessFormel = calculateSelbstbehaltProzentenGemaessFormel();
		if(prozentuallerSelbstbehaltGemaessFormel.compareTo(BigDecimal.valueOf(100)) > 0) {
			return BigDecimal.valueOf(100);
		}

		return prozentuallerSelbstbehaltGemaessFormel;
	}

	/**
	 * Berechnet den Selbstbehalt der Eltern
	 *
	 * returns prozentuallerSelbstbehaltGemaessFormel, wenn MassgebendesEinkommen <= MaximalMasgebendesEinkommen
	 * {@see AbstractLuzernRechner#calculateSelbstbehaltProzentenGemaessFormel()}
	 *
	 * returns 101 %, wenn MassgebendesEinkommen > MaximalMasgebendesEinkommen:
	 */
    private BigDecimal calculateSelbstbehaltElternProzentBaby() {
		if(getInputMassgebendesEinkommen().compareTo(getInputParameter().getMaxMassgebendesEinkommen()) > 0) {
			return BigDecimal.valueOf(1.01);
		}

		return calculateSelbstbehaltProzentenGemaessFormel();
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
	private BigDecimal calculateBGProTagByEinkommenKind() {
		BigDecimal einsMinusSelbstbehalt = EXACT.subtract(BigDecimal.ONE, getSelbstBehaltElternProzent());
		BigDecimal bgProTag = EXACT.multiply(getVollkostenTarif(), einsMinusSelbstbehalt);

		if(bgProTag.compareTo(getMinBetreuungsgutschein()) > 0) {
			return bgProTag;
		}

		if(getInputMassgebendesEinkommen().compareTo(getInputParameter().getMaxMassgebendesEinkommen()) <= 0) {
			return getMinBetreuungsgutschein();
		}

		return BigDecimal.ZERO;
	}

	protected BigDecimal gemeindeRulesAbhaengigVonVerfuegteZeiteinheit(
		@Nonnull BigDecimal gutschein
	) {
		// Zusaetzlicher Gutschein Gemeinde
		gutschein = EXACT.addNullSafe(gutschein, MathUtil.EXACT.multiply(rechnerParameter.getZusaetzlicherGutscheinGemeindeBetrag(), this.verfuegteZeiteinheit));
		// Zusaetzlicher Baby-Gutschein
		gutschein = EXACT.addNullSafe(gutschein, MathUtil.EXACT.multiply(rechnerParameter.getZusaetzlicherBabyGutscheinBetrag(), verfuegteZeiteinheit));

		return gutschein;
	}
}
