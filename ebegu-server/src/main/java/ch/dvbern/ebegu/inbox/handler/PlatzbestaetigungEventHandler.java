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

import ch.dvbern.ebegu.betreuung.BetreuungEinstellungen;
import ch.dvbern.ebegu.betreuung.BetreuungEinstellungenService;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungImportForm.ImportForm;
import ch.dvbern.ebegu.inbox.handler.pensum.PensumMapper;
import ch.dvbern.ebegu.inbox.handler.pensum.PensumMapperFactory;
import ch.dvbern.ebegu.inbox.handler.pensum.PensumMappingUtil;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;
import ch.dvbern.ebegu.util.MitteilungUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungImportForm.importAs;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungImportForm.isNotYetFreigegeben;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungProcessing.withImportFrom;
import static ch.dvbern.ebegu.inbox.handler.pensum.PensumMappingUtil.COMPARATOR_WITH_GUELTIGKEIT;
import static ch.dvbern.ebegu.inbox.handler.pensum.PensumMappingUtil.MITTEILUNG_COMPARATOR;
import static java.util.Objects.requireNonNull;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor
public class PlatzbestaetigungEventHandler extends BaseEventHandler<BetreuungEventDTO> {

	@SuppressWarnings("FieldMayBeFinal") // need a mock in unit test
	private Logger logger = LoggerFactory.getLogger(PlatzbestaetigungEventHandler.class);

	private static final String BETREFF_KEY = "mutationsmeldung_betreff_von";

