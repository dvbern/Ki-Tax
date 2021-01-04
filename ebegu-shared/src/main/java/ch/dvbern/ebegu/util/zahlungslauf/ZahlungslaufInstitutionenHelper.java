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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.ZahlungStatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
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
	public Zahlung findZahlungForEmpfaenger(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution
	) {
		Objects.requireNonNull(zeitabschnitt.getVerfuegung().getBetreuung());
		InstitutionStammdaten institution = zeitabschnitt.getVerfuegung().getBetreuung().getInstitutionStammdaten();
		if (zahlungProInstitution.containsKey(institution.getId())) {
			return zahlungProInstitution.get(institution.getId());
		}
		// Es gibt noch keine Zahlung fuer diesen Empfaenger, wir erstellen eine Neue
		Zahlung zahlung = createZahlung(institution, zahlungsauftrag);
		zahlungProInstitution.put(institution.getId(), zahlung);
		return zahlung;
	}

	@Nonnull
	private Zahlung createZahlung(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull Zahlungsauftrag zahlungsauftrag
	) {
		Zahlung zahlung = new Zahlung();
		zahlung.setStatus(ZahlungStatus.ENTWURF);
		final InstitutionStammdatenBetreuungsgutscheine stammdatenBG =
			institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine();
		Objects.requireNonNull(stammdatenBG, "Die Stammdaten muessen zu diesem Zeitpunkt definiert sein");
		final Auszahlungsdaten auszahlungsdaten = stammdatenBG.getAuszahlungsdaten();
		// Wenn die Zahlungsinformationen nicht komplett ausgefuellt sind, fahren wir hier nicht weiter.
		if (auszahlungsdaten == null || !auszahlungsdaten.isZahlungsinformationValid()) {
			throw new EbeguRuntimeException(KibonLogLevel.INFO,
				"createZahlung",
				ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_INSTITUTION_INCOMPLETE,
				institutionStammdaten.getInstitution().getName());
		}

		Objects.requireNonNull(auszahlungsdaten);
		zahlung.setAuszahlungsdaten(auszahlungsdaten);
		zahlung.setEmpfaengerId(institutionStammdaten.getInstitution().getId());
		zahlung.setEmpfaengerName(institutionStammdaten.getInstitution().getName());
		zahlung.setBetreuungsangebotTyp(institutionStammdaten.getBetreuungsangebotTyp());
		if (institutionStammdaten.getInstitution().getTraegerschaft() != null) {
			zahlung.setTraegerschaftName(institutionStammdaten.getInstitution().getTraegerschaft().getName());
		}
		zahlung.setZahlungsauftrag(zahlungsauftrag);
		zahlungsauftrag.getZahlungen().add(zahlung);
		return zahlung;
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
			newZeitabschnitt.getBgCalculationInputAsiv().setSameAusbezahlteVerguenstigung(false);
			newZeitabschnitt.getBgCalculationInputGemeinde().setSameAusbezahlteVerguenstigung(false);
		}
	}

	private void setIsSameAusbezahlteVerguenstigung(
		@Nonnull BGCalculationInput inputNeu,
		@Nonnull BGCalculationResult resultNeu,
		@Nonnull BGCalculationResult resultBisher
	) {
		inputNeu.setSameAusbezahlteVerguenstigung(MathUtil.isSame(resultNeu.getVerguenstigung(), resultBisher.getVerguenstigung()));
	}

	@Override
	public boolean isSamePersistedValues(@Nonnull VerfuegungZeitabschnitt abschnitt, @Nonnull VerfuegungZeitabschnitt otherAbschnitt) {
		// Im Fall der Institutionszahlungen koennen wir die "normale" Berechnung von isSamePersistedValues verwenden:
		return abschnitt.isSamePersistedValues(otherAbschnitt);
	}

	@Override
	public boolean isAuszuzahlen(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		// An Institutionen wird immer ausbezahlt
		return true;
	}
}
