package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
		evaluateGesuchBeenden(platz, inputData);
		executePartnerNotIdentischMitVorgesuch(platz, inputData);
	}

	private void evaluateGesuchBeenden(
			@Nonnull AbstractPlatz platz,
			@Nonnull BGCalculationInput inputData) {

		if (inputData.isGesuchBeendenKonkubinatMitZweiGS()) {
			executeGesuchBeenden(platz, inputData);
			return;
		}
	}

	private void executeGesuchBeenden(
			AbstractPlatz platz,
			BGCalculationInput inputData) {
		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();
		LocalDate startKonkubinat = Objects.requireNonNull(familiensituation).getStartKonkubinat();
		String konkubinatEndOfMonthPlusMinDauerKonkubinat =
				familiensituation.getStartKonkubinatPlusMindauerEndOfMonth(Objects.requireNonNull(startKonkubinat))
						.format(Constants.DATE_FORMATTER);

		inputData.setAnspruchspensumProzent(ZERO);
		inputData.setAnspruchspensumRest(ZERO);
		inputData.addBemerkung(
				MsgKey.FAMILIENSITUATION_X_JAHRE_KONKUBINAT_MSG,
				getLocale(),
				getGesuchstellerPartnerName(platz),
				konkubinatEndOfMonthPlusMinDauerKonkubinat);
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
			return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
		}

	}
