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

import static ch.dvbern.ebegu.util.MathUtil.EXACT;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot KITA.
 */
public class KitaKitaxRechner extends AbstractKitaxRechner {

	public KitaKitaxRechner(
		@Nonnull KitaxUebergangsloesungParameter kitaxParameter,
		@Nonnull KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten,
		@Nonnull Locale locale
	) {
		super(kitaxParameter, oeffnungszeiten, locale);
	}

	@Nonnull
	@Override
	@SuppressWarnings("PMD.NcssMethodCount")
	protected Optional<BGCalculationResult> calculateGemeinde(@Nonnull BGCalculationInput input, @Nonnull BGRechnerParameterDTO parameterDTO) {

		Objects.requireNonNull(oeffnungszeiten);
		input.getParent().setRegelwerk(Regelwerk.FEBR);

		if (!input.isBetreuungInGemeinde() && input.getBetreuungspensumProzent().doubleValue() > 0) {
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

		// Benoetigte Daten
		LocalDate von = input.getParent().getGueltigkeit().getGueltigAb();
		LocalDate bis = input.getParent().getGueltigkeit().getGueltigBis();

		BigDecimal oeffnungsstunden = oeffnungszeiten.getOeffnungsstunden();
		BigDecimal oeffnungstage = oeffnungszeiten.getOeffnungstage();
		BigDecimal bgPensum = EXACT.pctToFraction(input.getBgPensumProzent());
		BigDecimal massgebendesEinkommen = input.getMassgebendesEinkommen();

		// Inputdaten validieren
		checkArguments(von, bis, bgPensum, massgebendesEinkommen);
		Objects.requireNonNull(oeffnungsstunden, "oeffnungsstunden darf nicht null sein");
		Objects.requireNonNull(oeffnungstage, "oeffnungstage darf nicht null sein");

		// Zwischenresultate
		BigDecimal faktor = input.isBabyTarif() ? kitaxParameter.getBabyFaktor() : BigDecimal.ONE;
		BigDecimal anteilMonat = calculateAnteilMonat(von, bis);

		// Abgeltung pro Tag: Abgeltung des Kantons plus Beitrag der Stadt
		final BigDecimal beitragStadtProTagJahr = kitaxParameter.getBeitragStadtProTagJahr();
		BigDecimal abgeltungProTag = EXACT.add(kitaxParameter.getBeitragKantonProTag(), beitragStadtProTagJahr);
		// Massgebendes Einkommen: Minimum und Maximum berücksichtigen

		BigDecimal massgebendesEinkommenBerechnet = (massgebendesEinkommen.max(kitaxParameter.getMinMassgebendesEinkommen())).min(kitaxParameter.getMaxMassgebendesEinkommen());
		// Öffnungstage und Öffnungsstunden; Maximum berücksichtigen
		BigDecimal oeffnungstageBerechnet = oeffnungstage.min(kitaxParameter.getMaxTageKita());
		BigDecimal oeffnungsstundenBerechnet = oeffnungsstunden.min(kitaxParameter.getMaxStundenProTagKita());

		// Vollkosten
		BigDecimal vollkostenZaehler = EXACT.multiply(abgeltungProTag, oeffnungsstundenBerechnet, oeffnungstageBerechnet, bgPensum);
		BigDecimal vollkostenNenner = EXACT.multiply(kitaxParameter.getMaxStundenProTagKita(), ZWOELF);
		BigDecimal vollkosten = EXACT.divide(vollkostenZaehler, vollkostenNenner);

		// Elternbeitrag
		BigDecimal kostenProStundeMaxMinusMin = EXACT.subtract(kitaxParameter.getKostenProStundeMaximalKitaTagi(), kitaxParameter.getKostenProStundeMinimal());
		BigDecimal massgebendesEinkommenMinusMin = EXACT.subtract(massgebendesEinkommenBerechnet, kitaxParameter.getMinMassgebendesEinkommen());
		BigDecimal massgebendesEinkommenMaxMinusMin = EXACT.subtract(kitaxParameter.getMaxMassgebendesEinkommen(), kitaxParameter.getMinMassgebendesEinkommen());
		BigDecimal param1 = EXACT.multiply(kostenProStundeMaxMinusMin, massgebendesEinkommenMinusMin);
		BigDecimal param2 = EXACT.multiply(kitaxParameter.getKostenProStundeMinimal(), massgebendesEinkommenMaxMinusMin);
		BigDecimal param1Plus2 = EXACT.add(param1, param2);
		BigDecimal elternbeitragZaehler = EXACT.multiply(param1Plus2, NEUN, ZWANZIG, bgPensum, oeffnungstageBerechnet, oeffnungsstundenBerechnet);
		BigDecimal elternbeitragNenner = EXACT.multiply(massgebendesEinkommenMaxMinusMin, ZWEIHUNDERTVIERZIG, kitaxParameter.getMaxStundenProTagKita());
		BigDecimal elternbeitrag = EXACT.divide(elternbeitragZaehler, elternbeitragNenner);

		// Runden und auf Zeitabschnitt zurückschreiben
		BigDecimal vollkostenIntervall = EXACT.multiply(vollkosten, faktor, anteilMonat);
		BigDecimal elternbeitragIntervall;
		if (input.isBezahltVollkosten()) {
			elternbeitragIntervall = vollkostenIntervall;
		} else {
			elternbeitragIntervall = EXACT.multiply(elternbeitrag, anteilMonat);
		}

		Objects.requireNonNull(elternbeitragIntervall);
		Objects.requireNonNull(vollkostenIntervall);

		// Runden
		vollkostenIntervall = MathUtil.roundToFrankenRappen(vollkostenIntervall);
		elternbeitragIntervall = MathUtil.roundToFrankenRappen(elternbeitragIntervall);

		BigDecimal verguenstigungIntervall = vollkostenIntervall.subtract(elternbeitragIntervall);

		// Resultat erstellen
		BGCalculationResult result = createResult(input, vollkostenIntervall, verguenstigungIntervall, elternbeitragIntervall, anteilMonat);

		// Die Mahlzeiten werden immer fuer den ganzen Monat eingegeben und fuer das effektive
		// Betreuungspensum. Wir muessen daher noch auf den Anteil des Monats und das verguenstigte
		// Pensum reduzieren.
		BigDecimal anteilVerguenstigesPensumAmBetreuungspensum = calculateAnteilVerguenstigtesPensumAmBetreuungspensum(input);
		handleAnteileMahlzeitenverguenstigung(result, anteilMonat, anteilVerguenstigesPensumAmBetreuungspensum);

		// Bemerkung hinzufuegen
		input.addBemerkung(MsgKey.FEBR_INFO, locale);

		if (oeffnungszeiten.isDummyParams()) {
			input.addBemerkung(MsgKey.NO_MATCHING_FROM_KITAX, locale, oeffnungstageBerechnet, oeffnungsstundenBerechnet);
		}

		return Optional.of(result);
	}

	private BGCalculationResult createResult(
		@Nonnull BGCalculationInput input,
		@Nonnull BigDecimal vollkostenIntervall,
		@Nonnull BigDecimal verguenstigungIntervall,
		@Nonnull BigDecimal elternbeitragIntervall,
		@Nonnull BigDecimal anteilMonat
	) {
		Objects.requireNonNull(oeffnungszeiten);
		// Resultat erstellen und benoetigte Daten aus Input kopieren
		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(input, result);

		// In Ki-Tax gab es keinen Minimalen Elternbeitrag. Dieser wird immer 0 gesetzt
		result.setMinimalerElternbeitrag(BigDecimal.ZERO);
		result.setMinimalerElternbeitragGekuerzt(BigDecimal.ZERO);
		// In Ki-Tax wurden nicht drei "Stufen" des Gutscheins berechnet. Wir verwenden immer die berechnete Verguenstigung
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigungIntervall);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigungIntervall);
		result.setVerguenstigung(verguenstigungIntervall);
		// Elternbeitrag
		result.setElternbeitrag(elternbeitragIntervall);
		// Wir rechnen im Kitax-Rechner mit den berechneten Vollkosten, nicht mit denjenigen, die auf der Platzbestaetigung angegeben wurden.
		result.setVollkosten(vollkostenIntervall);

