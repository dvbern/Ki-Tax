package ch.dvbern.ebegu.services.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.SingularAttribute;

import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AlleFaelleView;
import ch.dvbern.ebegu.entities.AlleFaelleViewKind;
import ch.dvbern.ebegu.entities.AlleFaelleViewKind_;
import ch.dvbern.ebegu.entities.AlleFaelleView_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import ch.dvbern.ebegu.util.Constants;

public class AlleFaellePredicateBuilder {

	private static final Pattern COMPILE = Pattern.compile("^0+(?!$)");
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
			String fallNummerWithWildcards = getFallNummerSearchString(predicateObjectDto);
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

	/*
	Es gibt zwei Möglichkeiten, nach einer Fallnummer zu suchen:
	- Nur die Nummer, z.B. 123, dann werden alle entsprechenden Fallnummern gefunden, also auch 1234 oder 9123 oder 91234
	- Die exakte Nummer, beginnend mit 0, oder 00. Fallnummer ist immer sechststellig, ein Suchstring 0012 findet also 001234
	  und 00012 findet 000123.
	 */
	private static String getFallNummerSearchString(AntragPredicateObjectDTO predicateObjectDto) {
		var fallNr = predicateObjectDto.getFallNummer();
		StringBuilder fallNummerWithWildcards;
		if (fallNr.startsWith("0")) {
			fallNummerWithWildcards = new StringBuilder(fallNr);
			// add wildcards until fallnummer length reached
			while (fallNummerWithWildcards.length() < Constants.FALLNUMMER_LENGTH) {
				fallNummerWithWildcards.append('_');
			}
			// remove leading zeros
			var pattern = Pattern.compile("^0+(?!$)");
			return pattern.matcher(fallNummerWithWildcards.toString()).replaceFirst("");
		}
		return SearchUtil.withWildcards(predicateObjectDto.getFallNummer());
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
