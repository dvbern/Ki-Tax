/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.EJBAccessException;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.entities.Workjob_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.WorkJobConstants.BETRAG_PRO_KIND;
import static ch.dvbern.ebegu.enums.WorkJobConstants.DATE_FROM_PARAM;
import static ch.dvbern.ebegu.enums.WorkJobConstants.DATE_TO_PARAM;
import static ch.dvbern.ebegu.enums.WorkJobConstants.DO_SAVE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.INKL_BG_GESUCHE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.INKL_MISCH_GESUCHE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.INKL_TS_GESUCHE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.LANGUAGE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.OHNE_ERNEUERUNGSGESUCHE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.REPORT_VORLAGE_TYPE_PARAM;
import static ch.dvbern.ebegu.enums.WorkJobConstants.TEXT;

/**
 * Data Acess Object Bean zum zugriff auf Workjoben in der DB
 */
@Stateless
@Local(WorkjobService.class)
public class WorkjobServiceBean extends AbstractBaseService implements WorkjobService {

	private static final Logger LOG = LoggerFactory.getLogger(WorkjobServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private PrincipalBean principalBean;


	public WorkjobServiceBean() {

	}

	@Nonnull
	@Override
	public Workjob saveWorkjob(@Nonnull Workjob workJob) {
		return persistence.merge(workJob);
	}

	@Nullable
	@Override
	public Workjob findWorkjobByWorkjobID(@Nullable final String workJobId) {
		if (workJobId == null) {
			return null;
		}
		return persistence.find(Workjob.class, workJobId);
	}

	@Nullable
	@Override
	public Workjob findWorkjobByExecutionId(@Nonnull final Long executionId) {

		final Collection<Workjob> entitiesByAttribute = criteriaQueryHelper.getEntitiesByAttribute(Workjob.class, executionId, Workjob_.executionId);
		final Optional<Workjob> first = entitiesByAttribute
			.stream()
			.filter(workjob -> workjob.getTimestampErstellt() != null)
			.max(Comparator.comparing(Workjob::getTimestampErstellt));
		return first.orElse(null);
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
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
		@Nullable String text,
		@Nonnull Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			datumVon,
			datumBis,
			gesuchPeriodIdParam,
			null,
			inklBgGesuche,
			inklMischGesuche,
			inklTsGesuche,
			ohneErneuerungsgesuch,
			gemeinde,
			text,
			false,
			BigDecimal.ZERO,
			locale);
	}

