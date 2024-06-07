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

package ch.dvbern.ebegu.rechner;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.KitaxUebergangsloesungInstitutionOeffnungszeiten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.rechner.kitax.EmptyKitaxBernRechner;
import ch.dvbern.ebegu.rechner.kitax.KitaKitaxBernRechner;
import ch.dvbern.ebegu.rechner.kitax.TageselternKitaxBernRechner;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.KitaxUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Factory, welche für eine Betreuung den richtigen BG-Rechner ermittelt
 */
public final class BGRechnerFactory {

	private BGRechnerFactory() {

	}

	@Nonnull
	public static AbstractRechner getRechner(
		@Nonnull KitaxUebergangsloesungParameter kitaxParameter,
		@Nonnull Locale locale,
		@Nonnull List<RechnerRule> rechnerRulesForGemeinde,
		@Nonnull AbstractPlatz platz,
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt
	) {
		Mandant mandant = platz.getInstitutionStammdaten().getInstitution().getMandant();
		assert mandant != null;
		AbstractRechner
			asivRechner = BGRechnerFactory.getRechner(platz.getBetreuungsangebotTyp(), rechnerRulesForGemeinde, mandant);
		final boolean possibleKitaxRechner = KitaxUtil.isGemeindeWithKitaxUebergangsloesung(platz.extractGemeinde())
			&& platz.getBetreuungsangebotTyp().isJugendamt();
		// Den richtigen Rechner anwerfen
		// Es kann erst jetzt entschieden werden, welcher Rechner zum Einsatz kommt,
		// da fuer Stadt Bern bis zum Zeitpunkt X der alte Ki-Tax Rechner verwendet werden soll.
		AbstractRechner rechnerToUse = null;
		if (possibleKitaxRechner) {
			if (zeitabschnitt.getGueltigkeit().endsBefore(kitaxParameter.getStadtBernAsivStartDate())) {
				rechnerToUse = getPreAsivRechner(kitaxParameter, locale, platz, zeitabschnitt);
			} else if (kitaxParameter.isStadtBernAsivConfiguered()) {
				// Es ist Bern, und der Abschnitt liegt nach dem Stichtag. Falls ASIV schon konfiguriert ist,
				// koennen wir den normalen ASIV Rechner verwenden.
				rechnerToUse = asivRechner;
			} else {
				// Auch in diesem Fall muss zumindest ein leeres Objekt erstellt werden. Evtl. braucht es hier einen
				// NullRechner? Wegen Bemerkungen?
				rechnerToUse = new EmptyKitaxBernRechner(locale, MsgKey.FEBR_INFO_ASIV_NOT_CONFIGUERD);
			}
		} else {
			// Alle anderen rechnen normal mit dem Asiv-Rechner
			rechnerToUse = asivRechner;
		}

		if (rechnerToUse == null) {
			throw new EbeguRuntimeException("getRechner", "could not determine Rechner");
		}

		return rechnerToUse;
	}

	private static AbstractRechner getPreAsivRechner(
		KitaxUebergangsloesungParameter kitaxParameter,
		Locale locale,
		AbstractPlatz platz,
		VerfuegungZeitabschnitt zeitabschnitt) {
		AbstractRechner rechnerToUse;
		if (zeitabschnitt.getBgCalculationInputGemeinde().isBetreuungInGemeinde()) {
			String kitaName = platz.getInstitutionStammdaten().getInstitution().getName();
			KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten = null;
			if (platz.getInstitutionStammdaten().getBetreuungsangebotTyp().isKita()) {
				// Die Oeffnungszeiten sind nur fuer Kitas relevant
				oeffnungszeiten = kitaxParameter.getOeffnungszeiten(kitaName);
			}
			rechnerToUse = BGRechnerFactory.getKitaxRechner(platz, kitaxParameter, oeffnungszeiten, locale);
		} else {
			// Betreuung findet nicht in Gemeinde statt
			rechnerToUse = new EmptyKitaxBernRechner(locale, MsgKey.ZUSATZGUTSCHEIN_NEIN_NICHT_IN_GEMEINDE);
		}
		return rechnerToUse;
	}

	@Nullable
	public static AbstractRechner getRechner(
		@Nonnull BetreuungsangebotTyp betreuungsangebotTyp,
		@Nonnull List<RechnerRule> rechnerRulesForGemeinde,
		@Nonnull Mandant mandant) {
		return new BetreuungsangebotRechnerVisitor(mandant, rechnerRulesForGemeinde)
			.getRechnerForBetreuungsTyp(betreuungsangebotTyp);
	}

	@Nullable
	private static AbstractRechner getKitaxRechner(
		@Nonnull AbstractPlatz betreuung,
		@Nonnull KitaxUebergangsloesungParameter kitaxParameterDTO,
		@Nullable KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten,
		@Nonnull Locale locale
	) {
		BetreuungsangebotTyp betreuungsangebotTyp = betreuung.getBetreuungsangebotTyp();
		if (BetreuungsangebotTyp.KITA == betreuungsangebotTyp) {
			Objects.requireNonNull(oeffnungszeiten);
			return new KitaKitaxBernRechner(kitaxParameterDTO, oeffnungszeiten, locale);
		}
		if (BetreuungsangebotTyp.TAGESFAMILIEN == betreuungsangebotTyp) {
			return new TageselternKitaxBernRechner(kitaxParameterDTO, oeffnungszeiten, locale);
		}
		if (BetreuungsangebotTyp.TAGESSCHULE == betreuungsangebotTyp) {
			// Tagesschulen werden von Anfang an mit dem ASIV-Rechner berechnet
			return new TagesschuleBernRechner(Collections.emptyList());
		}
		// Alle anderen Angebotstypen werden nicht berechnet
		return null;
	}
}
