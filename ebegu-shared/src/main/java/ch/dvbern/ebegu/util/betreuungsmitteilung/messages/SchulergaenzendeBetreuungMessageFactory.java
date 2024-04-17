/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util.betreuungsmitteilung.messages;

import java.util.Locale;

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.ServerMessageUtil;

public class SchulergaenzendeBetreuungMessageFactory implements BetreuungsmitteilungPensumMessageFactory {

	private final Mandant mandant;
	private final Locale locale;

	public SchulergaenzendeBetreuungMessageFactory(Mandant mandant, Locale locale) {
		this.mandant = mandant;
		this.locale = locale;
	}

	@Override
	public String messageForPensum(int index, BetreuungsmitteilungPensum pensum) {
		if (Boolean.TRUE.equals(pensum.getBetreuungInFerienzeit())) {
			return ServerMessageUtil.getMessage(
				"mutationsmeldung_message_betreuung_in_ferienzeit",
				locale,
				mandant);
		}

		return ServerMessageUtil.getMessage(
			"mutationsmeldung_message_betreuung_nicht_in_ferienzeit",
			locale,
			mandant);
	}

	@Override
	public String getTrennenZeichnen() {
		return " ";
	}
}
