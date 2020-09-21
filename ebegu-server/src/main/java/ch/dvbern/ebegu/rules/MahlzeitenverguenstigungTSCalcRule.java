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

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;

/**
 * Regel die angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungTSCalcRule extends AbstractCalcRule {

	private final MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams;

	protected MahlzeitenverguenstigungTSCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		@Nonnull MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams
	) {

		super(RuleKey.MAHLZEITENVERGUENSTIGUNG, RuleType.GRUNDREGEL_CALC, RuleValidity.GEMEINDE, validityPeriod, locale);
		this.mahlzeitenverguenstigungParams = mahlzeitenverguenstigungParams;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(BetreuungsangebotTyp.TAGESSCHULE);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {

		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();

		boolean verguenstigungBeantrag = familiensituation != null && !familiensituation.isKeineMahlzeitenverguenstigungBeantragt();

		if (!verguenstigungBeantrag) {
			return;
		}

		if (!mahlzeitenverguenstigungParams.isEnabled()) {
			return;
		}

		BigDecimal verguenstigungGemaessEinkommen =
			mahlzeitenverguenstigungParams.getVerguenstigungProHauptmahlzeitWithParam(inputData.getMassgebendesEinkommen(), inputData.isSozialhilfeempfaenger());

		// Wenn die Vergünstigung pro Hauptmahlzeit grösser 0 ist
		if (verguenstigungGemaessEinkommen.compareTo(BigDecimal.ZERO) > 0) {

			BigDecimal verguenstigungMitBetreuung = getVerguenstigung(
				verguenstigungGemaessEinkommen,
				inputData.getTsInputMitBetreuung().getVerpflegungskostenUndMahlzeiten(),
				inputData.getTsInputMitBetreuung().getVerpflegungskostenUndMahlzeitenZweiWochen());
			BigDecimal verguenstigungOhneBetreuung = getVerguenstigung(
				verguenstigungGemaessEinkommen,
				inputData.getTsInputOhneBetreuung().getVerpflegungskostenUndMahlzeiten(),
				inputData.getTsInputOhneBetreuung().getVerpflegungskostenUndMahlzeitenZweiWochen());

			if (verguenstigungMitBetreuung.compareTo(BigDecimal.ZERO) > 0 ) {
				inputData.setTsVerpflegungskostenVerguenstigtMitBetreuung(verguenstigungMitBetreuung);
				inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS, getLocale());
			}
			if (verguenstigungOhneBetreuung.compareTo(BigDecimal.ZERO) > 0 ) {
				inputData.setTsVerpflegungskostenVerguenstigtOhneBetreuung(verguenstigungOhneBetreuung);
				inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS, getLocale());
			}
		}
	}

	@Nonnull
	private BigDecimal getVerguenstigung(
		@Nonnull BigDecimal verguenstigungGemaessEinkommen,
		@Nonnull Map<BigDecimal, Integer> kostenUndAnzMahlzeiten,
		@Nonnull Map<BigDecimal, Integer> kostenUndAnzMahlzeitenZweiWochen
	) {
		BigDecimal verguenstigung = BigDecimal.ZERO;
		for (Map.Entry<BigDecimal, Integer> entry : kostenUndAnzMahlzeiten.entrySet()) {
			BigDecimal verguenstigungEffektivMitBetreuung = mahlzeitenverguenstigungParams.getVerguenstigungEffektiv(verguenstigungGemaessEinkommen,
				entry.getKey(),
				mahlzeitenverguenstigungParams.getMinimalerElternbeitragHauptmahlzeit());

			verguenstigung = MathUtil.DEFAULT.addNullSafe(verguenstigung,
				verguenstigungEffektivMitBetreuung.multiply(BigDecimal.valueOf(entry.getValue())));
		}

		for (Map.Entry<BigDecimal, Integer> entry : kostenUndAnzMahlzeitenZweiWochen.entrySet()) {
			BigDecimal verguenstigungEffektivMitBetreuung = mahlzeitenverguenstigungParams.getVerguenstigungEffektiv(verguenstigungGemaessEinkommen,
				entry.getKey(),
				mahlzeitenverguenstigungParams.getMinimalerElternbeitragHauptmahlzeit());

			verguenstigungEffektivMitBetreuung = MathUtil.DEFAULT.multiply(verguenstigungEffektivMitBetreuung, BigDecimal.valueOf(0.5));

			verguenstigung = MathUtil.DEFAULT.addNullSafe(verguenstigung,
				verguenstigungEffektivMitBetreuung.multiply(BigDecimal.valueOf(entry.getValue())));
		}
		return verguenstigung;
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		return einstellungMap.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED).getValueAsBoolean();
	}
}
