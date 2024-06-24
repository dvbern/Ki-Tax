package ch.dvbern.ebegu.rechner.rules;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungParameter;
import org.junit.Before;
import org.junit.Test;
import org.needle4j.annotation.ObjectUnderTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MahlzeitenverguenstigungTSRechnerRuleTest {

	@ObjectUnderTest
	private final MahlzeitenverguenstigungTSRechnerRule rechnerRule = new MahlzeitenverguenstigungTSRechnerRule(Locale.GERMAN);

	private final BGRechnerParameterDTO parameter = new BGRechnerParameterDTO();
	private final BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.GEMEINDE);
	private final RechnerRuleParameterDTO ruleParameter = new RechnerRuleParameterDTO();
	private final Map<BigDecimal, Integer> kostenMap = new HashMap<>();

	@Before
	public void init() {
		input.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		input.setVerguenstigungMahlzeitenBeantragt(true);
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		MahlzeitenverguenstigungParameter  mzvParameter = new MahlzeitenverguenstigungParameter(
			true,
			false,
			BigDecimal.valueOf(51000),
			BigDecimal.valueOf(70000),
			BigDecimal.valueOf(6),
			BigDecimal.valueOf(3),
			BigDecimal.valueOf(0),
			BigDecimal.valueOf(2)
		);
		parameter.setMahlzeitenverguenstigungParameter(mzvParameter);
		parameter.getMahlzeitenverguenstigungParameter().setEnabled(true);


		kostenMap.put(BigDecimal.TEN, 1);
	}

	@Test
	public void isConfiguredForGemeinde_MZVDisabled() {
		parameter.getMahlzeitenverguenstigungParameter().setEnabled(false);
		assertThat(rechnerRule.isConfigueredForGemeinde(parameter), is(false));
	}

	@Test
	public void isConfiguredForGemeinde_MZVEnabled() {
		assertThat(rechnerRule.isConfigueredForGemeinde(parameter), is(true));
	}

	@Test
	public void isRelevantForVerfuegung_NoTagesschule() {
		input.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		assertThat(rechnerRule.isRelevantForVerfuegung(input, parameter), is(false));
	}

	@Test
	public void isRelevantForVerfuegung_NoMZVVerguenstigungBeantragt() {
		input.setVerguenstigungMahlzeitenBeantragt(false);
		assertThat(rechnerRule.isRelevantForVerfuegung(input, parameter), is(false));
	}

	@Test
	public void isRelevantForVerfuegung_NoMZVDisabled() {
		parameter.getMahlzeitenverguenstigungParameter().setEnabled(false);
		assertThat(rechnerRule.isRelevantForVerfuegung(input, parameter), is(false));
	}

	@Test
	public void isRelevantForVerfuegung_relevant() {
		assertThat(rechnerRule.isRelevantForVerfuegung(input, parameter), is(true));
	}

	@Test
	public void prepareParamter_NoVerguenstigungAufrundEinkommen() {
		input.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(70001));

	   	rechnerRule.prepareParameter(input, parameter, ruleParameter);
		assertThat(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS_NEIN), is(true));
	}

	@Test
	public void prepareParamter_verguenstigungMitBetreuung() {
		input.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(50000));
		input.setVerpflegungskostenUndMahlzeitenMitBetreuung(kostenMap);

		rechnerRule.prepareParameter(input, parameter, ruleParameter);
		assertThat(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS), is(true));
	}

	@Test
	public void prepareParamter_verguenstigungOhneBetreuung() {
		input.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(50000));
		input.setVerpflegungskostenUndMahlzeitenOhneBetreuung(kostenMap);

		rechnerRule.prepareParameter(input, parameter, ruleParameter);
		assertThat(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS), is(true));
	}

	@Test
	public void prepareParamter_verguenstigungMitBetreuungZweiWoechentlich() {
		input.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(50000));
		input.setVerpflegungskostenUndMahlzeitenMitBetreuungZweiWochen(kostenMap);

		rechnerRule.prepareParameter(input, parameter, ruleParameter);
		assertThat(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS), is(true));
	}

	@Test
	public void prepareParamter_verguenstigungOhneBetreuungZweiWoechentlich() {
		input.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(50000));
		input.setVerpflegungskostenUndMahlzeitenOhneBetreuungZweiWochen(kostenMap);

		rechnerRule.prepareParameter(input, parameter, ruleParameter);
		assertThat(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS), is(true));
	}

}
