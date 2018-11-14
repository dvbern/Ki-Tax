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
import java.util.Arrays;
import java.util.List;

import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("ConstantJUnitAssertArgument")
public class PdfGeneratorTest {

	private final List<String> gemeindeHeader = Arrays.asList(
		"Jugendamt",
		"Effingerstrasse 21",
		"3008 Bern",
		"",
		"Telefon 031 951 11 22",
		"kinderbetreuung@bern.ch",
		"www.bern.ch",
		"",
		"",
		"Bern, 08.11.2018");


	@Test
	public void begleitschreibenTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("StadtBern.jpg"));
		final BegleitschreibenPdfGenerator begleitschreibenPdfGenerator = new BegleitschreibenPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		begleitschreibenPdfGenerator.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Begleitschreiben.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void normaleVerfuegungTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final VerfuegungPdfGenerator
			verfuegungPdfGenerator = new VerfuegungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		verfuegungPdfGenerator.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Verfügung.pdf"), VerfuegungPdfGenerator.Art.NORMAL);
		Assert.assertTrue(true);
	}

	@Test
	public void keinAnspruchVerfuegungTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final VerfuegungPdfGenerator
			verfuegungPdfGenerator = new VerfuegungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		verfuegungPdfGenerator.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/KeinAnspruchVerfügung.pdf"), VerfuegungPdfGenerator.Art.KEIN_ANSPRUCH);
		Assert.assertTrue(true);
	}

	@Test
	public void nichtEintretenVerfuegungTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final VerfuegungPdfGenerator
			verfuegungPdfGenerator = new VerfuegungPdfGenerator(gemeindeLogo, gemeindeHeader, true);
		verfuegungPdfGenerator.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/NichtEintretenVerfügung.pdf"), VerfuegungPdfGenerator.Art.NICHT_EINTRETTEN);
		Assert.assertTrue(true);
	}

	@Test
	public void finanzielleSituationTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final FinanzielleSituationPdfGenerator finanzielleSituationPdfGenerator = new FinanzielleSituationPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		finanzielleSituationPdfGenerator.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/FinanzielleSituation.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void mahnung1Test() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final MahnungPdfGenerator mahnungPdfGenerator = new MahnungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		mahnungPdfGenerator.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Mahnung1.pdf"), false);
		Assert.assertTrue(true);
	}

	@Test
	public void mahnung2Test() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final MahnungPdfGenerator mahnungPdfGenerator = new MahnungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		mahnungPdfGenerator.generate(new FileOutputStream(FileUtils.getTempDirectoryPath() + "/Mahnung2.pdf"), true);
		Assert.assertTrue(true);
	}

}
