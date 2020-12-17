/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchsperiodeEmailCandidate;
import ch.dvbern.ebegu.entities.GesuchsperiodeEmailCandidate_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchsperiodeEmailCandiateStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Local(GesuchsperiodeEmailService.class)
public class GesuchsperiodeEmailServiceBean extends AbstractBaseService implements GesuchsperiodeEmailService {

	@Inject
	private DossierService dossierService;

	@Inject
	private Persistence persistence;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private MailService mailService;

	@Inject
	private GemeindeService gemeindeService;

	private static final Logger LOG =
		LoggerFactory.getLogger(GesuchsperiodeEmailCandidate.class.getSimpleName());

	@Override
	public void getAndSaveGesuchsperiodeEmailCandidates(
		@Nonnull Gesuchsperiode lastGesuchsperiode,
		@Nonnull Gesuchsperiode nextGesuchsperiode) {
		Collection<Dossier> allDossiers = dossierService.getAllDossiers(true);
		allDossiers.forEach(dossier -> {
			GesuchsperiodeEmailCandidate candidate = new GesuchsperiodeEmailCandidate(dossier, lastGesuchsperiode, nextGesuchsperiode);
			persistence.persist(candidate);
		});
	}

	@Override
	public void sendMailsForNCandidates(@Nonnull Integer numberOfCandidates) {
		List<GesuchsperiodeEmailCandidate> candidates = getNCandidates(numberOfCandidates);
		if (candidates.isEmpty()) {
			return;
		}
		LOG.info("Emails für " + candidates.size() + " Dossiers werden versendet");
		candidates.forEach(c -> {
			try {
				sendMailAndChangeStatus(c);
				persistence.persist(c);
			} catch (Exception e) {
				c.setStatus(GesuchsperiodeEmailCandiateStatus.FEHLGESCHLAGEN);
				LOG.error("Mail versenden fehlgeschlagen", e);
			} finally {
				persistence.persist(c);
			}
		});
	}

	private void sendMailAndChangeStatus(@Nonnull GesuchsperiodeEmailCandidate gesuchsperiodeEmailCandidate) {
		Optional<Gesuch> gesuchOpt = gesuchService.getNeuestesGesuchForDossierAndPeriod(
			gesuchsperiodeEmailCandidate.getDossier(),
			gesuchsperiodeEmailCandidate.getLastGesuchsperiode()
		);

		if (gesuchOpt.isEmpty()) {
			gesuchsperiodeEmailCandidate.setStatus(GesuchsperiodeEmailCandiateStatus.KEIN_GESUCH);
			return;
		}
		Gesuch gesuch = gesuchOpt.get();

		if (gesuch.extractAllBetreuungen().isEmpty()) {
			gesuchsperiodeEmailCandidate.setStatus(GesuchsperiodeEmailCandiateStatus.NUR_TAGESSCHULEN);
			return;
		}

		if (gesuch.getFall().getBesitzer() == null) {
			gesuchsperiodeEmailCandidate.setStatus(GesuchsperiodeEmailCandiateStatus.KEIN_BESITZER);
			return;
		}

		String gemeindeId = gesuch.getDossier().getGemeinde().getId();
		GemeindeStammdaten gemeindeStammdaten =
			gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException(
				"sendMailAndChangeStatus",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gemeindeId
			));

		if (!gemeindeStammdaten.getEmailBeiGesuchsperiodeOeffnung()) {
			gesuchsperiodeEmailCandidate.setStatus(GesuchsperiodeEmailCandiateStatus.GEMEINDE_EINSTELLUNG_DEAKTIVIERT);
			return;
		}

		mailService.sendInfoFreischaltungGesuchsperiode(
			gesuchsperiodeEmailCandidate.getNextGesuchsperiode(),
			gesuch
		);

		gesuchsperiodeEmailCandidate.setStatus(GesuchsperiodeEmailCandiateStatus.VERSENDET);
	}

	private List<GesuchsperiodeEmailCandidate> getNCandidates(@Nonnull Integer numberOfCandidates) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GesuchsperiodeEmailCandidate> query = cb.createQuery(GesuchsperiodeEmailCandidate.class);

		Root<GesuchsperiodeEmailCandidate> root = query.from(GesuchsperiodeEmailCandidate.class);

		Predicate predicateOffen = cb.equal(root.get(GesuchsperiodeEmailCandidate_.status), GesuchsperiodeEmailCandiateStatus.OFFEN);
		query.where(predicateOffen);
		return persistence.getCriteriaResults(query, numberOfCandidates);
	}
}
