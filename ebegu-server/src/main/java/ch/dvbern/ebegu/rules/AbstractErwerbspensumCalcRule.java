package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

import static java.util.Objects.requireNonNull;

public abstract class AbstractErwerbspensumCalcRule extends AbstractCalcRule {

	protected AbstractErwerbspensumCalcRule(
		@Nonnull RuleKey ruleKey,
		@Nonnull RuleType ruleType,
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(ruleKey, ruleType, ruleValidity, validityPeriod, locale);
	}

	/**
	 * Monat Rule, der GS2 ist nach aenderung die Famsit ab Anfang naechste Monat erst berucksichtig
	 */
	protected boolean hasSecondGSForZeit(@Nonnull Gesuch gesuch, DateRange gueltigkeit) {
		final Familiensituation familiensituation = requireNonNull(gesuch.extractFamiliensituation());
		final Familiensituation familiensituationErstGesuch = gesuch.extractFamiliensituationErstgesuch();

		LocalDate familiensituationGueltigAb = familiensituation.getAenderungPer();
		if (familiensituationGueltigAb != null
			&& familiensituationErstGesuch != null
			&& gueltigkeit.getGueltigAb().isBefore(familiensituationGueltigAb.plusMonths(1).withDayOfMonth(1))) {
			return familiensituationErstGesuch.hasSecondGesuchsteller(gueltigkeit.getGueltigBis());
		}
		return familiensituation.hasSecondGesuchsteller(gueltigkeit.getGueltigBis());
	}


}
