/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner.kitax;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot KITA.
 */
public class KitaKitaxRechner extends AbstractKitaxRechner {

	@Override
	public VerfuegungZeitabschnitt calculate(VerfuegungZeitabschnitt verfuegungZeitabschnitt, Verfuegung verfuegung, KitaxParameterDTO kitaxParameter) {
		// Benoetigte Daten
		LocalDate von = verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb();
		LocalDate bis = verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis();
		Objects.requireNonNull(verfuegung.getBetreuung(), "Betreuung muss gesetzt sein");
		LocalDate geburtsdatum = verfuegung.getBetreuung().getKind().getKindJA().getGeburtsdatum();
		BigDecimal oeffnungsstunden = kitaxParameter.getOeffnungsstundenKita();
		BigDecimal oeffnungstage = kitaxParameter.getOeffnungstageKita();
		BigDecimal bgPensum = MathUtil.EXACT.pctToFraction(verfuegungZeitabschnitt.getBgPensum());
		BigDecimal massgebendesEinkommen = verfuegungZeitabschnitt.getMassgebendesEinkommen();

		// Inputdaten validieren
		checkArguments(von, bis, bgPensum, massgebendesEinkommen);
		Objects.requireNonNull(geburtsdatum, "geburtsdatum darf nicht null sein");
		Objects.requireNonNull(oeffnungsstunden, "oeffnungsstunden darf nicht null sein");
		Objects.requireNonNull(oeffnungstage, "oeffnungstage darf nicht null sein");

		// Zwischenresultate
		BigDecimal faktor = von.isAfter(geburtsdatum.plusMonths(kitaxParameter.getBabyAlterInMonaten()).with(TemporalAdjusters.lastDayOfMonth())) ? FAKTOR_KIND : kitaxParameter.getBabyFaktor();
		BigDecimal anteilMonat = calculateAnteilMonat(von, bis);

		// Abgeltung pro Tag: Abgeltung des Kantons plus Beitrag der Stadt
		final BigDecimal beitragStadtProTagJahr = getBeitragStadtProTagJahr(kitaxParameter, verfuegung.getBetreuung().extractGesuch().getGesuchsperiode(), von);
		BigDecimal abgeltungProTag = MathUtil.EXACT.add(kitaxParameter.getBeitragKantonProTag(), beitragStadtProTagJahr);
		// Massgebendes Einkommen: Minimum und Maximum berücksichtigen

		BigDecimal massgebendesEinkommenBerechnet = (massgebendesEinkommen.max(kitaxParameter.getMinMassgebendesEinkommen())).min(kitaxParameter.getMaxMassgebendesEinkommen());
		// Öffnungstage und Öffnungsstunden; Maximum berücksichtigen
		BigDecimal oeffnungstageBerechnet = oeffnungstage.min(kitaxParameter.getMaxTageKita());
		BigDecimal oeffnungsstundenBerechnet = oeffnungsstunden.min(kitaxParameter.getMaxStundenProTagKita());

		// Vollkosten
		BigDecimal vollkostenZaehler = MathUtil.EXACT.multiply(abgeltungProTag, oeffnungsstundenBerechnet, oeffnungstageBerechnet, bgPensum);
		BigDecimal vollkostenNenner = MathUtil.EXACT.multiply(kitaxParameter.getMaxStundenProTagKita(), ZWOELF);
		BigDecimal vollkosten = MathUtil.EXACT.divide(vollkostenZaehler, vollkostenNenner);

		// Elternbeitrag
		BigDecimal kostenProStundeMaxMinusMin = MathUtil.EXACT.subtract(kitaxParameter.getKostenProStundeMaximalKitaTagi(), kitaxParameter.getKostenProStundeMinimal());
		BigDecimal massgebendesEinkommenMinusMin = MathUtil.EXACT.subtract(massgebendesEinkommenBerechnet, kitaxParameter.getMinMassgebendesEinkommen());
		BigDecimal massgebendesEinkommenMaxMinusMin = MathUtil.EXACT.subtract(kitaxParameter.getMaxMassgebendesEinkommen(), kitaxParameter.getMinMassgebendesEinkommen());
		BigDecimal param1 = MathUtil.EXACT.multiply(kostenProStundeMaxMinusMin, massgebendesEinkommenMinusMin);
		BigDecimal param2 = MathUtil.EXACT.multiply(kitaxParameter.getKostenProStundeMinimal(), massgebendesEinkommenMaxMinusMin);
		BigDecimal param1Plus2 = MathUtil.EXACT.add(param1, param2);
		BigDecimal elternbeitragZaehler = MathUtil.EXACT.multiply(param1Plus2, NEUN, ZWANZIG, bgPensum, oeffnungstageBerechnet, oeffnungsstundenBerechnet);
		BigDecimal elternbeitragNenner = MathUtil.EXACT.multiply(massgebendesEinkommenMaxMinusMin, ZWEIHUNDERTVIERZIG, kitaxParameter.getMaxStundenProTagKita());
		BigDecimal elternbeitrag = MathUtil.EXACT.divide(elternbeitragZaehler, elternbeitragNenner);

		// Runden und auf Zeitabschnitt zurückschreiben
		BigDecimal vollkostenIntervall = MathUtil.EXACT.multiply(vollkosten, faktor, anteilMonat);
		BigDecimal elternbeitragIntervall;
		if (verfuegungZeitabschnitt.getBgCalculationInputGemeinde().isBezahltVollkosten()) {
			elternbeitragIntervall = vollkostenIntervall;
		} else {
			elternbeitragIntervall = MathUtil.EXACT.multiply(elternbeitrag, anteilMonat);
		}

		// TODO KITAX: Berechnen wir den Elternbeitrag aufgrund der (fixen) Vollkosten, speichern als Kosten aber den eingegebenen Betrag aus Betreuung?
//		verfuegungZeitabschnitt.setVollkosten(MathUtil.roundToFrankenRappen(vollkostenIntervall));
//		verfuegungZeitabschnitt.setElternbeitrag(MathUtil.roundToFrankenRappen(elternbeitragIntervall));

		// TODO Resultat erstellen und benoetigte Daten aus Input kopieren
//		BGCalculationResult result = new BGCalculationResult();
//		VerfuegungZeitabschnitt.initBGCalculationResult(input, result);
//
//		result.setZeiteinheitenRoundingStrategy(zeiteinheitenRoundingStrategy());
//		result.setMinimalerElternbeitrag(minBetrag);
//		result.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigungVorVollkostenUndMinimalbetrag);
//		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigungVorMinimalbetrag);
//		result.setVerguenstigung(verguenstigung);
//		result.setVollkosten(vollkosten);
//		result.setElternbeitrag(elternbeitrag);
//		result.setMinimalerElternbeitragGekuerzt(minimalerElternbeitragGekuerzt);
//
//		// Die Stundenwerte (Betreuungsstunden, Anspruchsstunden und BG-Stunden) müssen gerundet werden
//		result.setBgPensumZeiteinheit(verfuegteZeiteinheiten);
//		result.setAnspruchspensumZeiteinheit(anspruchsberechtigteZeiteinheiten);
//		result.setZeiteinheit(getZeiteinheit());
//		result.setBetreuungspensumZeiteinheit(betreuungspensumZeiteinheit);

		return verfuegungZeitabschnitt;
	}

	/**
	 * Beitrag Stadt für erstes Halbjahr oder zweites Halbjahr holen gehen
	 */
	private BigDecimal getBeitragStadtProTagJahr(KitaxParameterDTO parameterDTO, Gesuchsperiode gesuchsperiode, LocalDate von) {
		if (von.getYear() == gesuchsperiode.getBasisJahrPlus1()) {
			return parameterDTO.getBeitragStadtProTagJahr1();
		}
		return parameterDTO.getBeitragStadtProTagJahr2();
	}
}
