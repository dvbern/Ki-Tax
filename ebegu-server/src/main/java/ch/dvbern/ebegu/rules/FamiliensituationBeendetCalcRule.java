package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

public class FamiliensituationBeendetCalcRule extends AbstractCalcRule {
	public static final int ZERO = 0;

	protected FamiliensituationBeendetCalcRule(
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale) {
		super(RuleKey.FAMILIENSITUATION, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	void executeRule(
			@Nonnull AbstractPlatz platz,
			@Nonnull BGCalculationInput inputData) {
		executeGesuchBeendenIfNecessary(platz, inputData);
		executePartnerNotIdentischMitVorgesuch(platz, inputData);
	}

	private void executeGesuchBeendenIfNecessary(
			@Nonnull AbstractPlatz platz,
			@Nonnull BGCalculationInput inputData) {

		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();
		if (null == familiensituation) {
			return;
		}
		if (!familiensituation.getFamilienstatus().equals(EnumFamilienstatus.KONKUBINAT_KEIN_KIND)) {
			return;
		}
		LocalDate startKonkubinat = familiensituation.getStartKonkubinat();
		if (null == startKonkubinat) {
			return;
		}
		LocalDate startKonkubinatPlusMindauer = familiensituation.getStartKonkubinatPlusMindauer(startKonkubinat);

		String konkubinatEndOfMonthPlusMinDauerKonkubinat =
				familiensituation.getStartKonkubinatPlusMindauerEndOfMonth(startKonkubinat)
						.format(Constants.DATE_FORMATTER);

		if (sollBeendetWerden(
				familiensituation,
				inputData.getParent().getGueltigkeit(),
				startKonkubinatPlusMindauer)) {
			inputData.setAnspruchspensumProzent(ZERO);
			inputData.setAnspruchspensumRest(ZERO);
			inputData.addBemerkung(
					MsgKey.FAMILIENSITUATION_X_JAHRE_KONKUBINAT_MSG,
					getLocale(),
					getGesuchstellerPartnerName(platz),
					konkubinatEndOfMonthPlusMinDauerKonkubinat);
		}
	}

	private boolean sollBeendetWerden(
			@Nonnull Familiensituation familiensituation,
			@Nonnull DateRange gueltigkeit,
			@Nonnull LocalDate startKonkubinatPlusMindauer) {
		//das x-Jahresdatum liegt in der Periode
		return isGesuchBeendenNoetig(familiensituation) &&
				gueltigkeit.isAfter(startKonkubinatPlusMindauer);
	}

	private boolean isGesuchBeendenNoetig(@Nonnull Familiensituation familiensituation) {
		if (familiensituation.getFamilienstatus().equals(EnumFamilienstatus.KONKUBINAT_KEIN_KIND) &&
				EbeguUtil.isNotNullAndTrue(familiensituation.getGeteilteObhut()) &&
				Objects.requireNonNull(familiensituation.getGesuchstellerKardinalitaet())
						.equals(EnumGesuchstellerKardinalitaet.ALLEINE)) {
			//Ja, Konkubinatspartner/in ohne gemeinsames Kind AND
			//Geteilte Obhut Ja AND
			//Antrag alleine
			return false;
		} else if (EbeguUtil.isNotNullAndFalse(familiensituation.getGeteilteObhut())) {
			Objects.requireNonNull(familiensituation.getUnterhaltsvereinbarung());
			// Geteilte Obhut nein AND
			// Unterhaltsvereinbarung Ja oder nicht-möglich
			return !familiensituation.getUnterhaltsvereinbarung()
					.equals(UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH) &&
					!familiensituation.getUnterhaltsvereinbarung()
							.equals(UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG);
		}
		return true;
	}

		private void executePartnerNotIdentischMitVorgesuch (
				@Nonnull AbstractPlatz platz,
				@Nonnull BGCalculationInput inputData){
			if (null == inputData.getPartnerIdentischMitVorgesuch() || inputData.getPartnerIdentischMitVorgesuch()) {
				return;
			}
			inputData.setAnspruchspensumProzent(ZERO);
			inputData.setAnspruchspensumRest(ZERO);
			inputData.addBemerkung(
					MsgKey.PARTNER_NOT_IDENTISCH_MIT_VORGESUCH,
					getLocale(),
					getGesuchstellerPartnerName(platz));
		}

		private static String getGesuchstellerPartnerName (AbstractPlatz platz){
			Gesuch gesuch = platz.extractGesuch();
			String gesuchstellerPartner =
					(gesuch.getGesuchsteller2() != null) ? gesuch.getGesuchsteller2().extractFullName() : "";
			return gesuchstellerPartner;
		}

		@Override
		protected List<BetreuungsangebotTyp> getAnwendbareAngebote () {
			return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
		}

	}
