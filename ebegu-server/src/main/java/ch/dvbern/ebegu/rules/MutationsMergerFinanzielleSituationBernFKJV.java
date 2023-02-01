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

		if (hasEinkommenChanged(inputAktuel, resultVorgaenger)) {
			if (inputAktuel.isEkvAccepted()) {
				//Einkommensverschlechterung behandeln, wie bisanhin in bern
				handleVerminderungEinkommen(inputAktuel, resultVorgaenger, mutationsEingansdatum);
			} else {
				//Finanzielle Situation ist gültig ab Datum vor Perioden-Start
				platz
					.extractGesuch()
					.setFinSitAenderungGueltigAbDatum(platz.extractGesuchsperiode().getGueltigkeit().getGueltigAb().minusDays(1));
				inputAktuel.addBemerkung(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST, getLocale());
			}
		}
	}
	private boolean hasEinkommenChanged(
		@Nonnull BGCalculationInput inputData,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt) {

		BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommen();

		return massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) != 0;
	}

}
