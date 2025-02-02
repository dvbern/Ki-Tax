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
import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.MsgKey;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public class MutationsMergerFinanzielleSituationBernFKJV extends MutationsMergerFinanzielleSituationBern {

	public MutationsMergerFinanzielleSituationBernFKJV(Locale local) {
		super(local);
	}

	@Override
	protected void handleEinkommen(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {
		if (inputAktuel.getParent().getGueltigkeit().getGueltigAb().isAfter(mutationsEingansdatum)) {
			return;
		}

		handleFamiliengroesse(inputAktuel, resultVorgaenger);
		handleVerminderungEinkommen(inputAktuel, resultVorgaenger, mutationsEingansdatum);
		handleFinanzielleSituationRueckwirkendAnpassen(inputAktuel, resultVorgaenger, platz);
	}

	private void handleFamiliengroesse(BGCalculationInput inputData, BGCalculationResult resultVorgaenger) {
		inputData.setFamGroesse(resultVorgaenger.getFamGroesse());
		inputData.setAbzugFamGroesse(resultVorgaenger.getAbzugFamGroesse());
	}

	private void handleFinanzielleSituationRueckwirkendAnpassen(
		BGCalculationInput inputData,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz) {
		// wenn einkommensjahr nicht gleich basisjahr, kommt das Einkommen aus EKV,
		// dies soll nie rückwirkend überschrieben werden
		if (platz.extractGesuchsperiode().getBasisJahr() != inputData.getEinkommensjahr()) {
			return;
		}

		BigDecimal massgebendesEinkommenFinSit = getMassgebendesEinkommenFromFinSit(inputData, platz);

		if (isFinSitRueckwirkendAnzupassen(inputData, massgebendesEinkommenFinSit, resultVorgaenger, platz)) {
			finsitRueckwirkendAnpassen(inputData, massgebendesEinkommenFinSit, platz);
			return;
		}
		if (massgebendesEinkommenFinSit.compareTo(resultVorgaenger.getMassgebendesEinkommenVorAbzugFamgr()) < 0) {
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(MsgKey.ANSPRUCHSAENDERUNG_MSG, getLocale());
		}
	}

	private boolean isFinSitRueckwirkendAnzupassen(
		BGCalculationInput input,
		BigDecimal massgebendesEinkommenFinSit,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz) {
		return hasMassgebendesEinkommenVorAbzugFamgrChanged(massgebendesEinkommenFinSit, resultVorgaenger)
			&& !isSozialhilfeBezueger(platz)
			&& !verguenstigungGewuenschtFlagChangedToVerguenstigungGewuenscht(input, resultVorgaenger);
	}

	private boolean isSozialhilfeBezueger(AbstractPlatz platz) {
		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();
		if (familiensituation == null || familiensituation.getSozialhilfeBezueger() == null) {
			return false;
		}

		return Boolean.TRUE.equals(familiensituation.getSozialhilfeBezueger());
	}

	private boolean verguenstigungGewuenschtFlagChangedToVerguenstigungGewuenscht(BGCalculationInput input, BGCalculationResult resultVorgaenger) {
		return input.isVerguenstigungGewuenscht() && !resultVorgaenger.isVerguenstigungGewuenscht();
	}

	private void finsitRueckwirkendAnpassen(BGCalculationInput inputData, BigDecimal massgebendesEinkommenFinSit, AbstractPlatz platz) {
		Gesuchsperiode gesuchsperiode = platz.extractGesuchsperiode();

		inputData.setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommenFinSit);
		inputData.setEinkommensjahr(gesuchsperiode.getBasisJahr());

		platz
			.extractGesuch()
			.setFinSitAenderungGueltigAbDatum(gesuchsperiode.getGueltigkeit().getGueltigAb().minusDays(1));
		platz.setFinSitRueckwirkendKorrigiertInThisMutation(true);

		if (!platz.isAngebotSchulamt()) {
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST, getLocale());
		}
	}

	private boolean hasMassgebendesEinkommenVorAbzugFamgrChanged(
		@Nonnull BigDecimal massgebendesEinkommenAktuell,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt) {

		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommenVorAbzugFamgr();

		return massgebendesEinkommenAktuell.compareTo(massgebendesEinkommenVorher) != 0;
	}

	private BigDecimal getMassgebendesEinkommenFromFinSit(BGCalculationInput inputAktuel, AbstractPlatz platz) {
		FinanzDatenDTO finanzDatenDTO ;

		if (!inputAktuel.isVerguenstigungGewuenscht()) {
			return inputAktuel.getMassgebendesEinkommenVorAbzugFamgr();
		}

		if (inputAktuel.isHasSecondGesuchstellerForFinanzielleSituation()) {
			finanzDatenDTO = platz.extractGesuch().getFinanzDatenDTO_zuZweit();
		} else {
			finanzDatenDTO = platz.extractGesuch().getFinanzDatenDTO_alleine();
		}

		return finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr();
	}

	@Override
	protected void handleRueckwirkendAnspruchaenderungMsg(
			BGCalculationInput inputData,
			BigDecimal massgebendesEinkommen,
			BigDecimal massgebendesEinkommenVorher) {
		// we don't want any msg for FKJV
	}
}
