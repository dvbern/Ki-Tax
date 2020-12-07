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

import java.io.IOException;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;

public interface ReportKinderMitZemisNummerService {

	@Nonnull
	UploadFileInfo generateZemisReport(@Nonnull Integer lastenausgleichJahr, @Nonnull Locale german) throws ExcelMergeException;

	void setFlagAndSaveZemisExcel(@Nonnull byte[] fileContent, @Nonnull Integer jahr) throws IOException, MailException;
}
