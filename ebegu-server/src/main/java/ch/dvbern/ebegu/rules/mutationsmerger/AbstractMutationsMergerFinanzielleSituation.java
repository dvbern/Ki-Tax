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
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

public abstract class AbstractMutationsMergerFinanzielleSituation {

	private final Locale locale;

	protected AbstractMutationsMergerFinanzielleSituation(Locale local) {
		this.locale = local;
	}

	protected void handleFinanzielleSituation(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {

		boolean finSitAbgelehnt = FinSitStatus.ABGELEHNT == platz.extractGesuch().getFinSitStatus();

		if (finSitAbgelehnt) {
			// Wenn FinSit abgelehnt, muss immer das letzte verfuegte Einkommen genommen werden
			handleAbgelehnteFinsit(inputAktuel, resultVorgaenger, platz);
		} else {
			// Der Spezialfall bei Änderung des Einkommens gilt nur, wenn die FinSit akzeptiert/null war!
			handleEinkommen(inputAktuel, resultVorgaenger, platz, mutationsEingansdatum);
		}
	}

	protected abstract void handleEinkommen(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum);

	private void handleAbgelehnteFinsit(
		@Nonnull BGCalculationInput inputData,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull AbstractPlatz platz
	) {
		final Verfuegung vorgaengerVerfuegung = platz.getVorgaengerVerfuegung();
		Objects.requireNonNull(vorgaengerVerfuegung);

		LocalDateTime timestampVerfuegtVorgaenger = vorgaengerVerfuegung.getPlatz().extractGesuch().getTimestampVerfuegt();
		Objects.requireNonNull(timestampVerfuegtVorgaenger);

		// Falls die FinSit in der Mutation abgelehnt wurde, muss grundsaetzlich das Einkommen der Vorverfuegung genommen werden,
		// unabhaengig davon, ob das Einkommen steigt oder sinkt und ob es rechtzeitig gemeldet wurde
		BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommen();

		setFinSitDataFromResultToInput(inputData, resultVorangehenderAbschnitt);
		inputData.setKeinAnspruchAufgrundEinkommen(vorgaengerVerfuegung.isKategorieMaxEinkommen());

		if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) != 0) {
			// Die Bemerkung immer dann setzen, wenn das Einkommen (egal in welche Richtung) geaendert haette
			String datumLetzteVerfuegung = Constants.DATE_FORMATTER.format(timestampVerfuegtVorgaenger);
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(
				MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_MUTATION_MSG, locale, datumLetzteVerfuegung);
		}
	}

	protected void setFinSitDataFromResultToInput(
		@Nonnull BGCalculationInput input,
		@Nonnull BGCalculationResult result
	) {
		input.setMassgebendesEinkommenVorAbzugFamgr(result.getMassgebendesEinkommenVorAbzugFamgr());
		input.setEinkommensjahr(result.getEinkommensjahr());
		input.setFamGroesse(result.getFamGroesse());
		input.setAbzugFamGroesse(result.getAbzugFamGroesse());
		input.setSozialhilfeempfaenger(result.isSozialhilfeAkzeptiert());
	}

	@Nonnull
	protected BigDecimal getValueOrZero(@Nullable BigDecimal value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		return value;
	}

	protected Locale getLocale() {
		return locale;
	}

}
