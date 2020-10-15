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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
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

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull BetreuungEventDTO dto) {
		String refnr = dto.getRefnr();
		try {
			Optional<Betreuung> betreuungOpt = betreuungService.findBetreuungByBGNummer(refnr, false);
			if (!betreuungOpt.isPresent()) {
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: {} existiert nicht!", refnr);
				return;
			}
			Betreuung betreuung = betreuungOpt.get();
			if (betreuung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
				LOG.warn("Platzbestaetigung: die Gesuchsperiode fuer die Betreuung mit RefNr: {} ist nicht aktiv!", refnr);
				return;
			}
			if (betreuung.getTimestampMutiert() != null && betreuung.getTimestampMutiert().isAfter(eventTime)) {
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: {} war spaeter als dieser "
					+ "Event im kiBon bearbeitet! Event ist ignoriert", refnr);
			} else if (betreuung.getBetreuungsstatus().equals(Betreuungsstatus.WARTEN)) {
				//Update the Betreuung and check if all data are available
				if (setBetreuungDaten(new PlatzbestaetigungProcessingContext(betreuung, dto))) {
					betreuungService.betreuungPlatzBestaetigen(betreuung);
					LOG.info("Platzbestaetigung: Betreuung mit RefNr: {} automatisch bestätigt", refnr);
				} else {
					betreuungService.saveBetreuung(betreuung, false);
					LOG.info("Platzbestaetigung: Betreuung mit RefNr: {} eingelesen, aber nicht automatisch bestätigt", refnr);
				}
			} else if (betreuung.getBetreuungsstatus().equals(Betreuungsstatus.VERFUEGT)
				|| betreuung.getBetreuungsstatus().equals(Betreuungsstatus.BESTAETIGT)) {
				if (isSame(dto, betreuung)) {
					LOG.warn("Platzbestaetigung: die Betreuung ist identisch wie der Event mit RefNr: {}" +
						" - MutationMitteilung wird nicht erstellt!", refnr);
					return;
				}
				//MutationMitteilungErstellen
				//we map all the data we know in a mitteilung object, we are only interested into Zeitabschnitt:
				Betreuungsmitteilung betreuungsmitteilung = this.setBetreuungsmitteilungDaten(dto, betreuung);
				if (betreuungsmitteilung != null) {
					// we first clear all the Mutationsmeldungen for the current Betreuung
					mitteilungService.removeOffeneBetreuungsmitteilungenForBetreuung(betreuung);
					// and then send the new Betreuungsmitteilung an die Gemeinde
					this.mitteilungService.sendBetreuungsmitteilung(betreuungsmitteilung);
					LOG.info("Mutationsmeldung erstellt für die Betreuung mit RefNr: {}", refnr);
				}
			} else {
				LOG.warn("Platzbestaetigung: die Betreuung mit RefNr: {} hat einen ungültigen Status: {}" , refnr, betreuung.getBetreuungsstatus());
			}
		} catch (Exception e) {
			LOG.error("Error while processing the record: {} error: {}", refnr, e.getMessage());
		}
	}

	/**
	 * Update the Betreuung Object and return if its ready for Bestaetigen
	 */
	private boolean setBetreuungDaten(@Nonnull PlatzbestaetigungProcessingContext ctx) {
		setErweitereBeduerfnisseBestaetigt(ctx);
		setBetreuungInGemeinde(ctx);
		setZeitabschnitte(ctx);

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

	private void setZeitabschnitte(@Nonnull PlatzbestaetigungProcessingContext ctx) {
		List<ZeitabschnittDTO> zeitabschnitte = ctx.getDto().getZeitabschnitte();
		if (zeitabschnitte.isEmpty()) {
			ctx.requireHumanConfirmation();

			return;
		}

		Betreuung betreuung = ctx.getBetreuung();
		betreuung.getBetreuungspensumContainers().clear();
		//Check if die Gemeinde erlaubt mahlzeitvergunstigung:
		boolean mahlzeitVergunstigungEnabled =
			isEnabled(betreuung, EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED);

		zeitabschnitte.stream()
			.map(z -> zeitabschnittToBetreuungspensumContainer(mahlzeitVergunstigungEnabled, ctx, z))
			.filter(Objects::nonNull)
			.forEach(c -> betreuung.getBetreuungspensumContainers().add(c));
	}

	@Nullable
	private BetreuungspensumContainer zeitabschnittToBetreuungspensumContainer(
		boolean mahlzeitVergunstigungEnabled,
		@Nonnull PlatzbestaetigungProcessingContext ctx,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO) {

		Betreuung betreuung = ctx.getBetreuung();
		Betreuungspensum betreuungspensum = mapZeitabschnitt(new Betreuungspensum(), zeitabschnittDTO, betreuung);
		if (betreuungspensum == null) {
			ctx.requireHumanConfirmation();
			return null;
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
		BetreuungEventDTO dto,
		Betreuung betreuung) {
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
			benutzerService.findBenutzerById(TECHNICAL_BENUTZER_ID).orElseThrow(() -> new EbeguEntityNotFoundException("", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, TECHNICAL_BENUTZER_ID));
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
			einstellungService.findEinstellung(EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
				gemeinde,
				gesuchsperiode);
		String message = "";
		int counter = 1;
		boolean areZeitabschnittCorrupted = false;
		for (ZeitabschnittDTO zeitabschnittDTO : dto.getZeitabschnitte()) {
			BetreuungsmitteilungPensum betreuungsmitteilungPensum = mapZeitabschnitt(new BetreuungsmitteilungPensum()
				, zeitabschnittDTO, betreuung);
			if (betreuungsmitteilungPensum == null) {
				areZeitabschnittCorrupted = true;
				continue;
			}
			betreuungsmitteilungPensum.setVollstaendig(!areZeitabschnittCorrupted);

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
				message = message + translate(MESSAGE_MAHLZEIT_KEY, sprache, counter,
					betreuungsmitteilungPensum.getGueltigkeit().getGueltigAb(),
					betreuungsmitteilungPensum.getGueltigkeit().getGueltigBis(),
					betreuungsmitteilungPensum.getPensum(),
					betreuungsmitteilungPensum.getMonatlicheBetreuungskosten(),
					betreuungsmitteilungPensum.getMonatlicheHauptmahlzeiten(),
					betreuungsmitteilungPensum.getTarifProHauptmahlzeit(),
					betreuungsmitteilungPensum.getMonatlicheNebenmahlzeiten(),
					betreuungsmitteilungPensum.getTarifProNebenmahlzeit());
			} else {
				message = message + translate(MESSAGE_KEY, sprache, counter,
					betreuungsmitteilungPensum.getGueltigkeit().getGueltigAb(),
					betreuungsmitteilungPensum.getGueltigkeit().getGueltigBis(),
					betreuungsmitteilungPensum.getPensum(),
					betreuungsmitteilungPensum.getMonatlicheBetreuungskosten());
			}
			//set betreuungsmitteilungPensum in model
			betreuungsmitteilungPensum.setBetreuungsmitteilung(betreuungsmitteilung);
			betreuungsmitteilung.getBetreuungspensen().add(betreuungsmitteilungPensum);
		}
		betreuungsmitteilung.setMessage(message);
		return betreuungsmitteilung;
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
				MathUtil.EXACT.divide(MathUtil.HUNDRED.multiply(zeitabschnittDTO.getBetreuungspensum()),
					MAX_TAGE_PRO_MONAT);
			neueBetreuung.setPensum(pensumInPercent);
		} else if (betreuung.isAngebotTagesfamilien()) {
			if (!zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.HOURS.name())) {
				LOG.error("Pensum is not supported, Zeitabschnitt ist corrupted");
				return null;
			}
			neueBetreuung.setUnitForDisplay(PensumUnits.HOURS);
			BigDecimal pensumInPercent =
				MathUtil.EXACT.divide(MathUtil.HUNDRED.multiply(zeitabschnittDTO.getBetreuungspensum()),
					MAX_STUNDEN_PRO_MONAT);
			neueBetreuung.setPensum(pensumInPercent);
		}
		neueBetreuung.setMonatlicheHauptmahlzeiten(zeitabschnittDTO.getAnzahlMonatlicheHauptmahlzeiten());
		neueBetreuung.setMonatlicheNebenmahlzeiten(zeitabschnittDTO.getAnzahlMonatlicheNebenmahlzeiten());
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
					betreuungspensum.getMonatlicheBetreuungskosten().compareTo(zeitabschnittDTO.getBetreuungskosten()) == 0 &&
					(zeitabschnittDTO.getTarifProHauptmahlzeiten() == null ||
						(zeitabschnittDTO.getTarifProHauptmahlzeiten() != null && betreuungspensum.getTarifProHauptmahlzeit().compareTo(zeitabschnittDTO.getTarifProHauptmahlzeiten())
							== 0))
					&&
					(zeitabschnittDTO.getTarifProNebenmahlzeiten() == null ||
						(zeitabschnittDTO.getTarifProNebenmahlzeiten() != null &&
							betreuungspensum.getTarifProNebenmahlzeit().compareTo(zeitabschnittDTO.getTarifProNebenmahlzeiten())
								== 0))
					&&
					betreuungspensum.getMonatlicheHauptmahlzeiten() == zeitabschnittDTO.getAnzahlMonatlicheHauptmahlzeiten() &&
					betreuungspensum.getMonatlicheNebenmahlzeiten() == zeitabschnittDTO.getAnzahlMonatlicheNebenmahlzeiten()
				) {
					//check pensum:
					if (zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.PERCENTAGE.name())) {
						match = betreuungspensum.getPensum().compareTo(zeitabschnittDTO.getBetreuungspensum()) == 0;
					} else if (zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.DAYS.name())) {
						BigDecimal pensumInPercent =
							MathUtil.EXACT.divide(MathUtil.HUNDRED.multiply(zeitabschnittDTO.getBetreuungspensum()),
								MAX_TAGE_PRO_MONAT);
						match = betreuungspensum.getPensum().compareTo(pensumInPercent) == 0;
					} else if (zeitabschnittDTO.getPensumUnit().name().equals(PensumUnits.HOURS.name())) {
						BigDecimal pensumInPercent =
							MathUtil.EXACT.divide(MathUtil.HUNDRED.multiply(zeitabschnittDTO.getBetreuungspensum()),
								MAX_STUNDEN_PRO_MONAT);
						match = betreuungspensum.getPensum().compareTo(pensumInPercent) == 0;
					}
				}
				if(match){
					break;
				}
			}
			if (!match) {
				return false;
			}
		}

		return true;
	}
}
