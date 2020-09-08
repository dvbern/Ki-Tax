/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests;

import java.util.List;

import javax.inject.Inject;

import ch.dvbern.ebegu.dto.geoadmin.JaxWohnadresse;
import ch.dvbern.ebegu.services.GeoadminSearchService;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
public class GeoadminSearchServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private GeoadminSearchService service;

	@Test
	public void testSearchAddressesFromWohnungsregister() {
		List<JaxWohnadresse> jaxWohnadresses =
			service.findWohnadressenBySearchText("Laubeggstrasse 30 3006");

		assertEquals(1, jaxWohnadresses.size());
	}

	@Test
	public void testFindGemeindeBern() {
		List<JaxWohnadresse> jaxWohnadressen = service.findWohnadressenByStrasseAndPlz("Spitalgasse", "1", "3011");
		assertEquals("Bern", jaxWohnadressen.get(0).getGemeinde());
		assertEquals(351, (long) jaxWohnadressen.get(0).getGemeindeBfsNr());
	}
}
