/*
 * Copyright © 2017 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */
package ch.dvbern.ebegu.api.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.batch.runtime.JobExecution;
import javax.enterprise.context.RequestScoped;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.batch.JaxBatchJobInformation;
import ch.dvbern.ebegu.api.dtos.batch.JaxWorkJob;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.enums.WorkJobType;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;

import static com.google.common.base.Preconditions.checkNotNull;

@RequestScoped
public class BatchJaxBConverter {
	@Nullable
	private LocalDateTime mangleDate(@Nullable Date date) {
		if (date == null) {
			return null;
		}

		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	@Nonnull
	public JaxBatchJobInformation toBatchJobInformation(@Nonnull JobExecution information) {
		JaxBatchJobInformation jInformation = new JaxBatchJobInformation();

		jInformation.setBatchStatus(information.getBatchStatus().name());
		jInformation.setCreateTime(mangleDate(information.getCreateTime()));
		jInformation.setEndTime(mangleDate(information.getEndTime()));
		jInformation.setExecutionId(information.getExecutionId());
		jInformation.setExitStatus(information.getExitStatus());
		jInformation.setJobName(information.getJobName());
		jInformation.setLastUpdatedTime(mangleDate(information.getLastUpdatedTime()));
		jInformation.setStartTime(mangleDate(information.getStartTime()));
		jInformation.setLastUpdatedTime(mangleDate(information.getLastUpdatedTime()));

		return jInformation;
	}


	public JaxWorkJob toBatchJobInformation(Workjob job) {

		final WorkJobType workJobType = job.getWorkJobType();
		final String startinguser = job.getStartinguser();
		final BatchJobStatus status = job.getStatus();
		final String params = job.getParams();
		final Long executionId = job.getExecutionId();
		JaxWorkJob jaxWorkJob = new JaxWorkJob();
		convertAbstractFieldsToJAX(job, jaxWorkJob);

		jaxWorkJob.setWorkJobType(workJobType);
		jaxWorkJob.setStartinguser(startinguser);
		jaxWorkJob.setBatchJobStatus(status);
		jaxWorkJob.setParams(params);
		jaxWorkJob.setExecutionId(executionId);
		jaxWorkJob.setResultData(job.getResultData());
		jaxWorkJob.setRequestURI(job.getRequestURI());
		return jaxWorkJob;
	}

	@Nonnull
	private <T extends JaxAbstractDTO> T convertAbstractFieldsToJAX(@Nonnull final AbstractEntity abstEntity, final T jaxDTOToConvertTo) {
		jaxDTOToConvertTo.setTimestampErstellt(abstEntity.getTimestampErstellt());
		jaxDTOToConvertTo.setTimestampMutiert(abstEntity.getTimestampMutiert());
		jaxDTOToConvertTo.setId(checkNotNull(abstEntity.getId()));
		return jaxDTOToConvertTo;
	}
}
