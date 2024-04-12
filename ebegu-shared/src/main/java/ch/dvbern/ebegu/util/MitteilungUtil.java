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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static ch.dvbern.ebegu.util.Constants.DATE_FORMATTER;

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
		betreuungsmitteilung.setSubject(ServerMessageUtil.getMessage("mutationsmeldung_betreff", locale, currentBenutzer.getMandant()));
	}

	@Nonnull
	public static String createNachrichtForMutationsmeldung(
		@Nonnull Set<BetreuungsmitteilungPensum> changedBetreuungen,
		boolean mahlzeitenverguenstigungEnabled,
		@Nonnull Locale locale,
		@Nonnull BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp,
		@Nonnull BigDecimal multiplier
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
			message.append(createNachrichtForMutationsmeldung(betreuungspensumContainer, mahlzeitenverguenstigungEnabled, index[0], locale, betreuungspensumAnzeigeTyp, multiplier));
			index[0]++;
		});
		return message.toString();
	}

	@Nonnull
	private static String createNachrichtForMutationsmeldung(
		@Nonnull BetreuungsmitteilungPensum pensumMitteilung,
		boolean mahlzeitenverguenstigungEnabled, int index,
		@Nonnull Locale locale,
		@Nonnull BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp,
		@Nonnull BigDecimal multiplier
	) {
		String datumAb = DATE_FORMATTER.format(pensumMitteilung.getGueltigkeit().getGueltigAb());
		String datumBis = DATE_FORMATTER.format(pensumMitteilung.getGueltigkeit().getGueltigBis());
		Mandant mandant = Objects.requireNonNull(pensumMitteilung.getBetreuungsmitteilung().getDossier().getFall().getMandant());

		BigDecimal monatlicheBetreuungskosten = pensumMitteilung.getMonatlicheBetreuungskosten();
		final BigDecimal multipliedPensum = MathUtil.DEFAULT.multiply(pensumMitteilung.getPensum(), multiplier);
		if (betreuungspensumAnzeigeTyp == BetreuungspensumAnzeigeTyp.NUR_MAHLZEITEN) {
			monatlicheBetreuungskosten = pensumMitteilung.getMonatlicheBetreuungskosten().divide(multipliedPensum, 2, RoundingMode.HALF_UP);
		}
		if (mahlzeitenverguenstigungEnabled) {
			BigDecimal hauptmahlzeiten = pensumMitteilung.getMonatlicheHauptmahlzeiten();
			BigDecimal nebemahlzeiten = pensumMitteilung.getMonatlicheNebenmahlzeiten();

			BigDecimal tarifHaupt = pensumMitteilung.getTarifProHauptmahlzeit();

			BigDecimal tarifNeben = pensumMitteilung.getTarifProNebenmahlzeit();

			return ServerMessageUtil.getMessage(
				betreuungspensumAnzeigeTyp == BetreuungspensumAnzeigeTyp.NUR_STUNDEN ?
					"mutationsmeldung_message_mahlzeitverguenstigung_mit_tarif_stunden" :
					"mutationsmeldung_message_mahlzeitverguenstigung_mit_tarif",
				locale,
				mandant,
				index,
				datumAb,
				datumBis,
				multipliedPensum,
				monatlicheBetreuungskosten,
				hauptmahlzeiten,
				nebemahlzeiten,
				tarifHaupt,
				tarifNeben)
				+ createNachrichtEingewoehnungPauschale(locale, mandant, pensumMitteilung.getEingewoehnungPauschale());
		}
		return ServerMessageUtil.getMessage(
			getMutationsmeldungTranslationKey(betreuungspensumAnzeigeTyp),
			locale,
			mandant,
			index,
			datumAb,
			datumBis,
			multipliedPensum,
			monatlicheBetreuungskosten)
			+ createNachrichtEingewoehnungPauschale(locale, mandant, pensumMitteilung.getEingewoehnungPauschale());
	}

	private static String createNachrichtEingewoehnungPauschale(
		Locale locale,
		Mandant mandant,
		@Nullable EingewoehnungPauschale eingewoehnungPauschale
	) {
		if (eingewoehnungPauschale == null) {
			return "";
		}

		BigDecimal pauschale = eingewoehnungPauschale.getPauschale();
		String eingewoehnungDatumAb = DATE_FORMATTER.format(eingewoehnungPauschale.getGueltigkeit().getGueltigAb());
		String eingewoehnungDatumBis = DATE_FORMATTER.format(eingewoehnungPauschale.getGueltigkeit().getGueltigBis());

		return ServerMessageUtil.getMessage(
			"mutationsmeldung_message_eingewoehnung_pauschale",
			locale,
			mandant,
			eingewoehnungDatumAb,
			eingewoehnungDatumBis,
			pauschale);
	}

	@Nonnull
	private static String getMutationsmeldungTranslationKey(@Nonnull BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp) {
		if (betreuungspensumAnzeigeTyp == BetreuungspensumAnzeigeTyp.NUR_MAHLZEITEN) {
			return "mutationsmeldung_message_mittagstisch";
		}
		return betreuungspensumAnzeigeTyp == BetreuungspensumAnzeigeTyp.NUR_STUNDEN ?
			"mutationsmeldung_message_stunden" :
			"mutationsmeldung_message";
	}
}
