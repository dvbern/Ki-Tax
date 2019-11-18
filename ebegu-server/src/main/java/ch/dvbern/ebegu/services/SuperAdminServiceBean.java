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
import javax.annotation.security.RolesAllowed;
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
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
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

	@Override
	@RolesAllowed({ GESUCHSTELLER, SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS })
	public void removeGesuch(@Nonnull String gesuchId) {
		gesuchService.removeGesuch(gesuchId, GesuchDeletionCause.USER);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS })
	public void removeDossier(@Nonnull String dossierId) {
		dossierService.removeDossier(dossierId, GesuchDeletionCause.USER);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS })
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
	public void removeFallAndBenutzer(@Nonnull String benutzername){
		LOG.info("Der Benutzer mit Benutzername: " + benutzername + " wird gelöscht");
		Benutzer benutzer = benutzerService.findBenutzer(benutzername).orElseThrow(() -> new EbeguEntityNotFoundException(
			"removeBenutzer",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			benutzername));

		Optional<Fall> fallOpt = fallService.findFallByBesitzer(benutzer);
		if(fallOpt.isPresent()){
			this.removeFall(fallOpt.get());
		}
		benutzerService.removeBenutzer(benutzername);
	}
}
