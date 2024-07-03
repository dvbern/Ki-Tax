package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;;
import static org.hamcrest.Matchers.is;;

class BetreuungspensumAbschnittRuleTest {

	BetreuungspensumAbschnittRule rule =
		new BetreuungspensumAbschnittRule(
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE,
			TestDataUtil.geKitaxUebergangsloesungParameter());

	@Test
	void hoehererBeitragShouldNotBeSetOnInputfNotBestaetigt() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
			Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
			BetreuungsangebotTyp.KITA,
			20,
			BigDecimal.valueOf(2000));
		betreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		Objects.requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA())
			.setErweiterteBeduerfnisseBestaetigt(false);
		betreuung.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = rule.createVerfuegungsZeitabschnitte(betreuung);

		assertThat(verfuegungsZeitabschnitte.get(0).getRelevantBgCalculationInput().getBedarfsstufe(), nullValue());
	}

	@Test
	void bemerkungHoehererBeitragNotVerrechnetShouldBeSetOnInputfNotBestaetigt() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
			Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
			BetreuungsangebotTyp.KITA,
			20,
			BigDecimal.valueOf(2000));
		betreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		Objects.requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA())
			.setErweiterteBeduerfnisseBestaetigt(false);
		betreuung.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = rule.createVerfuegungsZeitabschnitte(betreuung);

		assertThat(verfuegungsZeitabschnitte.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.BEDARFSSTUFE_NICHT_VERRECHNET_MSG), is(true));
	}

	@Test
	void hoehererBeitragShouldBeSetOnInputIfBestaetigt() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
			Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
			BetreuungsangebotTyp.KITA,
			20,
			BigDecimal.valueOf(2000));
		betreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		Objects.requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA())
			.setErweiterteBeduerfnisseBestaetigt(true);
		betreuung.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = rule.createVerfuegungsZeitabschnitte(betreuung);

		assertThat(verfuegungsZeitabschnitte.get(0).getRelevantBgCalculationInput().getBedarfsstufe(), notNullValue());
	}
}
