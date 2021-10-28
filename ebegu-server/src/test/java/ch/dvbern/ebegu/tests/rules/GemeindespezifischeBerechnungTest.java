package ch.dvbern.ebegu.tests.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinConfigurator;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinExecutor;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.rules.MonatsRule;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.MAX_ERWERBSPENSUM_FREIWILLIGENARBEIT;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.ZUSCHLAG_ERWERBSPENSUM;

public class GemeindespezifischeBerechnungTest extends AbstractBGRechnerTest {

	private BetreuungsgutscheinConfigurator ruleConfigurator = new BetreuungsgutscheinConfigurator();
	private Map<EinstellungKey, Einstellung> einstellungenGemaessAsiv = EbeguRuleTestsHelper.getAllEinstellungen(gesuchsperiodeOfEvaluator);
	private BetreuungsgutscheinExecutor executor = new BetreuungsgutscheinExecutor(true, einstellungenGemaessAsiv);
	private KitaxUebergangsloesungParameter kitaxParams = TestDataUtil.geKitaxUebergangsloesungParameter();

	private List<VerfuegungZeitabschnitt> initialerRestanspruch = new ArrayList<>();
	private static final boolean IS_DEBUG = false;
	private static final Locale GERMAN = Locale.GERMAN;

	@Before
	public void setUp() {
		final VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(Constants.DEFAULT_GUELTIGKEIT);
		zeitabschnitt.setAnspruchspensumRestForAsivAndGemeinde(-1);
		initialerRestanspruch.add(zeitabschnitt);
	}

