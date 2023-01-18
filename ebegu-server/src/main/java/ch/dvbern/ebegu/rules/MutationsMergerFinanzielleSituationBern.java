package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.MsgKey;

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
		LocalDate finSitGueltigAb = handleFinanzielleSituationGueltigAb(inputAktuel, platz, mutationsEingansdatum);
		handleVerminderungEinkommen(inputAktuel, resultVorgaenger, finSitGueltigAb);
	}

	private LocalDate handleFinanzielleSituationGueltigAb(
		BGCalculationInput inputData,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {
		LocalDate finSitGueltigAb = mutationsEingansdatum;

		if (platz.extractGesuch().getFinSitTyp() == FinanzielleSituationTyp.BERN_FKJV &&
			platz.extractGesuch().getFinSitAenderungGueltigAbDatum() != null) {
			finSitGueltigAb = platz.extractGesuch().getFinSitAenderungGueltigAbDatum();
			inputData.addBemerkung(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST, getLocale());
		}

		return finSitGueltigAb;
	}

	private void handleVerminderungEinkommen(
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
			if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) < 0) {
				inputData.addBemerkung(MsgKey.ANSPRUCHSAENDERUNG_MSG, getLocale());
			}
		}
	}

}
