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

public class SolothurnFinanzielleSituationDokumente extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation>   {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale) {

		if (isGemeinsameSteuererklaerung(gesuch))  {
			addDokumenteGemeinsam(gesuch, anlageVerzeichnis);
		} else {
			addDokumenteGS1(gesuch, anlageVerzeichnis);
			addDokumenteGS2(gesuch, anlageVerzeichnis);
		}
	}

	private void addDokumenteGemeinsam(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();

		if (gesuchsteller1 == null || gesuchsteller1.getFinanzielleSituationContainer() == null) {
			return;
		}

		final FinanzielleSituation finanzielleSituationGS1JA = gesuchsteller1.getFinanzielleSituationContainer().getFinanzielleSituationJA();

		if (finanzielleSituationGS1JA.getSteuerveranlagungErhalten() != null && finanzielleSituationGS1JA.getSteuerveranlagungErhalten()) {
			addDokumentVeranlagung(anlageVerzeichnis, 0, finanzielleSituationGS1JA);
		} else {
			addDokumenteGS1(gesuch, anlageVerzeichnis);
			addDokumenteGS2(gesuch, anlageVerzeichnis);
		}
	}

	private void addDokumenteGS1(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		addDokumenteGesuchsteller(gesuchsteller1, 1, gesuch, anlageVerzeichnis);
	}

	private void addDokumenteGS2(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		final GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();
		addDokumenteGesuchsteller(gesuchsteller2, 2, gesuch, anlageVerzeichnis);
	}

	private void addDokumenteGesuchsteller(GesuchstellerContainer gesuchsteller, int gesuchstellerNumber, Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		if (gesuchsteller == null || gesuchsteller.getFinanzielleSituationContainer() == null) {
			return;
		}

		final FinanzielleSituation finanzielleSituationJA = gesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA();

		if (finanzielleSituationJA.getSteuerveranlagungErhalten() != null && finanzielleSituationJA.getSteuerveranlagungErhalten()) {
			addDokumentVeranlagung(anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
		} else {
			addDokument(DokumentTyp.NACHWEIS_VERMOEGEN, gesuch, anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
			addDokument(DokumentTyp.NACHWEIS_BRUTTOLOHN, gesuch, anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
		}
	}

	private void addDokumentVeranlagung(
		Set<DokumentGrund> anlageVerzeichnis,
		int gesuchstellerNumber,
		FinanzielleSituation finanzielleSituationJA) {

		add(getDokument(
				DokumentTyp.STEUERVERANLAGUNG,
				finanzielleSituationJA,
				null,
				DokumentGrundPersonType.GESUCHSTELLER,
				gesuchstellerNumber,
				DokumentGrundTyp.FINANZIELLESITUATION),
			anlageVerzeichnis);
	}

	private void addDokument(
		DokumentTyp dokumentTyp,
		Gesuch gesuch,
		Set<DokumentGrund> anlageVerzeichnis,
		int gesuchstellerNumber,
		FinanzielleSituation finanzielleSituationJA) {

		final int basisJahr = gesuch.getGesuchsperiode().getGueltigkeit().calculateEndOfPreviousYear().getYear();

		add(getDokument(
				dokumentTyp,
				finanzielleSituationJA,
				String.valueOf(basisJahr),
				DokumentGrundPersonType.GESUCHSTELLER,
				gesuchstellerNumber,
				DokumentGrundTyp.FINANZIELLESITUATION),
			anlageVerzeichnis);
	}

	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable AbstractFinanzielleSituation dataForDocument) {
		return true;
	}
}
