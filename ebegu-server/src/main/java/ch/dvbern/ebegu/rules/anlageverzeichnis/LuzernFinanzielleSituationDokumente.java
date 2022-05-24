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

package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;

public class LuzernFinanzielleSituationDokumente extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale) {

		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		final GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();

		if(isVerheiratet(gesuch.extractFamiliensituation())) {
			addDokument(gesuch, gesuchsteller1, 0, anlageVerzeichnis);
		} else {
			addDokument(gesuch, gesuchsteller1, 1, anlageVerzeichnis);
			addDokument(gesuch, gesuchsteller2, 2, anlageVerzeichnis);
		}
	}

	private void addDokument(
		Gesuch gesuch,
		GesuchstellerContainer gesuchsteller,
		int gesuchstellerNumber,
		Set<DokumentGrund> anlageVerzeichnis) {
		if(gesuchsteller == null || gesuchsteller.getFinanzielleSituationContainer() == null) {
			return;
		}

		final int basisJahr = gesuch.getGesuchsperiode().getGueltigkeit().calculateEndOfPreviousYear().getYear();

		add(getDokument(
				DokumentTyp.NACHWEIS_BRUTTOLOHN,
				gesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA(),
				String.valueOf(basisJahr),
				DokumentGrundPersonType.GESUCHSTELLER,
				gesuchstellerNumber,
				DokumentGrundTyp.FINANZIELLESITUATION),
			anlageVerzeichnis);
	}

	protected boolean isVerheiratet(Familiensituation familiensituation) {
		return familiensituation != null &&
			familiensituation.getFamilienstatus() == EnumFamilienstatus.VERHEIRATET;
	}

	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation) {

		if(!(abstractFinanzielleSituation instanceof FinanzielleSituation)) {
			return false;
		}

		FinanzielleSituation finanzielleSituation = (FinanzielleSituation) abstractFinanzielleSituation;
		return finanzielleSituation.getQuellenbesteuert() != null && finanzielleSituation.getQuellenbesteuert();
	}
}
