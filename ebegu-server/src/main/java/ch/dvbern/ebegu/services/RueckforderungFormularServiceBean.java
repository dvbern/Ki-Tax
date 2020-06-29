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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungFormular_;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.RueckforderungInstitutionTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.interceptors.UpdateRueckfordFormStatusInterceptor;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Stateless
@Local(RueckforderungFormularService.class)
public class RueckforderungFormularServiceBean extends AbstractBaseService implements RueckforderungFormularService {

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	public List<RueckforderungFormular> initializeRueckforderungFormulare() {

		Collection<InstitutionStammdaten> institutionenStammdatenCollection = institutionStammdatenService.getAllInstitutionStammdaten();
		Collection<RueckforderungFormular> rueckforderungFormularCollection = getAllRueckforderungFormulare();

		List<RueckforderungFormular> rueckforderungFormulare = new ArrayList<>();
		for (InstitutionStammdaten institutionStammdaten : institutionenStammdatenCollection) {
			// neues Formular erstellen falls es sich un eine kita oder TFO handelt und noch kein Formular existiert
			if ((institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.KITA ||
				institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESFAMILIEN) &&
				!isFormularExisting(institutionStammdaten, rueckforderungFormularCollection)
				&& institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine() != null
				&& institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine().getIban() != null) {

				RueckforderungFormular formular = new RueckforderungFormular();
				formular.setInstitutionStammdaten(institutionStammdaten);
				formular.setStatus(RueckforderungStatus.NEU);
				rueckforderungFormulare.add(createRueckforderungFormular(formular));
			}
		}
		return rueckforderungFormulare;
	}

	/**
	 * Falls in der Liste der Rückforderungsformulare die Institution bereits existiert, wird true zurückgegeben
	 */
	private boolean isFormularExisting(@Nonnull InstitutionStammdaten stammdaten,
		@Nonnull Collection<RueckforderungFormular> rueckforderungFormularCollection
	) {
		List<RueckforderungFormular> filteredFormulare = rueckforderungFormularCollection
			.stream()
			.filter(formular -> formular.getInstitutionStammdaten().getId().equals(stammdaten.getId()))
			.collect(Collectors.toList());
		return !filteredFormulare.isEmpty();
	}

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	public RueckforderungFormular createRueckforderungFormular(@Nonnull RueckforderungFormular rueckforderungFormular) {
		return persistence.persist(rueckforderungFormular);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public Collection<RueckforderungFormular> getAllRueckforderungFormulare(){
		return criteriaQueryHelper.getAll(RueckforderungFormular.class);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION ,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public List<RueckforderungFormular> getRueckforderungFormulareForCurrentBenutzer() {
		Collection<RueckforderungFormular> allRueckforderungFormulare = getAllRueckforderungFormulare();
		Benutzer currentBenutzer = principalBean.getBenutzer();
		if(currentBenutzer.getRole().isRoleMandant() || currentBenutzer.getRole().isSuperadmin()){
			return allRueckforderungFormulare.stream().collect(Collectors.toList());
		}
		Collection<Institution> institutionenCurrentBenutzer =
			institutionService.getInstitutionenEditableForCurrentBenutzer(false);

		return allRueckforderungFormulare.stream().filter(formular -> {
			for (Institution institution : institutionenCurrentBenutzer) {
				if (institution.getId().equals(formular.getInstitutionStammdaten().getInstitution().getId())) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	@Interceptors(UpdateRueckfordFormStatusInterceptor.class)
	public Optional<RueckforderungFormular> findRueckforderungFormular(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		RueckforderungFormular rueckforderungFormular = persistence.find(RueckforderungFormular.class, id);
		return Optional.ofNullable(rueckforderungFormular);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public RueckforderungFormular save(@Nonnull RueckforderungFormular rueckforderungFormular) {
		Objects.requireNonNull(rueckforderungFormular);
		changeStatusAndCopyFields(rueckforderungFormular);
		final RueckforderungFormular mergedRueckforderungFormular = persistence.merge(rueckforderungFormular);
		return mergedRueckforderungFormular;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	public Collection<RueckforderungFormular> getRueckforderungFormulareByStatus(@Nonnull List<RueckforderungStatus> status) {
		Objects.requireNonNull(status.get(0), "Mindestens ein Status muss angegeben werden");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<RueckforderungFormular> query = cb.createQuery(RueckforderungFormular.class);

		final Root<RueckforderungFormular> root = query.from(RueckforderungFormular.class);

		Predicate predicateStatus = root.get(RueckforderungFormular_.status).in(status);
		query.where(predicateStatus);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public RueckforderungFormular addMitteilung(
		@Nonnull RueckforderungFormular formular,
		@Nonnull RueckforderungMitteilung mitteilung
	) {
		formular.addRueckforderungMitteilung(mitteilung);
		return persistence.persist(formular);
	}

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	public void initializePhase2() {
		//set Application Properties zu true
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.KANTON_NOTVERORDNUNG_PHASE_2_AKTIV, "true");
		//get alle Ruckforderungsformular, check status and changed if needed
		ArrayList<RueckforderungStatus> statusGeprueftStufe1 = new ArrayList<>();
		statusGeprueftStufe1.add(RueckforderungStatus.GEPRUEFT_STUFE_1);
		Collection<RueckforderungFormular> formulareWithStatusGeprueftStufe1 =
			getRueckforderungFormulareByStatus(statusGeprueftStufe1);
		for (RueckforderungFormular formular : formulareWithStatusGeprueftStufe1) {
			save(formular);
		}
	}

	private void changeStatusAndCopyFields(@Nonnull RueckforderungFormular rueckforderungFormular) {
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		switch (rueckforderungFormular.getStatus()) {
		case EINGELADEN: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1);
			}
			break;
		}
		case IN_BEARBEITUNG_INSTITUTION_STUFE_1: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1);
				rueckforderungFormular.setStufe1KantonKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe1KantonKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe1KantonKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeBetreuung());
			}
			break;
		}
		case IN_BEARBEITUNG_INSTITUTION_STUFE_2: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				RueckforderungStatus nextStatus = rueckforderungFormular.getStatus();
				if (rueckforderungFormular.getInstitutionTyp() == RueckforderungInstitutionTyp.OEFFENTLICH) {
					nextStatus = RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2;
				} else {
					// Definitiv ist es, wenn es entweder gar nicht beantragt wurde, oder schon verfuegt ist
					if (rueckforderungFormular.isKurzarbeitProzessBeendet() && rueckforderungFormular.isCoronaErwerbsersatzProzessBeendet()) {
						nextStatus = RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2;
					} else {
						nextStatus = RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2_PROVISORISCH;
					}
				}
				rueckforderungFormular.setStatus(nextStatus);
				rueckforderungFormular.setStufe2KantonKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe2KantonKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe2KantonKostenuebernahmeBetreuung(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeBetreuung());
			}
			break;
		}
		case IN_PRUEFUNG_KANTON_STUFE_1: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.GEPRUEFT_STUFE_1);
			}
			break;
		}
		case GEPRUEFT_STUFE_1: {
			if (applicationPropertyService.isKantonNotverordnungPhase2Aktiviert()
					&& principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2);
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1KantonKostenuebernahmeBetreuung());
			}
			break;
		}
		default:
			break;
		}
	}
}
