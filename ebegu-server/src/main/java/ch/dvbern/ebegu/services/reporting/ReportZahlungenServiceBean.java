/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.reporting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsauftrag_;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.ReportZahlungenService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;

@Stateless
@Local(ReportZahlungenService.class)
public class ReportZahlungenServiceBean extends AbstractReportServiceBean implements ReportZahlungenService {

	@Inject
	private Persistence persistence;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	public UploadFileInfo generateExcelReportZahlungen(
		@Nonnull Locale locale,
		@Nonnull String gesuchsperiodeId,
		@Nullable String gemeindeId,
		@Nullable String institutionId
	) throws ExcelMergeException, IOException {
		var zahlungsauftrage = findZahlungsauftrageWithAuszahlungsTypInstitution(gesuchsperiodeId, gemeindeId);

		for (Zahlungsauftrag zahlungsauftrag : zahlungsauftrage) {
			authorizer.checkReadAuthorizationZahlungsauftrag(zahlungsauftrag);

			// DO STUFF WITH ZAHLUNGSAUFTRAG

			var zahlungen = zahlungsauftrag.getZahlungen();
			if (institutionId != null) {
				zahlungen = filterZahlungenByInstitution(zahlungen, institutionId);
			}

			for (Zahlung zahlung : zahlungen) {
				authorizer.checkReadAuthorizationZahlung(zahlung);
				// DO STUFF WITH ZAHLUNGEN
			}
		}
		return null;
	}

	private List<Zahlungsauftrag> findZahlungsauftrageWithAuszahlungsTypInstitution(
		@Nonnull String gesuchsperiodeId,
		@Nullable String gemeindeId
	) {

		var periode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("findZahlungsauftrage", gesuchsperiodeId));

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsauftrag> query = cb.createQuery(Zahlungsauftrag.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);
		Predicate predicateInPeriode = cb.between(
			root.get(Zahlungsauftrag_.timestampErstellt),
			periode.getGueltigkeit().getGueltigAb().atStartOfDay(),
			periode.getGueltigkeit().getGueltigBis().atStartOfDay()
		);
		predicates.add(predicateInPeriode);

		Predicate zahlungslaufTyp = cb.equal(root.get(Zahlungsauftrag_.zahlungslaufTyp), ZahlungslaufTyp.GEMEINDE_INSTITUTION);
		predicates.add(zahlungslaufTyp);

		if (gemeindeId != null) {
			var gemeinde = gemeindeService.findGemeinde(gemeindeId)
				.orElseThrow(() -> new EbeguRuntimeException("findZahlungsauftrage", gemeindeId));
			Predicate predicateGemeinde = cb.equal(root.get(Zahlungsauftrag_.gemeinde), gemeinde);
			predicates.add(predicateGemeinde);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	private List<Zahlung> filterZahlungenByInstitution(
		@Nonnull List<Zahlung> zahlungen,
		@Nonnull String institutionId
	) {
		return zahlungen.stream()
			.filter(z -> z.extractInstitution().getId().equals(institutionId))
			.collect(Collectors.toList());
	}


}
