/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.mutationsmerger;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.MsgKey;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public class MutationsMergerFinanzielleSituationBern extends AbstractMutationsMergerFinanzielleSituation {

	public MutationsMergerFinanzielleSituationBern(Locale local) {
		super(local);
	}

	@Override
	protected void handleEinkommen(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {
		handleVerminderungEinkommen(inputAktuel, resultVorgaenger, mutationsEingansdatum);
	}

	protected void handleVerminderungEinkommen(
		@Nonnull BGCalculationInput inputData,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		// Massgebendes Einkommen
		BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommen();
		if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) <= 0) {
			// Massgebendes Einkommen wird kleiner, der Anspruch also höher: Darf nicht rückwirkend sein!
			if (inputData.getParent().getGueltigkeit().getGueltigAb().isAfter(mutationsEingansdatum)) {
				return;
			}

			// Der Stichtag fuer diese Erhöhung ist noch nicht erreicht -> Wir arbeiten mit dem alten Wert!
			// Sobald der Stichtag erreicht ist, müssen wir nichts mehr machen, da dieser Merger *nach* den Monatsabschnitten läuft
			// Wir haben also nie Abschnitte, die über die Monatsgrenze hinausgehen
			setFinSitDataFromResultToInput(inputData, resultVorangehenderAbschnitt);

			if (resultVorangehenderAbschnitt.getTsCalculationResultMitPaedagogischerBetreuung() != null) {
				inputData.getTsInputMitBetreuung().setVerpflegungskostenVerguenstigt(
					getValueOrZero(
						resultVorangehenderAbschnitt.getTsCalculationResultMitPaedagogischerBetreuung().getVerpflegungskostenVerguenstigt()));
			} else {
				inputData.getTsInputMitBetreuung().setVerpflegungskostenVerguenstigt(BigDecimal.ZERO);
			}
			if (resultVorangehenderAbschnitt.getTsCalculationResultOhnePaedagogischerBetreuung() != null) {
				inputData.getTsInputOhneBetreuung().setVerpflegungskostenVerguenstigt(
					getValueOrZero(
						resultVorangehenderAbschnitt.getTsCalculationResultOhnePaedagogischerBetreuung().getVerpflegungskostenVerguenstigt()));
			} else {
				inputData.getTsInputOhneBetreuung().setVerpflegungskostenVerguenstigt(BigDecimal.ZERO);
			}
			handleRueckwirkendAnspruchaenderungMsg(inputData, massgebendesEinkommen, massgebendesEinkommenVorher);
		}
	}

	protected void handleRueckwirkendAnspruchaenderungMsg(
			BGCalculationInput inputData,
			BigDecimal massgebendesEinkommen,
			BigDecimal massgebendesEinkommenVorher) {
		if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) < 0) {
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(MsgKey.ANSPRUCHSAENDERUNG_MSG, getLocale());
		}
	}

}
