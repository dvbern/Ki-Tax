/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KibonPdfGeneratorTest {

	private GemeindeStammdaten stammdaten;
	private List<DokumentGrund> benoetigteUnterlagen;
	private Gesuch gesuch_alleinstehend;
	private Gesuch gesuch_verheiratet;


	@Before
	public void init() throws IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(KibonPdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		stammdaten = TestDataUtil.createGemeindeWithStammdaten();
		stammdaten.setLogoContent(gemeindeLogo);
		gesuch_alleinstehend = TestDataUtil.createTestgesuchDagmar();
		gesuch_verheiratet = TestDataUtil.createTestgesuchYvonneFeuz();
		benoetigteUnterlagen = new ArrayList<>();
		benoetigteUnterlagen.add(new DokumentGrund(DokumentGrundTyp.FINANZIELLESITUATION, DokumentTyp.STEUERERKLAERUNG));
		benoetigteUnterlagen.add(new DokumentGrund(DokumentGrundTyp.ERWERBSPENSUM, DokumentTyp.NACHWEIS_ERWERBSPENSUM));
		benoetigteUnterlagen.add(new DokumentGrund(DokumentGrundTyp.ERWEITERTE_BETREUUNG, DokumentTyp.BESTAETIGUNG_ARZT));
	}

	@Test
	public void gemeindeStammdatenToHeader() {

		final FreigabequittungPdfGenerator quittungAlleinstehend = new FreigabequittungPdfGenerator(gesuch_alleinstehend, stammdaten, false,
			benoetigteUnterlagen);
		List<String> strings = quittungAlleinstehend.getAbsenderHeader(stammdaten);
		Assert.assertNotNull(strings);
		Assert.assertEquals(8, strings.size());
	}

	@Test
	public void freigabequittungTest() throws InvoiceGeneratorException, IOException {
		final FreigabequittungPdfGenerator quittungAlleinstehend = new FreigabequittungPdfGenerator(gesuch_alleinstehend, stammdaten, false,
			benoetigteUnterlagen);
		quittungAlleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Freigabequittung_alleinstehend.pdf"));

		final FreigabequittungPdfGenerator quittungVerheiratet = new FreigabequittungPdfGenerator(gesuch_verheiratet, stammdaten, false,
			benoetigteUnterlagen);
		quittungVerheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Freigabequittung_verheiratet.pdf"));
		Assert.assertTrue(true);
	}
}
