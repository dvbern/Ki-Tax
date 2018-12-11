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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;

/**
 * Ein Rechner mit den ganzen Operationen fuer Finanziellesituation
 * Created by imanol on 22.06.16.
 */
@Dependent
public class FinanzielleSituationRechner {

	/**
	 * Konstruktor, welcher einen Rechner erstellt, der die Paramter aus der DB liest
	 */
	public FinanzielleSituationRechner() {

	}

	@Nonnull
	public FinanzielleSituationResultateDTO calculateResultateFinanzielleSituation(@Nonnull Gesuch gesuch, boolean hasSecondGesuchsteller) {

		final FinanzielleSituationResultateDTO finSitResultDTO = new FinanzielleSituationResultateDTO();
		setFinanzielleSituationParameters(gesuch, finSitResultDTO, hasSecondGesuchsteller);

		return finSitResultDTO;
	}

	/**
	 * Diese Methode wird momentan im Print gebraucht um die Finanzielle Situation zu berechnen. Der Abzug aufgrund Familiengroesse wird
	 * hier auf 0 gesetzt
	 */

	@Nonnull
	public FinanzielleSituationResultateDTO calculateResultateEinkommensverschlechterung(@Nonnull Gesuch gesuch, int basisJahrPlus, boolean hasSecondGesuchsteller) {
		Objects.requireNonNull(gesuch.extractEinkommensverschlechterungInfo());

		final FinanzielleSituationResultateDTO einkVerResultDTO = new FinanzielleSituationResultateDTO();
		setEinkommensverschlechterungParameters(gesuch, basisJahrPlus, einkVerResultDTO, hasSecondGesuchsteller);

		return einkVerResultDTO;
	}

	/**
	 * Nimmt das uebergebene FinanzielleSituationResultateDTO und mit den Daten vom Gesuch, berechnet alle im
	 * FinanzielleSituationResultateDTO benoetigten Daten und setzt sie direkt im dto.
	 */
	private void setFinanzielleSituationParameters(@Nonnull Gesuch gesuch, final FinanzielleSituationResultateDTO finSitResultDTO, boolean hasSecondGesuchsteller) {
		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());
		finSitResultDTO.setGeschaeftsgewinnDurchschnittGesuchsteller1(calcGeschaeftsgewinnDurchschnitt(finanzielleSituationGS1));

		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		FinanzielleSituation finanzielleSituationGS2 = null;
		if (hasSecondGesuchsteller && gesuch.getGesuchsteller2() != null) {
			finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
			finSitResultDTO.setGeschaeftsgewinnDurchschnittGesuchsteller2(calcGeschaeftsgewinnDurchschnitt(finanzielleSituationGS2));
		}

