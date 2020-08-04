/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rechner.AbstractRechner;
import ch.dvbern.ebegu.rechner.BGRechnerFactory;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.rechner.rules.ZusaetzlicherBabyGutscheinRechnerRule;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public class ZusaetzlicherBabyGutscheinBerechnungTest extends AbstractBGRechnerTest {

	private static final MathUtil MATH = MathUtil.DEFAULT;
	private static final Locale GERMAN = Locale.GERMAN;

	private AbstractRechner rechner;
	private BGRechnerParameterDTO rechnerParameterDTO;
	private VerfuegungZeitabschnitt abschnittToTest;

	@Before
	public void setUp() {
		this.rechnerParameterDTO = prepareBgRechnerParameterDTO();
		this.rechner = prepareRechner();
		this.abschnittToTest = createVerfuegungZeitabschnitt();
	}

	@Test
	public void gemeindeBabyGutschein_berechnung() {
		// Sicherstellen, dass der minimale Elternbeitrag nichts beeinflusst:
		this.abschnittToTest.setMonatlicheBetreuungskostenForAsivAndGemeinde(MATH.from(30000));

		assertBabygutschein(100, MATH.from(0), MATH.from(0));
		assertBabygutschein(100, MATH.from(43000), MATH.from(0));
		assertBabygutschein(100, MATH.from(60000), MATH.from(145.30));
		assertBabygutschein(100, MATH.from(80000), MATH.from(316.20));
		assertBabygutschein(100, MATH.from(120000), MATH.from(658.10));
		assertBabygutschein(100, MATH.from(159000), MATH.from(991.45));
		assertBabygutschein(100, MATH.from(159999), MATH.from(999.95));
		// Ab 160'000 betraegt der Zuschlag 0
		assertBabygutschein(100, MATH.from(160000), MATH.from(0));
		assertBabygutschein(100, MATH.from(200000), MATH.from(0));
	}

	@Test
	public void gemeindeBabyGutschein_minimalerElternbeitrag() {
		// Der Minimale Elternbeitrag von CHR 7 / Tag darf nicht unterschritten werden
		// Dies entspricht bei einem 100% BG: 7 * 20 = 140.-
		final VerfuegungZeitabschnitt abschnitt = prepareVerfuegungZeitabschnitt(100, MATH.from(80000));

		// Zum Vergleichen merken wir uns zuerst den Betrag des Gutscheins bei "genug hohen" Betreuungskosten:
		// Dies entspricht dem Maximalen Baby-Gutschein (ungekuerzten) fuer diese Konstellation
		abschnitt.setMonatlicheBetreuungskostenForAsivAndGemeinde(MATH.from(30000));
		rechner.calculate(abschnitt, rechnerParameterDTO);
		Assert.assertNotNull(abschnitt.getBgCalculationResultGemeinde());
		BigDecimal babyGutscheinMax = MATH.subtract(
			abschnitt.getBgCalculationResultGemeinde().getVerguenstigung(),
			abschnitt.getBgCalculationResultAsiv().getVerguenstigung());
		Assert.assertEquals(MATH.from(316.20), babyGutscheinMax);

		// Wir setzen die Monatlichen Betreuungskosten auf einen Wert, der knapp ueber dem berechneten Gemeindegutschein liegt
		// so dass der minimale Elternbeitrag zum Zug kommt:
		abschnitt.setMonatlicheBetreuungskostenForAsivAndGemeinde(MATH.from(2400));
		// Berechnung durchfuehren
		rechner.calculate(abschnitt, rechnerParameterDTO);
		final BGCalculationResult resultAsiv = abschnitt.getBgCalculationResultAsiv();
		final BGCalculationResult resultGmde = abschnitt.getBgCalculationResultGemeinde();
		Assert.assertNotNull(resultAsiv);
		Assert.assertNotNull(resultGmde);

		// Die Vollkosten betragen immer 2400
		Assert.assertEquals(MATH.from(2400), resultAsiv.getVollkosten());
		Assert.assertEquals(MATH.from(2400), resultGmde.getVollkosten());

		// Verguenstigung vor Kuerzungen
		Assert.assertEquals(MATH.from(2051.30), resultAsiv.getVerguenstigungOhneBeruecksichtigungVollkosten());
		Assert.assertEquals(MATH.from(2051.30), resultAsiv.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());

		Assert.assertEquals(MATH.from(2367.50), resultGmde.getVerguenstigungOhneBeruecksichtigungVollkosten());
		Assert.assertEquals(MATH.from(2367.50), resultGmde.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());

		// Der Unterschied zwischen ASIV und Gemeinde beim Ungekuerzten Gutschein entspricht dem Baby-Gutschein, wenn
		// die Betreuungskosten so hoch *waeren*, dass der Elternbeitrag keine Rolle spielt:
		final BigDecimal differenzVerguenstigungOhneBeruecksichtigungVollkosten = MATH.subtract(
			resultGmde.getVerguenstigungOhneBeruecksichtigungVollkosten(),
			resultAsiv.getVerguenstigungOhneBeruecksichtigungVollkosten());
		Assert.assertEquals(babyGutscheinMax, differenzVerguenstigungOhneBeruecksichtigungVollkosten);

		final BigDecimal differenzVerguenstigungOhneBeruecksichtigungMinimalbeitrag = MATH.subtract(
			resultGmde.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag(),
			resultAsiv.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		Assert.assertEquals(babyGutscheinMax, differenzVerguenstigungOhneBeruecksichtigungMinimalbeitrag);

		// Die Verguenstigung entspricht bei ASIV dem ungekuerzten Wert
		Assert.assertEquals(MATH.from(2051.30), resultAsiv.getVerguenstigung());
		// Bei der Gemeinde wurde sie auf die Vollkosten begrenzt (minus 140.- minimaler Elternbeitrag)
		Assert.assertEquals(MATH.from(2260.00), resultGmde.getVerguenstigung());

		final BigDecimal babyGutschein = MATH.subtract(
			resultGmde.getVerguenstigung(),
			resultAsiv.getVerguenstigung());

		// Der Minimale Elternbeitrag ist bei beiden gleich und entspricht den CHF 7 * 20 Tage
		Assert.assertEquals(MATH.from(140), resultAsiv.getMinimalerElternbeitrag());
		Assert.assertEquals(MATH.from(140), resultGmde.getMinimalerElternbeitrag());

		// Der "effektive" Elternbeitrag ist bei ASIV hoeher als das Minimum (da kein Baby-Gutschein)
		Assert.assertEquals(MATH.from(348.70), resultAsiv.getElternbeitrag());
		// Entsprechend ist der gekuerzte Minimale Elternbeitrag bei ASIV 0 (da der gesamte Minimale Elternbeitrag im
		// Effektiven Elternbeitrag "Platz hat"
		Assert.assertEquals(MATH.from(0), resultAsiv.getMinimalerElternbeitragGekuerzt());

		// Bei der Gemeinde entspricht der Effektive Elternbeitrag genau dem Minimalen Elternbeitrag
		Assert.assertEquals(MATH.from(140), resultGmde.getElternbeitrag());
		// Der gekuerzte Minimale Elternbeitrag entspricht wiederum der Differenz, welche vom Baby-Gutschein her kommt
		Assert.assertEquals(MATH.from(107.50), resultGmde.getMinimalerElternbeitragGekuerzt());

		// Also: der ASIV-Elternbeitrag entspricht dem Gmde-gekuerztenElternbeitrag + Baby-Gutschein
		Assert.assertEquals(resultAsiv.getElternbeitrag(), MATH.add(resultGmde.getElternbeitrag(), babyGutschein));
	}

	private void assertBabygutschein(int bgPensum, @Nonnull BigDecimal massgebendesEinkommen, @Nonnull BigDecimal expectedBabygutscheinMonat) {
		final VerfuegungZeitabschnitt abschnitt = prepareVerfuegungZeitabschnitt(bgPensum, massgebendesEinkommen);
		rechner.calculate(abschnitt, rechnerParameterDTO);

		Assert.assertNotNull(abschnitt);
		Assert.assertTrue(abschnitt.isHasGemeindeSpezifischeBerechnung());
		Assert.assertNotNull(abschnitt.getBgCalculationResultAsiv());
		Assert.assertNotNull(abschnitt.getBgCalculationResultGemeinde());

		final BigDecimal verguenstigungAsiv = abschnitt.getBgCalculationResultAsiv().getVerguenstigung();
		final BigDecimal verguenstigungGemeinde = abschnitt.getBgCalculationResultGemeinde().getVerguenstigung();
		Assert.assertNotNull(verguenstigungAsiv);
		Assert.assertNotNull(verguenstigungGemeinde);

		// Da alle anderen Einstellungen ASIV entsprechen, entspricht die Differenz von ASIV und Gemeinde dem Babygutschein
		BigDecimal babyGutschein = MATH.subtract(verguenstigungGemeinde, verguenstigungAsiv);
		Assert.assertEquals(expectedBabygutscheinMonat, babyGutschein);
	}

	@Nonnull
	private VerfuegungZeitabschnitt prepareVerfuegungZeitabschnitt(int bgPensum, @Nonnull BigDecimal massgebendesEinkommen) {
		abschnittToTest.setAnspruchspensumProzentForAsivAndGemeinde(bgPensum);
		abschnittToTest.setBetreuungspensumProzentForAsivAndGemeinde(MathUtil.DEFAULT.from(bgPensum));
		abschnittToTest.getBgCalculationInputAsiv().setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
		abschnittToTest.getBgCalculationInputGemeinde().setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
		return abschnittToTest;
	}

	private VerfuegungZeitabschnitt createVerfuegungZeitabschnitt() {
		final VerfuegungZeitabschnitt abschnitt = new VerfuegungZeitabschnitt();
		LocalDate stichtag = LocalDate.now();
		DateRange fullMonth = new DateRange(stichtag.with(firstDayOfMonth()), stichtag.with(lastDayOfMonth()));
		abschnitt.setGueltigkeit(fullMonth);
		abschnitt.setHasGemeindeSpezifischeBerechnung(true);
		abschnitt.setBetreuungsangebotTypForAsivAndGemeinde(BetreuungsangebotTyp.KITA);
		abschnitt.setBabyTarifForAsivAndGemeinde(true);
		abschnitt.setSozialhilfeempfaengerForAsivAndGemeinde(false);
		abschnitt.setBetreuungspensumProzentForAsivAndGemeinde(MathUtil.DEFAULT.from(100));
		abschnitt.setAnspruchspensumProzentForAsivAndGemeinde(100);
		abschnitt.setMonatlicheBetreuungskostenForAsivAndGemeinde(MathUtil.DEFAULT.from(2000));
		abschnitt.getBgCalculationInputAsiv().setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.ZERO);
		abschnitt.getBgCalculationInputGemeinde().setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.ZERO);
		abschnitt.getBgCalculationInputAsiv().setAbzugFamGroesse(BigDecimal.ZERO);
		abschnitt.getBgCalculationInputGemeinde().setAbzugFamGroesse(BigDecimal.ZERO);
		return abschnitt;
	}

	@Nonnull
	private AbstractRechner prepareRechner() {
		List<RechnerRule> rechnerRules = new ArrayList<>();
		rechnerRules.add(new ZusaetzlicherBabyGutscheinRechnerRule(GERMAN));
		final AbstractRechner rechner = BGRechnerFactory.getRechner(BetreuungsangebotTyp.KITA, rechnerRules);
		Assert.assertNotNull(rechner);
		return rechner;
	}

	@Nonnull
	private BGRechnerParameterDTO prepareBgRechnerParameterDTO() {
		return new BGRechnerParameterDTO(prepareEinstellungenBabyGutschein(), gesuchsperiodeOfEvaluator, new Gemeinde());
	}

	@Nonnull
	private Map<EinstellungKey, Einstellung> prepareEinstellungenBabyGutschein() {
		Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(EbeguRuleTestsHelper.getAllEinstellungen(gesuchsperiodeOfEvaluator));
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED).setValue("true");
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA).setValue(String.valueOf(50));
		einstellungenGemeinde.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO).setValue(String.valueOf(4.54));
		return einstellungenGemeinde;
	}
}

