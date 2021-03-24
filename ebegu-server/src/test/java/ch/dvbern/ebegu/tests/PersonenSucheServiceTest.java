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
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.services.PersonenSucheService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Arquillian Tests fuer den PersonenSuche Service
 */
@SuppressWarnings("InstanceMethodNamingConvention")
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class PersonenSucheServiceTest extends AbstractEbeguLoginTest {

	private static final String ID_SCHUHMACHER = "1000028027";

	@Inject
	private PersonenSucheService personenSucheService;


	@Test
	public void suchePersonByGesuch() throws Exception {
		Gesuch gesuch = TestDataUtil.createTestgesuchDagmar();
		gesuch.getDossier().getGemeinde().setBfsNummer(351L);
		Assert.assertNotNull(		"bfs nummer muss gesetzt sein fuer suche", gesuch.getDossier().getGemeinde().getBfsNummer());

		EWKResultat ewkResultat = personenSucheService.suchePersonen(gesuch);
		Assert.assertNotNull(ewkResultat);
		Assert.assertEquals(3 , ewkResultat.getPersonen().size()); // gs1 und gs2 sowie ein kind existieren im gesuch, wir erwarten IMMer fuer alle eine Antwort

		//found GS
		final List<EWKPerson> gesuchstellerRes = ewkResultat.getPersonen().stream().filter(EWKPerson::isGesuchsteller).collect(Collectors.toList());
		Assert.assertEquals(1, gesuchstellerRes.size());

		//found kind
		final List<EWKPerson> kinderResults = ewkResultat.getPersonen().stream().filter(EWKPerson::isKind).collect(Collectors.toList());
		Assert.assertEquals("Should find exactly the one Kind in the Gesuch", 1, kinderResults.size());
		EWKPerson kindResult = kinderResults.get(0);
		Assert.assertEquals(LocalDate.of(2014,4,13), kindResult.getGeburtsdatum());
		Assert.assertEquals("Simon", kindResult.getVorname());
		Assert.assertEquals("Wälti-Muster", kindResult.getNachname());
	}


}
