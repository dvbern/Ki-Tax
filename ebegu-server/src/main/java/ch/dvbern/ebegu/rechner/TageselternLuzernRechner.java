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

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.MathUtil;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

public class TageselternLuzernRechner extends AbstractLuzernRechner {

	//Die Tarife werden im Moment als Konstante gespeichert. Dies wird in Zukunft evtl noch konfigurierbar gemacht.
	private static final BigDecimal MIN_BETREUUNGSGUTSCHEIN_BABY = BigDecimal.valueOf(1.30);
	private static final BigDecimal MIN_BETREUUNGSGUTSCHEIN_KIND = BigDecimal.ONE;

	private boolean isBaby = false;
	private BigDecimal stuendlicherVorllkostenTarif;

	protected TageselternLuzernRechner(List<RechnerRule> rechnerRulesForGemeinde) {
		super(rechnerRulesForGemeinde);
	}

	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		isBaby = verfuegungZeitabschnitt.getBgCalculationInputAsiv().isBabyTarif();
		stuendlicherVorllkostenTarif = verfuegungZeitabschnitt.getBgCalculationInputAsiv().getStuendlicheVollkosten();
		super.calculate(verfuegungZeitabschnitt, parameterDTO);
	}

	@Override
	protected BigDecimal calculateVollkostenProZeitabschnitt(BigDecimal vollkostenGekuerzt) {
		return EXACT.multiply(vollkostenGekuerzt, this.verfuegteZeiteinheit);
	}

	@Override
	protected BigDecimal calculateGutscheinProZeitabschnitt(BigDecimal gutschein) {
		return EXACT.multiply(gutschein, this.verfuegteZeiteinheit);
	}

	@Override
	protected BigDecimal calculateGutscheinVorZuschlagUndSelbstbehalt() {
		BigDecimal gutscheinProStundeAufgrundEinkommen = calculateBGProZeiteinheitByEinkommen();
		return calculateGutscheinProZeiteinheitVorZuschlagUndSelbstbehalt(gutscheinProStundeAufgrundEinkommen);
	}

	@Override
	protected BigDecimal calculateMinimalerSelbstbehalt() {
		return getMinimalTarif();
	}

	@Override
	protected BigDecimal calculateVollkosten() {
		return this.stuendlicherVorllkostenTarif;
	}

	@Override
	protected BigDecimal calculateZuschlag() {
		return calculateZuschlagProZeiteinheit();
	}

	@Override
	protected BigDecimal getMinimalTarif() {
		return getInputParameter().getMinVerguenstigungProStd();
	}

	@Override
	protected BigDecimal getVollkostenTarif() {
		return isBaby ? getInputParameter().getMaxVerguenstigungVorschuleBabyProStd() : getInputParameter().getMaxVerguenstigungVorschuleKindProStd();
	}

	@Override
	protected BigDecimal getKitaPlusZuschlag() {
		//Tageseltern haben keinen KitaPlus Zuschlag
		return BigDecimal.ZERO;
	}

	@Override
	protected BigDecimal calculateSelbstbehaltElternProzent() {
		BigDecimal prozentuallerSelbstbehaltGemaessFormel = calculateSelbstbehaltProzentenGemaessFormel();

		if(prozentuallerSelbstbehaltGemaessFormel.compareTo(BigDecimal.valueOf(100)) > 0) {
			return BigDecimal.valueOf(100);
		}

		return prozentuallerSelbstbehaltGemaessFormel;
	}

	@Override
	protected BigDecimal calculateBGProZeiteinheitByEinkommen() {
		BigDecimal bgProStunde = calculateBetreuungsgutscheinProZeiteinheitAufgrundEinkommenGemaessFormel();
		return MathUtil.maximum(bgProStunde, getMaximalWertBGProTagAufgrundEinkommen());
	}

	@Override
	protected BigDecimal getAnzahlZeiteinheitenProMonat() {
		BigDecimal tageProMonat = EXACT.divide(getInputParameter().getOeffnungstageTFO(), BigDecimal.valueOf(12));
		return EXACT.multiply(getInputParameter().getOeffnungsstundenTFO(), tageProMonat);
	}

	@Override
	protected PensumUnits getZeiteinheit() {
		return PensumUnits.HOURS;
	}

	@Override
	protected BigDecimal getMinBetreuungsgutschein() {
		return  isBaby ? MIN_BETREUUNGSGUTSCHEIN_BABY : MIN_BETREUUNGSGUTSCHEIN_KIND;
	}

	@Override
	protected BigDecimal gemeindeRulesAbhaengigVonVerfuegteZeiteinheit(
		@Nonnull BigDecimal gutschein
	) {
		// Zusaetzlicher Gutschein Gemeinde
		gutschein = EXACT.addNullSafe(gutschein, rechnerParameter.getZusaetzlicherGutscheinGemeindeBetrag());
		// Zusaetzlicher Baby-Gutschein
		gutschein = EXACT.addNullSafe(gutschein, rechnerParameter.getZusaetzlicherBabyGutscheinBetrag());

		return gutschein;
	}
}
