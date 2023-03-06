package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.MsgKey;

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

		handleFamiliengroesse(inputAktuel, resultVorgaenger, mutationsEingansdatum);

		if (hasMassgebendesEinkommenVorAbzugFamgrChanged(inputAktuel, resultVorgaenger) && !inputAktuel.isEkvAccepted()) {
			handleFinanzielleSituationRueckwirkendAnpassen(inputAktuel, platz);
			return;
		}

		handleVerminderungEinkommen(inputAktuel, resultVorgaenger, mutationsEingansdatum);
	}

	private void handleFamiliengroesse(BGCalculationInput inputData, BGCalculationResult resultVorgaenger, LocalDate mutationsEingansdatum) {
		if (inputData.getParent().getGueltigkeit().getGueltigAb().isAfter(mutationsEingansdatum)) {
			return;
		}

		inputData.setFamGroesse(resultVorgaenger.getFamGroesse());
		inputData.setAbzugFamGroesse(resultVorgaenger.getAbzugFamGroesse());
	}

	private void handleFinanzielleSituationRueckwirkendAnpassen(
		BGCalculationInput inputData,
		AbstractPlatz platz) {

		//Finanzielle Situation ist gültig ab Datum vor Perioden-Start
		platz
			.extractGesuch()
			.setFinSitAenderungGueltigAbDatum(platz.extractGesuchsperiode().getGueltigkeit().getGueltigAb().minusDays(1));
		platz.extractGesuch()
				.setFinSitRueckwirkendKorrigiertInThisMutation(true);
		inputData.addBemerkung(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST, getLocale());
	}

	private boolean hasMassgebendesEinkommenVorAbzugFamgrChanged(
		@Nonnull BGCalculationInput inputData,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt) {

		BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommenVorAbzugFamgr();
		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommenVorAbzugFamgr();

		return massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) != 0;
	}

}
