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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_SCHULDEN;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_VERMOEGEN;

/**
 * Dokumente für Einkommensverschlechterung:
 * <p>
 * Unterlagen zur aktuellen Einkommenssituation (pro Monat)
 * Notwendig, wenn Monatseinkünfte > 0
 * <p>
 * Nachweis über Familienzulagen (soweit nicht im Nettolohn enthalten)
 * Notwendig, wenn Familienzulage > 0
 * <p>
 * Nachweis über Ersatzeinkommen
 * Notwendig, wenn Ersatzeinkommen > 0
 * <p>
 * Nachweis über Ersatzeinkommen Selbstaendigkeit der letzten zwei Jahre (Jahr der Einkommensverschlechterung: x, x-1)
 * Notwendig wenn Ersatzeinkommen(Jahr der Einkommensverschlechterung: x, x-1) > 0
 * <p>
 * Nachweis über erhaltene Alimente (Unterhaltsbeiträge)
 * Notwendig, wenn erhaltene Alimente > 0
 * <p>
 * Nachweis über geleistete Alimente
 * Notwendig, wenn geleistete Alimente > 0
 * <p>
 * Nachweis über das Vermögen, Stand 31.12. (z.B.: Kto.-Auszug, Immobilien usw.)
 * Notwendig, wenn Vermögen > 0
 * <p>
 * Nachweis über die Schulden, Stand: 31.12. (z.B.: Kto.-Auszug, Darlehensvertrag usw.)
 * Notwendig, wenn Schulden > 0
 * <p>
 * Erfolgsrechnungen der letzten zwei Jahre (Jahr der Einkommensverschlechterung: x, x-1)
 * Notwendig, wenn Erfolgsrechnungen des Jahres nicht null
 **/
