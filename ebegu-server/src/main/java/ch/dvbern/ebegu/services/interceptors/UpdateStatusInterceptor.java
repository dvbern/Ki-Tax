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

package ch.dvbern.ebegu.services.interceptors;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRole.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRole.SUPER_ADMIN;

/**
 * UpdateStatusInterceptor:
 * - Fuer JA muessen wir den Status des Gesuchs von Freigegeben auf {@link AntragStatus.IN_BEARBEITUNG_JA}
 * setzen wenn das Jugendamt etwas an einem {@link AntragStatus#FREIGEGEBEN} Gesuch aendert.
 * - Fuer STV wenn die STV ein Gesuch oeffnet muss dieses Gesuch vom Status PRUEFUNG_STV auf Status
 * IN_BEARBEITUNG_STV wechseln
 */
public class UpdateStatusInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(UpdateStatusInterceptor.class.getSimpleName());

	private static final UserRole[] JA_OR_SCH_OR_ADM = { SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, SACHBEARBEITER_TS, ADMIN_TS };

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Persistence persistence;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private EbeguConfiguration configuration;

	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	@AroundInvoke
	public Object maybeChangeGesuchstatusToInBearbeitung(InvocationContext ctx) throws Exception {

		if (ctx.getParameters() != null && ctx.getParameters().length != 0) {
			String gesuchID = ctx.getParameters()[0] instanceof String ? (String) ctx.getParameters()[0] : null;
			if (gesuchID != null) {
				Gesuch gesuch = persistence.find(Gesuch.class, gesuchID);
				if (gesuch == null) {
					LOG.info("Gesuch mit ID " + gesuchID + " wurde nicht in der DB gefunden");
				} else {
					if (principalBean.isCallerInAnyOfRole(JA_OR_SCH_OR_ADM) && Eingangsart.ONLINE == gesuch.getEingangsart()
						&& AntragStatus.FREIGEGEBEN == gesuch.getStatus()) {
						changeGesuchStatus(gesuch, AntragStatus.IN_BEARBEITUNG_JA);
					} else if (principalBean.isCallerInRole(STEUERAMT) && AntragStatus.PRUEFUNG_STV == gesuch.getStatus()) {
						changeGesuchStatus(gesuch, AntragStatus.IN_BEARBEITUNG_STV);
					}
				}
			}
		}
		return ctx.proceed();
	}

	private void changeGesuchStatus(Gesuch gesuch, AntragStatus newStatus) {
		gesuch.setStatus(newStatus);
		gesuchService.updateGesuch(gesuch, true, null);

		if (configuration.getIsDevmode() || LOG.isDebugEnabled()) {
			LOG.info("Antrag wurde in den Status " + newStatus + " gesetzt. ID " + gesuch.getId());
		}
	}

}
