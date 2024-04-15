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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;

public class DefaultMessageFactory implements BetreuungsmitteilungPensumMessageFactory {

	private final Mandant mandant;
	private final Locale locale;
	private final BigDecimal pensumMultiplier;
	private final String messageKey;

	public DefaultMessageFactory(
		Mandant mandant,
		Locale locale,
		BetreuungspensumAnzeigeTyp anzeigeTyp,
		BigDecimal pensumMultiplier
	) {
		this.mandant = mandant;
		this.locale = locale;
		this.pensumMultiplier = pensumMultiplier;
		this.messageKey = anzeigeTyp == BetreuungspensumAnzeigeTyp.NUR_STUNDEN ?
			"mutationsmeldung_message_stunden" :
			"mutationsmeldung_message";
	}

	@Override
	public String messageForPensum(int index, BetreuungsmitteilungPensum pensum) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

		return ServerMessageUtil.getMessage(
			messageKey,
			locale,
			mandant,
			index,
			formatAb(pensum),
			formatBis(pensum),
			numberFormat.format(MathUtil.DEFAULT.multiply(pensum.getPensum(), pensumMultiplier)),
			numberFormat.format(pensum.getMonatlicheBetreuungskosten())
		);
	}
}
