/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

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
	private PrincipalBean principalBean;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private SequenceService sequenceService;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Gemeinde saveGemeinde(@Nonnull Gemeinde gemeinde) {
		requireNonNull(gemeinde);

		if (gemeinde.isNew()) {
			initGemeindeNummerAndMandant(gemeinde);
		}

		return persistence.merge(gemeinde);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Gemeinde createGemeinde(@Nonnull Gemeinde gemeinde) {
		if (findGemeindeByName(gemeinde.getName()).isPresent()) {
			throw new EntityExistsException(Gemeinde.class, "name", gemeinde.getName(), ErrorCodeEnum.ERROR_DUPLICATE_GEMEINDE_NAME);
		}
		final Long bfsNummer = gemeinde.getBfsNummer();
		if (findGemeindeByBSF(bfsNummer).isPresent()) {
			throw new EntityExistsException(Gemeinde.class, "bsf",
				bfsNummer != null ? Long.toString(bfsNummer) : "",
				ErrorCodeEnum.ERROR_DUPLICATE_GEMEINDE_BSF);
		}
		return saveGemeinde(gemeinde);
	}

	@Nonnull
	@Override
	public Optional<Gemeinde> findGemeinde(@Nonnull String id) {
		requireNonNull(id, "id muss gesetzt sein");
		Gemeinde gemeinde = persistence.find(Gemeinde.class, id);
		return Optional.ofNullable(gemeinde);
	}

	@Nonnull
	@Override
	public Optional<Gemeinde> findGemeindeByName(@Nonnull String name) {
		requireNonNull(name, "Gemeindename muss gesetzt sein");
		return criteriaQueryHelper.getEntityByUniqueAttribute(Gemeinde.class, name, Gemeinde_.name);
	}

	@Nonnull
	private Optional<Gemeinde> findGemeindeByBSF(@Nullable Long bsf) {
		return criteriaQueryHelper.getEntityByUniqueAttribute(Gemeinde.class, bsf, Gemeinde_.bfsNummer);
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
		return criteriaQueryHelper.getAllOrdered(Gemeinde.class, Gemeinde_.name);
	}

	private long getNextGemeindeNummer() {
		Mandant mandant = requireNonNull(principalBean.getMandant());

		return sequenceService.createNumberTransactional(SequenceType.GEMEINDE_NUMMER, mandant);
	}

	private void initGemeindeNummerAndMandant(@Nonnull Gemeinde gemeinde) {
		if (gemeinde.getMandant() == null) {
			gemeinde.setMandant(requireNonNull(principalBean.getMandant()));
		}
		if (gemeinde.getGemeindeNummer() == 0) {
			gemeinde.setGemeindeNummer(getNextGemeindeNummer());
		}
	}


	@Nonnull
	@Override
	public Collection<Gemeinde> getAktiveGemeinden() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gemeinde> query = cb.createQuery(Gemeinde.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);
		List<Predicate> predicates = new ArrayList<>();

		// Status muss aktiv sein
		Predicate predicateStatusActive = cb.equal(root.get(Gemeinde_.status), GemeindeStatus.AKTIV);
		predicates.add(predicateStatusActive);

//		// TODO MANDANTEN when developing kibon for multple mandanten we need to filter the mandanten too. Uncommenting the following code
//		// and taking the FIXME into account should be enough
//		// Nur Gemeinden meines Mandanten zurueckgeben
//		final Principal principal = principalBean.getPrincipal();
//		if (!Constants.ANONYMOUS_USER_USERNAME.equals(principal.getName())) {
//			// user anonymous can get the list of active Gemeinden, though anonymous user doesn't really exist
//			// FIXME MANDANTEN this is actually a problem if we work with different Mandanten because in onBoarding there is no user at all
//			// so we cannot get the mandant out of the user. In this case we need to send the mandant when calling this method
//			Mandant mandant = principalBean.getMandant();
//			if (mandant != null) {
//				Predicate predicateMandant = cb.equal(root.get(Gemeinde_.mandant), mandant);
//				predicates.add(predicateMandant);
//			}
//		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	@Nullable
	@Override
	public GemeindeStammdaten getStammdatenByGemeinde(@Nonnull String gemeindeId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdaten> query = cb.createQuery(GemeindeStammdaten.class);

		Root<Gesuch> root = query.from(GemeindeStammdaten.class);

		ParameterExpression<String> gemeindeId = cb.parameter(String.class, "gemeindeId");
		Predicate idPredicate = cb.equal(root.get(GemeindeStammdaten_.gemeinde).get(Gemeinde_.gemeindeNummer).get(AbstractPersonEntity_.nachname),
			gemeindeId);

		query.where(idPredicate);
		TypedQuery<Gesuch> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(gemeindeId);

		return q.getResult();
	}

}
