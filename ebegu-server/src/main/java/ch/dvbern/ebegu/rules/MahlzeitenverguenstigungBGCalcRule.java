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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungParameter;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;

/**
 * Regel die angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungBGCalcRule extends AbstractCalcRule {

	private final MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams;

	protected MahlzeitenverguenstigungBGCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		@Nonnull MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams
	) {

		super(RuleKey.MAHLZEITENVERGUENSTIGUNG, RuleType.GRUNDREGEL_CALC, RuleValidity.GEMEINDE, validityPeriod, locale);
		this.mahlzeitenverguenstigungParams = mahlzeitenverguenstigungParams;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {

		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();

		boolean verguenstigungBeantrag = familiensituation != null && !familiensituation.isKeineMahlzeitenverguenstigungBeantragt();

		if (!verguenstigungBeantrag) {
			return;
		}

		final BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		final boolean sozialhilfeempfaenger = inputData.isSozialhilfeempfaenger();

		if (!mahlzeitenverguenstigungParams.isEnabled() ||
			!validateInput(inputData) ||
			!mahlzeitenverguenstigungParams.hasAnspruch(massgebendesEinkommen, sozialhilfeempfaenger)) {
			return;
		}

		BigDecimal verguenstigungMahlzeit =
			mahlzeitenverguenstigungParams.getVerguenstigungProMahlzeitWithParam(massgebendesEinkommen, sozialhilfeempfaenger);

		// Ein vollständiger Kita Tag besteht aus 2 Nebenmahlzeiten und 1 Hauptmahlzeit
		BigDecimal anzahlNebenmahlzeitenStandardTag = new BigDecimal("2.00");

		// Wir machen eine Annahme für die Anzahl Tage, bei denen das Kind eine Betreuung hat
		// Für jeden Tag wo das Kind eine Hauptmahlzeit bezieht wird angenommen, dass es eine Ganztagesbetreuung hat
		// (1 Hauptmahlzeit, 2 Nebenmahlzeiten).
		// Für Differenz zwischen Anzahl Nebenmahlzeit - Anzahl Hauptmahlzeit*2 wird jeweils angenommen, dass
		// es sich z.B. und reine Nachmittagsbetreuungen handelt
		BigDecimal anzahlTageMitHM = inputData.getAnzahlHauptmahlzeiten();
		BigDecimal anzahlTageOhneHM = inputData.getAnzahlNebenmahlzeiten().subtract(
			anzahlTageMitHM.multiply(anzahlNebenmahlzeitenStandardTag)
		);
		anzahlTageOhneHM = MathUtil.minimum(anzahlTageOhneHM, BigDecimal.ZERO);

		// maximaler Tagesansatz für Tage mit Hauptmahlzeit ist Hauptmahlzeit + 2*Preis Nebenmahlzeit - minimaler Elternbeitrag.
		BigDecimal maxTagesansatzMitHM = inputData.getTarifNebenmahlzeit()
			.multiply(anzahlNebenmahlzeitenStandardTag)
			.add(inputData.getTarifHauptmahlzeit())
			.subtract(mahlzeitenverguenstigungParams.getMinimalerElternbeitragMahlzeit());
		maxTagesansatzMitHM = MathUtil.minimum(maxTagesansatzMitHM, BigDecimal.ZERO);

		// maximaler Tagesansatz für Tage ohne Hauptmahlzeit ist 2*Preis Nebenmahlzeit - 2*minimaler Elternbeitrag.
		final BigDecimal a = inputData.getTarifNebenmahlzeit().multiply(anzahlNebenmahlzeitenStandardTag);
		final BigDecimal b = mahlzeitenverguenstigungParams.getMinimalerElternbeitragMahlzeit().multiply(anzahlNebenmahlzeitenStandardTag);
		BigDecimal maxTagesansatzOhneHM = a.subtract(b);
		maxTagesansatzOhneHM = MathUtil.minimum(maxTagesansatzOhneHM, BigDecimal.ZERO);

		// Vergünstigung für Tage mit Hauptmahlzeit
		BigDecimal verguenstigungTageMitHM = MathUtil.maximum(
			anzahlTageMitHM.multiply(verguenstigungMahlzeit),
			anzahlTageMitHM.multiply(maxTagesansatzMitHM)
		);
		verguenstigungTageMitHM = MathUtil.minimum(verguenstigungTageMitHM, BigDecimal.ZERO);

		// Vergünstigung für Tage ohne Hauptmahlzeit
		BigDecimal verguenstigungTageOhneHM = MathUtil.maximum(
			anzahlTageOhneHM.multiply(verguenstigungMahlzeit),
			anzahlTageOhneHM.multiply(maxTagesansatzOhneHM)
		);
		verguenstigungTageOhneHM = MathUtil.minimum(verguenstigungTageOhneHM, BigDecimal.ZERO);

		BigDecimal verguenstigungTotal = verguenstigungTageMitHM.add(verguenstigungTageOhneHM);

		if (verguenstigungTotal.compareTo(BigDecimal.ZERO) > 0) {
			addBemerkung(inputData);
		}

		inputData.getParent().setVerguenstigungMahlzeitenTotalForAsivAndGemeinde(verguenstigungTotal);
	}

	private void addBemerkung(@Nonnull BGCalculationInput inputData) {
		inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG, getLocale());
	}

	private boolean validateInput(@Nonnull BGCalculationInput inputData) {
		return (inputData.getAnzahlHauptmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifHauptmahlzeit().compareTo(BigDecimal.ZERO) > 0) ||
			(inputData.getAnzahlNebenmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifNebenmahlzeit().compareTo(BigDecimal.ZERO) > 0);
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		return einstellungMap.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED).getValueAsBoolean();
	}
}
