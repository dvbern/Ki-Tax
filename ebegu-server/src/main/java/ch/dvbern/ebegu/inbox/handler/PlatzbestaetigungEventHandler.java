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
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungServiceBean;
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
	private EinstellungServiceBean einstellungServiceBean;

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
				einstellungServiceBean.findEinstellung(EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED,
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
			for (ZeitabschnittDTO zeitabschnittDTO : dto.getZeitabschnitte()) {
				Betreuungspensum betreuungspensum = new Betreuungspensum();
				//TODO ausfüllen
			}


		}
		return isReadyForBestaetigen;
	}

	private void checkZeitabschnittVollstaendig() {

	}

}
