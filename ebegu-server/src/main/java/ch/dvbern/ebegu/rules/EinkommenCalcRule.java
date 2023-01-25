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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

/**
 * Setzt fuer die Zeitabschnitte das Massgebende Einkommen. Sollte der Maximalwert uebschritte werden so wird das Pensum auf 0 gesetzt
 * ACHTUNG: Diese Regel gilt nur fuer Kita und Tageseltern Kleinkinder.  Bei Tageseltern Schulkinder und Tagesstaetten
 * gibt es keine Reduktion des Anspruchs, sie bezahlen aber den Volltarif
 * Regel 16.7 Maximales Einkommen
 */
public class EinkommenCalcRule extends AbstractCalcRule {

	private final BigDecimal maximalesEinkommen;
	private final Boolean pauschalBeiAnspruch;
	@Nullable private final BigDecimal maxEinkommenEKV;

	public EinkommenCalcRule(
		DateRange validityPeriod,
		BigDecimal maximalesEinkommen,
		@Nullable BigDecimal maxEinkommenEKV,
		Boolean pauschalBeiAnspruch,
		@Nonnull Locale locale
	) {
		super(RuleKey.EINKOMMEN, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
		this.maximalesEinkommen = maximalesEinkommen;
		this.pauschalBeiAnspruch = pauschalBeiAnspruch;
		this.maxEinkommenEKV = maxEinkommenEKV;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		// Es gibt zwei Faelle, in denen die Finanzielle Situation nicht bekannt ist:
		// - Sozialhilfeempfaenger: Wir rechnen mit Einkommen = 0
		// - Keine Vergünstigung gewünscht / FinSit abgelehnt: Wir rechnen mit dem Maximalen Einkommen

		// Sonderfall Keine Verguenstigung gewuenscht oder FinSit abgelehnt
		// - Wir rechnen mit dem Max. Einkommen
		// - Der Anspruch wird auf 0 gesetzt, AUSSER das Kind hat erweiterte Beduerfnisse
		// - "Bezahlt Vollkosten" darf nur gesetzt werden, wenn KEINE erweiterten Beduerfnisse

 		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();

		boolean sozialhilfeEmpfaenger = familiensituation != null && Boolean.TRUE.equals(familiensituation.getSozialhilfeBezueger());
		int basisjahr = platz.extractGesuchsperiode().getBasisJahr();

		if (sozialhilfeEmpfaenger) {
			inputData.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.ZERO);
			inputData.setAbzugFamGroesse(BigDecimal.ZERO);
			inputData.setEinkommensjahr(basisjahr);
			inputData.addBemerkung(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG, getLocale());
			return;
		}

		boolean keineFinSitErfasst = familiensituation != null && Boolean.FALSE.equals(familiensituation.getVerguenstigungGewuenscht());
		// FinSit abgelehnt muss nur bei Erstgesuch beachtet werden. In einer Mutation wird es im Mutationsmerger abgehandelt
		boolean finSitAbgelehnt = FinSitStatus.ABGELEHNT == platz.extractGesuch().getFinSitStatus()
			&& platz.extractGesuch().getTyp().isGesuch();
		boolean hasErweiterteBetreuung = hasErweiterteBetreuung(platz);

		// Die Finanzdaten berechnen
		FinanzDatenDTO finanzDatenDTO;
		if (inputData.isHasSecondGesuchstellerForFinanzielleSituation()) {
			finanzDatenDTO = platz.extractGesuch().getFinanzDatenDTO_zuZweit();
			setMassgebendesEinkommen(
				inputData.isEkv1ZuZweit(),
				inputData.isEkv2ZuZweit(),
				finanzDatenDTO,
				inputData,
				platz,
				getLocale());
		} else {
			finanzDatenDTO = platz.extractGesuch().getFinanzDatenDTO_alleine();
			setMassgebendesEinkommen(
				inputData.isEkv1Alleine(),
				inputData.isEkv2Alleine(),
				finanzDatenDTO,
				inputData,
				platz,
				getLocale());
		}

		// Keine FinSit erfasst oder FinSit abgelehnt, aber auch nicht Sozialhilfeempfaenger -> Bezahlt Vollkosten
		if (keineFinSitErfasst || finSitAbgelehnt) {
			// Wenn die FinSit nicht ausgefuellt oder nicht akzeptiert wurde, setzen wir das MaxEinkommen
			// Es wird auch automatisch das Basisjahr gesetzt, da in einem solchen Fall keine EKV akzeptiert wird.
			inputData.setMassgebendesEinkommenVorAbzugFamgr(maximalesEinkommen);
			inputData.setAbzugFamGroesse(BigDecimal.ZERO);
			inputData.setEinkommensjahr(basisjahr);
		}

		// Erst jetzt kann das Maximale Einkommen geprueft werden!
		if (keineFinSitErfasst || finSitAbgelehnt || inputData.getMassgebendesEinkommen().compareTo(maximalesEinkommen) >= 0) {
			//maximales einkommen wurde ueberschritten
			inputData.setKategorieMaxEinkommen(true);
			inputData.setKeinAnspruchAufgrundEinkommen(true);
			if (!hasErweiterteBetreuung || pauschalBeiAnspruch) {
				// Darf nur gesetzt werden, wenn KEINE erweiterten Beduerfnisse, da sonst der Zuschlag nicht ausbezahlt wird!
				inputData.setBezahltVollkostenKomplett();
				if (platz.getBetreuungsangebotTyp().isJugendamt()) {
					// Falls MaxEinkommen, aber kein Zuschlag fuer erweiterte Betreuung -> Anspruch wird auf 0 gesetzt
					// Wenn das Kind erweiterte Beduerfnisse hat, bleibt der Anspruch bestehen
					// Bei Tagesschule muss der Anspruch immer 100 bleiben, siehe BetreuungsangebotTypCalcRule
					inputData.setAnspruchZeroAndSaveRestanspruch();
				}
				if (hasErweiterteBetreuung) {
					inputData.addBemerkung(MsgKey.KEINE_ERWEITERTE_BEDUERFNISSE_MSG, getLocale());
				}
			}
			// Bemerkungen setzen, je nach Grund des Max-Einkommens
			if (keineFinSitErfasst) {
				inputData.addBemerkung(MsgKey.EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG, getLocale());
			} else if (finSitAbgelehnt) {
				inputData.addBemerkung(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG, getLocale());
			} else {
				inputData.addBemerkung(MsgKey.EINKOMMEN_MAX_MSG, getLocale(), NumberFormat.getInstance().format(maximalesEinkommen));
			}
		}
	}

