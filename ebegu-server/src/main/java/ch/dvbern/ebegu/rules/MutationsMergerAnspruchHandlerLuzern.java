package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Locale;

public class MutationsMergerAnspruchHandlerLuzern extends AbstractMutationsMergerAnspruchHandler {

	public MutationsMergerAnspruchHandlerLuzern(Locale locale) {
		super(locale);
	}

	@Override
	public void handleAnpassungAnspruch(
		@Nonnull BGCalculationInput inputData,
		@Nullable BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum) {
		if (isMeldungZuSpaet(inputData.getParent().getGueltigkeit(), mutationsEingansdatum)) {
			final int anspruchAufVorgaengerVerfuegung = resultVorangehenderAbschnitt == null ? 0
				: resultVorangehenderAbschnitt.getAnspruchspensumProzent();

			inputData.setAnspruchspensumProzent(anspruchAufVorgaengerVerfuegung);
		}
	}

}
