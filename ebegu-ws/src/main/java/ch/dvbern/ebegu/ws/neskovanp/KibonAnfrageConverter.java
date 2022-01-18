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

import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.SteuerDatenResponseType;
import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;

public class KibonAnfrageConverter {

	@SuppressWarnings("PMD.NcssMethodCount")
	public static SteuerdatenResponse convertFromKibonAnfrage(SteuerDatenResponseType steuerdatenResponseType) {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();

		steuerdatenResponse.setZpvNrAntragsteller(steuerdatenResponseType.getZPVNrAntragsteller());
		steuerdatenResponse.setGeburtsdatumAntragsteller(steuerdatenResponseType.getGeburtsdatumAntragsteller());
		steuerdatenResponse.setKiBonAntragId(steuerdatenResponseType.getKiBonAntragID());
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
		if (steuerdatenResponse.getErhalteneUnterhaltsbeitraegeDossiertraeger() != null) {
			steuerdatenResponse.setErhalteneUnterhaltsbeitraegeDossiertraeger(new BigDecimal(steuerdatenResponseType.getErwerbseinkommenUnselbstaendigkeitDossiertraeger()));
		}
		if (steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitPartner() != null) {
			steuerdatenResponse.setErwerbseinkommenUnselbstaendigkeitPartner(new BigDecimal(steuerdatenResponseType.getErwerbseinkommenUnselbstaendigkeitPartner()));
		}

		if (steuerdatenResponseType.getSteuerpflichtigesErsatzeinkommenDossiertraeger() != null) {
			steuerdatenResponse.setSteuerpflichtigesErsatzeinkommenDossiertraeger(new BigDecimal(steuerdatenResponseType.getSteuerpflichtigesErsatzeinkommenDossiertraeger()));
		}
		if (steuerdatenResponseType.getSteuerpflichtigesErsatzeinkommenPartner() != null) {
			steuerdatenResponse.setSteuerpflichtigesErsatzeinkommenPartner(new BigDecimal(steuerdatenResponseType.getSteuerpflichtigesErsatzeinkommenPartner()));
		}
		if (steuerdatenResponseType.getErhalteneUnterhaltsbeitraegeDossiertraeger() != null) {
			steuerdatenResponse.setErhalteneUnterhaltsbeitraegeDossiertraeger(new BigDecimal(steuerdatenResponseType.getErhalteneUnterhaltsbeitraegeDossiertraeger()));
		}
		if (steuerdatenResponseType.getErhalteneUnterhaltsbeitraegePartner() != null) {
			steuerdatenResponse.setErhalteneUnterhaltsbeitraegePartner(new BigDecimal(steuerdatenResponseType.getErhalteneUnterhaltsbeitraegePartner()));
		}
		if (steuerdatenResponseType.getAusgewiesenerGeschaeftsertragDossiertraeger() != null) {
			steuerdatenResponse.setAusgewiesenerGeschaeftsertragDossiertraeger(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragDossiertraeger()));
		}
		if (steuerdatenResponseType.getAusgewiesenerGeschaeftsertragPartner() != null) {
			steuerdatenResponse.setAusgewiesenerGeschaeftsertragPartner(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragPartner()));
		}
		if (steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger() != null) {
			steuerdatenResponse.setAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger()));
		}
		if (steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiodePartner() != null) {
			steuerdatenResponse.setAusgewiesenerGeschaeftsertragVorperiodePartner(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiodePartner()));
		}
		if (steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger() != null) {
			steuerdatenResponse.setAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger()));
		}
		if (steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiode2Partner() != null) {
			steuerdatenResponse.setAusgewiesenerGeschaeftsertragVorperiode2Partner(new BigDecimal(steuerdatenResponseType.getAusgewiesenerGeschaeftsertragVorperiode2Partner()));
		}
		if (steuerdatenResponseType.getWeitereSteuerbareEinkuenfteDossiertraeger() != null) {
			steuerdatenResponse.setWeitereSteuerbareEinkuenfteDossiertraeger(new BigDecimal(steuerdatenResponseType.getWeitereSteuerbareEinkuenfteDossiertraeger()));
		}
		if (steuerdatenResponseType.getWeitereSteuerbareEinkuenftePartner() != null) {
			steuerdatenResponse.setWeitereSteuerbareEinkuenftePartner(new BigDecimal(steuerdatenResponseType.getWeitereSteuerbareEinkuenftePartner()));
		}
		if (steuerdatenResponseType.getBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME() != null) {
			steuerdatenResponse.setBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme(new BigDecimal(steuerdatenResponseType.getBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME()));
		}
		if (steuerdatenResponseType.getBruttoertraegeAusLiegenschaften() != null) {
			steuerdatenResponse.setBruttoertraegeAusLiegenschaften(new BigDecimal(steuerdatenResponseType.getBruttoertraegeAusLiegenschaften()));
		}
		if (steuerdatenResponseType.getNettoertraegeAusEGMEDossiertraeger() != null) {
			steuerdatenResponse.setNettoertraegeAusEgmeDossiertraeger(new BigDecimal(steuerdatenResponseType.getNettoertraegeAusEGMEDossiertraeger()));
		}
		if (steuerdatenResponseType.getNettoertraegeAusEGMEPartner() != null) {
			steuerdatenResponse.setNettoertraegeAusEgmePartner(new BigDecimal(steuerdatenResponseType.getNettoertraegeAusEGMEPartner()));
		}
		if (steuerdatenResponseType.getGeleisteteUnterhaltsbeitraege() != null) {
			steuerdatenResponse.setGeleisteteUnterhaltsbeitraege(new BigDecimal(steuerdatenResponseType.getGeleisteteUnterhaltsbeitraege()));
		}
		if (steuerdatenResponseType.getGewinnungskostenBeweglichesVermoegen() != null) {
			steuerdatenResponse.setGewinnungskostenBeweglichesVermoegen(new BigDecimal(steuerdatenResponseType.getGewinnungskostenBeweglichesVermoegen()));
		}
		if (steuerdatenResponseType.getLiegenschaftsAbzuege() != null) {
			steuerdatenResponse.setLiegenschaftsAbzuege(new BigDecimal(steuerdatenResponseType.getLiegenschaftsAbzuege()));
		}
		if (steuerdatenResponseType.getNettovermoegen() != null) {
			steuerdatenResponse.setNettovermoegen(new BigDecimal(steuerdatenResponseType.getNettovermoegen()));
		}

		return steuerdatenResponse;
	}

}

