/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.FachstellenTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.ServerMessageUtil;

public abstract class AbstractFachstellenCalcRule  extends AbstractCalcRule {

	protected AbstractFachstellenCalcRule(
		@Nonnull RuleKey ruleKey,
		@Nonnull RuleType ruleType,
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(ruleKey, ruleType, ruleValidity, validityPeriod, locale);
	}


	protected PensumFachstelle findPensumFachstelleForGueltigkeit(Kind kind, DateRange gueltigkeit) {
		for (PensumFachstelle pensumFachstelle : kind.getPensumFachstelle()) {
			if (gueltigkeit.intersects(pensumFachstelle.getGueltigkeit())) {
				return pensumFachstelle;
			}
		}
		throw new EbeguRuntimeException("findPensumFachstelleForGueltigkeit", "PensumFachstelle for gueltigkeit not found");
	}

	protected String getFachstelleName(@Nullable Fachstelle fachstelle) {
		if (fachstelle == null) {
			return "";
		}
		return ServerMessageUtil.translateEnumValue(
			fachstelle.getName(),
			getLocale(), Objects.requireNonNull(fachstelle.getMandant()));
	}

	protected String getIndikationName(@Nonnull PensumFachstelle pensumFachstelle, @Nonnull Betreuung betreuung) {
		return pensumFachstelle.getIntegrationTyp().getIndikationMessage(getLocale(), betreuung.extractGesuch()
			.extractMandant());
	}

	protected FachstellenTyp getFachstellenTypFromEinstellungen(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung fachstellenTypEinstellung = einstellungMap.get(EinstellungKey.FACHSTELLEN_TYP);
		return FachstellenTyp.valueOf(fachstellenTypEinstellung.getValue());
	}
}
