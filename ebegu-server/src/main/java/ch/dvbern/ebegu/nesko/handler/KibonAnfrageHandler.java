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

import java.util.HashMap;
import java.util.Map;
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

	private enum ZpvEnum {
		ZPV_BESITZER,
		ZPV_GESUCHSTELLER_1,
		ZPV_GESUCHSTELLER_2
	}

	@Inject
	private KibonAnfrageService kibonAnfrageService;

	@Inject
	private PrincipalBean principalBean;

	public KibonAnfrageContext requestSteuerdaten(
		@Nonnull Gesuch gesuch,
		int zpvNummer,
		@Nonnull GesuchstellerTyp gesuchstellerTyp) {

		KibonAnfrageContext kibonAnfrageContext = new KibonAnfrageContext(gesuch, zpvNummer);
		return handleKibonAnfrage(kibonAnfrageContext, gesuchstellerTyp.getGesuchstellerNummer());
	}

	public KibonAnfrageContext handleKibonAnfrage(@Nonnull KibonAnfrageContext kibonAnfrageContext, int gesuchstellerNumber) {
		boolean hasTwoAntragStellende = kibonAnfrageContext.getGesuch().getGesuchsteller2() != null;
		Map<ZpvEnum, String> zpvNummerMap = mapZPVNummerToBesitzer(kibonAnfrageContext);

		if (zpvNummerMap.isEmpty()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);
			return kibonAnfrageContext;
		}
		if (hasTwoAntragStellende && kibonAnfrageContext.isGemeinsam()) {
			String zpvNummer = zpvNummerMap.get(ZpvEnum.ZPV_BESITZER) == null ?
					zpvNummerMap.get(ZpvEnum.ZPV_GESUCHSTELLER_1) : zpvNummerMap.get(ZpvEnum.ZPV_BESITZER);
			if (null != zpvNummer) {
				try {
					return getKibonAnfrageContextWithSteuerdaten(
							zpvNummer,
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
									zpvNummer,
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
			}
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);
		} else {
			// anfrage single GS
			if (gesuchstellerNumber == 1) {
				String zpvNummer = zpvNummerMap.get(ZpvEnum.ZPV_BESITZER) == null ?
						zpvNummerMap.get(ZpvEnum.ZPV_GESUCHSTELLER_1) : zpvNummerMap.get(ZpvEnum.ZPV_BESITZER);
				if (null == zpvNummer) {
					kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);
					return kibonAnfrageContext;
				}
				try {
					return getKibonAnfrageContextWithSteuerdaten(
							zpvNummer,
							Objects.requireNonNull(kibonAnfrageContext.getGesuch().getGesuchsteller1()),
							kibonAnfrageContext,
							gesuchstellerNumber);
				} catch (KiBonAnfrageServiceException e) {
					kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
				}
			}
			// anfrage single GS
			if (gesuchstellerNumber == 2) {
				String zpvNummer = zpvNummerMap.get(ZpvEnum.ZPV_BESITZER) == null ?
						zpvNummerMap.get(ZpvEnum.ZPV_GESUCHSTELLER_2) :
						zpvNummerMap.get(ZpvEnum.ZPV_BESITZER);
				if (null == zpvNummer) {
					kibonAnfrageContext.setSteuerdatenAnfrageStatus(
							SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER_GS2);
					return kibonAnfrageContext;
				}
				try {
					return getKibonAnfrageContextWithSteuerdaten(
							zpvNummer,
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
			String zpvNummer,
			GesuchstellerContainer gesuchstellerContainer,
			KibonAnfrageContext kibonAnfrageContext,
			int gesuchstellerNumber) throws KiBonAnfrageServiceException {
		SteuerdatenResponse steuerdatenResponseGS = kibonAnfrageService.getSteuerDaten(
				Integer.valueOf(zpvNummer),
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

	@Nonnull
	private Map<ZpvEnum, String> mapZPVNummerToBesitzer(@Nonnull KibonAnfrageContext context) {
		HashMap<ZpvEnum, String> gesuchstellerToZpvMap = new HashMap<>();
		String zpvBesitzer = findZpvNummerFromGesuchBesitzer(context);

		if (null != zpvBesitzer && !zpvBesitzer.trim().isEmpty()) {
			gesuchstellerToZpvMap.put(ZpvEnum.ZPV_BESITZER, zpvBesitzer);
		}
		if (null != context.getGesuch().getGesuchsteller1()
				&& null != context.getGesuch().getGesuchsteller1().getGesuchstellerJA()
				&& null != context.getGesuch().getGesuchsteller1().getGesuchstellerJA().getZpvNummer()) {
			gesuchstellerToZpvMap.put(
					ZpvEnum.ZPV_GESUCHSTELLER_1,
					context.getGesuch().getGesuchsteller1().getGesuchstellerJA().getZpvNummer().trim());
		}
		if (context.getGesuch().getGesuchsteller2() != null &&
				context.getGesuch().getGesuchsteller2().getGesuchstellerJA() != null &&
				context.getGesuch().getGesuchsteller2().getGesuchstellerJA().getZpvNummer() != null) {
			gesuchstellerToZpvMap.put(
					ZpvEnum.ZPV_GESUCHSTELLER_2,
					context.getGesuch().getGesuchsteller2().getGesuchstellerJA().getZpvNummer().trim());
		}
		return gesuchstellerToZpvMap;
	}

	@Nullable
	private String findZpvNummerFromGesuchBesitzer(KibonAnfrageContext context) {
		if (principalBean.isCallerInAnyOfRole(UserRole.getSuperadminAllGemeindeRoles())
				|| principalBean.isAnonymousSuperadmin()) {
			//Online Fall hat immer ein Besitzer
			Objects.requireNonNull(context.getGesuch().getFall().getBesitzer());
			return context.getGesuch().getFall().getBesitzer().getZpvNummer();
		}

		//wenn user role nicht gemeinde, dann soll nur der aktuelle benutzer die steuerdaten abfragen können
		return principalBean.getBenutzer().getZpvNummer();
	}

}
