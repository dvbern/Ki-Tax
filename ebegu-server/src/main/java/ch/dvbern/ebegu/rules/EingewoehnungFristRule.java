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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

public class EingewoehnungFristRule extends AbstractAbschlussRule {

	private Locale locale;

	protected EingewoehnungFristRule(@Nonnull Locale locale, boolean isDebug) {
		super(isDebug);
		this.locale = locale;
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		if (!platz.isAngebotSchulamt()) {
			Betreuung betreuung = (Betreuung) platz;
			if (betreuung.isEingewoehnung()) {
				return verlaengtAbschnitte(zeitabschnitte, betreuung.extractGesuch());
			}
		}

		return zeitabschnitte;
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> verlaengtAbschnitte(
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte,
		Gesuch gesuch) {
		List<VerfuegungZeitabschnitt> result = new LinkedList<>();
		VerfuegungZeitabschnitt vorgaenger = null;
		boolean found = false; //wenn wir eine erweitert haben es ist fertig als es kann wieder spaeter zu 0 sinken
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitte) {
			if (vorgaenger != null) {
				VerfuegungZeitabschnitt eingewoehnung = null;
				if (zeitabschnitt.getRelevantBgCalculationInput().getAnspruchspensumProzent() > 0
					&& vorgaenger.getRelevantBgCalculationInput().getAnspruchspensumProzent() <= 0
					&& !found) {
					// wir verlaengern der Anspruch aber die Input muessen von vorgaenger kopiert werden
					eingewoehnung = createEingewoehnungAbschnitt(vorgaenger, zeitabschnitt, zeitabschnitt.getGueltigkeit());
					if (gesuch.getGesuchsperiode()
						.getGueltigkeit()
						.getGueltigAb()
						.isBefore(eingewoehnung.getGueltigkeit().getGueltigAb())) {
						if (eingewoehnung.getGueltigkeit().getGueltigAb()
							.minusMonths(1)
							.isAfter(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb())) {
							eingewoehnung.getGueltigkeit().setGueltigAb(eingewoehnung.getGueltigkeit().getGueltigAb().minusMonths(1));
						} else {
							eingewoehnung.getGueltigkeit().setGueltigAb(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
						}
						eingewoehnung.getGueltigkeit().setGueltigBis(zeitabschnitt.getGueltigkeit().getGueltigAb().minusDays(1));
						vorgaenger.getGueltigkeit().setGueltigBis(eingewoehnung.getGueltigkeit().getGueltigAb().minusDays(1));
						if (vorgaenger.getGueltigkeit()
							.getGueltigAb()
							.compareTo(vorgaenger.getGueltigkeit().getGueltigBis()) >= 0) {
							//get the original Gueltigab
							LocalDate gueltigAb = eingewoehnung.getGueltigkeit().getGueltigAb();
							//set the new value for the eingewoehnung
							eingewoehnung.getGueltigkeit().setGueltigAb(vorgaenger.getGueltigkeit().getGueltigAb());
							vorgaenger = null;
							List<VerfuegungZeitabschnitt> resultOld = result;
							result = new ArrayList<>();
							for(VerfuegungZeitabschnitt zeitabschnittResult: resultOld){
								//Zeitabschnitt ist bevor den ersten Gueltigkeit OK
								if(zeitabschnittResult.getGueltigkeit().getGueltigBis().isBefore(gueltigAb)){
									result.add(zeitabschnittResult);
								}
								else {
									VerfuegungZeitabschnitt zusaetzlicheEingewoehnung = createEingewoehnungAbschnitt(zeitabschnittResult, eingewoehnung, zeitabschnittResult.getGueltigkeit());
									//Gueltigab ist bevor den Eingewoehnung setzen gueltigBis ab zu gueltigAb
									if (zeitabschnittResult.getGueltigkeit().getGueltigAb().isBefore(gueltigAb)){
										zeitabschnittResult.getGueltigkeit().setGueltigBis(gueltigAb.minusDays(1));
										result.add(zeitabschnittResult);
										//setzen der GueltigAb bei der zusaetzlicheEingewoehnung Abschnitt
										zusaetzlicheEingewoehnung.getGueltigkeit().setGueltigAb(gueltigAb);
									}
									result.add(zusaetzlicheEingewoehnung);
								}
							}
						}
					}
					found = true;
				}
				if (vorgaenger != null) {
					result.add(vorgaenger);
				}
				if (eingewoehnung != null) {
					result.add(eingewoehnung);
				}
			}
			vorgaenger = zeitabschnitt;
		}
		result.add(vorgaenger);

		return result;
	}

	private VerfuegungZeitabschnitt createEingewoehnungAbschnitt(@Nonnull VerfuegungZeitabschnitt baseAbschnitt, @Nonnull VerfuegungZeitabschnitt abschnittMitAnspruch, @Nonnull DateRange gueltigkeit) {
		VerfuegungZeitabschnitt eingewoehnung = new VerfuegungZeitabschnitt(baseAbschnitt);
		eingewoehnung.setAnspruchspensumProzentForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput().getAnspruchspensumProzent());
		eingewoehnung.setErwerbspensumGS1ForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput().getErwerbspensumGS1());
		eingewoehnung.setErwerbspensumGS2ForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput().getErwerbspensumGS2());
		eingewoehnung.setGueltigkeit(new DateRange(gueltigkeit));
		eingewoehnung.getRelevantBgCalculationInput().addBemerkung(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG, locale);
		if(eingewoehnung.getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH)){
			eingewoehnung.getBemerkungenList().removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH);
		}
		return  eingewoehnung;
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}
}
