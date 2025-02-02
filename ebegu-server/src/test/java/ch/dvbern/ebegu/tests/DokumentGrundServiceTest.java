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

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.DokumentService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests fuer die Klasse DokumentGrundService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class DokumentGrundServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private DokumentService dokumentService;

	@Inject
	private Persistence persistence;

	@Test
	public void createDokumentGrund() {
		Assert.assertNotNull(dokumentGrundService);

		createDokumentGrundAndAssertCreation();
	}

	@Test
	public void removeIfEmpty_NotEmpty() {
		DokumentGrund dokumentGrund = createDokumentGrundAndAssertCreation();

		dokumentGrundService.removeIfEmpty(dokumentGrund);

		Optional<DokumentGrund> dokumentGrundRemovedOpt = dokumentGrundService.findDokumentGrund(dokumentGrund.getId());
		Assert.assertTrue(dokumentGrundRemovedOpt.isPresent());
	}

	@Test
	@Transactional(TransactionMode.DEFAULT)
	public void removeIfEmpty_Empty() {
		DokumentGrund dokumentGrund = createDokumentGrundAndAssertCreation();

		dokumentGrund.getDokumente().forEach(dokument -> {
			final Optional<Dokument> foundDokument = dokumentService.findDokument(dokument.getId());
			Assert.assertTrue(foundDokument.isPresent());
			dokumentService.removeDokument(foundDokument.get());
		});

		Optional<DokumentGrund> dokumentGrundRemovedOpt = dokumentGrundService.findDokumentGrund(dokumentGrund.getId());
		Assert.assertFalse(dokumentGrundRemovedOpt.isPresent());
	}

	@Nonnull
	private DokumentGrund createDokumentGrundAndAssertCreation() {
		DokumentGrund dokumentGrund = TestDataUtil.createDefaultDokumentGrund();
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		dokumentGrund.setGesuch(gesuch);

		dokumentGrundService.saveDokumentGrund(dokumentGrund);

		Optional<DokumentGrund> dokumentGrundOpt = dokumentGrundService.findDokumentGrund(dokumentGrund.getId());
		Assert.assertTrue(dokumentGrundOpt.isPresent());
		return dokumentGrund;
	}

}
