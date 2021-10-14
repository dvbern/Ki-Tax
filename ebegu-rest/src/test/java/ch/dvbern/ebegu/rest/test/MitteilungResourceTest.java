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

package ch.dvbern.ebegu.rest.test;

import java.io.StringWriter;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ch.dvbern.ebegu.api.dtos.JaxBetreuungsmitteilung;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxMitteilung;
import ch.dvbern.ebegu.api.dtos.JaxMitteilungen;
import ch.dvbern.ebegu.api.resource.MitteilungResource;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Testet BetreuungResource
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class MitteilungResourceTest extends AbstractEbeguRestLoginTest {

	@Inject
	private MitteilungResource mitteilungResource;

	@Inject
	private Persistence persistence;

	@Test
	public void getMitteilungenOfDossierForCurrentRolleNoDossier() {
		try {
			mitteilungResource.getMitteilungenOfDossierForCurrentRolle(
				new JaxId("1d1dd5db-32f1-11e6-8ae4-acab47941422"),
				DUMMY_URIINFO,
				DUMMY_RESPONSE);
			Assert.fail("Exception should be thrown. The Fall doesn't exist");
		} catch (EbeguEntityNotFoundException e) {
			// nop
		}
	}

	@Test
	public void getMitteilungenOfDossierForCurrentRolleNoMitteilungen() {
		final Dossier dossier = createAndPersistDossier();

		final JaxMitteilungen mitteilungen = mitteilungResource
			.getMitteilungenOfDossierForCurrentRolle(new JaxId(dossier.getId()), DUMMY_URIINFO, DUMMY_RESPONSE);

		Assert.assertNotNull(mitteilungen);
		Assert.assertEquals(0, mitteilungen.getMitteilungen().size());
	}

	@Test
	public void getMitteilungenOfDossierForCurrentRolleNormalMitteilungen() {
		final Benutzer empfaengerJA = getDummySuperadmin();
		final Dossier dossier = createAndPersistDossier();
		final Benutzer sender = createAndPersistSender();

		final Mitteilung mitteilung = TestDataUtil.createMitteilung(
			dossier,
			empfaengerJA,
			MitteilungTeilnehmerTyp.JUGENDAMT,
			sender,
			MitteilungTeilnehmerTyp.INSTITUTION);
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		persistence.persist(mitteilung);

		final JaxMitteilungen mitteilungen = mitteilungResource
			.getMitteilungenOfDossierForCurrentRolle(new JaxId(dossier.getId()), DUMMY_URIINFO, DUMMY_RESPONSE);

		Assert.assertNotNull(mitteilungen);
		Assert.assertEquals(1, mitteilungen.getMitteilungen().size());
		Assert.assertSame(JaxMitteilung.class, mitteilungen.getMitteilungen().iterator().next().getClass());
		Assert.assertEquals(mitteilung.getId(), mitteilungen.getMitteilungen().iterator().next().getId());
	}

	@Transactional(TransactionMode.DEFAULT)
	@Test
	public void getMitteilungenOfDossierForCurrentRolle() throws JAXBException, JsonProcessingException {
		final Benutzer empfaengerJA = loginAsSachbearbeiterJA();
		final Dossier dossier = createAndPersistDossier();
		final Benutzer sender = createAndPersistSender();

		final Mitteilung mitteilung = TestDataUtil.createMitteilung(
			dossier,
			empfaengerJA,
			MitteilungTeilnehmerTyp.JUGENDAMT,
			sender,
			MitteilungTeilnehmerTyp.INSTITUTION);
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		persistence.persist(mitteilung);

		final JaxMitteilungen mitteilungen = mitteilungResource
			.getMitteilungenOfDossierForCurrentRolle(new JaxId(dossier.getId()), DUMMY_URIINFO, DUMMY_RESPONSE);

		Assert.assertNotNull(mitteilungen);
		Assert.assertEquals(1, mitteilungen.getMitteilungen().size());
		final Iterator<JaxMitteilung> iterator = mitteilungen.getMitteilungen().iterator();

		// Test Marshalling values
		JAXBContext jaxbContext = JAXBContext.newInstance(JaxMitteilung.class, JaxBetreuungsmitteilung.class);
		final Marshaller marshaller = jaxbContext.createMarshaller();

		final StringWriter stringFirst = new StringWriter();
		final JaxMitteilung first = iterator.next();
		Assert.assertSame(JaxMitteilung.class, first.getClass());
		Assert.assertEquals(mitteilung.getId(), first.getId());
		marshaller.marshal(first, stringFirst);
		Assert.assertFalse(stringFirst.toString().contains("betreuungspensen"));
		final ObjectMapper o = new ObjectMapper();
		final String s = o.writeValueAsString(first);
		Assert.assertFalse(s.contains("betreuungspensen"));
	}

	// HELP METHODS

	@Nonnull
	private Dossier createAndPersistDossier() {
		return TestDataUtil.createAndPersistDossierAndFall(persistence);
	}

	private Benutzer createAndPersistSender() {
		final Mandant mandant = persistence.find(Mandant.class, "e3736eb8-6eef-40ef-9e52-96ab48d8f220");
		final Traegerschaft traegerschaft = persistence.persist(TestDataUtil.createDefaultTraegerschaft(mandant));
		final Benutzer senderINST = TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, "insti",
			traegerschaft, null, mandant, persistence, null, null);
		persistence.persist(senderINST);
		return senderINST;
	}
}
