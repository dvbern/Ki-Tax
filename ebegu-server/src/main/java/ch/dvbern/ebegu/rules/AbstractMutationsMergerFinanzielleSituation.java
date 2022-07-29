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

		final Verfuegung vorgaengerVerfuegung = platz.getVorgaengerVerfuegung();
		Objects.requireNonNull(vorgaengerVerfuegung);

		LocalDateTime timestampVerfuegtVorgaenger = vorgaengerVerfuegung.getPlatz().extractGesuch().getTimestampVerfuegt();
		Objects.requireNonNull(timestampVerfuegtVorgaenger);

		boolean finSitAbgelehnt = FinSitStatus.ABGELEHNT == platz.extractGesuch().getFinSitStatus();
		if (finSitAbgelehnt) {
			// Wenn FinSit abgelehnt, muss immer das letzte verfuegte Einkommen genommen werden
			handleAbgelehnteFinsit(inputAktuel, resultVorgaenger, timestampVerfuegtVorgaenger);
		} else {
			// Der Spezialfall bei Verminderung des Einkommens gilt nur, wenn die FinSit akzeptiert/null war!
			handleVerminderungEinkommen(inputAktuel, resultVorgaenger, mutationsEingansdatum);
		}
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
					inputData.addBemerkung(MsgKey.ANSPRUCHSAENDERUNG_MSG, locale);
				}
			}
		}
	}


	private void handleAbgelehnteFinsit(
		@Nonnull BGCalculationInput inputData,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDateTime timestampVerfuegtVorgaenger
	) {
		// Falls die FinSit in der Mutation abgelehnt wurde, muss grundsaetzlich das Einkommen der Vorverfuegung genommen werden,
		// unabhaengig davon, ob das Einkommen steigt oder sinkt und ob es rechtzeitig gemeldet wurde
		BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommen();

		inputData.setMassgebendesEinkommenVorAbzugFamgr(resultVorangehenderAbschnitt.getMassgebendesEinkommenVorAbzugFamgr());
		inputData.setEinkommensjahr(resultVorangehenderAbschnitt.getEinkommensjahr());
		inputData.setFamGroesse(resultVorangehenderAbschnitt.getFamGroesse());
		inputData.setAbzugFamGroesse(resultVorangehenderAbschnitt.getAbzugFamGroesse());
		if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) != 0) {
			// Die Bemerkung immer dann setzen, wenn das Einkommen (egal in welche Richtung) geaendert haette
			String datumLetzteVerfuegung = Constants.DATE_FORMATTER.format(timestampVerfuegtVorgaenger);
			inputData.addBemerkung(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_MUTATION_MSG, locale, datumLetzteVerfuegung);
		}
	}

	@Nonnull
	private BigDecimal getValueOrZero(@Nullable BigDecimal value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		return value;
	}

}
