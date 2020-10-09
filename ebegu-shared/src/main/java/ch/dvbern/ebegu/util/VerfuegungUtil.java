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

package ch.dvbern.ebegu.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;

/**
 * Allgemeine Utils fuer Verfuegung
 */
public final class VerfuegungUtil {

	private VerfuegungUtil() {
	}

	/**
	 * Prueft, ob die aktuelle Verfuegung dieseilben Berechnungsrelevanten Daten beinhaltet wie
	 * die zuletzt verfuegte Verfuegung
	 * Wird verwendet fuer die Anzeige "Identische Daten" und folgend "Trotzdem verfuegen / Auf verfuegen verzichten"
	 */
	public static void setIsSameVerfuegungsdaten(@Nonnull Verfuegung verfuegung, @Nullable Verfuegung letzteVerfuegung) {
		if (letzteVerfuegung != null) {
			final List<VerfuegungZeitabschnitt> newZeitabschnitte = verfuegung.getZeitabschnitte();
			final List<VerfuegungZeitabschnitt> letztVerfuegteZeitabschnitte = letzteVerfuegung.getZeitabschnitte();

			for (VerfuegungZeitabschnitt newZeitabschnitt : newZeitabschnitte) {
				// todo imanol Dies sollte auch subzeitabschnitte vergleichen
				Optional<VerfuegungZeitabschnitt> oldSameZeitabschnitt = findZeitabschnittSameGueltigkeit(letztVerfuegteZeitabschnitte, newZeitabschnitt);
				if (oldSameZeitabschnitt.isPresent()) {
					newZeitabschnitt.setSameVerfuegteVerfuegungsrelevanteDatenForAsivAndGemeinde(newZeitabschnitt.isSameBerechnung(oldSameZeitabschnitt.get()));
				} else { // no Zeitabschnitt with the same Gueltigkeit has been found, so it must be different
					newZeitabschnitt.setSameVerfuegteVerfuegungsrelevanteDatenForAsivAndGemeinde(false);
				}
			}
		}
	}

	/**
	 * Prueft, ob die aktuelle Verfuegung denselben auszuzahlenden Betrag berechnet wie die
	 * zuletzt ausbezahlte Verfuegung
	 * Wird verwendet, um zu entscheiden, ob die Frage Ignorieren/Uebernehmen gestellt werden muss
	 */
	public static void setIsSameAusbezahlteVerguenstigung(
		@Nonnull Verfuegung verfuegung,
		@Nullable Verfuegung letzteAusbezahlteVerfuegung,
		@Nullable Verfuegung letzteAusbezahlteVerfuegungMahlzeiten,
		boolean mahlzeitenverguenstigungEnabled
	) {
		if (letzteAusbezahlteVerfuegung != null || letzteAusbezahlteVerfuegungMahlzeiten != null) {
			final List<VerfuegungZeitabschnitt> newZeitabschnitte = verfuegung.getZeitabschnitte();

			final List<VerfuegungZeitabschnitt> letztAusbezahlteZeitabschnitte
				= letzteAusbezahlteVerfuegung != null
				? letzteAusbezahlteVerfuegung.getZeitabschnitte()
				: Collections.emptyList();

			final List<VerfuegungZeitabschnitt> letztAusbezahlteZeitabschnitteMahlzeiten
				= mahlzeitenverguenstigungEnabled && letzteAusbezahlteVerfuegungMahlzeiten != null
				? letzteAusbezahlteVerfuegungMahlzeiten.getZeitabschnitte()
				: Collections.emptyList();

			final ZahlungslaufHelper zahlungslaufHelperGemeindeInstitution = ZahlungslaufHelperFactory.getZahlungslaufHelper(ZahlungslaufTyp.GEMEINDE_INSTITUTION);
			final ZahlungslaufHelper zahlungslaufHelperGemeindeAntragsteller = ZahlungslaufHelperFactory.getZahlungslaufHelper(ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER);

			for (VerfuegungZeitabschnitt newZeitabschnitt : newZeitabschnitte) {
				// "Normale" Auszahlungen
				Optional<VerfuegungZeitabschnitt> oldSameZeitabschnittOptional = findZeitabschnittSameGueltigkeit(letztAusbezahlteZeitabschnitte, newZeitabschnitt);
				zahlungslaufHelperGemeindeInstitution.setIsSameAusbezahlteVerguenstigung(oldSameZeitabschnittOptional, newZeitabschnitt);
				if (mahlzeitenverguenstigungEnabled) {
					// Dasselbe auch fuer Mahlzeiten, jedoch nur, wenn diese fuer die Gemeinde enabled sind
					// Das Feld ist transient, daher brauchen wir es auch nicht, falls die Mahlzeiten spaeter
					// fuer die Gemeinde aktiviert werden
					Optional<VerfuegungZeitabschnitt> oldSameZeitabschnittMahlzeitenOptional = findZeitabschnittSameGueltigkeit(letztAusbezahlteZeitabschnitteMahlzeiten, newZeitabschnitt);
					zahlungslaufHelperGemeindeAntragsteller.setIsSameAusbezahlteVerguenstigung(oldSameZeitabschnittMahlzeitenOptional, newZeitabschnitt);
				} else {
					// Wenn es keine Mahlzeitenverguenstigung gibt, wollen wir die Unterschiede nicht beachten. Wir setzen es einfach auf TRUE.
					newZeitabschnitt.getBgCalculationInputAsiv().setSameAusbezahlteMahlzeiten(true);
					newZeitabschnitt.getBgCalculationInputGemeinde().setSameAusbezahlteMahlzeiten(true);
				}
			}
		}
	}

