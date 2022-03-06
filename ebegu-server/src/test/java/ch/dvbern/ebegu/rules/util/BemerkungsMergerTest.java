/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules.util;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Class for Bemerkungsmerger
 */
public class BemerkungsMergerTest {

	private static final DateRange JAN = new DateRange(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 1).with(TemporalAdjusters.lastDayOfMonth()));
	private static final DateRange FEB = new DateRange(LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 1).with(TemporalAdjusters.lastDayOfMonth()));
	private static final DateRange MAR = new DateRange(LocalDate.of(2016, 3, 1), LocalDate.of(2016, 3, 1).with(TemporalAdjusters.lastDayOfMonth()));
	private static final DateRange APR = new DateRange(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 1).with(TemporalAdjusters.lastDayOfMonth()));
	private static final DateRange MAI = new DateRange(LocalDate.of(2016, 5, 1), LocalDate.of(2016, 5, 1).with(TemporalAdjusters.lastDayOfMonth()));

	private static final Pattern NEW_LINE = Pattern.compile("\\n");

	@Test
	public void evaluateRangesByBemerkungKeyTest() {
		VerfuegungZeitabschnitt jan = new VerfuegungZeitabschnitt(JAN);
		VerfuegungZeitabschnitt feb = new VerfuegungZeitabschnitt(FEB);
		VerfuegungZeitabschnitt mar = new VerfuegungZeitabschnitt(MAR);
		VerfuegungZeitabschnitt apr = new VerfuegungZeitabschnitt(APR);
		VerfuegungZeitabschnitt mai = new VerfuegungZeitabschnitt(MAI);

		// Abwesenheit: Durchgehend Jan-Mai
		jan.getBgCalculationInputAsiv().addBemerkung(MsgKey.ABWESENHEIT_MSG, Constants.DEFAULT_LOCALE);
		feb.getBgCalculationInputAsiv().addBemerkung(MsgKey.ABWESENHEIT_MSG, Constants.DEFAULT_LOCALE);
		mar.getBgCalculationInputAsiv().addBemerkung(MsgKey.ABWESENHEIT_MSG, Constants.DEFAULT_LOCALE);
		apr.getBgCalculationInputAsiv().addBemerkung(MsgKey.ABWESENHEIT_MSG, Constants.DEFAULT_LOCALE);
		mai.getBgCalculationInputAsiv().addBemerkung(MsgKey.ABWESENHEIT_MSG, Constants.DEFAULT_LOCALE);

		// Betreuungsangebot: Jan-März, Mai
		jan.getBgCalculationInputAsiv().addBemerkung(MsgKey.BETREUUNGSANGEBOT_MSG, Constants.DEFAULT_LOCALE);
		feb.getBgCalculationInputAsiv().addBemerkung(MsgKey.BETREUUNGSANGEBOT_MSG, Constants.DEFAULT_LOCALE);
		mar.getBgCalculationInputAsiv().addBemerkung(MsgKey.BETREUUNGSANGEBOT_MSG, Constants.DEFAULT_LOCALE);
		mai.getBgCalculationInputAsiv().addBemerkung(MsgKey.EINREICHUNGSFRIST_MSG, Constants.DEFAULT_LOCALE);

		// Einreichungsfrist: Jan-Feb, Apr-Mai
		jan.getBgCalculationInputAsiv().addBemerkung(MsgKey.EINREICHUNGSFRIST_MSG, Constants.DEFAULT_LOCALE);
		feb.getBgCalculationInputAsiv().addBemerkung(MsgKey.EINREICHUNGSFRIST_MSG, Constants.DEFAULT_LOCALE);
		apr.getBgCalculationInputAsiv().addBemerkung(MsgKey.EINREICHUNGSFRIST_MSG, Constants.DEFAULT_LOCALE);
		mai.getBgCalculationInputAsiv().addBemerkung(MsgKey.BETREUUNGSANGEBOT_MSG, Constants.DEFAULT_LOCALE);

		List<VerfuegungZeitabschnitt> verfZeitabschn = new ArrayList<>();
		Collections.addAll(verfZeitabschn, jan, feb, mar, apr, mai);

		//test output
		String resultingBem = BemerkungsMerger.evaluateBemerkungenForVerfuegung(verfZeitabschn, TestDataUtil.getMandantKantonBern(), false);
		Assert.assertNotNull(resultingBem);
		String[] strings = NEW_LINE.split(resultingBem);
		Assert.assertEquals(5, strings.length);
		Assert.assertTrue(strings[0].startsWith("01.01.2016 - 29.02.2016: Für diesen Zeitraum wird noch kein Betreuungsgutschein ausgestellt"));
		Assert.assertTrue(strings[1].startsWith("01.01.2016 - 31.03.2016: Betreuungsangebot Schulamt"));
		Assert.assertTrue(strings[2].startsWith("01.01.2016 - 31.05.2016: Das Kind wird länger als "));
		Assert.assertTrue(strings[3].startsWith("01.04.2016 - 31.05.2016: Für diesen Zeitraum wird noch kein Betreuungsgutschein ausgestellt"));
		Assert.assertTrue(strings[4].startsWith("01.05.2016 - 31.05.2016: Betreuungsangebot Schulamt"));
	}

	@Test
	public void evaluateBemerkungenForVerfuegungOverlappInvalidTest() {
		VerfuegungZeitabschnitt jan = new VerfuegungZeitabschnitt(JAN);
		VerfuegungZeitabschnitt overlappWithJan = new VerfuegungZeitabschnitt(new DateRange(JAN.getGueltigBis(), FEB.getGueltigBis()));

		jan.getBgCalculationInputAsiv().addBemerkung(MsgKey.ABWESENHEIT_MSG, Constants.DEFAULT_LOCALE);
		overlappWithJan.getBgCalculationInputAsiv().addBemerkung(MsgKey.ABWESENHEIT_MSG, Constants.DEFAULT_LOCALE);

		List<VerfuegungZeitabschnitt> verfZeitabschn = new ArrayList<>();
		Collections.addAll(verfZeitabschn, jan, overlappWithJan);
		try {
			BemerkungsMerger.evaluateBemerkungenForVerfuegung(verfZeitabschn, TestDataUtil.getMandantKantonBern(), false);
			Assert.fail("Should throw exception because of overlap");
		} catch (IllegalArgumentException ignore) {
			//noop
		}
	}

	@Test
	public void bemerkungenVonUeberschriebenenRegelnNichtAnzeigen() {
		// Wenn alle drei Regeln: Es wird nur der AusserordentlicheAnspruch berücksichtigt
		VerfuegungZeitabschnitt jan = new VerfuegungZeitabschnitt(JAN);
		jan.getBgCalculationInputAsiv().addBemerkung(MsgKey.ERWERBSPENSUM_ANSPRUCH, Constants.DEFAULT_LOCALE);
		jan.getBgCalculationInputAsiv().addBemerkung(MsgKey.FACHSTELLE_MSG, Constants.DEFAULT_LOCALE);
		jan.getBgCalculationInputAsiv().addBemerkung(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG, Constants.DEFAULT_LOCALE);
		// Wenn Fachstelle und Erwerbspensum -> nur Fachstelle anzeigen
		VerfuegungZeitabschnitt feb = new VerfuegungZeitabschnitt(FEB);
		feb.getBgCalculationInputAsiv().addBemerkung(MsgKey.ERWERBSPENSUM_ANSPRUCH, Constants.DEFAULT_LOCALE);
		feb.getBgCalculationInputAsiv().addBemerkung(MsgKey.FACHSTELLE_MSG, Constants.DEFAULT_LOCALE);

		List<VerfuegungZeitabschnitt> verfZeitabschn = new ArrayList<>();
		Collections.addAll(verfZeitabschn, jan, feb);

		//test output
		String resultingBem = BemerkungsMerger.evaluateBemerkungenForVerfuegung(verfZeitabschn, TestDataUtil.getMandantKantonBern(), false);
		Assert.assertNotNull(resultingBem);
		String[] strings = NEW_LINE.split(resultingBem);
		Assert.assertEquals(2, strings.length);
		Assert.assertTrue(strings[0].startsWith("01.01.2016 - 31.01.2016: Für diesen Zeitraum ist das erforderliche Beschäftigungspensum für den Erhalt eines Betreuungsgutscheins nicht erreicht"));
		Assert.assertTrue(strings[1].startsWith("01.02.2016 - 29.02.2016: Für diesen Zeitraum ist der Bedarf für die familienergänzende Betreuung"));
	}

	@Test
	public void bemerkungenVonFKJVUeberschriebenenRegeln() {
		VerfuegungZeitabschnitt jan = new VerfuegungZeitabschnitt(JAN);
		jan.getBgCalculationInputAsiv().addBemerkung(MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG, Constants.DEFAULT_LOCALE);

		List<VerfuegungZeitabschnitt> verfZeitabschn = Collections.singletonList(jan);

		String resultingBem = BemerkungsMerger.evaluateBemerkungenForVerfuegung(verfZeitabschn, TestDataUtil.getMandantKantonBern(), true);
		Assert.assertNotNull(resultingBem);
		String[] strings = NEW_LINE.split(resultingBem);
		Assert.assertEquals(1, strings.length);
		Assert.assertTrue(strings[0].startsWith("01.01.2016 - 31.01.2016: Das anrechenbare Einkommen des Jahres"));
		Assert.assertTrue(strings[0].contains("(Art. 57 Abs. 2 FKJV)"));
	}
}
