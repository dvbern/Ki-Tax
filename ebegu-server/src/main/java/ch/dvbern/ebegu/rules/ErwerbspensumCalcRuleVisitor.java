package ch.dvbern.ebegu.rules;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.util.AnspruchBeschaeftigungAbhangigkeitTypVisitor;
import ch.dvbern.ebegu.util.Constants;

import static ch.dvbern.ebegu.enums.EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM;
import static ch.dvbern.ebegu.enums.EinstellungKey.MINIMALDAUER_KONKUBINAT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;

public class ErwerbspensumCalcRuleVisitor implements AnspruchBeschaeftigungAbhangigkeitTypVisitor<AbstractErwerbspensumCalcRule> {
	private final Map<EinstellungKey, Einstellung> einstellungMap;
	private final Locale locale;

	public ErwerbspensumCalcRuleVisitor(Map<EinstellungKey, Einstellung> einstellungMap, Locale locale) {
		this.einstellungMap = einstellungMap;
		this.locale = locale;
	}

	public AbstractErwerbspensumCalcRule getErwerbspesumCalcRule() {
		AnspruchBeschaeftigungAbhaengigkeitTyp abhaengigkeitTyp = AnspruchBeschaeftigungAbhaengigkeitTyp
			.getEnumValue(einstellungMap.get(ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM));
		return abhaengigkeitTyp.accept(this);
	}

	@Override
	public AbstractErwerbspensumCalcRule visitUnabhaengig() {
		return new ErwerbspensumNotRelevantForAnspruchCalcRule(
			RuleKey.ERWERBSPENSUM,
			RuleType.GRUNDREGEL_CALC,
			RuleValidity.ASIV,
			Constants.DEFAULT_GUELTIGKEIT,
			locale
		);
	}

	@Override
	public AbstractErwerbspensumCalcRule visitAbhaengig() {
		Einstellung minEWP_nichtEingeschultAsiv = einstellungMap.get(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		Einstellung minEWP_eingeschultAsiv = einstellungMap.get(MIN_ERWERBSPENSUM_EINGESCHULT);
		Einstellung paramMinDauerKonkubinat = einstellungMap.get(MINIMALDAUER_KONKUBINAT);
		Objects.requireNonNull(minEWP_nichtEingeschultAsiv, "Parameter MIN_ERWERBSPENSUM_NICHT_EINGESCHULT muss gesetzt sein");
		Objects.requireNonNull(minEWP_eingeschultAsiv, "Parameter MIN_ERWERBSPENSUM_EINGESCHULT muss gesetzt sein");
		return new ErwerbspensumAsivCalcRule(
			Constants.DEFAULT_GUELTIGKEIT,
			minEWP_nichtEingeschultAsiv.getValueAsInteger(),
			minEWP_eingeschultAsiv.getValueAsInteger(),
			paramMinDauerKonkubinat.getValueAsInteger(),
			locale);
	}

	@Override
	public AbstractErwerbspensumCalcRule visitMinimum() {
		return new ErwerbspensumMinimumCalcRule(
			RuleKey.ERWERBSPENSUM,
			RuleType.GRUNDREGEL_CALC,
			RuleValidity.ASIV,
			Constants.DEFAULT_GUELTIGKEIT,
			locale
		);
	}

	@Override
	public AbstractErwerbspensumCalcRule visitSchwyz() {
		Einstellung minEWPnichtEingeschultAsiv = einstellungMap.get(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		Einstellung minEWPeingeschultAsiv = einstellungMap.get(MIN_ERWERBSPENSUM_EINGESCHULT);
		Einstellung paramMinDauerKonkubinat = einstellungMap.get(MINIMALDAUER_KONKUBINAT);

		return new ErwerbspensumSchwyzCalcRule(
			Constants.DEFAULT_GUELTIGKEIT,
			minEWPnichtEingeschultAsiv.getValueAsInteger(),
			minEWPeingeschultAsiv.getValueAsInteger(),
			paramMinDauerKonkubinat.getValueAsInteger(),
			locale
		);
	}
}
