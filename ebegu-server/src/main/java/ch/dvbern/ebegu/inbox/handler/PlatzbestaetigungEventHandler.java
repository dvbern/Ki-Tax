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

package ch.dvbern.ebegu.inbox.handler;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PlatzbestaetigungEventHandler extends BaseEventHandler<BetreuungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(PlatzbestaetigungEventHandler.class);

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private EinstellungService einstellungService;

	@Override
	protected void processEvent(@Nonnull LocalDateTime eventTime, @Nonnull EventType eventType,
		@Nonnull BetreuungEventDTO dto) {
		try {
			Optional<Betreuung> betreuungOpt = betreuungService.findGueltigeBetreuungByBGNummer(dto.getRefnr());
			if (!betreuungOpt.isPresent()) {
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: " + dto.getRefnr() + " existiert nicht!");
				return;
			}
			Betreuung betreuung = betreuungOpt.get();
			if (betreuung.getBetreuungsstatus().equals(Betreuungsstatus.WARTEN)) {
				//Update the Betreuung and check if all data are available
				if (setBetreuungDaten(betreuung, dto)) {
					betreuungService.betreuungPlatzBestaetigen(betreuung);
				} else {
					betreuungService.saveBetreuung(betreuung, false);
				}
			} else {
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: " + dto.getRefnr() + " war schon bestaetigt!");
			}
		} catch (Exception e) {
			LOG.error("Error while processing the record: " + dto.getRefnr() + " error: " + e.getMessage());
		}
	}

	/**
	 * Update the Betreuung Object and return if its ready for Bestaetigen
	 *
	 * @param betreuung
	 * @param dto
	 * @return
	 */
	private boolean setBetreuungDaten(Betreuung betreuung, BetreuungEventDTO dto) {
		boolean isReadyForBestaetigen = true;
		//erweiterte Betreuung muss true sein um der checkbox zu setzen! Sonst ist der Wert von DTO ignoriert
		ErweiterteBetreuung erweiterteBetreuung =
			betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
		if (erweiterteBetreuung != null && erweiterteBetreuung.getErweiterteBeduerfnisse()) {
			erweiterteBetreuung.setErweiterteBeduerfnisseBestaetigt(dto.getAusserordentlicherBetreuungsaufwand());
			betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(erweiterteBetreuung);
		}
		if (dto.getGemeindeName() != null) {
			//Check if Gemeinde Bern und if ja ob es ist gleich, sonst checkbox setzen
			Gemeinde gemeinde = betreuung.extractGemeinde();
			Gesuchsperiode gesuchsperiode = betreuung.extractGesuchsperiode();
			Einstellung zusaetzlicherGutscheinEnabled =
				einstellungService.findEinstellung(EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED,
					gemeinde,
					gesuchsperiode);
			if (zusaetzlicherGutscheinEnabled.getValueAsBoolean() && gemeinde.getName().equals("Bern") && !gemeinde.getName().equalsIgnoreCase(dto.getGemeindeName())) {
				if (erweiterteBetreuung == null) {
					erweiterteBetreuung = new ErweiterteBetreuung();
				}
				erweiterteBetreuung.setBetreuungInGemeinde(false);
				betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(erweiterteBetreuung);
			}
		} else {
			//falls Kiadmin keine Gemeinde sendet, muss einen Institution Benutzer der Platz Bestaetigen
			isReadyForBestaetigen = false;
		}
		//Erstellen die Zeitabschnitte auf basis der DTO:
		if (dto.getZeitabschnitte().isEmpty()) {
			isReadyForBestaetigen = false;
		} else {
			betreuung.getBetreuungspensumContainers().clear();
			for (ZeitabschnittDTO zeitabschnittDTO : dto.getZeitabschnitte()) {
				Betreuungspensum betreuungspensum = new Betreuungspensum();
				betreuungspensum.setPensum(zeitabschnittDTO.getBetreuungspensum()); // schauen ob es so korrekt ist
				if(betreuung.isAngebotKita()){
					if(!zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.DAYS.name())){
						LOG.error("Pensum is not supported, Zeitabschnitt ist corrupted");
						isReadyForBestaetigen = false;
						break;
					}
					betreuungspensum.setUnitForDisplay(PensumUnits.DAYS);
				}
				else if(betreuung.isAngebotTagesfamilien()){
					if(!zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.HOURS.name())){
						LOG.error("Pensum is not supported, Zeitabschnitt ist corrupted");
						isReadyForBestaetigen = false;
						break;
					}
					betreuungspensum.setUnitForDisplay(PensumUnits.HOURS);
				}
				betreuungspensum.setPensum(zeitabschnittDTO.getBetreuungspensum());
				betreuungspensum.setMonatlicheHauptmahlzeiten(zeitabschnittDTO.getAnzahlMonatlicheHauptmahlzeiten());
				betreuungspensum.setMonatlicheNebenmahlzeiten(zeitabschnittDTO.getAnzahlMonatlicheNebenmahlzeiten());
				betreuungspensum.setMonatlicheBetreuungskosten(zeitabschnittDTO.getBetreuungskosten());
				betreuungspensum.getGueltigkeit().setGueltigAb(zeitabschnittDTO.getVon());
				betreuungspensum.getGueltigkeit().setGueltigBis(zeitabschnittDTO.getBis());
				//Die Mahlzeitkosten koennen null sein, wir muessen dann die Gemeinde Werten nehmen oder default
				if(zeitabschnittDTO.getTarifProHauptmahlzeiten() != null){
					betreuungspensum.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
				}
				if(zeitabschnittDTO.getTarifProNebenmahlzeiten() != null){
					betreuungspensum.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
				}
				//set betreuungpensum in model
				BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
				betreuungspensumContainer.setBetreuung(betreuung);
				betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);
				betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
			}
		}
		return isReadyForBestaetigen;
	}
}
