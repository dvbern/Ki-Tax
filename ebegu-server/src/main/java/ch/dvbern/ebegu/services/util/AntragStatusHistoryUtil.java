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

package ch.dvbern.ebegu.services.util;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.EnumUtil;

import static ch.dvbern.ebegu.enums.AntragStatus.BESCHWERDE_HAENGIG;
import static ch.dvbern.ebegu.enums.AntragStatus.GEPRUEFT_STV;
import static ch.dvbern.ebegu.enums.AntragStatus.IN_BEARBEITUNG_STV;
import static ch.dvbern.ebegu.enums.AntragStatus.PRUEFUNG_STV;

/**
 * Util-Methoden fuer Status-Uebergaenge, ausgelagert zur besseren Testbarkeit.
 */
public final class AntragStatusHistoryUtil {

	private AntragStatusHistoryUtil() {
	}

	@Nonnull
	public static AntragStatusHistory findLastStatusChangeBeforeBeschwerde(@Nonnull List<AntragStatusHistory> allStatusChanges, @Nonnull String logInfo) {
		// Bei Beschwerde kann es nicht sein, dass ein anderer Status "dazwischenfunkt", da das Gesuch waehrend der Beschwerde
		// gesperrt ist
		if (allStatusChanges.size() < 2 || BESCHWERDE_HAENGIG != allStatusChanges.get(0).getStatus()) {
			throw new EbeguRuntimeException("findLastStatusChangeBeforeBeschwerde", ErrorCodeEnum.ERROR_NOT_FROM_STATUS_BESCHWERDE, logInfo);
		}
		return allStatusChanges.get(1);
	}

	@Nonnull
	public static AntragStatusHistory findLastStatusChangeBeforePruefungSTV(@Nonnull List<AntragStatusHistory> allStatusChanges, @Nonnull String logInfo) {
		// Es muss mindestens 2 StatusHistory haben, und der letzte muss GEPRUEFT_STV oder PRUEFUNG_STV sein
		if (allStatusChanges.size() < 2 || EnumUtil.isNoneOf(allStatusChanges.get(0).getStatus(), GEPRUEFT_STV, PRUEFUNG_STV)) {
			throw new EbeguRuntimeException("findLastStatusChangeBeforeBeschwerde", ErrorCodeEnum.ERROR_NOT_FROM_STATUS_BESCHWERDE, logInfo);
		}
		// Wir wollen den Status auf den letzten Status VOR dem Wechsel zu PRUEFUNG_STV setzen.
		// ABER: Es kann sein, dass zwischendurch weitere Statuswechsel waren (z.b. Beschwerde ein und aus).
		// Wir muessen daher zurueckgehen bis zum Status vor dem ERSTEN PRUEFUNG_STV nach dem letzten Ende-Pruefung STV.
		// Oder dem ersten ueberhaupt, falls es nur eines hatte.
		AntragStatusHistory tempResult = null;
		boolean changeToPruefungSTVFound = false;
		for (final AntragStatusHistory statusChange : allStatusChanges) { //they come DESC ordered from the DB
			if (changeToPruefungSTVFound) {
				// Wir merken uns den Status, der vor der PruefungSTV war
				tempResult = statusChange;
				// Und setzen das Flag zurueck, damit wir weiterfahren koennen
				changeToPruefungSTVFound = false;
			}
			if (statusChange.getStatus() == PRUEFUNG_STV) {
				changeToPruefungSTVFound = true;
			}
			// Zurueck von STV: IN_BEARBEITUNG_STV (falls zurueckgeholt), GEPRUEFT_STV (falls abgeschlossen STV)
			// Sobald wir eines davon gefunden haben, wissen wir, dass das zuletzt im tempResult gespeicherte das erste nach dem
			// letzten STV-Wechsel war und koennen dieses zurueckgeben.
			if (tempResult != null && EnumUtil.isOneOf(statusChange.getStatus(), IN_BEARBEITUNG_STV, GEPRUEFT_STV)) {
				return tempResult;
			}
		}
		if (tempResult != null) {
			return tempResult;
		}
		throw new EbeguRuntimeException("findLastStatusChangeBeforePruefungSTV", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, logInfo);
	}
}
