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

package ch.dvbern.ebegu.util.zahlungslauf;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Helper fuer die "normalen" BG Auszahlungen der Gemeinde an die Institutionen.
 */
public class ZahlungslaufInstitutionenHelper implements ZahlungslaufHelper {

	private static final long serialVersionUID = -4297840799469141643L;

	@Nonnull
	@Override
	public ZahlungslaufTyp getZahlungslaufTyp() {
		return ZahlungslaufTyp.GEMEINDE_INSTITUTION;
	}

	@Nonnull
	@Override
	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatus(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		return zeitabschnitt.getZahlungsstatus();
	}

	@Override
	public void setZahlungsstatus(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull VerfuegungsZeitabschnittZahlungsstatus status
	) {
		zeitabschnitt.setZahlungsstatus(status);
	}

	@Nonnull
	@Override
	public BigDecimal getAuszahlungsbetrag(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		return zeitabschnitt.getVerguenstigung();
	}

	@Nonnull
	@Override
	public Adresse getAuszahlungsadresseOrDefaultadresse(@Nonnull Zahlung zahlung) {
		// In erster Prio nehmen wir die speziell definierte Zahlungsadresse
		Adresse auszahlungsadresse = zahlung.getAuszahlungsdaten().getAdresseKontoinhaber();
		if (auszahlungsadresse == null) {
			// Falls keine spezifische Adresse definiert ist, nehmen wir die Adresse der Institution
			final Optional<Zahlungsposition> firstZahlungsposition = zahlung.getZahlungspositionen().stream().findFirst();
			if (firstZahlungsposition.isPresent()) {
				final AbstractPlatz platz =
					firstZahlungsposition.get().getVerfuegungZeitabschnitt().getVerfuegung().getPlatz();
				auszahlungsadresse = platz.getInstitutionStammdaten().getAdresse();
			}
		}
		// Jetzt muss zwingend eine Adresse vorhanden sein
		Objects.requireNonNull(auszahlungsadresse);
		return auszahlungsadresse;
	}

	@Override
	public void setIsSameAusbezahlteVerguenstigung(@Nonnull Optional<VerfuegungZeitabschnitt> oldSameZeitabschnittOptional,
		@Nonnull VerfuegungZeitabschnitt newZeitabschnitt) {
		// "Normale" Auszahlungen
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
			newZeitabschnitt.getBgCalculationInputAsiv().setSameAusbezahlterBetragInstitution(false);
			newZeitabschnitt.getBgCalculationInputGemeinde().setSameAusbezahlterBetragInstitution(false);
		}
	}

	private void setIsSameAusbezahlteVerguenstigung(
		@Nonnull BGCalculationInput inputNeu,
		@Nonnull BGCalculationResult resultNeu,
		@Nonnull BGCalculationResult resultBisher
	) {
		inputNeu.setSameAusbezahlterBetragInstitution(MathUtil.isSame(resultNeu.getVerguenstigung(), resultBisher.getVerguenstigung()));
	}

	@Override
	public boolean isSamePersistedValues(@Nonnull VerfuegungZeitabschnitt abschnitt, @Nonnull VerfuegungZeitabschnitt otherAbschnitt) {
		// Im Fall der Institutionszahlungen koennen wir die "normale" Berechnung von isSamePersistedValues verwenden:
		return abschnitt.isSamePersistedValues(otherAbschnitt);
	}

	@Override
	public boolean isAuszuzahlen(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull Gesuch letztesGueltigesGesuch
	) {
		return !zeitabschnitt.isAuszahlungAnEltern();
	}
}
