/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AlleFaelleView;
import ch.dvbern.ebegu.entities.AlleFaelleViewKind;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(AlleFaelleViewService.class)
@PermitAll
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AlleFaelleViewServiceBean extends AbstractBaseService implements AlleFaelleViewService {

	@Inject
	private Persistence persistence;

	@Override
	public void createViewForFullGesuch(Gesuch gesuch) {
		AlleFaelleView alleFaelleView = createAlleFaelleViewForFullGesuch(gesuch);
		persistence.merge(alleFaelleView);
	}

	@Override
	public void updateViewForGesuch(Gesuch gesuch) {
		Optional<AlleFaelleView> alleFaelleViewToUpdate =
				findAlleFaelleViewByAntragId(gesuch.getId());

		if (alleFaelleViewToUpdate.isEmpty()) {
			throw new EbeguEntityNotFoundException("updateViewForGesuch", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		}

		updateAlleFaelleViewForGesuch(gesuch, alleFaelleViewToUpdate.get());
		persistence.merge(alleFaelleViewToUpdate.get());
	}

	@Override
	public void removeViewForGesuch(Gesuch gesuch) {
		Optional<AlleFaelleView> alleFaelleViewToUpdate =
				findAlleFaelleViewByAntragId(gesuch.getId());

		alleFaelleViewToUpdate.ifPresent(alleFaelleView -> persistence.remove(alleFaelleView));
	}

	@Override
	public Long countAllGesuch() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery query = cb.createQuery(Long.class);
		Root<Gesuch> root = query.from(Gesuch.class);
		query.select(cb.countDistinct(root.get(AbstractEntity_.id)));
		Long count = (Long) persistence.getCriteriaSingleResult(query);
		return count;
	}

	@Override
	public List<String> searchAllGesuchIds(int start, int size) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery query = cb.createQuery(Gesuch.class);
		Root<Gesuch> root = query.from(Gesuch.class);
		query.select(root.get(AbstractEntity_.id));
		Path<LocalDateTime> orderByTimestamp = root.get(AbstractEntity_.timestampErstellt);
		query.orderBy(cb.asc(orderByTimestamp));
		TypedQuery<String> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setMaxResults(size);
		typedQuery.setFirstResult(start);
		return typedQuery.getResultList();
	}


	private Optional<AlleFaelleView> findAlleFaelleViewByAntragId(@NotNull String antragId) {
		AlleFaelleView alleFaelleView =  persistence.find(AlleFaelleView.class, antragId);
		return Optional.ofNullable(alleFaelleView);
	}

	private AlleFaelleView createAlleFaelleViewForFullGesuch(Gesuch gesuch) {
		AlleFaelleView alleFaelleView = new AlleFaelleView();
		alleFaelleView.setAntragId(gesuch.getId());
		alleFaelleView.setMandantId(gesuch.getGesuchsperiode().getMandant().getId());
		alleFaelleView.setDossierId(gesuch.getDossier().getId());
		alleFaelleView.setFallId(gesuch.getFall().getId());
		alleFaelleView.setFallNummer(String.valueOf(gesuch.getFall().getFallNummer()));
		alleFaelleView.setBesitzerId(gesuch.getFall().getBesitzer() != null ?
			gesuch.getFall().getBesitzer().getId() :
			null);
		alleFaelleView.setBesitzerUsername(gesuch.getFall().getBesitzer() != null ?
			gesuch.getFall().getBesitzer().getUsername() :
			null);
		alleFaelleView.setGemeindeId(gesuch.getDossier().getGemeinde().getId());
		alleFaelleView.setGemeindeName(gesuch.getDossier().getGemeinde().getName());

		alleFaelleView.setFamilienName(gesuch.extractFamiliennamenString());
		gesuch.getKindContainers().forEach(kindContainer -> createKindView(kindContainer.getKindJA(), alleFaelleView));

		alleFaelleView.setAngebotTypen(gesuch.getKindContainers().stream()
			.flatMap(kc -> kc.getAllPlaetze().stream())
			.map(b -> b.getInstitutionStammdaten().getBetreuungsangebotTyp().name()).collect(Collectors.joining(", ")));

		alleFaelleView.setSozialdienst(gesuch.getFall().isSozialdienstFall());
		alleFaelleView.setSozialdienstId(gesuch.getFall().getSozialdienstFall() != null ?
			gesuch.getFall().getSozialdienstFall().getSozialdienst().getId() :
			null);

		alleFaelleView.setGesuchsperiodeId(gesuch.getGesuchsperiode().getId());
		alleFaelleView.setGesuchsperiodeString(gesuch.getGesuchsperiode().getGesuchsperiodeString());

		Benutzer verantwortlicherBG = gesuch.getDossier().getVerantwortlicherBG();
		if (verantwortlicherBG != null) {
			alleFaelleView.setVerantwortlicherBG(verantwortlicherBG.getFullName());
			alleFaelleView.setVerantwortlicherBGId(verantwortlicherBG.getId());
		}
		Benutzer verantwortlicherTS = gesuch.getDossier().getVerantwortlicherTS();
		if (verantwortlicherTS != null) {
			alleFaelleView.setVerantwortlicherTS(verantwortlicherTS.getFullName());
			alleFaelleView.setVerantwortlicherTSId(verantwortlicherTS.getId());
		}

		alleFaelleView.setInstitutionen(createInstitutionenList(gesuch.getKindContainers()));
		updateAlleFaelleViewForGesuch(gesuch, alleFaelleView);
		return alleFaelleView;
	}

	private void createKindView(Kind kindJA, AlleFaelleView alleFaelleView) {
		AlleFaelleViewKind alleFaelleViewKind = new AlleFaelleViewKind();
		alleFaelleViewKind.setKindId(kindJA.getId());
		alleFaelleViewKind.setName(kindJA.getVorname());
		alleFaelleViewKind.setAlleFaelleView(alleFaelleView);
		alleFaelleView.addKind(alleFaelleViewKind);
	}

	private void updateAlleFaelleViewForGesuch(Gesuch gesuch, AlleFaelleView alleFaelleView) {
		alleFaelleView.setAntragStatus(gesuch.getStatus());
		alleFaelleView.setAntragTyp(gesuch.getTyp());
		alleFaelleView.setEingangsart(gesuch.getEingangsart());
		alleFaelleView.setLaufnummer(gesuch.getLaufnummer());
		Objects.requireNonNull(gesuch.getTimestampMutiert());
		alleFaelleView.setAenderungsdatum(gesuch.getTimestampMutiert());
		alleFaelleView.setEingangsdatum(gesuch.getEingangsdatum());
		alleFaelleView.setEingangsdatumSTV(gesuch.getEingangsdatumSTV());
		alleFaelleView.setInternePendenz(gesuch.getInternePendenz());
		alleFaelleView.setDokumenteHochgeladen(gesuch.getDokumenteHochgeladen());
	}

	private List<Institution> createInstitutionenList(Set<KindContainer> kindContainers) {
		return kindContainers.stream()
			.flatMap(kc -> kc.getBetreuungen().stream())
			.map(Betreuung::getInstitutionStammdaten)
			.map(InstitutionStammdaten::getInstitution)
			.distinct()
			.collect(Collectors.toList());
	}
}
