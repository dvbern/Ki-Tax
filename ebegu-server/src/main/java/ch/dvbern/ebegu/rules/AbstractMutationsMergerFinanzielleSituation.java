package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.util.Constants;

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
		if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) != 0) {
			// Die Bemerkung immer dann setzen, wenn das Einkommen (egal in welche Richtung) geaendert haette
			String datumLetzteVerfuegung = Constants.DATE_FORMATTER.format(timestampVerfuegtVorgaenger);
			inputData.addBemerkung(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_MUTATION_MSG, locale, datumLetzteVerfuegung);
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
