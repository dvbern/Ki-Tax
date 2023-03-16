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
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
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
		executeGesuchBeendenIfKonkubinatZweiJahreAlt(platz, inputData);
		executePartnerNotIdentischMitVorgesuch(platz, inputData);
	}

	private void executeGesuchBeendenIfKonkubinatZweiJahreAlt(
			@Nonnull AbstractPlatz platz,
			@Nonnull BGCalculationInput inputData) {

		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();
		if (null == familiensituation){
			return;
		}
		if (!familiensituation.getFamilienstatus().equals(EnumFamilienstatus.KONKUBINAT_KEIN_KIND)) {
			return;
		}
		LocalDate startKonkubinat = familiensituation.getStartKonkubinat();
		if (null == startKonkubinat){
			return;
		}
		LocalDate zweiJahreKonkubinat = startKonkubinat.plusYears(2);

		if(inputData.getParent().getGueltigkeit().getGueltigAb().isAfter(zweiJahreKonkubinat)){
			//das 2-Jahresdatum liegt in der Periode
			inputData.setAnspruchspensumProzent(ZERO);
			inputData.setAnspruchspensumRest(ZERO);
		}

	}

	private void executePartnerNotIdentischMitVorgesuch(
			@Nonnull AbstractPlatz platz,
			@Nonnull BGCalculationInput inputData) {
		if (null == inputData.getPartnerIdentischMitVorgesuch() || inputData.getPartnerIdentischMitVorgesuch()) {
			return;
		}
		Gesuch gesuch = platz.extractGesuch();
		String gesuchstellerPartner =
			(gesuch.getGesuchsteller2() != null) ? gesuch.getGesuchsteller2().extractFullName() : "";
		inputData.setAnspruchspensumProzent(ZERO);
		inputData.setAnspruchspensumRest(ZERO);
		inputData.addBemerkung(MsgKey.PARTNER_NOT_IDENTISCH_MIT_VORGESUCH, getLocale(), gesuchstellerPartner);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}

}
