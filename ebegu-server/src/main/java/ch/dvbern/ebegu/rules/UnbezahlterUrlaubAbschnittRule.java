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
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.UnbezahlterUrlaub;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import static java.util.Objects.requireNonNull;

/**
 * Regel für unbezahlten Urlaub. In dem Teil des Urlaubs, welcher 3 Monate übersteigt, verfällt der Anspruch (für dieses Erwerbspensum!)
 */
public class UnbezahlterUrlaubAbschnittRule extends AbstractAbschnittRule {


	public UnbezahlterUrlaubAbschnittRule(@Nonnull DateRange validityPeriod) {
		super(RuleKey.UNBEZAHLTER_URLAUB, RuleType.GRUNDREGEL_DATA, validityPeriod);
	}

	/**
	 * Die Abwesenheiten der Betreuung werden zuerst nach gueltigkeit sortiert. Danach suchen wir die erste lange Abweseneheit und erstellen
	 * die 2 entsprechenden Zeitabschnitte. Alle anderen Abwesenheiten werden nicht beruecksichtigt
	 * Sollte es keine lange Abwesenheit geben, wird eine leere Liste zurueckgegeben
	 * Nur fuer Betreuungen die isAngebotJugendamtKleinkind
	 */
	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> resultlist = new ArrayList<>();
		if (requireNonNull(betreuung.getBetreuungsangebotTyp()).isAngebotJugendamtKleinkind()) {
			List<VerfuegungZeitabschnitt> erwerbspensumAbschnitte = new ArrayList<>();
			Gesuch gesuch = betreuung.extractGesuch();
			if (gesuch.getGesuchsteller1() != null) {
				erwerbspensumAbschnitte.addAll(getErwerbspensumAbschnittForGesuchsteller(gesuch, gesuch.getGesuchsteller1(), false));
			}
			if (gesuch.getGesuchsteller2() != null) {
				erwerbspensumAbschnitte.addAll(getErwerbspensumAbschnittForGesuchsteller(gesuch, gesuch.getGesuchsteller2(), true));
			}
			return erwerbspensumAbschnitte;
		}
		return resultlist;
	}

	/**
	 * geht durch die Erwerpspensen des Gesuchstellers und gibt Abschnitte zurueck
	 *
	 * @param gesuchsteller Der Gesuchsteller dessen Erwerbspensumcontainers zu Abschnitte konvertiert werden
	 * @param gs2 handelt es sich um gesuchsteller1 -> false oder gesuchsteller2 -> true
	 */
	@Nonnull
	private List<VerfuegungZeitabschnitt> getErwerbspensumAbschnittForGesuchsteller(@Nonnull Gesuch gesuch, @Nonnull GesuchstellerContainer gesuchsteller, boolean gs2) {
		List<VerfuegungZeitabschnitt> ewpAbschnitte = new ArrayList<>();
		Set<ErwerbspensumContainer> ewpContainers = gesuchsteller.getErwerbspensenContainersNotEmpty();
		for (ErwerbspensumContainer erwerbspensumContainer : ewpContainers) {
			Erwerbspensum erwerbspensumJA = erwerbspensumContainer.getErwerbspensumJA();
			Objects.requireNonNull(erwerbspensumJA);
			if (erwerbspensumJA.getUnbezahlterUrlaub() != null) {
				final VerfuegungZeitabschnitt zeitabschnitt = toVerfuegungZeitabschnitt(gesuch, erwerbspensumJA, gs2);
				if (zeitabschnitt != null) {
					ewpAbschnitte.add(zeitabschnitt);
				}
			}

		}
		return ewpAbschnitte;
	}

	/**
	 * Konvertiert ein Erwerbspensum in einen Zeitabschnitt von entsprechender dauer und erwerbspensumGS1 (falls gs2=false)
	 * oder erwerpspensuGS2 (falls gs2=true)
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
			Objects.requireNonNull(familiensituation.getAenderungPer());
			if (!familiensituationErstgesuch.hasSecondGesuchsteller() && familiensituation.hasSecondGesuchsteller()) {
				// 1GS to 2GS
				if (gueltigkeit.getGueltigBis().isAfter(familiensituation.getAenderungPer())
					&& gueltigkeit.getGueltigAb().isBefore(familiensituation.getAenderungPer())) {
					gueltigkeit.setGueltigAb(familiensituation.getAenderungPer());
				}
			} else if (familiensituationErstgesuch.hasSecondGesuchsteller() && !familiensituation.hasSecondGesuchsteller()
				&& gueltigkeit.getGueltigAb().isBefore(familiensituation.getAenderungPer())
				&& gueltigkeit.getGueltigBis().isAfter(familiensituation.getAenderungPer())) {
				// 2GS to 1GS
				gueltigkeit.setGueltigBis(familiensituation.getAenderungPer().minusDays(1));
			}
			VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
			zeitabschnitt.setErwerbspensumGS2(0-erwerbspensumJA.getPensumInklZuschlag());
			zeitabschnitt.addBemerkung(RuleKey.UNBEZAHLTER_URLAUB, MsgKey.UNBEZAHLTER_URLAUB_MSG);
			return zeitabschnitt;
		}
		if (gs2 && !gesuch.isMutation()) {
			VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
			zeitabschnitt.setErwerbspensumGS2(0-erwerbspensumJA.getPensumInklZuschlag());
			zeitabschnitt.addBemerkung(RuleKey.UNBEZAHLTER_URLAUB, MsgKey.UNBEZAHLTER_URLAUB_MSG);
			return zeitabschnitt;
		}
		if (!gs2) {
			VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
			zeitabschnitt.setErwerbspensumGS1(0-erwerbspensumJA.getPensumInklZuschlag());
			zeitabschnitt.addBemerkung(RuleKey.UNBEZAHLTER_URLAUB, MsgKey.UNBEZAHLTER_URLAUB_MSG);
			return zeitabschnitt;
		}
		return null;
	}
}
