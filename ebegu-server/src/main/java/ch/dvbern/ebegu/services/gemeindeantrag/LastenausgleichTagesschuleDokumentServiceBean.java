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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.docxmerger.DocxDocument;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxDTO;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxMerger;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;

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

		byte[] template = container.getGesuchsperiode().getVorlageVerfuegungLatsWithSprache(sprache);
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
		Objects.requireNonNull(container.getAngabenKorrektur().getLastenausgleichberechtigteBetreuungsstunden());

		assert container.getAngabenKorrektur().getLastenausgleichberechtigteBetreuungsstunden() != null;
		LatsDocxDTO dto = new LatsDocxDTO(
			container.getGemeinde().getName(),
			container.getAngabenKorrektur().getLastenausgleichberechtigteBetreuungsstunden()
		);

		return dto;
	}
}


