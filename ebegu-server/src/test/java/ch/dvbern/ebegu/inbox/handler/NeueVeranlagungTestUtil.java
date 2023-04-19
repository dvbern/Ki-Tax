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

package ch.dvbern.ebegu.inbox.handler;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.kibon.exchange.commons.neskovanp.NeueVeranlagungEventDTO;

public final class NeueVeranlagungTestUtil {

	public static SteuerdatenResponse createSteuerdatenResponseAleine(NeueVeranlagungEventDTO dto) {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setZpvNrDossiertraeger(dto.getZpvNummer());
		steuerdatenResponse.setZpvNrAntragsteller(dto.getZpvNummer());
		steuerdatenResponse.setKiBonAntragId(dto.getKibonAntragId());
		steuerdatenResponse.setBeginnGesuchsperiode(dto.getGesuchsperiodeBeginnJahr());
		steuerdatenResponse.setNettovermoegen(new BigDecimal(1000000));
		steuerdatenResponse.setErwerbseinkommenUnselbstaendigkeitDossiertraeger(new BigDecimal(400000));
		steuerdatenResponse.setBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme(BigDecimal.ONE);
		steuerdatenResponse.setBruttoertraegeAusLiegenschaften(new BigDecimal(27000));
		steuerdatenResponse.setSchuldzinsen(new BigDecimal(7000));
		steuerdatenResponse.setLiegenschaftsAbzuege(new BigDecimal(5000));
		return steuerdatenResponse;
	}
}
