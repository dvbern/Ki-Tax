/*
 * Copyright © 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.api.errors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import ch.dvbern.ebegu.api.util.version.VersionInfo;
import ch.dvbern.ebegu.api.util.version.VersionInfoBean;
import io.sentry.Sentry;
import io.sentry.SentryClient;

/**
 * Sentry is used to report errors to our Sentry instance
 * This class creates enhances the Sentry client with some meta information (i.e. Version Number of backend)
 */
@WebListener
public class SentryInitializer implements ServletContextListener {

	@Inject
	private VersionInfoBean versionInfoBean;

	@Override
	public void contextInitialized(@Nonnull ServletContextEvent sce) {
		SentryClient client = Sentry.getStoredClient();

		String version = versionInfoBean.getVersionInfo()
				.map(VersionInfo::getVersion)
				.orElse("unknown vers.");
		client.setRelease(version);
		Sentry.capture("Serverstart: Application Context for KI Bon initialized");
	}

	@Override
	public void contextDestroyed(@Nonnull ServletContextEvent sce) {
		Sentry.capture("Serverstop: Application Context for KI Bon destroyed");

	}
}
