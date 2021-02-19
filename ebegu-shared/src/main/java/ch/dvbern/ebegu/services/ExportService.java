/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.util.UploadFileInfo;

/**
 * Service to export Verfuegungen for usage in other applications
 */
public interface ExportService {

	/**
	 * prepares a file containing a VerfuegungenExportDTO marshalled as JSON which contains the information
	 * of the verfuegung of the given betreuungId
	 *
	 * @param betreuungID ID of the Betreuung that should be exported
	 * @return All Information needed to download the generated file (i.e. the accessToken)
	 * @deprecated the classical file export as known from ki-tax will be discontinued. Instead, the
	 * kibon-exchange-api should be used by certified applications to import Verfuegungen.
	 * The entire dataexport.v1 package is deprecated.
	 *
	 * But we cannot remove this function for the moment, since one external application is still using
	 * this export and we are obligated by the contract not to remove functions that used to exist.
	 */
	@Deprecated
	UploadFileInfo exportVerfuegungOfBetreuungAsFile(String betreuungID);
}
