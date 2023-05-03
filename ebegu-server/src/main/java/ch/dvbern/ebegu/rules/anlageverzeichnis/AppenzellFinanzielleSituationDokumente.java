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
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;

public class AppenzellFinanzielleSituationDokumente extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation>   {

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
		addDokumentVeranlagung(anlageVerzeichnis, 0, finanzielleSituationGS1JA);
	}

	private void addDokumenteGS1(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		Objects.requireNonNull(gesuchsteller1);
		addDokumenteGesuchsteller(gesuchsteller1, 1, gesuch, anlageVerzeichnis);
		addDokumentAppenzellSpezialFall(gesuchsteller1.getFinanzielleSituationContainer(), anlageVerzeichnis);
	}

	private void addDokumentAppenzellSpezialFall(
			@Nullable FinanzielleSituationContainer finanzielleSituationContainer,
			Set<DokumentGrund> anlageVerzeichnis) {
		if (finanzielleSituationContainer == null ||
			finanzielleSituationContainer.getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell() == null ||
			finanzielleSituationContainer.getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell().getZusatzangabenPartner() == null
		) {
			return;
		}

		addDokumentVeranlagung(anlageVerzeichnis, 2, null);
	}

	private void addDokumenteGS2(Gesuch gesuch, Set<DokumentGrund> anlageVerzeichnis) {
		final GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();
		addDokumenteGesuchsteller(gesuchsteller2, 2, gesuch, anlageVerzeichnis);
	}

	private void addDokumenteGesuchsteller(
			@Nullable GesuchstellerContainer gesuchsteller,
			int gesuchstellerNumber,
			Gesuch gesuch,
			Set<DokumentGrund> anlageVerzeichnis) {
		if (gesuchsteller == null || gesuchsteller.getFinanzielleSituationContainer() == null) {
			return;
		}

		final FinanzielleSituation finanzielleSituationJA = gesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA();
		addDokumentVeranlagung(anlageVerzeichnis, gesuchstellerNumber, finanzielleSituationJA);
	}

	private void addDokumentVeranlagung(
		Set<DokumentGrund> anlageVerzeichnis,
		int gesuchstellerNumber,
		@Nullable FinanzielleSituation finanzielleSituationJA) {

		add(getDokument(
				DokumentTyp.STEUERVERANLAGUNG,
				finanzielleSituationJA,
				null,
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
