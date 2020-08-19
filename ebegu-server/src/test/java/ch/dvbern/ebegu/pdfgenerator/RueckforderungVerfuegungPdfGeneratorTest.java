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

package ch.dvbern.ebegu.pdfgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.enums.Land;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class RueckforderungVerfuegungPdfGeneratorTest {

	private String pfad = FileUtils.getTempDirectoryPath() + "/generated/";
	private InstitutionStammdaten institutionStammdaten;
	private RueckforderungFormular rueckforderungFormular;

	@Before
	public void init() throws IOException {
		this.institutionStammdaten = initInstitutionStammdaten();
		this.rueckforderungFormular = initRueckforderungFormular(this.institutionStammdaten);
		FileUtils.forceMkdir(new File(pfad));
	}

	@Test
	public void generateDefinitiveVerfuegung_negativ() throws IOException, InvoiceGeneratorException {
		this.rueckforderungFormular.setStufe2VerfuegungBetrag(BigDecimal.valueOf(500));

		this.rueckforderungFormular.setKorrespondenzSprache(Sprache.DEUTSCH);
		RueckforderungVerfuegungPdfGenerator generator =
			new RueckforderungVerfuegungPdfGenerator(this.rueckforderungFormular, "VerantwortlichePerson", "/temp/unterschrift.png");
		final String file_de = pfad + "notrecht_definitive_verfuegung_negativ_de.pdf";
		generator.generate(new FileOutputStream(file_de));
		System.out.println("PDF generated: " + file_de);

		this.rueckforderungFormular.setKorrespondenzSprache(Sprache.FRANZOESISCH);
		generator =
			new RueckforderungVerfuegungPdfGenerator(this.rueckforderungFormular, "VerantwortlichePerson", "/temp/unterschrift.png");
		final String file_fr = pfad + "notrecht_definitive_verfuegung_negativ_fr.pdf";
		System.out.println("PDF generated: " + file_fr);
		generator.generate(new FileOutputStream(file_fr));
	}

	@Test
	public void generateDefinitiveVerfuegung_positiv() throws IOException, InvoiceGeneratorException {
		this.rueckforderungFormular.setStufe2VerfuegungBetrag(BigDecimal.valueOf(50000));

		this.rueckforderungFormular.setKorrespondenzSprache(Sprache.DEUTSCH);
		RueckforderungVerfuegungPdfGenerator generator =
			new RueckforderungVerfuegungPdfGenerator(this.rueckforderungFormular, "VerantwortlichePerson", "/temp/unterschrift.png");
		final String file_de = pfad + "notrecht_definitive_verfuegung_positiv_de.pdf";
		generator.generate(new FileOutputStream(file_de));
		System.out.println("PDF generated: " + file_de);

		this.rueckforderungFormular.setKorrespondenzSprache(Sprache.FRANZOESISCH);
		generator =
			new RueckforderungVerfuegungPdfGenerator(this.rueckforderungFormular, "VerantwortlichePerson", "/temp/unterschrift.png");
		final String file_fr = pfad + "notrecht_definitive_verfuegung_positiv_fr.pdf";
		System.out.println("PDF generated: " + file_fr);
		generator.generate(new FileOutputStream(file_fr));
	}

	private InstitutionStammdaten initInstitutionStammdaten(){
		InstitutionStammdaten institutionStammdaten = new InstitutionStammdaten();
		Institution institution = new Institution();
		institution.setName("Kita kiBon");
		institutionStammdaten.setInstitution(institution);
		Adresse adresse = new Adresse();
		adresse.setOrt("Bern");
		adresse.setPlz("3000");
		adresse.setStrasse("Tester Strasse");
		adresse.setOrganisation("Kitabe");
		adresse.setLand(Land.CH);
		institutionStammdaten.setAdresse(adresse);
		institutionStammdaten.setMail("kitaBon@test.ch");
		return institutionStammdaten;
	}

	private RueckforderungFormular initRueckforderungFormular(InstitutionStammdaten institutionStammdaten){
		RueckforderungFormular rueckforderungFormular = new RueckforderungFormular();
		rueckforderungFormular.setStatus(RueckforderungStatus.VERFUEGT_PROVISORISCH);
		rueckforderungFormular.setStufe2VoraussichtlicheBetrag(new BigDecimal(5000.0));
		rueckforderungFormular.setStufe1FreigabeBetrag(new BigDecimal(4000.0));
		rueckforderungFormular.setInstitutionStammdaten(institutionStammdaten);
		rueckforderungFormular.setBemerkungFuerVerfuegung("Die Verfügungsbemerkung");

		return rueckforderungFormular;

	}
}
