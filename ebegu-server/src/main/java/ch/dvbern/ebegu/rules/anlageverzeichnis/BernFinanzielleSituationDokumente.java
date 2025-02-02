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

/**
 * Dokumente für FinanzielleSituation:
 * <p>
 * Steuerveranlagung
 * Notwendig, wenn Steuerveranlagung vorhanden. Ist dies der Fall, müssen alle weiteren Belege unter „Finanzielle
 * Situation“ nicht eingereicht werden
 * Bei Verheirateten wird das Dokument unter "Allgemeine Dokumente" angezeigt, bei allen anderen beim jeweiligen
 * Gesuchsteller
 * <p>
 * Steuererklärung
 * Notwendig, wenn keine Steuerveranlagung, jedoch Steuererklärung vorhanden
 * Bei Verheirateten wird das Dokument unter "Allgemeine Dokumente" angezeigt, bei allen anderen beim jeweiligen
 * Gesuchsteller
 * <p>
 * Jahreslohnausweise
 * Notwendig, wenn keine Veranlagung vorhanden und Nettolohn > 0
 * <p>
 * Nachweis über Familienzulagen (soweit nicht im Nettolohn enthalten)
 * Notwendig, wenn keine Veranlagung vorhanden und Familienzulage > 0
 * <p>
 * Nachweis über Ersatzeinkommen
 * Notwendig wenn keine Veranlagung vorhanden und Ersatzeinkommen > 0
 * <p>
 * Nachweis über Ersatzeinkommen Selbstaendigkeit der letzten drei Jahre (Basisjahr, Basisjahr-1, Basisjahr-2)
 * Notwendig wenn Ersatzeinkommen(Basisjahr, Basisjahr-1, Basisjahr-2) > 0
 * <p>
 * Nachweis über erhaltene Alimente (Unterhaltsbeiträge)
 * Notwendig, wenn keine Veranlagung vorhanden und erhaltene Alimente > 0
 * <p>
 * Nachweis über geleistete Alimente
 * Notwendig, wenn keine Veranlagung vorhanden und geleistete Alimente > 0
 * <p>
 * Nachweis über das Vermögen, Stand 31.12., (z.B.: Kto.-Auszug, Immobilien, Zinsbestätigung usw.)
 * Notwendig, wenn weder Steuerveranlagung noch Steuerklärung vorhanden und Vermögen> 0
 * <p>
 * Nachweis über die Schulden, Stand: 31.12., (z.B.: Kto.-Auszug, Darlehensvertrag usw.)
 * Notwendig, wenn weder Steuerveranlagung noch Steuerklärung vorhanden und Schulden > 0
 * <p>
 * Erfolgsrechnungen der letzten drei Jahre (Basisjahr, Basisjahr-1, Basisjahr-2)
 * Notwendig, wenn keine Steuerveranlagung vorhanden und Summe der Erfolgsrechnungen > 0
 * <p>
 * Nachweis über die Schulden
 * Notwendig, wenn Steuererklärung ausgefüllt
 * <p>
 * Nachweis über das Vermögen
 * Notwendig, wenn Steuererklärung ausgefüllt
 * <p>
 **/
public class BernFinanzielleSituationDokumente extends AbstractFinanzielleSituationDokumente {

	public BernFinanzielleSituationDokumente(boolean isFKJV) {
		super(isFKJV);
	}

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

		final int basisJahr = gesuch.getGesuchsperiode().getGueltigkeit().calculateEndOfPreviousYear().getYear();
		LocalDate stichtag = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();

		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		getAllDokumenteGesuchsteller(
			anlageVerzeichnis,
			gesuchsteller1,
			gemeinsam,
			1,
			basisJahr,
			stichtag,
			familiensituation);

