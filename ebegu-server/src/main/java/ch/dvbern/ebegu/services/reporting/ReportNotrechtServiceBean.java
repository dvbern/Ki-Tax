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

package ch.dvbern.ebegu.services.reporting;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.enums.RueckforderungInstitutionTyp;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.reporting.ReportNotrechtService;
import ch.dvbern.ebegu.reporting.notrecht.NotrechtDataRow;
import ch.dvbern.ebegu.reporting.notrecht.NotrechtExcelConverter;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.RueckforderungFormularService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.ejb3.annotation.TransactionTimeout;

@Stateless
@Local(ReportNotrechtService.class)
public class ReportNotrechtServiceBean extends AbstractReportServiceBean implements ReportNotrechtService {

	private NotrechtExcelConverter excelConverter = new NotrechtExcelConverter();

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private RueckforderungFormularService rueckforderungFormularService;


	@Nonnull
	@Override
	public List<NotrechtDataRow> getReportNotrecht(boolean zahlungenAusloesen) {
		List<NotrechtDataRow> formulare = new ArrayList<>();
		Collection<RueckforderungFormular> auszuzahlendeFormulare = findAllAuszuzahlendeFormulare();
		for (RueckforderungFormular rueckforderungFormular : auszuzahlendeFormulare) {
			if (zahlungenAusloesen) {
				rueckforderungFormular.handleAuszahlungIfNecessary();
			}
			NotrechtDataRow dataRow = convertToDataRow(rueckforderungFormular);
			formulare.add(dataRow);
		}
		return formulare;
	}

