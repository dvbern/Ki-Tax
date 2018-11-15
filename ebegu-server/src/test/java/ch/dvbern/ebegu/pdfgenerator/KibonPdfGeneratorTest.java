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

/**
 * Erstellt pro Brief ein Exemplar für Einzelpersonen und Paare. Jeweils das Beispiel für Alleinstehende wird als Draft
 * generiert.
 */
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
		List<String> strings = quittungAlleinstehend.getGemeindeAdresse(stammdaten);
		Assert.assertNotNull(strings);
		Assert.assertEquals(8, strings.size());
	}

	@Test
	public void freigabequittungTest() throws InvoiceGeneratorException, IOException {
		final FreigabequittungPdfGenerator alleinstehend = new FreigabequittungPdfGenerator(gesuch_alleinstehend, stammdaten, true,
			benoetigteUnterlagen);
		alleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Freigabequittung_alleinstehend.pdf"));

		final FreigabequittungPdfGenerator verheiratet = new FreigabequittungPdfGenerator(gesuch_verheiratet, stammdaten, false,
			benoetigteUnterlagen);
		verheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Freigabequittung_verheiratet.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void begleitschreibenTest() throws InvoiceGeneratorException, IOException {
		final BegleitschreibenPdfGenerator alleinstehend =
			new BegleitschreibenPdfGenerator(gesuch_alleinstehend, stammdaten, true);
		alleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Begleitschreiben_alleinstehend.pdf"));

		final BegleitschreibenPdfGenerator verheiratet =
			new BegleitschreibenPdfGenerator(gesuch_verheiratet, stammdaten, false);
		verheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Begleitschreiben_verheiratet.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void normaleVerfuegungTest() throws InvoiceGeneratorException, IOException {
		final VerfuegungPdfGenerator alleinstehend =
			new VerfuegungPdfGenerator(gesuch_alleinstehend, stammdaten, true, VerfuegungPdfGenerator.Art.NORMAL);
		alleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Verfügung_alleinstehend.pdf"));

		final VerfuegungPdfGenerator verheiratet =
			new VerfuegungPdfGenerator(gesuch_verheiratet, stammdaten, false, VerfuegungPdfGenerator.Art.NORMAL);
		verheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Verfügung_verheiratet.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void keinAnspruchVerfuegungTest() throws InvoiceGeneratorException, IOException {
		final VerfuegungPdfGenerator alleinstehend =
			new VerfuegungPdfGenerator(gesuch_alleinstehend, stammdaten, true, VerfuegungPdfGenerator.Art.KEIN_ANSPRUCH);
		alleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/KeinAnspruchVerfügung_alleinstehend.pdf"));

		final VerfuegungPdfGenerator verheiratet =
			new VerfuegungPdfGenerator(gesuch_verheiratet, stammdaten, false, VerfuegungPdfGenerator.Art.KEIN_ANSPRUCH);
		verheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/KeinAnspruchVerfügung_verheiratet.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void nichtEintretenVerfuegungTest() throws InvoiceGeneratorException, IOException {
		final VerfuegungPdfGenerator alleinstehend =
			new VerfuegungPdfGenerator(gesuch_alleinstehend, stammdaten, true, VerfuegungPdfGenerator.Art.NICHT_EINTRETTEN);
		alleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/NichtEintretenVerfügung_alleinstehend.pdf"));

		final VerfuegungPdfGenerator verheiratet =
			new VerfuegungPdfGenerator(gesuch_verheiratet, stammdaten, false, VerfuegungPdfGenerator.Art.NICHT_EINTRETTEN);
		verheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/NichtEintretenVerfügung_verheiratet.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void finanzielleSituationTest() throws InvoiceGeneratorException, IOException {
		final FinanzielleSituationPdfGenerator alleinstehend =
			new FinanzielleSituationPdfGenerator(gesuch_alleinstehend, stammdaten, true);
		alleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/FinanzielleSituation_alleinstehend.pdf"));

		final FinanzielleSituationPdfGenerator verheiratet =
			new FinanzielleSituationPdfGenerator(gesuch_verheiratet, stammdaten, false);
		verheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/FinanzielleSituation_verheiratet.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void mahnung1Test() throws InvoiceGeneratorException, IOException {
		final MahnungPdfGenerator alleinstehend =
			new MahnungPdfGenerator(gesuch_alleinstehend, stammdaten, true, false);
		alleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Mahnung1_alleinstehend.pdf"));

		final MahnungPdfGenerator verheiratet =
			new MahnungPdfGenerator(gesuch_verheiratet, stammdaten, false, false);
		verheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Mahnung1_verheiratet.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void mahnung2Test() throws InvoiceGeneratorException, IOException {
		final MahnungPdfGenerator alleinstehend =
			new MahnungPdfGenerator(gesuch_alleinstehend, stammdaten, true, true);
		alleinstehend.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Mahnung2_alleinstehend.pdf"));

		final MahnungPdfGenerator verheiratet =
			new MahnungPdfGenerator(gesuch_verheiratet, stammdaten, false, true);
		verheiratet.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Mahnung2_verheiratet.pdf"));
		Assert.assertTrue(true);
	}
}
