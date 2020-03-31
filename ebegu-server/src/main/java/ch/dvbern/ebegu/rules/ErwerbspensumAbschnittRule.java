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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Berechnet die hoehe des ErwerbspensumRule eines bestimmten Erwerbspensums
 * Diese Rule muss immer am Anfang kommen, d.h. sie setzt den initialen Anspruch
 * Die weiteren Rules müssen diesen Wert gegebenenfalls korrigieren.
 * Verweis 16.9.2
 */
public class ErwerbspensumAbschnittRule extends AbstractErwerbspensumAbschnittRule {

	public ErwerbspensumAbschnittRule(DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.ERWERBSPENSUM, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	/**
	 * geht durch die Erwerpspensen des Gesuchstellers und gibt Abschnitte zurueck
	 *
	 * @param gesuchsteller Der Gesuchsteller dessen Erwerbspensumcontainers zu Abschnitte konvertiert werden
	 * @param gs2 handelt es sich um gesuchsteller1 -> false oder gesuchsteller2 -> true
	 */
	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> getErwerbspensumAbschnittForGesuchsteller(
		@Nonnull Gesuch gesuch,
		@Nonnull GesuchstellerContainer gesuchsteller,
		boolean gs2
	) {
		List<VerfuegungZeitabschnitt> ewpAbschnitte = new ArrayList<>();
		Set<ErwerbspensumContainer> ewpContainers = gesuchsteller.getErwerbspensenContainersNotEmpty();

		ewpContainers.stream()
			.map(ErwerbspensumContainer::getErwerbspensumJA)
			.filter(Objects::nonNull)
			.map(erwerbspensumJA -> toVerfuegungZeitabschnitt(gesuch, erwerbspensumJA, gs2))
			.filter(Objects::nonNull)
			.forEach(ewpAbschnitte::add);

		return ewpAbschnitte;
	}

	/**
	 * Konvertiert ein Erwerbspensum in einen Zeitabschnitt von entsprechender dauer und erwerbspensumGS1 (falls gs2=false)
	 * oder erwerpspensuGS2 (falls gs2=true)
	 */
	@Nullable
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(@Nonnull Gesuch gesuch, @Nonnull Erwerbspensum erwerbspensum, boolean gs2) {
		final DateRange gueltigkeit = new DateRange(erwerbspensum.getGueltigkeit());

		// Wir merken uns hier den eingegebenen Wert, auch wenn dieser (mit Zuschlag) über 100% liegt
		Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
		Familiensituation familiensituation = gesuch.extractFamiliensituation();

		if (gs2 && gesuch.isMutation() && familiensituationErstgesuch != null && familiensituation != null) {
			getGueltigkeitFromFamiliensituation(gueltigkeit, familiensituationErstgesuch, familiensituation);
			return createZeitAbschnittForGS2(gueltigkeit, erwerbspensum);
		}
		if (gs2 && !gesuch.isMutation()) {
			return createZeitAbschnittForGS2(gueltigkeit, erwerbspensum);
		}
		if (!gs2) {
			return createZeitAbschnittForGS1(gueltigkeit, erwerbspensum);
		}
		return null;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitAbschnittForGS1(DateRange gueltigkeit, @Nonnull Erwerbspensum erwerbspensum) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
		zeitabschnitt.addTaetigkeitForAsivAndGemeinde(erwerbspensum.getTaetigkeit());
		zeitabschnitt.setErwerbspensumGS1ForAsivAndGemeinde(erwerbspensum.getPensum());
		return zeitabschnitt;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitAbschnittForGS2(DateRange gueltigkeit, @Nonnull Erwerbspensum erwerbspensum) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
		zeitabschnitt.addTaetigkeitForAsivAndGemeinde(erwerbspensum.getTaetigkeit());
		zeitabschnitt.setErwerbspensumGS2ForAsivAndGemeinde(erwerbspensum.getPensum());
		return zeitabschnitt;
	}
}
