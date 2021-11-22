/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

/**
 * Sonderregel die nach der MonatsRule ausgeführt wird. Die Regel sorgt dafür, dass es für jeden Monat genau einen Zeitabschnitt gibt.
 */
public class MonatsMergerRule extends AbstractAbschlussRule {

	private final boolean anspruchsBerchtigungMontasweise;

	public MonatsMergerRule(boolean isDebug, boolean anspruchsBerchtigungMontasweise) {
		super(isDebug);
		this.anspruchsBerchtigungMontasweise = anspruchsBerchtigungMontasweise;
	}

	@Override
	@Nonnull
	public List<VerfuegungZeitabschnitt> executeIfApplicable(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		//Rule ist nur anwendbar, wenn AnspruchsberechtigungMonatsweise=true konfiguriert ist
		if(anspruchsBerchtigungMontasweise) {
			return super.executeIfApplicable(platz, zeitabschnitte);
		}

		return zeitabschnitte;
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {

		Map<Month, List<VerfuegungZeitabschnitt>> monthVerfuegungZeitabschnittMap = mapZeitabschnitteByMonth(zeitabschnitte);
		return getZeitabschnitteFullMonth(monthVerfuegungZeitabschnittMap);
	}


	private Map<Month, List<VerfuegungZeitabschnitt>> mapZeitabschnitteByMonth(List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte.stream()
			.collect(Collectors.groupingBy(zeitabschnitt -> zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth(),
				Collectors.toCollection(ArrayList::new)));
	}

	private List<VerfuegungZeitabschnitt> getZeitabschnitteFullMonth(Map<Month, List<VerfuegungZeitabschnitt>> monthVerfuegungZeitabschnittMap) {
		return Arrays.stream(Month.values())
			.map(month -> getZeitabschnittFullMonth(monthVerfuegungZeitabschnittMap.get(month)))
			.sorted()
			.collect(Collectors.toList());
	}

	private VerfuegungZeitabschnitt getZeitabschnittFullMonth(List<VerfuegungZeitabschnitt> monthlyZeitabschnitte) {
		if(monthlyZeitabschnitte.size() == 1) {
			return monthlyZeitabschnitte.get(0);
		}

		//Die Zeitabschnitte sollen auf den ganzen Monat erweitert werden. Danach haben alle Zeitabschnitte eine Gültigkeit
		//vom ersten bis zum letzten Tag es Monats, die Calculation Input Werte werden dabei Prozentual zu den anzahl Tagen ausgerechnet
		List<VerfuegungZeitabschnitt> strechedZeitabschnitte = strechZeitabschnitteToFullMonth(monthlyZeitabschnitte);
		return mergeZeitabschnitte(strechedZeitabschnitte);
	}

	private VerfuegungZeitabschnitt mergeZeitabschnitte(List<VerfuegungZeitabschnitt> zeitabschnitte) {
		VerfuegungZeitabschnitt zeitabschnittMerged = null;

		for(VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitte) {
			if(zeitabschnittMerged == null) {
				zeitabschnittMerged = zeitabschnitt;
				continue;
			}

			zeitabschnittMerged.add(zeitabschnitt);
		}

		return zeitabschnittMerged;
	}

	private List<VerfuegungZeitabschnitt> strechZeitabschnitteToFullMonth(List<VerfuegungZeitabschnitt> zeitabschnitteList) {
		return zeitabschnitteList.stream()
			.map(this::strechZeitabschnittToFullMonth)
			.collect(Collectors.toList());
	}

	private VerfuegungZeitabschnitt strechZeitabschnittToFullMonth(VerfuegungZeitabschnitt zeitabschnittBeforeStretching) {
		VerfuegungZeitabschnitt zeitabschnittFullMonth = createZeitabschnittGueltigFullMonth(zeitabschnittBeforeStretching);

		handleCalculationOfInputValues(zeitabschnittFullMonth, zeitabschnittBeforeStretching);
		handleGueltigkeitOfBemerkungen(zeitabschnittFullMonth, zeitabschnittBeforeStretching.getGueltigkeit());
		return zeitabschnittFullMonth;
	}

	private void handleCalculationOfInputValues(VerfuegungZeitabschnitt zeitabschnittSteched, VerfuegungZeitabschnitt zeitabschnittBeforeStretching) {
		final int lengthOfMonth = zeitabschnittBeforeStretching.getGueltigkeit().getGueltigAb().lengthOfMonth();
		final long numberOfDaysBeforeStreching = zeitabschnittBeforeStretching.getGueltigkeit().getDays();

		double percentageOfDays = 100.0 / lengthOfMonth * numberOfDaysBeforeStreching;
		zeitabschnittSteched.calculateInputValuesProportionaly(percentageOfDays);
	}

	private VerfuegungZeitabschnitt createZeitabschnittGueltigFullMonth(VerfuegungZeitabschnitt zeitabschnitt) {
		VerfuegungZeitabschnitt zeitabschnittFullMonth = new VerfuegungZeitabschnitt(zeitabschnitt);
		zeitabschnittFullMonth.setGueltigkeit(zeitabschnitt.getGueltigkeit().withFullMonths());
		return zeitabschnittFullMonth;
	}

	private void handleGueltigkeitOfBemerkungen(
		VerfuegungZeitabschnitt zeitabschnittFullMonth,
		DateRange gueltigkeitBeforeStrechting) {

		//Alle Bemerkungen sollen nur solange gültig sein, wie der Zeitabschnitt vor dem Strecken
		setGueltigkeitBeforeStrechtingToAllBemerkungen(zeitabschnittFullMonth, gueltigkeitBeforeStrechting);

		//Ausser die Bemerkgung Verfuegung_mit_anspruch, die soll so lange gültig sein wie der gestreckte Zeitabschnitt
		setGueltigkeitOfZeitabschnittToBemerkung(zeitabschnittFullMonth, MsgKey.VERFUEGUNG_MIT_ANSPRUCH);
	}

	private void setGueltigkeitOfZeitabschnittToBemerkung(
		VerfuegungZeitabschnitt zeitabschnittFullMonth,
		MsgKey msgKey) {
		zeitabschnittFullMonth.getBemerkungenDTOList().getBemerkungenStream()
			.filter(bemerkung -> bemerkung.getMsgKey() == msgKey)
			.forEach(verfuegungsBemerkungDTO -> verfuegungsBemerkungDTO.setGueltigkeit(zeitabschnittFullMonth.getGueltigkeit()));
	}

	private void setGueltigkeitBeforeStrechtingToAllBemerkungen(
		VerfuegungZeitabschnitt zeitabschnittFullMonth,
		DateRange gueltigkeitBeforeStrechting) {

		zeitabschnittFullMonth.getBemerkungenDTOList()
			.getBemerkungenStream()
			.forEach(bemerkung -> bemerkung.setGueltigkeit(gueltigkeitBeforeStrechting));
	}


}
