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

package ch.dvbern.ebegu.services.authentication;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.EJBAccessException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.HasMandant;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BooleanAuthorizer;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.lib.cdipersistence.Persistence;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.Validate;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN;
import static ch.dvbern.ebegu.enums.UserRole.ADMINISTRATOR_SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRole.JURIST;
import static ch.dvbern.ebegu.enums.UserRole.REVISOR;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_JA;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRole.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRole.SUPER_ADMIN;

/**
 * Authorizer Implementation
 */
@RequestScoped
@SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
public class AuthorizerImpl implements Authorizer, BooleanAuthorizer {

	private static final UserRole[] JA_OR_ADM_OR_SCH = { ADMIN, SACHBEARBEITER_JA, SCHULAMT, ADMINISTRATOR_SCHULAMT };
	private static final UserRole[] OTHER_AMT_ROLES = { REVISOR, JURIST, STEUERAMT };

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Persistence persistence;

	@Inject
	private FallService fallService;

	@Inject
	private InstitutionService institutionService;

	@Override
	public void checkReadAuthorizationGesuchId(@Nullable String gesuchId) {
		if (gesuchId != null) {
			checkReadAuthorization(getGesuchById(gesuchId));
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable Gesuch gesuch) {
		if (gesuch != null) {
			boolean allowed = isReadAuthorized(gesuch);
			if (!allowed) {
				throwViolation(gesuch);
			}
		}
	}

	@Override
	public void checkReadAuthorizationGesuche(@Nullable Collection<Gesuch> gesuche) {
		if (gesuche != null) {
			gesuche.forEach(this::checkReadAuthorization);
		}
	}

	@Override
	public void checkCreateAuthorizationFinSit(@Nonnull FinanzielleSituationContainer finanzielleSituation) {
		if (principalBean.isCallerInAnyOfRole(ADMIN, SUPER_ADMIN)) {
			return;
		}
		if (principalBean.isCallerInRole(GESUCHSTELLER)) {
			//gesuchsteller darf nur welche machen wenn ihm der zugehoerige Fall gehoert
			Gesuch gesuch = extractGesuch(finanzielleSituation);

			if (gesuch == null || !isWriteAuthorized(() -> extractGesuch(finanzielleSituation), principalBean.getPrincipal().getName())) {
				throwCreateViolation();
			}
		}
	}

	@Override
	public void checkReadAuthorizationFall(String fallId) {
		Optional<Fall> fallOptional = fallService.findFall(fallId);
		if (fallOptional.isPresent()) {
			Fall fall = fallOptional.get();
			checkReadAuthorizationFall(fall);
		}
	}

	@Override
	public void checkReadAuthorizationFall(@Nullable Fall fall) {
		boolean allowed = isReadAuthorizedFall(fall);
		if (!allowed) {
			throwViolation(fall);
		}
	}

	@Override
	public void checkReadAuthorizationFaelle(Collection<Fall> faelle) {
		if (faelle != null) {
			faelle.forEach(this::checkReadAuthorizationFall);
		}
	}

	private boolean isReadAuthorizedFall(@Nullable final Fall fall) {
		if (fall == null) {
			return true;
		}

		validateMandantMatches(fall);
		//berechtigte Rollen pruefen
		UserRole[] allowedRoles = { SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA,
			SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT, JURIST, REVISOR };
		if (principalBean.isCallerInAnyOfRole(allowedRoles)) {
			return true;
		}
		//Gesuchstellereigentuemer pruefen
		if (this.isGSOwner(() -> fall, principalBean.getPrincipal().getName())) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private void validateMandantMatches(@Nullable HasMandant mandantEntity) {
		//noinspection ConstantConditions
		if (mandantEntity == null || mandantEntity.getMandant() == null) {
			return;
		}
		Mandant mandant = mandantEntity.getMandant();
		if (!principalBean.isCallerInRole(SUPER_ADMIN)) {
			if (!mandant.equals(principalBean.getMandant())) {
				throwMandantViolation(mandantEntity); // super admin darf auch wenn er keinen mandant hat
			}
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable Fall fall) {
		if (fall != null) {

			boolean allowed = isReadAuthorizedFall(fall);
			if (!allowed) {
				throwViolation(fall);
			}
		}
	}

	@Override
	public void checkWriteAuthorization(Gesuch gesuch) throws EJBAccessException {
		if (gesuch == null) {
			return;
		}
		boolean allowedJAORGS = isWriteAuthorized(gesuch, principalBean.getPrincipal().getName());

		//Wir pruefen schulamt separat (schulamt darf schulamt-only Gesuche vom Status FREIGABEQUITTUNG zum Status SCHULAMT schieben)
		boolean allowedSchulamt = false;
		if (!allowedJAORGS && principalBean.isCallerInAnyOfRole(SCHULAMT, ADMINISTRATOR_SCHULAMT)
			&& AntragStatus.FREIGABEQUITTUNG == gesuch.getStatus()) {
			allowedSchulamt = true;
		}

		//Wir pruefen steueramt separat (steueramt darf nur das Gesuch speichern wenn es im Status PRUEFUNG_STV oder IN_BEARBEITUNG_STV ist)
		boolean allowedSteueramt = false;
		if (!allowedJAORGS && !allowedSchulamt && principalBean.isCallerInRole(STEUERAMT)
			&& (AntragStatus.PRUEFUNG_STV == gesuch.getStatus() || AntragStatus.IN_BEARBEITUNG_STV == gesuch.getStatus()
			|| AntragStatus.GEPRUEFT_STV == gesuch.getStatus())) {
			allowedSteueramt = true;
		}

		if (!allowedJAORGS && !allowedSchulamt && !allowedSteueramt) {
			throwViolation(gesuch);
		}
	}

	@Override
	public void checkWriteAuthorization(Verfuegung verfuegung) {
		//nur sachbearbeiter ja und admins duefen verfuegen
		if (!principalBean.isCallerInAnyOfRole(SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA)) {
			throwViolation(verfuegung);
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable FinanzielleSituationContainer finanzielleSituation) {
		if (finanzielleSituation == null) {
			return;
		}
		String name = principalBean.getPrincipal().getName();
		Gesuch gesuch = extractGesuch(finanzielleSituation);
		boolean writeAllowed = isWriteAuthorized(gesuch, name);
		boolean isMutation = finanzielleSituation.getVorgaengerId() != null;
		//in einer Mutation kann der Gesuchsteller die Finanzielle Situation nicht anpassen
		if (!writeAllowed || (isMutation && principalBean.isCallerInRole(GESUCHSTELLER))) {
			throwViolation(finanzielleSituation);
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable Betreuung betr) {
		if (betr == null) {
			return;
		}
		boolean allowed = isReadAuthorized(betr);
		if (!allowed) {
			throwViolation(betr);
		}
	}

	@Override
	public void checkReadAuthorizationForAllBetreuungen(@Nullable Collection<Betreuung> betreuungen) {
		if (betreuungen != null) {
			betreuungen.stream()
				.filter(betreuung -> !isReadAuthorized(betreuung))
				.findAny()
				.ifPresent(this::throwViolation);
		}
	}

	@Override
	public void checkReadAuthorizationForAnyBetreuungen(@Nullable Collection<Betreuung> betreuungen) {
		if (betreuungen != null && !betreuungen.isEmpty()
			&& betreuungen.stream().noneMatch(this::isReadAuthorized)) {
			throw new EJBAccessException(
				"Access Violation"
					+ " user is not allowed for any of these betreuungen");
		}
	}

	@Override
	public void checkReadAuthorization(Verfuegung verfuegung) {
		if (verfuegung != null) {
			//an betreuung delegieren
			checkReadAuthorization(verfuegung.getBetreuung());
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable WizardStep step) {
		if (step != null) {
			checkReadAuthorization(step.getGesuch());
		}
	}

	@Override
	public void checkReadAuthorizationVerfuegungen(Collection<Verfuegung> verfuegungen) {
		if (verfuegungen != null) {
			verfuegungen.forEach(this::checkReadAuthorization);
		}
	}

	@Override
	public void checkWriteAuthorization(Betreuung betreuungToRemove) {
		if (betreuungToRemove == null) {
			return;
		}
		Gesuch gesuch = extractGesuch(betreuungToRemove);
		boolean allowed = isWriteAuthorized(gesuch, principalBean.getPrincipal().getName());
		if (!allowed) {
			throwViolation(betreuungToRemove);
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable ErwerbspensumContainer ewpCnt) {
		if (ewpCnt != null) {
			//Wenn wir hier 100% korrekt sein wollen muessten wir auch noch das Gesuch laden und den status pruefen.
			UserRole[] allowedRoles = { SACHBEARBEITER_JA, SUPER_ADMIN, ADMIN, REVISOR, JURIST, ADMINISTRATOR_SCHULAMT, SCHULAMT };
			boolean allowed = isInRoleOrGSOwner(allowedRoles, () -> extractGesuch(ewpCnt), principalBean.getPrincipal().getName());
			if (!allowed) {
				throwViolation(ewpCnt);
			}
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable FinanzielleSituationContainer finanzielleSituation) {
		if (finanzielleSituation != null) {
			// hier fuer alle lesbar ausser fuer institution/traegerschaft
			String name = principalBean.getPrincipal().getName();
			Gesuch owningGesuch = extractGesuch(finanzielleSituation);
			if (owningGesuch == null) {
				//wenn wir keinen fall finden dann gehen wir davon aus, dass die finanzielle Situation bzw ihr Gesuchsteller noch nicht gespeichert ist
				return;
			}
			Boolean allowedAdminOrSachbearbeiter = isAllowedAdminOrSachbearbeiter(owningGesuch);
			Boolean allowedSchulamt = isAllowedSchulamt(owningGesuch);

			Boolean allowedOthers = false;
			if (principalBean.isCallerInAnyOfRole(OTHER_AMT_ROLES) && (owningGesuch.getStatus().isReadableByJugendamtSchulamtSteueramt())) {
				allowedOthers = true;
			}
			Boolean allowedOwner = isGSOwner(owningGesuch::getFall, name);

			if (!(allowedAdminOrSachbearbeiter || allowedSchulamt || allowedOthers || allowedOwner)) {
				throwViolation(finanzielleSituation);
			}
		}
	}

	@Override
	public void checkReadAuthorizationForFreigabe(Gesuch gesuch) {
		if (gesuch != null) {
			boolean freigebeReadPrivilege = isReadAuthorizedFreigabe(gesuch);
			if (!(freigebeReadPrivilege || isReadAuthorized(gesuch))) {
				throwViolation(gesuch);
			}
		}
	}

	private boolean isReadAuthorizedFreigabe(Gesuch gesuch) {
		if (AntragStatus.FREIGABEQUITTUNG == gesuch.getStatus()) {
			if (principalBean.isCallerInAnyOfRole(SCHULAMT, ADMINISTRATOR_SCHULAMT)) {
				if (gesuch.hasBetreuungOfSchulamt()) {
					return true; //schulamt darf nur solche lesen die nur_schulamt sind
				}
			} else if (!gesuch.hasOnlyBetreuungenOfSchulamt()) {
				return true;     //nicht schulamtbenutzer duerfen keine lesen die exklusiv schulamt sind
			}
		}
		return false;
	}

	@Override
	public void checkReadAuthorization(@Nonnull Collection<FinanzielleSituationContainer> finanzielleSituationen) {
		finanzielleSituationen.forEach(this::checkReadAuthorization);
	}

	private boolean isInRoleOrGSOwner(UserRole[] allowedRoles, Supplier<Gesuch> gesuchSupplier, String principalName) {
		if (principalBean.isCallerInAnyOfRole(allowedRoles)) {
			return true;
		}

		if (isGSOwner(() -> gesuchSupplier.get().getFall(), principalName)) {
			return true;
		}
		return false;
	}

	private boolean isGSOwner(Supplier<Fall> fallSupplier, String principalName) {
		if (principalBean.isCallerInRole(GESUCHSTELLER.name())) {
			Fall fall = fallSupplier.get();
			if ((fall != null) && (fall.getUserErstellt() == null ||
				(fall.getBesitzer() != null && fall.getBesitzer().getUsername().equalsIgnoreCase(principalName)))) {
				return true;
			}
		}
		return false;
	}

	private boolean isReadAuthorized(final Betreuung betreuung) {
		if (isAllowedAdminOrSachbearbeiter(betreuung.extractGesuch())) {
			return true;
		}

		boolean isOwnerOrAdmin = isGSOwner(() -> betreuung.extractGesuch().getFall(), principalBean.getPrincipal().getName());
		if (isOwnerOrAdmin) {
			return true;
		}

		if (principalBean.isCallerInRole(SACHBEARBEITER_INSTITUTION)) {
			Institution institution = principalBean.getBenutzer().getInstitution();
			Validate.notNull(institution, "Institution des Sachbearbeiters muss gesetzt sein " + principalBean.getBenutzer());
			return betreuung.getInstitutionStammdaten().getInstitution().equals(institution);
		}
		if (principalBean.isCallerInRole(SACHBEARBEITER_TRAEGERSCHAFT)) {
			Traegerschaft traegerschaft = principalBean.getBenutzer().getTraegerschaft();
			Validate.notNull(traegerschaft, "Traegerschaft des des Sachbearbeiters muss gesetzt sein " + principalBean.getBenutzer());
			Collection<Institution> institutions = institutionService.getAllInstitutionenFromTraegerschaft(traegerschaft.getId());
			Institution instToMatch = betreuung.getInstitutionStammdaten().getInstitution();
			return institutions.stream().anyMatch(instToMatch::equals);
		}
		if (principalBean.isCallerInAnyOfRole(SCHULAMT, ADMINISTRATOR_SCHULAMT)) {
			return betreuung.getBetreuungsangebotTyp().isSchulamt();
		}
		return false;

	}

	@Override
	public void checkReadAuthorizationFinSit(@Nullable Gesuch gesuch) {
		if (gesuch != null) {
			FinanzielleSituationContainer finSitGs1 = gesuch.getGesuchsteller1() != null ? gesuch.getGesuchsteller1().getFinanzielleSituationContainer() : null;
			FinanzielleSituationContainer finSitGs2 = gesuch.getGesuchsteller2() != null ? gesuch.getGesuchsteller2().getFinanzielleSituationContainer() : null;
			checkReadAuthorization(finSitGs1);
			checkReadAuthorization(finSitGs2);
		}
	}

	private boolean isReadAuthorized(Gesuch entity) {
		if (isAllowedAdminOrSachbearbeiter(entity)) {
			return true;
		}
		if (isGSOwner(entity::getFall, principalBean.getPrincipal().getName())) {
			return true;
		}
		if (principalBean.isCallerInRole(SACHBEARBEITER_INSTITUTION)) {
			Institution institution = principalBean.getBenutzer().getInstitution();
			Validate.notNull(institution, "Institution des Sachbearbeiters muss gesetzt sein " + principalBean.getBenutzer());
			return entity.hasBetreuungOfInstitution(institution); //@reviewer: oder besser ueber service ?
		}
		if (principalBean.isCallerInRole(SACHBEARBEITER_TRAEGERSCHAFT)) {
			Traegerschaft traegerschaft = principalBean.getBenutzer().getTraegerschaft();
			Validate.notNull(traegerschaft, "Traegerschaft des des Sachbearbeiters muss gesetzt sein " + principalBean.getBenutzer());
			Collection<Institution> institutions = institutionService.getAllInstitutionenFromTraegerschaft(traegerschaft.getId());
			return institutions.stream().anyMatch(entity::hasBetreuungOfInstitution);  // irgend eine der betreuungen des gesuchs matched
		}
		if (isAllowedSchulamt(entity)) {
			return true;
		}
		if (isAllowedSteueramt(entity)) {
			return true;
		}
		if (isAllowedJuristOrRevisor(entity)) {
			return true;
		}
		return false;
	}

	@Nonnull
	private Boolean isAllowedAdminOrSachbearbeiter(Gesuch gesuch) {
		if (principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			return true;
		}
		//JA/SCH Benutzer duerfen nur freigegebene Gesuche anschauen
		if (principalBean.isCallerInAnyOfRole(JA_OR_ADM_OR_SCH)) {
			return gesuch.getStatus().isReadableByJugendamtSchulamtSteueramt();
		}
		return isAllowedJuristOrRevisor(gesuch);
	}

	private boolean isAllowedSchulamt(Gesuch entity) {
		if (principalBean.isCallerInAnyOfRole(SCHULAMT, ADMINISTRATOR_SCHULAMT)) {
			return entity.getStatus().isReadableBySchulamtSachbearbeiter();
		}
		return false;
	}

	private boolean isAllowedSteueramt(Gesuch entity) {
		if (principalBean.isCallerInRole(STEUERAMT)) {
			return entity.getStatus().isReadableBySteueramt();
		}
		return false;
	}

	private boolean isAllowedJuristOrRevisor(Gesuch gesuch) {
		if (principalBean.isCallerInRole(JURIST)) {
			return gesuch.getStatus().isReadableByJurist();
		}
		if (principalBean.isCallerInRole(REVISOR)) {
			return gesuch.getStatus().isReadableByRevisor();
		}
		return false;
	}

	//this method is named slightly wrong because it only checks write authorization for Admins SachbearbeiterJA and GS
	private boolean isWriteAuthorized(Supplier<Gesuch> gesuchSupplier, String principalName) {
		if (principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			return true;
		}
		Gesuch gesuch = gesuchSupplier.get();
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		if (userRole == null) {
			return false;
		}
		if (!AntragStatus.writeAllowedForRole(userRole).contains(gesuch.getStatus())) {
			String msg = "Cannot update Gesuch " + gesuch.getId() + " in Status " + gesuch.getStatus() + " in UserRole " + userRole;
			throw new EbeguRuntimeException("isWriteAuthorized", ErrorCodeEnum.ERROR_INVALID_EBEGUSTATE, gesuch.getId(), msg);
		}
		if (principalBean.isCallerInAnyOfRole(JA_OR_ADM_OR_SCH)) {
			return gesuch.getStatus().isReadableByJugendamtSchulamtSteueramt() || AntragStatus.FREIGABEQUITTUNG == gesuch.getStatus();
		}

		if (isGSOwner(gesuch::getFall, principalName)) {
			return true;
		}
		return false;
	}

	private boolean isWriteAuthorized(Gesuch entity, String principalName) {
		return isWriteAuthorized(() -> entity, principalName);
	}

	private void throwCreateViolation() {
		throw new EJBAccessException(
			"Access Violation"
				+ " user is not allowed to create entity:"
				+ " for current user: " + principalBean.getPrincipal()
		);
	}

	private void throwViolation(AbstractEntity abstractEntity) {
		throw new EJBAccessException(
			"Access Violation"
				+ " for Entity: " + abstractEntity.getClass().getSimpleName() + "(id=" + abstractEntity.getId() + "):"
				+ " for current user: " + principalBean.getPrincipal()
				+ " in role(s): " + principalBean.discoverRoles()
		);
	}

	private void throwMandantViolation(HasMandant mandantEntity) {
		throw new EJBAccessException(
			"Mandant Access Violation"
				+ " for Entity: " + mandantEntity.getClass().getSimpleName() + "(id=" + mandantEntity.getId() + "):"
				+ " for current user: " + principalBean.getPrincipal()
		);
	}

	public Gesuch getGesuchById(String gesuchID) {
		return persistence.find(Gesuch.class, gesuchID);
	}

	private Gesuch extractGesuch(Betreuung betreuung) {
		return betreuung.extractGesuch();
	}

	@Nullable
	private Gesuch extractGesuch(@Nonnull FinanzielleSituationContainer finanzielleSituationContainer) {
		return extractGesuch(finanzielleSituationContainer.getGesuchsteller());
	}

	@Nullable
	private Gesuch extractGesuch(@Nonnull ErwerbspensumContainer erwerbspensumContainer) {
		return extractGesuch(erwerbspensumContainer.getGesuchsteller());
	}

	@Nullable
	private Gesuch extractGesuch(GesuchstellerContainer gesuchstellerContainer) {
		//db abfrage des falls fuer den gesuchsteller
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateGs1 = cb.equal(root.get(Gesuch_.gesuchsteller1), gesuchstellerContainer);
		Predicate predicateGs2 = cb.equal(root.get(Gesuch_.gesuchsteller2), gesuchstellerContainer);
		Predicate predicateGs1OrGs2 = cb.or(predicateGs1, predicateGs2);
		query.where(predicateGs1OrGs2);
		return persistence.getCriteriaSingleResult(query);
	}

	@Override
	public boolean hasReadAuthorization(@Nonnull Gesuch gesuch) {
		return isReadAuthorized(gesuch);
	}

	@Override
	public void checkWriteAuthorizationMitteilung(@Nullable Mitteilung mitteilung) {
		if (mitteilung != null) {
			UserRole userRole = principalBean.discoverMostPrivilegedRole();
			Objects.requireNonNull(userRole);
			switch (userRole) {
			case GESUCHSTELLER: {
				// Beim schreiben (Entwurf speichern oder Mitteilung senden) muss der eingeloggte GS der Absender sein
				if (!isCurrentUserMitteilungsSender(mitteilung)) {
					throwViolation(mitteilung);
				}
				break;
			}
			case SACHBEARBEITER_INSTITUTION:
			case SACHBEARBEITER_TRAEGERSCHAFT:
				if (!isSenderTyp(mitteilung, MitteilungTeilnehmerTyp.INSTITUTION)) {
					throwViolation(mitteilung);
				}
				break;
			case SACHBEARBEITER_JA:
			case ADMIN:
			case SCHULAMT:
			case ADMINISTRATOR_SCHULAMT:
				if (!isSenderTyp(mitteilung, MitteilungTeilnehmerTyp.JUGENDAMT)) {
					throwViolation(mitteilung);
				}
				break;
			case SUPER_ADMIN: {
				// Superadmin darf alles!
				break;
			}
			default: {
				// Alle anderen Rollen sind nicht berechtigt
				throwViolation(mitteilung);
			}
			}
		}
	}

	@Override
	public void checkReadAuthorizationMitteilungen(@Nonnull Collection<Mitteilung> mitteilungen) {
		mitteilungen.forEach(this::checkReadAuthorizationMitteilung);
	}

	@Override
	public void checkReadAuthorizationMitteilung(@Nullable Mitteilung mitteilung) {
		if (mitteilung != null) {
			UserRole userRole = principalBean.discoverMostPrivilegedRole();
			Objects.requireNonNull(userRole);
			// Beim Lesen einer Mitteilung muss der eingeloggte Benutzer
			// - der Sender oder der Empfaenger sein (GESUCHSTELLER)
			// - der Sender sein (INSTITUTIONEN)
			// - SenderTyp oder EmpfaengerTyp muss JUGENDAMT sein (SACHBEARBEITER_JA)
			switch (userRole) {
			case GESUCHSTELLER: {
				if (!(isCurrentUserMitteilungsSender(mitteilung) || isCurrentUserMitteilungsEmpfaenger(mitteilung))) {
					throwViolation(mitteilung);
				}
				break;
			}
			case SACHBEARBEITER_INSTITUTION:
			case SACHBEARBEITER_TRAEGERSCHAFT: {
				if (!isSenderTypOrEmpfaengerTyp(mitteilung, MitteilungTeilnehmerTyp.INSTITUTION)) {
					throwViolation(mitteilung);
				}
				break;
			}
			case SACHBEARBEITER_JA:
			case ADMINISTRATOR_SCHULAMT:
			case SCHULAMT:
			case REVISOR: {
				if (!isSenderTypOrEmpfaengerTyp(mitteilung, MitteilungTeilnehmerTyp.JUGENDAMT)) {
					throwViolation(mitteilung);
				}
				break;
			}
			case SUPER_ADMIN:
			case ADMIN: {
				break;
			}
			default: {
				throwViolation(mitteilung);
			}
			}
		}
	}

	private boolean isSenderTypOrEmpfaengerTyp(@Nullable Mitteilung mitteilung, MitteilungTeilnehmerTyp typ) {
		if (mitteilung != null) {
			return mitteilung.getSenderTyp() == typ || mitteilung.getEmpfaengerTyp() == typ;
		}
		return false;
	}

	private boolean isSenderTyp(@Nullable Mitteilung mitteilung, MitteilungTeilnehmerTyp typ) {
		if (mitteilung != null) {
			return mitteilung.getSenderTyp() == typ;
		}
		return false;
	}

	private boolean isCurrentUserMitteilungsSender(@Nonnull Mitteilung mitteilung) {
		return principalBean.getBenutzer().equals(mitteilung.getSender());
	}

	private boolean isCurrentUserMitteilungsEmpfaenger(@Nonnull Mitteilung mitteilung) {
		return principalBean.getBenutzer().equals(mitteilung.getEmpfaenger());
	}
}
