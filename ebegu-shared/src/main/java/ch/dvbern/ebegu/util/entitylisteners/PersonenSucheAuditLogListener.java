/*
 * Copyright (C)  2020 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.util.entitylisteners;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.PersonensucheAuditLog;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonenSucheAuditLogListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(PersonenSucheAuditLogListener.class);

	private static PrincipalBean principalBean = null;

	@SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "Auch wenn das vlt. mehrfach initialisiert wird... das macht nix, solange am Ende was Richtiges drinsteht")
	private static PrincipalBean getPrincipalBean() {
		if (principalBean == null) {
			//FIXME: das ist nur ein Ugly Workaround, weil CDI-Injection (mal wieder) buggy ist.
			//noinspection NonThreadSafeLazyInitialization
			principalBean = CDI.current().select(PrincipalBean.class).get();
		}
		return principalBean;
	}

	@PrePersist
	protected void prePersist(@Nonnull PersonensucheAuditLog entity) {
		LocalDateTime now = LocalDateTime.now();
		entity.setTimestampResult(now);
		entity.setUsername(getPrincipalName());
	}

	private String getPrincipalName() {
		try {
			return getPrincipalBean().getPrincipal().getName();
		} catch (ContextNotActiveException e) {
			LOGGER.error("No context when persisting entity.");
			throw e;
		}
	}

	@PreUpdate
	protected void preUpdate(@Nonnull PersonensucheAuditLog entity) {
		throw new UnsupportedOperationException("Updates of PersonsnSucheAuditLog entries are forbidden");
	}
}
