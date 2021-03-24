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

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.MahnungService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.hibernate.Hibernate;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Arquillian Tests fuer die Klasse MahnungService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class MahnungServiceTest extends AbstractEbeguLoginTest {

	private Gemeinde bern;

	@Inject
	private MahnungService mahnungService;

	@Inject
	private Persistence persistence;

	@Before
	public void setUp() {
		bern = TestDataUtil.getGemeindeParis(persistence);
	}

	@Test
	public void createErsteMahnung() {
		Assert.assertNotNull(mahnungService);
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, bern);
		Mahnung mahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch));
		Assert.assertNotNull(mahnung);
		Assert.assertEquals(MahnungTyp.ERSTE_MAHNUNG, mahnung.getMahnungTyp());
	}

	@Test
	public void createZweiteMahnung() {
		Assert.assertNotNull(mahnungService);
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, bern);
		Mahnung ersteMahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch));
		Assert.assertNotNull(ersteMahnung);
		Mahnung zweiteMahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ZWEITE_MAHNUNG, gesuch));
		Assert.assertNotNull(zweiteMahnung);
	}

	@Test(expected = EbeguRuntimeException.class)
	public void createZweiteMahnungOhneErsteMahnung() {
		Assert.assertNotNull(mahnungService);
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, bern);
		mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ZWEITE_MAHNUNG, gesuch));
	}

	@Test
	public void findMahnung() {
		Assert.assertNotNull(mahnungService);
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, bern);
		Mahnung mahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch));
		Assert.assertNotNull(mahnung);

		Optional<Mahnung> readMahnungOptional = mahnungService.findMahnung(mahnung.getId());
		Assert.assertTrue(readMahnungOptional.isPresent());
		Assert.assertEquals(mahnung, readMahnungOptional.get());
	}

	@Test
	public void findMahnungenForGesuch() {
		Assert.assertNotNull(mahnungService);
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, bern);
		Mahnung ersteMahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch));
		Assert.assertNotNull(ersteMahnung);

		Collection<Mahnung> mahnungenForGesuch = mahnungService.findMahnungenForGesuch(gesuch);
		Assert.assertNotNull(mahnungenForGesuch);
		Assert.assertFalse(mahnungenForGesuch.isEmpty());
		Assert.assertEquals(1, mahnungenForGesuch.size());

		Mahnung zweiteMahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ZWEITE_MAHNUNG, gesuch));
		Assert.assertNotNull(zweiteMahnung);

		mahnungenForGesuch = mahnungService.findMahnungenForGesuch(gesuch);
		Assert.assertNotNull(mahnungenForGesuch);
		Assert.assertFalse(mahnungenForGesuch.isEmpty());
		Assert.assertEquals(2, mahnungenForGesuch.size());
	}

	@Test
	public void mahnlaufBeenden() {
		Assert.assertNotNull(mahnungService);
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, bern);
		mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch));
		mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ZWEITE_MAHNUNG, gesuch));

		// Alle Mahnungen sind aktiv
		Collection<Mahnung> mahnungenForGesuch = mahnungService.findMahnungenForGesuch(gesuch);
		Assert.assertNotNull(mahnungenForGesuch);
		for (Mahnung mahnung : mahnungenForGesuch) {
			Assert.assertNull(mahnung.getTimestampAbgeschlossen());
		}

		mahnungService.mahnlaufBeenden(gesuch);

		// Alle Mahnungen sind geschlossen
		mahnungenForGesuch = mahnungService.findMahnungenForGesuch(gesuch);
		Assert.assertNotNull(mahnungenForGesuch);
		for (Mahnung mahnung : mahnungenForGesuch) {
			Assert.assertNotNull(mahnung.getTimestampAbgeschlossen());
		}
	}

	@Test
	public void fristAblaufTimer() {
		Assert.assertNotNull(mahnungService);
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);

		Gesuch gesuchMitMahnung = TestDataUtil.createAndPersistGesuch(persistence, bern);
		gesuchMitMahnung.setStatus(AntragStatus.ERSTE_MAHNUNG);
		persistence.merge(gesuchMitMahnung);
		mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuchMitMahnung, LocalDate.now().plusWeeks(1), 3));

		Gesuch gesuchMitAbgelaufenerMahnung = createGesuchWithAbgelaufenerMahnung();

		mahnungService.fristAblaufTimer();

		Assert.assertEquals(AntragStatus.ERSTE_MAHNUNG, persistence.find(Gesuch.class, gesuchMitMahnung.getId()).getStatus());
		Assert.assertEquals(AntragStatus.ERSTE_MAHNUNG_ABGELAUFEN, persistence.find(Gesuch.class, gesuchMitAbgelaufenerMahnung.getId()).getStatus());
	}

	@Test
	public void fristAblaufTimerZweiteMahnung_Zukuenftig() {
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);
		Gesuch gesuch = createGesuchWithAbgelaufenerMahnung();

		mahnungService.fristAblaufTimer();
		gesuch = persistence.find(Gesuch.class, gesuch.getId()); // needed because the method fristAblaufTimer has persisted it

		Assert.assertEquals(AntragStatus.ERSTE_MAHNUNG_ABGELAUFEN, gesuch.getStatus());

		gesuch.setStatus(AntragStatus.ZWEITE_MAHNUNG);
		gesuch = persistence.merge(gesuch);
		if(!Hibernate.isInitialized(gesuch.getKindContainers())){ //problem with lazy loading and transaction.disabled
			gesuch.setKindContainers(Collections.emptySet());
		}
		Mahnung secondMahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ZWEITE_MAHNUNG, gesuch,
			LocalDate.now().plusWeeks(1), 3));

		mahnungService.fristAblaufTimer();
		gesuch = persistence.find(Gesuch.class, gesuch.getId()); // needed because the method fristAblaufTimer has persisted it
		secondMahnung = persistence.find(Mahnung.class, secondMahnung.getId());

		Assert.assertEquals(AntragStatus.ZWEITE_MAHNUNG, gesuch.getStatus());
		Assert.assertFalse(secondMahnung.getAbgelaufen());
	}

	@Test
	public void fristAblaufTimerZweiteMahnung_Vergangen() {
		TestDataUtil.createAndPersistTraegerschaftBenutzer(persistence);
		Gesuch gesuch = createGesuchWithAbgelaufenerMahnung();

		mahnungService.fristAblaufTimer();
		gesuch = persistence.find(Gesuch.class, gesuch.getId()); // needed because the method fristAblaufTimer has persisted it

		Assert.assertEquals(AntragStatus.ERSTE_MAHNUNG_ABGELAUFEN, gesuch.getStatus());

		gesuch.setStatus(AntragStatus.ZWEITE_MAHNUNG);
		gesuch = persistence.merge(gesuch);
		if(!Hibernate.isInitialized(gesuch.getKindContainers())){ //problem with lazy loading and transaction.disabled
			gesuch.setKindContainers(Collections.emptySet());
		}
		Mahnung secondMahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ZWEITE_MAHNUNG, gesuch,
			LocalDate.now().minusDays(1), 3));

		mahnungService.fristAblaufTimer();
		gesuch = persistence.find(Gesuch.class, gesuch.getId()); // needed because the method fristAblaufTimer has persisted it
		secondMahnung = persistence.find(Mahnung.class, secondMahnung.getId());

		Assert.assertEquals(AntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN, gesuch.getStatus());
		Assert.assertTrue(secondMahnung.getAbgelaufen());
	}

	@Nonnull
	private Gesuch createGesuchWithAbgelaufenerMahnung() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, bern);
		gesuch.setStatus(AntragStatus.ERSTE_MAHNUNG);
		gesuch = persistence.merge(gesuch);
		final Mahnung mahnung = mahnungService.createMahnung(TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch,
			LocalDate.now().minusDays(1), 3));
		return mahnung.getGesuch();
	}
}

