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

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.TransactionHelper;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.tests.AbstractEbeguLoginTest;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class LastenausgleichServiceBeanTest extends AbstractEbeguLoginTest {

	// Testdaten
	// Wir testen mit Dagmar Waelti. 1 mal Dagmar Waelti gibt folgende Werte
	private final BigDecimal waeltiTotalGutscheinHalbjahr1 = MathUtil.DEFAULT.fromNullSafe(7256.50);
	private final BigDecimal waeltiTotalBelegungHalbjahr1 = MathUtil.DEFAULT.fromNullSafe(33.33);
	private final BigDecimal waeltiSelbstbehaltPro100Prozent = MathUtil.DEFAULT.fromNullSafe(4353.90);

	private Gesuchsperiode gp1718;
	private Gesuchsperiode gp1819;

	private Gesuchsperiode gp2021;


	@Inject
	private LastenausgleichService lastenausgleichServiceBean;

	@Inject
	private Persistence persistence;

	@Inject
	private TestfaelleService testfaelleService;

	private String gemeinde;

	private Mandant mandant;

	@Inject
	private TransactionHelper transactionHelper;

	@Before
	public void init() {
		gp1718 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2017, 2018);
		gp1819 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2018, 2019);

		gp2021 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2020, 2021);

		TestDataUtil.prepareParameters(gp1718, persistence);
		TestDataUtil.prepareParameters(gp1819, persistence);
		TestDataUtil.prepareParameters(gp2021, persistence);

		insertInstitutionen();
		gemeinde = TestDataUtil.getGemeindeParis(persistence).getId();
		mandant = TestDataUtil.getMandantKantonBernAndPersist(persistence);
	}

	@Test
	public void createLastenausgleichNoData() {
		// Es sind noch keine Grundlagen gespeichert
		Optional<LastenausgleichGrundlagen> grundlagen =
			lastenausgleichServiceBean.findLastenausgleichGrundlagen(2018);
		Assert.assertFalse("Noch keine Grundlagen vorhanden", grundlagen.isPresent());
		// Einen (leeren) Lastenausgleich erstellen
		Lastenausgleich lastenausgleich =
			lastenausgleichServiceBean.createLastenausgleichOld(2018, MathUtil.DEFAULT.from(4025d),
				mandant);
		Assert.assertNotNull(lastenausgleich);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest = persistence.find(Lastenausgleich.class, lastenausgleich.getId());
			Assert.assertEquals(2018, latsToTest.getJahr().longValue());
			Assert.assertEquals(0, latsToTest.getLastenausgleichDetails().size());
			// Jetzt sind die Grundlagen gespeichert
			Optional<LastenausgleichGrundlagen> grundlagen2019 =
				lastenausgleichServiceBean.findLastenausgleichGrundlagen(2018);
			Assert.assertTrue("Die Grundlagen sollten jetzt gespeichert sein", grundlagen2019.isPresent());
		});
	}

	@Test (expected = EbeguRuntimeException.class)
	public void grundlagenKoennenNichtAktualisiertWerden() {
		// Es darf nicht moeglich sein, fuer dasselbe Jahr einen zweiten Lastenausgleich zu erstellen
		lastenausgleichServiceBean.createLastenausgleichOld(2018, MathUtil.DEFAULT.from(4025d), mandant);
		lastenausgleichServiceBean.createLastenausgleichOld(2018, MathUtil.DEFAULT.from(3012d), mandant);
	}

	@Test
	public void createLastenausgleichAusgeglichen() {
		// Die durchschnittlichen Kosten pro 100% Platz entsprechen genau den Kosten in der Gemeinde
		Gesuch gesuch = createGesuch(gp1718);

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich =
			lastenausgleichServiceBean.createLastenausgleichOld(2017, waeltiSelbstbehaltPro100Prozent,
				mandant);
		Assert.assertNotNull(lastenausgleich);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest = persistence.find(Lastenausgleich.class, lastenausgleich.getId());

			Assert.assertEquals(2017, latsToTest.getJahr().longValue());
			Assert.assertEquals(1, latsToTest.getLastenausgleichDetails().size());
			Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), latsToTest.getTotalAlleGemeinden());

			LastenausgleichDetail detail = latsToTest.getLastenausgleichDetails().iterator().next();
			Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
			Assert.assertEquals(2017, detail.getJahr().longValue());
			Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(
				MathUtil.DEFAULT.from(1451.30),
				detail.getSelbstbehaltGemeinde()); // dies entspricht genau 20% der Kosten
			Assert.assertEquals(
				MathUtil.DEFAULT.from(5805.20),
				detail.getBetragLastenausgleich()); // dies entspricht genau 80% der Kosten
		});
	}

	@Test
	public void createLastenausgleichAusgeglichen2018() {
		// Die durchschnittlichen Kosten pro 100% Platz entsprechen genau den Kosten in der Gemeinde
		Gesuch gesuch = createGesuch(gp1718);

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich =
			lastenausgleichServiceBean.createLastenausgleichOld(2018, waeltiSelbstbehaltPro100Prozent,
				mandant);
		Assert.assertNotNull(lastenausgleich);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest = persistence.find(Lastenausgleich.class, lastenausgleich.getId());
			Assert.assertEquals(2018, latsToTest.getJahr().longValue());
			Assert.assertEquals(1, latsToTest.getLastenausgleichDetails().size());
			Assert.assertEquals(MathUtil.DEFAULT.from(4644.16), latsToTest.getTotalAlleGemeinden());

			LastenausgleichDetail detail = latsToTest.getLastenausgleichDetails().iterator().next();
			Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
			Assert.assertEquals(2018, detail.getJahr().longValue());
			Assert.assertEquals(MathUtil.DEFAULT.from(26.67), detail.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), detail.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(
				MathUtil.DEFAULT.from(1161.04),
				detail.getSelbstbehaltGemeinde()); // dies entspricht genau 20% der Kosten
			Assert.assertEquals(
				MathUtil.DEFAULT.from(4644.16),
				detail.getBetragLastenausgleich()); // dies entspricht genau 80% der Kosten
		});
	}

	@Test
	public void createLastenausgleichReicheGemeinde() {
		Gesuch gesuch = createGesuch(gp1718);
		//  Die durchschnittlichen Kostne pro 100% Platz sind doppelt so hoch wie in der Gemeinde
		BigDecimal hoheKostenPro100ProzentPlatz =
			MathUtil.DEFAULT.multiply(waeltiSelbstbehaltPro100Prozent, MathUtil.DEFAULT.from(2));

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich =
			lastenausgleichServiceBean.createLastenausgleichOld(2017, hoheKostenPro100ProzentPlatz,
				mandant);
		Assert.assertNotNull(lastenausgleich);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest = persistence.find(Lastenausgleich.class, lastenausgleich.getId());
			Assert.assertEquals(2017, latsToTest.getJahr().longValue());
			Assert.assertEquals(1, latsToTest.getLastenausgleichDetails().size());
			Assert.assertEquals(MathUtil.DEFAULT.from(4353.90), latsToTest.getTotalAlleGemeinden());

			LastenausgleichDetail detail = latsToTest.getLastenausgleichDetails().iterator().next();
			Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
			Assert.assertEquals(2017, detail.getJahr().longValue());
			Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(2902.60), detail.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(4353.90), detail.getBetragLastenausgleich());
		});
	}

	@Test
	public void createLastenausgleichArmeGemeinde() {
		Gesuch gesuch = createGesuch(gp1718);
		//  Die durchschnittlichen Kostne pro 100% Platz sind halb so hoch wie in der Gemeinde
		BigDecimal tiefeKostenPro100ProzentPlatz =
			MathUtil.DEFAULT.divide(waeltiSelbstbehaltPro100Prozent, MathUtil.DEFAULT.from(2));

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich =
			lastenausgleichServiceBean.createLastenausgleichOld(2017, tiefeKostenPro100ProzentPlatz,
				mandant);
		Assert.assertNotNull(lastenausgleich);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest2017 = persistence.find(Lastenausgleich.class, lastenausgleich.getId());
			Assert.assertEquals(2017, latsToTest2017.getJahr().longValue());

			Assert.assertEquals(1, latsToTest2017.getLastenausgleichDetails().size());
			Assert.assertEquals(MathUtil.DEFAULT.from(6530.85), latsToTest2017.getTotalAlleGemeinden());

			LastenausgleichDetail detail = latsToTest2017.getLastenausgleichDetails().iterator().next();
			Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
			Assert.assertEquals(2017, detail.getJahr().longValue());
			Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(725.65), detail.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(6530.85), detail.getBetragLastenausgleich());
		});
	}

	@Test
	public void createLastenausgleichKorrekturen() {
		Gesuch gesuch = createGesuch(gp1718);

		// Lastenausgleich 2017 erstellen
		Lastenausgleich lastenausgleich2017 =
			lastenausgleichServiceBean.createLastenausgleichOld(2017, waeltiSelbstbehaltPro100Prozent,
				mandant);
		Assert.assertNotNull(lastenausgleich2017);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest2018 = persistence.find(Lastenausgleich.class, lastenausgleich2017.getId());
			Assert.assertEquals(2017, latsToTest2018.getJahr().longValue());
			Assert.assertEquals(1, latsToTest2018.getLastenausgleichDetails().size());
			Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), latsToTest2018.getTotalAlleGemeinden());

			LastenausgleichDetail detail = latsToTest2018.getLastenausgleichDetails().iterator().next();
			Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
			Assert.assertEquals(2017, detail.getJahr().longValue());
			Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(
				MathUtil.DEFAULT.from(1451.30),
				detail.getSelbstbehaltGemeinde()); // dies entspricht genau 20% der Kosten
			Assert.assertEquals(
				MathUtil.DEFAULT.from(5805.20),
				detail.getBetragLastenausgleich()); // dies entspricht genau 80% der Kosten
		});

		// Nachtraeglich ein weiteres Gesuch verfuegen
		createGesuch(gp1718);

		// Lastenausgleich 2018 erstellen: Dies soll auch zu Korrekturen fuer 2017 fuehren
		Lastenausgleich lastenausgleich2018 =
			lastenausgleichServiceBean.createLastenausgleichOld(2018, waeltiSelbstbehaltPro100Prozent,
				mandant);
		Assert.assertNotNull(lastenausgleich2018);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest2018 = persistence.find(Lastenausgleich.class, lastenausgleich2018.getId());
			Assert.assertEquals(2018, latsToTest2018.getJahr().longValue());
			Assert.assertEquals(3, latsToTest2018.getLastenausgleichDetails().size());
			Assert.assertEquals(MathUtil.DEFAULT.from(15093.52), latsToTest2018.getTotalAlleGemeinden());

			LastenausgleichDetail detail2018 = latsToTest2018.getLastenausgleichDetails().get(0);
			Assert.assertEquals(gesuch.extractGemeinde(), detail2018.getGemeinde());
			Assert.assertEquals(2018, detail2018.getJahr().longValue());
			Assert.assertEquals(MathUtil.DEFAULT.from(53.33), detail2018.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(11610.40), detail2018.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(2322.08), detail2018.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(9288.32), detail2018.getBetragLastenausgleich());
			Assert.assertFalse(detail2018.isKorrektur());

			LastenausgleichDetail detail2017_korrektur = latsToTest2018.getLastenausgleichDetails().get(1);
			Assert.assertEquals(gesuch.extractGemeinde(), detail2017_korrektur.getGemeinde());
			Assert.assertEquals(2017, detail2017_korrektur.getJahr().longValue());
			Assert.assertEquals(
				MathUtil.DEFAULT.from(-33.33),
				detail2017_korrektur.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(
				MathUtil.DEFAULT.from(-7256.50),
				detail2017_korrektur.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(-1451.30), detail2017_korrektur.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(-5805.20), detail2017_korrektur.getBetragLastenausgleich());
			Assert.assertTrue(detail2017_korrektur.isKorrektur());

			LastenausgleichDetail detail2017_new = latsToTest2018.getLastenausgleichDetails().get(2);
			Assert.assertEquals(gesuch.extractGemeinde(), detail2017_new.getGemeinde());
			Assert.assertEquals(2017, detail2017_new.getJahr().longValue());
			Assert.assertEquals(MathUtil.DEFAULT.from(66.67), detail2017_new.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(
				MathUtil.DEFAULT.from(14513.00),
				detail2017_new.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(2902.60), detail2017_new.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(11610.40), detail2017_new.getBetragLastenausgleich());
			Assert.assertTrue(detail2017_new.isKorrektur());
		});
	}

	@Test
	public void createLastenausgleichKorrekturenMehrereJahre() {

		// Lastenausgleich 2017: 1 aus Erhebung
		createGesuch(gp1718);
		Lastenausgleich lastenausgleich2017 =
			lastenausgleichServiceBean.createLastenausgleichOld(2017, waeltiSelbstbehaltPro100Prozent,
				mandant);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest2017 = persistence.find(Lastenausgleich.class, lastenausgleich2017.getId());
			Assert.assertEquals(1, latsToTest2017.getLastenausgleichDetails().size());
		});

		// Lastenausgleich 2018: 1 aus Erhebung, 2 aus Korrektur 2017
		createGesuch(gp1718);
		Lastenausgleich lastenausgleich2018 = lastenausgleichServiceBean.createLastenausgleichOld(
			2018,
			waeltiSelbstbehaltPro100Prozent,
			mandant);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest2018 = persistence.find(Lastenausgleich.class, lastenausgleich2018.getId());
			Assert.assertEquals(3, latsToTest2018.getLastenausgleichDetails().size());
		});

		// Lastenausgleich 2019: 2 aus Korrektur 2017, 2 aus Korrektur 2018. Im 2019 kein Gesuch!
		createGesuch(gp1718);
		Lastenausgleich lastenausgleich2019 =
			lastenausgleichServiceBean.createLastenausgleichOld(2019, waeltiSelbstbehaltPro100Prozent,
				mandant);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest2019 = persistence.find(Lastenausgleich.class, lastenausgleich2019.getId());

			Assert.assertEquals(4, latsToTest2019.getLastenausgleichDetails().size());
		});

	}

	@Test
	public void createLastenausgleichNeueBerechnung() {
		Gesuchsperiode gp2122 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2021, 2022);
		TestDataUtil.prepareParameters(gp2122, persistence);
		Gesuch gesuch = createGesuch(gp2122);

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleichNew(2022, mandant);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest = persistence.find(Lastenausgleich.class, lastenausgleich.getId());

			Assert.assertNotNull(latsToTest);
			Assert.assertEquals(2022, latsToTest.getJahr().longValue());
			Assert.assertEquals(1, latsToTest.getLastenausgleichDetails().size());
			Assert.assertEquals(MathUtil.DEFAULT.from(4644.16), latsToTest.getTotalAlleGemeinden());

			LastenausgleichGrundlagen grundlagen =
				lastenausgleichServiceBean.findLastenausgleichGrundlagen(2022).get();
			Assert.assertNull(grundlagen.getSelbstbehaltPro100ProzentPlatz());
			Assert.assertNull(grundlagen.getKostenPro100ProzentPlatz());

			LastenausgleichDetail detail = latsToTest.getLastenausgleichDetails().iterator().next();
			Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
			Assert.assertEquals(2022, detail.getJahr().longValue());
			Assert.assertEquals(MathUtil.DEFAULT.from(26.67), detail.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), detail.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(1161.04), detail.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(4644.16), detail.getBetragLastenausgleich());
		});
	}

	@Test
	public void createLastenausgleichKorrekturenDifferentCalculations() {
		Gesuchsperiode gp2223 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2022, 2023);
		TestDataUtil.prepareParameters(gp2223, persistence);
		// Lastenausgleich 2021: 1 aus Erhebung
		createGesuch(gp2021);
		Lastenausgleich lastenausgleich21 =
			lastenausgleichServiceBean.createLastenausgleichOld(2021, waeltiSelbstbehaltPro100Prozent,
				mandant);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest21 = persistence.find(Lastenausgleich.class, lastenausgleich21.getId());
			Assert.assertEquals(1, latsToTest21.getLastenausgleichDetails().size());
			var detailOld = latsToTest21.getLastenausgleichDetails().get(0);
			Assert.assertEquals(MathUtil.DEFAULT.from(26.67), detailOld.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), detailOld.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(1161.04), detailOld.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(4644.16), detailOld.getBetragLastenausgleich());
		});

		// Lastenausgleich 2022: 1 aus Erhebung, 1 aus Korrektur 2021
		createGesuch(gp2021);
		createGesuch(gp2223);
		Lastenausgleich lastenausgleich22 = lastenausgleichServiceBean.createLastenausgleichNew(
			2022,
			mandant);
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich latsToTest21 = persistence.find(Lastenausgleich.class, lastenausgleich21.getId());
			Assert.assertEquals(1, latsToTest21.getLastenausgleichDetails().size());
			var detailOld = latsToTest21.getLastenausgleichDetails().get(0);

			Lastenausgleich latsToTest22 = persistence.find(Lastenausgleich.class, lastenausgleich22.getId());

			Assert.assertEquals(3, latsToTest22.getLastenausgleichDetails().size());

			// lastenausgleich Details 0 müsste nach neuer Berechnung berechnet sein
			var detail0 = latsToTest22.getLastenausgleichDetails().get(0);
			Assert.assertEquals(MathUtil.DEFAULT.from(33.33), detail0.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(7256.5), detail0.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(1451.3), detail0.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(5805.2), detail0.getBetragLastenausgleich());

			// lastenausgleich Details 2 müsste nach alter Berechnung berechnet sein und enthalten 2x das Betrag von
			// der LATS 2021
			var detail2 = latsToTest22.getLastenausgleichDetails().get(2);
			Assert.assertEquals(MathUtil.DEFAULT.from(53.33), detail2.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(11610.40), detail2.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(MathUtil.DEFAULT.from(2322.08), detail2.getSelbstbehaltGemeinde());
			Assert.assertEquals(MathUtil.DEFAULT.from(9288.32), detail2.getBetragLastenausgleich());

			// und die korrektur ist alt die negative Wert von der alte Berechnung negationiert
			var detail1 = latsToTest22.getLastenausgleichDetails().get(1);
			Assert.assertEquals(
				detailOld.getTotalBelegungenMitSelbstbehalt().negate(),
				detail1.getTotalBelegungenMitSelbstbehalt());
			Assert.assertEquals(
				detailOld.getTotalBetragGutscheineMitSelbstbehalt().negate(),
				detail1.getTotalBetragGutscheineMitSelbstbehalt());
			Assert.assertEquals(detailOld.getSelbstbehaltGemeinde().negate(), detail1.getSelbstbehaltGemeinde());
			Assert.assertEquals(detailOld.getBetragLastenausgleich().negate(), detail1.getBetragLastenausgleich());
		});
	}

	private Gesuch createGesuch(Gesuchsperiode gesuchsperiode) {
		return testfaelleService.createAndSaveTestfaelle(TestfaelleService.WAELTI_DAGMAR, true, true, gemeinde, gesuchsperiode, mandant);
	}

}
