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
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
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


	@Inject
	private LastenausgleichService lastenausgleichServiceBean;

	@Inject
	private Persistence persistence;

	@Inject
	private TestfaelleService testfaelleService;

	private String gemeinde;

	@Before
	public void init() {
		gp1718 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2017, 2018);
		gp1819 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2018, 2019);
		TestDataUtil.prepareParameters(gp1718, persistence);
		TestDataUtil.prepareParameters(gp1819, persistence);
		insertInstitutionen();
		gemeinde = TestDataUtil.getGemeindeParis(persistence).getId();
	}

	@Test
	public void createLastenausgleichNoData() {
		// Es sind noch keine Grundlagen gespeichert
		Optional<LastenausgleichGrundlagen> grundlagen = lastenausgleichServiceBean.findLastenausgleichGrundlagen(2018);
		Assert.assertFalse("Noch keine Grundlagen vorhanden", grundlagen.isPresent());
		// Einen (leeren) Lastenausgleich erstellen
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleich(2018, MathUtil.DEFAULT.from(4025d));
		Assert.assertNotNull(lastenausgleich);
		Assert.assertEquals(2018, lastenausgleich.getJahr().longValue());
		Assert.assertEquals(0, lastenausgleich.getLastenausgleichDetails().size());
		// Jetzt sind die Grundlagen gespeichert
		grundlagen = lastenausgleichServiceBean.findLastenausgleichGrundlagen(2018);
		Assert.assertTrue("Die Grundlagen sollten jetzt gespeichert sein", grundlagen.isPresent());
	}

	@Test (expected = EbeguRuntimeException.class)
	public void grundlagenKoennenNichtAktualisiertWerden() {
		// Es darf nicht moeglich sein, fuer dasselbe Jahr einen zweiten Lastenausgleich zu erstellen
		lastenausgleichServiceBean.createLastenausgleich(2018, MathUtil.DEFAULT.from(4025d));
		lastenausgleichServiceBean.createLastenausgleich(2018, MathUtil.DEFAULT.from(3012d));
	}

	@Test
	public void createLastenausgleichAusgeglichen() {
		// Die durchschnittlichen Kosten pro 100% Platz entsprechen genau den Kosten in der Gemeinde
		Gesuch gesuch = createGesuch(gp1718);

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleich(2017, waeltiSelbstbehaltPro100Prozent);
		Assert.assertNotNull(lastenausgleich);
		Assert.assertEquals(2017, lastenausgleich.getJahr().longValue());
		Assert.assertEquals(1, lastenausgleich.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), lastenausgleich.getTotalAlleGemeinden());

		LastenausgleichDetail detail = lastenausgleich.getLastenausgleichDetails().iterator().next();
		Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
		Assert.assertEquals(2017, detail.getJahr().longValue());
		Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungenMitSelbstbehalt());
		Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheineMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(1451.30), detail.getSelbstbehaltGemeinde()); // dies entspricht genau 20% der Kosten
		Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), detail.getBetragLastenausgleich()); // dies entspricht genau 80% der Kosten
	}

	@Test
	public void createLastenausgleichAusgeglichen2018() {
		// Die durchschnittlichen Kosten pro 100% Platz entsprechen genau den Kosten in der Gemeinde
		Gesuch gesuch = createGesuch(gp1718);

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleich(2018, waeltiSelbstbehaltPro100Prozent);
		Assert.assertNotNull(lastenausgleich);
		Assert.assertEquals(2018, lastenausgleich.getJahr().longValue());
		Assert.assertEquals(1, lastenausgleich.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(4644.16), lastenausgleich.getTotalAlleGemeinden());

		LastenausgleichDetail detail = lastenausgleich.getLastenausgleichDetails().iterator().next();
		Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
		Assert.assertEquals(2018, detail.getJahr().longValue());
		Assert.assertEquals(MathUtil.DEFAULT.from(26.67), detail.getTotalBelegungenMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), detail.getTotalBetragGutscheineMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(1161.04), detail.getSelbstbehaltGemeinde()); // dies entspricht genau 20% der Kosten
		Assert.assertEquals(MathUtil.DEFAULT.from(4644.16), detail.getBetragLastenausgleich()); // dies entspricht genau 80% der Kosten
	}

	@Test
	public void createLastenausgleichReicheGemeinde() {
		Gesuch gesuch = createGesuch(gp1718);
		//  Die durchschnittlichen Kostne pro 100% Platz sind doppelt so hoch wie in der Gemeinde
		BigDecimal hoheKostenPro100ProzentPlatz = MathUtil.DEFAULT.multiply(waeltiSelbstbehaltPro100Prozent, MathUtil.DEFAULT.from(2));

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleich(2017, hoheKostenPro100ProzentPlatz);
		Assert.assertNotNull(lastenausgleich);
		Assert.assertEquals(2017, lastenausgleich.getJahr().longValue());
		Assert.assertEquals(1, lastenausgleich.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(4353.90), lastenausgleich.getTotalAlleGemeinden());

		LastenausgleichDetail detail = lastenausgleich.getLastenausgleichDetails().iterator().next();
		Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
		Assert.assertEquals(2017, detail.getJahr().longValue());
		Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungenMitSelbstbehalt());
		Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheineMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(2902.60), detail.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(4353.90), detail.getBetragLastenausgleich());
	}

	@Test
	public void createLastenausgleichArmeGemeinde() {
		Gesuch gesuch = createGesuch(gp1718);
		//  Die durchschnittlichen Kostne pro 100% Platz sind halb so hoch wie in der Gemeinde
		BigDecimal tiefeKostenPro100ProzentPlatz = MathUtil.DEFAULT.divide(waeltiSelbstbehaltPro100Prozent, MathUtil.DEFAULT.from(2));

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleich(2017, tiefeKostenPro100ProzentPlatz);
		Assert.assertNotNull(lastenausgleich);
		Assert.assertEquals(2017, lastenausgleich.getJahr().longValue());
		Assert.assertEquals(1, lastenausgleich.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(6530.85), lastenausgleich.getTotalAlleGemeinden());

		LastenausgleichDetail detail = lastenausgleich.getLastenausgleichDetails().iterator().next();
		Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
		Assert.assertEquals(2017, detail.getJahr().longValue());
		Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungenMitSelbstbehalt());
		Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheineMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(725.65), detail.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(6530.85), detail.getBetragLastenausgleich());
	}

	@Test
	public void createLastenausgleichKorrekturen() {
		Gesuch gesuch = createGesuch(gp1718);

		// Lastenausgleich 2017 erstellen
		Lastenausgleich lastenausgleich2017 = lastenausgleichServiceBean.createLastenausgleich(2017, waeltiSelbstbehaltPro100Prozent);

		Assert.assertNotNull(lastenausgleich2017);
		Assert.assertEquals(2017, lastenausgleich2017.getJahr().longValue());
		Assert.assertEquals(1, lastenausgleich2017.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), lastenausgleich2017.getTotalAlleGemeinden());

		LastenausgleichDetail detail = lastenausgleich2017.getLastenausgleichDetails().iterator().next();
		Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
		Assert.assertEquals(2017, detail.getJahr().longValue());
		Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungenMitSelbstbehalt());
		Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheineMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(1451.30), detail.getSelbstbehaltGemeinde()); // dies entspricht genau 20% der Kosten
		Assert.assertEquals(MathUtil.DEFAULT.from(5805.20), detail.getBetragLastenausgleich()); // dies entspricht genau 80% der Kosten

		Assert.assertNotNull(lastenausgleich2017);

		// Nachtraeglich ein weiteres Gesuch verfuegen
		createGesuch(gp1718);

		// Lastenausgleich 2018 erstellen: Dies soll auch zu Korrekturen fuer 2017 fuehren
		Lastenausgleich lastenausgleich2018 = lastenausgleichServiceBean.createLastenausgleich(2018, waeltiSelbstbehaltPro100Prozent);
		Assert.assertNotNull(lastenausgleich2018);

		Assert.assertEquals(2018, lastenausgleich2018.getJahr().longValue());
		Assert.assertEquals(3, lastenausgleich2018.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(15093.52), lastenausgleich2018.getTotalAlleGemeinden());

		LastenausgleichDetail detail2018 = lastenausgleich2018.getLastenausgleichDetails().get(0);
		Assert.assertEquals(gesuch.extractGemeinde(), detail2018.getGemeinde());
		Assert.assertEquals(2018, detail2018.getJahr().longValue());
		Assert.assertEquals(MathUtil.DEFAULT.from(53.33), detail2018.getTotalBelegungenMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(11610.40), detail2018.getTotalBetragGutscheineMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(2322.08), detail2018.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(9288.32), detail2018.getBetragLastenausgleich());
		Assert.assertFalse(detail2018.isKorrektur());

		LastenausgleichDetail detail2017_korrektur = lastenausgleich2018.getLastenausgleichDetails().get(1);
		Assert.assertEquals(gesuch.extractGemeinde(), detail2017_korrektur.getGemeinde());
		Assert.assertEquals(2017, detail2017_korrektur.getJahr().longValue());
		Assert.assertEquals(MathUtil.DEFAULT.from(-33.33), detail2017_korrektur.getTotalBelegungenMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(-7256.50), detail2017_korrektur.getTotalBetragGutscheineMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(-1451.30), detail2017_korrektur.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(-5805.20), detail2017_korrektur.getBetragLastenausgleich());
		Assert.assertTrue(detail2017_korrektur.isKorrektur());

		LastenausgleichDetail detail2017_new = lastenausgleich2018.getLastenausgleichDetails().get(2);
		Assert.assertEquals(gesuch.extractGemeinde(), detail2017_new.getGemeinde());
		Assert.assertEquals(2017, detail2017_new.getJahr().longValue());
		Assert.assertEquals(MathUtil.DEFAULT.from(66.67), detail2017_new.getTotalBelegungenMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(14513.00), detail2017_new.getTotalBetragGutscheineMitSelbstbehalt());
		Assert.assertEquals(MathUtil.DEFAULT.from(2902.60), detail2017_new.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(11610.40), detail2017_new.getBetragLastenausgleich());
		Assert.assertTrue(detail2017_new.isKorrektur());
	}

	@Test
	public void createLastenausgleichKorrekturenMehrereJahre() {

		// Lastenausgleich 2017: 1 aus Erhebung
		createGesuch(gp1718);
		Lastenausgleich lastenausgleich2017 = lastenausgleichServiceBean.createLastenausgleich(2017, waeltiSelbstbehaltPro100Prozent);
		Assert.assertEquals(1, lastenausgleich2017.getLastenausgleichDetails().size());

		// Lastenausgleich 2018: 1 aus Erhebung, 2 aus Korrektur 2017
		createGesuch(gp1718);
		Lastenausgleich lastenausgleich2018 = lastenausgleichServiceBean.createLastenausgleich(2018, waeltiSelbstbehaltPro100Prozent);
		Assert.assertEquals(3, lastenausgleich2018.getLastenausgleichDetails().size());

		// Lastenausgleich 2019: 2 aus Korrektur 2017, 2 aus Korrektur 2018. Im 2019 kein Gesuch!
		createGesuch(gp1718);
		Lastenausgleich lastenausgleich2019 = lastenausgleichServiceBean.createLastenausgleich(2019, waeltiSelbstbehaltPro100Prozent);
		Assert.assertEquals(4, lastenausgleich2019.getLastenausgleichDetails().size());

	}

	private Gesuch createGesuch(Gesuchsperiode gesuchsperiode) {
		return testfaelleService.createAndSaveTestfaelle(TestfaelleService.WAELTI_DAGMAR, true, true, gemeinde, gesuchsperiode);
	}
}
