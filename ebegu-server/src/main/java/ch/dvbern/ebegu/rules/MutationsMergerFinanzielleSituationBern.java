package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
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
		handleVerminderungEinkommen(inputAktuel, resultVorgaenger, mutationsEingansdatum);
		handleAnpassungAnspruch(inputAktuel, resultVorgaenger, mutationsEingansdatum);
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
			if (!inputData.getParent().getGueltigkeit().getGueltigAb().isAfter(mutationsEingansdatum)) {
				// Der Stichtag fuer diese Erhöhung ist noch nicht erreicht -> Wir arbeiten mit dem alten Wert!
				// Sobald der Stichtag erreicht ist, müssen wir nichts mehr machen, da dieser Merger *nach* den Monatsabschnitten läuft
				// Wir haben also nie Abschnitte, die über die Monatsgrenze hinausgehen
				inputData.setMassgebendesEinkommenVorAbzugFamgr(resultVorangehenderAbschnitt.getMassgebendesEinkommenVorAbzugFamgr());
				inputData.setEinkommensjahr(resultVorangehenderAbschnitt.getEinkommensjahr());
				inputData.setFamGroesse(resultVorangehenderAbschnitt.getFamGroesse());
				inputData.setAbzugFamGroesse(resultVorangehenderAbschnitt.getAbzugFamGroesse());

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

	private void handleAnpassungAnspruch(
		@Nonnull BGCalculationInput inputData,
		@Nullable BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		final int anspruchberechtigtesPensum = inputData.getAnspruchspensumProzent();
		final int anspruchAufVorgaengerVerfuegung = resultVorangehenderAbschnitt == null
			? 0
			: resultVorangehenderAbschnitt.getAnspruchspensumProzent();

		DateRange gueltigkeit = inputData.getParent().getGueltigkeit();

		if (anspruchberechtigtesPensum > anspruchAufVorgaengerVerfuegung) {
			//Anspruch wird erhöht
			//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
			if (isMeldungZuSpaet(gueltigkeit, mutationsEingansdatum)) {
				//Meldung nicht Rechtzeitig: Der Anspruch kann sich erst auf den Folgemonat des Eingangsdatum erhöhen
				inputData.setAnspruchspensumProzent(anspruchAufVorgaengerVerfuegung);
				inputData.setRueckwirkendReduziertesPensumRest(anspruchberechtigtesPensum - inputData.getAnspruchspensumProzent());
				//Wenn der Anspruch auf dem Vorgänger 0 ist, weil das Erstgesuch zu spät eingereicht wurde
				//soll die Bemerkung bezüglich der Erhöhung nicht angezeigt werden, da es sich um keine Erhöhung handelt
				if(!isAnspruchZeroBecauseVorgaengerZuSpaet(resultVorangehenderAbschnitt)) {
					inputData.addBemerkung(MsgKey.ANSPRUCHSAENDERUNG_MSG, getLocale());
				}
			}
		} else if (anspruchberechtigtesPensum < anspruchAufVorgaengerVerfuegung) {
			// Anspruch wird kleiner
			//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
			if (isMeldungZuSpaet(gueltigkeit, mutationsEingansdatum)) {
				//Meldung nicht Rechtzeitig: Reduktionen des Anspruchs sind auch rückwirkend erlaubt -> keine Aenderungen
				inputData.addBemerkung(MsgKey.REDUCKTION_RUECKWIRKEND_MSG, getLocale());
			}
		}
	}

	private boolean isMeldungZuSpaet(@Nonnull DateRange gueltigkeit, @Nonnull LocalDate mutationsEingansdatum) {
		return !gueltigkeit.getGueltigAb().withDayOfMonth(1).isAfter((mutationsEingansdatum));
	}


	private boolean isAnspruchZeroBecauseVorgaengerZuSpaet(BGCalculationResult resultVorangehenderAbschnitt) {
		if(resultVorangehenderAbschnitt == null) {
			return false;
		}

		boolean anspruchsPensumZero = resultVorangehenderAbschnitt.getAnspruchspensumProzent() == 0;
		return anspruchsPensumZero && resultVorangehenderAbschnitt.isZuSpaetEingereicht();
	}


}
