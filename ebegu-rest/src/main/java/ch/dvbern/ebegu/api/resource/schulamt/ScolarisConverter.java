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

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.api.dtos.JaxExternalModul;
import ch.dvbern.ebegu.api.enums.JaxExternalAntragstatus;
import ch.dvbern.ebegu.api.enums.JaxExternalBetreuungsangebotTyp;
import ch.dvbern.ebegu.api.enums.JaxExternalBetreuungsstatus;
import ch.dvbern.ebegu.api.enums.JaxExternalFerienName;
import ch.dvbern.ebegu.api.enums.JaxExternalModulName;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.enums.ModulTagesschuleName;
import ch.dvbern.ebegu.errors.SchulamtException;

@Dependent
public class ScolarisConverter {

	@Nonnull
	public JaxExternalAntragstatus antragstatusToScolaris(@Nonnull AntragStatus status) {
		// Es sind in Scolaris alle Status vorhanden, ausser KEIN_KONTINGENT. Dieses behandeln wir
		// wie GEPRUEFT
		if (AntragStatus.KEIN_KONTINGENT == status) {
			return JaxExternalAntragstatus.GEPRUEFT;
		}
		return JaxExternalAntragstatus.valueOf(status.name());
	}

	@Nonnull
	public JaxExternalBetreuungsangebotTyp betreuungsangebotTypToScolaris(@Nonnull BetreuungsangebotTyp typ) {
		// In Scolaris werden nur TAGESSCHULE und FERIENINSEL behandelt
		if (BetreuungsangebotTyp.TAGESSCHULE == typ) {
			return JaxExternalBetreuungsangebotTyp.TAGESSCHULE;
		}
		if (BetreuungsangebotTyp.FERIENINSEL == typ) {
			return JaxExternalBetreuungsangebotTyp.FERIENINSEL;
		}
		throw new SchulamtException("Could not convert BetreuungsangebotTyp " + typ);
	}

	@Nonnull
	public JaxExternalBetreuungsstatus betreuungsstatusToScolaris(@Nonnull Betreuungsstatus status) {
		switch (status) {
		case SCHULAMT_ANMELDUNG_ERFASST:
			return JaxExternalBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST;
		case SCHULAMT_ANMELDUNG_AUSGELOEST:
			return JaxExternalBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST;
		case SCHULAMT_MODULE_AKZEPTIERT: // Neuer Status, aus Sicht Scolaris wie UEBERNOMMEN
		case SCHULAMT_ANMELDUNG_UEBERNOMMEN:
			return JaxExternalBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN;
		case SCHULAMT_ANMELDUNG_ABGELEHNT:
			return JaxExternalBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT;
		case SCHULAMT_FALSCHE_INSTITUTION:
			return JaxExternalBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION;
		default:
			throw new SchulamtException("Could not convert Betreuungsstatus " + status);
		}
	}

	@Nonnull
	public JaxExternalFerienName feriennameToScolaris(@Nonnull Ferienname ferienname) {
		return JaxExternalFerienName.valueOf(ferienname.name());
	}

	@Nonnull
	public JaxExternalModulName modulnameToScolaris(@Nonnull ModulTagesschuleName modulTagesschuleName) {
		if (ModulTagesschuleName.DYNAMISCH == modulTagesschuleName) {
			throw new SchulamtException("Could not convert ModulTagesschuleName " + modulTagesschuleName);
		}
		return JaxExternalModulName.valueOf(modulTagesschuleName.name());
	}

	@Nonnull
	public JaxExternalModul modulToScolaris(@Nonnull BelegungTagesschuleModul tagesschuleModul) {
		ModulTagesschuleName modulTagesschuleName = tagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getModulTagesschuleName();
		JaxExternalModulName jaxModulname = modulnameToScolaris(modulTagesschuleName);
		return new JaxExternalModul(
			tagesschuleModul.getModulTagesschule().getWochentag(),
			jaxModulname);
	}
}
