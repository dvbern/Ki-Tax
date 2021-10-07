/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.UnbezahlterUrlaub;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Regel für unbezahlten Urlaub. In dem Teil des Urlaubs, welcher 3 Monate übersteigt, verfällt der Anspruch (für dieses Erwerbspensum!)
 */
public class UnbezahlterUrlaubAbschnittRule extends AbstractErwerbspensumAbschnittRule {


	public UnbezahlterUrlaubAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.UNBEZAHLTER_URLAUB, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
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
		@Nonnull AbstractPlatz platz,
		boolean gs2
	) {
		List<VerfuegungZeitabschnitt> ewpAbschnitte = new ArrayList<>();
		Set<ErwerbspensumContainer> ewpContainers = gesuchsteller.getErwerbspensenContainersNotEmpty();

		ewpContainers.stream()
			.map(ErwerbspensumContainer::getErwerbspensumJA)
			.filter(Objects::nonNull)
			.filter(erwerbspensumJA -> erwerbspensumJA.getUnbezahlterUrlaub() != null)
			.map(erwerbspensumJA -> toVerfuegungZeitabschnitt(gesuch, erwerbspensumJA, gs2))
			.filter(Objects::nonNull)
			.forEach(ewpAbschnitte::add);

		return ewpAbschnitte;
	}

	/**
	 * Fuer Zeitabschnitten bei denen einen langen Urlaub eingegeben wurde, setzen wir das Pensum auf -XX sodass das Pensum aus
	 * ErwerbspensumAbschnittRule kompensiert wird.
	 */
	@Nullable
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(@Nonnull Gesuch gesuch, @Nonnull Erwerbspensum erwerbspensumJA, boolean gs2) {

		// Jeder Urlaub ist mehr als 3 Monate, dies wird bereits beim Speichern validiert. Trotzdem pruefen wir es
		// hier nochmals
		UnbezahlterUrlaub urlaub = erwerbspensumJA.getUnbezahlterUrlaub();
		Objects.requireNonNull(urlaub);
		LocalDate urlaubNachFreibetragStart = urlaub.getGueltigkeit().getGueltigAb().plusMonths(3);
		LocalDate urlaubEnd = urlaub.getGueltigkeit().getGueltigBis();
		if (urlaubNachFreibetragStart.isAfter(urlaubEnd)) {
			return null;
		}
		final DateRange gueltigkeit = new DateRange(urlaubNachFreibetragStart, urlaubEnd);

		// Wir merken uns hier den eingegebenen Wert, auch wenn dieser (mit Zuschlag) über 100% liegt
		Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		if (gs2 && gesuch.isMutation() && familiensituationErstgesuch != null && familiensituation != null) {

			getGueltigkeitFromFamiliensituation(gueltigkeit, familiensituationErstgesuch, familiensituation);

			return createZeitabschnittUnbezahlterUrlaubGS2(erwerbspensumJA, gueltigkeit);
		}
		if (gs2 && !gesuch.isMutation()) {
			return createZeitabschnittUnbezahlterUrlaubGS2(erwerbspensumJA, gueltigkeit);
		}
		if (!gs2) {
			return createZeitabschnittUnbezahlterUrlaubGS1(erwerbspensumJA, gueltigkeit);
		}
		return null;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitabschnittUnbezahlterUrlaubGS1(
		@Nonnull Erwerbspensum erwerbspensumJA,
		DateRange gueltigkeit
	) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		zeitabschnitt.setErwerbspensumGS1ForAsivAndGemeinde(0 - erwerbspensumJA.getPensum());
		zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(MsgKey.UNBEZAHLTER_URLAUB_MSG, getLocale());
		return zeitabschnitt;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitabschnittUnbezahlterUrlaubGS2(
		@Nonnull Erwerbspensum erwerbspensumJA,
		DateRange gueltigkeit
	) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		zeitabschnitt.setErwerbspensumGS2ForAsivAndGemeinde(0 - erwerbspensumJA.getPensum());
		zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(MsgKey.UNBEZAHLTER_URLAUB_MSG, getLocale());
		return zeitabschnitt;
	}
}
