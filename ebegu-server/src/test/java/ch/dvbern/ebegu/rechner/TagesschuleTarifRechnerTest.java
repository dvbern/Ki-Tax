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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.services.VerfuegungService;
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

/**
 * Test fuer TagesschuleTarifRechner
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class TagesschuleTarifRechnerTest extends AbstractEbeguLoginTest {

	private final BigDecimal MATA_MIT_PEDAGOGISCHE_BETREUUNG = new BigDecimal(12.24);
	private final BigDecimal MATA_OHNE_PEDAGOGISCHE_BETREUUNG = new BigDecimal(6.11);
	private final BigDecimal MITA = new BigDecimal(0.78);
	private final BigDecimal MAXIMAL_MASSGEGEBENES_EINKOMMEN = new BigDecimal(160000.00);
	private final BigDecimal MINIMAL_MASSGEGEBENES_EINKOMMEN = new BigDecimal(43000.00);

	TagesschuleTarifRechner ttr = new TagesschuleTarifRechner(MATA_MIT_PEDAGOGISCHE_BETREUUNG,
		MATA_OHNE_PEDAGOGISCHE_BETREUUNG, MITA, MAXIMAL_MASSGEGEBENES_EINKOMMEN, MINIMAL_MASSGEGEBENES_EINKOMMEN);

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private Persistence persistence;

	private Gesuchsperiode gesuchsperiode;

	@Before
	public void setUp() {
		gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
	}

	@Test
	public void testTarifBerechnung() {
		Gesuch testfall11 = TestDataUtil.createAndPersistTestfall11_SchulamtOnly(persistence, LocalDate.of(1980,
			Month.MARCH, 25), AntragStatus.NUR_SCHULAMT, gesuchsperiode);

		BigDecimal massgegebenesEinkommenMitAbzug = new BigDecimal(80600);

		BigDecimal tarifPedagogischeBetreut = ttr.calculateTarifProStunde(testfall11, true,
			massgegebenesEinkommenMitAbzug);
		BigDecimal tarifNichtPedagogischeBetreut = ttr.calculateTarifProStunde(testfall11, false,
			massgegebenesEinkommenMitAbzug);

		Assert.assertEquals(MathUtil.DEFAULT.from(MATA_MIT_PEDAGOGISCHE_BETREUUNG), tarifPedagogischeBetreut);
		Assert.assertEquals(MathUtil.DEFAULT.from(MATA_OHNE_PEDAGOGISCHE_BETREUUNG), tarifNichtPedagogischeBetreut);

		assert testfall11.getGesuchsteller1() != null;
		assert testfall11.getGesuchsteller1().getFinanzielleSituationContainer() != null;
		testfall11.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerveranlagungErhalten(false);

		persistence.merge(testfall11);

		tarifPedagogischeBetreut = ttr.calculateTarifProStunde(testfall11, true, massgegebenesEinkommenMitAbzug);
		tarifNichtPedagogischeBetreut = ttr.calculateTarifProStunde(testfall11, false, massgegebenesEinkommenMitAbzug);

		Assert.assertEquals(MathUtil.DEFAULT.from(new BigDecimal(4.46)), tarifPedagogischeBetreut);
		Assert.assertEquals(MathUtil.DEFAULT.from(new BigDecimal(2.49)), tarifNichtPedagogischeBetreut);

		massgegebenesEinkommenMitAbzug = new BigDecimal(138000);  //4 Person

		tarifPedagogischeBetreut = ttr.calculateTarifProStunde(testfall11, true, massgegebenesEinkommenMitAbzug);
		tarifNichtPedagogischeBetreut = ttr.calculateTarifProStunde(testfall11, false, massgegebenesEinkommenMitAbzug);

		Assert.assertEquals(MathUtil.DEFAULT.from(new BigDecimal(10.09)), tarifPedagogischeBetreut);
		Assert.assertEquals(MathUtil.DEFAULT.from(new BigDecimal(5.11)), tarifNichtPedagogischeBetreut);

		massgegebenesEinkommenMitAbzug = new BigDecimal(102000);

		tarifPedagogischeBetreut = ttr.calculateTarifProStunde(testfall11, true, massgegebenesEinkommenMitAbzug);
		tarifNichtPedagogischeBetreut = ttr.calculateTarifProStunde(testfall11, false, massgegebenesEinkommenMitAbzug);

		Assert.assertEquals(MathUtil.DEFAULT.from(new BigDecimal(6.56)), tarifPedagogischeBetreut);
		Assert.assertEquals(MathUtil.DEFAULT.from(new BigDecimal(3.47)), tarifNichtPedagogischeBetreut);
	}
}
