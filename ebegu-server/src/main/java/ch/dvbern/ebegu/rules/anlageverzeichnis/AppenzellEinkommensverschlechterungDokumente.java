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

public class AppenzellEinkommensverschlechterungDokumente extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation>  {

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
			addDokuemnteEKV(gesuch, anlageVerzeichnis);
		}

		if (!einkommensverschlechterungInfo.getEkvFuerBasisJahrPlus1()
				&& einkommensverschlechterungInfo.getEkvFuerBasisJahrPlus2()) {
			addDokuemnteEKV(gesuch, anlageVerzeichnis);
		}
	}

	private void addDokuemnteEKV(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		addDokuemnteEKV(1, anlageVerzeichnis);

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		if(gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode() ||
				(familiensituation != null && familiensituation.isSpezialFallAR())) {
			addDokuemnteEKV(2, anlageVerzeichnis);
		}
	}

	private void addDokuemnteEKV(
		int personNumber,
		Set<DokumentGrund> anlageVerzeichnis) {

		add(getDokument
				(DokumentTyp.NACHWEIS_LOHNAUSWEIS_1,
					null,
					null,
					DokumentGrundPersonType.GESUCHSTELLER,
					personNumber,
					DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG),
			anlageVerzeichnis);
		add(getDokument
				(DokumentTyp.NACHWEIS_LOHNAUSWEIS_2,
					null,
					null,
					DokumentGrundPersonType.GESUCHSTELLER,
					personNumber,
					DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG),
			anlageVerzeichnis);
		add(getDokument
				(DokumentTyp.NACHWEIS_LOHNAUSWEIS_3,
					null,
					null,
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
