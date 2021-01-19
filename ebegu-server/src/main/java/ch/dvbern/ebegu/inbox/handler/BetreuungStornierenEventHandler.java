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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.inbox.services.BetreuungEventService;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class BetreuungStornierenEventHandler extends BaseEventHandler<String> {

	private static final Logger LOG = LoggerFactory.getLogger(BetreuungStornierenEventHandler.class);
	private static final String BETREFF_KEY = "mutationsmeldung_betreff";
	private static final String MESSAGE_KEY = "mutationsmeldung_message";

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private BetreuungEventService betreuungEventService;

	@Inject
	private GemeindeService gemeindeService;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String dto, @Nonnull String clientName) {
		//korrekt Kafka Daten:
		String refNummer = dto.trim();
		if (refNummer.startsWith(")")) {
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

	private Processing processEventForStornierung(
		LocalDateTime eventTime,
		String refNummer,
		String clientName,
		Betreuung betreuung) {
		if (betreuung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (betreuung.getTimestampMutiert() != null && betreuung.getTimestampMutiert().isAfter(eventTime)) {
			return Processing.failure("Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde.");
		}

		//if Betreuung in Status Warten und Mutation stornieren:
		if (betreuung.getVorgaengerId() != null && betreuung.getBetreuungsstatus() == Betreuungsstatus.WARTEN) {
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
		if (betreuung.getVorgaengerId() != null && isMutationsMitteilungStatus(betreuung.getBetreuungsstatus())) {
			//if Betreuung schon Bestaetigt => MutationMitteilung mit Storniereung erfassen
			Betreuungsmitteilung betreuungsmitteilung = createBetreuungsStornierenMitteilung(betreuung);
			mitteilungService.replaceBetreungsmitteilungen(betreuungsmitteilung);
		} else {
			return Processing.failure(
				"Die Betreuung befindet sich in einen Status wo eine Stornierung nicht erlaubt ist.");
		}

		//if Betreuung in Status Warten und erstAntrag ablehnen ?:

		return Processing.success();
	}

	@Nonnull
	private Betreuungsmitteilung createBetreuungsStornierenMitteilung(
		@Nonnull Betreuung betreuung) {

		Gesuch gesuch = betreuung.extractGesuch();
		Locale locale = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService).getLocale();

		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setDossier(gesuch.getDossier());
		betreuungsmitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
		betreuungsmitteilung.setSender(betreuungEventService.getMutationsmeldungBenutzer());
		betreuungsmitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
		betreuungsmitteilung.setEmpfaenger(gesuch.getDossier().getFall().getBesitzer());
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		betreuungsmitteilung.setSubject(ServerMessageUtil.getMessage(BETREFF_KEY, locale));
		betreuungsmitteilung.setBetreuung(betreuung);
		betreuungsmitteilung.setBetreuungStornieren(true);

		List<BetreuungsmitteilungPensum> betreuungsMitteilungPensen = betreuung.getBetreuungspensumContainers()
			.stream()
			.map(BetreuungStornierenEventHandler::fromBetreuungspensumContainerToZero)
			.collect(Collectors.toList());
		betreuungsmitteilung.getBetreuungspensen().addAll(betreuungsMitteilungPensen);
		betreuungsmitteilung.getBetreuungspensen().forEach(p -> p.setBetreuungsmitteilung(betreuungsmitteilung));
		betreuungsmitteilung.setMessage("STORNIERTE MITTEILUNG MELDUNG");

		return betreuungsmitteilung;
	}

	@Nonnull
	private static BetreuungsmitteilungPensum fromBetreuungspensumContainerToZero(
		@Nonnull BetreuungspensumContainer container) {
		BetreuungsmitteilungPensum pensum = new BetreuungsmitteilungPensum();

		container.getBetreuungspensumJA()
			.copyAbstractBetreuungspensumMahlzeitenEntity(pensum, AntragCopyType.MUTATION);
		pensum.setPensum(BigDecimal.ZERO);

		return pensum;
	}

	protected boolean isMutationsMitteilungStatus(@Nonnull Betreuungsstatus status) {
		return status == Betreuungsstatus.VERFUEGT
			|| status == Betreuungsstatus.BESTAETIGT
			|| status == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG;
	}
}
