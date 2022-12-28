/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.nesko.utils;

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageContext;

public final class KibonAnfrageUtil {

	private KibonAnfrageUtil(){
	}
	/**
	 * Bestimmen ob der Veranlagung Event betrifft der GS1 oder GS2 und das Context entsprechend initialisieren
	 *
	 * @return KibonAnfrageContext
	 */
	public static KibonAnfrageContext initKibonAnfrageContext(@Nonnull Gesuch gesuch, int zpvNummer) {
		KibonAnfrageContext kibonAnfrageContext = null;
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		if (gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.getSteuerdatenResponse() != null && gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.getSteuerdatenResponse()
			.getZpvNrDossiertraeger() != null) {
			if (gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenResponse()
				.getZpvNrDossiertraeger()
				.equals(zpvNummer)) {
				kibonAnfrageContext = new KibonAnfrageContext(
					gesuch,
					gesuch.getGesuchsteller1(),
					gesuch.getGesuchsteller1().getFinanzielleSituationContainer(),
					gesuch.getId());
			}
		} else {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			Objects.requireNonNull(gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer());
			if (gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenResponse() != null && gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenResponse()
				.getZpvNrDossiertraeger() != null && gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenResponse()
				.getZpvNrDossiertraeger()
				.equals(zpvNummer)) {
				kibonAnfrageContext = new KibonAnfrageContext(
					gesuch,
					gesuch.getGesuchsteller2(),
					gesuch.getGesuchsteller2().getFinanzielleSituationContainer(),
					gesuch.getId());

			}
		}
		return kibonAnfrageContext;
	}
}
