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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;

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
	public static void setIsSameAusbezahlteVerguenstigung(@Nonnull Verfuegung verfuegung, @Nullable Verfuegung letzteAusbezahlteVerfuegung) {
		if (letzteAusbezahlteVerfuegung != null) {
			final List<VerfuegungZeitabschnitt> newZeitabschnitte = verfuegung.getZeitabschnitte();
			final List<VerfuegungZeitabschnitt> letztAusbezahlteZeitabschnitte = letzteAusbezahlteVerfuegung.getZeitabschnitte();

			for (VerfuegungZeitabschnitt newZeitabschnitt : newZeitabschnitte) {
				Optional<VerfuegungZeitabschnitt> oldSameZeitabschnittOptional = findZeitabschnittSameGueltigkeit(letztAusbezahlteZeitabschnitte, newZeitabschnitt);
				if (oldSameZeitabschnittOptional.isPresent()) {
					VerfuegungZeitabschnitt oldSameZeitabschnitt = oldSameZeitabschnittOptional.get();
					// Der Vergleich muuss fuer ASIV und Gemeinde separat erfolgen
					setIsSameAusbezahlteVerguenstigung(
						newZeitabschnitt.getBgCalculationInputAsiv(),
						newZeitabschnitt.getBgCalculationResultAsiv(),
						oldSameZeitabschnitt.getBgCalculationResultAsiv());
					if (newZeitabschnitt.isHasGemeindeSpezifischeBerechnung()) {
						Objects.requireNonNull(newZeitabschnitt.getBgCalculationResultGemeinde());
						Objects.requireNonNull(oldSameZeitabschnitt.getBgCalculationResultGemeinde());
						setIsSameAusbezahlteVerguenstigung(
							newZeitabschnitt.getBgCalculationInputGemeinde(),
							newZeitabschnitt.getBgCalculationResultGemeinde(),
							oldSameZeitabschnitt.getBgCalculationResultGemeinde());
					}
				} else { // no Zeitabschnitt with the same Gueltigkeit has been found, so it must be different
					newZeitabschnitt.getBgCalculationInputAsiv().setSameAusbezahlteVerguenstigung(false);
					newZeitabschnitt.getBgCalculationInputGemeinde().setSameAusbezahlteVerguenstigung(false);
				}
			}
		}
	}

	private static void setIsSameAusbezahlteVerguenstigung(
		@Nonnull BGCalculationInput inputNeu,
		@Nonnull BGCalculationResult resultNeu,
		@Nonnull BGCalculationResult resultBisher
	) {
		inputNeu.setSameAusbezahlteVerguenstigung(MathUtil.isSame(resultNeu.getVerguenstigung(), resultBisher.getVerguenstigung()));
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
		@Nonnull List<VerfuegungZeitabschnitt> vorgaengerZeitabschnittList,
		@Nonnull VerfuegungZeitabschnitt newZeitabschnitt) {

		return vorgaengerZeitabschnittList.stream()
			.filter(z -> z.getGueltigkeit().equals(newZeitabschnitt.getGueltigkeit()))
			.filter(z -> z.getVerguenstigung().compareTo(newZeitabschnitt.getVerguenstigung()) == 0)
			.findAny();
	}

	public static void setZahlungsstatus(@Nonnull Verfuegung verfuegung, @Nullable Verfuegung verfuegungOnGesuchForMutation) {
		if (verfuegungOnGesuchForMutation == null) {
			return;
		}

		List<VerfuegungZeitabschnitt> newZeitabschnitte = verfuegung.getZeitabschnitte();
		List<VerfuegungZeitabschnitt> zeitabschnitteGSM = verfuegungOnGesuchForMutation.getZeitabschnitte();

		for (VerfuegungZeitabschnitt newZeitabschnitt : newZeitabschnitte) {
			VerfuegungsZeitabschnittZahlungsstatus oldStatusZeitabschnitt = findStatusOldZeitabschnitt(zeitabschnitteGSM, newZeitabschnitt);
			newZeitabschnitt.setZahlungsstatus(oldStatusZeitabschnitt);
		}
	}

	private static VerfuegungsZeitabschnittZahlungsstatus findStatusOldZeitabschnitt(List<VerfuegungZeitabschnitt> zeitabschnitteGSM, VerfuegungZeitabschnitt newZeitabschnitt) {
		for (VerfuegungZeitabschnitt zeitabschnittGSM : zeitabschnitteGSM) {
			if (zeitabschnittGSM.getGueltigkeit().getOverlap(newZeitabschnitt.getGueltigkeit()).isPresent()) {
				// Wenn ein Vorgaenger vorhanden ist, wird der Status von diesem uebernommen
				return zeitabschnittGSM.getZahlungsstatus();
			}
		}
		return VerfuegungsZeitabschnittZahlungsstatus.NEU;
	}
}
