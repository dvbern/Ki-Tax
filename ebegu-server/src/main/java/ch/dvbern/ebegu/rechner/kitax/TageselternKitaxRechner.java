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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.KitaxUebergangsloesungInstitutionOeffnungszeiten;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Regelwerk;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot Tageseltern.
 */
public class TageselternKitaxRechner extends AbstractKitaxRechner {

	public TageselternKitaxRechner(
		@Nonnull KitaxUebergangsloesungParameter kitaxParameter,
		@Nullable KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten,
		@Nonnull Locale locale
	) {
		super(kitaxParameter, oeffnungszeiten, locale);
	}

	@Nonnull
	@Override
	protected Optional<BGCalculationResult> calculateGemeinde(@Nonnull BGCalculationInput input, @Nonnull BGRechnerParameterDTO parameterDTO) {

		input.getParent().setRegelwerk(Regelwerk.FEBR);

		if (!input.isBetreuungInGemeinde()) {
			if (input.getBetreuungspensumProzent().doubleValue() > 0) {
				// Wenn die Betreuung zu diesem Zeitpunkt schon beendet ist, kommt hier FALSE (es gibt ja
				// keine Betreuung in der Gemeinde zu diesem Zeitpunkt). In diesem Fall darf aber der Anspruch
				// nicht 0 gesetzt werden, da sonst der Restanspruch falsch berechnet wird!
				// Daher nur 0 setzen, wenn tatsaechlich noch eine Betreuung vorhanden ist (die nicht in der
				// Gemeinde ist)
				input.setAnspruchspensumProzent(0);
				// Die Bemerkung wollen wir nur setzen, wenn es ueberhaupt eine Betreuung gibt zu diesem Zeitpunkt
				// Das Flag betreuungInBern ist logischerweise auf der Betreuung, und in Zeitabschnitten ohne Betreuung
				// defaultmaessig false!
				input.addBemerkung(MsgKey.FEBR_BETREUUNG_NICHT_IN_BERN, locale);
			}
		}

		// Benoetigte Daten
		LocalDate von = input.getParent().getGueltigkeit().getGueltigAb();
		LocalDate bis = input.getParent().getGueltigkeit().getGueltigBis();
		BigDecimal bgPensum = MathUtil.EXACT.pctToFraction(input.getBgPensumProzent());
		BigDecimal massgebendesEinkommen = input.getMassgebendesEinkommen();

		// Inputdaten validieren
		checkArguments(von, bis, bgPensum, massgebendesEinkommen);

		// Zwischenresultate
		BigDecimal anteilMonat = calculateAnteilMonat(von, bis);
		BigDecimal anzahlTageProMonat = MathUtil.EXACT.divideNullSafe(kitaxParameter.getMaxTageKita(), ZWOELF);
		BigDecimal betreuungsstundenProMonat = MathUtil.EXACT.multiplyNullSafe(anzahlTageProMonat, kitaxParameter.getMaxStundenProTagKita(), bgPensum);
		BigDecimal betreuungsstundenIntervall = MathUtil.EXACT.multiply(betreuungsstundenProMonat, anteilMonat);

		// Kosten Betreuungsstunde
		BigDecimal kostenProBetreuungsstunde = calculateKostenBetreuungsstunde(kitaxParameter.getKostenProStundeMaximalTageseltern(), massgebendesEinkommen, bgPensum, kitaxParameter);

		// Vollkosten und Elternbeitrag
		BigDecimal vollkosten = MathUtil.EXACT.multiply(kitaxParameter.getKostenProStundeMaximalTageseltern(), betreuungsstundenIntervall);
		BigDecimal elternbeitrag;
		if (input.isBezahltVollkosten()) {
			elternbeitrag = vollkosten;
		} else {
			elternbeitrag = MathUtil.EXACT.multiply(kostenProBetreuungsstunde, betreuungsstundenIntervall);
		}

		Objects.requireNonNull(elternbeitrag);
		Objects.requireNonNull(vollkosten);

		// Runden
		vollkosten = MathUtil.roundToFrankenRappen(vollkosten);
		elternbeitrag = MathUtil.roundToFrankenRappen(elternbeitrag);

		BigDecimal verguenstigung = vollkosten.subtract(elternbeitrag);

		// Resultat erstellen und benoetigte Daten aus Input kopieren
		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(input, result);

		// In Ki-Tax gab es keinen Minimalen Elternbeitrag. Dieser wird immer 0 gesetzt
		result.setMinimalerElternbeitrag(BigDecimal.ZERO);
		result.setMinimalerElternbeitragGekuerzt(BigDecimal.ZERO);
		// In Ki-Tax wurden nicht drei "Stufen" des Gutscheins berechnet. Wir verwenden immer die berechnete Verguenstigung
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigung);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigung);
		result.setVerguenstigung(verguenstigung);
		// Elternbeitrag
		result.setElternbeitrag(elternbeitrag);
		// Wir rechnen im Kitax-Rechner mit den berechneten Vollkosten, nicht mit denjenigen, die auf der Platzbestaetigung angegeben wurden.
		result.setVollkosten(vollkosten);

		BigDecimal betreuungsstundenProMonat100Prozent = MathUtil.EXACT.multiplyNullSafe(anzahlTageProMonat,
			kitaxParameter.getMaxStundenProTagKita());

		BigDecimal multiplierPensum = MathUtil.EXACT.divide(result.getBetreuungspensumProzent(), BigDecimal.valueOf(100));
		BigDecimal multiplierAnspruch =	MathUtil.EXACT.divide(MathUtil.EXACT.from(result.getAnspruchspensumProzent()), BigDecimal.valueOf(100));
		BigDecimal multiplierBgPensum = MathUtil.EXACT.divide(result.getBgPensumProzent(), BigDecimal.valueOf(100));

		// Ki-Tax hat nur mit Prozenten gerechnet. Wir muessen die Pensen in STUNDEN berechnen
		result.setZeiteinheit(PensumUnits.HOURS);
		result.setZeiteinheitenRoundingStrategy(MathUtil::toTwoKommastelle);
		result.setBetreuungspensumZeiteinheit(MathUtil.EXACT.multiplyNullSafe(multiplierPensum, betreuungsstundenProMonat100Prozent));
		result.setAnspruchspensumZeiteinheit(MathUtil.EXACT.multiplyNullSafe(multiplierAnspruch, betreuungsstundenProMonat100Prozent));
		result.setBgPensumZeiteinheit(MathUtil.EXACT.multiplyNullSafe(multiplierBgPensum, betreuungsstundenProMonat100Prozent));

		// Die Mahlzeiten werden immer fuer den ganzen Monat eingegeben und fuer das effektive
		// Betreuungspensum. Wir muessen daher noch auf den Anteil des Monats und das verguenstigte
		// Pensum reduzieren.
		BigDecimal anteilVerguenstigesPensumAmBetreuungspensum = calculateAnteilVerguenstigtesPensumAmBetreuungspensum(input);
		handleAnteileMahlzeitenverguenstigung(result, anteilMonat, anteilVerguenstigesPensumAmBetreuungspensum);

		// Bemerkung hinzufuegen
		input.addBemerkung(MsgKey.FEBR_INFO, locale);

		return Optional.of(result);
	}
}
