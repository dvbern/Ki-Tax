package ch.dvbern.ebegu.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.util.Locale.GERMAN;

public class BetreuungsgutscheinConfiguratorTest extends AbstractBGRechnerTest {

	private BetreuungsgutscheinConfigurator ruleConfigurator;
	private Map<EinstellungKey, Einstellung> einstellungenGemaessAsiv = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(gesuchsperiodeOfEvaluator);
	private KitaxUebergangsloesungParameter kitaxParams = TestDataUtil.geKitaxUebergangsloesungParameter();

	private static final int ANZAHL_RULES_ASIV = 31;

	@Before
	public void setUp() {
		ruleConfigurator = new BetreuungsgutscheinConfigurator();
		Set<EinstellungKey> keysToLoad = ruleConfigurator.getRequiredParametersForGemeinde();
		// Sicherstellen, dass wir alle Einstellungen gemaess Configurator haben
		Assert.assertEquals(keysToLoad.size(), einstellungenGemaessAsiv.size());
	}

	@Test
	public void ohneSpezialEinstellungenNurAsivRules() {
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeOfEvaluator, einstellungenGemaessAsiv, kitaxParams, GERMAN);
		// Keine zusaetzliche Regel erwartet
		Assert.assertEquals(ANZAHL_RULES_ASIV, rules.size());
		assertContainsRule(rules, ErwerbspensumGemeindeAbschnittRule.class, 0);
		assertContainsRule(rules, ErwerbspensumGemeindeCalcRule.class, 0);
	}

	@Test
	public void mitFreiwilligenarbeitEnabledAberGleicherWertWieAsiv() {
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(einstellungenGemaessAsiv);
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED).setValue("true");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT).setValue("0");
		// Keine zusaetzliche Regel erwartet
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeOfEvaluator, einstellungenGemeinde, kitaxParams, GERMAN);
		Assert.assertEquals(ANZAHL_RULES_ASIV, rules.size());
		assertContainsRule(rules, ErwerbspensumGemeindeAbschnittRule.class, 0);
		assertContainsRule(rules, ErwerbspensumGemeindeCalcRule.class, 0);
	}

	@Test
	public void mitFreiwilligenarbeitEnabledMitAnderemWert() {
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(einstellungenGemaessAsiv);
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED).setValue("true");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT).setValue("15");
		// Zusaetzliche Regel erwartet, da Wert abweicht
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeOfEvaluator, einstellungenGemeinde, kitaxParams, GERMAN);
		Assert.assertEquals(ANZAHL_RULES_ASIV + 2, rules.size());
		assertContainsRule(rules, ErwerbspensumGemeindeAbschnittRule.class, 1);
		assertContainsRule(rules, ErwerbspensumGemeindeCalcRule.class, 0);
	}

	@Test
	public void mitUeberschriebenemMinPensumVorschuleGleicherWertWieAsiv() {
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(einstellungenGemaessAsiv);
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT).setValue(EinstellungenDefaultWerteAsiv.EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		// Keine zusaetzliche Regel erwartet
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeOfEvaluator, einstellungenGemeinde, kitaxParams, GERMAN);
		Assert.assertEquals(ANZAHL_RULES_ASIV, rules.size());
		assertContainsRule(rules, ErwerbspensumGemeindeAbschnittRule.class, 0);
		assertContainsRule(rules, ErwerbspensumGemeindeCalcRule.class, 0);
	}

	@Test
	public void mitUeberschriebenemMinPensumVorschule() {
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(einstellungenGemaessAsiv);
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT).setValue("80");
		// 1 zusaetzliche Regel erwartet
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeOfEvaluator, einstellungenGemeinde, kitaxParams, GERMAN);
		Assert.assertEquals(ANZAHL_RULES_ASIV + 1, rules.size());
		assertContainsRule(rules, ErwerbspensumGemeindeAbschnittRule.class, 0);
		assertContainsRule(rules, ErwerbspensumGemeindeCalcRule.class, 1);
	}

	@Test
	public void mitAllenSondereinstellungen() {
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(einstellungenGemaessAsiv);
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED).setValue("true");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT).setValue("15");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT).setValue("80");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT).setValue("80");
		// 2 zusaetzliche Regeln erwartet
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeOfEvaluator, einstellungenGemeinde, kitaxParams, GERMAN);
		Assert.assertEquals(ANZAHL_RULES_ASIV + 3, rules.size());
		assertContainsRule(rules, ErwerbspensumGemeindeAbschnittRule.class, 1);
		assertContainsRule(rules, ErwerbspensumGemeindeCalcRule.class, 1);
	}

	@Test
	public void gemeindeParisOhneSondereinstellungen() {
		final Gemeinde gemeindeParis = TestDataUtil.createGemeindeParis();
		evaluator = createEvaluator(gesuchsperiodeOfEvaluator, gemeindeParis);

		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeParis, einstellungenGemaessAsiv, kitaxParams, GERMAN);
		// ErwerbspensumGemeindeCalcRule wird bei Paris/Bern immer hinzugefuegt, wegen dem MinEWP bei FEBR,
		// welches nicht ueber eine Einstellung geloest ist
		Assert.assertEquals(ANZAHL_RULES_ASIV + 1, rules.size());
		assertContainsRule(rules, ErwerbspensumGemeindeAbschnittRule.class, 0);
		assertContainsRule(rules, ErwerbspensumGemeindeCalcRule.class, 1);
	}

	@Test
	public void gemeindeParisMitUeberschriebenemMinErwerbspensum() {
		final Gemeinde gemeindeParis = TestDataUtil.createGemeindeParis();
		evaluator = createEvaluator(gesuchsperiodeOfEvaluator, gemeindeParis);
		// Gemeinde Paris hat Uebergangsloesung, d.h. es soll pr
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(einstellungenGemaessAsiv);
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT).setValue("80");

		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeParis, einstellungenGemeinde, kitaxParams, GERMAN);
		// Es hat zwar nur eine Einstellunge geaendert, aber wegen der Uebergangsloesung der Gemeinde Paris
		// kommt die Regel doppelt: Einmal fuer den Zeitraum vor dem Stichtag, einmal nach dem Stichtag
		Assert.assertEquals(ANZAHL_RULES_ASIV +  2, rules.size());
		assertContainsRule(rules, ErwerbspensumGemeindeAbschnittRule.class, 0);
		assertContainsRule(rules, ErwerbspensumGemeindeCalcRule.class, 2);
	}


	private void assertContainsRule(List<Rule> rules, Class<? extends Rule> classOfRuleToFind, int expectedCount) {
		int found = 0;
		for (Rule rule1 : rules) {
			if (rule1.getClass().equals(classOfRuleToFind)) {
				found++;
			}
		}
		Assert.assertEquals(expectedCount, found);
	}
}
