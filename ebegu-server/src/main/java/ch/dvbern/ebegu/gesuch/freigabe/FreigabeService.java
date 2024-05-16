/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.gesuch.freigabe;

import java.time.LocalDate;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.JaxFreigabeDTO;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AnmeldungMutationZustand;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.AntragStatusHistoryService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.VerantwortlicheService;
import ch.dvbern.ebegu.services.WizardStepService;
import ch.dvbern.ebegu.util.FreigabeCopyUtil;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
public class FreigabeService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private AntragStatusHistoryService antragStatusHistoryService;

	@Inject
	private VerantwortlicheService verantwortlicheService;

	@Inject
	private GesuchValidatorService gesuchValidationService;

	@Inject
	private EinstellungService einstellungService;

	@Nonnull
	public Gesuch antragFreigeben(@Nonnull String gesuchId, @Nonnull JaxFreigabeDTO freigabeDTO) {
		var method = "antragFreigeben";
		Gesuch gesuch = Optional.ofNullable(persistence.find(Gesuch.class, gesuchId))
			.orElseThrow(() -> new EbeguEntityNotFoundException(method, gesuchId));

		if (isInvalidOnlineFreigabe(gesuch, freigabeDTO)) {
			throw new EbeguRuntimeException(method, "Onlinefreigabe ohne Bestätigung");
		}

		gesuchValidationService.validateGesuchComplete(gesuch);

		if (gesuch.getStatus() != AntragStatus.FREIGABEQUITTUNG && gesuch.getStatus() != AntragStatus
			.IN_BEARBEITUNG_GS && gesuch.getStatus() != AntragStatus.IN_BEARBEITUNG_SOZIALDIENST) {
			throw new EbeguRuntimeException(
				method,
				"Gesuch war im falschen Status: "
					+ gesuch.getStatus()
					+ " wir erwarten aber nur Freigabequittung oder In Bearbeitung GS",
				"Das Gesuch wurde bereits freigegeben");
		}

		this.authorizer.checkWriteAuthorization(gesuch);

		// Die Daten des GS in die entsprechenden Containers kopieren
		FreigabeCopyUtil.copyForFreigabe(gesuch);
		// Je nach Status
		if (gesuch.getStatus() != AntragStatus.FREIGABEQUITTUNG) {
			// Es handelt sich um eine Mutation ohne Freigabequittung: Wir setzen das Tagesdatum als FreigabeDatum
			// an dem es der Gesuchsteller einreicht
			gesuch.setFreigabeDatum(LocalDate.now());
		}

		schulAmtAnmeldungenAusloesen(gesuch);

		// Den Gesuchsstatus auf Freigegeben setzen (auch bei reinen Schulamt-Gesuchen)
		gesuch.setStatus(AntragStatus.FREIGEGEBEN);

		// Step Freigabe gruen
		wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.FREIGABE);

		//VERANTWORTLICHE
		verantwortlicheService.updateVerantwortliche(gesuchId, freigabeDTO, gesuch);

		// Falls es ein OnlineGesuch war: Das Eingangsdatum setzen
		if (Eingangsart.ONLINE == gesuch.getEingangsart()) {
			gesuch.setEingangsdatum(LocalDate.now());
		}

		final Gesuch merged = persistence.merge(gesuch);
		antragStatusHistoryService.saveStatusChange(merged, null);
		//Bei Freigabe muessen die Anmeldung an der Exchange Service exportiert werden
		merged.extractAllAnmeldungenTagesschule()
			.forEach(anmeldungTagesschule -> betreuungService.fireAnmeldungTagesschuleAddedEvent(anmeldungTagesschule));

		return merged;

	}

	private boolean isInvalidOnlineFreigabe(@Nonnull Gesuch gesuch, @Nonnull JaxFreigabeDTO freigabeDTO) {
		var onlineFreigabeAktiv =
			getOnlineFreigabeEinstellung(gesuch);

		var userConfirmedCorrectness = freigabeDTO.getUserConfirmedCorrectness();
		return onlineFreigabeAktiv && (Boolean.FALSE.equals(userConfirmedCorrectness) || userConfirmedCorrectness == null);
	}

	private boolean getOnlineFreigabeEinstellung(Gesuch gesuch) {
		var onlineFreigabeAktiv =
			einstellungService.getEinstellungByMandant(EinstellungKey.GESUCHFREIGABE_ONLINE, gesuch.getGesuchsperiode());
		return onlineFreigabeAktiv.isPresent() && Boolean.TRUE.equals(onlineFreigabeAktiv.get().getValueAsBoolean());
	}

	private void schulAmtAnmeldungenAusloesen(Gesuch gesuch) {
		// Eventuelle Schulamt-Anmeldungen auf AUSGELOEST setzen
		for (AbstractAnmeldung anmeldung : gesuch.extractAllAnmeldungen()) {
			if (anmeldung.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST) {
				anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
			}
			// Set noch nicht freigegebene Betreuungen to aktuelle Anmeldung bei Freigabe
			if (anmeldung.getAnmeldungMutationZustand() == AnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN) {
				anmeldung.setAnmeldungMutationZustand(AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
				anmeldung.setGueltig(true);
				if (anmeldung.getVorgaengerId() != null) {
					final Optional<? extends AbstractAnmeldung> anmeldungOptional =
						betreuungService.findAnmeldung(anmeldung.getVorgaengerId());
					anmeldungOptional.ifPresent(abstractAnmeldung -> {
						abstractAnmeldung.setAnmeldungMutationZustand(AnmeldungMutationZustand.MUTIERT);
						betreuungService.updateGueltigFlagOnPlatzAndVorgaenger(anmeldung);
					});
				}
			}
		}
	}

}
