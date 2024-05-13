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

package ch.dvbern.ebegu.services;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.JaxFreigabeDTO;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.Eingangsart;

@Stateless
public class VerantwortlicheService {

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private DossierService dossierService;

	@Inject
	private GemeindeService gemeindeService;

	/**
	 * Verantwortliche müssen gesetzt werden wenn in einem Papiergesuch oder Papiermutation eine Betreuung hinzugefügt
	 * wird oder eine Online-Mutation freigegeben wird (direkte Freigabe). Beim Einlesen eines Papiergesuchs werden
	 * die Veratnwortliche mittels Dialogfenster durch den Benutzer gesetzt
	 *
	 * @param persist speichert die Verantwortliche direkt auf der DB in Update-Query
	 */
	public void setVerantwortliche(
		@Nullable Benutzer verantwortlicherBG,
		@Nullable Benutzer verantwortlicherTS,
		@Nonnull Gesuch gesuch,
		boolean onlyIfNotSet,
		boolean persist) {

		if (verantwortlicherBG != null && gesuch.hasBetreuungOfJugendamt()) {
			setVerantwortlicherIfNecessaryBG(verantwortlicherBG, gesuch, onlyIfNotSet, persist);
		}
		if (verantwortlicherTS != null && gesuch.hasBetreuungOfSchulamt()) {
			setVerantwortlicherIfNecessaryTS(verantwortlicherTS, gesuch, onlyIfNotSet, persist);
		}
	}

	private void setVerantwortlicherIfNecessaryBG(
		Benutzer user,
		Gesuch gesuch,
		boolean onlyIfNotSet,
		boolean persist) {

		if (user.getRole().isRoleGemeindeOrBG() && (gesuch.getDossier().getVerantwortlicherBG() == null
			|| !onlyIfNotSet)) {
			if (persist) {
				dossierService.setVerantwortlicherBG(gesuch.getDossier().getId(), user);
			}
			gesuch.getDossier().setVerantwortlicherBG(user);
		}
	}

	private void setVerantwortlicherIfNecessaryTS(
		Benutzer user,
		Gesuch gesuch,
		boolean onlyIfNotSet,
		boolean persist) {

		if (user.getRole().isRoleGemeindeOrTS() && (gesuch.getDossier().getVerantwortlicherTS() == null
			|| !onlyIfNotSet)) {
			if (persist) {
				dossierService.setVerantwortlicherTS(gesuch.getDossier().getId(), user);
			}
			gesuch.getDossier().setVerantwortlicherTS(user);
		}
	}


	public void updateVerantwortliche(
		@Nonnull Gesuch mergedGesuch, @Nonnull AbstractPlatz mergedBetreuung,
		boolean isAnmeldungSchulamtAusgeloest, boolean isNew) {
		if (updateVerantwortlicheNeeded(mergedGesuch.getEingangsart(), isAnmeldungSchulamtAusgeloest, isNew)) {
			Optional<GemeindeStammdaten> gemeindeStammdatenOptional =
				gemeindeService.getGemeindeStammdatenByGemeindeId(mergedGesuch
					.getDossier()
					.getGemeinde()
					.getId());

			Benutzer benutzerBG = null;
			Benutzer benutzerTS = null;
			if (gemeindeStammdatenOptional.isPresent()) {
				GemeindeStammdaten gemeindeStammdaten = gemeindeStammdatenOptional.get();
				benutzerBG = gemeindeStammdaten.getDefaultBenutzerWithRoleBG().orElse(null);
				benutzerTS = gemeindeStammdaten.getDefaultBenutzerWithRoleTS().orElse(null);
			}
			setVerantwortliche(benutzerBG, benutzerTS, mergedBetreuung.extractGesuch(), true, true);
		}
	}

	protected boolean updateVerantwortlicheNeeded(
		Eingangsart eingangsart,
		boolean isSchulamtAnmeldungAusgeloest,
		boolean isNew) {
		if (!isNew) {
			// nur neue Betreuungen duerfen den Verantwortlichen setzen
			return false;
		}
		return eingangsart == Eingangsart.PAPIER || isSchulamtAnmeldungAusgeloest;
	}

	public void updateVerantwortliche(String gesuchId, JaxFreigabeDTO freigabeDTO, Gesuch gesuch) {
		if (!gesuch.isMutation()) {
			// in case of erstgesuch: Verantwortliche werden beim einlesen gesetzt und kommen vom client
			setVerantwortliche(freigabeDTO.getUsernameJA(), freigabeDTO.getUsernameSCH(), gesuch);
		} else {
			// in case of mutation, we take default Verantwortliche and set them only if not set...
			Optional<GemeindeStammdaten> gemeindeStammdatenOptional =
				gemeindeService.getGemeindeStammdatenByGemeindeId(gesuchId);

			Benutzer benutzerBG = null;
			Benutzer benutzerTS = null;
			if (gemeindeStammdatenOptional.isPresent()) {
				GemeindeStammdaten gemeindeStammdaten = gemeindeStammdatenOptional.get();
				benutzerBG = gemeindeStammdaten.getDefaultBenutzerWithRoleBG().orElse(null);
				benutzerTS = gemeindeStammdaten.getDefaultBenutzerWithRoleTS().orElse(null);
			}
			setVerantwortliche(benutzerBG, benutzerTS, gesuch, true, false);
		}
	}

	private void setVerantwortliche(
		@Nullable String usernameBG,
		@Nullable String usernameTS,
		@Nonnull Gesuch gesuch) {

		Benutzer verantwortlicherBG = null;
		Benutzer verantwortlicherTS = null;

		if (usernameBG != null) {
			verantwortlicherBG = benutzerService.findBenutzer(usernameBG, gesuch.extractGemeinde().getMandant()).orElse(null);
		}
		if (usernameTS != null) {
			verantwortlicherTS = benutzerService.findBenutzer(usernameTS, gesuch.extractGemeinde().getMandant()).orElse(null);
		}

		setVerantwortliche(verantwortlicherBG, verantwortlicherTS, gesuch, false, false);
	}

}
