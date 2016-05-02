package ch.dvbern.ebegu.tests;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Person;
import ch.dvbern.ebegu.services.AdresseService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Tests fuer die Klasse AdresseService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class AdresseServiceBeanTest extends AbstractEbeguTest {

	@Inject
	private AdresseService adresseService;

	@Inject
	private Persistence<Adresse> persistence;

	@Deployment
	public static Archive<?> createDeploymentEnvironment() {
		return createTestArchive();
	}


	@Test
	public void createAdresseTogetherWithPersonTest() {
		Person pers  = TestDataUtil.createDefaultPerson();
		Person storedPerson = persistence.persist(pers);
		Assert.assertNotNull(storedPerson.getAdressen());
		Assert.assertTrue(storedPerson.getAdressen().stream().findAny().isPresent());

	}

	@Test
	public void updateAdresseTest() {
		Assert.assertNotNull(adresseService);
		Adresse insertedAdresses = insertNewEntity();
		Optional<Adresse> adresse = adresseService.findAdresse(insertedAdresses.getId());
		Assert.assertEquals("21", adresse.get().getHausnummer());

		adresse.get().setHausnummer("99");
		Adresse updatedAdr = adresseService.updateAdresse(adresse.get());
		Assert.assertEquals("99", updatedAdr.getHausnummer());
		Assert.assertEquals("99", adresseService.findAdresse(updatedAdr.getId()).get().getHausnummer());
	}

	@Test
	public void removeAdresseTest() {
		Assert.assertNotNull(adresseService);
		Adresse insertedAdresses = insertNewEntity();
		Assert.assertEquals(1, adresseService.getAllAdressen().size());
		adresseService.removeAdresse(insertedAdresses);
		Assert.assertEquals(0, adresseService.getAllAdressen().size());
	}

	// Help Methods
	private Adresse insertNewEntity() {
		Person pers = TestDataUtil.createDefaultPerson();
		Person storedPers =  persistence.persist(pers);
		return storedPers.getAdressen().stream().findAny().orElseThrow(() -> new IllegalStateException("Testdaten nicht korrekt aufgesetzt"));
	}

}