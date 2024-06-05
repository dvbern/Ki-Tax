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

package ch.dvbern.ebegu.betreuung;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.services.EinstellungService;

import static ch.dvbern.ebegu.enums.EinstellungKey.ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.SCHULERGAENZENDE_BETREUUNGEN;
import static java.util.Objects.requireNonNull;

@Stateless
public class BetreuungEinstellungServiceBean implements BetreuungEinstellungenService {

	@Inject
	private EinstellungService einstellungService;

	@Override
	public BetreuungEinstellungen getEinstellungen(Betreuung betreuung) {
		return BetreuungEinstellungen.builder()
			.betreuteTageEnabled(isBetreuteTageEnabled(betreuung))
			.schulergaenzendeBetreuungEnabled(showSchulergaenzenschulergaenzendeBetreuungEnabled(betreuung))
			.mahlzeitenVerguenstigungEnabled(einstellungService.isEnabled(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED, betreuung))
			.build();
	}

	private boolean isBetreuteTageEnabled(Betreuung betreuung) {
		return betreuung.isAngebotTagesfamilien()
			&& einstellungService.isEnabled(ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT, betreuung);
	}

	private boolean showSchulergaenzenschulergaenzendeBetreuungEnabled(Betreuung betreuung) {
		return requireNonNull(betreuung.getKind().getKindJA().getEinschulungTyp()).isEingeschult()
			&& einstellungService.isEnabled(SCHULERGAENZENDE_BETREUUNGEN, betreuung);
	}
}
