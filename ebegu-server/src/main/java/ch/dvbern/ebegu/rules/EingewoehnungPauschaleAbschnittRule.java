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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EingewoehnungPauschaleAbschnittRule extends AbstractAbschnittRule {


	public EingewoehnungPauschaleAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BETREUUNGSPENSUM, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Nonnull
	@Override
	@SuppressWarnings("PMD.NcssMethodCount")
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		Betreuung betreuung = (Betreuung) platz;
		Set<BetreuungspensumContainer> betreuungspensen = betreuung.getBetreuungspensumContainers();


		return betreuungspensen.stream()
			.map(betreuungspensumContainer ->
				createVerfuegungsZeitabschnittIfEingewoehnungPauschale(betreuungspensumContainer.getBetreuungspensumJA()))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Nullable
	private VerfuegungZeitabschnitt createVerfuegungsZeitabschnittIfEingewoehnungPauschale(
		Betreuungspensum betreuungspensum
	) {
		if (betreuungspensum.getEingewoehnungPauschale() == null) {
			return null;
		}

		return toVerfuegungZeitabschnitt(betreuungspensum.getEingewoehnungPauschale(), betreuungspensum.getGueltigkeit());
	}

	@Nonnull
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(
		@Nonnull EingewoehnungPauschale eingewoehnungPauschale,
		@Nonnull DateRange gueltigkeitBetreuungspensum
	) {
		DateRange gueltigkeit = calculateGueltigkeitForEingewoehnungZa(gueltigkeitBetreuungspensum);
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		zeitabschnitt.setEingewoehnungPauschale(eingewoehnungPauschale.getPauschale());
		zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(MsgKey.EINGEWOEHUNG_PASCHALE, getLocale());
		return zeitabschnitt;
	}

	@Nonnull
	private DateRange calculateGueltigkeitForEingewoehnungZa(DateRange gueltigkeitBetreuungspensum) {
		LocalDate gueltigBis =
			isGueltigAbAndGueltigBisInSameMonth(gueltigkeitBetreuungspensum) ?
				gueltigkeitBetreuungspensum.getGueltigBis() :
				gueltigkeitBetreuungspensum.getGueltigAb().with(TemporalAdjusters.lastDayOfMonth());

		return new DateRange(gueltigkeitBetreuungspensum.getGueltigAb(), gueltigBis);
	}

	private boolean isGueltigAbAndGueltigBisInSameMonth(DateRange gueltigkeit) {
		return gueltigkeit.getGueltigAb().getMonth() == gueltigkeit.getGueltigBis().getMonth();
	}
}
