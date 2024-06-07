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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource.schulamt;

import java.util.Arrays;

import ch.dvbern.ebegu.api.enums.JaxExternalAntragstatus;
import ch.dvbern.ebegu.api.enums.JaxExternalBetreuungsangebotTyp;
import ch.dvbern.ebegu.api.enums.JaxExternalFerienName;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.enums.ModulTagesschuleName;
import ch.dvbern.ebegu.errors.ScolarisException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Dieser Test ueberprueft, dass alle Enums aus kiBon eine Entsprechung in Scolaris haben, bzw.
 * eine Exception werfen, wenn sie nicht erlaubt sind.
 */
public class ScolarisConverterTest {

	private final ScolarisConverter converter = new ScolarisConverter();

	@Test
	public void antragstatusToScolaris() {
		// Es muessen alle Enum-Werte uebereinstimmen
		Arrays.stream(AntragStatus.values()).forEach(
			antragStatus -> {
				JaxExternalAntragstatus converted = converter.antragstatusToScolaris(antragStatus);
				Assert.assertNotNull(converted);
			});
	}

	@Test
	public void betreuungsangebotTypToScolaris() {
		Arrays.stream(BetreuungsangebotTyp.values()).forEach(betreuungsangebotTyp -> {
			if (betreuungsangebotTyp == BetreuungsangebotTyp.TAGESSCHULE
				|| betreuungsangebotTyp == BetreuungsangebotTyp.FERIENINSEL) {
				JaxExternalBetreuungsangebotTyp converted = converter.betreuungsangebotTypToScolaris(betreuungsangebotTyp);
				Assert.assertNotNull(converted);
			} else {
				// In allen anderen Faellen erwarten wir eine Exception
				try {
					converter.betreuungsangebotTypToScolaris(betreuungsangebotTyp);
					Assert.fail("SchulamtException expected");
				} catch (ScolarisException e) {
					// Exception expected
				}
			}
		});
	}

	@Test
	public void betreuungsstatusToScolaris() {
		Arrays.stream(Betreuungsstatus.values()).forEach(betreuungsstatus -> {
			if (Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST == betreuungsstatus
				|| Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST == betreuungsstatus
				|| Betreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT == betreuungsstatus
				|| Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN == betreuungsstatus
				|| Betreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT == betreuungsstatus
				|| Betreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION == betreuungsstatus) {
				converter.betreuungsstatusToScolaris(betreuungsstatus);
			} else {
				// In allen anderen Faellen erwarten wir eine Exception
				try {
					converter.betreuungsstatusToScolaris(betreuungsstatus);
					Assert.fail("SchulamtException expected");
				} catch (ScolarisException e) {
					// Exception expected
				}
			}
		});
	}

	@Test
	public void feriennameToScolaris() {
		// Es muessen alle Enum-Werte uebereinstimmen
		Arrays.stream(Ferienname.values()).forEach(ferienname -> {
			JaxExternalFerienName converted = converter.feriennameToScolaris(ferienname);
			Assert.assertNotNull(converted);
		});
	}

	@Test
	public void modulnameToScolaris() {
		Arrays.stream(ModulTagesschuleName.values()).forEach(modulTagesschuleName -> {
			// Alle ausser DYNAMISCH muessen uebereinstimmen
			if (ModulTagesschuleName.DYNAMISCH == modulTagesschuleName) {
				try {
					converter.modulnameToScolaris(modulTagesschuleName);
					Assert.fail("SchulamtException expected");
				} catch (ScolarisException e) {
					// Exception expected
				}
			} else {
				converter.modulnameToScolaris(modulTagesschuleName);
			}
		});
	}
}
