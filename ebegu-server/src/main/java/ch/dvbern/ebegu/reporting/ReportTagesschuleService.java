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

package ch.dvbern.ebegu.reporting;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.reporting.tagesschule.TagesschuleAnmeldungenDataRow;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;

public interface ReportTagesschuleService {

	@Nonnull
	UploadFileInfo generateExcelReportTagesschuleAnmeldungen(
		@Nonnull String stammdatenId,
		@Nonnull String gesuchsperiodeId,
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException;

	@Nonnull
	List<TagesschuleAnmeldungenDataRow> getReportDataTagesschuleAnmeldungen(
		@Nonnull String stammdatenID,
		@Nonnull String gesuchsperiodeID);

	@Nonnull
	UploadFileInfo generateExcelReportTagesschuleRechnungsstellung(
		@Nonnull Locale locale,
		@Nonnull String gesuchsperiodeID
	) throws ExcelMergeException, IOException;
}
