package ch.dvbern.ebegu.tests;

import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.DokumentService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Tests fuer die Klasse DokumentGrundService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class DokumentServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private DokumentService dokumentService;

	@Inject
	private Persistence<Gesuch> persistence;



	@Test
	public void createDokument() {
		Assert.assertNotNull(dokumentGrundService);
		Assert.assertNotNull(dokumentService);

		DokumentGrund dokumentGrund = TestDataUtil.createDefaultDokumentGrund();
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		dokumentGrund.setGesuch(gesuch);

		dokumentGrundService.saveDokumentGrund(dokumentGrund);
		Optional<DokumentGrund> dokumentGrundOpt = dokumentGrundService.findDokumentGrund(dokumentGrund.getId());
		Assert.assertTrue(dokumentGrundOpt.isPresent());
		Assert.assertEquals(dokumentGrund.getFullName(), dokumentGrundOpt.get().getFullName());

		final Optional<Dokument> dokument = dokumentService.findDokument(dokumentGrund.getDokumente().iterator().next().getId());
		Assert.assertTrue(dokument.isPresent());

	}

}
