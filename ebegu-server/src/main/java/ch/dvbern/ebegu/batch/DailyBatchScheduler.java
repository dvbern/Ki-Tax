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

package ch.dvbern.ebegu.batch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.services.DailyBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RunAs(UserRoleName.SUPER_ADMIN)
public class DailyBatchScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DailyBatchScheduler.class);

	@Inject
	private DailyBatch dailyBatch;

	@Schedule(second = "59", minute = "59", hour = "23", persistent = true)
	public void runBatchCleanDownloadFiles() {
		Future<Boolean> booleanFuture = dailyBatch.runBatchCleanDownloadFiles();
		try {
			Boolean resultat = booleanFuture.get();
			LOGGER.info("Batchjob CleanDownloadFiles durchgefuehrt mit Resultat: {}", resultat);
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Batch-Job CleanDownloadFiles konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Schedule(second = "59", minute = "58", hour = "23", persistent = true)
	public void runBatchCleanWorkjobs() {
		dailyBatch.runBatchCleanWorkjobs();
	}

	@Schedule(second = "59", minute = "00", hour = "01", persistent = true)
	public void runBatchMahnungFristablauf() {
		Future<Boolean> booleanFuture = dailyBatch.runBatchMahnungFristablauf();
		try {
			Boolean resultat = booleanFuture.get();
			LOGGER.info("Batchjob MahnungFristablauf durchgefuehrt mit Resultat: {}", resultat);
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Batch-Job Mahnung Fristablauf konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Schedule(second = "59", minute = "10", hour = "22", persistent = true)
	public void runBatchWarnungGesuchNichtFreigegeben() {
		dailyBatch.runBatchWarnungGesuchNichtFreigegeben();
	}

	@Schedule(second = "59", minute = "30", hour = "22", persistent = true)
	public void runBatchWarnungFreigabequittungFehlt() {
		dailyBatch.runBatchWarnungFreigabequittungFehlt();
	}

	@Schedule(second = "59", minute = "50", hour = "22", persistent = true)
	public void runBatchGesucheLoeschen() {
		dailyBatch.runBatchGesucheLoeschen();
	}

	@Schedule(second = "59", minute = "10", hour = "21", dayOfMonth = "1", month = "8", persistent = true)
	public void runBatchGesuchsperiodeLoeschen() {
		dailyBatch.runBatchGesuchsperiodeLoeschen();
	}

	@Schedule(second = "00", minute = "30", hour = "0", persistent = true)
	public void runBatchAbgelaufeneRollen() {
		dailyBatch.runBatchAbgelaufeneRollen();
	}

	@Schedule(second = "59", minute = "00", hour = "02", persistent = true)
	public void runBatchDeleteInvalidAuthTokens() {
		dailyBatch.runBatchDeleteInvalidAuthTokens();
	}

	@Schedule(second = "59", minute = "00", hour = "03", persistent = true)
	public void runBatchInfoOffenePendenzenInstitution() {
		dailyBatch.runBatchInfoOffenePendenzenInstitution();
	}

	@Schedule(second = "59", minute = "30", hour = "02", persistent = true)
	public void runBatchInstitutionCheckRequired() {
		dailyBatch.runBatchInstitutionCheckRequired();
	}

	@Schedule(second = "59", minute = "30", hour = "04", persistent = true)
	public void runBatchUpdateGemeindeForBGInstitutionen() {
		dailyBatch.runBatchUpdateGemeindeForBGInstitutionen();
	}

	@Schedule(second = "59", minute = "*", hour = "*", persistent = true)
	public void runBatchSendEmailsForNewGesuchsperiode() {
		dailyBatch.runBatchSendEmailsForNewGesuchsperiode();
	}
}
