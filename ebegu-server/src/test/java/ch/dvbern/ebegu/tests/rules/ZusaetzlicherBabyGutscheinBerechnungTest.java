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

	@Before
	public void setUp() {
		this.rechnerParameterDTO = prepareBgRechnerParameterDTO();
		this.rechner = prepareRechner();
	}

	@Test
	public void gemeindeBabyGutschein() {
		assertBabygutschein(100, MATH.from(0), MATH.from(0));
		assertBabygutschein(100, MATH.from(43000), MATH.from(0));
		assertBabygutschein(100, MATH.from(60000), MATH.from(145.30));
		assertBabygutschein(100, MATH.from(80000), MATH.from(316.20));
		assertBabygutschein(100, MATH.from(120000), MATH.from(658.10));
		// TODO Grenzfall: Bei 160'000 (und mehr) Einkommen ist ASIV bereits 0, der Babygutschein waere aber maximal!
		assertBabygutschein(100, MATH.from(160000), MATH.from(1000));
		// TODO: Mehr als 160'000: ist automatisch so, dass es nciht mehr als 50/tag gibt (im gegensatz zum excel)
		assertBabygutschein(100, MATH.from(200000), MATH.from(1000));
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
		final VerfuegungZeitabschnitt abschnitt = new VerfuegungZeitabschnitt();
		LocalDate stichtag = LocalDate.now();
		DateRange fullMonth = new DateRange(stichtag.with(firstDayOfMonth()), stichtag.with(lastDayOfMonth()));
		abschnitt.setGueltigkeit(fullMonth);
		abschnitt.setHasGemeindeSpezifischeBerechnung(true);
		abschnitt.setBetreuungsangebotTypForAsivAndGemeinde(BetreuungsangebotTyp.KITA);
		abschnitt.setBabyTarifForAsivAndGemeinde(true);
		abschnitt.setSozialhilfeempfaengerForAsivAndGemeinde(false);
		abschnitt.setBetreuungspensumProzentForAsivAndGemeinde(MathUtil.DEFAULT.from(bgPensum));
		abschnitt.setAnspruchspensumProzentForAsivAndGemeinde(bgPensum);
		abschnitt.setMonatlicheBetreuungskostenForAsivAndGemeinde(MathUtil.DEFAULT.from(30000));
		abschnitt.getBgCalculationInputAsiv().setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
		abschnitt.getBgCalculationInputGemeinde().setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
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

