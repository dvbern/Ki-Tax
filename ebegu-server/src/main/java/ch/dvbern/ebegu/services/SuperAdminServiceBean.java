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

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.RunAs;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.errors.BenutzerExistException;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@Override
	public void removeGesuch(@Nonnull String gesuchId) {
		gesuchService.removeGesuch(gesuchId, GesuchDeletionCause.USER);
	}

	@Override
	public void removeDossier(@Nonnull String dossierId) {
		dossierService.removeDossier(dossierId, GesuchDeletionCause.USER);
	}

	@Override
	public void removeFallIfExists(@Nonnull String fallId) {
		fallService.removeFallIfExists(fallId, GesuchDeletionCause.USER);
	}

	@Override
	public void removeFall(@Nonnull Fall fall) {
		fallService.removeFall(fall, GesuchDeletionCause.USER);
	}

	@Override
	@Nonnull
	public Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory, Benutzer saveAsUser) {
		return gesuchService.updateGesuch(gesuch, saveInStatusHistory, saveAsUser);
	}

	@Override
	public void removeFallAndBenutzer(@Nonnull String benutzernameToRemove, @Nonnull Benutzer eingeloggterBenutzer){
		Benutzer benutzer = benutzerService.findBenutzer(benutzernameToRemove).orElseThrow(() -> new EbeguEntityNotFoundException(
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

	private void removeFallAndBenutzerForced(@Nonnull Benutzer benutzerToRemove, @Nonnull Benutzer eingeloggterBenutzer) {
		LOG.warn("Der Benutzer mit Benutzername: {} und Rolle {} wird gelöscht durch Benutzer {} mit Rolle {}",
			benutzerToRemove.getUsername(),
			benutzerToRemove.getRole(),
			eingeloggterBenutzer.getUsername(),
			eingeloggterBenutzer.getRole());

		Optional<Fall> fallOpt = fallService.findFallByBesitzer(benutzerToRemove);
		fallOpt.ifPresent(this::removeFall);
		benutzerService.removeBenutzer(benutzerToRemove.getUsername());
	}
}