	/**
	 * Alle Einstellungen gemaess Defaults von ASIV.
	 * EWP 20%, Zuschlag 20% -> BG 40%
	 */
	@Test
	public void gemeindeOhneSpezialeinstellungen() {
		// Normale Default Einstellungen nach ASIV
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = prepareEinstellungen(
			MAX_ERWERBSPENSUM_FREIWILLIGENARBEIT, MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, MIN_ERWERBSPENSUM_EINGESCHULT, ZUSCHLAG_ERWERBSPENSUM);
		// EWP 20 -> erreicht
		final AbstractPlatz platz = preparePlatz(EinschulungTyp.VORSCHULALTER, 20, 0);

		final VerfuegungZeitabschnitt abschnitt = calculate(einstellungenGemeinde, platz);
		Assert.assertFalse(
			"Keine Gemeindespezifische Berechnung, die Einstellungen entsprechen den Defaults von ASIV",
			abschnitt.isHasGemeindeSpezifischeBerechnung());
		Assert.assertNull(
			"Entsprechend soll das Gemeinde-Result null sein",
			abschnitt.getBgCalculationResultGemeinde());

		Assert.assertEquals(40, abschnitt.getBgCalculationInputAsiv().getBgPensumProzent().intValue());
		Assert.assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	/**
	 * Alle Einstellungen gemaess Defaults von ASIV.
	 * EWP 20% Angestellt, 20% Freiwiligenarbeit (gemaess ASIV nicht beachtet), Zuschlag 20% -> BG 40%
	 */
	@Test
	public void gemeindeOhneSpezialeinstellungenMitFreiwilligenarbeit() {
		// Normale Default Einstellungen nach ASIV
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = prepareEinstellungen(
			MAX_ERWERBSPENSUM_FREIWILLIGENARBEIT, MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, MIN_ERWERBSPENSUM_EINGESCHULT, ZUSCHLAG_ERWERBSPENSUM);
		// EWP 20 Angestellt, plus 20 Freiwilligenarbeit, welche aber nicht beachtet werden darf.
		final AbstractPlatz platz = preparePlatz(EinschulungTyp.VORSCHULALTER, 20, 20);

		final VerfuegungZeitabschnitt abschnitt = calculate(einstellungenGemeinde, platz);
		Assert.assertFalse(
			"Keine Gemeindespezifische Berechnung, die Einstellungen entsprechen den Defaults von ASIV. "
				+ "Freiwilligenarbeit wird nicht gewaehrt",
			abschnitt.isHasGemeindeSpezifischeBerechnung());
		Assert.assertNull(
			"Entsprechend soll das Gemeinde-Result null sein",
			abschnitt.getBgCalculationResultGemeinde());

		Assert.assertEquals(40, abschnitt.getBgCalculationInputAsiv().getBgPensumProzent().intValue());
		Assert.assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	/**
	 * Einstellung zu Freiwilligenarbeit angepasst: Max 15%
	 * EWP 20% Angestellt, 15% Freiwiligenarbeit (gemaess ASIV nicht beachtet), Zuschlag 20% -> BG 55%
	 */
	@Test
	public void gemeindeFreiwilligenarbeitZugelassenMitFreiwilligenarbeit() {
		// Einstellung zu Freiwilligenarbeit angepasst
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = prepareEinstellungen(
			15, MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, MIN_ERWERBSPENSUM_EINGESCHULT, ZUSCHLAG_ERWERBSPENSUM);
		// EWP 20 Angestellt, plus 20 Freiwilligenarbeit, wovon 15% gewaehrt werden
		final AbstractPlatz platz = preparePlatz(EinschulungTyp.VORSCHULALTER, 20, 20);

		final VerfuegungZeitabschnitt abschnitt = calculate(einstellungenGemeinde, platz);
		Assert.assertTrue(
			"Gemeindespezifische Berechnung, Freiwilligenarbeit gewaehrt",
			abschnitt.isHasGemeindeSpezifischeBerechnung());
		Assert.assertNotNull(
			"Entsprechend darf das Gemeinde-Result nicht null sein",
			abschnitt.getBgCalculationResultGemeinde());

		Assert.assertEquals(40, abschnitt.getBgCalculationInputAsiv().getBgPensumProzent().intValue());
		Assert.assertEquals(55, abschnitt.getBgCalculationInputGemeinde().getBgPensumProzent().intValue());
		Assert.assertEquals(3, abschnitt.getBemerkungenList().uniqueSize());
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_FREIWILLIGENARBEIT));
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	/**
	 * Einstellungen zu den mimimalen Erwerbspensen reduziert.
	 * EWP 20% -> Wird auch gemaess ASIV erreicht
	 */
	@Test
	public void gemeindeMinEwpReduziert_PensumAuchNachAsivErreicht() {
		// Einstellungen zum Minimalen EWP ueberschrieben
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = prepareEinstellungen(
			MAX_ERWERBSPENSUM_FREIWILLIGENARBEIT,
			MIN_ERWERBSPENSUM_NICHT_EINGESCHULT-10,
			MIN_ERWERBSPENSUM_EINGESCHULT-10,
			ZUSCHLAG_ERWERBSPENSUM);
		// EWP wird auch gemaess ASIV erreicht
		final AbstractPlatz platz = preparePlatz(EinschulungTyp.VORSCHULALTER, 20, 0);

		final VerfuegungZeitabschnitt abschnitt = calculate(einstellungenGemeinde, platz);
		Assert.assertTrue(
			"Gemeindespezifische Berechnung, da die Minimalen Erwerbspensen ueberschrieben wurden",
			abschnitt.isHasGemeindeSpezifischeBerechnung());
		Assert.assertNotNull(
			"Entsprechend darf das Gemeinde-Result nicht null sein",
			abschnitt.getBgCalculationResultGemeinde());

		Assert.assertEquals("Die Werte sind sowohl nach ASIV wie nach Gmde erreicht",
			40, abschnitt.getBgCalculationInputAsiv().getBgPensumProzent().intValue());
		Assert.assertEquals("Die Werte sind sowohl nach ASIV wie nach Gmde erreicht",
			40, abschnitt.getBgCalculationInputGemeinde().getBgPensumProzent().intValue());
		Assert.assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	/**
	 * Einstellungen zu den mimimalen Erwerbspensen reduziert.
	 * EWP 10% -> Nur fuer Gemeinde erreicht, nach ASIV nicht
	 */
	@Test
	public void gemeindeMinEwpReduziert_PensumNachAsivNichtErreicht() {
		// Einstellungen zum Minimalen EWP ueberschrieben
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = prepareEinstellungen(
			MAX_ERWERBSPENSUM_FREIWILLIGENARBEIT,
			MIN_ERWERBSPENSUM_NICHT_EINGESCHULT-10,
			MIN_ERWERBSPENSUM_EINGESCHULT-10,
			ZUSCHLAG_ERWERBSPENSUM);
		// EWP wird auch gemaess ASIV erreicht
		final AbstractPlatz platz = preparePlatz(EinschulungTyp.VORSCHULALTER, 10, 0);

		final VerfuegungZeitabschnitt abschnitt = calculate(einstellungenGemeinde, platz);
		Assert.assertTrue(
			"Gemeindespezifische Berechnung, da die Minimalen Erwerbspensen ueberschrieben wurden",
			abschnitt.isHasGemeindeSpezifischeBerechnung());
		Assert.assertNotNull(
			"Entsprechend darf das Gemeinde-Result nicht null sein",
			abschnitt.getBgCalculationResultGemeinde());

		Assert.assertEquals("BG-Pensum gemaess ASIV 0", 0, abschnitt.getBgCalculationInputAsiv().getBgPensumProzent().intValue());
		Assert.assertEquals("BG-Pensum gemaess Gemeinde 30", 30, abschnitt.getBgCalculationInputGemeinde().getBgPensumProzent().intValue());
		Assert.assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	/**
	 * Einstellung Zuschlag zum Erwerbspensum reduziert
	 * DWP 20% -> Zuschlag (reduziert) 10% -> BG-Pensum 30%. Darf nicht zu einem Gemeinde-Gutschein fuehren!
	 */
	@Test
	public void gemeindeZuschlagReduziert() {
		// Einstellungen zum Minimalen EWP ueberschrieben
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = prepareEinstellungen(
			MAX_ERWERBSPENSUM_FREIWILLIGENARBEIT,
			MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
			MIN_ERWERBSPENSUM_EINGESCHULT,
			ZUSCHLAG_ERWERBSPENSUM-10);
		// EWP wird auch gemaess ASIV erreicht
		final AbstractPlatz platz = preparePlatz(EinschulungTyp.VORSCHULALTER, 20, 0);

		final VerfuegungZeitabschnitt abschnitt = calculate(einstellungenGemeinde, platz);
		Assert.assertFalse(
			"Keine Gemeindespezifische Berechnung, da nur Zuschlag reduziert",
			abschnitt.isHasGemeindeSpezifischeBerechnung());
		Assert.assertNull(
			"Entsprechend soll das Gemeinde-Result null sein",
			abschnitt.getBgCalculationResultGemeinde());

		Assert.assertEquals("BG-Pensum gemaess ASIV 0", 30, abschnitt.getBgCalculationInputAsiv().getBgPensumProzent().intValue());
		Assert.assertEquals(2, abschnitt.getBemerkungenList().uniqueSize());
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(abschnitt.getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	private VerfuegungZeitabschnitt calculate(Map<EinstellungKey, Einstellung> einstellungenGemeinde, AbstractPlatz platz) {
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(gemeindeOfEvaluator, einstellungenGemeinde, kitaxParams, GERMAN);
		TestDataUtil.calculateFinanzDaten(platz.extractGesuch());
		List<VerfuegungZeitabschnitt> result = executor.executeRules(rules, platz, initialerRestanspruch);
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		MonatsRule monatsRule = new MonatsRule(IS_DEBUG);
		result = monatsRule.executeIfApplicable(platz, result);
		Assert.assertNotNull(result);
		Assert.assertEquals(12, result.size());
		executor.calculateRechner(new BGRechnerParameterDTO(einstellungenGemeinde, gesuchsperiodeOfEvaluator, gemeindeOfEvaluator), kitaxParams, GERMAN,
			Collections.emptyList(),	platz, result);
		Assert.assertNotNull(result);
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		Assert.assertNotNull(abschnitt);
		return abschnitt;
	}

	private Map<EinstellungKey, Einstellung> prepareEinstellungen(
		int maxFreiwilligenarbeit, int minEwpNichtEingeschult, int minEwpEingeschult, int zuschlagEWP
	) {
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(einstellungenGemaessAsiv);
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED).setValue("true");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT).setValue(String.valueOf(maxFreiwilligenarbeit));
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT).setValue(String.valueOf(minEwpNichtEingeschult));
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT).setValue(String.valueOf(minEwpEingeschult));
		einstellungenGemeinde.get(EinstellungKey.ERWERBSPENSUM_ZUSCHLAG).setValue(String.valueOf(zuschlagEWP));
		return einstellungenGemeinde;
	}

	private AbstractPlatz preparePlatz(@Nonnull EinschulungTyp schulstufe, int ewpAngestellt, int ewpFreiwillig) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		final BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum());
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(new BigDecimal(100));
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		betreuung.getKind().getKindJA().setEinschulungTyp(schulstufe);
		final GesuchstellerContainer gesuchsteller1 = betreuung.extractGesuch().getGesuchsteller1();
		Assert.assertNotNull(gesuchsteller1);
		gesuchsteller1.getErwerbspensenContainers().clear();
		gesuchsteller1.getErwerbspensenContainers().add(TestDataUtil.createErwerbspensum(ewpAngestellt, Taetigkeit.ANGESTELLT));
		gesuchsteller1.getErwerbspensenContainers().add(TestDataUtil.createErwerbspensum(ewpFreiwillig, Taetigkeit.FREIWILLIGENARBEIT));
		gesuchsteller1.addAdresse(TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuchsteller1));
		final GesuchstellerAdresse gesuchstellerAdresseJA = gesuchsteller1.getAdressen().get(0).getGesuchstellerAdresseJA();
		Assert.assertNotNull(gesuchstellerAdresseJA);
		gesuchstellerAdresseJA.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		gesuchstellerAdresseJA.setNichtInGemeinde(false);
		return betreuung;
	}
}
