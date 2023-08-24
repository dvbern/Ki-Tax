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

import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.SearchUtil;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
@Local(AlleFaelleViewService.class)
@PermitAll
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AlleFaelleViewServiceBean extends AbstractBaseService implements AlleFaelleViewService {

	private static final Logger LOG = LoggerFactory.getLogger(AlleFaelleViewServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private BenutzerService benutzerService;

	@Override
	public boolean isNeueAlleFaelleViewActivated() {
		return false;
	}

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
	public void createKindInView(Kind kind, Gesuch gesuch) {
		AlleFaelleViewKind alleFaelleViewKind = createKindView(kind, gesuch.getId());
		persistence.merge(alleFaelleViewKind);
	}

	@Override
	public void updateKindInView(Kind kind) {
		Optional<AlleFaelleViewKind> alleFaelleViewKindToUpdate = findAlleFaelleViewKindByKindId(kind.getId());

		if (alleFaelleViewKindToUpdate.isEmpty()) {
			//hier wird kein Fehler geworffen, es kann sein dass das KindGS updated werden soll. für dieses
			//gibt es keinen Eintrag in der AlleFälleListe
			return;
		}

		AlleFaelleViewKind alleFaelleViewKind = alleFaelleViewKindToUpdate.get();
		alleFaelleViewKind.setName(kind.getVorname());
		persistence.merge(alleFaelleViewKind);
	}

	@Override
	public void removeKindInView(Kind kind) {
		Optional<AlleFaelleViewKind> alleFaelleViewKindToRemove = findAlleFaelleViewKindByKindId(kind.getId());
		alleFaelleViewKindToRemove.ifPresent(alleFaelleViewKind -> persistence.remove(alleFaelleViewKind));
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

	private Set<AntragStatus> getAntragStatuses(boolean searchForPendenzen, UserRole role) {
		if (searchForPendenzen) {
			return AntragStatus.pendenzenForRole(role);
		}
		return AntragStatus.allowedforRole(role);
	}


	private Optional<AlleFaelleView> findAlleFaelleViewByAntragId(@NotNull String antragId) {
		AlleFaelleView alleFaelleView =  persistence.find(AlleFaelleView.class, antragId);
		return Optional.ofNullable(alleFaelleView);
	}

	private Optional<AlleFaelleViewKind> findAlleFaelleViewKindByKindId(@NotNull String kindId) {
		AlleFaelleViewKind alleFaelleViewKind =  persistence.find(AlleFaelleViewKind.class, kindId);
		return Optional.ofNullable(alleFaelleViewKind);
	}

	private AlleFaelleView createAlleFaelleViewForFullGesuch(Gesuch gesuch) {
		AlleFaelleView alleFaelleView = new AlleFaelleView();
		alleFaelleView.setAntragId(gesuch.getId());
		alleFaelleView.setMandantId(gesuch.getGesuchsperiode().getMandant().getId());
		alleFaelleView.setDossierId(gesuch.getDossier().getId());
		alleFaelleView.setFallId(gesuch.getFall().getId());
		alleFaelleView.setFallNummer(gesuch.getFall().getFallNummer());
		alleFaelleView.setBesitzerId(gesuch.getFall().getBesitzer() != null ?
			gesuch.getFall().getBesitzer().getId() :
			null);
		alleFaelleView.setBesitzerUsername(gesuch.getFall().getBesitzer() != null ?
			gesuch.getFall().getBesitzer().getUsername() :
			null);
		alleFaelleView.setGemeindeId(gesuch.getDossier().getGemeinde().getId());
		alleFaelleView.setGemeindeName(gesuch.getDossier().getGemeinde().getName());

		alleFaelleView.setFamilienName(gesuch.extractFamiliennamenString());
		gesuch.getKindContainers()
				.forEach(kindContainer -> {
					AlleFaelleViewKind kind = createKindView(kindContainer.getKindJA(), alleFaelleView.getAntragId());
					alleFaelleView.addKind(kind);
				});

		alleFaelleView.setAngebotTypen(gesuch.getKindContainers().stream()
			.flatMap(kc -> kc.getAllPlaetze().stream())
			.map(b -> b.getInstitutionStammdaten().getBetreuungsangebotTyp().name()).collect(Collectors.joining(", ")));

		alleFaelleView.setSozialdienst(gesuch.getFall().isSozialdienstFall());
		alleFaelleView.setSozialdienstId(gesuch.getFall().getSozialdienstFall() != null ?
			gesuch.getFall().getSozialdienstFall().getSozialdienst().getId() :
			null);

		alleFaelleView.setGesuchsperiodeId(gesuch.getGesuchsperiode().getId());
		alleFaelleView.setGesuchsperiodeStatus(gesuch.getGesuchsperiode().getStatus());
		alleFaelleView.setGesuchsperiodeString(gesuch.getGesuchsperiode().getGesuchsperiodeStringShort());

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

	private AlleFaelleViewKind createKindView(Kind kindJA, String antragId) {
		AlleFaelleViewKind alleFaelleViewKind = new AlleFaelleViewKind();
		alleFaelleViewKind.setKindId(kindJA.getId());
		alleFaelleViewKind.setName(kindJA.getVorname());
		alleFaelleViewKind.setAntragId(antragId);
		return alleFaelleViewKind;
	}

	private void updateAlleFaelleViewForGesuch(Gesuch gesuch, AlleFaelleView alleFaelleView) {
		alleFaelleView.setAntragStatus(gesuch.getStatus());
		alleFaelleView.setGesuchBetreuungenStatus(gesuch.getGesuchBetreuungenStatus());
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

	@Override
	public List<AlleFaelleView> searchAntrage(
		AntragTableFilterDTO antragTableFilterDTO,
		boolean searchForPendenzen) {

		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new EbeguRuntimeException("searchAllAntraege", "No User is logged in"));

		Set<AntragStatus> allowedAntragStatus = getAntragStatuses(searchForPendenzen, user.getRole());

		if (allowedAntragStatus.isEmpty()) {
			return Collections.emptyList();
		}

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<AlleFaelleView> root = query.from(AlleFaelleView.class);

		List<Predicate> predicates = new ArrayList<>();

		Predicate inClauseStatus = root.get(AlleFaelleView_.ANTRAG_STATUS).in(allowedAntragStatus);
		predicates.add(inClauseStatus);
		Predicate mandantPredicate = cb.equal(root.get(AlleFaelleView_.MANDANT_ID), user.getMandant().getId());
		predicates.add(mandantPredicate);
		getGemeindePredicateForCurrentUser(user, root).ifPresent(predicates::add);
		getRoleBasedPredicate(user, cb, root).ifPresent(predicates::add);
		getOnlyAktivePeriodenPredicate(cb, root, antragTableFilterDTO.isOnlyAktivePerioden()).ifPresent(predicates::add);

		try {
			predicates.addAll(getFilterPredicates(antragTableFilterDTO, cb, root, user.getRole()));
		} catch (DateTimeParseException e) {
			// Versuch Filterung nach ungueltigem Datum. Es kann kein Gesuch geben, welches passt. Wir geben leer zurueck
			return Collections.emptyList();
		}

		query.select(root.get(AlleFaelleView_.ANTRAG_ID))
			.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		query.orderBy(cb.desc(root.get(AlleFaelleView_.aenderungsdatum)));

		List<String> alleFaelleViewIds = persistence.getCriteriaResults(query); //select all ids in order, may contain duplicates
		List<AlleFaelleView> pagedResult;

		if (antragTableFilterDTO.getPagination() != null) {
			int firstIndex = antragTableFilterDTO.getPagination().getStart();
			Integer maxresults = antragTableFilterDTO.getPagination().getNumber();
			List<String> orderedIdsToLoad =
				SearchUtil.determineDistinctIdsToLoad(alleFaelleViewIds, firstIndex, maxresults);
			pagedResult = findAlleFaelleViewByIds(orderedIdsToLoad);
		} else {
			pagedResult = findAlleFaelleViewByIds(alleFaelleViewIds);
		}

//		pagedResult.forEach(authorizer::checkReadAuthorization);
		return pagedResult;
	}

	private Optional<Predicate> getOnlyAktivePeriodenPredicate(
		CriteriaBuilder cb,
		Root<AlleFaelleView> root,
		@Nullable Boolean onlyAktivePerioden) {
		if (onlyAktivePerioden == null || !onlyAktivePerioden) {
			return Optional.empty();
		}

		return Optional.of(cb.equal(root.get(AlleFaelleView_.gesuchsperiodeStatus), GesuchsperiodeStatus.AKTIV));
	}

	private List<Predicate> getFilterPredicates(
		AntragTableFilterDTO antragTableFilterDTO,
		CriteriaBuilder cb,
		Root<AlleFaelleView> root,
		UserRole role) {
		if (antragTableFilterDTO.getSearch() == null ||
			antragTableFilterDTO.getSearch().getPredicateObject() == null) {
			return Collections.emptyList();
		}

		AntragPredicateObjectDTO predicateObjectDto = antragTableFilterDTO.getSearch().getPredicateObject();
		List<Predicate> predicates = new ArrayList<>();

		if (predicateObjectDto.getFallNummer() != null) {
			// Die Fallnummer muss als String mit LIKE verglichen werden: Bei Eingabe von "14" soll der Fall "114"
			// kommen
			Expression<String> fallNummerAsString = root.get(AlleFaelleView_.fallNummer).as(String.class);
			String fallNummerWithWildcards = SearchUtil.withWildcards(predicateObjectDto.getFallNummer());
			predicates.add(cb.like(fallNummerAsString, fallNummerWithWildcards));
		}
		if (predicateObjectDto.getGemeinde() != null) {
			Expression<String> gemeindeExpression = root.get(AlleFaelleView_.gemeindeName);
			predicates.add(cb.like(gemeindeExpression, predicateObjectDto.getGemeinde()));
		}
		if (predicateObjectDto.getFamilienName() != null) {
			Expression<String> familienNameExpression = root.get(AlleFaelleView_.familienName);
			predicates.add(cb.like(familienNameExpression, predicateObjectDto.getFamilienNameForLike()));
		}
		if (predicateObjectDto.getAntragTyp() != null) {
			List<AntragTyp> values = AntragTyp.getValuesForFilter(predicateObjectDto.getAntragTyp());

			if (!values.isEmpty()) {
				predicates.add(root.get(AlleFaelleView_.antragTyp).in(values));
			}
		}
		if (predicateObjectDto.getGesuchsperiodeString() != null) {
			Expression<String> gesuchsperiodeExpression = root.get(AlleFaelleView_.gesuchsperiodeString);
			predicates.add(cb.equal(gesuchsperiodeExpression, predicateObjectDto.getGesuchsperiodeString()));
		}
		if (predicateObjectDto.getEingangsdatum() != null) {
			LocalDate searchDate =
				LocalDate.parse(predicateObjectDto.getEingangsdatum(), Constants.DATE_FORMATTER);
			predicates.add(cb.equal(root.get(AlleFaelleView_.eingangsdatum), searchDate));
		}

		if (predicateObjectDto.getEingangsdatumSTV() != null) {
			LocalDate searchDate =
				LocalDate.parse(predicateObjectDto.getEingangsdatumSTV(), Constants.DATE_FORMATTER);
			predicates.add(cb.equal(root.get(AlleFaelleView_.eingangsdatumSTV), searchDate));
		}
		if (predicateObjectDto.getAenderungsdatum() != null) {
			// Wir wollen ohne Zeit vergleichen
			Expression<LocalDate> aenderungsdatumAsLocalDate =
				root.get(AlleFaelleView_.aenderungsdatum).as(LocalDate.class);
			LocalDate searchDate =
				LocalDate.parse(predicateObjectDto.getAenderungsdatum(), Constants.DATE_FORMATTER);
			predicates.add(cb.equal(aenderungsdatumAsLocalDate, searchDate));
		}
		if (predicateObjectDto.getStatus() != null) {
			createPredicateGesuchBetreuungenStatus(cb, root, predicates, predicateObjectDto.getStatus());
			// Achtung, hier muss von Client zu Server Status konvertiert werden!
			Collection<AntragStatus> antragStatus = AntragStatusConverterUtil.convertStatusToEntityForRole(
				AntragStatusDTO.valueOf(predicateObjectDto.getStatus()),
				role);
			predicates.add(root.get(AlleFaelleView_.antragStatus).in(antragStatus));
		}
		if (predicateObjectDto.getDokumenteHochgeladen() != null) {
			predicates.add(cb.equal(
				root.get(AlleFaelleView_.dokumenteHochgeladen),
				predicateObjectDto.getDokumenteHochgeladen()));
		}
		/*if (predicateObjectDto.getAngebote() != null) {
			switch (BetreuungsangebotTyp.valueOf(predicateObjectDto.getAngebote())) {
				case KITA:
				case TAGESFAMILIEN:
					predicates.add(cb.equal(
						joinInstitutionstammdatenBetreuungen.get(InstitutionStammdaten_.betreuungsangebotTyp),
						BetreuungsangebotTyp.valueOf(predicateObjectDto.getAngebote())));
					break;
				case TAGESSCHULE:
					predicates.add(cb.equal(
						joinInstitutionstammdatenTagesschule.get(InstitutionStammdaten_.betreuungsangebotTyp),
						BetreuungsangebotTyp.valueOf(predicateObjectDto.getAngebote())));
					break;
				case FERIENINSEL:
					predicates.add(cb.equal(
						joinInstitutionstammdatenFerieninsel.get(InstitutionStammdaten_.betreuungsangebotTyp),
						BetreuungsangebotTyp.valueOf(predicateObjectDto.getAngebote())));
					break;
			}
		}
		if (predicateObjectDto.getInstitutionen() != null) {
			predicates.add(
				cb.or(
					cb.equal(
						joinInstitutionBetreuungen.get(Institution_.name),
						predicateObjectDto.getInstitutionen()),
					cb.equal(
						joinInstitutionFerieninsel.get(Institution_.name),
						predicateObjectDto.getInstitutionen()),
					cb.equal(
						joinInstitutionTagesschule.get(Institution_.name),
						predicateObjectDto.getInstitutionen())
				));
		}*/
		if (predicateObjectDto.getKinder() != null) {
			SetJoin<AlleFaelleView, AlleFaelleViewKind> joinKinder = root.join(AlleFaelleView_.kinder, JoinType.LEFT);
			Expression<String> kindExpression = joinKinder.get(AlleFaelleViewKind_.name);
			predicates.add(cb.like(kindExpression, predicateObjectDto.getKindNameForLike()));
		}

		if (predicateObjectDto.getVerantwortlicherBG() != null && !role.isRoleSozialdienstabhaengig()) {
			Expression<String> verantworlicher = root.get(AlleFaelleView_.verantwortlicherBG);
			predicates.add(cb.equal(verantworlicher, predicateObjectDto.getVerantwortlicherBG()));
		}

		if (predicateObjectDto.getVerantwortlicherTS() != null && !role.isRoleSozialdienstabhaengig()) {
			Expression<String> verantworlicher = root.get(AlleFaelleView_.verantwortlicherTS);
			predicates.add(cb.equal(verantworlicher, predicateObjectDto.getVerantwortlicherTS()));
		}

		if (predicateObjectDto.getVerantwortlicherGemeinde() != null && !role.isRoleSozialdienstabhaengig()) {
			Expression<String> verantworlicherBG = root.get(AlleFaelleView_.verantwortlicherBG);
			Expression<String> verantworlicherTS = root.get(AlleFaelleView_.verantwortlicherTS);
			Predicate predicateBG = cb.equal(verantworlicherBG, predicateObjectDto.getVerantwortlicherGemeinde());
			Predicate predicateTS = cb.equal(verantworlicherTS, predicateObjectDto.getVerantwortlicherGemeinde());
			predicates.add(cb.and(cb.or(predicateBG, predicateTS)));
		}

		if (predicateObjectDto.getInternePendenz() != null) {
			predicates.add(cb.equal(root.get(AlleFaelleView_.internePendenz), predicateObjectDto.getInternePendenz()));
		}

		return predicates;
	}

	/**
	 * Adds a predicate to the predicates list if it is needed to filter by gesuchBetreuungenStatus. This will be
	 * needed just when the Status is GEPRUEFT, PLATZBESTAETIGUNG_WARTEN or PLATZBESTAETIGUNG_ABGEWIESEN
	 */
	private void createPredicateGesuchBetreuungenStatus(
		CriteriaBuilder cb,
		Root<AlleFaelleView> root,
		List<Predicate> predicates,
		String status) {
		if (AntragStatusDTO.PLATZBESTAETIGUNG_WARTEN.toString().equalsIgnoreCase(status)) {
			predicates.add(cb.equal(root.get(AlleFaelleView_.gesuchBetreuungenStatus), GesuchBetreuungenStatus.WARTEN));
		} else if (AntragStatusDTO.PLATZBESTAETIGUNG_ABGEWIESEN.toString().equalsIgnoreCase(status)) {
			predicates.add(cb.equal(root.get(AlleFaelleView_.gesuchBetreuungenStatus), GesuchBetreuungenStatus.ABGEWIESEN));
		} else if (AntragStatusDTO.GEPRUEFT.toString().equalsIgnoreCase(status)) {
			predicates.add(cb.equal(
				root.get(AlleFaelleView_.gesuchBetreuungenStatus),
				GesuchBetreuungenStatus.ALLE_BESTAETIGT));
		}
	}

	private List<AlleFaelleView> findAlleFaelleViewByIds(@NotNull List<String> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<AlleFaelleView> query = cb.createQuery(AlleFaelleView.class);
		Root<AlleFaelleView> root = query.from(AlleFaelleView.class);
		Predicate predicate = root.get(AlleFaelleView_.ANTRAG_ID).in(ids);
		root.fetch(AlleFaelleView_.kinder, JoinType.LEFT);

		query.where(predicate);
		query.orderBy(cb.desc(root.get(AlleFaelleView_.aenderungsdatum)));
		return persistence.getCriteriaResults(query);
	}

	private Optional<Predicate> getRoleBasedPredicate(Benutzer currentBenutzer, CriteriaBuilder cb, Root<AlleFaelleView> root) {
		Optional<Predicate> optionalPredicate =  Optional.empty();
		switch (currentBenutzer.getRole()) {
			case SUPER_ADMIN:
			case ADMIN_GEMEINDE:
			case SACHBEARBEITER_GEMEINDE:
				// Diese Rollen haben keine (rollenspezifischen) Einschränkungen!
				break;
			case SACHBEARBEITER_BG:
			case ADMIN_BG:
			case REVISOR:
			case JURIST:
			case ADMIN_MANDANT:
			case SACHBEARBEITER_MANDANT:
				/*if (searchForPendenzen) {
					Predicate jaOrMischGesuche = createPredicateJAOrMischGesuche(cb, joinDossier);
					if (!internePendenzGesuchIds.isEmpty()) {
						Predicate gesuchsIds = root.get(AbstractEntity_.id).in(internePendenzGesuchIds);
						predicates.add(cb.and(cb.or(jaOrMischGesuche, gesuchsIds)));
					} else {
						predicates.add(jaOrMischGesuche);
					}
				}*/
				break;
			case STEUERAMT:
				break;
			case ADMIN_SOZIALDIENST:
			case SACHBEARBEITER_SOZIALDIENST:
				Objects.requireNonNull(currentBenutzer.getSozialdienst());
				Predicate sozialDienstPredicate =
					cb.equal(root.get(AlleFaelleView_.sozialdienstId), currentBenutzer.getSozialdienst().getId());
				optionalPredicate = Optional.of(sozialDienstPredicate);
				break;
			case ADMIN_TRAEGERSCHAFT:
			case SACHBEARBEITER_TRAEGERSCHAFT:
				/*if (predicateObjectDto != null && predicateObjectDto.getAngebote() != null) {
					switch (BetreuungsangebotTyp.valueOf(predicateObjectDto.getAngebote())) {
						case KITA:
						case TAGESFAMILIEN:
							predicates.add(cb.equal(
								joinInstitutionBetreuungen.get(Institution_.traegerschaft),
								user.getTraegerschaft()));
							break;
						case TAGESSCHULE:
							predicates.add(cb.equal(
								joinInstitutionFerieninsel.get(Institution_.traegerschaft),
								user.getTraegerschaft()));
							break;
						case FERIENINSEL:
							predicates.add(cb.equal(
								joinInstitutionTagesschule.get(Institution_.traegerschaft),
								user.getTraegerschaft()));
							break;
						default:
							throw new EbeguRuntimeException(
								"searchAntraege",
								"BetreuungsangebotTyp nicht gefunden: "
									+ BetreuungsangebotTyp.valueOf(predicateObjectDto.getAngebote()));
					}*/
//				} else {
//					predicates.add(
//						cb.or(
//							cb.equal(joinInstitutionBetreuungen.get(Institution_.traegerschaft), user.getTraegerschaft()),
//							cb.equal(joinInstitutionFerieninsel.get(Institution_.traegerschaft), user.getTraegerschaft()),
//							cb.equal(joinInstitutionTagesschule.get(Institution_.traegerschaft), user.getTraegerschaft())
//						));
//				}
//				predicates.add(createPredicateAusgeloesteSCHJAAngebote(
//					cb,
//					joinAnmeldungTagesschule,
//					joinAnmeldungFerieninsel,
//					joinInstitutionstammdatenBetreuungen,
//					joinInstitutionstammdatenTagesschule,
//					joinInstitutionstammdatenFerieninsel));
//				break;
			case ADMIN_INSTITUTION:
			case SACHBEARBEITER_INSTITUTION:
				// es geht hier nicht um die joinInstitution des zugewiesenen benutzers sondern um die joinInstitution des
				// eingeloggten benutzers
//				if (predicateObjectDto != null && predicateObjectDto.getAngebote() != null) {
//					switch (BetreuungsangebotTyp.valueOf(predicateObjectDto.getAngebote())) {
//						case KITA:
//						case TAGESFAMILIEN:
//							predicates.add(cb.equal(joinInstitutionBetreuungen, user.getInstitution()));
//							break;
//						case TAGESSCHULE:
//							predicates.add(cb.equal(joinInstitutionTagesschule, user.getInstitution()));
//							break;
//						case FERIENINSEL:
//							predicates.add(cb.equal(joinInstitutionFerieninsel, user.getInstitution()));
//							break;
//						default:
//							throw new EbeguRuntimeException(
//								"searchAntraege",
//								"BetreuungsangebotTyp nicht gefunden: "
//									+ BetreuungsangebotTyp.valueOf(predicateObjectDto.getAngebote()));
//					}
//				} else {
//					predicates.add(
//						cb.or(
//							cb.equal(joinInstitutionBetreuungen, user.getInstitution()),
//							cb.equal(joinInstitutionFerieninsel, user.getInstitution()),
//							cb.equal(joinInstitutionTagesschule, user.getInstitution())
//						));
//				}
//				predicates.add(createPredicateAusgeloesteSCHJAAngebote(
//					cb,
//					joinAnmeldungTagesschule,
//					joinAnmeldungFerieninsel,
//					joinInstitutionstammdatenBetreuungen,
//					joinInstitutionstammdatenTagesschule,
//					joinInstitutionstammdatenFerieninsel));
				break;
			case SACHBEARBEITER_TS:
			case ADMIN_TS:
//				if (searchForPendenzen) {
//					Predicate schOrMischGesuche = createPredicateSCHOrMischGesuche(cb, root, joinDossier);
//					if (!internePendenzGesuchIds.isEmpty()) {
//						Predicate gesuchsIds = root.get(AbstractEntity_.id).in(internePendenzGesuchIds);
//						predicates.add(cb.and(cb.or(schOrMischGesuche, gesuchsIds)));
//					} else {
//						predicates.add(schOrMischGesuche);
//					}
//				}
				break;
			default:
				LOG.warn("antragSearch can not be performed by users in role {}", currentBenutzer.getRole());
				Predicate impossiblePredict = cb.isFalse(cb.literal(Boolean.TRUE)); // impossible predicate
				optionalPredicate = Optional.of(impossiblePredict);
				break;
		}
		return optionalPredicate;
	}

	private Optional<Predicate> getGemeindePredicateForCurrentUser(Benutzer currentBenutzer, Root<AlleFaelleView> root) {
		if (currentBenutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Collection<String> gemeindenForBenutzer =
				currentBenutzer.extractGemeindenForUser()
					.stream()
					.map(AbstractEntity::getId)
					.collect(Collectors.toList());
			return Optional.of(root.get(AlleFaelleView_.GEMEINDE_ID).in(gemeindenForBenutzer));
		}

		return Optional.empty();
	}
}
