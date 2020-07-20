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


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguMailiciousContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

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

	public void scan (FileMetadata fileMetadata) {

		if (ebeguConfiguration.isClamavDisabled() || !isReady() || client == null) {
			return;
		}

		try {
			InputStream is = new FileInputStream(fileMetadata.getFilepfad());
			ScanResult result = client.scan(is);

			if (result instanceof ScanResult.VirusFound) {
				LOG.error("Malicious file detected at: {}", fileMetadata.getFilepfad());
				throw new EbeguMailiciousContentException("scan", ErrorCodeEnum.ERROR_MALICIOUS_CONTENT, fileMetadata.getFilepfad());
			}
		} catch (FileNotFoundException e) {
			throw new EbeguEntityNotFoundException("scan",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, fileMetadata.getId());
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
}
