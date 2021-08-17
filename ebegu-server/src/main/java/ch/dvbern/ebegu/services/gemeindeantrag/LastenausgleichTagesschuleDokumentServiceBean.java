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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.docxmerger.DocxDocument;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxDTO;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxMerger;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.Constants;

/**
 * Service fuer den Lastenausgleich der Tagesschulen
 */
@Stateless
@Local(LastenausgleichTagesschuleDokumentService.class)
public class LastenausgleichTagesschuleDokumentServiceBean extends AbstractBaseService
	implements LastenausgleichTagesschuleDokumentService {

	@Inject
	LastenausgleichTagesschuleAngabenGemeindeService lastenausgleichTagesschuleAngabenGemeindeService;

	@Inject
	Authorizer authorizer;

	@Inject
	PrincipalBean principalBean;

	@Inject
	GemeindeService gemeindeService;

	@Override
	@Nonnull
	public byte[] createDocx(@Nonnull String containerId, @Nonnull Sprache sprache) {
		LastenausgleichTagesschuleAngabenGemeindeContainer container =
			lastenausgleichTagesschuleAngabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(containerId)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"createDocx",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerId)
				);

		authorizer.checkReadAuthorization(container);

		final byte[] template = container.getGesuchsperiode().getVorlageVerfuegungLatsWithSprache(sprache);
		if (template.length == 0) {
			throw new EbeguRuntimeException(
				"createDocx",
				"LATS Template not found für Gesuchsperiode " + container.getGesuchsperiode().getGesuchsperiodeString() + " und Sprache " + sprache,
				ErrorCodeEnum.ERROR_LATS_VERFUEGUNG_TEMPLATE_NOT_FOUND,
				container.getGesuchsperiode().getGesuchsperiodeString(),
				sprache
			);
		}

		DocxDocument document = new DocxDocument(template);
		LatsDocxMerger merger = new LatsDocxMerger(document);
		merger.addMergeFields(toLatsDocxDTO(container));
		merger.merge();
		return document.getDocument();
	}

	@Nonnull
	private LatsDocxDTO toLatsDocxDTO(@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container) {

		Objects.requireNonNull(container.getAngabenKorrektur());
		LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde = container.getAngabenKorrektur();

		LatsDocxDTO dto = new LatsDocxDTO();
		dto.setUserName(this.principalBean.getBenutzer().getFullName());
		dto.setUserEmail(this.principalBean.getBenutzer().getEmail());

		GemeindeStammdaten stammdaten = this.gemeindeService.getGemeindeStammdatenByGemeindeId(container.getGemeinde().getId()).orElseThrow(() ->
			new EbeguEntityNotFoundException("toLatsDocxDTO", container.getGemeinde().getId())
		);

		dto.setGemeindeAnschrift(stammdaten.getAdresse().getOrganisation());
		dto.setGemeindeStrasse(stammdaten.getAdresse().getStrasse());
		dto.setGemeindeNr(stammdaten.getAdresse().getHausnummer());
		dto.setGemeindePLZ(stammdaten.getAdresse().getPlz());
		dto.setGemeindeOrt(stammdaten.getAdresse().getOrt());
		dto.setDateToday(Constants.DATE_FORMATTER.format(LocalDate.now()));
		dto.setGemeindeName(container.getGemeinde().getName());

		dto.setBetreuungsstunden(angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden());
		setNormlohnkosten(dto);
		dto.setNormlohnkostenTotal(angabenGemeinde.getNormlohnkostenBetreuungBerechnet());
		dto.setElterngebuehren(angabenGemeinde.getEinnahmenElterngebuehren());
		dto.setLastenausgleichsberechtigterBetrag(angabenGemeinde.getLastenausgleichsberechtigerBetrag());
		calculateAndSetRaten(dto);

		calculateAndSetPrognoseValues(dto);

		return dto;
	}

	private void calculateAndSetRaten(@Nonnull LatsDocxDTO dto) {

	}

	private void setNormlohnkosten(@Nonnull LatsDocxDTO dto) {

	}

	private void calculateAndSetPrognoseValues(@Nonnull LatsDocxDTO dto) {

	}
}


