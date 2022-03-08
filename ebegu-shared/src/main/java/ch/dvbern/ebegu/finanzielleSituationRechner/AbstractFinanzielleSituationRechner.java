/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Abstract Class basiert auf dem Berner Finanzielle Situation Berechnung
 */
public abstract class AbstractFinanzielleSituationRechner {

	/**
	 * Berechnet das FinazDaten DTO fuer die Finanzielle Situation
	 *
	 * @param gesuch das Gesuch dessen finazDatenDTO gesetzt werden soll
	 */
	public void calculateFinanzDaten(@Nonnull Gesuch gesuch, BigDecimal minimumEKV) {
		FinanzDatenDTO finanzDatenDTOAlleine = new FinanzDatenDTO();
		FinanzDatenDTO finanzDatenDTOZuZweit = new FinanzDatenDTO();

		// Finanzielle Situation berechnen
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOAlleine =
			calculateResultateFinanzielleSituation(gesuch, false);
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOZuZweit =
			calculateResultateFinanzielleSituation(gesuch, true);

		finanzDatenDTOAlleine.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr());
		finanzDatenDTOZuZweit.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr());

		//Berechnung wird nur ausgefuehrt wenn Daten vorhanden, wenn es keine gibt machen wir nichts
		EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
		if (ekvInfo != null && ekvInfo.getEinkommensverschlechterung()) {
			FinanzielleSituationResultateDTO resultateEKV1Alleine =
				calculateResultateEinkommensverschlechterung(gesuch, 1, false);
			FinanzielleSituationResultateDTO resultateEKV1ZuZweit =
				calculateResultateEinkommensverschlechterung(gesuch, 1, true);
			BigDecimal massgebendesEinkommenBasisjahrAlleine =
				finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr();
			BigDecimal massgebendesEinkommenBasisjahrZuZweit =
				finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr();

			if (ekvInfo.getEkvFuerBasisJahrPlus1() != null && ekvInfo.getEkvFuerBasisJahrPlus1()) {
				finanzDatenDTOAlleine.setEkv1Erfasst(true);
				finanzDatenDTOZuZweit.setEkv1Erfasst(true);
				if (ekvInfo.getEkvBasisJahrPlus1Annulliert()) {
					finanzDatenDTOAlleine.setEkv1Annulliert(Boolean.TRUE);
					finanzDatenDTOZuZweit.setEkv1Annulliert(Boolean.TRUE);
				}
				// In der EKV 1 vergleichen wir immer mit dem Basisjahr
				handleEKV1(finanzDatenDTOAlleine, resultateEKV1Alleine.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrAlleine, minimumEKV);
				handleEKV1(finanzDatenDTOZuZweit, resultateEKV1ZuZweit.getMassgebendesEinkVorAbzFamGr(),
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
				finanzDatenDTOAlleine.setEkv2Erfasst(true);
				finanzDatenDTOZuZweit.setEkv2Erfasst(true);
				if (ekvInfo.getEkvBasisJahrPlus2Annulliert()) {
					finanzDatenDTOAlleine.setEkv2Annulliert(Boolean.TRUE);
					finanzDatenDTOZuZweit.setEkv2Annulliert(Boolean.TRUE);
				}
				FinanzielleSituationResultateDTO resultateEKV2Alleine =
					calculateResultateEinkommensverschlechterung(gesuch, 2, false);
				FinanzielleSituationResultateDTO resultateEKV2ZuZweit =
					calculateResultateEinkommensverschlechterung(gesuch, 2, true);
				// In der EKV 2 vergleichen wir immer mit dem Basisjahr
				handleEKV2(finanzDatenDTOAlleine,
					resultateEKV2Alleine.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrAlleine,
					minimumEKV);
				handleEKV2(finanzDatenDTOZuZweit,
					resultateEKV2ZuZweit.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrZuZweit,
					minimumEKV);
			} else {
				finanzDatenDTOAlleine.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahrAlleine);
				finanzDatenDTOZuZweit.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahrZuZweit);
			}
		}
		gesuch.setFinanzDatenDTO_alleine(finanzDatenDTOAlleine);
		gesuch.setFinanzDatenDTO_zuZweit(finanzDatenDTOZuZweit);
	}

	@Nonnull
	public FinanzielleSituationResultateDTO calculateResultateFinanzielleSituation(
		@Nonnull Gesuch gesuch,
		boolean hasSecondGesuchsteller) {

		final FinanzielleSituationResultateDTO finSitResultDTO = new FinanzielleSituationResultateDTO();
		setFinanzielleSituationParameters(gesuch, finSitResultDTO, hasSecondGesuchsteller);

		return finSitResultDTO;
	}

	/**
	 * Diese Methode wird momentan im Print gebraucht um die Finanzielle Situation zu berechnen. Der Abzug aufgrund
	 * Familiengroesse wird
	 * hier auf 0 gesetzt
	 */

	@Nonnull
	public FinanzielleSituationResultateDTO calculateResultateEinkommensverschlechterung(
		@Nonnull Gesuch gesuch,
		int basisJahrPlus,
		boolean hasSecondGesuchsteller) {
		Objects.requireNonNull(gesuch.extractEinkommensverschlechterungInfo());

		final FinanzielleSituationResultateDTO einkVerResultDTO = new FinanzielleSituationResultateDTO();
		setEinkommensverschlechterungParameters(gesuch, basisJahrPlus, einkVerResultDTO, hasSecondGesuchsteller);

		return einkVerResultDTO;
	}

	/**
	 * Nimmt das uebergebene FinanzielleSituationResultateDTO und mit den Daten vom Gesuch, berechnet alle im
	 * FinanzielleSituationResultateDTO benoetigten Daten und setzt sie direkt im dto.
	 */
	public abstract void setFinanzielleSituationParameters(
		@Nonnull Gesuch gesuch,
		final FinanzielleSituationResultateDTO finSitResultDTO,
		boolean hasSecondGesuchsteller);

	/**
	 * Nimmt das uebergebene FinanzielleSituationResultateDTO und mit den Daten vom Gesuch, berechnet alle im
	 * FinanzielleSituationResultateDTO benoetigten Daten.
	 */
	public abstract void setEinkommensverschlechterungParameters(
		@Nonnull Gesuch gesuch, int basisJahrPlus,
		final FinanzielleSituationResultateDTO einkVerResultDTO, boolean hasSecondGesuchsteller);

	protected void handleEKV1(
		@Nonnull FinanzDatenDTO finanzDatenDTO,
		BigDecimal massgebendesEinkommenEKV1,
		BigDecimal massgebendesEinkommenBasisjahr,
		BigDecimal minimumEKV
	) {
		// In der EKV 1 vergleichen wir immer mit dem Basisjahr
		finanzDatenDTO.setEkv1Erfasst(true);
		boolean accepted = acceptEKV(massgebendesEinkommenBasisjahr, massgebendesEinkommenEKV1, minimumEKV);
		finanzDatenDTO.setEkv1Accepted(accepted);
		if (accepted) {
			finanzDatenDTO.setMassgebendesEinkBjP1VorAbzFamGr(massgebendesEinkommenEKV1);
		} else {
			finanzDatenDTO.setMassgebendesEinkBjP1VorAbzFamGr(massgebendesEinkommenBasisjahr);
		}
	}

	protected void handleEKV2(
		@Nonnull FinanzDatenDTO finanzDatenDTO,
		BigDecimal massgebendesEinkommenEKV2,
		BigDecimal massgebendesEinkommenBasisjahr,
		BigDecimal minimumEKV
	) {
		// In der EKV 2 vergleichen wir immer mit dem Basisjahr. Egal ob eine EKV 1 vorhanden ist
		finanzDatenDTO.setEkv2Erfasst(true);
		boolean accepted = acceptEKV(
			massgebendesEinkommenBasisjahr,
			massgebendesEinkommenEKV2,
			minimumEKV);
		finanzDatenDTO.setEkv2Accepted(accepted);
		if (accepted) {
			finanzDatenDTO.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenEKV2);
		} else {
			finanzDatenDTO.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenBasisjahr);
		}
	}

	/**
	 * @return Berechnet ob die Einkommensverschlechterung mehr als 20 % gegenueber dem vorjahr betraegt, gibt true
	 * zurueckk wen ja; false sonst
	 */
	public boolean acceptEKV(
		BigDecimal massgebendesEinkommenBasisjahr,
		BigDecimal massgebendesEinkommenJahr,
		BigDecimal minimumEKV) {

		boolean result = massgebendesEinkommenBasisjahr.compareTo(BigDecimal.ZERO) > 0;
		if (result) {
			BigDecimal differenzGerundet = getCalculatedProzentualeDifferenzRounded(massgebendesEinkommenBasisjahr, massgebendesEinkommenJahr);
			// -19.999 => -19 (nicht akzeptiert)
			// -20.000 => -20 (akzeptiert)
			// -20.001 => -20 (akzeptiert)
			return differenzGerundet.compareTo(minimumEKV.negate()) <= 0;
		}
		return false;
	}

	@Nonnull
	public static BigDecimal getCalculatedProzentualeDifferenzRounded(@Nullable BigDecimal einkommenJahr, @Nullable BigDecimal einkommenJahrPlus1) {
		BigDecimal resultExact = AbstractFinanzielleSituationRechner.calculateProzentualeDifferenz(einkommenJahr, einkommenJahrPlus1);
		double doubleValue = resultExact.doubleValue();
		double resultFloor = Math.ceil(doubleValue);
		return MathUtil.GANZZAHL.from(resultFloor);
	}

	@Nonnull
	public static BigDecimal calculateProzentualeDifferenz(@Nullable BigDecimal einkommenJahr, @Nullable BigDecimal einkommenJahrPlus1) {
		BigDecimal HUNDERT = MathUtil.EXACT.from(100);
		if (einkommenJahr == null && einkommenJahrPlus1 == null) {
			return BigDecimal.ZERO;
		}
		if (einkommenJahr == null) {
			return HUNDERT;
		}
		if (einkommenJahrPlus1 == null) {
			return HUNDERT.negate();
		}
		boolean jahrZero = einkommenJahr.compareTo(BigDecimal.ZERO) <= 0;
		boolean jahrPlus1Zero = einkommenJahrPlus1.compareTo(BigDecimal.ZERO) <= 0;
		if (jahrZero && jahrPlus1Zero) {
			return BigDecimal.ZERO;
		}
		if (jahrPlus1Zero) {
			return HUNDERT.negate();
		}
		if (jahrZero) {
			return HUNDERT;
		}
		BigDecimal divide = MathUtil.EXACT.divide(einkommenJahrPlus1, einkommenJahr);
		divide = MathUtil.EXACT.multiply(divide, HUNDERT);
		return  MathUtil.EXACT.subtract(HUNDERT, divide).negate();
	}

	protected void calculateZusammen(
		@Nonnull final FinanzielleSituationResultateDTO finSitResultDTO,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable BigDecimal geschaeftsgewinnDurchschnitt1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2,
		@Nullable BigDecimal geschaeftsgewinnDurchschnitt2) {

		finSitResultDTO.setEinkommenBeiderGesuchsteller(calcEinkommen(
			finanzielleSituationGS1, geschaeftsgewinnDurchschnitt1,
			finanzielleSituationGS2, geschaeftsgewinnDurchschnitt2));
		finSitResultDTO.setNettovermoegenXProzent(calcVermoegen5Prozent(
			finanzielleSituationGS1,
			finanzielleSituationGS2));
			finSitResultDTO.setAbzuegeBeiderGesuchsteller(calcAbzuege(finanzielleSituationGS1, finanzielleSituationGS2));

		finSitResultDTO.setAnrechenbaresEinkommen(add(
			finSitResultDTO.getEinkommenBeiderGesuchsteller(),
			finSitResultDTO.getNettovermoegenXProzent()));
		finSitResultDTO.setMassgebendesEinkVorAbzFamGr(
			MathUtil.positiveNonNullAndRound(
				subtract(
					finSitResultDTO.getAnrechenbaresEinkommen(),
					finSitResultDTO.getAbzuegeBeiderGesuchsteller())));
	}

	/**
	 * Diese Methode aufrufen um den GeschaeftsgewinnDurchschnitt fuer die Finanzielle Situation zu berechnen.
	 */
	@Nullable
	public static BigDecimal calcGeschaeftsgewinnDurchschnitt(@Nullable FinanzielleSituation finanzielleSituation) {
		if (finanzielleSituation != null) {
			return calcGeschaeftsgewinnDurchschnitt(
				finanzielleSituation.getGeschaeftsgewinnBasisjahr(),
				finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1(),
				finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2());
		}
		return null;
	}

	/**
	 * Diese Methode aufrufen um den GeschaeftsgewinnDurchschnitt fuer die Einkommensverschlechterung zu berechnen.
	 * Die finanzielle Situation
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
				return calcGeschaeftsgewinnDurchschnitt(
					einkVersBjp1.getGeschaeftsgewinnBasisjahr(),
					finanzielleSituation.getGeschaeftsgewinnBasisjahr(),
					finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1());
			}
		} else if (basisJahrPlus == 2 && finanzielleSituation != null && einkVersBjp2 != null) {
			if (ekvi != null && ekvi.getEkvFuerBasisJahrPlus1() && einkVersBjp1 != null) {
				return calcGeschaeftsgewinnDurchschnitt(
					einkVersBjp2.getGeschaeftsgewinnBasisjahr(),
					einkVersBjp1.getGeschaeftsgewinnBasisjahr(),
					finanzielleSituation.getGeschaeftsgewinnBasisjahr());
			} else {
				return calcGeschaeftsgewinnDurchschnitt(
					einkVersBjp2.getGeschaeftsgewinnBasisjahr(),
					einkVersBjp2.getGeschaeftsgewinnBasisjahrMinus1(),
					finanzielleSituation.getGeschaeftsgewinnBasisjahr());
			}
		}
		return null;
	}

	/**
	 * Allgemeine Methode fuer die Berechnung des GeschaeftsgewinnDurchschnitt. Die drei benoetigten Felder werden
	 * uebergeben
	 */
	@Nullable
	public static BigDecimal calcGeschaeftsgewinnDurchschnitt(
		@Nullable final BigDecimal geschaeftsgewinnBasisjahr,
		@Nullable final BigDecimal geschaeftsgewinnBasisjahrMinus1,
		@Nullable final BigDecimal geschaeftsgewinnBasisjahrMinus2) {
		if (geschaeftsgewinnBasisjahr == null) {
			return BigDecimal.ZERO;
		}
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

		total = total.add(geschaeftsgewinnBasisjahr);
		anzahlJahre = anzahlJahre.add(BigDecimal.ONE);

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
		@Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2) {
		BigDecimal totalBruttovermoegen = BigDecimal.ZERO;
		BigDecimal totalSchulden = BigDecimal.ZERO;
		BigDecimal nettovermoegen = BigDecimal.ZERO;
		if(gs1 != null && Boolean.TRUE.equals(gs1.getSteuerdatenZugriff())) {
			nettovermoegen = gs1.getNettoVermoegen();
		}
		else {
			totalBruttovermoegen = gs1 != null ? gs1.getBruttovermoegen() : BigDecimal.ZERO;
			totalSchulden = gs1 != null ? gs1.getSchulden() : BigDecimal.ZERO;
		}
		if(gs2 != null && Boolean.TRUE.equals(gs2.getSteuerdatenZugriff())) {
			nettovermoegen = add(nettovermoegen, gs2.getNettoVermoegen());
		}
		else {
			totalBruttovermoegen = add(totalBruttovermoegen, gs2 != null ? gs2.getBruttovermoegen() : BigDecimal.ZERO);
			totalSchulden = add(totalSchulden, gs2 != null ? gs2.getSchulden() : BigDecimal.ZERO);
		}

		BigDecimal total = subtract(totalBruttovermoegen, totalSchulden);
		total = add(total, nettovermoegen);
		if (total.compareTo(BigDecimal.ZERO) < 0) {
			total = BigDecimal.ZERO;
		} //total vermoegen + schulden muss gruesser null sein, individuell pro gs kann es aber negativ sein
		total = percent(total, 5);
		return MathUtil.GANZZAHL.from(total);
	}

	@Nonnull
	public BigDecimal calcTotalEinkommen(
		@Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2) {

		return MathUtil.DEFAULT.addNullSafe(
			BigDecimal.ZERO,
			gs1 != null ? getZwischentotalEinkommen(gs1) : BigDecimal.ZERO,
			gs2 != null ? getZwischentotalEinkommen(gs2) : BigDecimal.ZERO);
	}

	@Nonnull
	public BigDecimal calcTotalVermoegen(
		@Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2) {

		return MathUtil.DEFAULT.addNullSafe(
			BigDecimal.ZERO,
			gs1 != null ? getZwischentotalVermoegen(gs1) : BigDecimal.ZERO,
			gs2 != null ? getZwischentotalVermoegen(gs2) : BigDecimal.ZERO);
	}

	public BigDecimal calcMassgebendesEinkommenVorAbzugFamiliengroesse(
		@Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2) {

		BigDecimal totalEinkommen = MathUtil.DEFAULT.addNullSafe(
			BigDecimal.ZERO,
			calcTotalEinkommen(gs1, gs2),
			calcVermoegen5Prozent(gs1, gs2));

		return MathUtil.DEFAULT.subtract(
			totalEinkommen,
			calcAbzuege(gs1, gs2));
	}

	@Nonnull
	public final BigDecimal getZwischentotalEinkommen(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		BigDecimal einkommenZwischentotal = calcEinkommenProGS(
			abstractFinanzielleSituation,
			abstractFinanzielleSituation.getDurchschnittlicherGeschaeftsgewinn(),
			BigDecimal.ZERO);

		return einkommenZwischentotal != null ? einkommenZwischentotal : BigDecimal.ZERO;
	}

	@Nonnull
	public BigDecimal getZwischentotalVermoegen(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		BigDecimal vermoegenPlus = MathUtil.DEFAULT.addNullSafe(BigDecimal.ZERO, abstractFinanzielleSituation.getBruttovermoegen());
		return MathUtil.DEFAULT.subtractNullSafe(vermoegenPlus, abstractFinanzielleSituation.getSchulden());
	}

	@Nonnull
	public BigDecimal getZwischetotalAbzuege(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		return MathUtil.DEFAULT.addNullSafe(BigDecimal.ZERO, abstractFinanzielleSituation.getGeleisteteAlimente());
	}

	protected static BigDecimal add(@Nullable BigDecimal value1, @Nullable BigDecimal value2) {
		value1 = value1 != null ? value1 : BigDecimal.ZERO;
		value2 = value2 != null ? value2 : BigDecimal.ZERO;
		return value1.add(value2);
	}

	protected static BigDecimal subtract(@Nullable BigDecimal value1, @Nullable BigDecimal value2) {
		value1 = value1 != null ? value1 : BigDecimal.ZERO;
		value2 = value2 != null ? value2 : BigDecimal.ZERO;
		return value1.subtract(value2);
	}

	protected static BigDecimal percent(@Nullable BigDecimal value, int percent) {
		BigDecimal total = value != null ? value : BigDecimal.ZERO;
		total = total.multiply(new BigDecimal(String.valueOf(percent)));
		total = total.divide(new BigDecimal("100"), RoundingMode.HALF_UP);
		return total;
	}

	@Nullable
	private BigDecimal calcEinkommen(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation1,
		@Nullable BigDecimal geschaeftsgewinnDurchschnitt1,
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation2,
		@Nullable BigDecimal geschaeftsgewinnDurchschnitt2
	) {
		BigDecimal total = BigDecimal.ZERO;
		total = calcEinkommenProGS(abstractFinanzielleSituation1, geschaeftsgewinnDurchschnitt1, total);
		total = calcEinkommenProGS(abstractFinanzielleSituation2, geschaeftsgewinnDurchschnitt2, total);
		return total;
	}

	@Nullable
	protected BigDecimal calcEinkommenProGS(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation,
		@Nullable BigDecimal geschaeftsgewinnDurchschnitt,
		@Nullable BigDecimal total
	) {
		if (abstractFinanzielleSituation != null) {
			total = add(total, abstractFinanzielleSituation.getNettolohn());
			total = add(total, abstractFinanzielleSituation.getFamilienzulage());
			total = add(total, abstractFinanzielleSituation.getErsatzeinkommen());
			total = add(total, abstractFinanzielleSituation.getErhalteneAlimente());
			total = add(total, geschaeftsgewinnDurchschnitt);
		}
		return total;
	}

	public BigDecimal calcAbzuege(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2
	) {
		BigDecimal totalAbzuege = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			totalAbzuege = add(totalAbzuege, finanzielleSituationGS1.getGeleisteteAlimente());
		}
		if (finanzielleSituationGS2 != null) {
			totalAbzuege = add(totalAbzuege, finanzielleSituationGS2.getGeleisteteAlimente());
		}
		return totalAbzuege;
	}

	@Nullable
	protected Einkommensverschlechterung getEinkommensverschlechterungGS(
		@Nullable GesuchstellerContainer gesuchsteller,
		int basisJahrPlus) {
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
	protected FinanzielleSituation getFinanzielleSituationGS(@Nullable GesuchstellerContainer gesuchsteller) {
		if (gesuchsteller != null && gesuchsteller.getFinanzielleSituationContainer() != null) {
			return gesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA();
		}
		return null;
	}

	public abstract boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation);
}
