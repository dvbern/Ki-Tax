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
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
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
		final List<VerfuegungZeitabschnitt> neueZeitabschnitte = new LinkedList<>();

		if (familiensituation.getFamilienstatus().equals(EnumFamilienstatus.KONKUBINAT_KEIN_KIND)) {
			LocalDate startKonkubinat = familiensituation.getStartKonkubinat();
			if (null == startKonkubinat) {
				return neueZeitabschnitte;
			}
			createZeitabschnitteNachZweiJahrenKonkubinat(neueZeitabschnitte, gesuch, startKonkubinat);
		}

		LocalDate familiensituationAenderungPer = Objects.requireNonNull(familiensituation).getAenderungPer();
		if (null == familiensituationAenderungPer) {
			return neueZeitabschnitte;
		}
		if (null == gesuch || null == gesuch.getGesuchsperiode()) {
			return neueZeitabschnitte;
		}

		if (Objects.isNull(familiensituation.getPartnerIdentischMitVorgesuch()) ||
				Objects.equals(Boolean.TRUE, familiensituation.getPartnerIdentischMitVorgesuch())) {
			return neueZeitabschnitte;
		}
		LocalDate gueltigBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		LocalDate firstDayOfNextMonth = familiensituationAenderungPer.with(TemporalAdjusters.firstDayOfNextMonth());

		createZeitabschnitteNachPartnerStatusAenderung(neueZeitabschnitte, gueltigBis, firstDayOfNextMonth);
		return neueZeitabschnitte;
	}

	private void createZeitabschnitteNachZweiJahrenKonkubinat(
			@Nonnull List<VerfuegungZeitabschnitt> neueZeitabschnitte,
			@Nonnull Gesuch gesuch,
			@Nonnull LocalDate startKonkubinat) {

		LocalDate minDauerKonkubinat =
				Objects.requireNonNull(gesuch.extractFamiliensituation())
						.getStartKonkubinatPlusMindauer(startKonkubinat);

		if (!gesuch.getGesuchsperiode().getGueltigkeit().contains(minDauerKonkubinat)) {
			return;
		}
		LocalDate zweiJahreKonkubinatNextMonth = Objects.requireNonNull(gesuch.extractFamiliensituation())
				.getStartKonkubinatPlusMindauerEndOfMonth(startKonkubinat);
		VerfuegungZeitabschnitt abschnittNachJahrenKonkubinat =
				createZeitabschnittWithinValidityPeriodOfRule(new DateRange(
						zweiJahreKonkubinatNextMonth,
						gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()));
		neueZeitabschnitte.add(abschnittNachJahrenKonkubinat);

	}

	private void createZeitabschnitteNachPartnerStatusAenderung(
			List<VerfuegungZeitabschnitt> neueZeitabschnitte, @Nonnull LocalDate gueltigBis,
			@Nonnull LocalDate firstDayOfNextMonth) {
		VerfuegungZeitabschnitt abschnittNachPartnerStatusAenderung =
				createZeitabschnittWithinValidityPeriodOfRule(new DateRange(firstDayOfNextMonth, gueltigBis));
		abschnittNachPartnerStatusAenderung.setPartnerIdentischMitVorgesuch(Boolean.FALSE);
		neueZeitabschnitte.add(abschnittNachPartnerStatusAenderung);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}
}
