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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.ReportService;
import ch.dvbern.ebegu.reporting.mahlzeiten.MahlzeitenverguenstigungDataRow;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;

public class ReportMahlzeitenServiceBean extends AbstractReportServiceBean {

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private Persistence persistence;

	@Nonnull
	private List<MahlzeitenverguenstigungDataRow> getReportDataMahlzeitenverguenstigung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable Gesuchsperiode gesuchsperiode,
		@Nonnull Locale locale
	) {

		List<VerfuegungZeitabschnitt> zeitabschnittList = getReportDataMahlzeitenverguenstigung(datumVon, datumBis);
		List<MahlzeitenverguenstigungDataRow> dataRows = convertToMahlzeitDataRow(zeitabschnittList, locale);

		dataRows.sort(Comparator.comparing(MahlzeitenverguenstigungDataRow::getBgNummer)
			.thenComparing(MahlzeitenverguenstigungDataRow::getZeitabschnittVon));

		return dataRows;
	}

	private List<MahlzeitenverguenstigungDataRow> convertToMahlzeitDataRow(
		List<VerfuegungZeitabschnitt> zeitabschnittList,
		@Nonnull Locale locale
	) {

		List<MahlzeitenverguenstigungDataRow> dataRowList = new ArrayList<>();

		Map<Long, Gesuch> neustesVerfuegtesGesuchCache = new HashMap<>();

		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			MahlzeitenverguenstigungDataRow row =
				createRowForKinderReport(zeitabschnitt, neustesVerfuegtesGesuchCache, locale);
			dataRowList.add(row);
		}

		return dataRowList;
	}

	private MahlzeitenverguenstigungDataRow createRowForKinderReport(VerfuegungZeitabschnitt zeitabschnitt, Map<Long, Gesuch> neustesVerfuegtesGesuchCache, Locale locale) {
		return null;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Nonnull
	private List<VerfuegungZeitabschnitt> getReportDataMahlzeitenverguenstigung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis) {
		validateDateParams(datumVon, datumBis);

		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getReportDataMahlzeitenverguenstigung", NO_USER_IS_LOGGED_IN));

		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt
		// verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinBetreuungKindContainer = joinBetreuung.join(Betreuung_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinBetreuungGesuch = joinBetreuungKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinBetreuungDossier = joinBetreuungGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Gemeinde> joinBetreuungGemeinde = joinBetreuungDossier.join(Dossier_.gemeinde, JoinType.LEFT);

		Join<Verfuegung, AnmeldungTagesschule> joinAnmeldung = joinVerfuegung.join(Verfuegung_.anmeldungTagesschule);
		Join<AnmeldungTagesschule, KindContainer> joinAnmeldungKindContainer = joinAnmeldung.join(Betreuung_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinAnmeldungGesuch = joinAnmeldungKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinAnmeldungDossier = joinAnmeldungGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Gemeinde> joinAnmeldungGemeinde = joinAnmeldungDossier.join(Dossier_.gemeinde, JoinType.LEFT);


		List<Predicate> predicatesToUse = new ArrayList<>();

		// startAbschnitt <= datumBis && endeAbschnitt >= datumVon
		Path<DateRange> dateRangePath = root.get(AbstractDateRangedEntity_.gueltigkeit);
		Predicate predicateStart = builder.lessThanOrEqualTo(dateRangePath.get(DateRange_.gueltigAb), datumBis);
		predicatesToUse.add(predicateStart);
		Predicate predicateEnd = builder.greaterThanOrEqualTo(dateRangePath.get(DateRange_.gueltigBis), datumVon);
		predicatesToUse.add(predicateEnd);

		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = builder.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);
		predicatesToUse.add(predicateGueltig);

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		Collection<Gemeinde> gemeindenForBenutzer = user.extractGemeindenForUser();
		Predicate inGemeindeForBetreuung = joinBetreuungGemeinde.in(gemeindenForBenutzer);
		Predicate inGemeindeForTagesschule = joinAnmeldungGemeinde.in(gemeindenForBenutzer);
		Predicate inGemeinde = builder.or(inGemeindeForBetreuung, inGemeindeForTagesschule);
		predicatesToUse.add(inGemeinde);

		Predicate predicateForBenutzerRole = getPredicateForBenutzerRole(builder, root);
		if (predicateForBenutzerRole != null) {
			predicatesToUse.add(predicateForBenutzerRole);
		}
		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicatesToUse));
		return persistence.getCriteriaResults(query);
	}
}
