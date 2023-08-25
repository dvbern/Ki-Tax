package ch.dvbern.ebegu.services.util;

import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nullable;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AlleFaellePredicateBuilder {

	private final CriteriaBuilder cb;
	private final Root<AlleFaelleView> root;

	public AlleFaellePredicateBuilder(CriteriaBuilder cb, Root<AlleFaelleView> root) {
		this.cb = cb;
		this.root = root;
	}

	public Optional<Predicate> buildOptionalGemeindePredicateForCurrentUser(Benutzer currentBenutzer) {
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

	public Optional<Predicate> buildOptionalOnlyAktivePeriodenPredicate(@Nullable Boolean onlyAktivePerioden) {
		if (onlyAktivePerioden == null || !onlyAktivePerioden) {
			return Optional.empty();
		}

		return Optional.of(cb.equal(root.get(AlleFaelleView_.gesuchsperiodeStatus), GesuchsperiodeStatus.AKTIV));
	}

	public List<Predicate> buildFilterPredicates(AntragTableFilterDTO antragTableFilterDTO, UserRole role) {
		if (antragTableFilterDTO.getSearch() == null ||
			antragTableFilterDTO.getSearch().getPredicateObject() == null) {
			return Collections.emptyList();
		}

		AntragPredicateObjectDTO predicateObjectDto = antragTableFilterDTO.getSearch().getPredicateObject();
		List<Predicate> predicates = new ArrayList<>();

		getOptionalPredicateLike(predicateObjectDto.getGemeinde(), AlleFaelleView_.gemeindeName).ifPresent(predicates::add);
		getOptionalPredicateLike(predicateObjectDto.getFamilienNameForLike(), AlleFaelleView_.familienName).ifPresent(predicates::add);
		getOptionalPredicateEqual(predicateObjectDto.getGesuchsperiodeString(), AlleFaelleView_.gesuchsperiodeString).ifPresent(predicates::add);
		getOptionalPredicateEqual(predicateObjectDto.getInternePendenz(), AlleFaelleView_.internePendenz).ifPresent(predicates::add);
		getOptionalPredicateEqual(predicateObjectDto.getDokumenteHochgeladen(), AlleFaelleView_.dokumenteHochgeladen).ifPresent(predicates::add);
		getOptionalPredicateDate(predicateObjectDto.getEingangsdatum(), AlleFaelleView_.eingangsdatum).ifPresent(predicates::add);
		getOptionalPredicateDate(predicateObjectDto.getEingangsdatumSTV(), AlleFaelleView_.eingangsdatumSTV).ifPresent(predicates::add);

		if (predicateObjectDto.getFallNummer() != null) {
			// Die Fallnummer muss als String mit LIKE verglichen werden: Bei Eingabe von "14" soll der Fall "114"
			// kommen
			Expression<String> fallNummerAsString = root.get(AlleFaelleView_.fallNummer).as(String.class);
			String fallNummerWithWildcards = SearchUtil.withWildcards(predicateObjectDto.getFallNummer());
			predicates.add(cb.like(fallNummerAsString, fallNummerWithWildcards));
		}

		if (predicateObjectDto.getAntragTyp() != null) {
			List<AntragTyp> values = AntragTyp.getValuesForFilter(predicateObjectDto.getAntragTyp());

			if (!values.isEmpty()) {
				predicates.add(root.get(AlleFaelleView_.antragTyp).in(values));
			}
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
			createPredicateGesuchBetreuungenStatus(predicates, predicateObjectDto.getStatus());
			// Achtung, hier muss von Client zu Server Status konvertiert werden!
			Collection<AntragStatus> antragStatus = AntragStatusConverterUtil.convertStatusToEntityForRole(
				AntragStatusDTO.valueOf(predicateObjectDto.getStatus()),
				role);
			predicates.add(root.get(AlleFaelleView_.antragStatus).in(antragStatus));
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

		if (!role.isRoleSozialdienstabhaengig()) {
			getOptionalPredicateEqual(predicateObjectDto.getVerantwortlicherBG(), AlleFaelleView_.verantwortlicherBG).ifPresent(predicates::add);
			getOptionalPredicateEqual(predicateObjectDto.getVerantwortlicherTS(), AlleFaelleView_.verantwortlicherTS).ifPresent(predicates::add);
		}

		if (predicateObjectDto.getVerantwortlicherGemeinde() != null && !role.isRoleSozialdienstabhaengig()) {
			Expression<String> verantworlicherBG = root.get(AlleFaelleView_.verantwortlicherBG);
			Expression<String> verantworlicherTS = root.get(AlleFaelleView_.verantwortlicherTS);
			Predicate predicateBG = cb.equal(verantworlicherBG, predicateObjectDto.getVerantwortlicherGemeinde());
			Predicate predicateTS = cb.equal(verantworlicherTS, predicateObjectDto.getVerantwortlicherGemeinde());
			predicates.add(cb.and(cb.or(predicateBG, predicateTS)));
		}

		return predicates;
	}

	private Optional<Predicate> getOptionalPredicateDate(@Nullable String date, SingularAttribute<AlleFaelleView, LocalDate> attribute) {
		if (date == null) {
			return Optional.empty();
		}

		LocalDate searchDate = LocalDate.parse(date, Constants.DATE_FORMATTER);
		return getOptionalPredicateEqual(searchDate, attribute);
	}

	private Optional<Predicate> getOptionalPredicateLike(@Nullable String value, SingularAttribute<AlleFaelleView, String> attribute) {
		if (value == null) {
			return Optional.empty();
		}

		Expression<String> expression = root.get(attribute);
		return Optional.of(cb.like(expression, value));
	}

	private <T> Optional<Predicate> getOptionalPredicateEqual(@Nullable T value, SingularAttribute<AlleFaelleView, T> attribute) {
		if (value == null) {
			return Optional.empty();
		}

		Expression<T> expression = root.get(attribute);
		return Optional.of(cb.equal(expression, value));
	}

	/**
	 * Adds a predicate to the predicates list if it is needed to filter by gesuchBetreuungenStatus. This will be
	 * needed just when the Status is GEPRUEFT, PLATZBESTAETIGUNG_WARTEN or PLATZBESTAETIGUNG_ABGEWIESEN
	 */
	private void createPredicateGesuchBetreuungenStatus(List<Predicate> predicates, String status) {
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

}