		// Ki-Tax hat nur mit Prozenten gerechnet. Wir muessen die Pensen in TAGE berechnen
		result.setZeiteinheit(PensumUnits.DAYS);
		result.setZeiteinheitenRoundingStrategy(MathUtil::toTwoKommastelle);
		BigDecimal tageProMonat = EXACT.divide(oeffnungszeiten.getOeffnungstage(), BigDecimal.valueOf(12));

		BigDecimal multiplierPensum = EXACT.divide(result.getBetreuungspensumProzent(), BigDecimal.valueOf(100));
		BigDecimal multiplierAnspruch =	EXACT.divide(EXACT.from(result.getAnspruchspensumProzent()), BigDecimal.valueOf(100));
		BigDecimal multiplierBgPensum = EXACT.divide(result.getBgPensumProzent(), BigDecimal.valueOf(100));

		result.setBetreuungspensumZeiteinheit(EXACT.multiplyNullSafe(tageProMonat, multiplierPensum, anteilMonat));
		result.setAnspruchspensumZeiteinheit(EXACT.multiplyNullSafe(tageProMonat, multiplierAnspruch, anteilMonat));
		result.setBgPensumZeiteinheit(EXACT.multiplyNullSafe(tageProMonat, multiplierBgPensum, anteilMonat));

		return result;
	}
}
