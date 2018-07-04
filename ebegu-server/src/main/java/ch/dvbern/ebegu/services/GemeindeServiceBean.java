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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer Gemeinden
 */
@Stateless
@Local(GemeindeService.class)
@PermitAll
public class GemeindeServiceBean extends AbstractBaseService implements GemeindeService {

	private static final Logger LOG = LoggerFactory.getLogger(GemeindeServiceBean.class);

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;


	@Nonnull
	@Override
	public Optional<Gemeinde> findGemeinde(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Gemeinde gemeinde = persistence.find(Gemeinde.class, id);
		return Optional.ofNullable(gemeinde);
	}

	@Nonnull
	@Override
	public Gemeinde getFirst() {
		Collection<Gemeinde> gemeinden = criteriaQueryHelper.getAll(Gemeinde.class);
		if (gemeinden == null || gemeinden.isEmpty()) {
			LOG.error("Wir erwarten, dass mindestens eine Gemeinde bereits in der DB existiert");
			throw new EbeguRuntimeException("getFirst", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		}
		return gemeinden.iterator().next();
	}

	@Nonnull
	@Override
	public Collection<Gemeinde> getAllGemeinden() {
		return criteriaQueryHelper.getAll(Gemeinde.class);
	}
}
