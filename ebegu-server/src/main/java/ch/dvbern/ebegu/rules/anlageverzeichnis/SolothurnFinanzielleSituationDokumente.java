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
import ch.dvbern.ebegu.util.ServerMessageUtil;

public class SolothurnFinanzielleSituationDokumente extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation>   {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale) {

		if (isGemeinsameSteuererklaerung(gesuch))  {
			addDokumenteGemeinsam(gesuch, anlageVerzeichnis, locale);
		} else {
			addDokumenteGS1(gesuch, anlageVerzeichnis, locale);
			addDokumenteGS2(gesuch, anlageVerzeichnis, locale);
		}
	}

	private void addDokumenteGemeinsam(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis, Locale locale) {
		GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();

		if (gesuchsteller1 == null || gesuchsteller1.getFinanzielleSituationContainer() == null) {
			return;
		}

		final FinanzielleSituation finanzielleSituationGS1JA = gesuchsteller1.getFinanzielleSituationContainer().getFinanzielleSituationJA();

		if (finanzielleSituationGS1JA.getSteuerveranlagungErhalten() != null && finanzielleSituationGS1JA.getSteuerveranlagungErhalten()) {
			addDokumentVeranlagung(gesuch, anlageVerzeichnis, 0, finanzielleSituationGS1JA, locale);
		} else {
			addDokumenteGS1(gesuch, anlageVerzeichnis, locale);
			addDokumenteGS2(gesuch, anlageVerzeichnis, locale);
		}
	}

	private void addDokumenteGS1(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis, Locale locale) {
		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		addDokumenteGesuchsteller(gesuchsteller1, 1, gesuch, anlageVerzeichnis, locale);
	}

	private void addDokumenteGS2(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis, Locale locale) {
		final GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();
		addDokumenteGesuchsteller(gesuchsteller2, 2, gesuch, anlageVerzeichnis, locale);
	}

	private void addDokumenteGesuchsteller(GesuchstellerContainer gesuchsteller, int gesuchstellerNumber, Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis, Locale locale) {
		if (gesuchsteller == null || gesuchsteller.getFinanzielleSituationContainer() == null) {
			return;
		}

		final FinanzielleSituation finanzielleSituationJA = gesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA();

		if (finanzielleSituationJA.getSteuerveranlagungErhalten() != null && finanzielleSituationJA.getSteuerveranlagungErhalten()) {
			addDokumentVeranlagung(gesuch, anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA, locale);
		} else {
			addDokument(DokumentTyp.NACHWEIS_VERMOEGEN, gesuch, anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
			addDokument(DokumentTyp.NACHWEIS_BRUTTOLOHN, gesuch, anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
		}
	}

	private void addDokumentVeranlagung(
		Gesuch gesuch,
		Set<DokumentGrund> anlageVerzeichnis,
		int gesuchstellerNumber,
		FinanzielleSituation finanzielleSituationJA,
		Locale locale) {

		final String documentTag = getDokuementTagForVeranlagung(gesuch, locale);

		add(getDokument(
				DokumentTyp.STEUERVERANLAGUNG,
				finanzielleSituationJA,
				documentTag,
				DokumentGrundPersonType.GESUCHSTELLER,
				gesuchstellerNumber,
				DokumentGrundTyp.FINANZIELLESITUATION),
			anlageVerzeichnis);
	}

	private String getDokuementTagForVeranlagung(Gesuch gesuch, Locale locale) {
		final int basisJahr = gesuch.getGesuchsperiode().getGueltigkeit().calculateEndOfPreviousYear().getYear();
		final int basisJahrPlus1 = basisJahr + 1;
		return ServerMessageUtil.getMessage(
			"DOKUMENTE_TAG_BASISJAHR_ODER_PLUS_EINS",
			locale,
			gesuch.extractMandant(),
			String.valueOf(basisJahr),
			String.valueOf(basisJahrPlus1));
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
