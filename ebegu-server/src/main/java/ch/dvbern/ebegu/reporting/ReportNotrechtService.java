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

package ch.dvbern.ebegu.reporting;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.reporting.notrecht.NotrechtDataRow;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;

public interface ReportNotrechtService {

	@Nonnull
	List<NotrechtDataRow> getReportNotrecht(boolean zahlungenAusloesen);

	@Nonnull
	UploadFileInfo generateExcelReportNotrecht(boolean zahlungenAusloesen) throws ExcelMergeException, IOException;
}
