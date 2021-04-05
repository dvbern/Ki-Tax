/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.enums.Land;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class VollmachtPdfGeneratorTest {

	private String pfad = FileUtils.getTempDirectoryPath() + "/generated/";
	private Fall fall;

	public VollmachtPdfGeneratorTest() {
		initFallDaten();
	}


	@Test
	public void createVollmacht() throws IOException, InvoiceGeneratorException {
		FileUtils.forceMkdir(new File(pfad));
		generateVollmacht(Sprache.DEUTSCH, "Vollmacht_de.pdf");
	}

	private void generateVollmacht(@Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		assert this.fall.getSozialdienstFall() != null;
		final VollmachtPdfGenerator generator = new VollmachtPdfGenerator(locale, this.fall.getSozialdienstFall());
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	private void initFallDaten() {
		this.fall = new Fall();
		this.fall.setFallNummer(1);
		this.fall.setBesitzer(null);
		this.fall.setMandant(new Mandant());
		this.fall.setNextNumberKind(1);

		SozialdienstFall sozialdienstFall = new SozialdienstFall();
		sozialdienstFall.setName("Ethan");
		sozialdienstFall.setVorname("Hunt");
		Adresse adresse = new Adresse();
		adresse.setStrasse("London Dienst Strasse");
		adresse.setPlz("1000");
		adresse.setLand(Land.CH);
		adresse.setOrt("Fribourg");
		adresse.setHausnummer("3a");
		sozialdienstFall.setAdresse(adresse);
		sozialdienstFall.setGeburtsdatum(LocalDate.now().minusYears(18));

		Sozialdienst sozialdienst = new Sozialdienst();
		sozialdienst.setName("Sozialdienst Stadt London");
		sozialdienstFall.setSozialdienst(sozialdienst);

		this.fall.setSozialdienstFall(sozialdienstFall);
	}
}
