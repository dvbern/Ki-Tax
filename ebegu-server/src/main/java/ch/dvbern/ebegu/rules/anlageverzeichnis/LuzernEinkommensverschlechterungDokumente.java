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
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;

public class LuzernEinkommensverschlechterungDokumente extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale) {

		final EinkommensverschlechterungInfo einkommensverschlechterungInfo =
			gesuch.extractEinkommensverschlechterungInfo();

		if(einkommensverschlechterungInfo == null) {
			return;
		}

		if (einkommensverschlechterungInfo.getEkvFuerBasisJahrPlus1())  {
			final int basisJahrPlus1 = gesuch.getGesuchsperiode().getGueltigkeit().calculateEndOfPreviousYear().getYear() + 1;
			addDokuemnteEKV(basisJahrPlus1, gesuch, anlageVerzeichnis);
		}

		if (einkommensverschlechterungInfo.getEkvFuerBasisJahrPlus2()) {
			final int basisJahrPlus2 = gesuch.getGesuchsperiode().getGueltigkeit().calculateEndOfPreviousYear().getYear() + 2;
			addDokuemnteEKV(basisJahrPlus2, gesuch, anlageVerzeichnis);
		}
	}

	private void addDokuemnteEKV(int basisJahr, Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		addDokuemnteEKV(basisJahr, 1, anlageVerzeichnis);

		if(gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()) {
			addDokuemnteEKV(basisJahr, 2, anlageVerzeichnis);
		}
	}

	private void addDokuemnteEKV(
		int basisJahr,
		int personNumber,
		Set<DokumentGrund> anlageVerzeichnis) {

		add(getDokument
			(DokumentTyp.NACHWEIS_VERMOEGEN,
			null,
			String.valueOf(basisJahr),
			DokumentGrundPersonType.GESUCHSTELLER,
			personNumber,
			DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG),
			anlageVerzeichnis);

		add(getDokument(
			DokumentTyp.JAHRESLOHNAUSWEISE,
			null,
			String.valueOf(basisJahr),
			DokumentGrundPersonType.GESUCHSTELLER,
			personNumber,
			DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG),
			anlageVerzeichnis);
	}



	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable AbstractFinanzielleSituation dataForDocument) {
		return true;
	}
}