	@Nonnull
	@SuppressWarnings("PMD.NcssMethodCount")
	private NotrechtDataRow convertToDataRow(@Nonnull RueckforderungFormular formular) {
		NotrechtDataRow row = new NotrechtDataRow();
		InstitutionStammdaten stammdaten = formular.getInstitutionStammdaten();
		Institution institution = stammdaten.getInstitution();

		row.setInstitution(institution.getName());
		row.setStatus(formular.getStatus());
		row.setBetreuungsangebotTyp(stammdaten.getBetreuungsangebotTyp());
		row.setTraegerschaft(institution.getTraegerschaft() != null ? institution.getTraegerschaft().getName() : null);
		row.setEmail(stammdaten.getMail());

		Adresse adresse = stammdaten.getAdresse();
		row.setAdresseOrganisation(adresse.getOrganisation());
		row.setAdresseStrasse(adresse.getStrasse());
		row.setAdresseHausnummer(adresse.getHausnummer());
		row.setAdressePlz(adresse.getPlz());
		row.setAdresseOrt(adresse.getOrt());

		row.setTelefon(stammdaten.getTelefon());

		row.setStufe1InstitutionKostenuebernahmeAnzahlTage(printBigDecimal(formular.getStufe1InstitutionKostenuebernahmeAnzahlTage()));
		row.setStufe1InstitutionKostenuebernahmeAnzahlStunden(printBigDecimal(formular.getStufe1InstitutionKostenuebernahmeAnzahlStunden()));
		row.setStufe1InstitutionKostenuebernahmeBetreuung(printBigDecimal(formular.getStufe1InstitutionKostenuebernahmeBetreuung()));
		row.setStufe1KantonKostenuebernahmeAnzahlTage(printBigDecimal(formular.getStufe1KantonKostenuebernahmeAnzahlTage()));
		row.setStufe1KantonKostenuebernahmeAnzahlStunden(printBigDecimal(formular.getStufe1KantonKostenuebernahmeAnzahlStunden()));
		row.setStufe1KantonKostenuebernahmeBetreuung(printBigDecimal(formular.getStufe1KantonKostenuebernahmeBetreuung()));

		row.setStufe1FreigabeBetrag(printBigDecimal(formular.getStufe1FreigabeBetrag()));
		row.setStufe1FreigabeDatum(formular.getStufe1FreigabeDatum());
		row.setStufe1FreigabeAusbezahltAm(formular.getStufe1FreigabeAusbezahltAm());
		row.setStufe1ZahlungJetztAusgeloest((formular.isStufe1ZahlungJetztAusgeloest()) ? "Ja" : "-");

		row.setInstitutionTyp(formular.getInstitutionTyp());

		if (formular.getInstitutionTyp() == null || formular.getInstitutionTyp() == RueckforderungInstitutionTyp.OEFFENTLICH) {
			row.setStufe2InstitutionKostenuebernahmeAnzahlTage(printBigDecimal(formular.getStufe2InstitutionKostenuebernahmeAnzahlTage()));
			row.setStufe2InstitutionKostenuebernahmeAnzahlStunden(printBigDecimal(formular.getStufe2InstitutionKostenuebernahmeAnzahlStunden()));
			row.setStufe2InstitutionKostenuebernahmeBetreuung(printBigDecimal(formular.getStufe2InstitutionKostenuebernahmeBetreuung()));
			row.setStufe2KantonKostenuebernahmeAnzahlTage(printBigDecimal(formular.getStufe2KantonKostenuebernahmeAnzahlTage()));
			row.setStufe2KantonKostenuebernahmeAnzahlStunden(printBigDecimal(formular.getStufe2KantonKostenuebernahmeAnzahlStunden()));
			row.setStufe2KantonKostenuebernahmeBetreuung(printBigDecimal(formular.getStufe2KantonKostenuebernahmeBetreuung()));
		} else {
			row.setBetragEntgangeneElternbeitraege(printBigDecimal(formular.getBetragEntgangeneElternbeitraege()));
			row.setBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(printBigDecimal(formular.getBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten()));
			row.setRueckerstattungNichtAngeboteneBetreuungstage(printBigDecimal(formular.getAnzahlNichtAngeboteneEinheiten()));
			row.setKurzarbeitBetrag(printBigDecimal(formular.getKurzarbeitBetrag()));
			row.setCoronaErwerbsersatzBetrag(printBigDecimal(formular.getCoronaErwerbsersatzBetrag()));
		}

		row.setStufe2VerfuegungBetrag(printBigDecimal(formular.getStufe2VerfuegungBetrag()));
		row.setStufe2VerfuegungDatum(formular.getStufe2VerfuegungDatum());
		row.setStufe2VerfuegungAusbezahltAm(formular.getStufe2VerfuegungAusbezahltAm());
		row.setStufe2ZahlungJetztAusgeloest((formular.isStufe2ZahlungJetztAusgeloest()) ? "Ja" : "-");

		row.setBeschwerdeBetrag(formular.getBeschwerdeBetrag());
		row.setBeschwerdeAusbezahltAm(formular.getBeschwerdeAusbezahltAm());
		row.setBeschwerdeZahlungJetztAusgeloest(formular.isBeschwerdeZahlungJetztAusgeloest() ? "Ja" : "-");

		InstitutionStammdatenBetreuungsgutscheine stammdatenBetreuungsgutscheine = stammdaten.getInstitutionStammdatenBetreuungsgutscheine();
		if (stammdatenBetreuungsgutscheine != null) {
			final IBAN extractIban = stammdatenBetreuungsgutscheine.extractIban();
			if (extractIban != null) {
				row.setIban(extractIban.getIban());
			}
			row.setKontoinhaber(stammdatenBetreuungsgutscheine.extractKontoinhaber());

			Adresse zahlungsadresse = stammdatenBetreuungsgutscheine.extractAdresseKontoinhaber();
			if (zahlungsadresse == null) {
				zahlungsadresse = stammdaten.getAdresse();
			}
			row.setAuszahlungOrganisation(zahlungsadresse.getOrganisation());
			row.setAuszahlungStrasse(zahlungsadresse.getStrasse());
			row.setAuszahlungHausnummer(zahlungsadresse.getHausnummer());
			row.setAuszahlungPlz(zahlungsadresse.getPlz());
			row.setAuszahlungOrt(zahlungsadresse.getOrt());
		}
		return row;
	}

	@Nonnull
	private Collection<RueckforderungFormular> findAllAuszuzahlendeFormulare() {
		return rueckforderungFormularService.getRueckforderungFormulareForCurrentBenutzer();
	}

	@Nonnull
	private BigDecimal printBigDecimal(@Nullable BigDecimal input) {
		if (input == null) {
			return BigDecimal.ZERO;
		}
		return input;
	}

	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportNotrecht(boolean zahlungenAusloesen) throws ExcelMergeException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_NOTRECHT;

		InputStream is = ReportNotrechtServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<NotrechtDataRow> reportData = getReportNotrecht(zahlungenAusloesen);
		ExcelMergerDTO excelMergerDTO = excelConverter.toExcelMergerDTO(reportData, zahlungenAusloesen);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		excelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			ServerMessageUtil.translateEnumValue(reportVorlage.getDefaultExportFilename(), Locale.GERMAN) + ".xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}
}
