/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.tests;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBerechnungCSVConverter;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBerechnungDataRow;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

public class LastenausgleichBerechnungCSVConverterTest {

	private MathUtil MATH = MathUtil.DEFAULT;

	@Nonnull
	private final LastenausgleichBerechnungCSVConverter lastenausgleichCSVConverter =
		new LastenausgleichBerechnungCSVConverter();

	@Test
	public void testAusExcelBeispiel() {
		MathUtil ROUND = MathUtil.ZWEI_NACHKOMMASTELLE;
		BigDecimal kosten100ProzentPlatz = MathUtil.EXACT.from(15176.4705882353);

		LastenausgleichBerechnungDataRow dataRow1 = new LastenausgleichBerechnungDataRow();
		dataRow1.setGemeinde("Bern");
		dataRow1.setBfsNummer("351");
		dataRow1.setVerrechnungsjahr("2021");
		BigDecimal belegung = new BigDecimal(1000);
		dataRow1.setTotalBelegungMitSelbstbehalt(belegung);
		dataRow1.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow1.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(16376470.59)));
		dataRow1.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(15176)));
		dataRow1.setSelbstbehaltGemeinde(ROUND.from(MATH.from(3035294.12)));
		dataRow1.setEingabeLastenausgleich(ROUND.from(MATH.from(13341176.47)));
		dataRow1.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow1.setKorrektur(false);

		LastenausgleichBerechnungDataRow dataRow2 = new LastenausgleichBerechnungDataRow();
		dataRow2.setGemeinde("Münchenbuchsee");
		dataRow2.setBfsNummer("546");
		dataRow2.setVerrechnungsjahr("2021");
		belegung = new BigDecimal(100);
		dataRow2.setTotalBelegungMitSelbstbehalt(belegung);
		dataRow2.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow2.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(1417647.06)));
		dataRow2.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(15176)));
		dataRow2.setSelbstbehaltGemeinde(ROUND.from(MATH.from(303529.41)));
		dataRow2.setEingabeLastenausgleich(ROUND.from(MATH.from(1114117.65)));
		dataRow2.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow2.setKorrektur(false);

		LastenausgleichBerechnungDataRow dataRow3 = new LastenausgleichBerechnungDataRow();
		dataRow3.setGemeinde("Münchenbuchsee");
		dataRow3.setBfsNummer("546");
		dataRow3.setVerrechnungsjahr("2019");
		belegung = new BigDecimal(-50);
		dataRow3.setTotalBelegungMitSelbstbehalt(belegung);
		dataRow3.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow3.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(-1017647.06)));
		dataRow3.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(-14105)));
		dataRow3.setSelbstbehaltGemeinde(ROUND.from(MATH.from(141050)));
		dataRow3.setEingabeLastenausgleich(ROUND.from(MATH.from(-1158697.06)));
		dataRow3.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow3.setKorrektur(true);

		LastenausgleichBerechnungDataRow dataRow4 = new LastenausgleichBerechnungDataRow();
		dataRow4.setGemeinde("Münchenbuchsee");
		dataRow4.setBfsNummer("546");
		dataRow4.setVerrechnungsjahr("2020");
		belegung = new BigDecimal(50);
		dataRow4.setTotalBelegungMitSelbstbehalt(belegung);
		dataRow4.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow4.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(400000)));
		dataRow4.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(14105)));
		dataRow4.setSelbstbehaltGemeinde(ROUND.from(MATH.from(141050)));
		dataRow4.setEingabeLastenausgleich(ROUND.from(MATH.from(258950)));
		dataRow4.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow4.setKorrektur(true);

		List<LastenausgleichBerechnungDataRow> reportData = new ArrayList<>();
		reportData.add(dataRow1);
		reportData.add(dataRow2);
		reportData.add(dataRow3);
		reportData.add(dataRow4);

		String lastenausgleichCSV = lastenausgleichCSVConverter.createLastenausgleichCSV(reportData);

		String csvExpected = "BFS-Nr.;kibon_Belegung;kibon_Gutscheine;kibon_Erhebung;kibon_Selbstbehalt\n"
			+ "351;1000;16376470.59;13341176.47;3035294.12\n"
			+ "546;100;1417647.06;214370.59;303529.41\n";

		Assert.assertEquals(csvExpected, lastenausgleichCSV);
	}

	@Test
	/*
	 * Für Gemeinden ohne Erhebungen aber mit Revisionen muss trotzdem einen Eintrag im CSV erstellt werden
	 */
	public void testGemeindenOhneErhebungen() {

		MathUtil ROUND = MathUtil.ZWEI_NACHKOMMASTELLE;
		BigDecimal kosten100ProzentPlatz = MathUtil.EXACT.from(3000);

		LastenausgleichBerechnungDataRow dataRow1 = new LastenausgleichBerechnungDataRow();
		dataRow1.setGemeinde("Bern");
		dataRow1.setBfsNummer("351");
		dataRow1.setVerrechnungsjahr("2020");
		BigDecimal belegung = new BigDecimal(0.3333);
		dataRow1.setTotalBelegungMitSelbstbehalt(ROUND.from(belegung.multiply(new BigDecimal(100))));
		dataRow1.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow1.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(7256.5)));
		dataRow1.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(15000)));
		dataRow1.setSelbstbehaltGemeinde(ROUND.from(MATH.from(1000)));
		dataRow1.setEingabeLastenausgleich(ROUND.from(MATH.from(6256.5)));
		dataRow1.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow1.setKorrektur(false);

		LastenausgleichBerechnungDataRow dataRow2 = new LastenausgleichBerechnungDataRow();
		dataRow2.setGemeinde("Münchenbuchsee");
		dataRow2.setBfsNummer("546");
		dataRow2.setVerrechnungsjahr("2020");
		dataRow2.setTotalBelegungMitSelbstbehalt(BigDecimal.ZERO);
		dataRow2.setTotalAnrechenbar(BigDecimal.ZERO);
		dataRow2.setTotalGutscheineMitSelbstbehalt(BigDecimal.ZERO);
		dataRow2.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(15000)));
		dataRow2.setSelbstbehaltGemeinde(BigDecimal.ZERO);
		dataRow2.setEingabeLastenausgleich(BigDecimal.ZERO);
		dataRow2.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow2.setKorrektur(false);

		LastenausgleichBerechnungDataRow dataRow3 = new LastenausgleichBerechnungDataRow();
		dataRow3.setGemeinde("Münchenbuchsee");
		dataRow3.setBfsNummer("546");
		dataRow3.setVerrechnungsjahr("2019");
		belegung = new BigDecimal(-0.3333);
		dataRow3.setTotalBelegungMitSelbstbehalt(ROUND.from(belegung.multiply(new BigDecimal(100))));
		dataRow3.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow3.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(-7256.5)));
		dataRow3.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(-15000)));
		dataRow3.setSelbstbehaltGemeinde(ROUND.from(MATH.from(-1000)));
		dataRow3.setEingabeLastenausgleich(ROUND.from(MATH.from(-6256.5)));
		dataRow3.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow3.setKorrektur(true);

		LastenausgleichBerechnungDataRow dataRow4 = new LastenausgleichBerechnungDataRow();
		dataRow4.setGemeinde("Münchenbuchsee");
		dataRow4.setBfsNummer("546");
		dataRow4.setVerrechnungsjahr("2019");
		dataRow4.setTotalBelegungMitSelbstbehalt(BigDecimal.ZERO);
		dataRow4.setTotalAnrechenbar(BigDecimal.ZERO);
		dataRow4.setTotalGutscheineMitSelbstbehalt(BigDecimal.ZERO);
		dataRow4.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(15000)));
		dataRow4.setSelbstbehaltGemeinde(BigDecimal.ZERO);
		dataRow4.setEingabeLastenausgleich(BigDecimal.ZERO);
		dataRow4.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow4.setKorrektur(true);

		List<LastenausgleichBerechnungDataRow> reportData = new ArrayList<>();
		reportData.add(dataRow1);
		reportData.add(dataRow2);
		reportData.add(dataRow3);
		reportData.add(dataRow4);

		String lastenausgleichCSV = lastenausgleichCSVConverter.createLastenausgleichCSV(reportData);

		String csvExpected = "BFS-Nr.;kibon_Belegung;kibon_Gutscheine;kibon_Erhebung;kibon_Selbstbehalt\n"
			+ "351;33.33;7256.50;6256.50;1000.00\n"
			+ "546;0;0.00;-6256.50;0\n";

		Assert.assertEquals(csvExpected, lastenausgleichCSV);
	}

	@Test
	public void testGemeindeMitKindOhneSelbstbehalt() {

		MathUtil ROUND = MathUtil.ZWEI_NACHKOMMASTELLE;
		BigDecimal kosten100ProzentPlatz = MathUtil.EXACT.from(3000);

		LastenausgleichBerechnungDataRow dataRow1 = new LastenausgleichBerechnungDataRow();
		dataRow1.setGemeinde("Bern");
		dataRow1.setBfsNummer("351");
		dataRow1.setVerrechnungsjahr("2021");
		BigDecimal belegung = new BigDecimal(1000);
		dataRow1.setTotalBelegungMitSelbstbehalt(belegung);
		dataRow1.setTotalBelegungOhneSelbstbehalt(belegung);
		dataRow1.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow1.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(16376470.59)));
		dataRow1.setTotalGutscheineOhneSelbstbehalt(ROUND.from(MATH.from(6500.41)));
		dataRow1.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(15176)));
		dataRow1.setSelbstbehaltGemeinde(ROUND.from(MATH.from(3035294.12)));
		dataRow1.setEingabeLastenausgleich(ROUND.from(MATH.from(13341176.47)));
		dataRow1.setKorrektur(false);

		LastenausgleichBerechnungDataRow dataRow2 = new LastenausgleichBerechnungDataRow();
		dataRow2.setGemeinde("Münchenbuchsee");
		dataRow2.setBfsNummer("546");
		dataRow2.setVerrechnungsjahr("2021");
		belegung = new BigDecimal(100);
		dataRow2.setTotalBelegungMitSelbstbehalt(belegung);
		dataRow2.setTotalBelegungOhneSelbstbehalt(belegung);
		dataRow2.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow2.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(1417647.06)));
		dataRow2.setTotalGutscheineOhneSelbstbehalt(ROUND.from(MATH.from(6500.44)));
		dataRow2.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(15176)));
		dataRow2.setSelbstbehaltGemeinde(ROUND.from(MATH.from(303529.41)));
		dataRow2.setEingabeLastenausgleich(ROUND.from(MATH.from(1114117.65)));
		dataRow2.setKorrektur(false);

		LastenausgleichBerechnungDataRow dataRow3 = new LastenausgleichBerechnungDataRow();
		dataRow3.setGemeinde("Münchenbuchsee");
		dataRow3.setBfsNummer("546");
		dataRow3.setVerrechnungsjahr("2019");
		belegung = new BigDecimal(-50);
		dataRow3.setTotalBelegungMitSelbstbehalt(belegung);
		dataRow3.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow3.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(-1017647.06)));
		dataRow3.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(-14105)));
		dataRow3.setSelbstbehaltGemeinde(ROUND.from(MATH.from(141050)));
		dataRow3.setEingabeLastenausgleich(ROUND.from(MATH.from(-1158697.06)));
		dataRow3.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow3.setKorrektur(true);

		LastenausgleichBerechnungDataRow dataRow4 = new LastenausgleichBerechnungDataRow();
		dataRow4.setGemeinde("Münchenbuchsee");
		dataRow4.setBfsNummer("546");
		dataRow4.setVerrechnungsjahr("2020");
		belegung = new BigDecimal(50);
		dataRow4.setTotalBelegungMitSelbstbehalt(belegung);
		dataRow4.setTotalAnrechenbar(ROUND.from(kosten100ProzentPlatz.multiply(belegung)));
		dataRow4.setTotalGutscheineMitSelbstbehalt(ROUND.from(MATH.from(400000)));
		dataRow4.setKostenPro100ProzentPlatz(ROUND.from(MATH.from(14105)));
		dataRow4.setSelbstbehaltGemeinde(ROUND.from(MATH.from(141050)));
		dataRow4.setEingabeLastenausgleich(ROUND.from(MATH.from(258950)));
		dataRow4.setTotalGutscheineOhneSelbstbehalt(BigDecimal.ZERO);
		dataRow4.setKorrektur(true);

		List<LastenausgleichBerechnungDataRow> reportData = new ArrayList<>();
		reportData.add(dataRow1);
		reportData.add(dataRow2);
		reportData.add(dataRow3);
		reportData.add(dataRow4);

		String lastenausgleichCSV = lastenausgleichCSVConverter.createLastenausgleichCSV(reportData);

		String csvExpected = "BFS-Nr.;kibon_Belegung;kibon_Gutscheine;kibon_Erhebung;kibon_Selbstbehalt\n"
			+ "351;2000.00;16382971.00;13347676.88;3035294.12\n"
			+ "546;200.00;1424147.50;220871.03;303529.41\n";

		Assert.assertEquals(csvExpected, lastenausgleichCSV);
	}
}
