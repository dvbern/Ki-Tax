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
import static java.util.Objects.requireNonNull;

/**
 * Setzt fuer die Zeitabschnitte das Massgebende Einkommen. Sollte der Maximalwert uebschritte werden so wird das Pensum auf 0 gesetzt
 * ACHTUNG: Diese Regel gilt nur fuer Kita und Tageseltern Kleinkinder.  Bei Tageseltern Schulkinder und Tagesstaetten
 * gibt es keine Reduktion des Anspruchs, sie bezahlen aber den Volltarif
 * Regel 16.7 Maximales Einkommen
 */
public class EinkommenCalcRule extends AbstractCalcRule {

	private final BigDecimal maximalesEinkommen;

	public EinkommenCalcRule(
		DateRange validityPeriod,
		BigDecimal maximalesEinkommen,
		@Nonnull Locale locale
	) {
		super(RuleKey.EINKOMMEN, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
		this.maximalesEinkommen = maximalesEinkommen;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData) {

		// TODO (hefr) Hier drin kann nur der Gesuch-Fall abgehandelt werden, die Mutation muss im MutationsMerger behandelt werden

		// Es gibt zwei Faelle, in denen die Finanzielle Situation nicht bekannt ist:
		// - Sozialhilfeempfaenger: Wir rechnen mit Einkommen = 0
		// - Keine Vergünstigung gewünscht: Wir rechnen mit dem Maximalen Einkommen

		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();
		boolean keineFinSitErfasst = false;
		boolean finSitAbgelehnt = FinSitStatus.ABGELEHNT == platz.extractGesuch().getFinSitStatus();
		if (familiensituation != null) {
			keineFinSitErfasst = Boolean.FALSE.equals(familiensituation.getVerguenstigungGewuenscht());
			int basisjahr = platz.extractGesuchsperiode().getBasisJahr();
			if (Boolean.TRUE.equals(familiensituation.getSozialhilfeBezueger())) {
				inputData.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.ZERO);
				inputData.setAbzugFamGroesse(BigDecimal.ZERO);
				inputData.setEinkommensjahr(basisjahr);
				inputData.addBemerkung(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG, getLocale());
				return;
			}
			// Keine FinSit erfasst, aber auch nicht Sozialhilfeempfaenger -> Bezahlt Vollkosten
			if (keineFinSitErfasst || finSitAbgelehnt) {
				inputData.setBezahltVollkosten(true);
				inputData.setMassgebendesEinkommenVorAbzugFamgr(maximalesEinkommen);
			}
			// keine FinSit erfasst wurde, aber ein Anspruch auf die Pauschale besteht, gehen wir von Maximalem Einkommen
			// aus. Da Anspruch auf die Pauschale besteht, wird das Anspruchberechtigte Pensum nicht auf 0 gesetzt!
			// Dies betrifft nur Betreuungsgutscheine
			if (platz.getBetreuungsangebotTyp().isJugendamt()) {
				Betreuung betreuung = (Betreuung) platz;
				if ((keineFinSitErfasst || finSitAbgelehnt) && Boolean.TRUE.equals(betreuung.hasErweiterteBetreuung())) {
					inputData.setAbzugFamGroesse(BigDecimal.ZERO);
					inputData.setEinkommensjahr(basisjahr);
					if (keineFinSitErfasst) {
						// TODO (hefr) hier kommt bisher die "normale" Max-Einkommen Bemerkung. Soll das so sein???
						inputData.addBemerkung(MsgKey.EINKOMMEN_MAX_MSG, getLocale(), NumberFormat.getInstance().format(maximalesEinkommen));
					} else {
						inputData.addBemerkung(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG, getLocale());
					}
					return;
				}
			}
		}

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

		// Erst jetzt kann das Maximale Einkommen geprueft werden!
		if (requireNonNull(platz.getBetreuungsangebotTyp()).isJugendamt()) {
			if (keineFinSitErfasst || finSitAbgelehnt || inputData.getMassgebendesEinkommen().compareTo(maximalesEinkommen) >= 0) {
				//maximales einkommen wurde ueberschritten
				inputData.setKategorieMaxEinkommen(true);
				if (platz.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
					Betreuung betreuung = (Betreuung) platz;
					reduceAnspruchInNormalCase(betreuung, inputData);
					if (keineFinSitErfasst) {
						// TODO (hefr) hier kommt bisher die "normale" Max-Einkommen Bemerkung. Soll das so sein???
						inputData.addBemerkung(MsgKey.EINKOMMEN_MAX_MSG, getLocale(), NumberFormat.getInstance().format(maximalesEinkommen));
					} else if (finSitAbgelehnt) {
						inputData.addBemerkung(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG, getLocale());
					} else {
						inputData.addBemerkung(MsgKey.EINKOMMEN_MAX_MSG, getLocale(), NumberFormat.getInstance().format(maximalesEinkommen));
					}
				}
			}
		}
	}

	/**
	 * If the Betreuung is set as "erweiterteBeduerfniss" there is no need to reduce the Anspruch tu zero when the incomes are too high.
	 * This is because the child still has Anspruch, though it will only get a redutcion of the costs due to this erweiterteBeduerfniss
	 */
	private void reduceAnspruchInNormalCase(@Nonnull Betreuung betreuung, @Nonnull BGCalculationInput inputData) {
		if (!betreuung.hasErweiterteBetreuung()) {
			inputData.setAnspruchspensumProzent(0);
		}
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
			if (finanzDatenDTO.isEkv1AcceptedAndNotAnnuliert()) {
				inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjP1VorAbzFamGr());
				inputData.setEinkommensjahr(basisjahrPlus1);
				inputData.addBemerkung(
					MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG,
					locale,
					String.valueOf(basisjahrPlus1),
					String.valueOf(basisjahr));
			} else {
				inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
				inputData.setEinkommensjahr(basisjahr);
				// Je nachdem, ob es (manuell) annulliert war oder die 20% nicht erreicht hat, kommt eine andere Meldung
				if (finanzDatenDTO.isEkv1Annulliert()) {
					inputData.addBemerkung(
						MsgKey.EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG,
						locale,
						String.valueOf(basisjahrPlus1));
				} else {
					inputData.addBemerkung(
						MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG,
						locale,
						String.valueOf(basisjahrPlus1),
						String.valueOf(basisjahr));
				}
			}

		} else if (isEkv2) {
			if (finanzDatenDTO.isEkv2AcceptedAndNotAnnuliert()) {
				inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjP2VorAbzFamGr());
				inputData.setEinkommensjahr(basisjahrPlus2);
				inputData.addBemerkung(
					MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG,
					locale,
					String.valueOf(basisjahrPlus2),
					String.valueOf(basisjahr));
			} else {
				inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
				inputData.setEinkommensjahr(basisjahr);
				if (finanzDatenDTO.isEkv2Annulliert()) {
					inputData.addBemerkung(
						MsgKey.EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG,
						locale,
						String.valueOf(basisjahrPlus2));
				} else {
					inputData.addBemerkung(
						MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG,
						locale,
						String.valueOf(basisjahrPlus2),
						String.valueOf(basisjahr));
				}
			}
		} else {
			inputData.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
			inputData.setEinkommensjahr(basisjahr);
		}
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
