package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Locale;

public class MutationsMergerAnspruchHandlerBern extends AbstractMutationsMergerAnspruchHandler {

	public MutationsMergerAnspruchHandlerBern(Locale locale) {
		super(locale);
	}

	@Override
	public void handleAnpassungAnspruch(
		@Nonnull BGCalculationInput inputData,
		@Nullable BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		DateRange gueltigkeit = inputData.getParent().getGueltigkeit();

		//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
		if (!isMeldungZuSpaet(gueltigkeit, mutationsEingansdatum)) {
			return;
		}

		final int anspruchberechtigtesPensum = inputData.getAnspruchspensumProzent();
		final int anspruchAufVorgaengerVerfuegung = resultVorangehenderAbschnitt == null
			? 0
			: resultVorangehenderAbschnitt.getAnspruchspensumProzent();

		if (anspruchberechtigtesPensum > anspruchAufVorgaengerVerfuegung) {
			//Anspruch wird erhöht
			//Meldung nicht Rechtzeitig: Der Anspruch kann sich erst auf den Folgemonat des Eingangsdatum erhöhen
			inputData.setAnspruchspensumProzent(anspruchAufVorgaengerVerfuegung);
			inputData.setRueckwirkendReduziertesPensumRest(anspruchberechtigtesPensum - anspruchAufVorgaengerVerfuegung);
			//Wenn der Anspruch auf dem Vorgänger 0 ist, weil das Erstgesuch zu spät eingereicht wurde
			//soll die Bemerkung bezüglich der Erhöhung nicht angezeigt werden, da es sich um keine Erhöhung handelt
			if(!isAnspruchZeroBecauseVorgaengerZuSpaet(resultVorangehenderAbschnitt)) {
				inputData.addBemerkung(MsgKey.ANSPRUCHSAENDERUNG_MSG, locale);
			}

		} else if (anspruchberechtigtesPensum < anspruchAufVorgaengerVerfuegung) {
			//Meldung nicht Rechtzeitig: Reduktionen des Anspruchs sind auch rückwirkend erlaubt -> keine Aenderungen
			inputData.addBemerkung(MsgKey.REDUCKTION_RUECKWIRKEND_MSG, locale);
		}
	}


	private boolean isAnspruchZeroBecauseVorgaengerZuSpaet(@Nullable BGCalculationResult resultVorangehenderAbschnitt) {
		if (resultVorangehenderAbschnitt == null) {
			return false;
		}

		boolean anspruchsPensumZero = resultVorangehenderAbschnitt.getAnspruchspensumProzent() == 0;
		return anspruchsPensumZero && resultVorangehenderAbschnitt.isZuSpaetEingereicht();
	}
}
