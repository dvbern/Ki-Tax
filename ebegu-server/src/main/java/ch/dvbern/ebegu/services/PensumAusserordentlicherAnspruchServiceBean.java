/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.PensumAusserordentlicherAnspruch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer pensumAusserordentlicherAnspruch
 */
@Stateless
@Local(PensumAusserordentlicherAnspruchService.class)
public class PensumAusserordentlicherAnspruchServiceBean extends AbstractBaseService implements PensumAusserordentlicherAnspruchService {

	@Inject
	private Persistence persistence;

	@Inject
	private VerfuegungService verfuegungService;

	@Override
	@Nonnull
	public PensumAusserordentlicherAnspruch savePensumAusserordentlicherAnspruch(@Nonnull PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch) {
		Objects.requireNonNull(pensumAusserordentlicherAnspruch);
		return persistence.merge(pensumAusserordentlicherAnspruch);
	}

	@Override
	@Nonnull
	public Optional<PensumAusserordentlicherAnspruch> findPensumAusserordentlicherAnspruch(@Nonnull String pensumAusserordentlicherAnspruchId) {
		Objects.requireNonNull(pensumAusserordentlicherAnspruchId, "id muss gesetzt sein");
		PensumAusserordentlicherAnspruch a = persistence.find(PensumAusserordentlicherAnspruch.class, pensumAusserordentlicherAnspruchId);
		return Optional.ofNullable(a);
	}

	@Override
	public boolean isAusserordentlicherAnspruchPossible(@Nonnull Gesuch gesuch) {
		// Bei mind. 1 Kind ist KEINE Fachstelle definiert
		boolean result = hasAtLeastOneKindWithoutFachstelle(gesuch);
		// Das minimale Erwerbspensum wurde unterschritten
		result = result && isMinimalesErwerbspensumUnterschritten(gesuch);
		return result;
	}

	private boolean hasAtLeastOneKindWithoutFachstelle(@Nonnull Gesuch gesuch) {
		return gesuch.extractAllKinderWithAngebot().stream()
			.anyMatch(kind -> kind.getPensumFachstelle() == null);
	}

	private boolean isMinimalesErwerbspensumUnterschritten(@Nonnull Gesuch gesuch) {
		// Wenn es nur Schulamt-Plaetze hat, spielt das Erwerbspensum keine Rolle
		if (gesuch.hasOnlyBetreuungenOfSchulamt()) {
			return false;
		}
		Gesuch gesuchWithCalcVerfuegung = verfuegungService.calculateVerfuegung(gesuch);
		if (gesuchWithCalcVerfuegung.extractAllBetreuungen().isEmpty()) {
			return false;
		}
		for (Betreuung betreuung : gesuchWithCalcVerfuegung.extractAllBetreuungen()) {
			Verfuegung verfuegung;
			// im status GESCHLOSSEN_OHNE_VERFUEGUNG gibt es keine Verfügung oder VerfügungPreview. Hier muss der
			// Vorgänger genommen werden.
			if (betreuung.getBetreuungsstatus() == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG) {
				verfuegung = betreuung.getVorgaengerVerfuegung();
			} else {
				verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
			}
			if (verfuegung != null) {
				Objects.requireNonNull(verfuegung);
				// Ermitteln, ob die Minimales-Erwerbspensum-Regel zugeschlagen hat
				for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : verfuegung.getZeitabschnitte()) {
					if (!verfuegungZeitabschnitt.isMinimalesEwpUnterschritten()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