	private static final Comparator<Mitteilung> LATEST_BETREUUNGSMITTEILUNG = Comparator
		.comparing(Mitteilung::getSentDatum, Comparator.nullsFirst(Comparator.naturalOrder()))
		.thenComparing(AbstractEntity::getTimestampErstellt, Comparator.nullsFirst(Comparator.naturalOrder()))
		.thenComparing(AbstractEntity::getId);

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private BetreuungEinstellungenService betreuungEinstellungenService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private PensumMapperFactory pensumMapperFactory;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BetreuungEventHelper betreuungEventHelper;

	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String key,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull String clientName) {

		String refnr = dto.getRefnr();
		EventMonitor eventMonitor = new EventMonitor(betreuungMonitoringService, eventTime, refnr, clientName);
		Processing processing = attemptProcessing(eventMonitor, dto);

		switch (processing.getState()) {
		case SUCCESS:
			logger.debug("Platzbestaetigung Event für Betreuung mit RefNr: {} erfolgreich verarbeitet: {}", refnr, processing);
			return;
		case IGNORE:
			logger.info(
				"Platzbestaetigung Event für Betreuung mit RefNr: {} wurde ignoriert und nicht verarbeitet: {}",
				refnr,
				processing);
			eventMonitor.record("Eine Platzbestaetigung Event wurde ignoriert: " + processing);
			return;
		case FAILURE:
			logger.warn(
				"Platzbestaetigung Event für Betreuung mit RefNr: {} nicht verarbeitet: {}",
				refnr,
				processing);
			eventMonitor.record("Eine Platzbestaetigung Event wurde nicht verarbeitet: " + processing);
		}
	}

	@Nonnull
	protected Processing attemptProcessing(@Nonnull EventMonitor eventMonitor, @Nonnull BetreuungEventDTO dto) {

		if (dto.getZeitabschnitte().isEmpty()) {
			return Processing.failure("Es wurden keine Zeitabschnitte übergeben.");
		}

		return betreuungService.findBetreuungByReferenzNummer(dto.getRefnr(), false)
			.map(betreuung -> processEventForBetreuung(eventMonitor, dto, betreuung))
			.orElseGet(() -> Processing.failure("Betreuung nicht gefunden."));
	}

	@Nonnull
	private Processing processEventForBetreuung(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull Betreuung betreuung) {

		if (betreuung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (eventMonitor.isTooLate(betreuung.getTimestampMutiert())) {
			return Processing.failure("Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde.");
		}

		if (hasZeitabschnittWithPensumInDays(dto) && !betreuung.isAngebotKita()) {
			return Processing.failure("Eine Pensum in DAYS kann nur für ein Angebot in einer Kita angegeben werden.");
		}

		InstitutionExternalClients clients =
			betreuungEventHelper.getExternalClients(eventMonitor.getClientName(), betreuung);

		return clients.getRelevantClient()
			.map(client -> processEventForExternalClient(eventMonitor, dto, betreuung, client, clients.getOther().isEmpty()))
			.orElseGet(() -> betreuungEventHelper.clientNotFoundFailure(eventMonitor.getClientName(), betreuung));
	}

	private boolean hasZeitabschnittWithPensumInDays(@Nonnull BetreuungEventDTO dto) {
		return dto.getZeitabschnitte().stream().anyMatch(z -> z.getPensumUnit() == Zeiteinheit.DAYS);
	}

	@Nonnull
	private Processing processEventForExternalClient(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull Betreuung betreuung,
		@Nonnull InstitutionExternalClient client,
		boolean singleClientForPeriod) {

		DateRange gesuchsperiode = betreuung.extractGesuchsperiode().getGueltigkeit();
		DateRange clientGueltigkeit = client.getGueltigkeit();

		return gesuchsperiode.getOverlap(clientGueltigkeit)
			.map(overlap -> {
				DateRange institutionGueltigkeit = betreuung.getInstitutionStammdaten().getGueltigkeit();
				Processing validationProcess = validateZeitabschnitteGueltigkeit(dto, overlap, institutionGueltigkeit);
				if (!validationProcess.isProcessingSuccess()) {
					return validationProcess;
				}

				BetreuungEinstellungen einstellungen = betreuungEinstellungenService.getEinstellungen(betreuung);

				var params = new ProcessingContextParams(dto, einstellungen, eventMonitor, singleClientForPeriod, overlap);

				return processEventForExternalClient(params, betreuung);
			})
			.orElseGet(() -> Processing.failure("Der Client hat innerhalb der Periode keine Berechtigung."));
	}

	@Nonnull
	private Processing processEventForExternalClient(@Nonnull ProcessingContextParams params, @Nonnull Betreuung betreuung) {
		ProcessingContext ctx = toProcessingContext(params, betreuung);

		Set<ImportForm> importForms = importAs(ctx);

		Optional<PlatzbestaetigungProcessing> processingPlatzbestaetigung = importForms.contains(ImportForm.PLATZBESTAETIGUNG) ?
			Optional.of(withImportFrom(ImportForm.PLATZBESTAETIGUNG, handlePlatzbestaetigung(ctx))) :
			Optional.empty();

		Optional<PlatzbestaetigungProcessing> processingMutation = importForms.contains(ImportForm.MUTATIONS_MITTEILUNG) ?
			Optional.of(withImportFrom(ImportForm.MUTATIONS_MITTEILUNG, handleMutationsMitteilung(ctx))) :
			Optional.empty();

		// Falls die Betreuung noch nicht freigegeben ist und einen Vorgänger hat, dann ist sie in Bearbeitung Gesuchsteller.
		// Damit keine Daten verloren gehen, falls der Gesuchsteller die Betreuung nie freigibt, muss eine Mutations Mitteilung
		// erstellt werden.
		Optional<PlatzbestaetigungProcessing> processingLastGueltig = Optional.ofNullable(betreuung.getVorgaengerId())
			.filter(id -> isNotYetFreigegeben(betreuung))
			.flatMap(id -> betreuungService.findBetreuung(id, false))
			.map(b -> toProcessingContext(ctx.getParams(), b))
			.map(this::handleMutationsMitteilung)
			.map(p -> withImportFrom(ImportForm.MUTATIONS_MITTEILUNG, p));

		List<PlatzbestaetigungProcessing> processings = Lists.newArrayList();
		processingPlatzbestaetigung.ifPresent(processings::add);
		processingMutation.ifPresent(processings::add);
		processingLastGueltig.ifPresent(processings::add);

		return PlatzbestaetigungProcessing.fromImport(processings);
	}

	@Nonnull
	private ProcessingContext toProcessingContext(@Nonnull ProcessingContextParams params, @Nonnull Betreuung betreuung) {
		Betreuungsmitteilung betreuungsmitteilung = findLatestOffeneBetreungsmitteilung(betreuung)
			.orElse(null);

		return new ProcessingContext(betreuung, betreuungsmitteilung, params);
	}

	@Nonnull
	private Processing validateZeitabschnitteGueltigkeit(
		@Nonnull BetreuungEventDTO dto,
		@Nonnull DateRange clientGueltigkeitInPeriode,
		@Nonnull DateRange institutionGueltigkeit) {

		List<DateRange> ranges = dto.getZeitabschnitte().stream()
			.map(z -> new DateRange(z.getVon(), z.getBis()))
			.collect(Collectors.toList());

		if (GueltigkeitsUtil.hasOverlap(ranges)) {
			return Processing.failure("Zeitabschnitte dürfen nicht überlappen.");
		}

		Optional<DateRange> writableOverlap = clientGueltigkeitInPeriode.getOverlap(institutionGueltigkeit);
		if (writableOverlap.isEmpty()) {
			return Processing.failure("Die Institution Gültigkeit überlappt nicht mit der Client Gültigkeit.");
		}

		if (ranges.stream().noneMatch(r -> r.intersects(writableOverlap.get()))) {
			return Processing.failure(
				"Kein Zeitabschnitt liegt innerhalb Client Gültigkeit & Periode & Institution Gültigkeit.");
		}

		return Processing.success();
	}

	/**
	 * Update the Betreuung and automatically confirm if all required data is present.
	 */
	@Nonnull
	private Processing handlePlatzbestaetigung(@Nonnull ProcessingContext ctx) {
		boolean isReadyForBestaetigen = updateBetreuungForPlatzbestaetigung(ctx);
		String clientName = ctx.getEventMonitor().getClientName();
		String refnr = ctx.getDto().getRefnr();

		if (isReadyForBestaetigen) {
			ctx.getBetreuung().setDatumBestaetigung(LocalDate.now());
			//noinspection ResultOfMethodCallIgnored

			betreuungService.betreuungPlatzBestaetigen(ctx.getBetreuung(), clientName);
			logger.info("PlatzbestaetigungEvent Betreuung mit RefNr: {} automatisch bestätigt", refnr);
			ctx.getEventMonitor().record("PlatzbestaetigungEvent automatisch bestätigt");
		} else {
			//noinspection ResultOfMethodCallIgnored
			betreuungService.saveBetreuung(ctx.getBetreuung(), false, clientName);
			logger.info(
				"PlatzbestaetigungEvent Betreuung mit RefNr: {} eingelesen, aber nicht automatisch bestätigt",
				refnr);
			ctx.getEventMonitor().record(
				"PlatzbestaetigungEvent eingelesen, aber nicht automatisch bestätigt: %s",
				ctx.getHumanConfirmationMessages());
		}

		return Processing.success();
	}

	/**
	 * Update the Betreuung Object and return if its ready for Bestaetigen
	 */
	private boolean updateBetreuungForPlatzbestaetigung(@Nonnull ProcessingContext ctx) {

		if (!ctx.isGueltigkeitCoveringPeriode() && !ctx.isSingleClientForPeriod()) {
			ctx.requireHumanConfirmation();
			logger.info(
				"Eine manuelle Bestätigung ist nötig für die PlatzbestaetigungEvent fuer Betreuung mit RefNr: {}, weil"
					+ " die Drittanwendung nicht für die gesamte Gesuchsperiode berechtigt ist"
				,
				ctx.getDto().getRefnr());
			ctx.addHumanConfirmationMessage(
				"Eine manuelle Bestätigung ist nötig, weil"
					+ " die Drittanwendung nicht für die gesamte Gesuchsperiode berechtigt ist");
		}

		setErweitereBeduerfnisseBestaetigt(ctx);
		setEingewoehnungPhase(ctx);
		setBetreuungInGemeinde(ctx);
		setSprachfoerderungBestaetigt(ctx);
		PensumMapper<Betreuungspensum> pensumMapper = pensumMapperFactory.createForPlatzbestaetigung(ctx);
		PensumMappingUtil.addZeitabschnitteToBetreuung(ctx, pensumMapper);

		return ctx.isReadyForBestaetigen();
	}

	private void setSprachfoerderungBestaetigt(@Nonnull ProcessingContext ctx) {
		ErweiterteBetreuung erweiterteBetreuung =
			ctx.getBetreuung().getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
		if (erweiterteBetreuung == null) {
			return;
		}
		Mandant mandant = ctx.getBetreuung().extractGemeinde().getMandant();
		LocalDate sprachfoerderungBesteatigtAktiviereungDatum =
			applicationPropertyService.getSchnittstelleSprachfoerderungAktivAb(mandant);

		if (requireNonNull(sprachfoerderungBesteatigtAktiviereungDatum).isAfter(LocalDate.now())
			&& ctx.getDto().getSprachfoerderungBestaetigt() == null) {
			erweiterteBetreuung.setSprachfoerderungBestaetigt(true);
		} else {
			erweiterteBetreuung.setSprachfoerderungBestaetigt(ctx.getDto().getSprachfoerderungBestaetigt() != null ?
				ctx.getDto().getSprachfoerderungBestaetigt() :
				false);
		}
	}

	private void setEingewoehnungPhase(@Nonnull ProcessingContext ctx) {
		Betreuung b = ctx.getBetreuung();

		// Der Wert aus dem DTO wird nur berücksichtigt, wenn der 'true' ist
		if (ctx.getDto().getEingewoehnungInPeriode()) {
			b.setEingewoehnung(true);
		}
	}

	private void setErweitereBeduerfnisseBestaetigt(@Nonnull ProcessingContext ctx) {
		ErweiterteBetreuung eb = ctx.getBetreuung().getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
		Kind kind = ctx.getBetreuung().getKind().getKindJA();
		// Der Wert aus dem DTO wird nur berücksichtigt, wenn bereits bei dem Antrag erweitere Bedürfnisse angemeldet
		// wurden.
		if (eb != null) {
			boolean claimed = eb.getErweiterteBeduerfnisse() || kind.getHoehereBeitraegeWegenBeeintraechtigungBeantragen();
			boolean confirmed = ctx.getDto().getAusserordentlicherBetreuungsaufwand();

			eb.setErweiterteBeduerfnisseBestaetigt(claimed == confirmed);
		}
	}

	private void setBetreuungInGemeinde(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();
		boolean enabled = einstellungService.isEnabled(EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, betreuung);

		if (!enabled) {
			// no need to set BetreuungInGemeinde -> continue automated processing
			return;
		}

		String incomingName = ctx.getDto().getGemeindeName();
		Long incomingBfsNumber = ctx.getDto().getGemeindeBfsNr();

		if (incomingName == null && incomingBfsNumber == null) {
			// Gemeinde not received, cannot evaluate -> abort automated processing
			ctx.requireHumanConfirmation();
			logger.info(
				"PlatzbestaetigungEvent fuer Betreuung mit RefNr: {} hat keine Gemeinde spezifiziert",
				ctx.getDto().getRefnr());
			ctx.addHumanConfirmationMessage("PlatzbestaetigungEvent hat keine Gemeinde spezifiziert");
			return;
		}

		Gemeinde gemeinde = betreuung.extractGemeinde();
		Long bfsNummer = gemeinde.getBfsNummer();
		String name = gemeinde.getName();

		getOrCreateErweiterteBetreuung(betreuung)
			.setBetreuungInGemeinde(bfsNummer.equals(incomingBfsNumber) || name.equalsIgnoreCase(incomingName));
	}

	@Nonnull
	private Processing handleMutationsMitteilung(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();

		if (!mitteilungService.isBetreuungGueltigForMutation(betreuung)) {
			return Processing.ignore(
				"Die Betreuung wurde storniert und es gibt eine neuere Betreuung für dieses Kind und Institution");
		}

		Betreuungsmitteilung latest = ctx.getLatestOpenBetreuungsmitteilung();
		Betreuungsmitteilung betreuungsmitteilung = createBetreuungsmitteilung(ctx, latest);

		if (latest != null && MITTEILUNG_COMPARATOR.compare(latest, betreuungsmitteilung) == 0) {
			return Processing.ignore("Die Betreuungsmeldung ist identisch mit der neusten offenen Betreuungsmeldung.");
		}

		if (latest == null && isSame(betreuungsmitteilung, betreuung)) {
			return Processing.ignore("Die Betreuungsmeldung und die Betreuung sind identisch.");
		}

		mitteilungService.replaceOffeneBetreungsmitteilungenWithSameReferenzNummer(betreuungsmitteilung, ctx.getDto().getRefnr());
		logger.info(
			"PlatzbestaetigungEvent: Mutationsmeldung erstellt für die Betreuung mit RefNr: {}",
			ctx.getDto().getRefnr());
		ctx.getEventMonitor().record("PlatzbestaetigungEvent: Mutationsmeldung erstellt.");
		if (!ctx.isReadyForBestaetigen()) {
			String message = ctx.getHumanConfirmationMessages();
			ctx.getEventMonitor().record("Die Mutationsmeldung erfordert manuelle Bestätigung: " + message);
		}

		return Processing.success();
	}

	@Nonnull
	private Betreuungsmitteilung createBetreuungsmitteilung(
		@Nonnull ProcessingContext ctx,
		@Nullable Betreuungsmitteilung latest) {

		Locale locale = EbeguUtil.extractKorrespondenzsprache(ctx.getBetreuung().extractGesuch(), gemeindeService).getLocale();
		Betreuungsmitteilung mitteilung = createBetreuungsmitteilung(ctx, locale, latest);
		String msg = mitteilungService.createNachrichtForMutationsmeldung(mitteilung, mitteilung.getBetreuungspensen(), locale);
		mitteilung.setMessage(msg);

		return mitteilung;
	}

	@Nonnull
	private Betreuungsmitteilung createBetreuungsmitteilung(
		@Nonnull ProcessingContext ctx,
		@Nonnull Locale locale,
		@Nullable Betreuungsmitteilung latest) {

		Betreuung betreuung = ctx.getBetreuung();

		Benutzer benutzer = betreuungEventHelper.getMutationsmeldungBenutzer(betreuung);
		Mandant mandant = betreuung.extractGemeinde().getMandant();

		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setBetreuung(betreuung);

		PensumMapper<BetreuungsmitteilungPensum> pensumMapper = pensumMapperFactory.createForBetreuungsmitteilung(ctx);
		PensumMappingUtil.addZeitabschnitteToBetreuungsmitteilung(ctx, latest, betreuungsmitteilung, pensumMapper);

		MitteilungUtil.initializeBetreuungsmitteilung(betreuungsmitteilung, betreuung, benutzer, locale);

		boolean isStorniert = betreuung.getBetreuungsstatus() == Betreuungsstatus.STORNIERT;
		betreuungsmitteilung.getBetreuungspensen().stream()
			.filter(p -> isStorniert && p.getPensum().compareTo(BigDecimal.ZERO) == 0)
			.forEach(p -> p.setVollstaendig(false));

		// overriding subject: keep below MitteilungUtil.initializeBetreuungsmitteilung
		String clientName = ctx.getEventMonitor().getClientName();
		betreuungsmitteilung.setSubject(ServerMessageUtil.getMessage(BETREFF_KEY, locale, mandant) + ' ' + clientName);

		return betreuungsmitteilung;
	}

	@Nonnull
	private Optional<Betreuungsmitteilung> findLatestOffeneBetreungsmitteilung(Betreuung betreuung) {
		Collection<Betreuungsmitteilung> open = mitteilungService.findOffeneBetreuungsmitteilungenForBetreuung(betreuung);

		return findLatest(open);
	}

	@Nonnull
	private Optional<Betreuungsmitteilung> findLatest(@Nonnull Collection<Betreuungsmitteilung> open) {
		return open.stream().max(LATEST_BETREUUNGSMITTEILUNG);
	}

	protected boolean isSame(@Nonnull Betreuungsmitteilung betreuungsmitteilung, @Nonnull Betreuung betreuung) {
		var fromBetreuung = betreuung.getBetreuungenJA();
		var fromMitteilung = betreuungsmitteilung.getBetreuungenJA();

		return EbeguUtil.areComparableCollections(fromBetreuung, fromMitteilung, COMPARATOR_WITH_GUELTIGKEIT);
	}

	@Nonnull
	private ErweiterteBetreuung getOrCreateErweiterteBetreuung(@Nonnull Betreuung betreuung) {
		ErweiterteBetreuungContainer container = betreuung.getErweiterteBetreuungContainer();
		ErweiterteBetreuung erweiterteBetreuung = container.getErweiterteBetreuungJA();

		if (erweiterteBetreuung != null) {
			return erweiterteBetreuung;
		}

		ErweiterteBetreuung created = new ErweiterteBetreuung();
		betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(created);

		return created;
	}
}
