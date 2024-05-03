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

package ch.dvbern.ebegu.inbox.handler;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragStatus;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlatzbestaetigungImportForm {

	@Nonnull
	public static Set<ImportForm> importAs(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();

		ImportForm statusBased = basedOnBetreuungsstatus(betreuung);
		ImportForm mitteilungBased = ctx.getLatestOpenBetreuungsmitteilung() == null ? null : ImportForm.MUTATIONS_MITTEILUNG;

		return Stream.of(statusBased, mitteilungBased)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	@Nullable
	public static ImportForm basedOnBetreuungsstatus(Betreuung betreuung) {
		switch (betreuung.getBetreuungsstatus()) {
		case WARTEN:
			return ImportForm.PLATZBESTAETIGUNG;
		case BESTAETIGT:
			Gesuch gesuch = betreuung.extractGesuch();
			return isNotYetFreigegeben(gesuch.getStatus()) ?
				ImportForm.PLATZBESTAETIGUNG :
				ImportForm.MUTATIONS_MITTEILUNG;
		case VERFUEGT:
		case STORNIERT:
		case GESCHLOSSEN_OHNE_VERFUEGUNG:
			return ImportForm.MUTATIONS_MITTEILUNG;
		default:
			return null;
		}
	}

	private boolean isNotYetFreigegeben(@Nonnull AntragStatus antragStatus) {
		return antragStatus == AntragStatus.IN_BEARBEITUNG_GS
			|| antragStatus == AntragStatus.IN_BEARBEITUNG_SOZIALDIENST;
	}

	public enum ImportForm {
		PLATZBESTAETIGUNG,
		MUTATIONS_MITTEILUNG,
		MUTATIONS_MITTEILUNG_LAST_GUELTIG
	}
}
