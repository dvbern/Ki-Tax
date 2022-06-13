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

package ch.dvbern.ebegu.api.resource.handler;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
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
		Benutzer benutzer = principalBean.getBenutzer();
		if (hasTwoAntragStellende && isGemeinsam) {
			// nur erstes Mal, dann schon initialisiert
			if (doRetry) {
				createFinSitGS2Container(kibonAnfrageContext);
			}

			String zpvNummer = benutzer.getZpvNummer() != null ?
				benutzer.getZpvNummer() :
				kibonAnfrageContext.getGesuchsteller().getGesuchstellerJA().getZpvNummer();

			if (zpvNummer != null) {
				try {
					// try gemeinsame Steuererklärung anfrage
					SteuerdatenResponse steuerdatenResponse = kibonAnfrageService.getSteuerDaten(
						Integer.valueOf(zpvNummer),
						kibonAnfrageContext.getGesuchsteller().getGesuchstellerJA().getGeburtsdatum(),
						kibonAnfrageContext.getKibonAnfrageId(),
						kibonAnfrageContext.getGesuch().getGesuchsperiode().getBasisJahrPlus1());
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
						return handleKibonAnfrage(kibonAnfrageContext.zwitchGSContainer(), true, false);
					} else {
						kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
					}
				}
			} else {
				kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);
			}
		} else {
			// anfrage single GS
			final boolean isGS2 =
				kibonAnfrageContext.getGesuchsteller().equals(kibonAnfrageContext.getGesuch().getGesuchsteller2());
			String zpvNummer = isGS2 || benutzer.getZpvNummer() == null ?
				kibonAnfrageContext.getGesuchsteller().getGesuchstellerJA().getZpvNummer() :
				benutzer.getZpvNummer();
			if (zpvNummer != null) {
				try {
					SteuerdatenResponse steuerdatenResponseGS1 = kibonAnfrageService.getSteuerDaten(
						Integer.valueOf(zpvNummer),
						kibonAnfrageContext.getGesuchsteller().getGesuchstellerJA().getGeburtsdatum(),
						kibonAnfrageContext.getKibonAnfrageId(),
						kibonAnfrageContext.getGesuch().getGesuchsperiode().getBasisJahrPlus1());
					KibonAnfrageHelper.handleSteuerdatenResponse(
						kibonAnfrageContext,
						steuerdatenResponseGS1);
				} catch (KiBonAnfrageServiceException e) {
					kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
				}
			} else {
				kibonAnfrageContext.setSteuerdatenAnfrageStatus(
					isGS2 ?
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
}
