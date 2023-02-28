package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.FinanzDatenDTO;
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

		if (inputAktuel.getParent().getGueltigkeit().getGueltigAb().isAfter(mutationsEingansdatum)) {
			return;
		}

		if (inputAktuel.isEkvAccepted()) {
			BigDecimal massgebendesEinkommen = getMassgebendesEinkommenFromFinSit(inputAktuel, platz);

			if (hasMassgebendesEinkommenVorAbzugFamgrChanged(massgebendesEinkommen, resultVorgaenger)) {
				inputAktuel.setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
				inputAktuel.setEinkommensjahr(platz.extractGesuchsperiode().getBasisJahr());
				handleFinanzielleSituationRueckwirkendAnpassen(inputAktuel, resultVorgaenger, platz);
			} else {
				handleVerminderungEinkommen(inputAktuel, resultVorgaenger, mutationsEingansdatum);
			}

			return;
		}

		if (hasMassgebendesEinkommenVorAbzugFamgrChanged(inputAktuel.getMassgebendesEinkommenVorAbzugFamgr(), resultVorgaenger)) {
			handleFinanzielleSituationRueckwirkendAnpassen(inputAktuel, resultVorgaenger, platz);
		}
	}

	private BigDecimal getMassgebendesEinkommenFromFinSit(BGCalculationInput inputAktuel, AbstractPlatz platz) {
		FinanzDatenDTO finanzDatenDTO ;

		if (inputAktuel.isHasSecondGesuchstellerForFinanzielleSituation()) {
			finanzDatenDTO = platz.extractGesuch().getFinanzDatenDTO_zuZweit();
		} else {
			finanzDatenDTO = platz.extractGesuch().getFinanzDatenDTO_alleine();
		}

		return finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr();
	}

	private void handleFinanzielleSituationRueckwirkendAnpassen(
		BGCalculationInput inputData,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz) {

		//Finanzielle Situation ist gültig ab Datum vor Perioden-Start
		platz
			.extractGesuch()
			.setFinSitAenderungGueltigAbDatum(platz.extractGesuchsperiode().getGueltigkeit().getGueltigAb().minusDays(1));
		platz.extractGesuch()
				.setFinSitRueckwirkendKorrigiertInThisMutation(true);
		inputData.addBemerkung(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST, getLocale());

		//Nur die finanzielle Situation soll rückwirkend geändert werden, FamilienSituation nicht
		inputData.setFamGroesse(resultVorgaenger.getFamGroesse());
		inputData.setAbzugFamGroesse(resultVorgaenger.getAbzugFamGroesse());

	}

	private boolean hasMassgebendesEinkommenVorAbzugFamgrChanged(
		@Nonnull BigDecimal massgebendesEinkommen,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt) {
		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommenVorAbzugFamgr();

		return massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) != 0;
	}

}