public class BernEinkommensverschlechterungDokumente extends AbstractFinanzielleSituationDokumente {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale
	) {

		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		final boolean gemeinsam = familiensituation != null &&
			familiensituation.getGemeinsameSteuererklaerung() != null &&
			familiensituation.getGemeinsameSteuererklaerung();

		final EinkommensverschlechterungInfo einkommensverschlechterungInfo =
			gesuch.extractEinkommensverschlechterungInfo();

		final int basisJahrPlus1 =
			gesuch.getGesuchsperiode().getGueltigkeit().calculateEndOfPreviousYear().getYear() + 1;
		final int basisJahrPlus2 =
			gesuch.getGesuchsperiode().getGueltigkeit().calculateEndOfPreviousYear().getYear() + 2;

		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		final GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();

		// we need to check the familiensituation at the end of the period to see if there is 1GS or 2GS
		final LocalDate stichtag = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();

		if (einkommensverschlechterungInfo != null) {
			if (einkommensverschlechterungInfo.getEkvFuerBasisJahrPlus1()) {
				getAllDokumenteGesuchsteller(
					anlageVerzeichnis,
					gesuchsteller1,
					gemeinsam,
					1,
					1,
					basisJahrPlus1,
					stichtag,
					familiensituation);
				if (gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()) {
					getAllDokumenteGesuchsteller(
						anlageVerzeichnis,
						gesuchsteller2,
						gemeinsam,
						2,
						1,
						basisJahrPlus1,
						stichtag,
						familiensituation);
				}
			}
			if (einkommensverschlechterungInfo.getEkvFuerBasisJahrPlus2()) {
				getAllDokumenteGesuchsteller(
					anlageVerzeichnis,
					gesuchsteller1,
					gemeinsam,
					1,
					2,
					basisJahrPlus2,
					stichtag,
					familiensituation);
				if (gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()) {
					getAllDokumenteGesuchsteller(
						anlageVerzeichnis,
						gesuchsteller2,
						gemeinsam,
						2,
						2,
						basisJahrPlus2,
						stichtag,
						familiensituation);
				}
			}
		}

	}

	private void getAllDokumenteGesuchsteller(
		Set<DokumentGrund> anlageVerzeichnis,
		@Nullable GesuchstellerContainer gesuchsteller,
		boolean gemeinsam, int gesuchstellerNumber, int basisJahrPlusNumber, int basisJahr,
		@Nonnull LocalDate stichtag, @Nullable Familiensituation familiensituation) {

		if (gesuchsteller == null || gesuchsteller.getEinkommensverschlechterungContainer() == null) {
			return;
		}

		if (isSozialhilfeempfaenger(familiensituation) || !isVerguenstigungGewuenscht(familiensituation)) {
			return;
		}

		final EinkommensverschlechterungContainer einkommensverschlechterungContainer =
			gesuchsteller.getEinkommensverschlechterungContainer();
		Einkommensverschlechterung einkommensverschlechterung;
		if (basisJahrPlusNumber == 2) {
			einkommensverschlechterung = einkommensverschlechterungContainer.getEkvJABasisJahrPlus2();
		} else {
			einkommensverschlechterung = einkommensverschlechterungContainer.getEkvJABasisJahrPlus1();
		}

		getAllDokumenteGesuchstellerAutomatischeSteuerabfrage(anlageVerzeichnis, basisJahr,
			gesuchstellerNumber, einkommensverschlechterung, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, stichtag);

		getAllDokumenteGesuchstellerManuelleSteuerabfrage(anlageVerzeichnis, basisJahr, gemeinsam,
			gesuchstellerNumber, einkommensverschlechterung, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, stichtag);

		add(
			getDokument(
				DokumentTyp.JAHRESLOHNAUSWEISE,
				einkommensverschlechterung,
				familiensituation,
				String.valueOf(basisJahr),
				DokumentGrundPersonType.GESUCHSTELLER,
				gesuchstellerNumber,
				DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG,
				stichtag
			),
			anlageVerzeichnis
		);
	}

	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation
	) {
		if (abstractFinanzielleSituation == null) {
			return false;
		}
		if (dokumentTyp == NACHWEIS_VERMOEGEN) {
			return true;
		}
		if (dokumentTyp == NACHWEIS_SCHULDEN) {
			return abstractFinanzielleSituation.getSchulden() != null &&
				abstractFinanzielleSituation.getSchulden().compareTo(BigDecimal.ZERO) > 0;
		}

		return super.isDokumentNeeded(dokumentTyp, abstractFinanzielleSituation);
	}

	@Override
	protected boolean isJahresLohnausweisNeeded(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		if (abstractFinanzielleSituation instanceof Einkommensverschlechterung) {
			Einkommensverschlechterung ekv = (Einkommensverschlechterung) abstractFinanzielleSituation;

			return ekv.getNettolohn() != null &&
				ekv.getNettolohn().compareTo(BigDecimal.ZERO) > 0;
		}
		return false;
	}

	@Override
	protected boolean isErfolgsrechnungNeeded(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation, int minus) {
		if (abstractFinanzielleSituation instanceof Einkommensverschlechterung) {
			Einkommensverschlechterung einkommensverschlechterung =
				(Einkommensverschlechterung) abstractFinanzielleSituation;
			switch (minus) {
			case 0:
				return !einkommensverschlechterung.getSteuerveranlagungErhalten()
					&& (einkommensverschlechterung.getGeschaeftsgewinnBasisjahr() != null);
			case 1:
				return !einkommensverschlechterung.getSteuerveranlagungErhalten()
					&& (einkommensverschlechterung.getGeschaeftsgewinnBasisjahrMinus1() != null);
			}
		}
		return false;
	}

	@Override
	protected boolean isNachweisErsatzeinkommenSelbststaendigkeitNeeded(
		AbstractFinanzielleSituation abstractFinanzielleSituation,
		int minus) {
		if (abstractFinanzielleSituation instanceof Einkommensverschlechterung) {
			Einkommensverschlechterung einkommensverschlechterung = (Einkommensverschlechterung) abstractFinanzielleSituation;
			switch (minus) {
			case 0:
				return hasValueBigerThanZero(einkommensverschlechterung.getErsatzeinkommenSelbststaendigkeitBasisjahr());
			case 1:
				return hasValueBigerThanZero(einkommensverschlechterung.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1());
			default:
				return false;
			}
		}
		return false;
	}
}
