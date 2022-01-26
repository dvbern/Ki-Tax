/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.reporting.verrechnungKibon.VerrechnungKibonDataRow;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;

public interface ReportVerrechnungKibonService {

	@Nonnull
	List<VerrechnungKibonDataRow> getReportVerrechnungKibon(boolean doSave, @Nonnull BigDecimal betragProKind, @Nonnull Locale locale, @Nonnull
			Mandant mandant);


	@Nonnull
	UploadFileInfo generateExcelReportVerrechnungKibon(boolean doSave, @Nonnull BigDecimal betragProKind, @Nonnull Locale locale, @Nonnull Mandant mandant) throws ExcelMergeException;
}
