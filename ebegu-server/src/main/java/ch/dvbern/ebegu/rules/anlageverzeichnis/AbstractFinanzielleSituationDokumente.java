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

package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;

/**
 * Gemeinsame Basisklasse zum berechnen der benötigten Dokumente für die Finanzielle Situation und die
 * Einkommensverschlechterung
 */
abstract class AbstractFinanzielleSituationDokumente
	extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> {

	void getAllDokumenteGesuchsteller(
		Set<DokumentGrund> anlageVerzeichnis,
		int basisJahr,
		boolean gemeinsam,
		int gesuchstellerNumber,
		AbstractFinanzielleSituation abstractFinanzielleSituation,
		DokumentGrundTyp dokumentGrundTyp,
		@Nonnull LocalDate stichtag
		) {

		final String basisJahrString = String.valueOf(basisJahr);

		if (gemeinsam) {
			if (gesuchstellerNumber == 1) {
				add(getDokument(DokumentTyp.STEUERVERANLAGUNG, abstractFinanzielleSituation, null, basisJahrString,
					DokumentGrundPersonType.GESUCHSTELLER,
					0, dokumentGrundTyp, stichtag), anlageVerzeichnis);
				add(getDokument(DokumentTyp.STEUERERKLAERUNG, abstractFinanzielleSituation, null, basisJahrString,
					DokumentGrundPersonType.GESUCHSTELLER,
					0, dokumentGrundTyp, stichtag), anlageVerzeichnis);
			}
		} else {
			add(getDokument(DokumentTyp.STEUERVERANLAGUNG, abstractFinanzielleSituation, null, basisJahrString,
				DokumentGrundPersonType.GESUCHSTELLER,
				gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
			add(getDokument(DokumentTyp.STEUERERKLAERUNG, abstractFinanzielleSituation, null, basisJahrString,
				DokumentGrundPersonType.GESUCHSTELLER,
				gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		}

		add(getDokument(DokumentTyp.NACHWEIS_FAMILIENZULAGEN, abstractFinanzielleSituation, null, basisJahrString,
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_ERSATZEINKOMMEN, abstractFinanzielleSituation, null, basisJahrString,
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_ERHALTENE_ALIMENTE, abstractFinanzielleSituation, null, basisJahrString,
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_GELEISTETE_ALIMENTE, abstractFinanzielleSituation, null, basisJahrString,
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_VERMOEGEN, abstractFinanzielleSituation, null, basisJahrString,
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_SCHULDEN, abstractFinanzielleSituation, null, basisJahrString,
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		add(getDokument(DokumentTyp.ERFOLGSRECHNUNGEN_JAHR, abstractFinanzielleSituation, null, basisJahrString,
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		add(getDokument(DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS1, abstractFinanzielleSituation, null,
			String.valueOf(basisJahr - 1), DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
		add(getDokument(DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS2, abstractFinanzielleSituation, null,
			String.valueOf(basisJahr - 2), DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber, dokumentGrundTyp, stichtag), anlageVerzeichnis);
	}

	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation) {
		if (abstractFinanzielleSituation != null) {
			switch (dokumentTyp) {
			case STEUERVERANLAGUNG:
				return abstractFinanzielleSituation.getSteuerveranlagungErhalten();
			case STEUERERKLAERUNG:
				return !abstractFinanzielleSituation.getSteuerveranlagungErhalten()
					&& abstractFinanzielleSituation.getSteuererklaerungAusgefuellt();
			case JAHRESLOHNAUSWEISE:
				return isJahresLohnausweisNeeded(abstractFinanzielleSituation);
			case NACHWEIS_FAMILIENZULAGEN:
				return !abstractFinanzielleSituation.getSteuerveranlagungErhalten() &&
					abstractFinanzielleSituation.getFamilienzulage() != null &&
					abstractFinanzielleSituation.getFamilienzulage().compareTo(BigDecimal.ZERO) > 0;
			case NACHWEIS_ERSATZEINKOMMEN:
				return !abstractFinanzielleSituation.getSteuerveranlagungErhalten() &&
					abstractFinanzielleSituation.getErsatzeinkommen() != null &&
					abstractFinanzielleSituation.getErsatzeinkommen().compareTo(BigDecimal.ZERO) > 0;
			case NACHWEIS_ERHALTENE_ALIMENTE:
				return !abstractFinanzielleSituation.getSteuerveranlagungErhalten() &&
					abstractFinanzielleSituation.getErhalteneAlimente() != null &&
					abstractFinanzielleSituation.getErhalteneAlimente().compareTo(BigDecimal.ZERO) > 0;
			case NACHWEIS_GELEISTETE_ALIMENTE:
				return !abstractFinanzielleSituation.getSteuerveranlagungErhalten() &&
					abstractFinanzielleSituation.getGeleisteteAlimente() != null &&
					abstractFinanzielleSituation.getGeleisteteAlimente().compareTo(BigDecimal.ZERO) > 0;
			case NACHWEIS_VERMOEGEN:
				// Vermögen muss immer ausgewiesen werden!
				return !abstractFinanzielleSituation.getSteuerveranlagungErhalten() &&
					!abstractFinanzielleSituation.getSteuererklaerungAusgefuellt();
			case NACHWEIS_SCHULDEN:
				return !abstractFinanzielleSituation.getSteuerveranlagungErhalten() &&
					!abstractFinanzielleSituation.getSteuererklaerungAusgefuellt() &&
					abstractFinanzielleSituation.getSchulden() != null &&
					abstractFinanzielleSituation.getSchulden().compareTo(BigDecimal.ZERO) > 0;
			case ERFOLGSRECHNUNGEN_JAHR:
				return isErfolgsrechnungNeeded(abstractFinanzielleSituation, 0);
			case ERFOLGSRECHNUNGEN_JAHR_MINUS1:
				return isErfolgsrechnungNeeded(abstractFinanzielleSituation, 1);
			case ERFOLGSRECHNUNGEN_JAHR_MINUS2:
				return isErfolgsrechnungNeeded(abstractFinanzielleSituation, 2);
			case NACHWEIS_LOHNAUSWEIS_1:
			case NACHWEIS_LOHNAUSWEIS_2:
			case NACHWEIS_LOHNAUSWEIS_3:
				return true;
			case NACHWEIS_EINKOMMEN_VERFAHREN:
				return abstractFinanzielleSituation.getEinkommenInVereinfachtemVerfahrenAbgerechnet() != null &&
					abstractFinanzielleSituation.getEinkommenInVereinfachtemVerfahrenAbgerechnet();
			case NACHWEIS_BRUTTOVERMOEGENERTRAEGE:
				return abstractFinanzielleSituation.getBruttoertraegeVermoegen() != null
					&& abstractFinanzielleSituation.getBruttoertraegeVermoegen().compareTo(BigDecimal.ZERO) > 0;
			case NACHWEIS_GEWINNUNGSKOSTEN:
				return abstractFinanzielleSituation.getGewinnungskosten() != null
					&& abstractFinanzielleSituation.getGewinnungskosten().compareTo(BigDecimal.ZERO) > 0;
			case NACHWEIS_SCHULDZINSEN:
				return abstractFinanzielleSituation.getAbzugSchuldzinsen() != null
					&& abstractFinanzielleSituation.getAbzugSchuldzinsen().compareTo(BigDecimal.ZERO) > 0;
			case NACHWEIS_NETTOERTRAEGE_ERBENGEMEINSCHAFTEN:
				return abstractFinanzielleSituation.getNettoertraegeErbengemeinschaft() != null
					&& abstractFinanzielleSituation.getNettoertraegeErbengemeinschaft().compareTo(BigDecimal.ZERO) > 0;
			default:
				return false;
			}
		}
		return false;
	}

	protected abstract boolean isJahresLohnausweisNeeded(
		@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation);

	protected abstract boolean isErfolgsrechnungNeeded(
		@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation, int minus);
}
