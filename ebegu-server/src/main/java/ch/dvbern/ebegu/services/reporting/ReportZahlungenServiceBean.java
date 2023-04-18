/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.reporting;

import java.io.IOException;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;

import ch.dvbern.ebegu.reporting.ReportZahlungenService;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;

@Stateless
@Local(ReportZahlungenService.class)
public class ReportZahlungenServiceBean extends AbstractReportServiceBean implements ReportZahlungenService {

	@Nonnull
	@Override
	public UploadFileInfo generateExcelReportZahlungen(
		@Nonnull Locale locale,
		@Nonnull String gesuchsperiodeId,
		@Nullable String gemeindeId,
		@Nullable String institutionId
	) throws ExcelMergeException, IOException {
		return null;
	}
}
