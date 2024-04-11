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
import java.util.Locale;

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;

public class MahlzeitenVerguenstigungMessageFactory implements BetreuungsmitteilungPensumMessageFactory {

	private final Mandant mandant;
	private final Locale locale;
	private final BigDecimal pensumMultiplier;
	private final String messageKey;

	public MahlzeitenVerguenstigungMessageFactory(
		Mandant mandant,
		Locale locale,
		BetreuungspensumAnzeigeTyp anzeigeTyp,
		BigDecimal pensumMultiplier
	) {
		this.mandant = mandant;
		this.locale = locale;
		this.pensumMultiplier = pensumMultiplier;
		this.messageKey = anzeigeTyp == BetreuungspensumAnzeigeTyp.NUR_STUNDEN ?
			"mutationsmeldung_message_mahlzeitverguenstigung_mit_tarif_stunden" :
			"mutationsmeldung_message_mahlzeitverguenstigung_mit_tarif";
	}

	@Override
	public String messageForPensum(int index, BetreuungsmitteilungPensum pensum) {
		return ServerMessageUtil.getMessage(
			messageKey,
			locale,
			mandant,
			index,
			formatAb(pensum),
			formatBis(pensum),
			MathUtil.DEFAULT.multiply(pensum.getPensum(), pensumMultiplier),
			pensum.getMonatlicheBetreuungskosten(),
			pensum.getMonatlicheHauptmahlzeiten(),
			pensum.getMonatlicheNebenmahlzeiten(),
			pensum.getTarifProHauptmahlzeit(),
			pensum.getTarifProNebenmahlzeit());
	}
}
