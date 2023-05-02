/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.nesko.handler;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.KiBonAnfrageServiceException;
import ch.dvbern.ebegu.nesko.utils.KibonAnfrageUtil;
import ch.dvbern.ebegu.services.KibonAnfrageService;

@Stateless
public class KibonAnfrageHandler {

	@Inject
	private KibonAnfrageService kibonAnfrageService;

	@Inject
	private PrincipalBean principalBean;

	public KibonAnfrageContext handleKibonAnfrage(Gesuch gesuch, GesuchstellerTyp gesuchstellerTyp) {
		String zpvBesitzer = findZpvNummerFromGesuchBesitzer(gesuch);
		KibonAnfrageContext kibonAnfrageContext = new KibonAnfrageContext(gesuch, gesuchstellerTyp, zpvBesitzer);

		try {
			getSteuerdatenAndHandleResponse(kibonAnfrageContext);
		} catch (KiBonAnfrageServiceException e) {
			if (kibonAnfrageContext.isGemeinsam()) {
				return retryWithOtherGesuchstellersGeburtsdatum(kibonAnfrageContext);
			}
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
		}
		return kibonAnfrageContext;
	}

	private KibonAnfrageContext retryWithOtherGesuchstellersGeburtsdatum(KibonAnfrageContext kibonAnfrageContext) {
		kibonAnfrageContext.useGeburtrsdatumFromOtherGesuchsteller();

		try {
			getSteuerdatenAndHandleResponse(kibonAnfrageContext);
		} catch (KiBonAnfrageServiceException ex) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
		}

		return kibonAnfrageContext;
	}

	private void getSteuerdatenAndHandleResponse(KibonAnfrageContext kibonAnfrageContext)
			throws KiBonAnfrageServiceException {

		kibonAnfrageContext.setSteuerdatenAbfrageTimestampNow();

		if (kibonAnfrageContext.getZpvNummerForRequest().isEmpty()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatusFailedNoZPV();
			return;
		}

		if (kibonAnfrageContext.getGeburstdatumForRequest().isEmpty()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM);
			return;
		}

		SteuerdatenResponse steuerdatenResponseGS = kibonAnfrageService.getSteuerDaten(
				kibonAnfrageContext.getZpvNummerForRequest().get(),
				kibonAnfrageContext.getGeburstdatumForRequest().get(),
				kibonAnfrageContext.getGesuch().getId(),
				kibonAnfrageContext.getGesuch().getGesuchsperiode().getBasisJahrPlus1());

		kibonAnfrageContext.setSteuerdatenResponse(steuerdatenResponseGS);
		if (kibonAnfrageContext.isGemeinsam()) {
			KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(
				kibonAnfrageContext,
				steuerdatenResponseGS);
			return;
		}
		KibonAnfrageHelper.handleSteuerdatenResponse(kibonAnfrageContext, steuerdatenResponseGS);
	}

	@Nullable
	private String findZpvNummerFromGesuchBesitzer(Gesuch gesuch) {
		if (principalBean.isCallerInAnyOfRole(UserRole.getSuperadminAllGemeindeRoles())
				|| principalBean.isAnonymousSuperadmin()) {
			return KibonAnfrageUtil.getZpvFromBesitzer(gesuch);
		}

		//wenn user role nicht gemeinde, dann soll nur der aktuelle benutzer die steuerdaten abfragen können
		return principalBean.getBenutzer().getZpvNummer();
	}

}
