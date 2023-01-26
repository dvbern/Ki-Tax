package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

public class FamiliensituationBeendetAbschnittRule extends AbstractAbschnittRule {
	public static final int ZERO = 0;

	protected FamiliensituationBeendetAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(RuleKey.FAMILIENSITUATION, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Nonnull
	@Override
	List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		Gesuch gesuch = platz.extractGesuch();
		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();

		LocalDate familiensituationAenderungPer = Objects.requireNonNull(familiensituation).getAenderungPer();
		if (null == familiensituationAenderungPer) {
			return new LinkedList<>();
		}
		LocalDate gueltigBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		LocalDate firstDayOfNextMonth = familiensituationAenderungPer.with(TemporalAdjusters.firstDayOfNextMonth());

		VerfuegungZeitabschnitt neuerZeitabschnittOhnePartner =
			createZeitabschnittWithinValidityPeriodOfRule(new DateRange(firstDayOfNextMonth, gueltigBis));
		neuerZeitabschnittOhnePartner.setPartnerIdentischMitVorgesuch(Boolean.FALSE);

		final List<VerfuegungZeitabschnitt> neueZeitabschnitte = new LinkedList<>();
		neueZeitabschnitte.add(neuerZeitabschnittOhnePartner);
		return neueZeitabschnitte;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}
}