	@Nonnull
	public static Optional<VerfuegungZeitabschnitt> findZeitabschnittSameGueltigkeit(
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitteGSM,
		@Nonnull VerfuegungZeitabschnitt newZeitabschnitt
	) {
		return zeitabschnitteGSM.stream()
			.filter(z -> z.getGueltigkeit().equals(newZeitabschnitt.getGueltigkeit()))
			.findAny();
	}

	@Nonnull
	public static Optional<VerfuegungZeitabschnitt> findZeitabschnittSameGueltigkeitSameBetrag(
		@Nonnull ZahlungslaufHelper zahlungslaufHelper,
		@Nonnull List<VerfuegungZeitabschnitt> vorgaengerZeitabschnittList,
		@Nonnull VerfuegungZeitabschnitt newZeitabschnitt) {

		return vorgaengerZeitabschnittList.stream()
			.filter(z -> z.getGueltigkeit().equals(newZeitabschnitt.getGueltigkeit()))
			.filter(z -> zahlungslaufHelper.getAuszahlungsbetrag(z).compareTo(zahlungslaufHelper.getAuszahlungsbetrag(newZeitabschnitt)) == 0)
			.findAny();
	}

	public static void setZahlungsstatusForAllZahlungslauftypes(
		@Nonnull Verfuegung verfuegung,
		@Nullable Map<ZahlungslaufTyp, Verfuegung> verfuegungOnGesuchForMutationForAllZahlungslaufTypes
	) {
		if (verfuegungOnGesuchForMutationForAllZahlungslaufTypes == null) {
			return;
		}
		List<VerfuegungZeitabschnitt> newZeitabschnitte = verfuegung.getZeitabschnitte();
		// Der Zahlungsstatus ist pro Zahlungslauf unterschiedlich!
		Arrays.stream(ZahlungslaufTyp.values()).iterator().forEachRemaining(zahlungslaufTyp -> {
			// Die letzte ausbezahlte Verfuegung pro Zahlungslauf betrachten
			final Verfuegung verfuegungOnGesuchForMutation = verfuegungOnGesuchForMutationForAllZahlungslaufTypes.get(zahlungslaufTyp);
			if (verfuegungOnGesuchForMutation != null) {
				List<VerfuegungZeitabschnitt> zeitabschnitteGSM = verfuegungOnGesuchForMutation.getZeitabschnitte();
				for (VerfuegungZeitabschnitt newZeitabschnitt : newZeitabschnitte) {
					final ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);
					final Optional<VerfuegungZeitabschnitt> oldZeitabschnitt = findOldZeitabschnitt(zeitabschnitteGSM, newZeitabschnitt);
					VerfuegungsZeitabschnittZahlungsstatus statusOldZeitabchnitt = VerfuegungsZeitabschnittZahlungsstatus.NEU;
					if (oldZeitabschnitt.isPresent()) {
						statusOldZeitabchnitt = zahlungslaufHelper.getZahlungsstatus(oldZeitabschnitt.get());
					}
					zahlungslaufHelper.setZahlungsstatus(newZeitabschnitt, statusOldZeitabchnitt);

				}
			}
		});
	}

	@Nonnull
	private static Optional<VerfuegungZeitabschnitt> findOldZeitabschnitt(
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitteGSM,
		@Nonnull VerfuegungZeitabschnitt newZeitabschnitt
	) {
		for (VerfuegungZeitabschnitt zeitabschnittGSM : zeitabschnitteGSM) {
			if (zeitabschnittGSM.getGueltigkeit().getOverlap(newZeitabschnitt.getGueltigkeit()).isPresent()) {
				// Wenn ein Vorgaenger vorhanden ist, wird der Status von diesem uebernommen
				return Optional.of(zeitabschnittGSM);
			}
		}
		return Optional.empty();
	}
}
