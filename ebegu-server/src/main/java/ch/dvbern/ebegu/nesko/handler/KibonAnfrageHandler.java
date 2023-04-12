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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.KiBonAnfrageServiceException;
import ch.dvbern.ebegu.services.KibonAnfrageService;

@Stateless
public class KibonAnfrageHandler {

	@Inject
	private KibonAnfrageService kibonAnfrageService;

	@Inject
	private PrincipalBean principalBean;

	public KibonAnfrageContext requestSteuerdaten(
		@Nonnull Gesuch gesuch,
		int zpvNummer,
		@Nonnull GesuchstellerTyp gesuchstellerTyp) {

		KibonAnfrageContext kibonAnfrageContext = new KibonAnfrageContext(gesuch, zpvNummer, gesuchstellerTyp);

		try {
			getSteuerdatenAndHandleResponse(kibonAnfrageContext);
		} catch (KiBonAnfrageServiceException e) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
		}

		return kibonAnfrageContext;
	}

	public KibonAnfrageContext handleKibonAnfrage(Gesuch gesuch, boolean isGemsinam, GesuchstellerTyp gesuchstellerTyp) {
		String zpvBesitzer = findZpvNummerFromGesuchBesitzer(gesuch);
		KibonAnfrageContext kibonAnfrageContext = new KibonAnfrageContext(
				gesuch,
				isGemsinam,
				gesuchstellerTyp,
				zpvBesitzer);

		int gesuchstellerNumber = gesuchstellerTyp.getGesuchstellerNummer();
		boolean hasTwoAntragStellende = kibonAnfrageContext.getGesuch().getGesuchsteller2() != null;

		if (hasTwoAntragStellende && kibonAnfrageContext.isGemeinsam()) {
			try {
				return getKibonAnfrageContextWithSteuerdaten(
						Objects.requireNonNull(kibonAnfrageContext.getGesuch().getGesuchsteller1()),
						kibonAnfrageContext,
						gesuchstellerNumber);

			} catch (KiBonAnfrageServiceException e) {
				Objects.requireNonNull(kibonAnfrageContext.getGesuch().getFamiliensituationContainer());
				Objects.requireNonNull(kibonAnfrageContext.getGesuch()
											   .getFamiliensituationContainer()
											   .getFamiliensituationJA());
				if (kibonAnfrageContext.getGesuch()
							.getFamiliensituationContainer()
							.getFamiliensituationJA()
							.getFamilienstatus()
							.equals(
									EnumFamilienstatus.VERHEIRATET)) {
					try {
						return getKibonAnfrageContextWithSteuerdaten(
								kibonAnfrageContext.getGesuch().getGesuchsteller2(),
								kibonAnfrageContext,
								gesuchstellerNumber);
					} catch (KiBonAnfrageServiceException e2) {
						kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
						return kibonAnfrageContext;
					}
				}
				kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
				return kibonAnfrageContext;
			}
		} else {
			// anfrage single GS
			if (gesuchstellerNumber == 1) {
				try {
					return getKibonAnfrageContextWithSteuerdaten(
							Objects.requireNonNull(kibonAnfrageContext.getGesuch().getGesuchsteller1()),
							kibonAnfrageContext,
							gesuchstellerNumber);
				} catch (KiBonAnfrageServiceException e) {
					kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
				}
			}
			// anfrage single GS
			if (gesuchstellerNumber == 2) {
				try {
					return getKibonAnfrageContextWithSteuerdaten(
							kibonAnfrageContext.getGesuch().getGesuchsteller2(),
							kibonAnfrageContext,
							gesuchstellerNumber);
				} catch (KiBonAnfrageServiceException e) {
					kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
					return kibonAnfrageContext;
				}
			}
		}
		return kibonAnfrageContext;
	}

	private KibonAnfrageContext getKibonAnfrageContextWithSteuerdaten(
			GesuchstellerContainer gesuchstellerContainer,
			KibonAnfrageContext kibonAnfrageContext,
			int gesuchstellerNumber) throws KiBonAnfrageServiceException {
		if (kibonAnfrageContext.getZpvNummerForRequest().isEmpty()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatusFailedNoZPV();
			return kibonAnfrageContext;
		}

		SteuerdatenResponse steuerdatenResponseGS = kibonAnfrageService.getSteuerDaten(
				kibonAnfrageContext.getZpvNummerForRequest().get(),
				gesuchstellerContainer.getGesuchstellerJA().getGeburtsdatum(),
				kibonAnfrageContext.getGesuch().getId(),
				kibonAnfrageContext.getGesuch().getGesuchsperiode().getBasisJahrPlus1());
		kibonAnfrageContext.setSteuerdatenResponse(steuerdatenResponseGS);
		if (kibonAnfrageContext.isGemeinsam()) {
			KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(
					kibonAnfrageContext,
					steuerdatenResponseGS);
			return kibonAnfrageContext;
		}
		KibonAnfrageHelper.handleSteuerdatenResponse(
				kibonAnfrageContext,
				steuerdatenResponseGS,
				gesuchstellerNumber);
		return kibonAnfrageContext;
	}

	private void getSteuerdatenAndHandleResponse(KibonAnfrageContext kibonAnfrageContext)
			throws KiBonAnfrageServiceException {

		if (kibonAnfrageContext.getZpvNummerForRequest().isEmpty()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatusFailedNoZPV();
			return;
		}

		SteuerdatenResponse steuerdatenResponseGS = kibonAnfrageService.getSteuerDaten(
				kibonAnfrageContext.getZpvNummerForRequest().get(),
				kibonAnfrageContext.getGeburstdatumForRequest(),
				kibonAnfrageContext.getGesuch().getId(),
				kibonAnfrageContext.getGesuch().getGesuchsperiode().getBasisJahrPlus1());

		kibonAnfrageContext.setSteuerdatenResponse(steuerdatenResponseGS);
		if (kibonAnfrageContext.isGemeinsam()) {
			KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(
				kibonAnfrageContext,
				steuerdatenResponseGS);
			return;
		}
		KibonAnfrageHelper.handleSteuerdatenResponse(
				kibonAnfrageContext,
				steuerdatenResponseGS,
				kibonAnfrageContext.getGesuchstellernTyp().getGesuchstellerNummer());
	}

	@Nullable
	private String findZpvNummerFromGesuchBesitzer(Gesuch gesuch) {
		if (principalBean.isCallerInAnyOfRole(UserRole.getSuperadminAllGemeindeRoles())
				|| principalBean.isAnonymousSuperadmin()) {
			//Online Fall hat immer ein Besitzer
			Objects.requireNonNull(gesuch.getFall().getBesitzer());
			return gesuch.getFall().getBesitzer().getZpvNummer();
		}

		//wenn user role nicht gemeinde, dann soll nur der aktuelle benutzer die steuerdaten abfragen können
		return principalBean.getBenutzer().getZpvNummer();
	}

}
