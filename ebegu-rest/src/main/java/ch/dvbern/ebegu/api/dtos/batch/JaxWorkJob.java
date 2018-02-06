/*
 * Copyright © 2017 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.api.dtos.batch;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.enums.WorkJobType;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;

import static com.google.common.base.Preconditions.checkNotNull;

@XmlRootElement(name = "batchJob")
public class JaxWorkJob extends JaxAbstractDTO {
	@Nonnull
	@NotNull
	private String name = "";


	private JaxBatchJobInformation execution;
	private WorkJobType workJobType;
	private String startinguser;
	private BatchJobStatus batchJobStatus;
	private String params;
	private Long executionId;
	private String resultData;
	private String requestURI;



	public JaxWorkJob(String name, JaxBatchJobInformation execution) {
		this.name = checkNotNull(name);
		this.execution = execution;
	}

	public JaxWorkJob() {
		// nop
	}

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = checkNotNull(name);
	}

	public JaxBatchJobInformation getExecution() {
		return execution;
	}

	public void setExecution(JaxBatchJobInformation execution) {
		this.execution = execution;
	}

	public void setWorkJobType(WorkJobType workJobType) {
		this.workJobType = workJobType;
	}

	public WorkJobType getWorkJobType() {
		return workJobType;
	}

	public void setStartinguser(String startinguser) {
		this.startinguser = startinguser;
	}

	public String getStartinguser() {
		return startinguser;
	}

	public void setBatchJobStatus(BatchJobStatus batchJobStatus) {
		this.batchJobStatus = batchJobStatus;
	}

	public BatchJobStatus getBatchJobStatus() {
		return batchJobStatus;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getParams() {
		return params;
	}

	public void setExecutionId(Long executionId) {
		this.executionId = executionId;
	}

	public Long getExecutionId() {
		return executionId;
	}


	public void setResultData(String resultData) {
		this.resultData = resultData;
	}

	public String getResultData() {
		return resultData;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public String getRequestURI() {
		return requestURI;
	}
}
