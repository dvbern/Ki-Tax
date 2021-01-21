/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.sozialdienst;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.SozialdienstService;
import ch.dvbern.lib.cdipersistence.Persistence;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer Gemeinden
 */
@Stateless
@Local(SozialdienstService.class)
public class SozialdienstServiceBean extends AbstractBaseService implements SozialdienstService {

	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	public Sozialdienst saveSozialdienst(
		@Nonnull Sozialdienst sozialdienst) {
		requireNonNull(sozialdienst);

		if (sozialdienst.isNew()) {
			sozialdienst.setMandant(requireNonNull(principalBean.getMandant()));
		}
		return persistence.merge(sozialdienst);
	}

	@Nonnull
	@Override
	public Sozialdienst createSozialdienst(@Nonnull Sozialdienst sozialdienst) {
		Optional<Sozialdienst> sozialdienstOpt =
			criteriaQueryHelper.getEntityByUniqueAttribute(Sozialdienst.class, sozialdienst.getName(), Sozialdienst_.name);
		if (sozialdienstOpt.isPresent()) {
			throw new EntityExistsException(
				KibonLogLevel.INFO,
				Sozialdienst.class,
				"name",
				sozialdienst.getName(),
				ErrorCodeEnum.ERROR_DUPLICATE_SOZIALDIENST_NAME);
		}

		return saveSozialdienst(sozialdienst);
	}

	@Nonnull
	@Override
	public Optional<Sozialdienst> findSozialdienst(@Nonnull String id) {
		requireNonNull(id, "Sozialdienst id muss gesetzt sein");
		Sozialdienst sozialdienst = persistence.find(Sozialdienst.class, id);
		return Optional.ofNullable(sozialdienst);
	}

	@Nonnull
	@Override
	public Collection<Sozialdienst> getAllSozialdienste() {
		return criteriaQueryHelper.getAllOrdered(Sozialdienst.class, Sozialdienst_.name);
	}
}
