/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.testfaelle;

import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationSelbstdeklaration;
import ch.dvbern.ebegu.util.MathUtil;

public final class TestFaelleUtil {

	private TestFaelleUtil() {
	}

	public static void fillInFinSitLuZero(FinanzielleSituationContainer finSitContainerLu) {
		finSitContainerLu.getFinanzielleSituationJA().setQuellenbesteuert(true);
		final FinanzielleSituationSelbstdeklaration selbstdeklaration =
				new FinanzielleSituationSelbstdeklaration();

		selbstdeklaration.setEinkunftErwerb(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftVersicherung(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftAusgleichskassen(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftWertschriften(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftUnterhaltsbeitragSteuerpflichtige(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftUnterhaltsbeitragKinder(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftUeberige(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftLiegenschaften(MathUtil.DEFAULT.from(0));

		selbstdeklaration.setAbzugBerufsauslagen(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugUnterhaltsbeitragEhepartner(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugUnterhaltsbeitragKinder(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugRentenleistungen(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugSaeule3A(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugVersicherungspraemien(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugKrankheitsUnfallKosten(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugFreiweiligeZuwendungPartien(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugKinderSchule(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugKinderVorschule(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugKinderAuswaertigerAufenthalt(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugEigenbetreuung(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugFremdbetreuung(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugErwerbsunfaehigePersonen(MathUtil.DEFAULT.from(0));

		selbstdeklaration.setVermoegen(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugSteuerfreierBetragErwachsene(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugSteuerfreierBetragKinder(MathUtil.DEFAULT.from(0));

		finSitContainerLu.getFinanzielleSituationJA().setSelbstdeklaration(selbstdeklaration);

	}

	public static void fillInFinSitSoZero(FinanzielleSituationContainer finSitSo) {
		finSitSo.getFinanzielleSituationJA().setUnterhaltsBeitraege(MathUtil.DEFAULT.from(0));
		finSitSo.getFinanzielleSituationJA().setAbzuegeKinderAusbildung(MathUtil.DEFAULT.from(0));
		finSitSo.getFinanzielleSituationJA().setSteuerbaresVermoegen(MathUtil.DEFAULT.from(0));
	}
}
