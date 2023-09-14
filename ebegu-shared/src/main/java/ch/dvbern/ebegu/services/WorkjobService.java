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

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;
import ch.dvbern.ebegu.enums.reporting.DatumTyp;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;

/**
 * Service zum Verwalten von Workjobs
 */
public interface WorkjobService {

	@Nonnull
	Workjob saveWorkjob(@Nonnull Workjob workJob);

	@Nullable
	Workjob findWorkjobByWorkjobID(@Nonnull String workJobId);

	@Nullable
	Workjob findWorkjobByExecutionId(@Nonnull Long executionId);

	void removeOldWorkjobs();

	@Nonnull
	Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable String gesuchPeriodIdParam,
		boolean inklBgGesuche,
		boolean inklMischGesuche,
		boolean inklTsGesuche,
		boolean ohneErneuerungsgesuch,
		@Nullable Gemeinde gemeinde,
		@Nullable Institution institution,
		@Nullable Integer jahr,
		@Nullable String text,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	@Nonnull
	Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable String gesuchPeriodIdParam,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	@Nonnull
	Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable BigDecimal kantonSelbstbehalt,
		@Nullable String gesuchPeriodIdParam,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	@Nonnull
	Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		boolean doSave,
		@Nonnull BigDecimal betragProKind,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	@Nonnull
	Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nonnull String stammdatenId,
		@Nonnull String gesuchsperiodeId,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	@Nonnull
	Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nonnull DatumTyp datumTyp,
		@Nullable String gesuchPeriodIdParam,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	@Nonnull
	List<Workjob> findWorkjobs(@Nonnull String startingUserName, @Nonnull Set<BatchJobStatus> statesToSearch);

	/**
	 * gibt eine Liste aller Workjobs aus der DB zurueck
	 */
	@Nonnull
	List<Workjob> findAllWorkjobs();

	/**
	 * Gibt die Liste aller Workjobs zurueck die entweder fehlgeschlagen sind, oder noch nicht fertig sind
	 */
	@Nonnull
	List<Workjob> findUnfinishedWorkjobs();

	/**
	 * update query that changes state
	 */
	void changeStateOfWorkjob(long executionId,@Nonnull BatchJobStatus status);

	void addResultToWorkjob(@Nonnull String workjobID, @Nonnull String resultData);

	void removeWorkjob(Workjob workjob);

}
