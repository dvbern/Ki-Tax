/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungParameter;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;

/**
 * Regel die angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungBGCalcRule extends AbstractCalcRule {

	private final static MathUtil MATH = MathUtil.DEFAULT;
	private final MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams;

	protected MahlzeitenverguenstigungBGCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		@Nonnull MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams
	) {

		super(RuleKey.MAHLZEITENVERGUENSTIGUNG, RuleType.GRUNDREGEL_CALC, RuleValidity.GEMEINDE, validityPeriod, locale);
		this.mahlzeitenverguenstigungParams = mahlzeitenverguenstigungParams;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {

		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();

		boolean verguenstigungBeantrag = familiensituation != null && !familiensituation.isKeineMahlzeitenverguenstigungBeantragt();

		if (!verguenstigungBeantrag) {
			return;
		}

		final BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		final boolean sozialhilfeempfaenger = inputData.isSozialhilfeempfaenger();

		if (!mahlzeitenverguenstigungParams.isEnabled() ||
			!validateInput(inputData) ||
			!mahlzeitenverguenstigungParams.hasAnspruch(massgebendesEinkommen, sozialhilfeempfaenger)) {
			return;
		}

		BigDecimal verguenstigungMahlzeit =
			mahlzeitenverguenstigungParams.getVerguenstigungProMahlzeitWithParam(massgebendesEinkommen, sozialhilfeempfaenger);
		BigDecimal selbstbehaltProTag = mahlzeitenverguenstigungParams.getMinimalerElternbeitragMahlzeit();

		final BigDecimal verguenstigung = berechneMahlzeitenverguenstigung(
			inputData.getAnzahlHauptmahlzeiten(),
			inputData.getAnzahlNebenmahlzeiten(),
			inputData.getTarifHauptmahlzeit(),
			inputData.getTarifNebenmahlzeit(),
			selbstbehaltProTag,
			verguenstigungMahlzeit
		);

		if (verguenstigung.compareTo(BigDecimal.ZERO) > 0) {
			addBemerkung(inputData);
		}

		inputData.getParent().setVerguenstigungMahlzeitenTotalForAsivAndGemeinde(verguenstigung);
	}

	private void addBemerkung(@Nonnull BGCalculationInput inputData) {
		inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG, getLocale());
	}

	private boolean validateInput(@Nonnull BGCalculationInput inputData) {
		return (inputData.getAnzahlHauptmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifHauptmahlzeit().compareTo(BigDecimal.ZERO) > 0) ||
			(inputData.getAnzahlNebenmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifNebenmahlzeit().compareTo(BigDecimal.ZERO) > 0);
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		return einstellungMap.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED).getValueAsBoolean();
	}

	@Nonnull
	private BigDecimal berechneMahlzeitenverguenstigung(
		@Nonnull BigDecimal anzahlHauptmahlzeiten,
		@Nonnull BigDecimal anzahlNebenmahlzeiten,
		@Nonnull BigDecimal tarifProHauptmahlzeit,
		@Nonnull BigDecimal tarifProNebenmahlzeit,
		@Nonnull BigDecimal minElternbeitragProTag,
		@Nonnull BigDecimal maxVerguenstigungProTag
	) {
		// Ein vollständiger Kita Tag besteht aus 2 Nebenmahlzeiten und 1 Hauptmahlzeit
		BigDecimal anzahlNebenmahlzeitenStandardTag = new BigDecimal("2.00");
		BigDecimal dieOeminoeseZwei = new BigDecimal("2.00");

		// Nicht in HMZ berechnete NMZ
		BigDecimal nichtBerechneteNebenmahlzeiten = getNichtInHauptmahlzeitenBerechneteNebenmahlzeiten(
			anzahlHauptmahlzeiten, anzahlNebenmahlzeiten, anzahlNebenmahlzeitenStandardTag);

		// Tasächlicher Betrag MZV HMZ/ pro Tag
		BigDecimal tatsaechlicherBetragProTag = getTatsaechlicherBetragProTag(
			tarifProHauptmahlzeit, minElternbeitragProTag, maxVerguenstigungProTag);

		//Minimaler Betrag MZV NMZ/ pro Tag
		BigDecimal minBetragProTag = getMinimalerBetragProTag(
			tarifProNebenmahlzeit, minElternbeitragProTag, maxVerguenstigungProTag);

		// Vergünstigung HMZ:
		BigDecimal verguenstigungHauptmahlzeiten = getVerguenstigungHauptmahlzeiten(anzahlHauptmahlzeiten, tatsaechlicherBetragProTag);

		// Vergünstigung der in HMZ nicht enthaltene NMZ:
		BigDecimal verguenstigungNebenmahlzeiten = getVerguenstigungNebenmahlzeiten(dieOeminoeseZwei, nichtBerechneteNebenmahlzeiten, minBetragProTag);

		// Vergünstigung:
		// =Q2+R2
		// =Q2+R2
		BigDecimal verguenstigung = MATH.addNullSafe(verguenstigungHauptmahlzeiten, verguenstigungNebenmahlzeiten);
		return verguenstigung;
	}

	@Nonnull
	private BigDecimal getNichtInHauptmahlzeitenBerechneteNebenmahlzeiten(
		@Nonnull BigDecimal anzahlHauptmahlzeiten,
		@Nonnull BigDecimal anzahlNebenmahlzeiten,
		@Nonnull BigDecimal anzahlNebenmahlzeitenStandardTag
	) {
		// =WENN((I2-G2*2)<0;0;I2-G2*2)
		// =WENN((anzahlNebenmahlzeiten-anzahlHauptmahlzeiten*2)<0;0;anzahlNebenmahlzeiten-anzahlHauptmahlzeiten*2)
		BigDecimal nichtBerechneteNebenmahlzeiten = MATH.subtract(
			anzahlNebenmahlzeiten,
			MATH.multiply(anzahlHauptmahlzeiten, anzahlNebenmahlzeitenStandardTag));
		nichtBerechneteNebenmahlzeiten = MathUtil.minimum(nichtBerechneteNebenmahlzeiten, BigDecimal.ZERO);
		return nichtBerechneteNebenmahlzeiten;
	}

	@Nonnull
	private BigDecimal getTatsaechlicherBetragProTag(
		@Nonnull BigDecimal tarifProHauptmahlzeit,
		@Nonnull BigDecimal minElternbeitragProTag,
		@Nonnull BigDecimal maxVerguenstigungProTag
	) {
		// =WENN(MIN(F2-L2;K2)<0;0;MIN(F2-L2;K2))
		// =WENN(MIN(tarifProHauptmahlzeit-minElternbeitragProTag;maxVerguenstigungProTag)<0;0;MIN(tarifProHauptmahlzeit-minElternbeitragProTag;maxVerguenstigungProTag))
		BigDecimal tatsaechlicherBetragProTag = MathUtil.maximum(MATH.subtract(tarifProHauptmahlzeit, minElternbeitragProTag), maxVerguenstigungProTag);
		tatsaechlicherBetragProTag = MathUtil.minimum(tatsaechlicherBetragProTag, BigDecimal.ZERO);
		return tatsaechlicherBetragProTag;
	}

	@Nonnull
	private BigDecimal getMinimalerBetragProTag(
		@Nonnull BigDecimal tarifProNebenmahlzeit,
		@Nonnull BigDecimal minElternbeitragProTag,
		@Nonnull BigDecimal maxVerguenstigungProTag
	) {
		// =WENN(MIN(H2-L2;K2)<0;0;MIN(H2-L2;K2))
		// =WENN(MIN(tarifProNebenmahlzeit-minElternbeitragProTag;maxVerguenstigungProTag)<0;0;MIN(tarifProNebenmahlzeit-minElternbeitragProTag;maxVerguenstigungProTag))
		BigDecimal minBetragProTag = MathUtil.maximum(MATH.subtract(tarifProNebenmahlzeit, minElternbeitragProTag), maxVerguenstigungProTag);
		minBetragProTag = MathUtil.minimum(minBetragProTag, BigDecimal.ZERO);
		return minBetragProTag;
	}

	@Nonnull
	private BigDecimal getVerguenstigungHauptmahlzeiten(
		@Nonnull BigDecimal anzahlHauptmahlzeiten,
		@Nonnull BigDecimal tatsaechlicherBetragProTag
	) {
		// =RUNDEN(M2*G2;2)
		// =RUNDEN(tatsaechlicherBetragProTag*anzahlHauptmahlzeiten;2)
		BigDecimal verguenstigungHauptmahlzeiten = MATH.multiply(tatsaechlicherBetragProTag, anzahlHauptmahlzeiten);
		return verguenstigungHauptmahlzeiten;
	}

	@Nonnull
	private BigDecimal getVerguenstigungNebenmahlzeiten(
		@Nonnull BigDecimal anzahlNebenmahlzeitenStandardTag,
		@Nonnull BigDecimal nichtBerechneteNebenmahlzeiten,
		@Nonnull BigDecimal minBetragProTag
	) {
		// =WENN(ISTGERADE(J2);O2*J2/2;(O2*(J2-1)/2)+N2)
		// =WENN(ISTGERADE(nichtBerechneteNebenmahlzeiten);O2*nichtBerechneteNebenmahlzeiten/2;(O2*(nichtBerechneteNebenmahlzeiten-1)/2)+minBetragProTag)
		BigDecimal verguenstigungNebenmahlzeiten = null;
		if (MathUtil.isEven(nichtBerechneteNebenmahlzeiten.intValue())) {
			verguenstigungNebenmahlzeiten = MATH.divide(
				MATH.multiply(anzahlNebenmahlzeitenStandardTag, nichtBerechneteNebenmahlzeiten),
				anzahlNebenmahlzeitenStandardTag);
		} else {
			BigDecimal zuberuecksichtigendeAnzahlNebenmahlzeiten = MATH.subtract(nichtBerechneteNebenmahlzeiten, BigDecimal.ONE);
			verguenstigungNebenmahlzeiten = MATH.add(
				MATH.divide(
					MATH.multiply(anzahlNebenmahlzeitenStandardTag, zuberuecksichtigendeAnzahlNebenmahlzeiten),
					anzahlNebenmahlzeitenStandardTag),
				minBetragProTag);
		}
		return verguenstigungNebenmahlzeiten;
	}
}
