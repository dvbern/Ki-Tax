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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.PensumUnits;

public final class MitteilungUtil {

	private MitteilungUtil() {
		// Should not be initialized
	}

	public static void initializeBetreuungsmitteilung(
		@Nonnull Betreuungsmitteilung betreuungsmitteilung,
		@Nonnull Betreuung betreuung,
		@Nonnull Benutzer currentBenutzer,
		Locale locale
	) {
		betreuungsmitteilung.setDossier(betreuung.extractGesuch().getDossier());
		betreuungsmitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
		betreuungsmitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
		betreuungsmitteilung.setSender(currentBenutzer);
		betreuungsmitteilung.setEmpfaenger(betreuung.extractGesuch().getDossier().getFall().getBesitzer());
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		betreuungsmitteilung.setSubject(ServerMessageUtil.getMessage("mutationsmeldung_betreff", locale));
	}

	@Nonnull
	public static String createNachrichtForMutationsmeldung(
		@Nonnull Set<BetreuungsmitteilungPensum> changedBetreuungen,
		boolean mahlzeitenverguenstigungEnabled,
		@Nonnull Locale locale
	) {
		final StringBuilder message = new StringBuilder();
		final int[] index = { 1 }; // Array, weil es final sein muss, damit es in LambdaExpression verwendet werden darf...

		final List<BetreuungsmitteilungPensum> betreuungspensumContainers =
			changedBetreuungen
				.stream()
				.sorted(Comparator.comparing(o -> o.getGueltigkeit().getGueltigAb()))
				.collect(Collectors.toList());

		betreuungspensumContainers.forEach(betreuungspensumContainer -> {
			if (index[0] > 1) {
				message.append('\n');
			}
			message.append(createNachrichtForMutationsmeldung(betreuungspensumContainer, mahlzeitenverguenstigungEnabled, index[0], locale));
			index[0]++;
		});
		return message.toString();
	}

	@Nonnull
	private static String createNachrichtForMutationsmeldung(
		@Nonnull BetreuungsmitteilungPensum pensumMitteilung,
		boolean mahlzeitenverguenstigungEnabled, int index,
		@Nonnull Locale locale
	) {
		String datumAb = Constants.DATE_FORMATTER.format(pensumMitteilung.getGueltigkeit().getGueltigAb());
		String datumBis = Constants.DATE_FORMATTER.format(pensumMitteilung.getGueltigkeit().getGueltigBis());

		if (mahlzeitenverguenstigungEnabled) {
			BigDecimal hauptmahlzeiten = BigDecimal.valueOf(pensumMitteilung.getMonatlicheHauptmahlzeiten());
			BigDecimal nebemahlzeiten = BigDecimal.valueOf(pensumMitteilung.getMonatlicheNebenmahlzeiten());

			BigDecimal tarifHaupt = BigDecimal.ZERO;
			if (pensumMitteilung.getTarifProHauptmahlzeit() != null) {
				tarifHaupt = pensumMitteilung.getTarifProHauptmahlzeit();
			}
			BigDecimal tarifNeben = BigDecimal.ZERO;
			if (pensumMitteilung.getTarifProNebenmahlzeit() != null) {
				tarifNeben = pensumMitteilung.getTarifProNebenmahlzeit();
			}
			return ServerMessageUtil.getMessage(
				"mutationsmeldung_message_mahlzeitverguenstigung_mit_tarif", locale, index,
				datumAb,
				datumBis,
				pensumMitteilung.getPensum(),
				pensumMitteilung.getMonatlicheBetreuungskosten(),
				hauptmahlzeiten,
				nebemahlzeiten,
				tarifHaupt,
				tarifNeben);
		} else {
			return ServerMessageUtil.getMessage("mutationsmeldung_message", locale, index,
				datumAb,
				datumBis,
				pensumMitteilung.getPensum(),
				pensumMitteilung.getMonatlicheBetreuungskosten());
		}
	}

	@Nonnull
	public static String createNachrichtForMutationsmeldungFromAbweichung(
		@Nonnull Betreuung betreuung,
		boolean mahlzeitenverguenstigungEnabled,
		@Nonnull Locale locale
	) {
		final StringBuilder message = new StringBuilder();
		final int[] index = { 1 }; // Array, weil es final sein muss, damit es in LambdaExpression verwendet werden darf...
		final List<BetreuungspensumAbweichung> abweichungen = betreuung.getBetreuungspensumAbweichungen().stream().filter(
			betreuungspensumAbweichung ->
				(!betreuungspensumAbweichung.isNew()
					|| (betreuungspensumAbweichung.getVertraglichesPensum() != null && betreuungspensumAbweichung.getVertraglicheKosten() != null))
		).collect(Collectors.toList());

		abweichungen.forEach(abweichung -> {
			if (index[0] > 1) {
				message.append('\n');
			}
			message.append(createNachrichtForMutationsmeldungFromAbweichung(abweichung, mahlzeitenverguenstigungEnabled, index[0], locale));
			index[0]++;
		});
		return message.toString();
	}

	@Nonnull
	private static String createNachrichtForMutationsmeldungFromAbweichung(
		@Nonnull BetreuungspensumAbweichung abweichung,
		boolean mahlzeitenverguenstigungEnabled,
		int index,
		@Nonnull Locale locale
	) {
		BigDecimal multiplier = abweichung.getUnitForDisplay() == PensumUnits.DAYS
			? Constants.MULTIPLIER_KITA
			: Constants.MULTIPLIER_TAGESFAMILIEN;
		BigDecimal pensumPercentage = MathUtil.DEFAULT.divide(abweichung.getPensum(), multiplier);
		String datumAb = Constants.DATE_FORMATTER.format(abweichung.getGueltigkeit().getGueltigAb());
		String datumBis = Constants.DATE_FORMATTER.format(abweichung.getGueltigkeit().getGueltigBis());
		BigDecimal kosten = abweichung.getMonatlicheBetreuungskosten();

		if (mahlzeitenverguenstigungEnabled) {
			BigDecimal hauptmahlzeiten = BigDecimal.valueOf(abweichung.getMonatlicheHauptmahlzeiten());
			BigDecimal nebenmahlzeiten = BigDecimal.valueOf(abweichung.getMonatlicheNebenmahlzeiten());
			return ServerMessageUtil.getMessage(
				"mutationsmeldung_message_mahlzeitverguenstigung",	locale, index,
				datumAb, datumBis, pensumPercentage, kosten, hauptmahlzeiten, nebenmahlzeiten);
		} else {
			return ServerMessageUtil.getMessage(
				"mutationsmeldung_message", locale, index,
				datumAb, datumBis, pensumPercentage, kosten);
		}
	}
}
