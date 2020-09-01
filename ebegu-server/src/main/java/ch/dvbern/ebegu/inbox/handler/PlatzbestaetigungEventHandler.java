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
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PlatzbestaetigungEventHandler extends BaseEventHandler<BetreuungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(PlatzbestaetigungEventHandler.class);

	@Inject
	private BetreuungService betreuungService;

	@Override
	protected void processEvent(@Nonnull LocalDateTime eventTime, @Nonnull EventType eventType,
		@Nonnull BetreuungEventDTO dto) {
		try{
			Optional<Betreuung> betreuungOpt = betreuungService.findGueltigeBetreuungByBGNummer(dto.getRefnr());
			if(!betreuungOpt.isPresent()){
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: " + dto.getRefnr() + " existiert nicht!");
				return;
			}
			Betreuung betreuung = betreuungOpt.get();
			if(betreuung.getBetreuungsstatus().equals(Betreuungsstatus.WARTEN)){



			}
			else {
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: " + dto.getRefnr() + " war schon bestaetigt!");
			}
		}
		catch (Exception e){
			LOG.error("Error while processing the record: " + dto.getRefnr() + " error: " + e.getMessage());
		}
	}
}
