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

package ch.dvbern.ebegu.inbox.handler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungService;

@ApplicationScoped
public class BetreuungStornierenEventHandler extends BaseEventHandler<String> {

	@Inject
	private BetreuungService betreuungService;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String dto, @Nonnull String clientName) {
		//korrekt Kafka Daten:
		String refNummer = dto.trim();
		if(refNummer.startsWith(")")){
			refNummer = refNummer.substring(2, refNummer.length());
		}

		attemptProcessing(eventTime, refNummer, clientName);
//Warten + Vorgaenger ID gesetzt => Stornieren ok son
	}

	@Nonnull
	protected Processing attemptProcessing(
		@Nonnull LocalDateTime eventTime,
		@Nonnull String refNummer,
		@Nonnull String clientName) {

		return betreuungService.findBetreuungByBGNummer(refNummer, false)
			.map(betreuung -> processEventForStornierung(eventTime, refNummer, clientName, betreuung))
			.orElseGet(() -> Processing.failure("Betreuung nicht gefunden."));
	}

	private Processing processEventForStornierung(LocalDateTime eventTime, String refNummer, String clientName, Betreuung betreuung) {
		if (betreuung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (betreuung.getTimestampMutiert() != null && betreuung.getTimestampMutiert().isAfter(eventTime)) {
			return Processing.failure("Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde.");
		}

		//if Betreuung in Status Warten und Mutation stornieren:
		if(betreuung.getVorgaengerId() != null && betreuung.getBetreuungsstatus() == Betreuungsstatus.WARTEN) {
			betreuung.setDatumBestaetigung(LocalDate.now());
			betreuung.getBetreuungspensumContainers().stream().forEach(
				betreuungspensumContainer -> {
					betreuungspensumContainer.getBetreuungspensumJA().setPensum(BigDecimal.ZERO);
					betreuungspensumContainer.getBetreuungspensumJA().setNichtEingetreten(true);
				}
			);
			betreuung.setBetreuungsstatus(Betreuungsstatus.STORNIERT);
			betreuungService.saveBetreuung(betreuung, false);
		}
		if(betreuung.getVorgaengerId() != null && isMutationsMitteilungStatus(betreuung.getBetreuungsstatus())){
			//if Betreuung schon Bestaetigt => MutationMitteilung mit Storniereung erfassen

		}
		else{
			return Processing.failure("Die Betreuung befindet sich in einen Status wo eine Stornierung nicht erlaubt ist.");
		}

		//if Betreuung in Status Warten und erstAntrag ablehnen ?:



		return Processing.success();
	}

	protected boolean isMutationsMitteilungStatus(@Nonnull Betreuungsstatus status) {
		return status == Betreuungsstatus.VERFUEGT
			|| status == Betreuungsstatus.BESTAETIGT
			|| status == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG;
	}
}
