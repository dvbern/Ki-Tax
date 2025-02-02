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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.KitaxUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;

/**
 * Regel für die Erstellung der Zeitabschnitte der Betreuungspensen
 * Verweis 16.9.3
 */
public class BetreuungspensumAbschnittRule extends AbstractAbschnittRule {

	private final KitaxUebergangsloesungParameter kitaxParameter;

	public BetreuungspensumAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale, KitaxUebergangsloesungParameter kitaxParameter) {
		super(RuleKey.BETREUUNGSPENSUM, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);

		this.kitaxParameter = kitaxParameter;
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
		List<VerfuegungZeitabschnitt> betreuungspensumAbschnitte = new ArrayList<>();
		Set<BetreuungspensumContainer> betreuungspensen = betreuung.getBetreuungspensumContainers();

		final boolean possibleKitaxRechner = KitaxUtil.isGemeindeWithKitaxUebergangsloesung(betreuung.extractGemeinde())
			&& betreuung.getBetreuungsangebotTyp().isJugendamt();


		// es handelt sich um FEBR und wir müssen die Pensen gemäss dem alten System umrechnen.
		if (possibleKitaxRechner) {
			handleKitaxRechner(betreuungspensen, betreuung, betreuungspensumAbschnitte);
		} else {
			for (BetreuungspensumContainer betreuungspensumContainer : betreuungspensen) {
				Betreuungspensum betreuungspensum = betreuungspensumContainer.getBetreuungspensumJA();
				betreuungspensumAbschnitte.add(toVerfuegungZeitabschnitt(betreuungspensum, betreuung));
			}
		}

		return betreuungspensumAbschnitte;
	}

	private void handleKitaxRechner(
		Set<BetreuungspensumContainer> betreuungspensen,
		Betreuung betreuung,
		List<VerfuegungZeitabschnitt> betreuungspensumAbschnitte) {
		List<Betreuungspensum> pensenToUse = new ArrayList<>();
		for (BetreuungspensumContainer betreuungspensumContainer : betreuungspensen) {

			Betreuungspensum betreuungspensum = betreuungspensumContainer.getBetreuungspensumJA();

			if (KitaxUtil.isCompletePensumASIV(kitaxParameter, betreuungspensum)) {
				pensenToUse.add(betreuungspensum);
				continue;
			}

			boolean recalculationNecessary = false;

			// Eine Kopie erstellen, da die Daten nicht veraendert werden duerfen!
			Betreuungspensum copy = initBetreuungspensumCopy(betreuungspensum);

			// das komplette Pensum liegt innerhalb von FEBR
			if (kitaxParameter.getStadtBernAsivStartDate().isAfter(betreuungspensum.getGueltigkeit().getGueltigBis())) {
				recalculationNecessary = true;
			}

			if (KitaxUtil.isPensumMixedFEBRandASIV(kitaxParameter, betreuungspensum)) {
				recalculationNecessary = true;
				copy.getGueltigkeit().setGueltigBis(kitaxParameter.getStadtBernAsivStartDate().minusDays(1l));

				Betreuungspensum restPensumAsiv = initBetreuungspensumCopy(betreuungspensum);
				restPensumAsiv.getGueltigkeit().setGueltigAb(kitaxParameter.getStadtBernAsivStartDate());
				restPensumAsiv.getGueltigkeit().setGueltigBis(betreuungspensum.getGueltigkeit().getGueltigBis());
				restPensumAsiv.setPensum(betreuungspensum.getPensum());
				pensenToUse.add(restPensumAsiv);
			}

			boolean betreuungInGemeinde;
			// wenn das Flag betreuungInGemeinde nicht aktiviert ist, ist der BG beim Ki-Tax Rechner immer 0.
			// dann muss auch nichts umgerechnet werden. Dies verhindert spätere Nullpointer, wenn die Öffnungszeiten
			// abgefragt werden.
			if (betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA() != null) {
				betreuungInGemeinde = betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA().getBetreuungInGemeinde();
			} else {
				betreuungInGemeinde = betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungGS().getBetreuungInGemeinde();
			}
			if (!betreuungInGemeinde) {
				recalculationNecessary = false;
			}

			if (recalculationNecessary) {
				String kitaName = betreuung.getInstitutionStammdaten().getInstitution().getName();
				final BigDecimal convertedPensum = KitaxUtil.recalculatePensumKonvertierung(kitaName, kitaxParameter, betreuungspensum);
				copy.setPensum(convertedPensum);
				pensenToUse.add(copy);
			}
		}

		for (Betreuungspensum pensum : pensenToUse) {
			betreuungspensumAbschnitte.add(toVerfuegungZeitabschnitt(pensum, betreuung));
		}

		pensenToUse.clear();
	}

	/**
	 * @param betreuungspensum zu konvertiertendes Betreuungspensum
	 * @return VerfuegungZeitabschnitt mit gleicher gueltigkeit und uebernommenem betreuungspensum
	 */
	@Nonnull
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(
		@Nonnull Betreuungspensum betreuungspensum,
		@Nonnull Betreuung betreuung
	) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(betreuungspensum.getGueltigkeit());

		// Eigentliches Betreuungspensum
		zeitabschnitt.setBetreuungspensumProzentForAsivAndGemeinde(betreuungspensum.getPensum());
		zeitabschnitt.setMonatlicheBetreuungskostenForAsivAndGemeinde(betreuungspensum.getMonatlicheBetreuungskosten());
		zeitabschnitt.setPensumUnitForAsivAndGemeinde(betreuungspensum.getUnitForDisplay());
		if (betreuungspensum.getBetreuteTage() != null) {
			zeitabschnitt.setAnwesenheitsTageProMonat(betreuungspensum.getBetreuteTage());
		}
		// Anzahl Haupt und Nebenmahlzeiten übernehmen
		zeitabschnitt.setMonatlicheHauptmahlzeitenForAsivAndGemeinde(betreuungspensum.getMonatlicheHauptmahlzeiten());
		zeitabschnitt.setMonatlicheNebenmahlzeitenForAsivAndGemeinde(betreuungspensum.getMonatlicheNebenmahlzeiten());
		// Tarife der Mahlzeiten übernehmen
		zeitabschnitt.setTarifHauptmahlzeitForAsivAndGemeinde(betreuungspensum.getTarifProHauptmahlzeit());
		zeitabschnitt.setTarifNebenmahlzeitForAsivAndGemeinde(betreuungspensum.getTarifProNebenmahlzeit());
		Familiensituation famSit = betreuung.extractGesuch().extractFamiliensituation();
		boolean verguenstigungBeantrag = famSit != null && !famSit.isKeineMahlzeitenverguenstigungBeantragt();
		zeitabschnitt.setVerguenstigungMahlzeitenBeantragtForAsivAndGemeinde(verguenstigungBeantrag);
		//TFO Stuendliche Vollkosten übernehmen
		if (betreuungspensum.getStuendlicheVollkosten() != null) {
			zeitabschnitt.setStuendlicheVollkosten(betreuungspensum.getStuendlicheVollkosten());
		}

		// ErweiterteBetreuung-Flag gesetzt?
		boolean besondereBeduerfnisse = betreuung.hasErweiterteBetreuung();

		// Falls die Betreuung im Status UNBEKANNTE_INSTITUTION ist, soll die Pauschale immer berechnet werden
		boolean besondereBeduerfnisseBestaetigt =
			besondereBeduerfnisse
			&& (betreuung.isErweiterteBeduerfnisseBestaetigt()
				|| betreuung.getBetreuungsstatus() == Betreuungsstatus.UNBEKANNTE_INSTITUTION);

		zeitabschnitt.setBesondereBeduerfnisseBestaetigtForAsivAndGemeinde(besondereBeduerfnisseBestaetigt);

		// Betreuung in Gemeinde?
		ErweiterteBetreuung erweiterteBetreuung = betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
		if (erweiterteBetreuung != null && erweiterteBetreuung.getBetreuungInGemeinde() != null) {
			zeitabschnitt.setBetreuungInGemeindeForAsivAndGemeinde(erweiterteBetreuung.getBetreuungInGemeinde());
		}

		// Besondere Beduerfnisse Betrag konfigurierbar?
		if (besondereBeduerfnisseBestaetigt && betreuung.hasErweiterteBeduerfnisseBetrag()) {
			BigDecimal erweitereteBeduerfnisseBetrag = betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA().getErweitereteBeduerfnisseBetrag();
			zeitabschnitt.setBesondereBeduerfnisseBetragForAsivAndGemeinde(erweitereteBeduerfnisseBetrag);
		}

		// Die Institution muss die besonderen Bedürfnisse bestätigt haben
		if (besondereBeduerfnisseBestaetigt) {
			zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(
				MsgKey.ERWEITERTE_BEDUERFNISSE_MSG,
				getLocale());
		}

		zeitabschnitt.setBetreuungInFerienzeit(Boolean.TRUE.equals(betreuungspensum.getBetreuungInFerienzeit()));

		handleHoehereBeitraege(betreuung, zeitabschnitt);

		return zeitabschnitt;
	}

	private void handleHoehereBeitraege(Betreuung betreuung, VerfuegungZeitabschnitt zeitabschnitt) {
		// hier kind hoehereBeitraegeWegenBeeintraechtigungBeantragen pruefen
		// dazu auch die gegebene Wert im Drop down beruchsichtigen wenn gesetzt und checkbox dann Wert im Input schreiben
		if (Boolean.TRUE.equals(betreuung.getKind().getKindJA().getHoehereBeitraegeWegenBeeintraechtigungBeantragen()) && betreuung.getBedarfsstufe() != null) {
			if (Objects.requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA()).isErweiterteBeduerfnisseBestaetigt()) {
				zeitabschnitt.setBedarfsstufeForAsivAndGemeinde(betreuung.getBedarfsstufe());
				zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(
					betreuung.getBedarfsstufe() == Bedarfsstufe.KEINE ?
						MsgKey.BEDARFSSTUFE_NICHT_GEWAEHRT_MSG :
						MsgKey.BEDARFSSTUFE_MSG,
					getLocale(),
					ServerMessageUtil.translateEnumValue(betreuung.getBedarfsstufe(), getLocale(), betreuung.extractGesuch().extractMandant()));
			} else {
				zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(
					MsgKey.BEDARFSSTUFE_NICHT_VERRECHNET_MSG,
					getLocale());
			}
		}
	}

	private Betreuungspensum initBetreuungspensumCopy(Betreuungspensum original) {
		// das pensum muss kopiert werden, ansonsten wird es in der DB überschrieben
		Betreuungspensum copy = new Betreuungspensum();
		copy.setMonatlicheBetreuungskosten(original.getMonatlicheBetreuungskosten());
		copy.setMonatlicheHauptmahlzeiten(original.getMonatlicheHauptmahlzeiten());
		copy.setMonatlicheNebenmahlzeiten(original.getMonatlicheNebenmahlzeiten());
		copy.setTarifProHauptmahlzeit(original.getTarifProHauptmahlzeit());
		copy.setTarifProNebenmahlzeit(original.getTarifProNebenmahlzeit());
		copy.setGueltigkeit(original.getGueltigkeit());

		return copy;
	}
}
