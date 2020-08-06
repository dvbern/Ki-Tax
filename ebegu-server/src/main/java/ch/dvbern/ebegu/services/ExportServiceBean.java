/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.dto.dataexport.v1.ExportConverter;
import ch.dvbern.ebegu.dto.dataexport.v1.VerfuegungenExportDTO;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.UploadFileInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static java.util.Objects.requireNonNull;

@Stateless
@Local(ExportService.class)
public class ExportServiceBean implements ExportService {

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private ExportConverter exportConverter;

	@Override
	public UploadFileInfo exportVerfuegungOfBetreuungAsFile(String betreuungID) {
		Betreuung betreuung = readBetreuung(betreuungID);

		VerfuegungenExportDTO verfuegungenExportDTO = convertBetreuungToExport(betreuung);

		byte[] bytes = convertToJson(verfuegungenExportDTO);
		String filename = "export_" + betreuung.getBGNummer() + ".json";
		String gesuchId = betreuung.extractGesuch().getId();

		return this.fileSaverService.save(bytes, filename, gesuchId, getContentTypeForExport());
	}

	@Nonnull
	private Betreuung readBetreuung(@Nonnull String betreuungID) {
		requireNonNull(betreuungID, "betreuungID muss gesetzt sein");

		Betreuung betreuung = betreuungService.findBetreuung(betreuungID)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"exportVerfuegungOfBetreuung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				betreuungID));

		authorizer.checkReadAuthorization(betreuung);

		return betreuung;
	}

	@Nonnull
	private VerfuegungenExportDTO convertBetreuungToExport(@Nonnull Betreuung betreuung) {
		List<Verfuegung> verfuegungToExport = new ArrayList<>();
		if (betreuung.getVerfuegung() != null) {
			//single element in list to export
			verfuegungToExport.add(betreuung.getVerfuegung());
		}

		return exportConverter.createVerfuegungenExportDTO(verfuegungToExport);
	}

	@Nonnull
	private MimeType getContentTypeForExport() {
		try {
			return new MimeType(MediaType.TEXT_PLAIN);
		} catch (MimeTypeParseException e) {
			throw new EbeguRuntimeException(
				"getContentTypeForExport",
				"could not parse mime type",
				e,
				MediaType.TEXT_PLAIN);
		}
	}

	/**
	 * convert the dto as json
	 */
	private byte[] convertToJson(@Nonnull VerfuegungenExportDTO verfuegungenExportDTO) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		try {
			return mapper.writeValueAsBytes(verfuegungenExportDTO);
		} catch (JsonProcessingException e) {
			throw new EbeguRuntimeException(
				"convertToJson",
				"Objekt kann nicht JSON konvertiert werden",
				e,
				"Objekt kann nicht JSON konvertiert werden");
		}
	}
}
