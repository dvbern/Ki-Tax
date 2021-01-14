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

package ch.dvbern.ebegu.tests.reporting;

import java.time.LocalDate;

import ch.dvbern.ebegu.mocks.FileSaverServiceMock;
import ch.dvbern.ebegu.mocks.ReportMahlzeitenverguenstigungServiceMock;
import ch.dvbern.ebegu.reporting.ReportMahlzeitenService;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import org.junit.Rule;
import org.junit.Test;
import org.needle4j.annotation.InjectIntoMany;
import org.needle4j.annotation.ObjectUnderTest;
import org.needle4j.junit.NeedleRule;

import static org.junit.Assert.assertNotNull;

public class ReportMahlzeitenverguenstigungServiceBeanTest {
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest
	private ReportMahlzeitenService reportService = new ReportMahlzeitenverguenstigungServiceMock();

	@InjectIntoMany
	private FileSaverService fileSaverService = new FileSaverServiceMock();

	@Test
	public void generateExcelReportMahlzeiten() throws Exception {

		UploadFileInfo uploadFileInfo = reportService.generateExcelReportMahlzeiten(
			LocalDate.now().minusDays(100),
			LocalDate.now().plusDays(100),
			Constants.DEFAULT_LOCALE,
			"1111"
			);

		assertNotNull(uploadFileInfo.getBytes());
	}
}
