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

import java.util.List;
import java.util.ListIterator;
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
	private Boolean eingewoehnungAktiviert;

	protected EingewoehnungFristRule(@Nonnull Locale locale, boolean isDebug, Boolean eingewoehnungAktiviert) {
		super(isDebug);
		this.locale = locale;
		this.eingewoehnungAktiviert = eingewoehnungAktiviert;
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		if (!platz.isAngebotSchulamt()) {
			Betreuung betreuung = (Betreuung) platz;
			if (betreuung.isEingewoehnung() && eingewoehnungAktiviert) {
				return abschnitteVerlaengern(zeitabschnitte, betreuung.extractGesuch());
			}
		}

		return zeitabschnitte;
	}

	private List<VerfuegungZeitabschnitt> abschnitteVerlaengern(@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitteList, Gesuch gesuch) {
		if (zeitabschnitteList.size() < 2) {
			return zeitabschnitteList;
		}
		VerfuegungZeitabschnitt eingewoehnung = null;

		for (int i = 1; i < zeitabschnitteList.size(); i++) {
			VerfuegungZeitabschnitt current = zeitabschnitteList.get(i);
			VerfuegungZeitabschnitt previous = zeitabschnitteList.get(i - 1);

			if (isZeitabschnittEingewoehnung(current, previous)) {
				eingewoehnung = createEingewoehnungAbschnitt(previous, current);
				setEingewoehnungZeitabschnittGueltigkeit(gesuch, current, eingewoehnung);
				zeitabschnitteList.add(i, eingewoehnung);
				break;
			}
		}

		if (eingewoehnung == null) {
			return zeitabschnitteList;
		}

		adaptZeitabschnitteBeforeEingewoehnung(zeitabschnitteList, eingewoehnung);

		return zeitabschnitteList;
	}

	private void adaptZeitabschnitteBeforeEingewoehnung(List<VerfuegungZeitabschnitt> zeitabschnitteList, VerfuegungZeitabschnitt eingewoehnung) {
		ListIterator<VerfuegungZeitabschnitt> iterator = zeitabschnitteList.listIterator();
		while (iterator.hasNext()) {
			VerfuegungZeitabschnitt current = iterator.next();
			if (current.equals(eingewoehnung)) {
				break;
			}

			if (current.getGueltigkeit().endsBefore(eingewoehnung.getGueltigkeit())) {
				continue;
			}

			current.getGueltigkeit().setGueltigBis(eingewoehnung.getGueltigkeit().getGueltigAb().minusDays(1));

			if (!current.getGueltigkeit().isValid()) {
				iterator.remove();
			}
		}
	}

	private boolean isZeitabschnittEingewoehnung(VerfuegungZeitabschnitt current, VerfuegungZeitabschnitt previous) {
		return current.getRelevantBgCalculationInput().getAnspruchspensumProzent() > 0
		&& previous.getRelevantBgCalculationInput().getAnspruchspensumProzent() <= 0;
	}

	private VerfuegungZeitabschnitt createEingewoehnungAbschnitt(@Nonnull VerfuegungZeitabschnitt baseAbschnitt, @Nonnull VerfuegungZeitabschnitt abschnittMitAnspruch) {
		VerfuegungZeitabschnitt eingewoehnung = new VerfuegungZeitabschnitt(baseAbschnitt);
		eingewoehnung.setAnspruchspensumProzentForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput().getAnspruchspensumProzent());
		eingewoehnung.setErwerbspensumGS1ForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput().getErwerbspensumGS1());
		eingewoehnung.setErwerbspensumGS2ForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput().getErwerbspensumGS2());
		eingewoehnung.setGueltigkeit(abschnittMitAnspruch.getGueltigkeit());
		eingewoehnung.getRelevantBgCalculationInput().addBemerkung(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG, locale);
		if(eingewoehnung.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH)){
			eingewoehnung.getBemerkungenDTOList().removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH);
		}
		return  eingewoehnung;
	}

	private void setEingewoehnungZeitabschnittGueltigkeit(
			Gesuch gesuch,
			VerfuegungZeitabschnitt zeitabschnitt,
			VerfuegungZeitabschnitt eingewoehnung) {
		if (eingewoehnung.getGueltigkeit().getGueltigAb()
				.minusMonths(1)
				.isAfter(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb())) {
			eingewoehnung.getGueltigkeit().setGueltigAb(zeitabschnitt.getGueltigkeit().getGueltigAb().minusMonths(1));
		} else {
			eingewoehnung.getGueltigkeit().setGueltigAb(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
		}
		eingewoehnung.getGueltigkeit().setGueltigBis(zeitabschnitt.getGueltigkeit().getGueltigAb().minusDays(1));
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}
}
