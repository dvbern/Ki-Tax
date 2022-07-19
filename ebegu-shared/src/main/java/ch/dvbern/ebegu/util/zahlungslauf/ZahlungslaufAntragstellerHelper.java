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
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.ZahlungStatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Helper fuer die Auszahlung der Mahlzeitenverguenstigung der Gemeinde an die Antragsteller
 */
public class ZahlungslaufAntragstellerHelper implements ZahlungslaufHelper {

	private static final long serialVersionUID = -3499105749075247695L;

	@Nonnull
	@Override
	public ZahlungslaufTyp getZahlungslaufTyp() {
		return ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER;
	}

	@Nonnull
	@Override
	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatus(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		return zeitabschnitt.getZahlungsstatusAntragsteller();
	}

	@Override
	public void setZahlungsstatus(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull VerfuegungsZeitabschnittZahlungsstatus status
	) {
		zeitabschnitt.setZahlungsstatusAntragsteller(status);
	}

	@Nonnull
	@Override
	public BigDecimal getAuszahlungsbetrag(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		BigDecimal total = BigDecimal.ZERO;
		if (zeitabschnitt.isAuszahlungAnEltern()) {
			total = MathUtil.DEFAULT.addNullSafe(total, ZahlungslaufGutscheinUtil.getAuszahlungsbetrag(zeitabschnitt));
		}
		if (ZahlungslaufMahlzeitenverguenstigungUtil.isAuszuzahlen(zeitabschnitt)) {
			total = MathUtil.DEFAULT.addNullSafe(total, ZahlungslaufMahlzeitenverguenstigungUtil.getAuszahlungsbetrag(zeitabschnitt));
		}
		return total;
	}

	@Nonnull
	@Override
	public Adresse getAuszahlungsadresseOrDefaultadresse(@Nonnull Zahlung zahlung) {
		final Optional<Zahlungsposition> firstZahlungsposition = zahlung.getZahlungspositionen().stream().findFirst();
		Adresse auszahlungsadresse = zahlung.getAuszahlungsdaten().getAdresseKontoinhaber();
		// Wenn jeweils keine spezifische Adresse gesetzt ist, nehmen wir die Wohnadresse GS1
		if (auszahlungsadresse == null && firstZahlungsposition.isPresent()) {
			auszahlungsadresse = extractWohnadresseGS1(firstZahlungsposition.get());
		}
		// Jetzt muss zwingend eine Adresse vorhanden sein
		Objects.requireNonNull(auszahlungsadresse);
		return auszahlungsadresse;
	}

	@Nullable
	protected static Adresse extractWohnadresseGS1(@Nonnull Zahlungsposition zahlungsposition) {
		final GesuchstellerContainer gesuchsteller1 =
			zahlungsposition
				.getVerfuegungZeitabschnitt()
				.getVerfuegung()
				.getPlatz()
				.extractGesuch()
				.getGesuchsteller1();
		Objects.requireNonNull(gesuchsteller1);
		return gesuchsteller1.getWohnadresseAm(LocalDate.now());
	}

	@Override
	public void setIsSameAusbezahlteVerguenstigung(
		@Nonnull Optional<VerfuegungZeitabschnitt> oldSameZeitabschnittOptional,
		@Nonnull VerfuegungZeitabschnitt newZeitabschnitt
	) {
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
			newZeitabschnitt.getBgCalculationInputAsiv().setSameAusbezahlterBetragAntragsteller(false);
			newZeitabschnitt.getBgCalculationInputGemeinde().setSameAusbezahlterBetragAntragsteller(false);
		}
	}

	private void setIsSameAusbezahlteVerguenstigung(
		@Nonnull BGCalculationInput inputNeu,
		@Nonnull BGCalculationResult resultNeu,
		@Nonnull BGCalculationResult resultBisher
	) {
		boolean result = true;
		if (inputNeu.getParent().isAuszahlungAnEltern()) {
			boolean sameGutschein = ZahlungslaufGutscheinUtil.isSameAusbezahlterBetrag(resultNeu, resultBisher);
			result = sameGutschein;
		}
		if (ZahlungslaufMahlzeitenverguenstigungUtil.isAuszuzahlen(inputNeu.getParent())) {
			boolean sameMahlzeitenverguenstigung = ZahlungslaufMahlzeitenverguenstigungUtil.isSameAusbezahlterBetrag(
				resultNeu, resultBisher);
			result = result && sameMahlzeitenverguenstigung;
		}
		inputNeu.setSameAusbezahlterBetragAntragsteller(result);
	}

	@Override
	public boolean isSamePersistedValues(@Nonnull VerfuegungZeitabschnitt abschnitt, @Nonnull VerfuegungZeitabschnitt otherAbschnitt) {
		boolean result = true;
		if (abschnitt.isAuszahlungAnEltern()) {
			boolean sameGutschein = ZahlungslaufGutscheinUtil.isSamePersistedValues(abschnitt, otherAbschnitt);
			result = sameGutschein;
		}
		if (ZahlungslaufMahlzeitenverguenstigungUtil.isAuszuzahlen(abschnitt)) {
			boolean sameMahlzeiten = ZahlungslaufMahlzeitenverguenstigungUtil.isSamePersistedValues(abschnitt, otherAbschnitt);
			result = result && sameMahlzeiten;
		}
		return result;
	}

	@Override
	public boolean isAuszuzahlen(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		boolean isAuszuzahlenGutschein = zeitabschnitt.isAuszahlungAnEltern();
		boolean isAuszuzahlenMahlzeiten = ZahlungslaufMahlzeitenverguenstigungUtil.isAuszuzahlen(zeitabschnitt);
		return isAuszuzahlenGutschein || isAuszuzahlenMahlzeiten;
	}
}
