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

		if(isGemeinsameSteuererklaerung(gesuch)) {
			if (isVeranlagt(gesuch)) {
				getAllDokumenteGemeinsam(gesuch, anlageVerzeichnis);
			} else {
				getAllDokumenteVerheirtatetForBothGS(gesuch, anlageVerzeichnis);
			}
		} else {
			getAllDokumenteGS1(gesuch, anlageVerzeichnis);
			getAllDokumenteGS2(gesuch, anlageVerzeichnis);
		}
	}

	private void getAllDokumenteVerheirtatetForBothGS(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		assert gesuch.getGesuchsteller1() != null;
		assert gesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null;
		addDokument(
				DokumentTyp.JAHRESLOHNAUSWEISE,
				gesuch,
				anlageVerzeichnis,
				1,
				gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		addDokument(
				DokumentTyp.NACHWEIS_VERMOEGEN,
				gesuch,
				anlageVerzeichnis,
				1,
				gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		addDokument(
				DokumentTyp.JAHRESLOHNAUSWEISE,
				gesuch,
				anlageVerzeichnis,
				2,
				gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		addDokument(
				DokumentTyp.NACHWEIS_VERMOEGEN,
				gesuch,
				anlageVerzeichnis,
				2,
				gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
	}

	private boolean isVeranlagt(Gesuch gesuch) {
		assert gesuch.getGesuchsteller1() != null;
		assert gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer() != null;
		return Boolean.TRUE.equals(gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getVeranlagt());
	}

	@Override
	protected boolean isGemeinsameSteuererklaerung(@Nonnull Gesuch gesuch) {
		final Familiensituation familiensituation = gesuch.extractFamiliensituation();

		return familiensituation != null &&
				familiensituation.getFamilienstatus() == EnumFamilienstatus.VERHEIRATET;
	}

	private void getAllDokumenteGS1(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		final GesuchstellerContainer gesuchsteller = gesuch.getGesuchsteller1();
		getAllDokumenteGesuchsteller(gesuchsteller, 1, gesuch, anlageVerzeichnis);
	}

	private void getAllDokumenteGS2(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		final GesuchstellerContainer gesuchsteller = gesuch.getGesuchsteller2();
		getAllDokumenteGesuchsteller(gesuchsteller, 2, gesuch, anlageVerzeichnis);
	}

	private void getAllDokumenteGemeinsam(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();

		if (gesuchsteller1 == null || gesuchsteller1.getFinanzielleSituationContainer() == null) {
			return;
		}

		final FinanzielleSituation finanzielleSituationGS1JA = gesuchsteller1.getFinanzielleSituationContainer().getFinanzielleSituationJA();

		if (finanzielleSituationGS1JA.getVeranlagt() != null && finanzielleSituationGS1JA.getVeranlagt()) {
			addDokument(DokumentTyp.STEUERVERANLAGUNG, gesuch, anlageVerzeichnis, 0, finanzielleSituationGS1JA);
		} else {
			getAllDokumenteGS1(gesuch, anlageVerzeichnis);
			getAllDokumenteGS2(gesuch, anlageVerzeichnis);
		}
	}

	private void getAllDokumenteGesuchsteller(
		GesuchstellerContainer gesuchsteller,
		int gesuchstellerNumber,
		Gesuch gesuch,
		Set<DokumentGrund> anlageVerzeichnis) {

		if (gesuchsteller == null || gesuchsteller.getFinanzielleSituationContainer() == null) {
			return;
		}

		final FinanzielleSituation finanzielleSituationJA = gesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA();

		if (finanzielleSituationJA.getVeranlagt() != null && finanzielleSituationJA.getVeranlagt()) {
			addDokument(DokumentTyp.STEUERVERANLAGUNG, gesuch, anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
		} else {
			//Selbstdeklaration
			addDokument(DokumentTyp.JAHRESLOHNAUSWEISE, gesuch, anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
			addDokument(DokumentTyp.NACHWEIS_VERMOEGEN, gesuch, anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
		}
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
