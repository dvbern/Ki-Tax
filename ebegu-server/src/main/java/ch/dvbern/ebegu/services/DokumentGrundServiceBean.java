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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.DokumentGrund_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer Kind
 */
@Stateless
@Local(DokumentGrundService.class)
public class DokumentGrundServiceBean extends AbstractBaseService implements DokumentGrundService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DokumentGrundServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private Authorizer authorizer;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	public DokumentGrund saveDokumentGrund(@Nonnull DokumentGrund dokumentGrund) {
		Objects.requireNonNull(dokumentGrund);

		// Wir muessen zuerst den TimestampUpload setzen, bevor der Authorizer aufgerufen wird,
		// da dieser u.U. flusht und die Daten dann noch ungueltig waeren
		dokumentGrund.getDokumente().forEach(dokument -> {
			if (dokument.getTimestampUpload() == null) {
				dokument.setTimestampUpload(LocalDateTime.now());
			}
		});
		authorizer.checkWriteAuthorization(dokumentGrund.getGesuch());

		// Falls es der Gesuchsteller war, der das Dokument hochgeladen hat, soll das Flag auf dem Gesuch gesetzt werden,
		// damit das Jugendamt es sieht. Allerdings nur wenn das Gesuch schon freigegeben wurde
		if (principalBean.isCallerInAnyOfRole(UserRole.GESUCHSTELLER, UserRole.ADMIN_SOZIALDIENST, UserRole.SACHBEARBEITER_SOZIALDIENST)
			&& !dokumentGrund.getGesuch().getStatus().isAnyOfInBearbeitungGSOrSZD()) {
			dokumentGrund.getGesuch().setDokumenteHochgeladen(Boolean.TRUE);
		}
		final DokumentGrund mergedDokumentGrund = persistence.merge(dokumentGrund);
		// beim DokumentGrund mit dem DokumentGrundTyp SONSTIGE_NACHWEISE oder PAPIERGESUCH  soll das needed-Flag (transient)
		// per default auf false sein. sonst stimmt der Wizardstep-Status spaeter nicht
		if (DokumentGrundTyp.isSonstigeOrPapiergesuch(mergedDokumentGrund.getDokumentGrundTyp())) {
			dokumentGrund.setNeeded(false);
		}
		wizardStepService.updateSteps(mergedDokumentGrund.getGesuch().getId(), null, null, WizardStepName.DOKUMENTE);
		return mergedDokumentGrund;
	}

	@Override
	@Nonnull
	public Optional<DokumentGrund> findDokumentGrund(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		DokumentGrund dokGrund = persistence.find(DokumentGrund.class, key);
		if (dokGrund == null) {
			return Optional.empty();
		}
		authorizer.checkReadAuthorization(dokGrund.getGesuch());
		return Optional.of(dokGrund);
	}

	@Override
	@Nonnull
	public Collection<DokumentGrund> findAllDokumentGrundByGesuch(@Nonnull Gesuch gesuch) {
		final Collection<DokumentGrund> dokumentGruende = this.findAllDokumentGrundByGesuch(gesuch, true);
		dokumentGruende.forEach(dokumentGrund -> authorizer.checkReadAuthorization(dokumentGrund));
		return dokumentGruende;
	}

	@Nonnull
	@Override
	public Collection<DokumentGrund> findAllDokumentGrundByGesuch(@Nonnull Gesuch gesuch, boolean doAuthCheck) {
		Objects.requireNonNull(gesuch);
		if (doAuthCheck) {
			this.authorizer.checkReadAuthorization(gesuch);
		}
		Collection<DokumentGrund> dokumentGrunds = criteriaQueryHelper.getEntitiesByAttribute(DokumentGrund.class, gesuch, DokumentGrund_.gesuch);
		setSonstigeNeededFalse(dokumentGrunds);
		return dokumentGrunds;
	}

	@Override
	@Nonnull
	public Collection<DokumentGrund> findAllDokumentGrundByGesuchAndDokumentType(@Nonnull Gesuch gesuch, @Nonnull DokumentGrundTyp dokumentGrundTyp) {
		Objects.requireNonNull(gesuch);

		this.authorizer.checkReadAuthorization(gesuch);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<DokumentGrund> query = cb.createQuery(DokumentGrund.class);

		Root<DokumentGrund> root = query.from(DokumentGrund.class);

		Predicate predicateGesuch = cb.equal(root.get(DokumentGrund_.gesuch), gesuch);
		Predicate predicateDokumentGrundTyp = cb.equal(root.get(DokumentGrund_.dokumentGrundTyp), dokumentGrundTyp);

		query.where(predicateGesuch, predicateDokumentGrundTyp);
		List<DokumentGrund> dokumentGrunds = persistence.getCriteriaResults(query);
		setSonstigeNeededFalse(dokumentGrunds);

		return dokumentGrunds;
	}

	private void setSonstigeNeededFalse(Collection<DokumentGrund> dokumentGrunds) {
		// beim DokumentGrund mit dem DokumentGrundTyp SONSTIGE_NACHWEISE oder PAPIERGESUCH soll das needed-Flag (transient)
		// per default auf false sein. sonst stimmt der Wizardstep-Status spaeter nicht
		dokumentGrunds.forEach(d -> {
			if (DokumentGrundTyp.isSonstigeOrPapiergesuch(d.getDokumentGrundTyp())) {
				d.setNeeded(false);
			}
		});
	}

	@Override
	@Nullable
	public DokumentGrund updateDokumentGrund(@Nonnull DokumentGrund dokumentGrund) {
		Objects.requireNonNull(dokumentGrund);
		authorizer.checkWriteAuthorization(dokumentGrund.getGesuch());

		//Wenn DokumentGrund keine Dokumente mehr hat und nicht gebraucht wird, wird er entfernt ausser es ist SONSTIGE NACHWEISE oder PAPIERGESUCH  (da ist needed immer false)
		if (!DokumentGrundTyp.isSonstigeOrPapiergesuch(dokumentGrund.getDokumentGrundTyp()) && !dokumentGrund.isNeeded() && dokumentGrund.getDokumente().isEmpty()) {
			persistence.remove(dokumentGrund);
			return null;
		}
		final DokumentGrund mergedDokument = persistence.merge(dokumentGrund);
		// beim DokumentGrund mit dem DokumentGrundTyp SONSTIGE_NACHWEISE oder PAPIERGESUCH soll das needed-Flag (transient)
		// per default auf false sein. sonst stimmt der Wizardstep-Status spaeter nicht
		if(DokumentGrundTyp.isSonstigeOrPapiergesuch(mergedDokument.getDokumentGrundTyp())) {
			mergedDokument.setNeeded(false);
		}
		wizardStepService.updateSteps(mergedDokument.getGesuch().getId(), null, null, WizardStepName.DOKUMENTE);
		return mergedDokument;
	}

	@Override
	public void removeAllDokumentGrundeFromGesuch(@Nonnull Gesuch gesuch) {
		LOGGER.info("Deleting Dokument-Gruende of Gesuch: {} / {}", gesuch.getDossier(), gesuch.getGesuchsperiode().getGesuchsperiodeString());
		Collection<DokumentGrund> dokumentsFromGesuch = findAllDokumentGrundByGesuch(gesuch, false);
		for (DokumentGrund dokument : dokumentsFromGesuch) {
			authorizer.checkWriteAuthorization(dokument);
			LOGGER.info("Deleting DokumentGrund: {}", dokument.getId());
			persistence.remove(DokumentGrund.class, dokument.getId());
		}
	}

	@Override
	public void removeIfEmpty(@Nonnull DokumentGrund dokumentGrund) {
		authorizer.checkWriteAuthorization(dokumentGrund.getGesuch());
		if (dokumentGrund.isEmpty()) {
			persistence.remove(dokumentGrund);
		}
	}
}
