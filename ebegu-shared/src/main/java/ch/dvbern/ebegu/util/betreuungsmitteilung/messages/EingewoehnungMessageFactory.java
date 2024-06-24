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

import java.text.NumberFormat;
import java.util.Locale;

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Eingewoehnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.commons.lang.StringUtils;

public class EingewoehnungMessageFactory implements BetreuungsmitteilungPensumMessageFactory {

	private final Mandant mandant;
	private final Locale locale;

	public EingewoehnungMessageFactory(Mandant mandant, Locale locale) {
		this.mandant = mandant;
		this.locale = locale;
	}

	@Override
	public String messageForPensum(int index, BetreuungsmitteilungPensum pensum) {
		Eingewoehnung eingewoehnung = pensum.getEingewoehnung();
		if (eingewoehnung == null) {
			return StringUtils.EMPTY;
		}

		return ServerMessageUtil.getMessage(
			"mutationsmeldung_message_eingewoehnung",
			locale,
			mandant,
			formatAb(eingewoehnung),
			formatBis(eingewoehnung),
			NumberFormat.getNumberInstance(locale).format(eingewoehnung.getKosten())
		);
	}
}
