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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractDecimalPensum;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.ExternalClientService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;
import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND;
import static ch.dvbern.ebegu.util.EbeguUtil.coalesce;
import static ch.dvbern.ebegu.util.EbeguUtil.collectionComparator;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@ApplicationScoped
public class PlatzbestaetigungEventHandler extends BaseEventHandler<BetreuungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(PlatzbestaetigungEventHandler.class);

	static final String TECHNICAL_BENUTZER_ID = "88888888-2222-2222-2222-222222222222";
	private static final String BETREFF_KEY = "mutationsmeldung_betreff";
	private static final String MESSAGE_KEY = "mutationsmeldung_message";
	private static final String MESSAGE_MAHLZEIT_KEY = "mutationsmeldung_message_mahlzeitverguenstigung_mit_tarif";
	static final LocalDate GO_LIVE = LocalDate.of(2021, 1, 1);

	private static final Comparator<AbstractMahlzeitenPensum> COMPARATOR = Comparator
		.comparing(AbstractMahlzeitenPensum::getMonatlicheBetreuungskosten)
		.thenComparing(AbstractDecimalPensum::getPensumRounded)
		.thenComparing(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit)
		.thenComparing(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit)
		.thenComparing(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten)
		.thenComparing(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten);

	private static final Comparator<AbstractMahlzeitenPensum> COMPARATOR_WITH_GUELTIGKEIT = COMPARATOR
		.thenComparing(AbstractMahlzeitenPensum::getGueltigkeit);

	private static final Comparator<Betreuungsmitteilung> MITTEILUNG_COMPARATOR = Comparator
		.comparing(Betreuungsmitteilung::getBetreuungspensen, collectionComparator(COMPARATOR_WITH_GUELTIGKEIT));

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private ExternalClientService externalClientService;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull String clientName) {

		Processing processing = attemptProcessing(eventTime, dto, clientName);

		if (!processing.isProcessingSuccess()) {
			String message = processing.getMessage();
			LOG.warn("Event für Betreuung mit RefNr: {} nicht verarbeitet: {}", dto.getRefnr(), message);
		}
	}

	@Nonnull
	protected Processing attemptProcessing(
		@Nonnull LocalDateTime eventTime,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull String clientName) {

		if (dto.getZeitabschnitte().isEmpty()) {
			return Processing.failure("Es wurden keine Zeitabschnitte übergeben.");
		}

		return betreuungService.findBetreuungByBGNummer(dto.getRefnr(), false)
			.map(betreuung -> processEventForBetreuung(eventTime, dto, clientName, betreuung))
			.orElseGet(() -> Processing.failure("Betreuung nicht gefunden."));
	}

	@Nonnull
	private Processing processEventForBetreuung(
		@Nonnull LocalDateTime eventTime,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull String clientName,
		@Nonnull Betreuung betreuung) {

		if (betreuung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (betreuung.getTimestampMutiert() != null && betreuung.getTimestampMutiert().isAfter(eventTime)) {
			return Processing.failure("Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde.");
		}

		if (hasZeitabschnittWithPensumUnit(dto, Zeiteinheit.HOURS) && !betreuung.isAngebotTagesfamilien()) {
			return Processing.failure("Eine Pensum in HOURS kann nur für ein Angebot in einer TFO angegeben werden.");
		}

		if (hasZeitabschnittWithPensumUnit(dto, Zeiteinheit.DAYS) && !betreuung.isAngebotKita()) {
			return Processing.failure("Eine Pensum in DAYS kann nur für ein Angebot in einer Kita angegeben werden.");
		}

		return getExternalClient(clientName, betreuung)
			.map(externalClient -> processEventForExternalClient(dto, betreuung, externalClient.getGueltigkeit()))
			.orElseGet(() -> clientNotFoundFailure(clientName, betreuung));
	}

	private boolean hasZeitabschnittWithPensumUnit(@Nonnull BetreuungEventDTO dto, @Nonnull Zeiteinheit zeiteinheit) {
		return dto.getZeitabschnitte().stream().anyMatch(z -> z.getPensumUnit() == zeiteinheit);
	}

	@Nonnull
	private Optional<InstitutionExternalClient> getExternalClient(
		@Nonnull String clientName,
		@Nonnull Betreuung betreuung) {

		Institution institution = betreuung.getInstitutionStammdaten().getInstitution();
		Collection<InstitutionExternalClient> institutionExternalClients =
			externalClientService.getInstitutionExternalClientForInstitution(institution);

		return institutionExternalClients.stream()
			.filter(iec -> iec.getExternalClient().getClientName().equals(clientName))
			.findAny();
	}

	@Nonnull
	private Processing clientNotFoundFailure(@Nonnull String clientName, @Nonnull Betreuung betreuung) {
		Institution institution = betreuung.getInstitutionStammdaten().getInstitution();

		return Processing.failure(String.format(
			"Kein InstitutionExternalClient Namens >>%s<< ist der Institution %s/%s zugewiesen",
			clientName,
			institution.getName(),
			institution.getId()));
	}

	@Nonnull
	private Processing processEventForExternalClient(
		@Nonnull BetreuungEventDTO dto,
		@Nonnull Betreuung betreuung,
		@Nonnull DateRange clientGueltigkeit) {

		DateRange gesuchsperiode = betreuung.extractGesuchsperiode().getGueltigkeit();
		Optional<DateRange> overlap = gesuchsperiode.getOverlap(clientGueltigkeit);
		if (overlap.isEmpty()) {
			return Processing.failure("Der Client hat innerhalb der Periode keinen Berechtigung.");
		}

		Processing validationProcess = validateZeitabschnitteGueltigkeit(dto, overlap.get());
		if (!validationProcess.isProcessingSuccess()) {
			return validationProcess;
		}

		boolean mahlzeitVergunstigungEnabled = isEnabled(betreuung, GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED);
		ProcessingContext ctx = new ProcessingContext(betreuung, dto, overlap.get(), mahlzeitVergunstigungEnabled);

		Betreuungsstatus status = betreuung.getBetreuungsstatus();

		if (status == Betreuungsstatus.WARTEN) {
			return handlePlatzbestaetigung(ctx);
		}

		if (isMutationsMitteilungStatus(status)) {
			return handleMutationsMitteilung(ctx);
		}

		return Processing.failure("Die Betreuung hat einen ungültigen Status: " + status);
	}

	@Nonnull
	private Processing validateZeitabschnitteGueltigkeit(
		@Nonnull BetreuungEventDTO dto,
		@Nonnull DateRange gueltigkeitInPeriode) {

		List<DateRange> ranges = dto.getZeitabschnitte().stream()
			.map(z -> new DateRange(z.getVon(), z.getBis()))
			.collect(Collectors.toList());

		if (GueltigkeitsUtil.hasOverlap(ranges)) {
			return Processing.failure("Zeitabschnitte dürfen nicht überlappen.");
		}

		if (ranges.stream().noneMatch(r -> r.intersects(gueltigkeitInPeriode))) {
			return Processing.failure("Kein Zeitabschnitt liegt innerhalb Client Gültigkeit & Periode.");
		}

		return Processing.success();
	}

	protected boolean isMutationsMitteilungStatus(@Nonnull Betreuungsstatus status) {
		return status == Betreuungsstatus.VERFUEGT
			|| status == Betreuungsstatus.BESTAETIGT
			|| status == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG
			|| status == Betreuungsstatus.STORNIERT;
	}

	/**
	 * Update the Betreuung and automatically confirm if all required data is present.
	 */
	@Nonnull
	private Processing handlePlatzbestaetigung(@Nonnull ProcessingContext ctx) {
		boolean isReadyForBestaetigen = updateBetreuungForPlatzbestaetigung(ctx);

		if (isReadyForBestaetigen) {
			//noinspection ResultOfMethodCallIgnored
			betreuungService.betreuungPlatzBestaetigen(ctx.getBetreuung());
			LOG.info("Betreuung mit RefNr: {} automatisch bestätigt", ctx.getDto().getRefnr());
		} else {
			//noinspection ResultOfMethodCallIgnored
			betreuungService.saveBetreuung(ctx.getBetreuung(), false);
			LOG.info("Betreuung mit RefNr: {} eingelesen, aber nicht automatisch bestätigt", ctx.getDto().getRefnr());
		}

		return Processing.success();
	}

	/**
	 * Update the Betreuung Object and return if its ready for Bestaetigen
	 */
	private boolean updateBetreuungForPlatzbestaetigung(@Nonnull ProcessingContext ctx) {

		if (!ctx.isGueltigkeitCoveringPeriode()) {
			ctx.requireHumanConfirmation();
		}

		setErweitereBeduerfnisseBestaetigt(ctx);
		setBetreuungInGemeinde(ctx);
		setZeitabschnitteToBetreuung(ctx);

		return ctx.isReadyForBestaetigen();
	}

	private void setErweitereBeduerfnisseBestaetigt(@Nonnull ProcessingContext ctx) {
		ErweiterteBetreuung eb = ctx.getBetreuung().getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
		// Der Wert aus dem DTO wird nur berücksichtigt, wenn bereits bei dem Antrag erweitere Bedürfnisse angemeldet
		// wurden.
		if (eb != null) {
			Boolean claimed = eb.getErweiterteBeduerfnisse();
			boolean confirmed = ctx.getDto().getAusserordentlicherBetreuungsaufwand();

			eb.setErweiterteBeduerfnisseBestaetigt(claimed == confirmed);
		}
	}

	private void setBetreuungInGemeinde(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();
		boolean enabled = isEnabled(betreuung, EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED);

		if (!enabled) {
			// no need to set BetreuungInGemeinde -> continue automated processing
			return;
		}

		String incomingName = ctx.getDto().getGemeindeName();
		Long incomingBfsNumber = ctx.getDto().getGemeindeBfsNr();

		if (incomingName == null && incomingBfsNumber == null) {
			// Gemeinde not received, cannot evaluate -> abort automated processing
			ctx.requireHumanConfirmation();
			return;
		}

		Gemeinde gemeinde = betreuung.extractGemeinde();
		Long bfsNummer = gemeinde.getBfsNummer();
		String name = gemeinde.getName();

		getOrCreateErweiterteBetreuung(betreuung)
			.setBetreuungInGemeinde(bfsNummer.equals(incomingBfsNumber) || name.equalsIgnoreCase(incomingName));
	}

	private void setZeitabschnitteToBetreuung(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();
		DateRange gueltigkeit = ctx.getGueltigkeitInPeriode();

		List<BetreuungspensumContainer> containersToUpdate = betreuung.getBetreuungspensumContainers().stream()
			.filter(c -> c.getGueltigkeit().intersects(gueltigkeit))
			.collect(Collectors.toList());
		betreuung.getBetreuungspensumContainers().removeAll(containersToUpdate);

		// first deal with gueltigBis, since findLast and findFirst might return the same container, but only for last
		// we create a copy (and we want to copy before we mutate).
		Optional<BetreuungspensumContainer> overlappingGueltigBis = GueltigkeitsUtil.findLast(containersToUpdate)
			.filter(last -> last.getGueltigkeit().endsAfter(gueltigkeit))
			.map(BetreuungspensumContainer::copyWithPensumJA)
			.map(copy -> {
				copy.getGueltigkeit().setGueltigAb(gueltigkeit.getGueltigBis().plusDays(1));

				return copy;
			});

		GueltigkeitsUtil.findFirst(containersToUpdate)
			.filter(first -> first.getGueltigkeit().startsBefore(gueltigkeit))
			.ifPresent(first -> first.getGueltigkeit().setGueltigBis(gueltigkeit.getGueltigAb().minusDays(1)));

		// everything still affecting gueltigkeit is obsolete (will be replaced with import data)
		containersToUpdate.removeIf(c -> c.getGueltigkeit().intersects(gueltigkeit));

		List<BetreuungspensumContainer> toImport =
			convertZeitabschnitte(ctx, gueltigkeit, z -> toBetreuungspensumContainer(z, ctx));

		overlappingGueltigBis.ifPresent(containersToUpdate::add);
		betreuung.getBetreuungspensumContainers().addAll(containersToUpdate);
		betreuung.getBetreuungspensumContainers().addAll(toImport);
	}

	@Nonnull
	private <T extends Gueltigkeit> List<T> convertZeitabschnitte(
		@Nonnull ProcessingContext ctx,
		@Nonnull DateRange gueltigkeit,
		@Nonnull Function<ZeitabschnittDTO, T> mappingFunction) {

		List<T> toImport = ctx.getDto().getZeitabschnitte().stream()
			.filter(z -> new DateRange(z.getVon(), z.getBis()).intersects(gueltigkeit))
			.map(mappingFunction)
			.collect(Collectors.toList());

		GueltigkeitsUtil.findFirst(toImport)
			.filter(first -> first.getGueltigkeit().startsBefore(gueltigkeit))
			.ifPresent(first -> first.getGueltigkeit().setGueltigAb(gueltigkeit.getGueltigAb()));

		GueltigkeitsUtil.findLast(toImport)
			.filter(last -> last.getGueltigkeit().endsAfter(gueltigkeit))
			.ifPresent(last -> last.getGueltigkeit().setGueltigBis(gueltigkeit.getGueltigBis()));

		return toImport;
	}

	@Nonnull
	private BetreuungspensumContainer toBetreuungspensumContainer(
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull ProcessingContext ctx) {

		Betreuungspensum betreuungspensum = toAbstractMahlzeitenPensum(new Betreuungspensum(), zeitabschnittDTO, ctx);

		BetreuungspensumContainer container = new BetreuungspensumContainer();
		container.setBetreuungspensumJA(betreuungspensum);
		container.setBetreuung(ctx.getBetreuung());

		return container;
	}

	@Nonnull
	private Processing handleMutationsMitteilung(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();

		Collection<Betreuungsmitteilung> open =
			mitteilungService.findOffeneBetreuungsmitteilungenForBetreuung(betreuung);

		Optional<Betreuungsmitteilung> latest = findLatest(open);

		Betreuungsmitteilung betreuungsmitteilung = createBetreuungsmitteilung(ctx, latest);

		if (latest.filter(l -> MITTEILUNG_COMPARATOR.compare(l, betreuungsmitteilung) == 0).isPresent()) {
			return Processing.failure("Die Betreuungsmeldung ist identisch mit der neusten offenen Betreuungsmeldung.");
		}

		if (latest.isEmpty() && isSame(betreuungsmitteilung, betreuung)) {
			return Processing.failure("Die Betreuungsmeldung und die Betreuung sind identisch.");
		}

		//noinspection ResultOfMethodCallIgnored
		mitteilungService.sendBetreuungsmitteilung(betreuungsmitteilung);
		LOG.info("Mutationsmeldung erstellt für die Betreuung mit RefNr: {}", ctx.getDto().getRefnr());

		return Processing.success();
	}

	@Nonnull
	private Betreuungsmitteilung createBetreuungsmitteilung(
		@Nonnull ProcessingContext ctx,
		@Nonnull Optional<Betreuungsmitteilung> latest) {

		Betreuung betreuung = ctx.getBetreuung();
		Gesuch gesuch = betreuung.extractGesuch();
		DateRange mutationRange = getMutationRange(ctx);
		Locale locale = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService).getLocale();

		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setDossier(gesuch.getDossier());
		betreuungsmitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
		betreuungsmitteilung.setSender(getMutationsmeldungBenutzer());
		betreuungsmitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
		betreuungsmitteilung.setEmpfaenger(gesuch.getDossier().getFall().getBesitzer());
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		betreuungsmitteilung.setSubject(ServerMessageUtil.getMessage(BETREFF_KEY, locale));
		betreuungsmitteilung.setBetreuung(betreuung);

		List<BetreuungsmitteilungPensum> existing = getExisting(ctx, latest, mutationRange);

		List<BetreuungsmitteilungPensum> toImport =
			convertZeitabschnitte(ctx, mutationRange, z -> toBetreuungsmitteilungPensum(z, ctx));

		existing.addAll(toImport);
		betreuungsmitteilung.getBetreuungspensen().addAll(extendGueltigkeit(existing));
		betreuungsmitteilung.getBetreuungspensen().forEach(p -> p.setBetreuungsmitteilung(betreuungsmitteilung));

		BiFunction<BetreuungsmitteilungPensum, Integer, String> messageMapper = ctx.isMahlzeitVergunstigungEnabled() ?
			mahlzeitenMessage(locale) :
			defaultMessage(locale);

		betreuungsmitteilung.setMessage(getMessage(messageMapper, betreuungsmitteilung.getBetreuungspensen()));

		return betreuungsmitteilung;
	}

	@Nonnull
	private List<BetreuungsmitteilungPensum> getExisting(
		@Nonnull ProcessingContext ctx,
		@Nonnull Optional<Betreuungsmitteilung> latest,
		@Nonnull DateRange mutationRange) {

		LocalDate mutationRangeAb = mutationRange.getGueltigAb();
		LocalDate mutationRangeBis = mutationRange.getGueltigBis();

		List<BetreuungsmitteilungPensum> existing =
			getExistingFromLatestOrBetreuung(ctx.getBetreuung(), latest, mutationRange);

		Optional<BetreuungsmitteilungPensum> overlappingGueltigBis =
			GueltigkeitsUtil.findAnyAtStichtag(existing, mutationRangeBis)
				.filter(overlappingBis -> overlappingBis.getGueltigkeit().endsAfter(mutationRange))
				.map(BetreuungsmitteilungPensum::copy)
				.map(copy -> {
					copy.getGueltigkeit().setGueltigAb(mutationRangeBis.plusDays(1));

					return copy;
				});

		GueltigkeitsUtil.findAnyAtStichtag(existing, mutationRangeAb)
			.filter(overlappingAb -> overlappingAb.getGueltigkeit().startsBefore(mutationRange))
			.ifPresent(overlappingAb -> overlappingAb.getGueltigkeit().setGueltigBis(mutationRangeAb.minusDays(1)));

		overlappingGueltigBis.ifPresent(existing::add);

		return existing;
	}

	@Nonnull
	private List<BetreuungsmitteilungPensum> getExistingFromLatestOrBetreuung(
		@Nonnull Betreuung betreuung,
		@Nonnull Optional<Betreuungsmitteilung> latest,
		@Nonnull DateRange mutationRange) {

		return latest
			.map(existing -> existing.getBetreuungspensen().stream()
				.filter(c -> !mutationRange.contains(c.getGueltigkeit()))
				.map(BetreuungsmitteilungPensum::copy))
			.orElseGet(() -> betreuung.getBetreuungspensumContainers().stream()
				.filter(c -> !mutationRange.contains(c.getGueltigkeit()))
				.map(this::fromBetreuungspensumContainer))
			.collect(Collectors.toList());
	}

	@Nonnull
	private BetreuungsmitteilungPensum fromBetreuungspensumContainer(@Nonnull BetreuungspensumContainer container) {
		BetreuungsmitteilungPensum pensum = new BetreuungsmitteilungPensum();

		container.getBetreuungspensumJA()
			.copyAbstractBetreuungspensumMahlzeitenEntity(pensum, AntragCopyType.MUTATION);

		return pensum;
	}

	@Nonnull
	private BetreuungsmitteilungPensum toBetreuungsmitteilungPensum(
		@Nonnull ZeitabschnittDTO zeitabschnitt,
		@Nonnull ProcessingContext ctx) {

		return toAbstractMahlzeitenPensum(new BetreuungsmitteilungPensum(), zeitabschnitt, ctx);
	}

	@Nonnull
	private Optional<Betreuungsmitteilung> findLatest(@Nonnull Collection<Betreuungsmitteilung> open) {
		Comparator<Mitteilung> bySentDateTime = Comparator
			.comparing(Mitteilung::getSentDatum, Comparator.nullsFirst(Comparator.naturalOrder()))
			.thenComparing(AbstractEntity::getTimestampErstellt, Comparator.nullsFirst(Comparator.naturalOrder()))
			.thenComparing(AbstractEntity::getId);

		return open.stream().max(bySentDateTime);
	}

	@Nonnull
	private DateRange getMutationRange(@Nonnull ProcessingContext ctx) {
		DateRange gueltigkeitInPeriode = ctx.getGueltigkeitInPeriode();

		return gueltigkeitInPeriode.getGueltigAb().isBefore(GO_LIVE) ?
			new DateRange(GO_LIVE, gueltigkeitInPeriode.getGueltigBis()) :
			gueltigkeitInPeriode;
	}

	@Nonnull
	private Benutzer getMutationsmeldungBenutzer() {
		return benutzerService.findBenutzerById(TECHNICAL_BENUTZER_ID)
			.orElseThrow(() -> new EbeguEntityNotFoundException(EMPTY, ERROR_ENTITY_NOT_FOUND, TECHNICAL_BENUTZER_ID));
	}

	@Nonnull
	private String getMessage(
		@Nonnull BiFunction<BetreuungsmitteilungPensum, Integer, String> messageMapper,
		@Nonnull Set<BetreuungsmitteilungPensum> pensen) {

		List<BetreuungsmitteilungPensum> sorted = pensen.stream()
			.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR)
			.collect(Collectors.toList());

		return IntStream.rangeClosed(1, sorted.size())
			.mapToObj(index -> messageMapper.apply(sorted.get(index - 1), index))
			.collect(Collectors.joining(StringUtils.LF));
	}

	@Nonnull
	private BiFunction<BetreuungsmitteilungPensum, Integer, String> mahlzeitenMessage(@Nonnull Locale lang) {
		return (pensum, counter) -> ServerMessageUtil.getMessage(
			MESSAGE_MAHLZEIT_KEY,
			lang,
			counter,
			pensum.getGueltigkeit().getGueltigAb(),
			pensum.getGueltigkeit().getGueltigBis(),
			pensum.getPensum(),
			pensum.getMonatlicheBetreuungskosten(),
			pensum.getMonatlicheHauptmahlzeiten(),
			pensum.getTarifProHauptmahlzeit(),
			pensum.getMonatlicheNebenmahlzeiten(),
			pensum.getTarifProNebenmahlzeit());
	}

	@Nonnull
	private BiFunction<BetreuungsmitteilungPensum, Integer, String> defaultMessage(@Nonnull Locale lang) {
		return (pensum, counter) -> ServerMessageUtil.getMessage(
			MESSAGE_KEY,
			lang,
			counter,
			pensum.getGueltigkeit().getGueltigAb(),
			pensum.getGueltigkeit().getGueltigBis(),
			pensum.getPensum(),
			pensum.getMonatlicheBetreuungskosten());
	}

	@Nonnull
	@CanIgnoreReturnValue
	protected <T extends AbstractMahlzeitenPensum> T toAbstractMahlzeitenPensum(
		@Nonnull T target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull ProcessingContext ctx) {

		target.getGueltigkeit().setGueltigAb(zeitabschnittDTO.getVon());
		target.getGueltigkeit().setGueltigBis(zeitabschnittDTO.getBis());
		target.setMonatlicheBetreuungskosten(zeitabschnittDTO.getBetreuungskosten());
		setPensum(target, zeitabschnittDTO);
		target.setMonatlicheHauptmahlzeiten(coalesce(zeitabschnittDTO.getAnzahlHauptmahlzeiten(), ZERO));
		target.setMonatlicheNebenmahlzeiten(coalesce(zeitabschnittDTO.getAnzahlNebenmahlzeiten(), ZERO));

		if (ctx.isMahlzeitVergunstigungEnabled()) {
			setTarifeProMahlzeiten(target, zeitabschnittDTO, ctx);
		}

		return target;
	}

	private <T extends AbstractMahlzeitenPensum> void setPensum(
		@Nonnull T target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO) {

		switch (zeitabschnittDTO.getPensumUnit()) {
		case DAYS:
			target.setPensumFromDays(zeitabschnittDTO.getBetreuungspensum());
			break;
		case HOURS:
			target.setPensumFromHours(zeitabschnittDTO.getBetreuungspensum());
			break;
		case PERCENTAGE:
			target.setPensumFromPercentage(zeitabschnittDTO.getBetreuungspensum());
			break;
		default:
			throw new IllegalArgumentException("Unsupported pensum unit: " + zeitabschnittDTO.getPensumUnit());
		}
	}

	private <T extends AbstractMahlzeitenPensum> void setTarifeProMahlzeiten(
		@Nonnull T target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull ProcessingContext ctx) {

		// Die Mahlzeitkosten koennen null sein, wir nehmen dann die default Werten
		if (zeitabschnittDTO.getTarifProHauptmahlzeiten() != null) {
			target.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
		} else {
			target.setVollstaendig(false);
			ctx.requireHumanConfirmation();
		}
		if (zeitabschnittDTO.getTarifProNebenmahlzeiten() != null) {
			target.setTarifProNebenmahlzeit(zeitabschnittDTO.getTarifProNebenmahlzeiten());
		} else {
			target.setVollstaendig(false);
			ctx.requireHumanConfirmation();
		}
	}

	protected boolean isSame(@Nonnull Betreuungsmitteilung betreuungsmitteilung, @Nonnull Betreuung betreuung) {
		List<? extends AbstractMahlzeitenPensum> fromBetreuung = betreuung.getBetreuungspensumContainers().stream()
			.map(BetreuungspensumContainer::getBetreuungspensumJA)
			.collect(Collectors.toList());

		Set<? extends AbstractMahlzeitenPensum> fromMitteilung = betreuungsmitteilung.getBetreuungspensen();

		return EbeguUtil.areComparableCollections(fromBetreuung, fromMitteilung, COMPARATOR_WITH_GUELTIGKEIT);
	}

	private boolean isEnabled(@Nonnull Betreuung betreuung, @Nonnull EinstellungKey key) {
		Gemeinde gemeinde = betreuung.extractGemeinde();
		Gesuchsperiode periode = betreuung.extractGesuchsperiode();

		return einstellungService.findEinstellung(key, gemeinde, periode).getValueAsBoolean();
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

	/**
	 * When adjacent pensen are comparable, merge them together to one pensum with extended Gueltigkeit
	 */
	@Nonnull
	protected Collection<BetreuungsmitteilungPensum> extendGueltigkeit(
		@Nonnull Collection<BetreuungsmitteilungPensum> pensen) {

		if (pensen.size() <= 1) {
			return pensen;
		}

		List<BetreuungsmitteilungPensum> sorted = pensen.stream()
			.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR)
			.collect(Collectors.toList());

		List<BetreuungsmitteilungPensum> result = new ArrayList<>();
		Iterator<BetreuungsmitteilungPensum> iter = sorted.iterator();
		BetreuungsmitteilungPensum current = iter.next();
		result.add(current);

		while (iter.hasNext()) {
			BetreuungsmitteilungPensum next = iter.next();

			if (areAdjacent(current, next) && areSame(current, next)) {
				// extend gueltigkeit of current
				current.getGueltigkeit().setGueltigBis(next.getGueltigkeit().getGueltigBis());
				continue;
			}

			current = next;
			result.add(next);
		}

		return result;
	}

	private boolean areAdjacent(@Nonnull Gueltigkeit current, @Nonnull Gueltigkeit next) {
		return current.getGueltigkeit().endsDayBefore(next.getGueltigkeit());
	}

	private boolean areSame(
		@Nonnull BetreuungsmitteilungPensum current,
		@Nonnull BetreuungsmitteilungPensum next) {

		return COMPARATOR.compare(current, next) == 0;
	}
}
