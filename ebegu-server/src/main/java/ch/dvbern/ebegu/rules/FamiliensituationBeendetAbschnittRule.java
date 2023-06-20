package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.*;

public class FamiliensituationBeendetAbschnittRule extends AbstractAbschnittRule {
	public static final int ZERO = 0;

	private boolean familiensituationBeendenActivated;

	protected FamiliensituationBeendetAbschnittRule(
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale,
			@Nonnull boolean familiensituationBeendenActivated) {
		super(RuleKey.FAMILIENSITUATION, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
		this.familiensituationBeendenActivated = familiensituationBeendenActivated;
	}

	@Nonnull
	@Override
	List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		final List<VerfuegungZeitabschnitt> neueZeitabschnitte = new LinkedList<>();

		if (!this.familiensituationBeendenActivated) {
			return neueZeitabschnitte;
		}

		Gesuch gesuch = platz.extractGesuch();
		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();
		if (familiensituation != null &&
			familiensituation.getFamilienstatus() == EnumFamilienstatus.KONKUBINAT_KEIN_KIND) {
			LocalDate startKonkubinat = familiensituation.getStartKonkubinat();
			if (null != startKonkubinat) {
				createZeitabschnitteNachZweiJahrenKonkubinat(neueZeitabschnitte, gesuch, startKonkubinat);
			}

			return neueZeitabschnitte;
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

		LocalDate konkubinatPlusMinDauerKonukubinat =
				Objects.requireNonNull(gesuch.extractFamiliensituation())
						.getStartKonkubinatPlusMindauer(startKonkubinat);

		if (!gesuch.getGesuchsperiode().getGueltigkeit().contains(konkubinatPlusMinDauerKonukubinat)) {
			return;
		}
		//Wechsel von 1 nach 2 -> nicht beenden
		if (istWechselVon1NachZwei(gesuch)) {
			return;
		}

		LocalDate zweiJahreKonkubinatNextMonth = Objects.requireNonNull(gesuch.extractFamiliensituation())
				.getStartKonkubinatPlusMindauerEndOfMonth(startKonkubinat);
		VerfuegungZeitabschnitt abschnittNachJahrenKonkubinat =
				createZeitabschnittWithinValidityPeriodOfRule(new DateRange(
						zweiJahreKonkubinatNextMonth,
						gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()));
		abschnittNachJahrenKonkubinat.setGesuchBeendenKonkubinatWirdInPeriodeXJahreAlt(true);
		neueZeitabschnitte.add(abschnittNachJahrenKonkubinat);

	}

	private boolean istWechselVon1NachZwei(@Nonnull Gesuch gesuch) {
		FamiliensituationContainer familiensituationContainer = gesuch.getFamiliensituationContainer();
		Familiensituation familiensituationJA = Objects.requireNonNull(familiensituationContainer).getFamiliensituationJA();
		if (null == familiensituationJA){
			return true;
		}
		boolean familiensituationKonkubinatKeinKind = familiensituationJA
				.getFamilienstatus() == EnumFamilienstatus.KONKUBINAT_KEIN_KIND;

		if (!familiensituationKonkubinatKeinKind) {
			return false;
		}

		boolean geteilteObhut = Boolean.TRUE.equals(familiensituationJA.getGeteilteObhut());
		boolean antragAlleine = familiensituationJA
				.getGesuchstellerKardinalitaet() == EnumGesuchstellerKardinalitaet.ALLEINE;

		if (geteilteObhut) {
			return antragAlleine;
		}

		return familiensituationJA.getUnterhaltsvereinbarung()
				!= UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
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
