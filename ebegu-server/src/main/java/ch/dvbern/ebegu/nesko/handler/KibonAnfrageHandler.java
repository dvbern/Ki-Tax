/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.nesko.handler;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
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

	public KibonAnfrageContext handleKibonAnfrage(
		KibonAnfrageContext kibonAnfrageContext,
		boolean isGemeinsam) {
		return handleKibonAnfrage(kibonAnfrageContext, isGemeinsam, true);
	}

	private KibonAnfrageContext handleKibonAnfrage(
		KibonAnfrageContext kibonAnfrageContext,
		boolean isGemeinsam,
		boolean doRetry) {
		boolean hasTwoAntragStellende = kibonAnfrageContext.getGesuch().getGesuchsteller2() != null;

		if (hasTwoAntragStellende && isGemeinsam) {
			// nur erstes Mal, dann schon initialisiert
			if (doRetry) {
				createFinSitGS2Container(kibonAnfrageContext);
			}

			String zpvNummer = findZpvNummerForRequest(kibonAnfrageContext, false);

			if (zpvNummer != null) {
				try {
					// try gemeinsame Steuererklärung anfrage
					SteuerdatenResponse steuerdatenResponse = kibonAnfrageService.getSteuerDaten(
						Integer.valueOf(zpvNummer),
						kibonAnfrageContext.getGesuchsteller().getGesuchstellerJA().getGeburtsdatum(),
						kibonAnfrageContext.getKibonAnfrageId(),
						kibonAnfrageContext.getGesuch().getGesuchsperiode().getBasisJahrPlus1());
					kibonAnfrageContext.setSteuerdatenResponse(steuerdatenResponse);
					assert kibonAnfrageContext.getFinSitContGS2() != null;
					KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(kibonAnfrageContext, steuerdatenResponse);
				} catch (KiBonAnfrageServiceException e) {
					assert kibonAnfrageContext.getGesuch().getFamiliensituationContainer() != null;
					assert kibonAnfrageContext.getGesuch().getFamiliensituationContainer().getFamiliensituationJA()
						!= null;
					if (kibonAnfrageContext.getGesuch()
						.getFamiliensituationContainer()
						.getFamiliensituationJA()
						.getFamilienstatus()
						.equals(
							EnumFamilienstatus.VERHEIRATET) && doRetry) {
						return handleKibonAnfrage(kibonAnfrageContext.switchGSContainer(), true, false);
					} else {
						kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
					}
				}
			} else {
				kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);
			}
		} else {
			// anfrage single GS
			String zpvNummer = findZpvNummerForRequest(kibonAnfrageContext, kibonAnfrageContext.isGesuchsteller2());

			if (zpvNummer != null) {
				try {
					SteuerdatenResponse steuerdatenResponseGS1 = kibonAnfrageService.getSteuerDaten(
						Integer.valueOf(zpvNummer),
						kibonAnfrageContext.getGesuchsteller().getGesuchstellerJA().getGeburtsdatum(),
						kibonAnfrageContext.getKibonAnfrageId(),
						kibonAnfrageContext.getGesuch().getGesuchsperiode().getBasisJahrPlus1());
					kibonAnfrageContext.setSteuerdatenResponse(steuerdatenResponseGS1);
					KibonAnfrageHelper.handleSteuerdatenResponse(
						kibonAnfrageContext,
						steuerdatenResponseGS1);
				} catch (KiBonAnfrageServiceException e) {
					kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
				}
			} else {
				kibonAnfrageContext.setSteuerdatenAnfrageStatus(
					kibonAnfrageContext.isGesuchsteller2() ?
						SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER_GS2 :
						SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);
			}
		}
		return kibonAnfrageContext;
	}

	private void createFinSitGS2Container(KibonAnfrageContext kibonAnfrageContext) {
		assert kibonAnfrageContext.getGesuch().getGesuchsteller2() != null;
		FinanzielleSituationContainer finSitGS2Cont =
			kibonAnfrageContext.getGesuch().getGesuchsteller2().getFinanzielleSituationContainer() != null ?
				kibonAnfrageContext.getGesuch().getGesuchsteller2().getFinanzielleSituationContainer() :
				new FinanzielleSituationContainer();
		if (finSitGS2Cont.getFinanzielleSituationJA() == null) {
			finSitGS2Cont.setFinanzielleSituationJA(new FinanzielleSituation());
		}
		finSitGS2Cont.setJahr(kibonAnfrageContext.getFinSitCont().getJahr());
		finSitGS2Cont.getFinanzielleSituationJA().setSteuerdatenZugriff(true);
		finSitGS2Cont.getFinanzielleSituationJA()
			.setSteuererklaerungAusgefuellt(kibonAnfrageContext.getFinSitCont().getFinanzielleSituationJA()
				.getSteuererklaerungAusgefuellt());
		finSitGS2Cont.getFinanzielleSituationJA()
			.setSteuerveranlagungErhalten(kibonAnfrageContext.getFinSitCont().getFinanzielleSituationJA()
				.getSteuerveranlagungErhalten());
		finSitGS2Cont.setGesuchsteller(kibonAnfrageContext.getGesuch().getGesuchsteller2());
		kibonAnfrageContext.setFinSitContGS2(finSitGS2Cont);
	}

	@Nullable
	private String findZpvNummerForRequest(KibonAnfrageContext context, boolean isGesuchsteller2) {
		String zpvBesitzer = findZpvNummerFromGesuchBesitzer(context);

		return isGesuchsteller2 || zpvBesitzer == null ?
			context.getGesuchsteller().getGesuchstellerJA().getZpvNummer() :
			zpvBesitzer;
	}

	@Nullable
	private String findZpvNummerFromGesuchBesitzer(KibonAnfrageContext context) {
		if ((principalBean.isCallerInAnyOfRole(UserRole.getBgAndGemeindeRoles())
			&& context.getGesuch().isMutation()) || principalBean.isAnonymousSuperadmin()) {
			//Online Fall hat immer ein Besitzer
			Objects.requireNonNull(context.getGesuch().getFall().getBesitzer());
			return context.getGesuch().getFall().getBesitzer().getZpvNummer();
		}

		//wenn user role nicht gemeinde, dann soll nur der aktuelle benutzer die steuerdaten abfragen können
		return principalBean.getBenutzer().getZpvNummer();
	}


}
