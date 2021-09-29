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

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.TestdataCreationService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.testdata.TestdataSetupConfig;
import org.junit.Before;

/**
 * Superklasse fuer Tests, welche den TestdataCreationService verwenden.
 * Initialisiert diesen mit Mandant, Gesuchsperiode und Institutionen
 */
public abstract class AbstractTestdataCreationTest extends AbstractEbeguLoginTest {

	@Inject
	private TestdataCreationService testdataCreationService;

	protected Gesuchsperiode gesuchsperiode;

	protected Mandant mandant;

	@Before
	public void init() {
		// Tests initialisieren
		gesuchsperiode = createGesuchsperiode();
		final InstitutionStammdaten kitaAaregg = TestDataUtil.createInstitutionStammdatenKitaWeissenstein();
		final InstitutionStammdaten kitaBruennen = TestDataUtil.createInstitutionStammdatenKitaBruennen();
		final InstitutionStammdaten tagesfamilien = TestDataUtil.createInstitutionStammdatenTagesfamilien();
		final InstitutionStammdaten tagesschule = TestDataUtil.createInstitutionStammdatenTagesschuleBern(gesuchsperiode);
		final InstitutionStammdaten ferieninsel = TestDataUtil.createInstitutionStammdatenFerieninselGuarda();
		mandant = TestDataUtil.createDefaultMandant();
		TestdataSetupConfig setupConfig = new TestdataSetupConfig(
			mandant,
			kitaBruennen,
			kitaAaregg,
			tagesfamilien,
			tagesschule,
			ferieninsel,
			gesuchsperiode);
		testdataCreationService.setupTestdata(setupConfig);
	}
}
