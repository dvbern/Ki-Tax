/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.errors.BenutzerExistException;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.util.CsvCreator;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Interface um gewisse Services als SUPER_ADMIN aufrufen zu koennen
 */
@Stateless
@Local(SuperAdminService.class)
@RunAs(UserRoleName.SUPER_ADMIN)
public class SuperAdminServiceBean implements SuperAdminService {

	private static final Logger LOG = LoggerFactory.getLogger(SuperAdminServiceBean.class.getSimpleName());

	@Inject
	private GesuchService gesuchService;

	@Inject
	private DossierService dossierService;

	@Inject
	private FallService fallService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private MailService mailService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private Persistence persistence;


	@Override
	@RolesAllowed({ GESUCHSTELLER, SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, ADMIN_SOZIALDIENST })
	public void removeGesuch(@Nonnull String gesuchId) {
		gesuchService.removeGesuch(gesuchId, GesuchDeletionCause.USER);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, ADMIN_SOZIALDIENST })
	public void removeDossier(@Nonnull String dossierId) {
		dossierService.removeDossier(dossierId, GesuchDeletionCause.USER);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, ADMIN_SOZIALDIENST })
	public void removeFallIfExists(@Nonnull String fallId) {
		fallService.removeFallIfExists(fallId, GesuchDeletionCause.USER);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, ADMIN_SOZIALDIENST })
	public void removeFall(@Nonnull Fall fall) {
		fallService.removeFall(fall, GesuchDeletionCause.USER);
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory, Benutzer saveAsUser) {
		return gesuchService.updateGesuch(gesuch, saveInStatusHistory, saveAsUser);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void removeFallAndBenutzer(@Nonnull String benutzernameToRemove, @Nonnull Benutzer eingeloggterBenutzer){
		Benutzer benutzer = benutzerService.findBenutzer(benutzernameToRemove, eingeloggterBenutzer.getMandant()).orElseThrow(() -> new EbeguEntityNotFoundException(
			"removeBenutzer",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			benutzernameToRemove));
		try {
			benutzerService.checkBenutzerIsNotGesuchstellerWithFreigegebenemGesuch(benutzer);
			// Keine Exception: Es ist kein Gesuchsteller: Wir können immer löschen
			removeFallAndBenutzerForced(benutzer, eingeloggterBenutzer);
		} catch (BenutzerExistException b) {
			// Es ist ein Gesuchsteller: Wir löschen, solange er keine freigegebenen/verfuegten Gesuche hat
			if (b.getErrorCodeEnum() != ErrorCodeEnum.ERROR_GESUCHSTELLER_EXIST_WITH_FREGEGEBENE_GESUCH) {
				removeFallAndBenutzerForced(benutzer, eingeloggterBenutzer);
			} else {
				throw b;
			}
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void removeFallAndBenutzerForced(@Nonnull Benutzer benutzerToRemove, @Nonnull Benutzer eingeloggterBenutzer) {
		LOG.warn("Der Benutzer mit Benutzername: {} und Rolle {} wird gelöscht durch Benutzer {} mit Rolle {}",
			benutzerToRemove.getUsername(),
			benutzerToRemove.getRole(),
			eingeloggterBenutzer.getUsername(),
			eingeloggterBenutzer.getRole());

		Optional<Fall> fallOpt = fallService.findFallByBesitzer(benutzerToRemove);
		fallOpt.ifPresent(this::removeFall);
		benutzerService.removeBenutzer(benutzerToRemove.getUsername(),
				Objects.requireNonNull(benutzerToRemove.getMandant()));
	}

	@Override
	@Asynchronous
	@TransactionTimeout(value = 360, unit = TimeUnit.MINUTES)
	public void createMutationForEachClosedAntragOfGemeinde(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		Objects.requireNonNull(gemeinde, "Gemeinde muss gesetzt sein");
		Objects.requireNonNull(gesuchsperiode, "Periode muss gesetzt sein");

		try {
			LOG.info("Starting Massenmutation...");
			// Alle Dossiers der gewuenschten Gemeinde untersuchen
			final Collection<Dossier> allDossiers = dossierService.findDossiersByGemeinde(gemeinde);
			CsvCreator csvCreator = new CsvCreator();
			csvCreator.append("Fall", "Resultat", "Detail");
			for (Dossier dossier : allDossiers) {
				final Optional<Gesuch> gesuchOptional = getNeuestesGesuchFuerGpAndWithBetreuungen(dossier, gesuchsperiode);
				if (gesuchOptional.isPresent()) {
					Gesuch neuesterAntrag = gesuchOptional.get();
					if (neuesterAntrag.getStatus().isAnyStatusOfVerfuegt()) {
						try {
							// Eigentliche Mutation, in einer eigenen Transaktion, damit wir mit den anderen Gesuchen
							// im Fehlerfall weitermachen koennen
							gesuchService.createMutationAndAskForPlatzbestaetigung(neuesterAntrag);
							protokolliereResultat(csvCreator, neuesterAntrag, KibonLogLevel.INFO, "Mutation wurde erstellt");
						} catch (Exception e) {
							LOG.error("Massenmutation fehlgeschlagen fuer Antrag {}", neuesterAntrag.getFall().getFallNummer(), e);
							protokolliereResultat(csvCreator, neuesterAntrag, KibonLogLevel.ERROR, "Fehler beim Erstellen der Mutation" + ExceptionUtils.getStackTrace(e));
						}
					} else if (neuesterAntrag.getStatus() == AntragStatus.VERFUEGEN) {
						// Im Status VERFUEGEN koennen theoretisch einige Betreuungen bereits verfuegt sein
						final Optional<Betreuung> verfuegteBetreuungOptional = neuesterAntrag.extractAllBetreuungen().stream()
							.filter(betreuung -> betreuung.getBetreuungsstatus().isAnyStatusOfVerfuegt())
							.findAny();
						if (verfuegteBetreuungOptional.isPresent()) {
							protokolliereResultat(csvCreator, neuesterAntrag, KibonLogLevel.WARN, "Mind. 1 Betreuung schon verfuegt bei Antrag im Status VERFUEGEN");
						} else {
							protokolliereResultat(csvCreator, neuesterAntrag, KibonLogLevel.INFO, "Antrag im Status VERFUEGEN, Betreuungen noch nicht verfuegt");
						}
					} else if (neuesterAntrag.getStatus().isAnyOfInBearbeitungGSOrSZD() && neuesterAntrag.getEingangsart() == Eingangsart.ONLINE) {
						// Falls beim GS: Muessen auf eine Pendenzenliste. Es muss sichergestellt werden, dass die Mutation auch tatsaechlich
						// eingereicht / verfuegt wird.
						protokolliereResultat(csvCreator, neuesterAntrag, KibonLogLevel.WARN, "Mutation bei Gesuchsteller offen");
					} else {
						// alle anderen sind schon / noch offen
						protokolliereResultat(csvCreator, neuesterAntrag, KibonLogLevel.INFO, "Mutation offen bei Gemeinde");
					}
				}
			}
			// Das Resultat ins CSV schreiben.
			final byte[] result = csvCreator.create();
			final UploadFileInfo uploadFileInfo = fileSaverService.save(
				result, "MassenMutationProtokoll.csv", "MassenMutation", new MimeType(MediaType.TEXT_PLAIN));
			final String mailEmpfaenger = ebeguConfiguration.getMassenmutationEmpfaengerMail();
			mailService.sendMessageWithAttachment(
				"Protokoll Massenmutation", new String(result, StandardCharsets.UTF_8), mailEmpfaenger, uploadFileInfo);
			LOG.info("... Massenmutation beendet");
		} catch (IOException | MailException | MimeTypeParseException e) {
			LOG.error("Could not create MassenMutation", e);
		}
	}

	@Nonnull
	private Optional<Gesuch> getNeuestesGesuchFuerGpAndWithBetreuungen(
		@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode
	) {
		final Optional<String> antragIdOptional =
			gesuchService.getIdOfNeuestesGesuchForDossierAndGesuchsperiode(gesuchsperiode, dossier);
		if (antragIdOptional.isPresent()) {
			// Es gibt in der gewuenschten Periode einen Antrag. Wir lesen diesen direkt ueber persistence.find()
			// da der Authorizer hier nicht benoetigt wird.
			final Gesuch neuesterAntrag = persistence.find(Gesuch.class, antragIdOptional.get());
			if (neuesterAntrag == null) {
				throw new EbeguEntityNotFoundException(
					"getNeuestesGesuchFuerGpAndWithBetreuungen", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, antragIdOptional.get());
			}
			if (!neuesterAntrag.extractAllBetreuungen().isEmpty()) {
				// In diesem Antrag gibt es Kita und/oder TFO Angebote
				return Optional.of(neuesterAntrag);
			}
		}
		return Optional.empty();
	}

	private void protokolliereResultat(
		@Nonnull CsvCreator csvCreator,  @Nonnull Gesuch neuesterAntrag, @Nonnull KibonLogLevel result, @Nonnull String details
	) throws IOException {
		final String fallnummer = String.valueOf(neuesterAntrag.getFall().getFallNummer());

		// Die Warnungen und Fehler schreiben wir vorsichtshalber zusaetzlich ins Logfile
		if (result == KibonLogLevel.ERROR) {
			LOG.error("Massenmutation Fall {}: {}", fallnummer, details);
		} else if (result == KibonLogLevel.WARN) {
			LOG.warn("Massenmutation Fall {}: {}", fallnummer, details);
		} else {
			LOG.info("Massenmutation Fall {}: {}", fallnummer, details);
		}

		csvCreator.append(fallnummer, result.name(), details);
	}
}
