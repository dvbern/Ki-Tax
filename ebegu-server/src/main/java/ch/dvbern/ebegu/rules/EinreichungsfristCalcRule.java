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

package ch.dvbern.ebegu.rules;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;
import static ch.dvbern.ebegu.enums.MsgKey.EINREICHUNGSFRIST_MSG;

/**
 * Regel bezüglich der Einreichungsfrist des Gesuchs:
 * - Wird ein Gesuch zu spät eingereicht, beginnt der Anspruch am 1. des Folgemonats
 * - Beispiel: Ein Gesuch wird am 5. September 2017 eingereicht. In diesem Fall ist erst per 1. Oktober 2017
 * ein Anspruch verfügbar.
 * D.h. für die Angebote „Kita“ und „Tageseltern – Kleinkinder“ ist im August und September kein Anspruch verfügbar.
 * Falls sie einen Platz haben, wird dieser zum privaten Tarif der Kita berechnet.
 * - Für die Angebote Tageseltern–Schulkinder und Tagesstätten entspricht der Anspruch dem gewünschten Pensum.
 * Ihnen wird für den Monat August aber der Volltarif verrechnet.
 * Verweis 16.11 Gesuch zu Speat
 */
public class EinreichungsfristCalcRule extends AbstractCalcRule {

	public EinreichungsfristCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.EINREICHUNGSFRIST, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		if (inputData.isZuSpaetEingereicht()) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.addBemerkung(EINREICHUNGSFRIST_MSG, getLocale());
		}
	}
}
