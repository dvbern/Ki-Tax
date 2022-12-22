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

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Service fuer Gesuch
 */
@Stateless
@Local(SimulationService.class)
public class SimulationServiceBean extends AbstractBaseService implements SimulationService {
	@Inject
	private VerfuegungService verfuegungService;

	@Override
	public String simulateNewVerfuegung(@Nonnull Gesuch gesuch) {
		HashMap<String, BigDecimal> initialBgs = storeInitialBGsAndResetBetreuungsstatus(gesuch);
		var newGesuch = verfuegungService.calculateVerfuegung(gesuch);

		var log = new StringBuilder();

		newGesuch.getKindContainers().forEach(k -> {
			k.getBetreuungen().forEach(b -> {
				if (!simulationAllowed(b)) {
					return;
				}
				var verfuegung = b.getVerfuegungPreview();
				var sumNew = calculateSumBG(verfuegung.getZeitabschnitte());
				var sumOld = initialBgs.get(b.getId());
				if (sumOld.compareTo(sumNew) != 0) {
					logDifference(log, b, sumNew, sumOld);
				}
			});
		});
		return log.toString();
	}

	private static void logDifference(StringBuilder log, Betreuung b, BigDecimal sumNew, BigDecimal sumOld) {
		log
			.append("BG-Nummer: ")
			.append(b.getBGNummer())
			.append("; Gemeinde: ")
			.append(b.getKind().getGesuch().getDossier().getGemeinde().getName())
			.append("; Betreuung mit id ")
			.append(b.getId())
			.append(" nicht identisch. Alt: ")
			.append(sumOld).append(" Neu: ")
			.append(sumNew)
			.append("\n");
	}

	private HashMap<String, BigDecimal> storeInitialBGsAndResetBetreuungsstatus(Gesuch gesuch) {
		var initialBgs = new HashMap<String, BigDecimal>();
		gesuch.getKindContainers().forEach(k -> {
			k.getBetreuungen().forEach(b -> {
				if (!simulationAllowed(b)) {
					return;
				}
				var verfuegung = b.getVerfuegung();
				var sumBG = calculateSumBG(verfuegung.getZeitabschnitte());
				initialBgs.put(b.getId(), sumBG);
				// wir müssen den Betreuungsstatus zurücksetzen, damit die Verfügung erneut durchgeführt werden kann.
				// diese Statusänderung wird nicht gespeichert, weil die Transaction rollbacked wird.
				b.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
			});
		});
		return initialBgs;
	}

	private boolean simulationAllowed(@Nonnull Betreuung betreuung) {
		if (betreuung.getVerfuegung() == null) {
			return false;
		}
		if (betreuung.isAngebotSchulamt()) {
			return false;
		}
		return true;
	}

	private BigDecimal calculateSumBG(@Nonnull List< VerfuegungZeitabschnitt > zeitabschnitte) {
		var sum = BigDecimal.ZERO;
		for (var z : zeitabschnitte) {
			sum = MathUtil.DEFAULT.add(sum, z.getVerguenstigung());
		}
		return sum;
	}


}


