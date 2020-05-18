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
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot KITA.
 */
public class KitaKitaxRechner extends AbstractKitaxRechner {

	// Kitax hat nur mit Prozenten gerechnet, neu brauchen wir (auch) Zeiteinheiten, bei Kita TAGE
	// 100% = 20 days => 1% = 0.2 days
	public static final BigDecimal MULTIPLIER_KITA = MathUtil.DEFAULT.fromNullSafe(0.2);

	public KitaKitaxRechner(
		@Nonnull KitaxUebergangsloesungParameter kitaxParameter,
		@Nonnull KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten,
		@Nonnull Locale locale
	) {
		super(kitaxParameter, oeffnungszeiten, locale);
	}

	@Nonnull
	@Override
	protected Optional<BGCalculationResult> calculateGemeinde(@Nonnull BGCalculationInput input, @Nonnull BGRechnerParameterDTO parameterDTO) {

		if (!input.isBetreuungInGemeinde()) {
			input.setAnspruchspensumProzent(0);
			// Die Bemerkung wollen wir nur setzen, wenn es ueberhaupt eine Betreuung gibt zu diesem Zeitpunkt
			// Das Flag betreuungInBern ist logischerweise auf der Betreuung, und in Zeitabschnitten ohne Betreuung
			// defaultmaessig false!
			if (input.getBetreuungspensumProzent().doubleValue() > 0) {
				input.addBemerkung(MsgKey.FEBR_BETREUUNG_NICHT_IN_BERN, locale);
			}
		}

		// Benoetigte Daten
		LocalDate von = input.getParent().getGueltigkeit().getGueltigAb();
		LocalDate bis = input.getParent().getGueltigkeit().getGueltigBis();

		BigDecimal oeffnungsstunden = oeffnungszeiten.getOeffnungsstunden();
		BigDecimal oeffnungstage = oeffnungszeiten.getOeffnungstage();
		BigDecimal bgPensum = MathUtil.EXACT.pctToFraction(input.getBgPensumProzent());
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
		if (input.isBezahltVollkosten()) {
			elternbeitragIntervall = vollkostenIntervall;
		} else {
			elternbeitragIntervall = MathUtil.EXACT.multiply(elternbeitrag, anteilMonat);
		}

		Objects.requireNonNull(elternbeitragIntervall);
		Objects.requireNonNull(vollkostenIntervall);

		// Runden
		vollkostenIntervall = MathUtil.roundToFrankenRappen(vollkostenIntervall);
		elternbeitragIntervall = MathUtil.roundToFrankenRappen(elternbeitragIntervall);

		BigDecimal verguenstigungIntervall = vollkostenIntervall.subtract(elternbeitragIntervall);

		// Resultat erstellen
		BGCalculationResult result = createResult(input, vollkostenIntervall, verguenstigungIntervall, elternbeitragIntervall);

		// Bemerkung hinzufuegen
		input.addBemerkung(MsgKey.FEBR_INFO, locale);

		return Optional.of(result);
	}

	private BGCalculationResult createResult(
		@Nonnull BGCalculationInput input,
		@Nonnull BigDecimal vollkostenIntervall,
		@Nonnull BigDecimal verguenstigungIntervall,
		@Nonnull BigDecimal elternbeitragIntervall
	) {
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
		result.setBetreuungspensumZeiteinheit(MathUtil.DEFAULT.multiplyNullSafe(result.getBetreuungspensumProzent(), MULTIPLIER_KITA));
		result.setAnspruchspensumZeiteinheit(MathUtil.DEFAULT.multiply(MathUtil.DEFAULT.from(result.getAnspruchspensumProzent()), MULTIPLIER_KITA));
		result.setBgPensumZeiteinheit(MathUtil.DEFAULT.multiply(result.getBgPensumProzent(), MULTIPLIER_KITA));

		return result;
	}
}
