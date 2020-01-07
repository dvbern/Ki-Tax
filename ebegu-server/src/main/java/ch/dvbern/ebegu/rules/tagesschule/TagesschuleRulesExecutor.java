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

package ch.dvbern.ebegu.rules.tagesschule;

import java.util.List;
import java.util.Set;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschuleZeitabschnitt;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagesschuleRulesExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(TagesschuleRulesExecutor.class);

	public List<VerfuegungZeitabschnitt> executeAllVerfuegungZeitabschnittRules(Gesuch gesuch,
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts) {

		Reflections reflections = new Reflections("ch.dvbern.ebegu.rules.tagesschule");
		Set<Class<? extends AbstractTagesschuleRule>> tagesschuleRules =
			reflections.getSubTypesOf(AbstractTagesschuleRule.class);
		for (Class<? extends AbstractTagesschuleRule> tagesschuleRule : tagesschuleRules) {
			try {
				AbstractTagesschuleRule abstractTagesschuleRule = tagesschuleRule.newInstance();
				verfuegungZeitabschnitts =
					abstractTagesschuleRule.executeVerfuegungZeitabschnittRule(gesuch,
						verfuegungZeitabschnitts);
			} catch (Exception e) {
				LOG.error("Es gab einen Fehler mit einem Rule bei der Tagesschule Verfuegung Zeitabschnitt: " + e.getMessage());
			}
		}
		return verfuegungZeitabschnitts;
	}

	public List<AnmeldungTagesschuleZeitabschnitt> executeAllTagesschuleZeitabschnittRules(AnmeldungTagesschule anmeldungTagesschule,
		List<AnmeldungTagesschuleZeitabschnitt> tagesschuleZeitabschnitts) {

		Reflections reflections = new Reflections("ch.dvbern.ebegu.rules.tagesschule");
		Set<Class<? extends AbstractTagesschuleRule>> tagesschuleRules =
			reflections.getSubTypesOf(AbstractTagesschuleRule.class);
		for (Class<? extends AbstractTagesschuleRule> tagesschuleRule : tagesschuleRules) {
			try {
				AbstractTagesschuleRule abstractTagesschuleRule = tagesschuleRule.newInstance();
				tagesschuleZeitabschnitts =
					abstractTagesschuleRule.executeAnmeldungTagesschuleZeitabschnittRule(anmeldungTagesschule,
						tagesschuleZeitabschnitts);
			} catch (Exception e) {
				LOG.error("Es gab einen Fehler mit einem Rule bei der Tagesschule Zeitabschnitt: " + e.getMessage());
			}
		}
		return tagesschuleZeitabschnitts;
	}
}
