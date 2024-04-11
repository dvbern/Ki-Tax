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

package ch.dvbern.ebegu.util;

import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;

public final class MitteilungUtil {

	private MitteilungUtil() {
		// Should not be initialized
	}

	public static void initializeBetreuungsmitteilung(
		@Nonnull Betreuungsmitteilung betreuungsmitteilung,
		@Nonnull Betreuung betreuung,
		@Nonnull Benutzer currentBenutzer,
		Locale locale
	) {
		PensumUtil.transformBetreuungsPensumContainers(betreuungsmitteilung);
		betreuungsmitteilung.setDossier(betreuung.extractGesuch().getDossier());
		betreuungsmitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
		betreuungsmitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
		betreuungsmitteilung.setSender(currentBenutzer);
		betreuungsmitteilung.setEmpfaenger(betreuung.extractGesuch().getDossier().getFall().getBesitzer());
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		betreuungsmitteilung.setSubject(ServerMessageUtil.getMessage("mutationsmeldung_betreff", locale, currentBenutzer.getMandant()));
	}
}
