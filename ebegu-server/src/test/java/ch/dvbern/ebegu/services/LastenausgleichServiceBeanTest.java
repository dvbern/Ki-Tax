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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
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
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class LastenausgleichServiceBeanTest extends AbstractEbeguLoginTest {

	// Testdaten
	// Wir testen mit Dagmar Waelti. 1 mal Dagmar Waelti gibt folgende Werte
	private final BigDecimal waeltiTotalGutscheinHalbjahr1 = MathUtil.DEFAULT.fromNullSafe(7256.50);
	private final BigDecimal waeltiTotalGutscheinHalbjahr2 = MathUtil.DEFAULT.fromNullSafe(5805.20);
	private final BigDecimal waeltiTotalBelegungHalbjahr1 = MathUtil.DEFAULT.fromNullSafe(33.35);
	private final BigDecimal waeltiTotalBelegungHalbjahr2 = MathUtil.DEFAULT.fromNullSafe(26.65);
	private final BigDecimal waeltiKostenPro100ProzentHalbjahr1 = MathUtil.DEFAULT.fromNullSafe(21769.50);
	private final BigDecimal waeltiKostenPro100ProzentHalbjahr2 = MathUtil.DEFAULT.fromNullSafe(21769.50);

	private Gesuchsperiode gp1718;

	@Inject
	private LastenausgleichService lastenausgleichServiceBean;

	@Inject
	private Persistence persistence;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private InstitutionService institutionService;


	@Before
	public void init() {
		gp1718 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2017, 2018);
		TestDataUtil.prepareParameters(gp1718, persistence);
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
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleich(2017, waeltiKostenPro100ProzentHalbjahr1);
		Assert.assertNotNull(lastenausgleich);
		Assert.assertEquals(2017, lastenausgleich.getJahr().longValue());
		Assert.assertEquals(1, lastenausgleich.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(5819.71), lastenausgleich.getTotalAlleGemeinden());

		LastenausgleichDetail detail = lastenausgleich.getLastenausgleichDetails().iterator().next();
		Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
		Assert.assertEquals(2017, detail.getJahr().longValue());
		Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungen());
		Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheine());
		Assert.assertEquals(MathUtil.DEFAULT.from(1436.79), detail.getSelbstbehaltGemeinde()); // dies entspricht genau 20% der Kosten
		Assert.assertEquals(MathUtil.DEFAULT.from(5819.71), detail.getBetragLastenausgleich()); // dies entspricht genau 80% der Kosten
	}

	@Test
	public void createLastenausgleichReicheGemeinde() {
		Gesuch gesuch = createGesuch(gp1718);
		//  Die durchschnittlichen Kostne pro 100% Platz sind doppelt so hoch wie in der Gemeinde
		BigDecimal hoheKostenPro100ProzentPlatz = MathUtil.DEFAULT.multiply(waeltiKostenPro100ProzentHalbjahr1, MathUtil.DEFAULT.from(2));

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleich(2017, hoheKostenPro100ProzentPlatz);
		Assert.assertNotNull(lastenausgleich);
		Assert.assertEquals(2017, lastenausgleich.getJahr().longValue());
		Assert.assertEquals(1, lastenausgleich.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(4382.93), lastenausgleich.getTotalAlleGemeinden());

		LastenausgleichDetail detail = lastenausgleich.getLastenausgleichDetails().iterator().next();
		Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
		Assert.assertEquals(2017, detail.getJahr().longValue());
		Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungen());
		Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheine());
		Assert.assertEquals(MathUtil.DEFAULT.from(2873.57), detail.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(4382.93), detail.getBetragLastenausgleich());
	}

	@Test
	public void createLastenausgleichArmeGemeinde() {
		Gesuch gesuch = createGesuch(gp1718);
		//  Die durchschnittlichen Kostne pro 100% Platz sind halb so hoch wie in der Gemeinde
		BigDecimal tiefeKostenPro100ProzentPlatz = MathUtil.DEFAULT.divide(waeltiKostenPro100ProzentHalbjahr1, MathUtil.DEFAULT.from(2));

		// Lastenausgleich erstellen
		Lastenausgleich lastenausgleich = lastenausgleichServiceBean.createLastenausgleich(2017, tiefeKostenPro100ProzentPlatz);
		Assert.assertNotNull(lastenausgleich);
		Assert.assertEquals(2017, lastenausgleich.getJahr().longValue());
		Assert.assertEquals(1, lastenausgleich.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(6538.11), lastenausgleich.getTotalAlleGemeinden());

		LastenausgleichDetail detail = lastenausgleich.getLastenausgleichDetails().iterator().next();
		Assert.assertEquals(gesuch.extractGemeinde(), detail.getGemeinde());
		Assert.assertEquals(2017, detail.getJahr().longValue());
		Assert.assertEquals(waeltiTotalBelegungHalbjahr1, detail.getTotalBelegungen());
		Assert.assertEquals(waeltiTotalGutscheinHalbjahr1, detail.getTotalBetragGutscheine());
		Assert.assertEquals(MathUtil.DEFAULT.from(718.39), detail.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(6538.11), detail.getBetragLastenausgleich());
	}

	@Test
	public void createLastenausgleichKorrekturen() {
		Gesuch gesuch = createGesuch(gp1718);

		// Lastenausgleich 2017 erstellen
		Lastenausgleich lastenausgleich2017 = lastenausgleichServiceBean.createLastenausgleich(2017, waeltiKostenPro100ProzentHalbjahr1);
		Assert.assertNotNull(lastenausgleich2017);

		// Nachtraeglich ein weiteres Gesuch verfuegen
		createGesuch(gp1718);

		// Lastenausgleich 2018 erstellen: Dies soll auch zu Korrekturen fuer 2017 fuehren
		Lastenausgleich lastenausgleich2018 = lastenausgleichServiceBean.createLastenausgleich(2018, waeltiKostenPro100ProzentHalbjahr2);
		Assert.assertNotNull(lastenausgleich2018);

		Assert.assertEquals(2018, lastenausgleich2018.getJahr().longValue());
		Assert.assertEquals(2, lastenausgleich2018.getLastenausgleichDetails().size());
		Assert.assertEquals(MathUtil.DEFAULT.from(20898.72), lastenausgleich2018.getTotalAlleGemeinden());

		LastenausgleichDetail detail2018 = lastenausgleich2018.getLastenausgleichDetails().get(0);
		Assert.assertEquals(gesuch.extractGemeinde(), detail2018.getGemeinde());
		Assert.assertEquals(2018, detail2018.getJahr().longValue());
		Assert.assertEquals(MathUtil.DEFAULT.from(53.30), detail2018.getTotalBelegungen());
		Assert.assertEquals(MathUtil.DEFAULT.from(11610.40), detail2018.getTotalBetragGutscheine());
		Assert.assertEquals(MathUtil.DEFAULT.from(2307.57), detail2018.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(9302.83), detail2018.getBetragLastenausgleich());
		Assert.assertFalse(detail2018.isKorrektur());

		LastenausgleichDetail detail2017 = lastenausgleich2018.getLastenausgleichDetails().get(1);
		Assert.assertEquals(gesuch.extractGemeinde(), detail2017.getGemeinde());
		Assert.assertEquals(2017, detail2017.getJahr().longValue());
		Assert.assertEquals(MathUtil.DEFAULT.from(66.70), detail2017.getTotalBelegungen());
		Assert.assertEquals(MathUtil.DEFAULT.from(14513.00), detail2017.getTotalBetragGutscheine());
		Assert.assertEquals(MathUtil.DEFAULT.from(2917.11), detail2017.getSelbstbehaltGemeinde());
		Assert.assertEquals(MathUtil.DEFAULT.from(11595.89), detail2017.getBetragLastenausgleich());
		Assert.assertTrue(detail2017.isKorrektur());
	}

	private Gesuch createGesuch(Gesuchsperiode gesuchsperiode) {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence, null, AntragStatus.VERFUEGT,
			gesuchsperiode);
		TestDataUtil.calculateFinanzDaten(gesuch);
		Gesuch erstgesuch = verfuegungService.calculateVerfuegung(gesuch);
		for (Betreuung betreuung : erstgesuch.extractAllBetreuungen()) {
			verfuegungService.verfuegen(erstgesuch.getId(), betreuung.getId(), "bla", false, false);
		}
		return erstgesuch;
	}
}
