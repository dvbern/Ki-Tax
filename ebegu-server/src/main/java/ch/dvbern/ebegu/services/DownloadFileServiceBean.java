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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.entities.DownloadFile_;
import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.enums.TokenLifespan;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer den Download von Dokumenten
 */
@Stateless
@Local(DownloadFileService.class)
public class DownloadFileServiceBean implements DownloadFileService {

	private static final Logger LOG = LoggerFactory.getLogger(DownloadFileServiceBean.class);

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private FileSaverService fileSaverService;


	@Nonnull
	@Override
	public DownloadFile create(@Nonnull FileMetadata fileMetadata, @Nonnull String ip) {
		requireNonNull(fileMetadata);
		requireNonNull(ip);

		return persistence.persist(new DownloadFile(fileMetadata, ip));
	}

	@Nonnull
	@Override
	public DownloadFile create(@Nonnull UploadFileInfo fileInfo, @Nonnull TokenLifespan lifespan, @Nonnull String ip) {
		requireNonNull(fileInfo);
		requireNonNull(lifespan);
		requireNonNull(ip);
		final DownloadFile downloadFile = new DownloadFile(fileInfo, ip);
		downloadFile.setLifespan(lifespan);
		return persistence.persist(downloadFile);
	}

	@Nullable
	@Override
	public DownloadFile getDownloadFileByAccessToken(@Nonnull String accessToken) {
		requireNonNull(accessToken);

		Optional<DownloadFile> tempDokumentOptional = criteriaQueryHelper.getEntityByUniqueAttribute(DownloadFile.class, accessToken, DownloadFile_.accessToken);

		if (!tempDokumentOptional.isPresent()) {
			return null;
		}
		DownloadFile downloadFile = tempDokumentOptional.get();

		if (isFileDownloadExpired(downloadFile)) {
			return null;
		}
		return downloadFile;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void cleanUp() {
		deleteShortTermAccessTokens();
		deleteLongTermAccessTokens();
		// Auch die physischen Files loeschen
		fileSaverService.deleteAllFilesInTempReportsFolder();
		fileSaverService.deleteAllFilesInTempNotverordnungFolder();
	}

	private void deleteShortTermAccessTokens() {
		LocalDateTime deleteOlderThan = LocalDateTime.now().minus(Constants.MAX_SHORT_TEMP_DOWNLOAD_AGE_MINUTES, ChronoUnit.MINUTES);
		LOG.debug("Deleting {} ShortTerm TempDocuments before {}", TokenLifespan.SHORT, deleteOlderThan);

		try {
			requireNonNull(deleteOlderThan);
			int result = this.deleteAllTokensBefore(DownloadFile.class, TokenLifespan.SHORT, deleteOlderThan);
			LOG.info("... Deleteted {} ShortTerm TempDocuments", result);
		} catch (RuntimeException rte) {
			// timer methods may not throw exceptions or the timer will get cancelled (as per spec)
			String msg = "Unexpected error while deleting old TempDocuments";
			LOG.error(msg, rte);
		}
	}

	private void deleteLongTermAccessTokens() {
		LocalDateTime deleteOlderThan = LocalDateTime.now().minus(Constants.MAX_LONGER_TEMP_DOWNLOAD_AGE_MINUTES, ChronoUnit.MINUTES);
		LOG.debug("Deleting {} LongTerm TempDocuments before {}", TokenLifespan.LONG, deleteOlderThan);

		try {
			requireNonNull(deleteOlderThan);
			int result = this.deleteAllTokensBefore(DownloadFile.class, TokenLifespan.LONG, deleteOlderThan);
			LOG.info("... Deleteted {} LongTerm TempDocuments", result);
		} catch (RuntimeException rte) {
			// timer methods may not throw exceptions or the timer will get cancelled (as per spec)
			String msg = "Unexpected error while deleting old TempDocuments";
			LOG.error(msg, rte);
		}
	}

	private <T extends DownloadFile> int deleteAllTokensBefore(@Nonnull Class<T> entityClazz, @Nonnull TokenLifespan lifespan, @Nonnull LocalDateTime before) {
		checkNotNull(entityClazz);
		checkNotNull(before);

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaDelete<T> delete = cb.createCriteriaDelete(entityClazz);
		Root<T> root = delete.from(entityClazz);

		ParameterExpression<LocalDateTime> beforeParam = cb.parameter(LocalDateTime.class, "before");
		Predicate timePred = cb.lessThan(root.get(AbstractDateRangedEntity_.timestampMutiert), beforeParam);

		ParameterExpression<TokenLifespan> lifespanParam = cb.parameter(TokenLifespan.class, "type");
		root.get(DownloadFile_.ip);
		Predicate lifespanPred = cb.equal(root.get(DownloadFile_.lifespan), lifespanParam);

		delete.where(timePred, lifespanPred);
		Query query = persistence.getEntityManager().createQuery(delete);
		query.setParameter(beforeParam, before);
		query.setParameter(lifespanParam, lifespan);
		return query.executeUpdate();
	}

	/**
	 * Access Token fuer Download ist nur fuer eine bestimmte Zeitspanne (3Min) gueltig
	 */
	private boolean isFileDownloadExpired(@Nonnull DownloadFile tempBlob) {
		LocalDateTime timestampMutiert = checkNotNull(tempBlob.getTimestampMutiert());
		if (tempBlob.getLifespan() == TokenLifespan.SHORT) {
			return timestampMutiert.isBefore(LocalDateTime.now().minus(Constants.MAX_SHORT_TEMP_DOWNLOAD_AGE_MINUTES, ChronoUnit.MINUTES));
		}
		return timestampMutiert.isBefore(LocalDateTime.now().minus(Constants.MAX_LONGER_TEMP_DOWNLOAD_AGE_MINUTES, ChronoUnit.MINUTES));
	}
}
