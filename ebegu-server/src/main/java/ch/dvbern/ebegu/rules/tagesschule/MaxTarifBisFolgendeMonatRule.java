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

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

public class MaxTarifBisFolgendeMonatRule extends AbstractTagesschuleRule{

	@Override
	protected List<VerfuegungZeitabschnitt> executeVerfuegungZeitabschnittRule(@Nonnull Gesuch gesuch,
		@Nonnull List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts) {
		LocalDate eingangsdatum = gesuch.getRegelStartDatum();
		if(eingangsdatum == null){
			return verfuegungZeitabschnitts;
		}
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittsNeu = new ArrayList<>();
		verfuegungZeitabschnittsNeu.addAll(verfuegungZeitabschnitts);
		for(VerfuegungZeitabschnitt verfuegungZeitabschnitt : verfuegungZeitabschnitts){
			if(verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().isBefore(eingangsdatum)){
				//Wir können es löschen
				verfuegungZeitabschnittsNeu.remove(verfuegungZeitabschnitt);
				if(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().isAfter(eingangsdatum)){
					//aber wenn die Gültig Bis Datum ist nach den Gesuch eingangs dann muss man es anpassen
					//und die Gütlig ab, ab ersten Tag von folgende Monat definieren
					verfuegungZeitabschnitt.getGueltigkeit().setGueltigAb(eingangsdatum.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()));
					verfuegungZeitabschnittsNeu.add(verfuegungZeitabschnitt);
					//und wir erstellen eine Verfuegung Zeitabschnit fuer das Periode Bevor mit Vollkosten
					VerfuegungZeitabschnitt verfuegungZeitabschnittVollKosten = new VerfuegungZeitabschnitt();
					verfuegungZeitabschnittVollKosten.getGueltigkeit().setGueltigAb(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
					verfuegungZeitabschnittVollKosten.getGueltigkeit().setGueltigBis(verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().minusDays(1));
					verfuegungZeitabschnittVollKosten.getBgCalculationInput().setBezahltVollkosten(true);
					verfuegungZeitabschnittsNeu.add(verfuegungZeitabschnittVollKosten);
				}
			}

		}
		return verfuegungZeitabschnittsNeu;
	}
}
