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

import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;


/**
 * Service fuer Dossier
 */
@Stateless
@Local(DossierService.class)
@PermitAll
public class DossierServiceBean extends AbstractBaseService implements DossierService {


	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private FallService fallService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private MitteilungService mitteilungService;

	@Nonnull
	@Override
	public Optional<Dossier> findDossier(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Dossier dossier = persistence.find(Dossier.class, id);
		if (dossier != null) {
			authorizer.checkReadAuthorizationDossier(dossier);
		}
		return Optional.ofNullable(dossier);
	}

	@Nonnull
	@Override
	public Collection<Dossier> findDossiersByFall(@Nonnull String fallId) {
		final Fall fall = fallService.findFall(fallId).orElseThrow(() -> new EbeguEntityNotFoundException("findDossiersByFall",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, fallId));

		Collection<Dossier> dossiers = criteriaQueryHelper.getEntitiesByAttribute(Dossier.class, fall, Dossier_.fall);
		return dossiers;
	}

	@Nonnull
	@Override
	public Optional<Dossier> findDossierByGemeindeAndFall(@Nonnull String gemeindeId, @Nonnull String fallId) {
		//TODO (KIBON-6) Aktuell wissen wir beim Aufruf dieser Methode die GemeindeId noch nicht. Wir geben immer das erste / einzige Dossier zurueck
		Collection<Dossier> dossiersByFall = findDossiersByFall(fallId);
		if (!dossiersByFall.isEmpty()) {
			return dossiersByFall.stream().findFirst();
		}
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Dossier saveDossier(@Nonnull Dossier dossier) {
		Objects.requireNonNull(dossier);
		Gemeinde bern = gemeindeService.getFirst();
		if (dossier.isNew()) {
			Optional<Dossier> dossierByGemeindeAndFall = findDossierByGemeindeAndFall(bern.getId(), dossier.getFall().getId());
			if (dossierByGemeindeAndFall.isPresent()) {
				dossier = dossierByGemeindeAndFall.get();
			}
		}
		//TODO (KIBON-6) Wir setzen im Moment fix die Gemeinde Bern
		dossier.setGemeinde(bern);
		authorizer.checkWriteAuthorizationDossier(dossier);
		return persistence.merge(dossier);
	}

	@Nonnull
	@Override
	public Dossier getOrCreateDossierAndFallForCurrentUserAsBesitzer(@Nonnull String gemeindeId) {
		Optional<Fall> fallOptional = fallService.findFallByCurrentBenutzerAsBesitzer();
		if (!fallOptional.isPresent()) {
			fallOptional = fallService.createFallForCurrentGesuchstellerAsBesitzer();
		}
		//noinspection ConstantConditions
		Objects.requireNonNull(fallOptional.get());
		Optional<Dossier> dossierOptional = findDossierByGemeindeAndFall("TODO", fallOptional.get().getId());
		if (dossierOptional.isPresent()) {
			return dossierOptional.get();
		}
		//TODO (KIBON-6) Gemeinde nach ID suchen und setzen
		Dossier dossier = new Dossier();
		dossier.setFall(fallOptional.get());
		return saveDossier(dossier);
	}

	@Override
	public boolean hasDossierAnyMitteilung(@Nonnull String dossierId) {
		final Optional<Dossier> dossierOptional = findDossier(dossierId);
		final Dossier dossier = dossierOptional.orElseThrow(() -> new EbeguEntityNotFoundException("hasDossierAnyMitteilung",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dossierId));
		final Collection<Mitteilung> mitteilungenForCurrentRolle = mitteilungService.getMitteilungenForCurrentRolle(dossier);
		return !mitteilungenForCurrentRolle.isEmpty();
	}
}