	/**
	 * Gibt zurueck, ob fuer diesen Platz eine erweiterte Betreuung besteht.
	 * Fuer Tagesschulen immer false!
	 */
	private boolean hasErweiterteBetreuung(@Nonnull AbstractPlatz platz) {
		if (platz.getBetreuungsangebotTyp().isJugendamt()) {
			Betreuung betreuung = (Betreuung) platz;
			return Boolean.TRUE.equals(betreuung.hasErweiterteBetreuung());
		}
		return false;
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private void setMassgebendesEinkommen(
		boolean isEkv1,
		boolean isEkv2,
		FinanzDatenDTO finanzDatenDTO,
		@Nonnull BGCalculationInput inputData,
		AbstractPlatz betreuung,
		@Nonnull Locale locale
	) {
		int basisjahr = betreuung.extractGesuchsperiode().getBasisJahr();
		int basisjahrPlus1 = betreuung.extractGesuchsperiode().getBasisJahrPlus1();
		int basisjahrPlus2 = betreuung.extractGesuchsperiode().getBasisJahrPlus2();

		if (isEkv1) {
			boolean isEkv1DifferenceMoreThan20 = finanzDatenDTO.isEkv1Accepted();
			boolean isEkv1Annuliert = finanzDatenDTO.isEkv1Annulliert();

			handleEKV(
					finanzDatenDTO,
					inputData,
					locale,
					basisjahr,
					basisjahrPlus1,
					isEkv1DifferenceMoreThan20,
					isEkv1Annuliert);

		} else if (isEkv2) {
			boolean isEkv2DifferenceMoreThan20 = finanzDatenDTO.isEkv2Accepted();
			boolean isEkv2Annuliert = finanzDatenDTO.isEkv2Annulliert();
			handleEKV(
					finanzDatenDTO,
					inputData,
					locale,
					basisjahr,
					basisjahrPlus2,
					isEkv2DifferenceMoreThan20,
					isEkv2Annuliert);

		} else {
			inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
			inputData.setEinkommensjahr(basisjahr);
		}
	}

	private void handleEKV(
			FinanzDatenDTO finanzDatenDTO,
			@Nonnull BGCalculationInput inputData,
			@Nonnull Locale locale,
			int basisjahr,
			int ekvJahr,
			boolean isEkvDifferenceMoreThan20,
			boolean isEkvAnnuliert) {
		boolean isMassgebendesEinkommenTooHighForEKV =
				checkMassgebendesEinkommenNachAbzugTooHighForEKV(inputData, finanzDatenDTO);

		if (isEkvAnnuliert) {
			setEKVAnnuliertDataAndMessage(finanzDatenDTO, inputData, locale, basisjahr, ekvJahr);
			return;
		}

		if (!isEkvDifferenceMoreThan20) {
			setEKVDifferenceTooLowDataAndMessage(finanzDatenDTO, inputData, locale, basisjahr, ekvJahr);
			return;
		}

		if (isMassgebendesEinkommenTooHighForEKV) {
			ignoreEKVAndSetMessage(finanzDatenDTO, inputData, basisjahr);
		} else {
			acceptEKVAndSetMessage(finanzDatenDTO, inputData, locale, basisjahr, ekvJahr);
		}

	}

	private static void setEKVAnnuliertDataAndMessage(
			FinanzDatenDTO finanzDatenDTO,
			BGCalculationInput inputData,
			Locale locale,
			int basisjahr,
			int ekvJahr) {
		inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
		inputData.setEinkommensjahr(basisjahr);
		inputData.addBemerkung(
				MsgKey.EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG,
				locale,
				String.valueOf(ekvJahr)
		);
	}

	private void setEKVDifferenceTooLowDataAndMessage(
			FinanzDatenDTO finanzDatenDTO,
			BGCalculationInput inputData,
			Locale locale,
			int basisjahr,
			int ekvJahr) {
		boolean isMassgebendesEinkommenTooHighForEKV =
				checkMassgebendesEinkommenNachAbzugTooHighForEKV(inputData, finanzDatenDTO);

		inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
		inputData.setEinkommensjahr(basisjahr);
		inputData.addBemerkung(
				MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG,
				locale,
				String.valueOf(ekvJahr),
				String.valueOf(finanzDatenDTO.getMinEKV()),
				String.valueOf(basisjahr));
		if (isMassgebendesEinkommenTooHighForEKV) {
			inputData.addBemerkung(
					MsgKey.EINKOMMEN_TOO_HIGH_FOR_EKV,
					getLocale(),
					String.valueOf(basisjahr),
					NumberFormat.getInstance().format(this.maxEinkommenEKV)
			);
		}
	}

	private void ignoreEKVAndSetMessage(
			@Nonnull FinanzDatenDTO finanzDatenDTO,
			@Nonnull BGCalculationInput inputData,
			int basisjahr
	) {
		inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
		inputData.setEinkommensjahr(basisjahr);
		inputData.addBemerkung(
				MsgKey.EINKOMMEN_TOO_HIGH_FOR_EKV,
				getLocale(),
				String.valueOf(basisjahr),
				NumberFormat.getInstance().format(this.maxEinkommenEKV)
		);
	}

	private static void acceptEKVAndSetMessage(
			FinanzDatenDTO finanzDatenDTO,
			BGCalculationInput inputData,
			Locale locale,
			int basisjahr,
			int ekvJahr) {
		if (ekvJahr == basisjahr + 1) {
			inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjP1VorAbzFamGr());
		} else {
			inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjP2VorAbzFamGr());
		}
		inputData.setEinkommensjahr(ekvJahr);
		inputData.addBemerkung(
				MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG,
				locale,
				String.valueOf(ekvJahr),
				String.valueOf(finanzDatenDTO.getMinEKV()),
				String.valueOf(basisjahr));
	}

	private boolean checkMassgebendesEinkommenNachAbzugTooHighForEKV(
		@Nonnull BGCalculationInput inputData,
		@Nonnull FinanzDatenDTO finanzDatenDTO
	) {
		// rule not active
		if (this.maxEinkommenEKV == null) {
			return false;
		}
		// abzug is null if familienAbzugAbschnittRule not active. In this case, there is no familienabzug
		var abzug = (inputData.getAbzugFamGroesse() == null)
			? BigDecimal.ZERO
			: inputData.getAbzugFamGroesse();
		return finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr()
			.subtract(abzug)
			.compareTo(this.maxEinkommenEKV) > 0;
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