	@Nonnull
	private Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable String gesuchPeriodIdParam,
		@Nullable String stammdatenIdParam,
		boolean inklBgGesuche,
		boolean inklMischGesuche,
		boolean inklTsGesuche,
		boolean ohneErneuerungsgesuch,
		@Nullable Gemeinde gemeinde,
		@Nullable String text,
		boolean doSave,
		@Nonnull BigDecimal betragProKind,
		@Nonnull Locale locale
	) {
		checkIfJobCreationAllowed(workJob, vorlage);

		JobOperator jobOperator = BatchRuntime.getJobOperator();
		final Properties jobParameters = new Properties();
		final String datumVonString;
		if (datumVon != null) {
			datumVonString = Constants.SQL_DATE_FORMAT.format(datumVon);
			jobParameters.setProperty(DATE_FROM_PARAM, datumVonString);
		}
		final String datumBisString;
		if (datumBis != null) {
			datumBisString = Constants.SQL_DATE_FORMAT.format(datumBis);
			jobParameters.setProperty(DATE_TO_PARAM, datumBisString);
		}

		jobParameters.setProperty(INKL_BG_GESUCHE, String.valueOf(inklBgGesuche));
		jobParameters.setProperty(INKL_MISCH_GESUCHE, String.valueOf(inklMischGesuche));
		jobParameters.setProperty(INKL_TS_GESUCHE, String.valueOf(inklTsGesuche));
		jobParameters.setProperty(OHNE_ERNEUERUNGSGESUCHE, String.valueOf(ohneErneuerungsgesuch));
		if (StringUtils.isNotEmpty(text)) {
			jobParameters.setProperty(TEXT, text);
		}
		jobParameters.setProperty(DO_SAVE, String.valueOf(doSave));
		jobParameters.setProperty(BETRAG_PRO_KIND, String.valueOf(betragProKind));
		jobParameters.setProperty(REPORT_VORLAGE_TYPE_PARAM, vorlage.name());
		jobParameters.setProperty(LANGUAGE, locale.getLanguage());

		setPropertyIfPresent(jobParameters, WorkJobConstants.GESUCH_PERIODE_ID_PARAM, gesuchPeriodIdParam);
		setPropertyIfPresent(jobParameters, WorkJobConstants.STAMMDATEN_ID_PARAM, stammdatenIdParam);
		if (gemeinde != null) {
			jobParameters.setProperty(WorkJobConstants.GEMEINDE_ID_PARAM, gemeinde.getId());
		}
		jobParameters.setProperty(WorkJobConstants.EMAIL_OF_USER, principalBean.getBenutzer().getEmail());
		jobOperator.getJobNames();
		workJob.setStatus(BatchJobStatus.REQUESTED);
		workJob = this.saveWorkjob(workJob);
		persistence.getEntityManager().flush(); //so we can actually set state to running using an update script in the job-listener
		long executionId = jobOperator.start("reportbatch", jobParameters);

		this.persistence.getEntityManager().refresh(workJob); //evtl hat job schon gestartet
		workJob.setExecutionId(executionId);
		workJob = this.saveWorkjob(workJob);

		LOG.debug("Startet GesuchStichttagStatistik with executionId {}", executionId);

		return workJob;
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable String gesuchPeriodIdParam,
		@Nonnull Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			datumVon,
			datumBis,
			gesuchPeriodIdParam,
			false,
			false,
			false,
			false,
			null, null,
			locale);
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(@Nonnull Workjob workJob, @Nonnull ReportVorlage vorlage, boolean doSave, @Nonnull BigDecimal betragProKind,
		@Nonnull Locale locale) {
		return createNewReporting(
			workJob,
			vorlage,
			null,
			null,
			null,
			null,
			false,
			false,
			false,
			false,
			null,
			null,
			doSave,
			betragProKind,
			locale);
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nonnull String stammdatenId,
		@Nonnull String gesuchsperiodeId,
		@Nonnull Locale locale) {
		return createNewReporting(
			workJob,
			vorlage,
			null,
			null,
			gesuchsperiodeId,
			stammdatenId,
			false,
			false,
			false,
			false,
			null,
			null,
			false,
			BigDecimal.ZERO,
			locale);
	}

	private void setPropertyIfPresent(@Nonnull Properties jobParameters, @Nonnull String paramName, @Nullable String paramValue) {
		if (paramValue != null) {
			jobParameters.setProperty(paramName, paramValue);
		}
	}

	private void checkIfJobCreationAllowed(@Nonnull Workjob workJob, ReportVorlage vorlage) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		if (!ReportVorlage.checkAllowed(userRole, vorlage)) {
			throw new EJBAccessException(
				"Access Violation"
					+ " for Report: " + vorlage
					+ " for current user: " + principalBean.getPrincipal()
					+ " in role(s): " + userRole);
		}
		Set<BatchJobStatus> statesToSearch = Sets.newHashSet(BatchJobStatus.REQUESTED, BatchJobStatus.RUNNING);

		final List<Workjob> openWorkjobs = this.findWorkjobs(principalBean.getPrincipal().getName(), statesToSearch);
		final boolean alreadyQueued = openWorkjobs.stream().anyMatch(workJob::isSame);
		if (alreadyQueued) {
			String messsage = String.format("An identical Workjob was already queued by this user; %s ", workJob);
			throw new EbeguRuntimeException(KibonLogLevel.INFO, "checkIfJobCreationAllowed", messsage, ErrorCodeEnum.ERROR_JOB_ALREADY_EXISTS);
		}
	}


	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void removeOldWorkjobs() {
		LocalDateTime cutoffDate = LocalDateTime.now().minusMinutes(Constants.MAX_LONGER_TEMP_DOWNLOAD_AGE_MINUTES);
		int i = this.criteriaQueryHelper.deleteAllBefore(Workjob.class, cutoffDate);
		LOG.info("... deleted {} + workjobs", i);
	}

	@Nonnull
	@Override
	public List<Workjob> findWorkjobs(@Nonnull String startingUserName, @Nonnull Set<BatchJobStatus> statesToSearch) {
		Objects.requireNonNull(startingUserName, "username to search must be set");
		Objects.requireNonNull(statesToSearch, "statesToSearch  must be set");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Workjob> query = cb.createQuery(Workjob.class);
		Root<Workjob> root = query.from(Workjob.class);

		ParameterExpression<String> startingUsernameParam = cb.parameter(String.class, "startingUsernameParam");
		Predicate userPredicate = cb.equal(root.get(Workjob_.startinguser), startingUsernameParam);

		ParameterExpression<Collection> statusParam = cb.parameter(Collection.class, "statusParam");
		Predicate statusPredicate = root.get(Workjob_.status).in(statusParam);

		query.where(userPredicate, statusPredicate);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampMutiert)));
		TypedQuery<Workjob> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(startingUsernameParam, startingUserName);
		q.setParameter(statusParam, statesToSearch);

		return q.getResultList();
	}

	@Nonnull
	@Override
	public List<Workjob> findUnfinishedWorkjobs() {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Workjob> query = cb.createQuery(Workjob.class);
		Root<Workjob> root = query.from(Workjob.class);

		ParameterExpression<Collection> statusParam = cb.parameter(Collection.class, "statusParam");
		Predicate statusPredicate = root.get(Workjob_.status).in(statusParam);

		query.where(statusPredicate);
		TypedQuery<Workjob> q = persistence.getEntityManager().createQuery(query);
		Collection<BatchJobStatus> statesToSearch =  Sets.newHashSet(BatchJobStatus.RUNNING, BatchJobStatus.REQUESTED);
		q.setParameter(statusParam, statesToSearch);

		return q.getResultList();
	}

	@Nonnull
	@Override
	public List<Workjob> findAllWorkjobs() {
		final Collection<Workjob> all = criteriaQueryHelper.getAll(Workjob.class);
		return new ArrayList<>(all);
	}

	@Override
	public void changeStateOfWorkjob(long executionId, @Nonnull BatchJobStatus status) {
		persistence.getEntityManager().createNamedQuery(Workjob.Q_WORK_JOB_STATE_UPDATE)
			.setParameter("exId", executionId)
			.setParameter("status", status)
			.executeUpdate();
	}

	@Override
	public void addResultToWorkjob(@Nonnull String workjobID, @Nonnull String resultData) {
		Objects.requireNonNull(resultData);

		CriteriaBuilder cb = this.persistence.getCriteriaBuilder();
		CriteriaUpdate<Workjob> updateQuery = cb.createCriteriaUpdate(Workjob.class);
		// query to setResultData into the workjob. the result is the accessToken of the download file
		Root<Workjob> root = updateQuery.from(Workjob.class);
		updateQuery.set(root.get(Workjob_.resultData), resultData);
		updateQuery.where(cb.equal(root.get(AbstractEntity_.id), workjobID));
		this.persistence.getEntityManager().createQuery(updateQuery).executeUpdate();
	}

	@Override
	public void removeWorkjob(Workjob workjob) {
		this.persistence.remove(workjob);

	}
}
