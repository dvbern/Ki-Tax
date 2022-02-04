/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import static ch.dvbern.ebegu.enums.EinstellungKey.AUSSERORDENTLICHER_ANSPRUCH_RULE;
import static ch.dvbern.ebegu.rules.AbstractAusserordentlicherAnspruchCalcRule.AusserordentlicherAnspruchType.FKJV;

/**
 * Regel für den ausserordentlichen Anspruch. Sie beachtet:
 * Ausserordentliches Pensum übersteuert den Anspruch, der aus anderen Reglen berechnet wurde, AUSSER dieser wäre
 * höher. Die maximale Differenz zwischen dem effektiven Anspruch und dem ausserodentlichen Anspruch, darf nicht mehr als der
 * konfigurierte Wert sein. Das Beschäftigungspensum muss eine minimale Grenze übersteigen, damit der ausserordentliche
 * Anspruch angewandt werden kann.
 * Diese Regel kann also den Anspruch nur hinaufsetzen, nie hinunter.
 */
public class FKJVAusserordentlicherAnspruchCalcRule extends AbstractAusserordentlicherAnspruchCalcRule {

	private int minBeschaeftigungsPensumVorschule;
	private int minBeschaeftigungsPensumEingeschult;
	private int maxDifferenzBeschaeftigungsPensum;

	public FKJVAusserordentlicherAnspruchCalcRule(
			int minBeschaeftigungsPensumVorschule,
			int minBeschaeftigungsPensumEingeschult,
			int maxDifferenzBeschaeftigungsPensum,
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale) {
		super(validityPeriod, locale);
		this.minBeschaeftigungsPensumVorschule = minBeschaeftigungsPensumVorschule;
		this.minBeschaeftigungsPensumEingeschult = minBeschaeftigungsPensumEingeschult;
		this.maxDifferenzBeschaeftigungsPensum = maxDifferenzBeschaeftigungsPensum;
	}

	@Override
	public boolean isRelevantForGemeinde(
			@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung ausserOrdentlicherAnspruchRuleTyp = einstellungMap.get(AUSSERORDENTLICHER_ANSPRUCH_RULE);
		Objects.requireNonNull(ausserOrdentlicherAnspruchRuleTyp,"Parameter AUSSERORDENTLICHER_ANSPRUCH_RULE muss gesetzt sein");

		return ausserOrdentlicherAnspruchRuleTyp.getValue().equals(FKJV.toString());
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData) {
		int ausserordentlicherAnspruch = inputData.getAusserordentlicherAnspruch();
		int pensumAnspruch = inputData.getAnspruchspensumProzent();

		if(!beschaeftigungspensumSufficient(platz, inputData)) {
			inputData.setAusserordentlicherAnspruch(0);
			inputData.addBemerkung(
					MsgKey.KEIN_AUSSERORDENTLICHER_ANSPRUCH_MSG,
					getLocale());
			return;
		}

		// Es wird der grössere der beiden Werte genommen!
		if (ausserordentlicherAnspruch > pensumAnspruch) {
			inputData.setAnspruchspensumProzent(ausserordentlicherAnspruch);
			inputData.addBemerkung(
				MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG,
				getLocale());
		}
	}

	private boolean beschaeftigungspensumSufficient(AbstractPlatz platz, BGCalculationInput inputData) {
		if(hasSecondGesuchsteller(platz)) {
			return beschaeftigungspensumSufficientForTwoGesuchstellende(platz, inputData.getErwerbspensumGS1(), inputData.getErwerbspensumGS2());
		}

		return beschaeftigungspensumSufficientForOneGesuchstellende(platz, inputData.getErwerbspensumGS1());
	}


	private boolean hasSecondGesuchsteller(AbstractPlatz platz) {
		return platz.extractGesuch().getGesuchsteller2() != null;
	}

	private boolean beschaeftigungspensumSufficientForTwoGesuchstellende(
			AbstractPlatz platz,
			Integer erwerbspensumGS1,
			Integer erwerbspensumGS2) {
		Kind kind = platz.getKind().getKindJA();
		assert kind.getEinschulungTyp() != null;
		if (erwerbspensumGS1 == null || erwerbspensumGS2 == null) {
			return false;
		}
		final int totalBeschaeftigungspensum = erwerbspensumGS1 + erwerbspensumGS2;
		if (kind.getEinschulungTyp().isEingeschult()) {
			return erwerbspensumSufficient(totalBeschaeftigungspensum - 100, this.minBeschaeftigungsPensumEingeschult);
		}
		return erwerbspensumSufficient(totalBeschaeftigungspensum - 100, this.minBeschaeftigungsPensumVorschule);
	}

	private boolean erwerbspensumSufficient(int beschaeftigungspensum, int minBeschaeftigungspensum) {
		if (beschaeftigungspensum > minBeschaeftigungspensum) {
			return true;
		}
		return Math.abs(beschaeftigungspensum - minBeschaeftigungspensum) <= this.maxDifferenzBeschaeftigungsPensum;
	}

	private boolean beschaeftigungspensumSufficientForOneGesuchstellende(
			AbstractPlatz platz,
			Integer beschaeftigungsPensumGS) {
		Kind kind = platz.getKind().getKindJA();
		assert kind.getEinschulungTyp() != null;
		if (beschaeftigungsPensumGS == null || beschaeftigungsPensumGS == 0) {
			return false;
		}
		if (kind.getEinschulungTyp().isEingeschult()) {
			return erwerbspensumSufficient(beschaeftigungsPensumGS, this.minBeschaeftigungsPensumEingeschult);
		}
		return  erwerbspensumSufficient(beschaeftigungsPensumGS, this.minBeschaeftigungsPensumVorschule);
	}

}
