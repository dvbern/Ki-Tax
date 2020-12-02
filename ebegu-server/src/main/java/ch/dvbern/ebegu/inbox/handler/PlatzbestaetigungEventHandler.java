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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Sprache;
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
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PlatzbestaetigungEventHandler extends BaseEventHandler<BetreuungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(PlatzbestaetigungEventHandler.class);
	private static final BigDecimal MAX_TAGE_PRO_MONAT = new BigDecimal("20.00");
	private static final BigDecimal MAX_STUNDEN_PRO_MONAT = new BigDecimal("220.00");
	private static final String TECHNICAL_BENUTZER_ID = "88888888-2222-2222-2222-222222222222";
	private static final String BETREFF_KEY = "mutationsmeldung_betreff";
	private static final String MESSAGE_KEY = "mutationsmeldung_message";
	private static final String MESSAGE_MAHLZEIT_KEY = "mutationsmeldung_message_mahlzeitverguenstigung_mit_tarif";
	private static final Integer GO_LIVE_YEAR = 2021;
	private static final Integer GO_LIVE_MONTH = 1;
	private static final Integer GO_LIVE_DAY = 1;

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
		@Nonnull String clientName
	) {
		String refnr = dto.getRefnr();
		try {
			Optional<Betreuung> betreuungOpt = betreuungService.findBetreuungByBGNummer(refnr, false);
			if (!betreuungOpt.isPresent()) {
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: {} existiert nicht!", refnr);
				return;
			}
			Betreuung betreuung = betreuungOpt.get();
			if (betreuung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
				LOG.warn(
					"Platzbestaetigung: die Gesuchsperiode fuer die Betreuung mit RefNr: {} ist nicht aktiv!",
					refnr);
				return;
			}
			if (betreuung.getTimestampMutiert() != null && betreuung.getTimestampMutiert().isAfter(eventTime)) {
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: {} war spaeter als dieser "
					+ "Event im kiBon bearbeitet! Event ist ignoriert", refnr);
				return;
			}
			//Get InstitutinClient Gueltigkeit
			Collection<InstitutionExternalClient> institutionExternalClients =
				externalClientService.getInstitutionExternalClientForInstitution(
					betreuung.getInstitutionStammdaten().getInstitution());
			InstitutionExternalClient institutionExternalClient = institutionExternalClients.stream()
				.filter(iec -> iec.getExternalClient().getClientName().equals(clientName))
				.findAny()
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"processEvent",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					clientName, betreuung.getInstitutionStammdaten().getInstitution().getName()));

			if (betreuung.getBetreuungsstatus() == Betreuungsstatus.WARTEN) {
				//Update the Betreuung and check if all data are available
				if (setBetreuungDaten(
					new PlatzbestaetigungProcessingContext(betreuung, dto),
					institutionExternalClient.getGueltigkeit())) {
					//noinspection ResultOfMethodCallIgnored
					betreuungService.betreuungPlatzBestaetigen(betreuung);
					LOG.info("Platzbestaetigung: Betreuung mit RefNr: {} automatisch bestätigt", refnr);
				} else {
					//noinspection ResultOfMethodCallIgnored
					betreuungService.saveBetreuung(betreuung, false);
					LOG.info(
						"Platzbestaetigung: Betreuung mit RefNr: {} eingelesen, aber nicht automatisch bestätigt",
						refnr);
				}
			} else if (betreuung.getBetreuungsstatus() == Betreuungsstatus.VERFUEGT
				|| betreuung.getBetreuungsstatus() == Betreuungsstatus.BESTAETIGT) {
				if (isSame(dto, betreuung)) {
					LOG.warn("Platzbestaetigung: die Betreuung ist identisch wie der Event mit RefNr: {}"
						+
						" - MutationMitteilung wird nicht erstellt!", refnr);
					return;
				}
				//MutationMitteilungErstellen
				//we map all the data we know in a mitteilung object, we are only interested into Zeitabschnitt:
				Betreuungsmitteilung betreuungsmitteilung =
					this.setBetreuungsmitteilungDaten(dto, betreuung, institutionExternalClient.getGueltigkeit());
				if (betreuungsmitteilung != null) {
					// we first clear all the Mutationsmeldungen for the current Betreuung
					mitteilungService.removeOffeneBetreuungsmitteilungenForBetreuung(betreuung);
					// and then send the new Betreuungsmitteilung an die Gemeinde
					//noinspection ResultOfMethodCallIgnored
					this.mitteilungService.sendBetreuungsmitteilung(betreuungsmitteilung);
					LOG.info("Mutationsmeldung erstellt für die Betreuung mit RefNr: {}", refnr);
				}
			} else {
				LOG.warn(
					"Platzbestaetigung: die Betreuung mit RefNr: {} hat einen ungültigen Status: {}",
					refnr,
					betreuung.getBetreuungsstatus());
			}
		} catch (Exception e) {
			LOG.error("Error while processing the record: {} error: {}", refnr, e.getMessage());
		}
	}

	/**
	 * Update the Betreuung Object and return if its ready for Bestaetigen
	 */
	private boolean setBetreuungDaten(
		@Nonnull PlatzbestaetigungProcessingContext ctx,
		@Nonnull DateRange gueltigkeit
	) {
		//Check if die Gemeinde erlaubt mahlzeitvergunstigung:
		boolean mahlzeitVergunstigungEnabled =
			isEnabled(ctx.getBetreuung(), EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED);
		setErweitereBeduerfnisseBestaetigt(ctx);
		setBetreuungInGemeinde(ctx);
		setZeitabschnitte(ctx, mahlzeitVergunstigungEnabled, gueltigkeit);

		return ctx.isReadyForBestaetigen();
	}

	private void setErweitereBeduerfnisseBestaetigt(@Nonnull PlatzbestaetigungProcessingContext ctx) {
		ErweiterteBetreuung eb = ctx.getBetreuung().getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
		// Der Wert aus dem DTO wird nur berücksichtigt, wenn bereits bei dem Antrag erweitere Bedürfnisse angemeldet
		// wurden.
		if (eb != null && eb.getErweiterteBeduerfnisse()) {
			eb.setErweiterteBeduerfnisseBestaetigt(ctx.getDto().getAusserordentlicherBetreuungsaufwand());
		}
	}

	private void setBetreuungInGemeinde(@Nonnull PlatzbestaetigungProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();
		boolean enabled = isEnabled(betreuung, EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED);

		if (!enabled) {
			// no need to set BetreuungInGemeinde -> continue automated processing
			return;
		}

		String incomingGemeindeName = ctx.getDto().getGemeindeName();
		if (incomingGemeindeName == null) {
			// Gemeinde not received, cannot evaluate -> abort automated processing
			ctx.requireHumanConfirmation();
			return;
		}

		Gemeinde gemeinde = betreuung.extractGemeinde();

		if (!gemeinde.getName().equalsIgnoreCase(incomingGemeindeName)) {
			getOrCreateErweiterteBetreuung(betreuung)
				.setBetreuungInGemeinde(false);
		}
	}

	protected void setZeitabschnitte(
		@Nonnull PlatzbestaetigungProcessingContext ctx,
		boolean mahlzeitVergunstigungEnabled,
		@Nonnull DateRange gueltigkeit
	) {
		List<ZeitabschnittDTO> zeitabschnitte = ctx.getDto().getZeitabschnitte();
		if (zeitabschnitte.isEmpty()) {
			ctx.requireHumanConfirmation();

			return;
		}

		Betreuung betreuung = ctx.getBetreuung();
		List<BetreuungspensumContainer> currentBetreuungspensumContainers = adaptBetreuung(betreuung, gueltigkeit);
		betreuung.getBetreuungspensumContainers().clear();
		betreuung.getBetreuungspensumContainers().addAll(currentBetreuungspensumContainers);

		zeitabschnitte.stream()
			.map(z -> zeitabschnittToBetreuungspensumContainer(mahlzeitVergunstigungEnabled, ctx, z, gueltigkeit))
			.filter(Objects::nonNull)
			.forEach(c -> betreuung.getBetreuungspensumContainers().add(c));
	}

	@Nullable
	private BetreuungspensumContainer zeitabschnittToBetreuungspensumContainer(
		boolean mahlzeitVergunstigungEnabled,
		@Nonnull PlatzbestaetigungProcessingContext ctx,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull DateRange gueltigkeit) {

		Betreuung betreuung = ctx.getBetreuung();
		Betreuungspensum betreuungspensum = mapZeitabschnitt(new Betreuungspensum(), zeitabschnittDTO, betreuung);
		if (betreuungspensum == null) {
			ctx.requireHumanConfirmation();
			return null;
		}
		// Adaptieren die Gueltigkeit oder betpens loeschen gemaess Schnittstelle Gueltigkeit
		if (!gueltigkeit.contains(betreuungspensum.getGueltigkeit())) {
			if (betreuungspensum.getGueltigkeit().getGueltigAb().isBefore(gueltigkeit.getGueltigAb())) {
				if (betreuungspensum.getGueltigkeit().getGueltigBis().isBefore(gueltigkeit.getGueltigAb())) {
					return null;
				}
				betreuungspensum.getGueltigkeit().setGueltigAb(gueltigkeit.getGueltigAb());
			}
			if (betreuungspensum.getGueltigkeit().getGueltigBis().isAfter(gueltigkeit.getGueltigBis())) {
				if (betreuungspensum.getGueltigkeit().getGueltigAb().isAfter(gueltigkeit.getGueltigBis())) {
					return null;
				}
				betreuungspensum.getGueltigkeit().setGueltigBis(gueltigkeit.getGueltigBis());
			}
		}

		if (mahlzeitVergunstigungEnabled) {
			setTarifeProMahlzeiten(ctx, zeitabschnittDTO, betreuungspensum);
		}

		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);
		betreuungspensumContainer.setBetreuung(betreuung);

		return betreuungspensumContainer;
	}

	private void setTarifeProMahlzeiten(
		@Nonnull PlatzbestaetigungProcessingContext ctx,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull Betreuungspensum betreuungspensum) {

		// Die Mahlzeitkosten koennen null sein, wir nehmen dann die default Werten
		if (zeitabschnittDTO.getTarifProHauptmahlzeiten() != null) {
			betreuungspensum.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
		} else {
			ctx.requireHumanConfirmation();
		}
		if (zeitabschnittDTO.getTarifProNebenmahlzeiten() != null) {
			betreuungspensum.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
		} else {
			ctx.requireHumanConfirmation();
		}
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
	 * Create a new Betreuungsmitteilung Object and return if its ready for Bestaetigen
	 *
	 * @return Betreuungsmitteilung oder null
	 */
	@SuppressWarnings("PMD.NcssMethodCount")
	@Nullable
	private Betreuungsmitteilung setBetreuungsmitteilungDaten(
		@Nonnull BetreuungEventDTO dto,
		@Nonnull Betreuung betreuung,
		@Nonnull DateRange gueltigkeit
	) {
		if (dto.getZeitabschnitte().isEmpty()) {
			LOG.error("Zeitabschnitt are missing, we cannot work with this dto");
			return null;
		}
		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		Gesuch gesuch = betreuung.extractGesuch();
		betreuungsmitteilung.setDossier(gesuch.getDossier());
		betreuungsmitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
		//we don't have any sender...
		Benutzer benutzer =
			benutzerService.findBenutzerById(TECHNICAL_BENUTZER_ID)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					StringUtils.EMPTY,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					TECHNICAL_BENUTZER_ID));
		betreuungsmitteilung.setSender(benutzer);
		betreuungsmitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
		betreuungsmitteilung.setEmpfaenger(gesuch.getDossier().getFall().getBesitzer());
		betreuungsmitteilung.setBetreuung(betreuung);
		Gemeinde gemeinde = betreuung.extractGemeinde();
		Optional<GemeindeStammdaten> stammdaten = gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId());
		Locale sprache = Sprache.DEUTSCH.getLocale();
		if (stammdaten.isPresent() && stammdaten.get().getKorrespondenzsprache() == KorrespondenzSpracheTyp.FR) {
			sprache = Sprache.FRANZOESISCH.getLocale();
		}

		betreuungsmitteilung.setSubject(translate(BETREFF_KEY, sprache));

		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);

		//Pensen mappen and create message
		Gesuchsperiode gesuchsperiode = betreuung.extractGesuchsperiode();
		Einstellung mahlzeitVergunstigungEnabled =
			einstellungService.findEinstellung(
				EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
				gemeinde,
				gesuchsperiode);
		StringBuilder message = new StringBuilder();
		int counter = 1;
		boolean areZeitabschnittCorrupted = false;

		List<ZeitabschnittDTO> zeitabschnitteToImport = mapZeitabschnitteToImport(dto, betreuung, gueltigkeit);
		//
		//		if(zeitabschnitteToImport.isEmpty()) {
		//			LOG.info("There are no Zeitabschnitte to import for the Gueltigkeit of this institution");
		//			return null;
		//		}

		for (ZeitabschnittDTO zeitabschnittDTO : zeitabschnitteToImport) {
			BetreuungsmitteilungPensum betreuungsmitteilungPensum = mapZeitabschnitt(new BetreuungsmitteilungPensum()
				, zeitabschnittDTO, betreuung);
			if (betreuungsmitteilungPensum == null) {
				areZeitabschnittCorrupted = true;
				continue;
			}
			betreuungsmitteilungPensum.setVollstaendig(!areZeitabschnittCorrupted);
			if (message.length() > 0) {
				message.append(StringUtils.LF);
			}
			if (mahlzeitVergunstigungEnabled.getValueAsBoolean()) {
				//Die Mahlzeitkosten koennen null sein, wir nehmen dann die default Werten
				if (zeitabschnittDTO.getTarifProHauptmahlzeiten() != null) {
					betreuungsmitteilungPensum.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
				} else {
					//Die MutationsMitteilung soll in Status WARTEN eroeffnet werden
					betreuungsmitteilungPensum.setVollstaendig(false);
				}
				if (zeitabschnittDTO.getTarifProNebenmahlzeiten() != null) {
					betreuungsmitteilungPensum.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
				} else {
					//Die MutationsMitteilung soll in Status WARTEN eroeffnet werden
					betreuungsmitteilungPensum.setVollstaendig(false);
				}
				message.append(mahlzeitenMessage(sprache, counter, betreuungsmitteilungPensum));
			} else {
				message.append(defaultMessage(sprache, counter, betreuungsmitteilungPensum));
			}
			//set betreuungsmitteilungPensum in model
			betreuungsmitteilungPensum.setBetreuungsmitteilung(betreuungsmitteilung);
			betreuungsmitteilung.getBetreuungspensen().add(betreuungsmitteilungPensum);
			counter++;
		}
		betreuungsmitteilung.setMessage(message.toString());
		return betreuungsmitteilung;
	}

	@Nonnull
	protected List<ZeitabschnittDTO> mapZeitabschnitteToImport(
		@Nonnull BetreuungEventDTO dto,
		@Nonnull Betreuung betreuung,
		@Nonnull DateRange gueltigkeit) {

		if (gueltigkeit.getGueltigAb().isBefore(LocalDate.of(GO_LIVE_YEAR, GO_LIVE_MONTH, GO_LIVE_DAY))) {
			gueltigkeit.setGueltigAb(LocalDate.of(GO_LIVE_YEAR, GO_LIVE_MONTH, GO_LIVE_DAY));
		}

		List<ZeitabschnittDTO> zeitabschnitteToImport = filterZeitabschnitte(
			dto.getZeitabschnitte(), gueltigkeit
		);
		//
		//		// we don't have to adapt anything if there is no zeiabschnitt to import and can return early
		//		if (zeitabschnitteToImport.isEmpty()) {
		//			return zeitabschnitteToImport;
		//		}
		List<BetreuungspensumContainer> currentBetreuungspensumContainers = adaptBetreuung(betreuung, gueltigkeit);
		List<Betreuungspensum> currentPensen = currentBetreuungspensumContainers.stream().map(
			BetreuungspensumContainer::getBetreuungspensumJA).collect(Collectors.toList());

		// We want to keep only the zeitabschnitt from before the go live date, therefore we can
		// keep the zeitabschnitte that end before the go live date and for the pensen around the go live we split the
		// zeitabschnitt,
		// end the first zeitabschnitt on the go live date and ignore the zeitabschnitt after
		List<ZeitabschnittDTO> zeitabschnitteFromCurrentPensenToKeep = currentPensen.stream()
			.map(this::pensumToZeitabschnittDTO)
			.collect(Collectors.toList());

		zeitabschnitteToImport.addAll(0, zeitabschnitteFromCurrentPensenToKeep);

		return zeitabschnitteToImport;
	}

	@Nonnull
	private List<ZeitabschnittDTO> filterZeitabschnitte(
		@Nonnull List<ZeitabschnittDTO> zeitabschnitte,
		@Nonnull DateRange gueltigkeit
	) {
		List<ZeitabschnittDTO> zeitabschnittDTOS = new ArrayList<>();
		for (ZeitabschnittDTO zeitabschnittDTO : zeitabschnitte) {
			if (zeitabschnittDTO.getVon().isBefore(gueltigkeit.getGueltigAb())) {
				if (zeitabschnittDTO.getBis().isBefore(gueltigkeit.getGueltigAb())) {
					continue;
				}
				zeitabschnittDTO.setVon(gueltigkeit.getGueltigAb());
			}
			if (zeitabschnittDTO.getBis().isAfter(gueltigkeit.getGueltigBis())) {
				if (zeitabschnittDTO.getVon().isAfter(gueltigkeit.getGueltigBis())) {
					continue;
				}
				zeitabschnittDTO.setBis(gueltigkeit.getGueltigBis());
			}
			zeitabschnittDTOS.add(zeitabschnittDTO);
		}
		return zeitabschnittDTOS;
	}

	@Nonnull
	private ZeitabschnittDTO pensumToZeitabschnittDTO(@Nonnull Betreuungspensum pensum) {

		Zeiteinheit pensumUnit;

		switch (pensum.getUnitForDisplay()) {
		case DAYS:
			pensumUnit = Zeiteinheit.DAYS;
			break;
		case HOURS:
			pensumUnit = Zeiteinheit.HOURS;
			break;
		case PERCENTAGE:
			pensumUnit = Zeiteinheit.PERCENTAGE;
			break;
		default:
			pensumUnit = null;
		}

		return new ZeitabschnittDTO(
			pensum.getMonatlicheBetreuungskosten(),
			pensum.getPensum(),
			pensum.getGueltigkeit().getGueltigAb(),
			pensum.getGueltigkeit().getGueltigBis(),
			pensumUnit,
			pensum.getMonatlicheHauptmahlzeiten(),
			pensum.getMonatlicheNebenmahlzeiten(),
			pensum.getTarifProHauptmahlzeit(),
			pensum.getTarifProNebenmahlzeit()
		);
	}

	@Nonnull
	private String mahlzeitenMessage(@Nonnull Locale lang, int counter, @Nonnull BetreuungsmitteilungPensum pensum) {
		return translate(MESSAGE_MAHLZEIT_KEY, lang, counter,
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
	private String defaultMessage(@Nonnull Locale lang, int counter, @Nonnull BetreuungsmitteilungPensum pensum) {
		return translate(MESSAGE_KEY, lang, counter,
			pensum.getGueltigkeit().getGueltigAb(),
			pensum.getGueltigkeit().getGueltigBis(),
			pensum.getPensum(),
			pensum.getMonatlicheBetreuungskosten());
	}

	@Nullable
	protected <T extends AbstractMahlzeitenPensum> T mapZeitabschnitt(
		T neueBetreuung,
		ZeitabschnittDTO zeitabschnittDTO,
		Betreuung betreuung) {

		if (zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.PERCENTAGE.name())) {
			neueBetreuung.setPensum(zeitabschnittDTO.getBetreuungspensum()); // schauen ob es so korrekt ist
			neueBetreuung.setUnitForDisplay(PensumUnits.PERCENTAGE);
		} else if (betreuung.isAngebotKita()) {
			if (!zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.DAYS.name())) {
				LOG.error("Pensum is not supported, Zeitabschnitt ist corrupted");
				return null;
			}
			neueBetreuung.setUnitForDisplay(PensumUnits.DAYS);
			BigDecimal pensumInPercent =
				MathUtil.EXACT.divide(
					MathUtil.HUNDRED.multiply(zeitabschnittDTO.getBetreuungspensum()),
					MAX_TAGE_PRO_MONAT);
			neueBetreuung.setPensum(pensumInPercent);
		} else if (betreuung.isAngebotTagesfamilien()) {
			if (!zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.HOURS.name())) {
				LOG.error("Pensum is not supported, Zeitabschnitt ist corrupted");
				return null;
			}
			neueBetreuung.setUnitForDisplay(PensumUnits.HOURS);
			BigDecimal pensumInPercent =
				MathUtil.EXACT.divide(
					MathUtil.HUNDRED.multiply(zeitabschnittDTO.getBetreuungspensum()),
					MAX_STUNDEN_PRO_MONAT);
			neueBetreuung.setPensum(pensumInPercent);
		}
		neueBetreuung.setMonatlicheHauptmahlzeiten(zeitabschnittDTO.getAnzahlHauptmahlzeiten());
		neueBetreuung.setMonatlicheNebenmahlzeiten(zeitabschnittDTO.getAnzahlNebenmahlzeiten());
		neueBetreuung.setMonatlicheBetreuungskosten(zeitabschnittDTO.getBetreuungskosten());
		neueBetreuung.getGueltigkeit().setGueltigAb(zeitabschnittDTO.getVon());
		neueBetreuung.getGueltigkeit().setGueltigBis(zeitabschnittDTO.getBis());

		return neueBetreuung;
	}

	@Nonnull
	protected String translate(String key, Locale sprache, Object... args) {
		return ServerMessageUtil.getMessage(key, sprache, args);
	}

	/**
	 * Dieser Methode prueft ob die Zeitabschnitt gleich sind als die von der letzte Gesuch
	 * Wenn gleich dann soll keine MutationMitteilung erstellt werden
	 *
	 * Deswegen sind alle Parametern die nicht vorhanden sind (im MutationMitteilung) ignoriert
	 * So man muss nur die Zeitabschnitt Werten ueberpruefen
	 */
	protected boolean isSame(BetreuungEventDTO betreuungEventDTO, Betreuung betreuung) {
		Set<BetreuungspensumContainer> betreuungspensumContainers = betreuung.getBetreuungspensumContainers();
		List<ZeitabschnittDTO> zeitabschnittDTOS = betreuungEventDTO.getZeitabschnitte();
		if (betreuungspensumContainers.size() != zeitabschnittDTOS.size()) {
			return false;
		}
		for (BetreuungspensumContainer betreuungspensumContainer : betreuungspensumContainers) {
			Betreuungspensum betreuungspensum = betreuungspensumContainer.getBetreuungspensumJA();
			boolean match = false;
			for (ZeitabschnittDTO zeitabschnittDTO : zeitabschnittDTOS) {
				if (zeitabschnittDTO.getVon().isEqual(betreuungspensum.getGueltigkeit().getGueltigAb()) &&
					zeitabschnittDTO.getBis().isEqual(betreuungspensum.getGueltigkeit().getGueltigBis()) &&
					betreuungspensum.getMonatlicheBetreuungskosten().compareTo(zeitabschnittDTO.getBetreuungskosten())
						== 0 &&
					(zeitabschnittDTO.getTarifProHauptmahlzeiten() == null ||
						(zeitabschnittDTO.getTarifProHauptmahlzeiten() != null
							&& betreuungspensum.getTarifProHauptmahlzeit()
							.compareTo(zeitabschnittDTO.getTarifProHauptmahlzeiten())
							== 0))
					&&
					(zeitabschnittDTO.getTarifProNebenmahlzeiten() == null ||
						(zeitabschnittDTO.getTarifProNebenmahlzeiten() != null &&
							betreuungspensum.getTarifProNebenmahlzeit()
								.compareTo(zeitabschnittDTO.getTarifProNebenmahlzeiten())
								== 0))
					&&
					(zeitabschnittDTO.getAnzahlHauptmahlzeiten() == null ||
						(zeitabschnittDTO.getAnzahlHauptmahlzeiten() != null &&
							betreuungspensum.getMonatlicheHauptmahlzeiten()
								.compareTo(zeitabschnittDTO.getAnzahlHauptmahlzeiten())
								== 0))
					&&
					(zeitabschnittDTO.getAnzahlNebenmahlzeiten() == null ||
						(zeitabschnittDTO.getAnzahlNebenmahlzeiten() != null &&
							betreuungspensum.getMonatlicheNebenmahlzeiten()
								.compareTo(zeitabschnittDTO.getAnzahlNebenmahlzeiten())
								== 0))
				) {
					//check pensum:
					if (zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.PERCENTAGE.name())) {
						match = betreuungspensum.getPensum().compareTo(zeitabschnittDTO.getBetreuungspensum()) == 0;
					} else if (zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.DAYS.name())) {
						BigDecimal pensumInPercent =
							MathUtil.EXACT.divide(
								MathUtil.HUNDRED.multiply(zeitabschnittDTO.getBetreuungspensum()),
								MAX_TAGE_PRO_MONAT);
						match = betreuungspensum.getPensum().compareTo(pensumInPercent) == 0;
					} else if (zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.HOURS.name())) {
						BigDecimal pensumInPercent =
							MathUtil.EXACT.divide(
								MathUtil.HUNDRED.multiply(zeitabschnittDTO.getBetreuungspensum()),
								MAX_STUNDEN_PRO_MONAT);
						match = betreuungspensum.getPensum().compareTo(pensumInPercent) == 0;
					}
				}
				if (match) {
					break;
				}
			}
			if (!match) {
				return false;
			}
		}

		return true;
	}

	@Nonnull
	private BetreuungspensumContainer cloneBetreuungspensumContainerJa(
		@Nonnull BetreuungspensumContainer betreuungspensumContainer
	) {
		BetreuungspensumContainer betreuungspensumContainerCloned = new BetreuungspensumContainer();
		betreuungspensumContainerCloned.setBetreuung(betreuungspensumContainer.getBetreuung());
		Betreuungspensum betreuungspensumJaCloned =
			cloneBetreuungspensum(betreuungspensumContainer.getBetreuungspensumJA());
		betreuungspensumContainerCloned.setBetreuungspensumJA(betreuungspensumJaCloned);
		return betreuungspensumContainerCloned;
	}

	@Nonnull
	private Betreuungspensum cloneBetreuungspensum(
		@Nonnull Betreuungspensum betreuungspensum
	) {
		Betreuungspensum betreuungspensumCloned = new Betreuungspensum();
		betreuungspensumCloned.setGueltigkeit(betreuungspensum.getGueltigkeit());
		betreuungspensumCloned.setMonatlicheBetreuungskosten(betreuungspensum.getMonatlicheBetreuungskosten());
		betreuungspensumCloned.setMonatlicheNebenmahlzeiten(betreuungspensum.getMonatlicheNebenmahlzeiten());
		betreuungspensumCloned.setMonatlicheHauptmahlzeiten(betreuungspensum.getMonatlicheHauptmahlzeiten());
		betreuungspensumCloned.setUnitForDisplay(betreuungspensum.getUnitForDisplay());
		betreuungspensumCloned.setPensum(betreuungspensum.getPensum());
		betreuungspensumCloned.setTarifProHauptmahlzeit(betreuungspensum.getTarifProHauptmahlzeit());
		betreuungspensumCloned.setTarifProNebenmahlzeit(betreuungspensum.getTarifProNebenmahlzeit());
		betreuungspensumCloned.setNichtEingetreten(betreuungspensum.getNichtEingetreten());
		return betreuungspensumCloned;
	}

	@Nonnull
	private List<BetreuungspensumContainer> adaptBetreuung(
		@Nonnull Betreuung betreuung,
		@Nonnull DateRange gueltigkeit
	) {
		List<BetreuungspensumContainer> currentBetreuungspensumContainer =
			betreuung.getBetreuungspensumContainers().stream()
				.map(this::cloneBetreuungspensumContainerJa)
				.collect(Collectors.toList());

		//alle betpencont mit gueltigkeit kleiner als schnittstelle oder groesser lassen
		currentBetreuungspensumContainer
			.removeIf(betreuungspensumContainer -> gueltigkeit.contains(betreuungspensumContainer.getBetreuungspensumJA()
				.getGueltigkeit()));

		//adapt Gueltigkeit if needed
		currentBetreuungspensumContainer.stream().forEach(betreuungspensumContainer -> {
			if (betreuungspensumContainer.getBetreuungspensumJA()
				.getGueltigkeit()
				.getGueltigBis()
				.isAfter(gueltigkeit.getGueltigAb())) {
				betreuungspensumContainer.getBetreuungspensumJA()
					.getGueltigkeit()
					.setGueltigBis(gueltigkeit.getGueltigAb().minusDays(1));
			}
		});
		//we add the splitted one if needed
		currentBetreuungspensumContainer.removeIf(betreuungspensumContainer ->
			betreuungspensumContainer.getBetreuungspensumJA().getGueltigkeit().isAfter(
				LocalDate.of(GO_LIVE_YEAR, GO_LIVE_MONTH, GO_LIVE_DAY).minusDays(1)
			)
		);
		return currentBetreuungspensumContainer;
	}

}