		if (gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()) {
			final GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();
			getAllDokumenteGesuchsteller(
				anlageVerzeichnis,
				gesuchsteller2,
				gemeinsam,
				2,
				basisJahr,
				stichtag,
				familiensituation);
		}
	}

	private void getAllDokumenteGesuchsteller(
		Set<DokumentGrund> anlageVerzeichnis,
		@Nullable GesuchstellerContainer gesuchsteller,
		boolean gemeinsam, int gesuchstellerNumber, int basisJahr,
		@Nonnull LocalDate stichtag, @Nullable Familiensituation familiensituation
	) {

		if (gesuchsteller == null || gesuchsteller.getFinanzielleSituationContainer() == null) {
			return;
		}

		if (isSozialhilfeempfaenger(familiensituation) || !isVerguenstigungGewuenscht(familiensituation)) {
			return;
		}

		final FinanzielleSituationContainer finanzielleSituationContainer =
			gesuchsteller.getFinanzielleSituationContainer();

		final FinanzielleSituation finanzielleSituationJA = finanzielleSituationContainer.getFinanzielleSituationJA();

		if (this.isFKJV() && finanzielleSituationJA.getEinkommenInVereinfachtemVerfahrenAbgerechnet() != null
			&& finanzielleSituationJA.getEinkommenInVereinfachtemVerfahrenAbgerechnet()) {
			add(
				getDokument(
					DokumentTyp.NACHWEIS_EINKOMMEN_VERFAHREN,
					finanzielleSituationJA,
					familiensituation,
					String.valueOf(basisJahr),
					DokumentGrundPersonType.GESUCHSTELLER,
					gesuchstellerNumber,
					DokumentGrundTyp.FINANZIELLESITUATION,
					stichtag
				),
				anlageVerzeichnis
			);
		}

		getAllDokumenteGesuchstellerAutomatischeSteuerabfrage(
			anlageVerzeichnis,
			basisJahr,
			gesuchstellerNumber,
			finanzielleSituationJA,
			DokumentGrundTyp.FINANZIELLESITUATION,
			stichtag
		);

		if (Boolean.TRUE.equals(finanzielleSituationJA.getSteuerdatenZugriff())
			&& finanzielleSituationJA.getSteuerdatenAbfrageStatus() != null
			&& finanzielleSituationJA.getSteuerdatenAbfrageStatus()
			.isSteuerdatenAbfrageErfolgreich()) {
			return;
		}

		getAllDokumenteGesuchstellerManuelleSteuerabfrage(
			anlageVerzeichnis,
			basisJahr,
			gemeinsam,
			gesuchstellerNumber,
			finanzielleSituationJA,
			DokumentGrundTyp.FINANZIELLESITUATION,
			stichtag
		);

		add(
			getDokument(
				DokumentTyp.JAHRESLOHNAUSWEISE,
				finanzielleSituationJA,
				familiensituation,
				String.valueOf(basisJahr),
				DokumentGrundPersonType.GESUCHSTELLER,
				gesuchstellerNumber,
				DokumentGrundTyp.FINANZIELLESITUATION,
				stichtag
			),
			anlageVerzeichnis
		);

		if (this.isFKJV()) {
			add(
				getDokument(
					DokumentTyp.NACHWEIS_BRUTTOVERMOEGENERTRAEGE,
					finanzielleSituationJA,
					familiensituation,
					String.valueOf(basisJahr),
					DokumentGrundPersonType.GESUCHSTELLER,
					gesuchstellerNumber,
					DokumentGrundTyp.FINANZIELLESITUATION,
					stichtag
				),
				anlageVerzeichnis
			);
			add(
				getDokument(
					DokumentTyp.NACHWEIS_GEWINNUNGSKOSTEN,
					finanzielleSituationJA,
					familiensituation,
					String.valueOf(basisJahr),
					DokumentGrundPersonType.GESUCHSTELLER,
					gesuchstellerNumber,
					DokumentGrundTyp.FINANZIELLESITUATION,
					stichtag
				),
				anlageVerzeichnis
			);
			add(
				getDokument(
					DokumentTyp.NACHWEIS_SCHULDZINSEN,
					finanzielleSituationJA,
					familiensituation,
					String.valueOf(basisJahr),
					DokumentGrundPersonType.GESUCHSTELLER,
					gesuchstellerNumber,
					DokumentGrundTyp.FINANZIELLESITUATION,
					stichtag
				),
				anlageVerzeichnis
			);
			add(
				getDokument(
					DokumentTyp.NACHWEIS_NETTOERTRAEGE_ERBENGEMEINSCHAFTEN,
					finanzielleSituationJA,
					familiensituation,
					String.valueOf(basisJahr),
					DokumentGrundPersonType.GESUCHSTELLER,
					gesuchstellerNumber,
					DokumentGrundTyp.FINANZIELLESITUATION,
					stichtag
				),
				anlageVerzeichnis
			);
		}
	}

	@Override
	protected boolean isJahresLohnausweisNeeded(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		if (abstractFinanzielleSituation instanceof FinanzielleSituation) {
			FinanzielleSituation finanzielleSituation = (FinanzielleSituation) abstractFinanzielleSituation;

			return !finanzielleSituation.getSteuerveranlagungErhalten() &&
				finanzielleSituation.getNettolohn() != null &&
				finanzielleSituation.getNettolohn().compareTo(BigDecimal.ZERO) > 0;
		}
		return false;
	}

	@Override
	protected boolean isErfolgsrechnungNeeded(
		@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation,
		int minus) {
		if (abstractFinanzielleSituation instanceof FinanzielleSituation) {
			FinanzielleSituation finanzielleSituation = (FinanzielleSituation) abstractFinanzielleSituation;
			switch (minus) {
			case 0:
				return !finanzielleSituation.getSteuerveranlagungErhalten()
					&& (finanzielleSituation.getGeschaeftsgewinnBasisjahr() != null);
			case 1:
				return !finanzielleSituation.getSteuerveranlagungErhalten()
					&& (finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1() != null);
			case 2:
				return !finanzielleSituation.getSteuerveranlagungErhalten()
					&& (finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2() != null);
			default:
				return false;
			}
		}
		return false;
	}

	@Override
	protected boolean isNachweisErsatzeinkommenSelbststaendigkeitNeeded(
		AbstractFinanzielleSituation abstractFinanzielleSituation,
		int basisjahr) {
		if (abstractFinanzielleSituation instanceof FinanzielleSituation) {
			FinanzielleSituation finanzielleSituation = (FinanzielleSituation) abstractFinanzielleSituation;
			switch (basisjahr) {
				case 0:
					return hasValueBigerThanZero(finanzielleSituation.getErsatzeinkommenSelbststaendigkeitBasisjahr());
				case 1:
					return hasValueBigerThanZero(finanzielleSituation.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1());
				case 2:
					return hasValueBigerThanZero(finanzielleSituation.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2());
				default:
					return false;
			}
		}
		return false;
	}
}
