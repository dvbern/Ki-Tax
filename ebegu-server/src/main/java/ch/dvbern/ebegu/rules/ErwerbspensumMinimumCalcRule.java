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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Die Rule wird ausgeführt, wenn die Konfig ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM auf MINIMUM gesetzt ist.
 * Die Antragsteller müssen ein minimales Beschäftigungspensum haben, um den vollen Anspruch zu erhalten.
 * Jeder Antragsteller muss mindestens 20% arbeiten.
 * Die Mindest-Beschäftigung bei Alleinerziehenden ist 20% und bei Paaren 120%. Wenn das Minimum erreicht ist,
 * wird 100% Anspruch gewährt sonst 0%
 */
public class ErwerbspensumMinimumCalcRule extends AbstractErwerbspensumCalcRule {

	private static final int MINIMUM_EWP_FOR_ONE_GS = 20;

	protected ErwerbspensumMinimumCalcRule(@Nonnull RuleKey ruleKey, @Nonnull RuleType ruleType, @Nonnull RuleValidity ruleValidity, @Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(ruleKey, ruleType, ruleValidity, validityPeriod, locale);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		Gesuch gesuch = platz.extractGesuch();
		boolean has2Gs = hasSecondGSForZeit(gesuch, inputData.getParent().getGueltigkeit());
		setAnspruch(inputData, has2Gs);
	}

	protected void setAnspruch(@Nonnull BGCalculationInput inputData, boolean has2Gs) {
		if (isMimimumErwerpsmensumErreicht(inputData, has2Gs)) {
			inputData.setAnspruchspensumProzent(100);
		} else {
			inputData.setAnspruchspensumProzent(0);
			inputData.setAnspruchspensumRest(0);
			inputData.setMinimalErforderlichesPensum(0);
			inputData.setMinimalesEwpUnterschritten(true);
			inputData.addBemerkung(MsgKey.ERWERBSPENSUM_MINIMUM_NICHT_ERRECHT, getLocale());
		}
	}

	private boolean isMimimumErwerpsmensumErreicht(BGCalculationInput inputData, boolean has2Gs) {
		int ewpGS1 = getErwebspensumMax100(inputData.getErwerbspensumGS1());
		int ewpGS2 = getErwebspensumMax100(inputData.getErwerbspensumGS2());
		int minimumEWP = getMinimumErwerbspensum(has2Gs);

		if (has2Gs) {
			return ewpGS1 + ewpGS2 >= minimumEWP;
		}

		return ewpGS1 >= minimumEWP;
	}

	private int getMinimumErwerbspensum(boolean has2Gs) {
		return has2Gs ? 100 + MINIMUM_EWP_FOR_ONE_GS : MINIMUM_EWP_FOR_ONE_GS;
	}

	private int getErwebspensumMax100(@Nullable Integer erwerbspensum) {
		if (erwerbspensum == null) {
			return 0;
		}

		if (erwerbspensum.compareTo(100) > 0) {
			return 100;
		}

		return erwerbspensum;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}
}
