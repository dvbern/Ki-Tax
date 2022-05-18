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

package ch.dvbern.ebegu.api.av;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguMailiciousContentException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.commands.scan.result.ScanResult.VirusFound;

@Stateless
public class AVClient {

	private static final Logger LOG = LoggerFactory.getLogger(AVClient.class);

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Nullable
	private ClamavClient client;

	public void createClient(String host, int port) {
		try {
			client = new ClamavClient(host, port);
		} catch (ClamavException e) {
			LOG.warn("ClamAV is not responsible");
			client = null;
		}
	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	public void scan (@NonNull FileMetadata fileMetadata) {
		if (ebeguConfiguration.isClamavDisabled() || !isReady() || client == null) {
			return;
		}
		try {
			InputStream is = new FileInputStream(fileMetadata.getFilepfad());
			ScanResult result = client.scan(is);

			if (result instanceof ScanResult.VirusFound) {
				logFoundViruses((VirusFound) result, fileMetadata);
				throw new EbeguMailiciousContentException("scan", ErrorCodeEnum.ERROR_MALICIOUS_CONTENT, fileMetadata.getFilepfad());
			}
		} catch (FileNotFoundException e) {
			throw new EbeguEntityNotFoundException("scan",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, fileMetadata.getId());
		}
	}

	public void scan(byte[] content, @NonNull String info) {
		if (ebeguConfiguration.isClamavDisabled() || !isReady() || client == null) {
			return;
		}
		try (
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
		) {
			ScanResult result = client.scan(inputStream);
			if (result instanceof ScanResult.VirusFound) {
				logFoundViruses((VirusFound) result, info);
				throw new EbeguMailiciousContentException("scan", ErrorCodeEnum.ERROR_MALICIOUS_CONTENT, info);
			}
		} catch (IOException e) {
			throw new EbeguEntityNotFoundException("scan",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, info);
		}
	}

	private boolean isReady() {
		if (client == null) {
			createClient(ebeguConfiguration.getClamavHost(), ebeguConfiguration.getClamavPort());
		}

		try {
			client.ping();
			return true;
		} catch (Exception e) {
			LOG.warn("ClamAV seems not to be responsible at the moment", e);
			client = null;
			return false;
		}
	}

	private void logFoundViruses(@NonNull VirusFound result, @NonNull FileMetadata fileMetadata) {
		this.logFoundViruses(result, fileMetadata.getFilepfad());
	}

	private void logFoundViruses(@NonNull VirusFound result, @NonNull String description) {
		StringBuilder log = new StringBuilder("Malicious file detected at: ");
		log.append(description);
		for (Entry<String, Collection<String>> virus : result.getFoundViruses().entrySet()) {
			int count = 0;
			for (String info : virus.getValue()) {
				count++;
				log
					.append("\nVirus ")
					.append(count)
					.append(" signature: ")
					.append(info);
			}
		}
		LOG.error(log.toString());
	}
}
