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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
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
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.outbox.institution.InstitutionEventConverter.toErlaubteIntervalle;

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

		String refnr = dto.getRefnr();
		EventMonitor eventMonitor = new EventMonitor(betreuungMonitoringService, eventTime, refnr, clientName);
		Processing processing = attemptProcessing(eventMonitor, dto);

		if (!processing.isProcessingSuccess()) {
			String message = processing.getMessage();
			LOG.warn(
				"AnmeldungBestaetigung Event für Tagesschule Anmeldung mit RefNr: {} nicht verarbeitet: {}",
				refnr,
				message);
			eventMonitor.record("Eine Anmeldungbestaetigung Event wurde nicht verarbeitet: " + message);
		}
	}

	@Nonnull
	protected Processing attemptProcessing(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull TagesschuleBestaetigungEventDTO dto) {

		return betreuungService.findAnmeldungenTagesschuleByBGNummer(dto.getRefnr())
			.map(anmeldung -> processEventForAnmeldungBestaetigung(eventMonitor, dto, anmeldung))
			.orElseGet(() -> Processing.failure("AnmeldungTagessschule nicht gefunden."));
	}

	@Nonnull
	private Processing processEventForAnmeldungBestaetigung(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull TagesschuleBestaetigungEventDTO dto,
		@Nonnull AnmeldungTagesschule anmeldung) {

		if (anmeldung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (eventMonitor.isTooLate(anmeldung.getTimestampMutiert())) {
			return Processing.failure(
				"Die AnmeldungTagesschule wurde verändert, nachdem das AnmeldungTagesschuleEvent generiert wurde.");
		}

		return betreuungEventHelper.getExternalClient(eventMonitor.getClientName(), anmeldung)
			.map(client -> processEventForExternalClient(eventMonitor, dto, anmeldung, client.getGueltigkeit()))
			.orElseGet(() -> betreuungEventHelper.clientNotFoundFailure(eventMonitor.getClientName(), anmeldung));
	}

	@Nonnull
	private Processing processEventForExternalClient(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull TagesschuleBestaetigungEventDTO dto,
		@Nonnull AnmeldungTagesschule anmeldung,
		@Nonnull DateRange clientGueltigkeit) {

		DateRange gesuchsperiode = anmeldung.extractGesuchsperiode().getGueltigkeit();
		Optional<DateRange> overlap = gesuchsperiode.getOverlap(clientGueltigkeit);
		if (overlap.isEmpty()) {
			return Processing.failure("Der Client hat innerhalb der Periode keine Berechtigung.");
		}

		if (anmeldung.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST) {
			return handleBestaetigung(eventMonitor, dto, anmeldung);
		}

		return Processing.failure("Die AnmeldungTagesschule hat einen ungültigen Status: "
			+ anmeldung.getBetreuungsstatus());
	}

	@Nonnull
	private Processing handleBestaetigung(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull TagesschuleBestaetigungEventDTO dto,
		@Nonnull AnmeldungTagesschule anmeldung) {

		if (anmeldung.getBelegungTagesschule() == null) {
			return Processing.failure("Anmeldung hat einen Datenproblem, keine BelegungTagesschule");
		}

		if (dto.getModule().isEmpty()) {
			return Processing.failure("TagesschuleBestaetigungEventDTO hat keine Module");
		}

		updateAnmeldung(eventMonitor, dto, anmeldung);

		if (anmeldungDirektUebernehmen(anmeldung)) {
			//noinspection ResultOfMethodCallIgnored
			verfuegungService.anmeldungTagesschuleUebernehmen(anmeldung);
			eventMonitor.record("Tagesschuleanmeldung wurde automatisch uebergenommen");
		} else {
			//noinspection ResultOfMethodCallIgnored
			betreuungService.anmeldungSchulamtModuleAkzeptieren(anmeldung);
			eventMonitor.record("Tagesschuleanmeldung wurde automatisch akzeptiert");
		}

		return Processing.success();
	}

	private boolean anmeldungDirektUebernehmen(@Nonnull AnmeldungTagesschule anmeldungTagesschule) {
		Gesuch gesuch = anmeldungTagesschule.extractGesuch();

		return gesuch.getStatus().isAnyStatusOfVerfuegt()
			|| gesuch.getStatus() == AntragStatus.VERFUEGEN
			|| gesuch.getStatus() == AntragStatus.KEIN_KONTINGENT;
	}

	// the API only needs to send the modules. Everything else is only optionally overwritten
	private void updateAnmeldung(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull TagesschuleBestaetigungEventDTO dto,
		@Nonnull AnmeldungTagesschule anmeldung) {

		BelegungTagesschule belegung = getOrCreateBelegungTagesschule(anmeldung);

		if (dto.getEintrittsdatum() != null) {
			belegung.setEintrittsdatum(dto.getEintrittsdatum());
		}
		if (dto.getPlanKlasse() != null) {
			belegung.setPlanKlasse(dto.getPlanKlasse());
		}
		if (dto.getAbholung() != null) {
			belegung.setAbholungTagesschule(AbholungTagesschule.valueOf(dto.getAbholung().name()));
		}
		if (dto.getAbweichungZweitesSemester() != null) {
			belegung.setAbweichungZweitesSemester(dto.getAbweichungZweitesSemester());
		}
		if (dto.getBemerkung() != null) {
			belegung.setBemerkung(dto.getBemerkung());
		}

		updateModule(eventMonitor, dto, belegung);
	}

	@Nonnull
	private BelegungTagesschule getOrCreateBelegungTagesschule(@Nonnull AnmeldungTagesschule anmeldung) {
		BelegungTagesschule belegungTagesschule = Optional.ofNullable(anmeldung.getBelegungTagesschule())
			.orElseGet(BelegungTagesschule::new);

		anmeldung.setBelegungTagesschule(belegungTagesschule);

		return belegungTagesschule;
	}

	private void updateModule(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull TagesschuleBestaetigungEventDTO dto,
		@Nonnull BelegungTagesschule belegung) {

		Map<String, ModulTagesschuleGroup> moduleById = dto.getModule().stream()
			.map(ModulAuswahlDTO::getModulId)
			.distinct()
			.flatMap(id -> modulTagesschuleService.findModulTagesschuleGroup(id).stream())
			.collect(Collectors.toMap(AbstractEntity::getId, m -> m));

		Set<BelegungTagesschuleModul> existingModule = Sets.newHashSet(belegung.getBelegungTagesschuleModule());
		belegung.getBelegungTagesschuleModule().clear();

		List<BelegungTagesschuleModul> module = dto.getModule().stream()
			.flatMap(toBelegungTagesschuleModul(eventMonitor, moduleById, existingModule))
			.collect(Collectors.toList());

		module.forEach(m -> {
			m.setBelegungTagesschule(belegung);
			belegung.getBelegungTagesschuleModule().add(m);
		});
	}

	@Nonnull
	private Function<ModulAuswahlDTO, Stream<BelegungTagesschuleModul>> toBelegungTagesschuleModul(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull Map<String, ModulTagesschuleGroup> moduleById,
		@Nonnull Set<BelegungTagesschuleModul> existingModule) {

		return modulAuswahlDTO -> findModulTagesschule(eventMonitor, modulAuswahlDTO, moduleById)
			.map(modulTagesschule -> {
				BelegungTagesschuleModulIntervall intervall = getIntervall(modulAuswahlDTO);

				return findExistingBelegungTagesschuleModul(existingModule, modulTagesschule, intervall)
					.orElseGet(() -> createNewBelegungTagesschuleModul(modulTagesschule, intervall));
			})
			.stream();
	}

	@Nonnull
	private Optional<ModulTagesschule> findModulTagesschule(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull ModulAuswahlDTO dto,
		@Nonnull Map<String, ModulTagesschuleGroup> moduleById) {

		String id = dto.getModulId();
		if (!moduleById.containsKey(id)) {
			eventMonitor.record("Es wurde eine ungültige ModulTagesschuleGroup ID übergeben: %s", id);

			return Optional.empty();
		}

		ModulTagesschuleGroup group = moduleById.get(id);

		if (!toErlaubteIntervalle(group.getIntervall()).contains(dto.getIntervall())) {
			eventMonitor.record("ModulTagesschuleGroup %s gestattet das Intervall %s nicht.", id, dto.getIntervall());

			return Optional.empty();
		}

		Optional<ModulTagesschule> match = group.getModule().stream()
			.filter(m -> m.getWochentag().name().equals(dto.getWeekday().name()))
			.findAny();

		if (match.isEmpty()) {
			eventMonitor.record("ModulTagesschuleGroup %s ist an %s nicht definiert.", id, dto.getWeekday());

			return Optional.empty();
		}

		return match;
	}

	@Nonnull
	private BelegungTagesschuleModulIntervall getIntervall(@Nonnull ModulAuswahlDTO modulAuswahlDTO) {
		return BelegungTagesschuleModulIntervall.valueOf(modulAuswahlDTO.getIntervall().name());
	}

	@Nonnull
	private Optional<BelegungTagesschuleModul> findExistingBelegungTagesschuleModul(
		@Nonnull Set<BelegungTagesschuleModul> existingModule,
		@Nonnull ModulTagesschule modulTagesschule,
		@Nonnull BelegungTagesschuleModulIntervall intervall) {

		return existingModule.stream()
			.filter(e -> e.getModulTagesschule().equals(modulTagesschule) && e.getIntervall() == intervall)
			.findAny();
	}

	@Nonnull
	private BelegungTagesschuleModul createNewBelegungTagesschuleModul(
		@Nonnull ModulTagesschule modulTagesschule,
		@Nonnull BelegungTagesschuleModulIntervall intervall) {

		BelegungTagesschuleModul belegungTagesschuleModul = new BelegungTagesschuleModul();
		belegungTagesschuleModul.setIntervall(intervall);
		belegungTagesschuleModul.setModulTagesschule(modulTagesschule);

		return belegungTagesschuleModul;
	}
}
