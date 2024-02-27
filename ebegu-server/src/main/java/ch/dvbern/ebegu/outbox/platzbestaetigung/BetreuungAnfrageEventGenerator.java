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

package ch.dvbern.ebegu.outbox.platzbestaetigung;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.outbox.EventGeneratorServiceBean;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
public class BetreuungAnfrageEventGenerator {
	@Inject
	private EventGeneratorServiceBean eventGeneratorServiceBean;
	/**
	 * This is a job starting every night, there must be no more need for this job after the first execution
	 * but this could be a great help if we want to re-export something, then we just have to change the database
	 * column event_published value and it is re-exported automatically during the following night
	 */
	@Schedule(info = "Migration-aid, pushes Betreuung waiting for Platzbestaetigung and not yet published",
		hour = "4",
		persistent = true)
	public void publishWartendeBetreuung() {
		eventGeneratorServiceBean.exportBetreuungAnfrageEvent();
	}
}
