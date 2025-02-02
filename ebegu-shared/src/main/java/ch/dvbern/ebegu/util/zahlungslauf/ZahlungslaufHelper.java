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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;

/**
 * Interface fuer Zahlungshelper. In diesen wird alles ausgelagert, was pro ZahlungslaufTyp
 * unterschiedlich ist, z.B. je nach Empfaenger unterschiedliche Zahlungsadresse
 */
public interface ZahlungslaufHelper extends Serializable {

	/**
	 * Gibt den ZahlungslaufTyp zurueck, welcher mit diesem Helper behandelt wird
	 */
	@Nonnull
	ZahlungslaufTyp getZahlungslaufTyp();

	/**
	 * Gibt den Zahlungsstatus dieses Zeitabschnitts zurueck.
	 */
	@Nonnull
	VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatus(@Nonnull VerfuegungZeitabschnitt zeitabschnitt);

	/**
	 * Setzt den Zahlungsstatus dieses Zeitabschnitts auf den uebergebenen Wert
	 */
	void setZahlungsstatus(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull VerfuegungsZeitabschnittZahlungsstatus status);

	/**
	 * Gibt den auszuzahlenden Betrag zurueck.
	 */
	@Nonnull
	BigDecimal getAuszahlungsbetrag(@Nonnull VerfuegungZeitabschnitt zeitabschnitt);

	/**
	 * Gibt die Auszahlungsadresse zurueck. Falls eine spezifische Auszahlungsadresse gesetzt ist
	 * wird diese zurueckgegeben, sonst je nach Empfaenger ein sinnvoller Default (z.B. Wohnadresse)
	 */
	@Nonnull
	Adresse getAuszahlungsadresseOrDefaultadresse(@Nonnull Zahlung zahlung);

	/**
	 * Setzt das Flag, ob der auszuzahlende Betrag fuer diesen Zahlunglauf geaendert hat.
	 * Wird benoetigt, um die Frage nach dem Ignorieren korrekt stellen zu koennen und um
	 * keine 'leeren' Auszahlungen zu machen.
	 */
	void setIsSameAusbezahlteVerguenstigung(
		@Nonnull Optional<VerfuegungZeitabschnitt> oldSameZeitabschnittOptional,
		@Nonnull VerfuegungZeitabschnitt newZeitabschnitt);

	/**
	 * Gibt zurueck, ob die gespeicherten Daten, welche fuer diesen Zahlungslauftyp relevant sind,
	 * identisch sind
	 */
	boolean isSamePersistedValues(@Nonnull VerfuegungZeitabschnitt abschnitt, @Nonnull VerfuegungZeitabschnitt otherAbschnitt);

	/**
	 * Gibt zurueck, ob fuer diesen Zeitabschnitt grundsaetzlich eine Auszahlung in Frage kommt.
	 */
	boolean isAuszuzahlen(@Nonnull VerfuegungZeitabschnitt zeitabschnitt);
}
