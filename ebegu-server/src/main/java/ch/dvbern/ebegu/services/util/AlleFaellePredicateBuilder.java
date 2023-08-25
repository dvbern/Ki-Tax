package ch.dvbern.ebegu.services.util;

import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nullable;
import javax.persistence.criteria.*;
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
			createPredicateGesuchBetreuungenStatus(predicates, predicateObjectDto.getStatus());
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
