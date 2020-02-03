/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.ws.ewk;

import java.time.LocalDate;

import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoParametersType;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;

/**
 * Interface fuer Geres Web Service
 */
public interface IGeresWebService extends IEWKWebService {

	String test() throws PersonenSucheServiceException;

	EWKResultat residentInfoFull(ResidentInfoParametersType residentInfoParameters, LocalDate validityDate, Integer searchMax) throws PersonenSucheServiceException;

	EWKResultat residentInfoFast(ResidentInfoParametersType residentInfoParameters, LocalDate validityDate, Integer searchMax) throws PersonenSucheServiceException;
}
