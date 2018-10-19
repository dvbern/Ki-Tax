/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.vorlagen.begleitschreiben;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.vorlagen.AufzaehlungPrint;
import ch.dvbern.ebegu.vorlagen.AufzaehlungPrintImpl;
import ch.dvbern.ebegu.vorlagen.BriefPrintImpl;

/**
 * Transferobjekt
 */
public class BegleitschreibenPrintImpl extends BriefPrintImpl implements BegleitschreibenPrint {

	private final List<AufzaehlungPrint> beilagen = new ArrayList<>();

	public BegleitschreibenPrintImpl(Gesuch gesuch) {

		super(gesuch);

		Set<Betreuung> betreuungen = new TreeSet<>();

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			betreuungen.addAll(kindContainer.getBetreuungen());
		}

		beilagen.addAll(betreuungen.stream()
			.filter(betreuung -> betreuung.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()
				&& betreuung.getBetreuungsstatus() != Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG)
			.map(betreuung -> new AufzaehlungPrintImpl(ServerMessageUtil.getMessage("BegleitschreibenPrintImpl_VERFUEGUNG") + ' ' + betreuung.getBGNummer()))
			.collect(Collectors.toList()));
	}

	@Override
	public List<AufzaehlungPrint> getBeilagen() {
		return beilagen;
	}

	@Override
	public boolean isHasFSDokument() {
		return getGesuch().isHasFSDokument();
	}

	@Override
	public boolean isHasBeilagen() {
		return isHasFSDokument() || !beilagen.isEmpty();
	}
}
