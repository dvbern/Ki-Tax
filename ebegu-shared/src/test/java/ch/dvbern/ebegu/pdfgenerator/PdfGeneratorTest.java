package ch.dvbern.ebegu.pdfgenerator;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
	public void freigabequittungTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final FreigabequittungPdfGenerator freigabequittungPdfGenerator = new FreigabequittungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		freigabequittungPdfGenerator.generate(new FileOutputStream("target/Freigabequittung.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void begleitschreibenTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("StadtBern.jpg"));
		final BegleitschreibenPdfGenerator begleitschreibenPdfGenerator = new BegleitschreibenPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		begleitschreibenPdfGenerator.generate(new FileOutputStream("target/Begleitschreiben.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void normaleVerfügungTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final VerfügungPdfGenerator verfügungPdfGenerator = new VerfügungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		verfügungPdfGenerator.generate(new FileOutputStream("target/Verfügung.pdf"), VerfügungPdfGenerator.Art.NORMAL);
		Assert.assertTrue(true);
	}

	@Test
	public void keinAnspruchVerfügungTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final VerfügungPdfGenerator verfügungPdfGenerator = new VerfügungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		verfügungPdfGenerator.generate(new FileOutputStream("target/KeinAnspruchVerfügung.pdf"), VerfügungPdfGenerator.Art.KEIN_ANSPRUCH);
		Assert.assertTrue(true);
	}

	@Test
	public void nichtEintretenVerfügungTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final VerfügungPdfGenerator verfügungPdfGenerator = new VerfügungPdfGenerator(gemeindeLogo, gemeindeHeader, true);
		verfügungPdfGenerator.generate(new FileOutputStream("target/NichtEintretenVerfügung.pdf"), VerfügungPdfGenerator.Art.NICHT_EINTRETTEN);
		Assert.assertTrue(true);
	}

	@Test
	public void finanzielleSituationTest() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final FinanzielleSituationPdfGenerator finanzielleSituationPdfGenerator = new FinanzielleSituationPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		finanzielleSituationPdfGenerator.generate(new FileOutputStream("target/FinanzielleSituation.pdf"));
		Assert.assertTrue(true);
	}

	@Test
	public void mahnung1Test() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final MahnungPdfGenerator mahnungPdfGenerator = new MahnungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		mahnungPdfGenerator.generate(new FileOutputStream("target/Mahnung1.pdf"), false);
		Assert.assertTrue(true);
	}

	@Test
	public void mahnung2Test() throws InvoiceGeneratorException, IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(PdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		final MahnungPdfGenerator mahnungPdfGenerator = new MahnungPdfGenerator(gemeindeLogo, gemeindeHeader, false);
		mahnungPdfGenerator.generate(new FileOutputStream("target/Mahnung2.pdf"), true);
		Assert.assertTrue(true);
	}

}
