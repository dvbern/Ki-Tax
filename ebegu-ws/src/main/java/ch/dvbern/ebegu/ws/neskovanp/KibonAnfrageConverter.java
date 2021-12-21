/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.ws.neskovanp;

import java.math.BigDecimal;

import ch.dvbern.ebegu.dto.neskovanp.SteuerdatenResponse;
import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.SteuerDatenResponseType;
import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;

public class KibonAnfrageConverter {

	public static SteuerdatenResponse convertFromKibonAnfrage(SteuerDatenResponseType steuerdatenResponseType) {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();

		steuerdatenResponse.setZpvNrAntragsteller(steuerdatenResponseType.getZPVNrAntragsteller());
		steuerdatenResponse.setGeburtsdatumAntragsteller(steuerdatenResponseType.getGeburtsdatumAntragsteller());
		steuerdatenResponse.setKiBonAntragID(steuerdatenResponseType.getKiBonAntragID());
		steuerdatenResponse.setBeginnGesuchsperiode(steuerdatenResponseType.getBeginnGesuchsperiode());
		steuerdatenResponse.setZpvNrDossiertraeger(steuerdatenResponseType.getZPVNrDossiertraeger());
		steuerdatenResponse.setGeburtsdatumDossiertraeger(steuerdatenResponseType.getGeburtsdatumDossiertraeger());
		steuerdatenResponse.setZpvNrPartner(steuerdatenResponseType.getZPVNrPartner());
		steuerdatenResponse.setGeburtsdatumPartner(steuerdatenResponseType.getGeburtsdatumPartner());
		steuerdatenResponse.setFallId(steuerdatenResponseType.getFallId());
		steuerdatenResponse.setAntwortdatum(steuerdatenResponseType.getAntwortdatum());
		steuerdatenResponse.setSynchroneAntwort(steuerdatenResponseType.isSynchroneAntwort());
		steuerdatenResponse.setVeranlagungsstand(Veranlagungsstand.valueOf(steuerdatenResponseType.getVeranlagungsstand().value()));
		steuerdatenResponse.setUnterjaehrigerFall(steuerdatenResponseType.isUnterjaehrigerFall());
		steuerdatenResponse.setErwerbseinkommenUnselbstaendigkeitDossiertraeger(new BigDecimal(steuerdatenResponseType.getErwerbseinkommenUnselbstaendigkeitDossiertraeger()));
		steuerdatenResponse.setErwerbseinkommenUnselbstaendigkeitPartner(new BigDecimal(steuerdatenResponseType.getErwerbseinkommenUnselbstaendigkeitPartner()));
		steuerdatenResponse.setSteuerpflichtigesErsatzeinkommenDossiertraeger(new BigDecimal(steuerdatenResponseType.getSteuerpflichtigesErsatzeinkommenDossiertraeger()));
		steuerdatenResponse.setSteuerpflichtigesErsatzeinkommenPartner(new BigDecimal(steuerdatenResponseType.getSteuerpflichtigesErsatzeinkommenPartner()));
		steuerdatenResponse.setErhalteneUnterhaltsbeitraegeDossiertraeger(new BigDecimal(steuerdatenResponseType.getErhalteneUnterhaltsbeitraegeDossiertraeger()));
		steuerdatenResponse.setErhalteneUnterhaltsbeitraegePartner(new BigDecimal(steuerdatenResponseType.getErhalteneUnterhaltsbeitraegePartner()));
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragDossiertraeger(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragDossiertraeger()));
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragPartner(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragPartner()));
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger()));
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragVorperiodePartner(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiodePartner()));
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger()));
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragVorperiode2Partner(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiode2Partner()));
		steuerdatenResponse.setWeitereSteuerbareEinkuenfteDossiertraeger(new BigDecimal(steuerdatenResponseType.getWeitereSteuerbareEinkuenfteDossiertraeger()));
		steuerdatenResponse.setWeitereSteuerbareEinkuenftePartner(new BigDecimal(steuerdatenResponseType.getWeitereSteuerbareEinkuenftePartner()));
		steuerdatenResponse.setBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME(new BigDecimal(steuerdatenResponseType.getBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME()));
		steuerdatenResponse.setBruttoertraegeAusLiegenschaften(new BigDecimal(steuerdatenResponseType.getBruttoertraegeAusLiegenschaften()));
		steuerdatenResponse.setNettoertraegeAusEGMEDossiertraeger(new BigDecimal(steuerdatenResponseType.getNettoertraegeAusEGMEDossiertraeger()));
		steuerdatenResponse.setNettoertraegeAusEGMEPartner(new BigDecimal(steuerdatenResponseType.getNettoertraegeAusEGMEPartner()));
		steuerdatenResponse.setGeleisteteUnterhaltsbeitraege(new BigDecimal(steuerdatenResponseType.getGeleisteteUnterhaltsbeitraege()));
		steuerdatenResponse.setGewinnungskostenBeweglichesVermoegen(new BigDecimal(steuerdatenResponseType.getGewinnungskostenBeweglichesVermoegen()));
		steuerdatenResponse.setLiegenschaftsAbzuege(new BigDecimal(steuerdatenResponseType.getLiegenschaftsAbzuege()));
		steuerdatenResponse.setNettovermoegen(new BigDecimal(steuerdatenResponseType.getNettovermoegen()));

		return steuerdatenResponse;
	}

}

