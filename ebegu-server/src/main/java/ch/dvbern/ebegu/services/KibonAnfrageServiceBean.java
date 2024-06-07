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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.errors.KiBonAnfrageServiceException;
import ch.dvbern.ebegu.errors.OIDCServiceException;
import ch.dvbern.ebegu.ws.neskovanp.IKibonAnfrageWebService;

@Stateless
@Local(KibonAnfrageService.class)
public class KibonAnfrageServiceBean implements KibonAnfrageService {

	@Inject
	private IKibonAnfrageWebService kibonAnfrageWebService;

	@Override
	@Nonnull
	public SteuerdatenResponse getSteuerDaten(Integer zpvNummer, LocalDate geburtsdatum, String gesuchId, Integer gesuchsperiodeBeginnJahr)
		throws KiBonAnfrageServiceException, OIDCServiceException {
		return kibonAnfrageWebService.getSteuerDaten(zpvNummer, geburtsdatum, gesuchId, gesuchsperiodeBeginnJahr);
	}

}
