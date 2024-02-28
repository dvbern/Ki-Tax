/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.gemeindekennzahlen;

import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.outbox.EventGeneratorServiceBean;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
public class GemeindeKennzahlenEventGenerator {
	@Inject
	private EventGeneratorServiceBean eventGeneratorServiceBean;
	@Schedule(info = "Migration-aid, pushes already existing Gemeinden to outbox",
		hour = "5",
		minute = "15",
		persistent = true)
	public void publishExistingGemeinden() {
		eventGeneratorServiceBean.exportGemeindeKennzahlenEvent();
	}
}
