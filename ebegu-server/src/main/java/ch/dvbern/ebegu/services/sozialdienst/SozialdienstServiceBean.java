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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstStammdaten;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstStammdaten_;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.BenutzerService;
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
	private BenutzerService benutzerService;

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
	@Transactional
	@Override
	public Sozialdienst createSozialdienst(@Nonnull String adminMail, @Nonnull Sozialdienst sozialdienst) {

		Sozialdienst persistedSozialdienst = saveSozialdienst(sozialdienst);

		final Mandant mandant = persistedSozialdienst.getMandant();

		final Benutzer benutzer = benutzerService.findBenutzer(adminMail, mandant)
			.map(b -> {
				if (b.getRole() != UserRole.GESUCHSTELLER) {
					// an existing user cannot be used to create a new Sozial / Unterstuetzung Dienst
					throw new EbeguRuntimeException(
						KibonLogLevel.INFO,
						"createSozialdienst",
						ErrorCodeEnum.EXISTING_USER_MAIL,
						adminMail);
				}
				return b;
			})
			.orElseGet(() -> benutzerService.createAdminSozialdienstByEmail(adminMail, persistedSozialdienst));

		benutzerService.einladen(Einladung.forSozialdienst(benutzer, persistedSozialdienst), mandant);

		return persistedSozialdienst;
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
	public Collection<Sozialdienst> getAllSozialdienste(Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Sozialdienst> query = cb.createQuery(Sozialdienst.class);
		Root<Sozialdienst> root = query.from(Sozialdienst.class);
		Predicate sameMandant = cb.equal(root.get(Sozialdienst_.mandant), mandant);
		query.orderBy(cb.asc(root.get(Sozialdienst_.name)));
		query.where(sameMandant);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Optional<SozialdienstStammdaten> getSozialdienstStammdaten(String id) {
		requireNonNull(id, "Sozialdienst Stammdaten id muss gesetzt sein");
		SozialdienstStammdaten stammdaten = persistence.find(SozialdienstStammdaten.class, id);
		return Optional.ofNullable(stammdaten);
	}

	@Nonnull
	@Override
	public Optional<SozialdienstStammdaten> getSozialdienstStammdatenBySozialdienstId(@Nonnull String sozialdienstId) {
		requireNonNull(sozialdienstId, "Gemeinde id muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<SozialdienstStammdaten> query = cb.createQuery(SozialdienstStammdaten.class);
		Root<SozialdienstStammdaten> root = query.from(SozialdienstStammdaten.class);
		Predicate predicate = cb.equal(root.get(SozialdienstStammdaten_.sozialdienst).get(AbstractEntity_.id), sozialdienstId);
		query.where(predicate);
		SozialdienstStammdaten stammdaten = persistence.getCriteriaSingleResult(query);
		return Optional.ofNullable(stammdaten);
	}

	@Nonnull
	@Override
	public SozialdienstStammdaten saveSozialdienstStammdaten(@Nonnull SozialdienstStammdaten stammdaten) {
		requireNonNull(stammdaten);
		return persistence.merge(stammdaten);
	}

	@Nonnull
	@Override
	public Optional<SozialdienstFall> findSozialdienstFall(
		@Nonnull String id) {
		SozialdienstFall sozialdienstFall = persistence.find(SozialdienstFall.class, id);
		return Optional.ofNullable(sozialdienstFall);
	}
}
