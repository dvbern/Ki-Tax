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

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.Gueltigkeit;
import org.apache.commons.lang.StringUtils;

public interface BetreuungsmitteilungPensumMessageFactory {

	static BetreuungsmitteilungPensumMessageFactory empty() {
		return (index, pensum) -> StringUtils.EMPTY;
	}

	static BetreuungsmitteilungPensumMessageFactory combine(
		String trennzeichen,
		BetreuungsmitteilungPensumMessageFactory... factories) {
		return (index, pensum) -> Arrays.stream(factories)
			.map(factory -> factory.messageForPensum(index, pensum))
			.filter(Predicate.not(String::isEmpty))
			.collect(Collectors.joining(trennzeichen));
	}

	default String formatAb(Gueltigkeit pensum) {
		return Constants.DATE_FORMATTER.format(pensum.getGueltigkeit().getGueltigAb());
	}

	default String formatBis(Gueltigkeit pensum) {
		return Constants.DATE_FORMATTER.format(pensum.getGueltigkeit().getGueltigBis());
	}

	String messageForPensum(int index, BetreuungsmitteilungPensum pensum);
}
