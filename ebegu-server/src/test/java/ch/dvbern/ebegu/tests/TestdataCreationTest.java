/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.TestdataCreationService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.TestfallName;
import ch.dvbern.ebegu.util.testdata.AnmeldungConfig;
import ch.dvbern.ebegu.util.testdata.ErstgesuchConfig;
import ch.dvbern.ebegu.util.testdata.MutationConfig;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests fuer den Zahlungsservice
 */
@SuppressWarnings({ "LocalVariableNamingConvention", "InstanceMethodNamingConvention", "InstanceVariableNamingConvention" })
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
public class TestdataCreationTest extends AbstractTestdataCreationTest {

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private TestdataCreationService testdataCreationService;

	@Inject
	private Persistence persistence;


	@Test
	public void gesuchsperiodePresentAfterSetup() {
		Collection<Gesuchsperiode> gesuchsperioden = criteriaQueryHelper.getAll(Gesuchsperiode.class);
		Assert.assertNotNull(gesuchsperioden);
		Assert.assertFalse(gesuchsperioden.isEmpty());
	}

	@Test
	public void mandantPresentAfterSetup() {
		Collection<Mandant> mandanten = criteriaQueryHelper.getAll(Mandant.class);
		Assert.assertNotNull(mandanten);
		Assert.assertFalse(mandanten.isEmpty());
	}

	@Test
	public void institutionsStammdatenPresentAfterSetup() {
		Collection<InstitutionStammdaten> institutionsStammdaten = criteriaQueryHelper.getAll(InstitutionStammdaten.class);
		Assert.assertNotNull(institutionsStammdaten);
		Assert.assertEquals("Es wurden 5 Institutionen erstellt", 5, institutionsStammdaten.size());
	}

	@Test
	public void createErstgesuchForDefaultPeriode() {
		ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(TestfallName.LUETHI_MERET, LocalDate.now(), LocalDateTime.now());
		Gesuch erstgesuch = testdataCreationService.createErstgesuch(config, mandant);
		Assert.assertNotNull(erstgesuch);
		Assert.assertNotNull(erstgesuch.getGesuchsperiode());
		Assert.assertEquals(gesuchsperiode, erstgesuch.getGesuchsperiode());
	}

	@Test
	public void createErstgesuchForAnotherPeriode() {
		Gesuchsperiode nextGesuchsperiode = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, gesuchsperiode.getBasisJahr(), gesuchsperiode.getBasisJahrPlus1());
		TestDataUtil.prepareParameters(nextGesuchsperiode, persistence);
		ErstgesuchConfig config = ErstgesuchConfig.createErstgesuch(TestfallName.LUETHI_MERET, nextGesuchsperiode, LocalDate.now());
		Gesuch erstgesuch = testdataCreationService.createErstgesuch(config, mandant);
		Assert.assertNotNull(erstgesuch);
		Assert.assertNotNull(erstgesuch.getGesuchsperiode());
		Assert.assertEquals(gesuchsperiode.getBasisJahr(), erstgesuch.getGesuchsperiode().getBasisJahrPlus1());
	}

	@Test
	public void createEmptyMutation() {
		ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(TestfallName.LUETHI_MERET, LocalDate.now(), LocalDateTime.now());
		Gesuch erstgesuch = testdataCreationService.createErstgesuch(config, mandant);
		Gesuch mutation = testdataCreationService.createMutation(MutationConfig.createEmptyMutationVerfuegt(LocalDate.now(),
			LocalDateTime.now()), erstgesuch);

		Assert.assertNotNull(mutation);
		Assert.assertTrue(mutation.isGueltig());
		Assert.assertNotNull(mutation.getTimestampVerfuegt());
		Assert.assertEquals(AntragStatus.VERFUEGT, mutation.getStatus());
		Assert.assertTrue(mutation.isMutation());
		Assert.assertEquals(GesuchBetreuungenStatus.ALLE_BESTAETIGT, mutation.getGesuchBetreuungenStatus());
	}

	@Test
	public void createMutation() {
		ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(TestfallName.LUETHI_MERET, LocalDate.now(), LocalDateTime.now());
		Gesuch erstgesuch = testdataCreationService.createErstgesuch(config, mandant);
		Gesuch mutation = testdataCreationService.createMutation(MutationConfig.createMutationVerfuegt(LocalDate.now(), LocalDateTime.now(),
			50, false), erstgesuch);

		Assert.assertNotNull(mutation);
		Assert.assertTrue(mutation.isGueltig());
		Assert.assertNotNull(mutation.getTimestampVerfuegt());
		Assert.assertEquals(AntragStatus.VERFUEGT, mutation.getStatus());
		Assert.assertTrue(mutation.isMutation());
		Assert.assertEquals(GesuchBetreuungenStatus.ALLE_BESTAETIGT, mutation.getGesuchBetreuungenStatus());
	}

	@Test
	public void createAnmeldungTagesschule() {
		ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(TestfallName.LUETHI_MERET, LocalDate.now(), LocalDateTime.now());
		Gesuch erstgesuch = testdataCreationService.createErstgesuch(config, mandant);
		int anzahlBetreuungenBefore = erstgesuch.extractAllBetreuungen().size();
		erstgesuch = testdataCreationService.addAnmeldung(AnmeldungConfig.createAnmeldungTagesschule(), erstgesuch);
		Assert.assertEquals(anzahlBetreuungenBefore + 1, erstgesuch.extractAllPlaetze().size());
	}
}
