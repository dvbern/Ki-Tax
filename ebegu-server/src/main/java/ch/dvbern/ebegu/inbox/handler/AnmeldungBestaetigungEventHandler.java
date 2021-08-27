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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.enums.AbholungTagesschule;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.ModulTagesschuleService;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulAuswahlDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleBestaetigungEventDTO;
import ch.dvbern.kibon.exchange.commons.types.Wochentag;
import org.infinispan.container.versioning.irac.MapEntry$___Marshaller_37a121f54771729f5bc90dae7b319161c44a1e9491f8af866c7aba312bf9651e;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AnmeldungBestaetigungEventHandler extends BaseEventHandler<TagesschuleBestaetigungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(AnmeldungBestaetigungEventHandler.class);

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;

	@Inject
	private ModulTagesschuleService modulTagesschuleService;

	@Inject
	private BetreuungEventHelper betreuungEventHelper;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String key,
		@Nonnull TagesschuleBestaetigungEventDTO dto,
		@Nonnull String clientName) {
		Processing processing = attemptProcessing(eventTime, dto, clientName);

		if (!processing.isProcessingSuccess()) {
			String message = processing.getMessage();
			LOG.warn(
				"AnmeldungBestaetigung Event für Tagesschule Anmeldung mit RefNr: {} nicht verarbeitet: {}",
				dto.getRefnr(),
				message);
			betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
				dto.getRefnr(),
				clientName,
				"Eine Anmeldungbestaetigung Event wurde nicht verarbeitet: " + message,
				LocalDateTime.now()));
		}

	}

	protected Processing attemptProcessing(
		LocalDateTime eventTime,
		TagesschuleBestaetigungEventDTO dto,
		String clientName) {
		return betreuungService.findAnmeldungenTagesschuleByBGNummer(dto.getRefnr()).map(
			anmeldungTagesschule -> processEventForAnmeldungBestaetigung(
				eventTime,
				dto,
				clientName,
				anmeldungTagesschule))
			.orElseGet(() -> Processing.failure("AnmeldungTagessschule nicht gefunden."));
	}

	private Processing processEventForAnmeldungBestaetigung(
		LocalDateTime eventTime,
		TagesschuleBestaetigungEventDTO dto,
		String clientName,
		AnmeldungTagesschule anmeldungTagesschule) {
		if (anmeldungTagesschule.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (anmeldungTagesschule.getTimestampMutiert() != null && anmeldungTagesschule.getTimestampMutiert()
			.isAfter(eventTime)) {
			return Processing.failure(
				"Die AnmeldungTagesschule wurde verändert, nachdem das AnmeldungTagesschuleEvent generiert wurde.");
		}

		return betreuungEventHelper.getExternalClient(clientName, anmeldungTagesschule)
			.map(externalClient -> processEventForExternalClient(anmeldungTagesschule, externalClient.getGueltigkeit(),
				dto, clientName))
			.orElseGet(() -> betreuungEventHelper.clientNotFoundFailure(clientName, anmeldungTagesschule));
	}

	private Processing processEventForExternalClient(
		AnmeldungTagesschule anmeldungTagesschule,
		DateRange clientGueltigkeit,
		TagesschuleBestaetigungEventDTO dto,
		String clientName) {
		DateRange gesuchsperiode = anmeldungTagesschule.extractGesuchsperiode().getGueltigkeit();
		Optional<DateRange> overlap = gesuchsperiode.getOverlap(clientGueltigkeit);
		if (overlap.isEmpty()) {
			return Processing.failure("Der Client hat innerhalb der Periode keinen Berechtigung.");
		}
		if (anmeldungTagesschule.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST) {
			return handlePlatzbestaetigung(anmeldungTagesschule, dto, clientName);
		}

		return Processing.failure("Die AnmeldungTagesschule hat einen ungültigen Status: "
			+ anmeldungTagesschule.getBetreuungsstatus());
	}

	@Nonnull
	private Processing handlePlatzbestaetigung(
		AnmeldungTagesschule anmeldungTagesschule,
		TagesschuleBestaetigungEventDTO dto,
		String clientName) {
		if (anmeldungTagesschule.getBelegungTagesschule() == null) {
			return Processing.failure("Anmeldung hat einen Datenproblem, keine BelegungTagesschule");
		}

		if (dto.getModule().isEmpty()) {
			return Processing.failure("TagesschuleBestaetigungEventDTO hat keine Module");
		}

		copyAnmeldungDtoDatenInAnmeldung(anmeldungTagesschule, dto);

		mergeDtoModuleInAnmeldung(anmeldungTagesschule, dto, clientName);

		if (anmeldungDirektUebernehmen(anmeldungTagesschule.extractGesuch())) {
			verfuegungService.anmeldungTagesschuleUebernehmen(anmeldungTagesschule);
			betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
				dto.getRefnr(),
				clientName,
				"Tagesschuleanmeldung wurde automatisch uebergenommen",
				LocalDateTime.now()));
		} else {
			betreuungService.anmeldungSchulamtModuleAkzeptieren(anmeldungTagesschule);
			betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
				dto.getRefnr(),
				clientName,
				"Tagesschuleanmeldung wurde automatisch akzeptiert",
				LocalDateTime.now()));
		}

		return Processing.success();
	}

	private boolean anmeldungDirektUebernehmen(Gesuch gesuch) {
		return gesuch.getStatus().isAnyStatusOfVerfuegt() || gesuch.getStatus() == AntragStatus.VERFUEGEN
			|| gesuch.getStatus() == AntragStatus.KEIN_KONTINGENT;
	}

	private void copyAnmeldungDtoDatenInAnmeldung(
		AnmeldungTagesschule anmeldungTagesschule,
		TagesschuleBestaetigungEventDTO dto) {
		anmeldungTagesschule.getBelegungTagesschule()
			.setAbholungTagesschule(AbholungTagesschule.valueOf(dto.getAbholung().name()));
		anmeldungTagesschule.getBelegungTagesschule().setBemerkung(dto.getBemerkung());
		anmeldungTagesschule.getBelegungTagesschule().setAbweichungZweitesSemester(dto.getAbweichungZweitesSemester());
		anmeldungTagesschule.getBelegungTagesschule().setEintrittsdatum(dto.getEintrittsdatum());
		anmeldungTagesschule.getBelegungTagesschule().setPlanKlasse(dto.getPlanKlasse());
	}

	private void mergeDtoModuleInAnmeldung(
		AnmeldungTagesschule anmeldungTagesschule,
		TagesschuleBestaetigungEventDTO dto,
		String clientName) {
		// addieren oder anpassen die ubermittele Module von der Schnittstelle
		dto.getModule().forEach(modulAuswahlDTO -> {
			Optional<ModulTagesschuleGroup> modulTagesschuleGroup =
				modulTagesschuleService.findModulTagesschuleGroup(modulAuswahlDTO.getModulId());
			if (modulTagesschuleGroup.isPresent()) {
				updateOrAddDTOModuleInExistingAnmeldung(anmeldungTagesschule, modulTagesschuleGroup.get(), modulAuswahlDTO, dto.getRefnr(), clientName);
			} else {
				betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
					dto.getRefnr(),
					clientName,
					"Tagesschuleanmeldung einen ModulTagesschuleGroup wurde nicht gefunden: " + modulAuswahlDTO.getModulId(),
					LocalDateTime.now()));
			}
		});

		//entfernt alle die unselektiert sind
		anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().removeIf(
			belegungTagesschuleModul -> isNotFoundInDTO(belegungTagesschuleModul, dto)
		);
	}

	private boolean isNotFoundInDTO(BelegungTagesschuleModul belegungTagesschuleModul, TagesschuleBestaetigungEventDTO dto) {
		Optional<ModulAuswahlDTO> modulAuswahlDTOOpt = dto.getModule().stream().findFirst().filter(modulAuswahlDTO ->
			modulAuswahlDTO.getModulId().equals(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId())
				&& belegungTagesschuleModul.getModulTagesschule().getWochentag().getValue() == modulAuswahlDTO.getWeekday()
		);
		return modulAuswahlDTOOpt.isPresent();
	}

	private void updateOrAddDTOModuleInExistingAnmeldung(
		AnmeldungTagesschule anmeldungTagesschule,
		ModulTagesschuleGroup modulTagesschuleGroup,
		ModulAuswahlDTO modulAuswahlDTO,
		String refNummer,
		String clientName) {
		Optional<ModulTagesschule> modulTagesschuleOpt =
			modulTagesschuleGroup.getModule().stream().findFirst().filter(modulTagesschule ->
				modulTagesschule.getWochentag().equals(DayOfWeek.of(modulAuswahlDTO.getWeekday())));

		if(modulTagesschuleOpt.isPresent()) {
			ModulTagesschule modulTagesschule = modulTagesschuleOpt.get();
			Optional<BelegungTagesschuleModul> belegungTagesschuleModulOpt = anmeldungTagesschule.getBelegungTagesschule()
				.getBelegungTagesschuleModule()
				.stream()
				.findFirst()
				.filter(belegungTagesschuleModul -> belegungTagesschuleModul.getModulTagesschule()
					.equals(modulTagesschule));
			BelegungTagesschuleModul belegungTagesschuleModul = belegungTagesschuleModulOpt.isPresent() ? belegungTagesschuleModulOpt.get() : new BelegungTagesschuleModul();
			belegungTagesschuleModul.setIntervall(BelegungTagesschuleModulIntervall.valueOf(modulAuswahlDTO.getIntervall().name()));
			if (!belegungTagesschuleModulOpt.isPresent()) {
				belegungTagesschuleModul.setBelegungTagesschule(anmeldungTagesschule.getBelegungTagesschule());
				belegungTagesschuleModul.setModulTagesschule(modulTagesschule);
				anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().add(belegungTagesschuleModul);
			}
		}
		else {
			betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
				refNummer,
				clientName,
				"Tagesschuleanmeldung einen ModulTagesschuleGroup wurde nicht gefunden: " + modulAuswahlDTO.getModulId(),
				LocalDateTime.now()));
		}
	}
}
