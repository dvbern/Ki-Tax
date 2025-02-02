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

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Regel bezüglich der Einreichungsfrist des Gesuchs:
 * - Wird ein Gesuch zu spät eingereicht, beginnt der Anspruch am 1. des Folgemonats
 * - Beispiel: Ein Gesuch wird am 5. September 2017 eingereicht. In diesem Fall ist erst per 1. Oktober 2017
 * ein Anspruch verfügbar.
 * D.h. für die Angebote „Kita“ und „Tageseltern – Kleinkinder“ ist im August und September kein Anspruch verfügbar.
 * Falls sie einen Platz haben, wird dieser zum privaten Tarif der Kita berechnet.
 * - Für die Angebote Tageseltern–Schulkinder und Tagesstätten entspricht der Anspruch dem gewünschten Pensum.
 * Ihnen wird für den Monat August aber der Volltarif verrechnet.
 * Verweis 16.11 Gesuch zu Speat
 */
public class EinreichungsfristAbschnittRule extends AbstractAbschnittRule {

	public EinreichungsfristAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.EINREICHUNGSFRIST, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> einreichungsfristAbschnitte = new ArrayList<>();
		Gesuch gesuch = platz.extractGesuch();
		LocalDate startDatum = gesuch.getRegelStartDatum();
		if (applyEinreichungsfristAbschnittStueckelung(platz) && startDatum != null) {
			LocalDate einreichefristStichtag = getEinreichefristStichtag(startDatum, platz.extractGesuch().extractMandant());
			if (platz.extractGesuchsperiode().getGueltigkeit().getGueltigAb().isBefore(einreichefristStichtag)) {
				VerfuegungZeitabschnitt abschnittVorAnspruch =
					createZeitabschnittBevorEinreichung(
						gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
						einreichefristStichtag.minusDays(1));
				if (abschnittVorAnspruch != null) {
					einreichungsfristAbschnitte.add(abschnittVorAnspruch);
				}
			}
		}
		return einreichungsfristAbschnitte;
	}

	private boolean applyEinreichungsfristAbschnittStueckelung(@Nonnull AbstractPlatz platz) {
		// Die Frist ist per default relevant bei Erstgesuch und "Erst-Betreuung", also wenn ein Platz in einer Mutation
		// neu hinzugekommen ist. Es kann aber sein, dass es zwar einen Vorgaenger gab, dieser aber nicht
		// verfuegt wurde, darum reicht es nicht, nur die VorgaengerID zu pruefen!
		return platz.extractGesuch().getTyp().isGesuch() || Objects.isNull(platz.getVorgaengerVerfuegung());
	}

	private LocalDate getEinreichefristStichtag(LocalDate startDatum, Mandant mandant) {
		return new EinreichefristVisitor().getEinreichefristCalculator(mandant.getMandantIdentifier()).getStichtagEinreichefrist(startDatum);
	}

	@Nullable
	private VerfuegungZeitabschnitt createZeitabschnittBevorEinreichung(@Nonnull LocalDate startGP, @Nonnull LocalDate tagBevorAnspruch) {
		// Der Anspruch beginnt erst am 1. des Monats der Einreichung
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(startGP, tagBevorAnspruch);
		verfuegungZeitabschnitt.setZuSpaetEingereichtForAsivAndGemeinde(true);
		// Sicherstellen, dass nicht der ganze Zeitraum vor dem Einreichungsdatum liegt
		if (verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().isAfter(verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb())) {
			return verfuegungZeitabschnitt;
		}
		return null;
	}
}
