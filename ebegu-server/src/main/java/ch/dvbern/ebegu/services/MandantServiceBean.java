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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Cookie;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mandant_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer Mandanten
 */
@Stateless
@Local(MandantService.class)
public class MandantServiceBean extends AbstractBaseService implements MandantService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	public Optional<Mandant> findMandant(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Mandant a = persistence.find(Mandant.class, id);
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	public Optional<Mandant> findMandantByName(@Nonnull String name) {
		return criteriaQueryHelper.getEntityByUniqueAttribute(
				Mandant.class,
				name,
				Mandant_.name
		);
	}

	@Nonnull
	@Override
	public Mandant findMandantByCookie(@Nullable Cookie mandantCookie) {
		if (mandantCookie == null) {
			throw new EbeguRuntimeException("findMandantByCookie", ErrorCodeEnum.ERROR_MANDANT_COOKIE_IS_NULL);
		}
		var cookieDecoded = URLDecoder.decode(mandantCookie.getValue(), StandardCharsets.UTF_8);
		return findMandantByName(cookieDecoded)
			.orElseThrow(() -> {
				throw new EbeguEntityNotFoundException("findMandantByCookie", cookieDecoded);
			});
	}

	@Nonnull
	@Override
	public Mandant getDefaultMandant() {
		return findMandantByName("Kanton Bern").orElseThrow(()
			-> new EbeguRuntimeException("getDefaultMandant", "Kanton Bern Mandant not found"));
	}
}
