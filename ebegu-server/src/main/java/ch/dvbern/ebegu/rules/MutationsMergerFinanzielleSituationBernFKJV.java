package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
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

		if (isFinSitRueckwirkendAnzupassen(inputData, massgebendesEinkommenFinSit, resultVorgaenger)) {
			finsitRueckwirkendAnpassen(inputData, massgebendesEinkommenFinSit, platz);
			return;
		}
		if (massgebendesEinkommenFinSit.compareTo(resultVorgaenger.getMassgebendesEinkommenVorAbzugFamgr()) < 0) {
			inputData.addBemerkung(MsgKey.ANSPRUCHSAENDERUNG_MSG, getLocale());
		}
	}

	private boolean isFinSitRueckwirkendAnzupassen(BGCalculationInput input, BigDecimal massgebendesEinkommenFinSit, BGCalculationResult resultVorgaenger) {
		return hasMassgebendesEinkommenVorAbzugFamgrChanged(massgebendesEinkommenFinSit, resultVorgaenger) &&
			!input.isSozialhilfeempfaenger() &&
			!input.isKeineVerguenstigungGewuenscht();
	}

	private void finsitRueckwirkendAnpassen(BGCalculationInput inputData, BigDecimal massgebendesEinkommenFinSit, AbstractPlatz platz) {
		Gesuchsperiode gesuchsperiode = platz.extractGesuchsperiode();

		inputData.setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommenFinSit);
		inputData.setEinkommensjahr(gesuchsperiode.getBasisJahr());

		platz
			.extractGesuch()
			.setFinSitAenderungGueltigAbDatum(gesuchsperiode.getGueltigkeit().getGueltigAb().minusDays(1));
		platz.setFinSitRueckwirkendKorrigiertInThisMutation(true);
		inputData.addBemerkung(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST, getLocale());
	}

	private boolean hasMassgebendesEinkommenVorAbzugFamgrChanged(
		@Nonnull BigDecimal massgebendesEinkommenAktuell,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt) {

		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommenVorAbzugFamgr();

		return massgebendesEinkommenAktuell.compareTo(massgebendesEinkommenVorher) != 0;
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

	@Override
	protected void handleRueckwirkendAnspruchaenderungMsg(
			BGCalculationInput inputData,
			BigDecimal massgebendesEinkommen,
			BigDecimal massgebendesEinkommenVorher) {
		// we don't want any msg for FKJV
	}
}