		calculateZusammen(finSitResultDTO, finanzielleSituationGS1,
			calculateNettoJahresLohn(finanzielleSituationGS1),
			finSitResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller1(),
			finanzielleSituationGS2, calculateNettoJahresLohn(finanzielleSituationGS2),
			finSitResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller2());
	}

	/**
	 * Nimmt das uebergebene FinanzielleSituationResultateDTO und mit den Daten vom Gesuch, berechnet alle im
	 * FinanzielleSituationResultateDTO benoetigten Daten.
	 */
	private void setEinkommensverschlechterungParameters(@Nonnull Gesuch gesuch, int basisJahrPlus,
		final FinanzielleSituationResultateDTO einkVerResultDTO, boolean hasSecondGesuchsteller) {
		Einkommensverschlechterung einkommensverschlechterungGS1Bjp1 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 1);
		Einkommensverschlechterung einkommensverschlechterungGS1Bjp2 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 2);
		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());
		einkVerResultDTO.setGeschaeftsgewinnDurchschnittGesuchsteller1(
			calcGeschaeftsgewinnDurchschnitt(finanzielleSituationGS1, einkommensverschlechterungGS1Bjp1, einkommensverschlechterungGS1Bjp2,
				gesuch.extractEinkommensverschlechterungInfo(), basisJahrPlus));

		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		Einkommensverschlechterung einkommensverschlechterungGS2Bjp1 = null;
		Einkommensverschlechterung einkommensverschlechterungGS2Bjp2 = null;
		if (hasSecondGesuchsteller) {
			einkommensverschlechterungGS2Bjp1 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 1);
			einkommensverschlechterungGS2Bjp2 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 2);
			final FinanzielleSituation finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
			einkVerResultDTO.setGeschaeftsgewinnDurchschnittGesuchsteller2(
				calcGeschaeftsgewinnDurchschnitt(finanzielleSituationGS2, einkommensverschlechterungGS2Bjp1,
					einkommensverschlechterungGS2Bjp2, gesuch.extractEinkommensverschlechterungInfo(), basisJahrPlus));
		}

		if (basisJahrPlus == 2) {
			calculateZusammen(einkVerResultDTO, einkommensverschlechterungGS1Bjp2,
				calculateNettoJahresLohn(einkommensverschlechterungGS1Bjp2),
				einkVerResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller1(),
				einkommensverschlechterungGS2Bjp2, calculateNettoJahresLohn(einkommensverschlechterungGS2Bjp2),
				einkVerResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller2());
		} else {
			calculateZusammen(einkVerResultDTO, einkommensverschlechterungGS1Bjp1,
				calculateNettoJahresLohn(einkommensverschlechterungGS1Bjp1),
				einkVerResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller1(),
				einkommensverschlechterungGS2Bjp1, calculateNettoJahresLohn(einkommensverschlechterungGS2Bjp1),
				einkVerResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller2());
		}
	}

	/**
	 * Berechnet das FinazDaten DTO fuer die Finanzielle Situation
	 *
	 * @param gesuch das Gesuch dessen finazDatenDTO gesetzt werden soll
	 */
	public void calculateFinanzDaten(@Nonnull Gesuch gesuch, BigDecimal minimumEKV) {
		FinanzDatenDTO finanzDatenDTOAlleine = new FinanzDatenDTO();
		FinanzDatenDTO finanzDatenDTOZuZweit = new FinanzDatenDTO();

		// Finanzielle Situation berechnen
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOAlleine = calculateResultateFinanzielleSituation(gesuch, false);
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOZuZweit = calculateResultateFinanzielleSituation(gesuch, true);

		finanzDatenDTOAlleine.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr());
		finanzDatenDTOAlleine.setDatumVonBasisjahr(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
		finanzDatenDTOZuZweit.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr());
		finanzDatenDTOZuZweit.setDatumVonBasisjahr(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());

		//Berechnung wird nur ausgefuehrt wenn Daten vorhanden, wenn es keine gibt machen wir nichts
		EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
		if (ekvInfo != null && ekvInfo.getEinkommensverschlechterung()) {
			FinanzielleSituationResultateDTO resultateEKV1Alleine = calculateResultateEinkommensverschlechterung(gesuch, 1, false);
			FinanzielleSituationResultateDTO resultateEKV1ZuZweit = calculateResultateEinkommensverschlechterung(gesuch, 1, true);
			BigDecimal massgebendesEinkommenBasisjahrAlleine = finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr();
			BigDecimal massgebendesEinkommenBasisjahrZuZweit = finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr();

			if (ekvInfo.getEkvFuerBasisJahrPlus1() != null && ekvInfo.getEkvFuerBasisJahrPlus1()) {
				if (ekvInfo.getEkvBasisJahrPlus1Annulliert()) {
					finanzDatenDTOAlleine.setEkv1Annulliert(Boolean.TRUE);
					finanzDatenDTOZuZweit.setEkv1Annulliert(Boolean.TRUE);
				}
				// In der EKV 1 vergleichen wir immer mit dem Basisjahr
				handleEKV1(finanzDatenDTOAlleine, ekvInfo.getStichtagFuerBasisJahrPlus1(), resultateEKV1Alleine.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrAlleine, minimumEKV);
				handleEKV1(finanzDatenDTOZuZweit, ekvInfo.getStichtagFuerBasisJahrPlus1(), resultateEKV1ZuZweit.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrZuZweit, minimumEKV);
			}

			BigDecimal massgebendesEinkommenVorjahrAlleine;
			if (finanzDatenDTOAlleine.isEkv1AcceptedAndNotAnnuliert()) {
				massgebendesEinkommenVorjahrAlleine = resultateEKV1Alleine.getMassgebendesEinkVorAbzFamGr();
			} else {
				massgebendesEinkommenVorjahrAlleine = massgebendesEinkommenBasisjahrAlleine;
			}
			BigDecimal massgebendesEinkommenVorjahrZuZweit;
			if (finanzDatenDTOZuZweit.isEkv1AcceptedAndNotAnnuliert()) {
				massgebendesEinkommenVorjahrZuZweit = resultateEKV1ZuZweit.getMassgebendesEinkVorAbzFamGr();
			} else {
				massgebendesEinkommenVorjahrZuZweit = massgebendesEinkommenBasisjahrZuZweit;
			}

			if (ekvInfo.getEkvFuerBasisJahrPlus2() != null && ekvInfo.getEkvFuerBasisJahrPlus2()) {
				if (ekvInfo.getEkvBasisJahrPlus2Annulliert()) {
					finanzDatenDTOAlleine.setEkv2Annulliert(Boolean.TRUE);
					finanzDatenDTOZuZweit.setEkv2Annulliert(Boolean.TRUE);
				}
				FinanzielleSituationResultateDTO resultateEKV2Alleine = calculateResultateEinkommensverschlechterung(gesuch, 2, false);
				FinanzielleSituationResultateDTO resultateEKV2ZuZweit = calculateResultateEinkommensverschlechterung(gesuch, 2, true);
				// In der EKV 2 vergleichen wir immer mit dem EKV 1, egal ob diese akzeptiert war
				handleEKV2(finanzDatenDTOAlleine, ekvInfo.getStichtagFuerBasisJahrPlus2(),
					resultateEKV2Alleine.getMassgebendesEinkVorAbzFamGr(), massgebendesEinkommenVorjahrAlleine,
					massgebendesEinkommenBasisjahrAlleine, minimumEKV);
				handleEKV2(finanzDatenDTOZuZweit, ekvInfo.getStichtagFuerBasisJahrPlus2(),
					resultateEKV2ZuZweit.getMassgebendesEinkVorAbzFamGr(), massgebendesEinkommenVorjahrZuZweit,
					massgebendesEinkommenBasisjahrZuZweit, minimumEKV);
			} else {
				finanzDatenDTOAlleine.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahrAlleine);
				finanzDatenDTOZuZweit.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahrZuZweit);
			}
		}
		gesuch.setFinanzDatenDTO_alleine(finanzDatenDTOAlleine);
		gesuch.setFinanzDatenDTO_zuZweit(finanzDatenDTOZuZweit);
	}

	private void handleEKV1(@Nonnull FinanzDatenDTO finanzDatenDTO, @Nullable LocalDate stichtagEKV1, BigDecimal massgebendesEinkommenEKV1, BigDecimal massgebendesEinkommenBasisjahr,
		BigDecimal minimumEKV) {
		// In der EKV 1 vergleichen wir immer mit dem Basisjahr
		finanzDatenDTO.setDatumVonBasisjahrPlus1(stichtagEKV1);
		boolean accepted = acceptEKV(massgebendesEinkommenBasisjahr, massgebendesEinkommenEKV1, minimumEKV);
		finanzDatenDTO.setEkv1Accepted(accepted);
		if (accepted) {
			finanzDatenDTO.setMassgebendesEinkBjP1VorAbzFamGr(massgebendesEinkommenEKV1);
		} else {
			finanzDatenDTO.setMassgebendesEinkBjP1VorAbzFamGr(massgebendesEinkommenBasisjahr);
		}
	}

	private void handleEKV2(@Nonnull FinanzDatenDTO finanzDatenDTO, @Nullable LocalDate stichtagEKV2, BigDecimal massgebendesEinkommenEKV2, BigDecimal massgebendesEinkommenVorjahr,
		BigDecimal massgebendesEinkommenBasisjahr, BigDecimal minimumEKV) {
		// In der EKV 2 vergleichen wir immer mit dem EKV 1, egal ob diese akzeptiert war
		finanzDatenDTO.setDatumVonBasisjahrPlus2(stichtagEKV2);
		boolean ekv2AlleineAccepted = acceptEKV2(massgebendesEinkommenVorjahr, massgebendesEinkommenBasisjahr, massgebendesEinkommenEKV2, minimumEKV);
		finanzDatenDTO.setEkv2Accepted(ekv2AlleineAccepted);
		if (ekv2AlleineAccepted) {
			finanzDatenDTO.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenEKV2);
		} else {
			finanzDatenDTO.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahr);
		}
	}

	/**
	 * @return Berechnet ob die Einkommensverschlechterung mehr als 20 % gegenueber dem vorjahr betraegt, gibt true zurueckk wen ja; false sonst
	 */
	private boolean acceptEKV(BigDecimal massgebendesEinkommenBasisjahr, BigDecimal massgebendesEinkommenJahr, BigDecimal minimumEKV) {
		// EKV gewährt. Es braucht VIER_NACHKOMMASTELLE weil wir mit 1-Prozentuell arbeiten und in 100-Prozentuell gilt ZWEI_NACHKOMMASTELLE
		return massgebendesEinkommenBasisjahr.compareTo(BigDecimal.ZERO) > 0
			&& MathUtil.VIER_NACHKOMMASTELLE.divide(massgebendesEinkommenJahr, massgebendesEinkommenBasisjahr).compareTo(minimumEKV) < 0;
	}

	/**
	 * @return Die Einkommensverschlechterung II kommt zum Zuge, falls diese grösser als die Einkommensverschlechterung I ist und auch grösser 20%
	 */
	private boolean acceptEKV2(BigDecimal massgebendesEinkommenVorjahr, BigDecimal massgebendesEinkommenBasisjahr, BigDecimal massgebendesEinkommenJahr,
		BigDecimal minimumEKV) {
		// EKV gewährt. Es braucht VIER_NACHKOMMASTELLE weil wir mit 1-Prozentuell arbeiten und in 100-Prozentuell gilt ZWEI_NACHKOMMASTELLE
		return massgebendesEinkommenBasisjahr.compareTo(BigDecimal.ZERO) > 0 &&
			massgebendesEinkommenVorjahr.compareTo(massgebendesEinkommenJahr) > 0 &&
			MathUtil.VIER_NACHKOMMASTELLE.divide(massgebendesEinkommenJahr, massgebendesEinkommenBasisjahr).compareTo(minimumEKV) < 0;
	}

	private void calculateZusammen(final FinanzielleSituationResultateDTO finSitResultDTO,
		AbstractFinanzielleSituation finanzielleSituationGS1,
		BigDecimal nettoJahresLohn1, BigDecimal geschaeftsgewinnDurchschnitt1,
		AbstractFinanzielleSituation finanzielleSituationGS2,
		BigDecimal nettoJahresLohn2, BigDecimal geschaeftsgewinnDurchschnitt2) {

		finSitResultDTO.setEinkommenBeiderGesuchsteller(calcEinkommen(finanzielleSituationGS1, nettoJahresLohn1,
			geschaeftsgewinnDurchschnitt1, finanzielleSituationGS2, nettoJahresLohn2, geschaeftsgewinnDurchschnitt2));
		finSitResultDTO.setNettovermoegenFuenfProzent(calcVermoegen5Prozent(finanzielleSituationGS1, finanzielleSituationGS2));
		finSitResultDTO.setAbzuegeBeiderGesuchsteller(calcAbzuege(finanzielleSituationGS1, finanzielleSituationGS2));

		finSitResultDTO.setAnrechenbaresEinkommen(add(finSitResultDTO.getEinkommenBeiderGesuchsteller(), finSitResultDTO.getNettovermoegenFuenfProzent()));
		finSitResultDTO.setMassgebendesEinkVorAbzFamGr(
			MathUtil.positiveNonNullAndRound(
				subtract(finSitResultDTO.getAnrechenbaresEinkommen(), finSitResultDTO.getAbzuegeBeiderGesuchsteller())));
	}

	/**
	 * Diese Methode aufrufen um den GeschaeftsgewinnDurchschnitt fuer die Finanzielle Situation zu berechnen.
	 */
	@Nullable
	public static BigDecimal calcGeschaeftsgewinnDurchschnitt(@Nullable FinanzielleSituation finanzielleSituation) {
		if (finanzielleSituation != null) {
			return calcGeschaeftsgewinnDurchschnitt(finanzielleSituation.getGeschaeftsgewinnBasisjahr(),
				finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1(),
				finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2());
		}
		return null;
	}

	/**
	 * Diese Methode aufrufen um den GeschaeftsgewinnDurchschnitt fuer die Einkommensverschlechterung zu berechnen. Die finanzielle Situation
	 * muss auch uebergeben werden, da manche Daten aus ihr genommen werden
	 */
	@Nullable
	public static BigDecimal calcGeschaeftsgewinnDurchschnitt(
		@Nullable FinanzielleSituation finanzielleSituation,
		@Nullable Einkommensverschlechterung einkVersBjp1,
		@Nullable Einkommensverschlechterung einkVersBjp2,
		@Nullable EinkommensverschlechterungInfo ekvi,
		int basisJahrPlus) {
		if (basisJahrPlus == 1) {
			if (finanzielleSituation != null && einkVersBjp1 != null) {
				return calcGeschaeftsgewinnDurchschnitt(einkVersBjp1.getGeschaeftsgewinnBasisjahr(),
					finanzielleSituation.getGeschaeftsgewinnBasisjahr(),
					finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1());
			}
		} else if (basisJahrPlus == 2 && finanzielleSituation != null && einkVersBjp2 != null) {
			if (ekvi != null && ekvi.getEkvFuerBasisJahrPlus1() && einkVersBjp1 != null) {
				return calcGeschaeftsgewinnDurchschnitt(einkVersBjp2.getGeschaeftsgewinnBasisjahr(),
					einkVersBjp1.getGeschaeftsgewinnBasisjahr(),
					finanzielleSituation.getGeschaeftsgewinnBasisjahr());
			} else {
				return calcGeschaeftsgewinnDurchschnitt(einkVersBjp2.getGeschaeftsgewinnBasisjahr(),
					einkVersBjp2.getGeschaeftsgewinnBasisjahrMinus1(),
					finanzielleSituation.getGeschaeftsgewinnBasisjahr());
			}
		}
		return null;
	}

	/**
	 * Allgemeine Methode fuer die Berechnung des GeschaeftsgewinnDurchschnitt. Die drei benoetigten Felder werden uebergeben
	 */
	@Nullable
	private static BigDecimal calcGeschaeftsgewinnDurchschnitt(@Nullable final BigDecimal geschaeftsgewinnBasisjahr,
		@Nullable final BigDecimal geschaeftsgewinnBasisjahrMinus1,
		@Nullable final BigDecimal geschaeftsgewinnBasisjahrMinus2) {

		BigDecimal total = BigDecimal.ZERO;
		BigDecimal anzahlJahre = BigDecimal.ZERO;
		if (geschaeftsgewinnBasisjahrMinus2 != null) {
			total = total.add(geschaeftsgewinnBasisjahrMinus2);
			anzahlJahre = anzahlJahre.add(BigDecimal.ONE);
		}
		if (geschaeftsgewinnBasisjahrMinus1 != null) {
			total = total.add(geschaeftsgewinnBasisjahrMinus1);
			anzahlJahre = anzahlJahre.add(BigDecimal.ONE);
		}
		if (geschaeftsgewinnBasisjahr != null) {
			total = total.add(geschaeftsgewinnBasisjahr);
			anzahlJahre = anzahlJahre.add(BigDecimal.ONE);
		}
		if (anzahlJahre.intValue() > 0) {
			final BigDecimal divided = total.divide(anzahlJahre, RoundingMode.HALF_UP);
			// Durschnitt darf NIE kleiner als 0 sein
			return divided.intValue() >= 0 ? divided : BigDecimal.ZERO;
		}

		return null;
	}

	/**
	 * Berechnet 5 prozent des Nettovermoegens von GS1 und GS2. Der Gesamtwert kann dabei nicht kleiner als 0 sein auch
	 * wenn ein einzelner Gesuchsteller ein negatives Nettovermoegen hat.
	 */
	public static BigDecimal calcVermoegen5Prozent(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation1,
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation2) {

		final BigDecimal totalBruttovermoegen = add(abstractFinanzielleSituation1 != null ? abstractFinanzielleSituation1.getBruttovermoegen() : BigDecimal.ZERO,
			abstractFinanzielleSituation2 != null ? abstractFinanzielleSituation2.getBruttovermoegen() : BigDecimal.ZERO);

		final BigDecimal totalSchulden = add(abstractFinanzielleSituation1 != null ? abstractFinanzielleSituation1.getSchulden() : BigDecimal.ZERO,
			abstractFinanzielleSituation2 != null ? abstractFinanzielleSituation2.getSchulden() : BigDecimal.ZERO);

		BigDecimal total = subtract(totalBruttovermoegen, totalSchulden);
		if (total.compareTo(BigDecimal.ZERO) < 0) {
			total = BigDecimal.ZERO;
		} //total vermoegen + schulden muss gruesser null sein, individuell pro gs kann es aber negativ sein
		total = percent(total, 5);
		return MathUtil.GANZZAHL.from(total);
	}

	protected static BigDecimal add(BigDecimal value1, BigDecimal value2) {
		value1 = value1 != null ? value1 : BigDecimal.ZERO;
		value2 = value2 != null ? value2 : BigDecimal.ZERO;
		return value1.add(value2);
	}

	private static BigDecimal subtract(BigDecimal value1, BigDecimal value2) {
		value1 = value1 != null ? value1 : BigDecimal.ZERO;
		value2 = value2 != null ? value2 : BigDecimal.ZERO;
		return value1.subtract(value2);
	}

	private static BigDecimal percent(BigDecimal value, int percent) {
		BigDecimal total = value != null ? value : BigDecimal.ZERO;
		total = total.multiply(new BigDecimal(String.valueOf(percent)));
		total = total.divide(new BigDecimal("100"), RoundingMode.HALF_UP);
		return total;
	}

	private BigDecimal calcEinkommen(AbstractFinanzielleSituation abstractFinanzielleSituation1, BigDecimal nettoJahresLohn1, BigDecimal geschaeftsgewinnDurchschnitt1,
		AbstractFinanzielleSituation abstractFinanzielleSituation2, BigDecimal nettoJahresLohn2, BigDecimal geschaeftsgewinnDurchschnitt2) {
		BigDecimal total = BigDecimal.ZERO;
		total = calcEinkommenProGS(abstractFinanzielleSituation1, nettoJahresLohn1, geschaeftsgewinnDurchschnitt1, total);
		total = calcEinkommenProGS(abstractFinanzielleSituation2, nettoJahresLohn2, geschaeftsgewinnDurchschnitt2, total);
		return total;
	}

	private BigDecimal calcEinkommenProGS(AbstractFinanzielleSituation abstractFinanzielleSituation, BigDecimal nettoJahresLohn,
		BigDecimal geschaeftsgewinnDurchschnitt, BigDecimal total) {
		if (abstractFinanzielleSituation != null) {
			total = add(total, nettoJahresLohn);
			total = add(total, abstractFinanzielleSituation.getFamilienzulage());
			total = add(total, abstractFinanzielleSituation.getErsatzeinkommen());
			total = add(total, abstractFinanzielleSituation.getErhalteneAlimente());
			total = add(total, geschaeftsgewinnDurchschnitt);
		}
		return total;
	}

	private BigDecimal calcAbzuege(AbstractFinanzielleSituation finanzielleSituationGS1, AbstractFinanzielleSituation finanzielleSituationGS2) {
		BigDecimal totalAbzuege = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			totalAbzuege = add(totalAbzuege, finanzielleSituationGS1.getGeleisteteAlimente());
		}
		if (finanzielleSituationGS2 != null) {
			totalAbzuege = add(totalAbzuege, finanzielleSituationGS2.getGeleisteteAlimente());
		}
		return totalAbzuege;
	}

	/**
	 * Berechnet die NettoJahresLohn fuer ein Einkommensverschlechterung
	 */
	private BigDecimal calculateNettoJahresLohn(Einkommensverschlechterung einkommensverschlechterung) {
		BigDecimal total = BigDecimal.ZERO;
		if (einkommensverschlechterung != null) {
			total = add(total, einkommensverschlechterung.getNettolohnJan());
			total = add(total, einkommensverschlechterung.getNettolohnFeb());
			total = add(total, einkommensverschlechterung.getNettolohnMrz());
			total = add(total, einkommensverschlechterung.getNettolohnApr());
			total = add(total, einkommensverschlechterung.getNettolohnMai());
			total = add(total, einkommensverschlechterung.getNettolohnJun());
			total = add(total, einkommensverschlechterung.getNettolohnJul());
			total = add(total, einkommensverschlechterung.getNettolohnAug());
			total = add(total, einkommensverschlechterung.getNettolohnSep());
			total = add(total, einkommensverschlechterung.getNettolohnOkt());
			total = add(total, einkommensverschlechterung.getNettolohnNov());
			total = add(total, einkommensverschlechterung.getNettolohnDez());
			total = add(total, einkommensverschlechterung.getNettolohnZus());
		}
		return total;
	}

	/**
	 * Berechnet die NettoJahresLohn fuer eine Finanzielle Situation
	 */
	@Nullable
	private BigDecimal calculateNettoJahresLohn(@Nullable FinanzielleSituation finanzielleSituation) {
		if (finanzielleSituation != null) {
			return finanzielleSituation.getNettolohn();
		}
		return BigDecimal.ZERO;
	}

	@Nullable
	private Einkommensverschlechterung getEinkommensverschlechterungGS(@Nullable GesuchstellerContainer gesuchsteller, int basisJahrPlus) {
		if (gesuchsteller != null && gesuchsteller.getEinkommensverschlechterungContainer() != null) {
			if (basisJahrPlus == 2) {
				return gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2();
			} else {
				return gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1();
			}
		}
		return null;
	}

	@Nullable
	private FinanzielleSituation getFinanzielleSituationGS(@Nullable GesuchstellerContainer gesuchsteller) {
		if (gesuchsteller != null && gesuchsteller.getFinanzielleSituationContainer() != null) {
			return gesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA();
		}
		return null;
	}

}
