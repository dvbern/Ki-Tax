/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.api.util.health;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import ch.dvbern.oss.healthcheck.gc.GCHealthCheck;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheckResponse;

@ApplicationScoped
@Health
public class DVHealthCheck implements org.eclipse.microprofile.health.HealthCheck {

	private static final int MAX_GC_TIME_PERCETAGE = 20;
	private GCHealthCheck gcHealthCheck = null;

	@Produces
	public GCHealthCheck produceGCHealthCheck() {
		return gcHealthCheck;
	}

	@PostConstruct
	public void init() {
		gcHealthCheck = GCHealthCheck.init(MAX_GC_TIME_PERCETAGE);
	}

	@Override
	public HealthCheckResponse call() {
		final boolean gcHealthy = gcHealthCheck.current().isHealthy();
		return HealthCheckResponse.builder().name("dv-gc-health-check")
			.withData("gc healthy", gcHealthy)
			.withData("gcTimeInPercent", String.valueOf(gcHealthCheck.current().getGcTimeInPercent()))
			.withData("accessTimeMillis", String.valueOf(gcHealthCheck.current().getAccessTimeMillis()))
			.state(gcHealthy)
			.build();
	}
}
