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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GeneratedDokument;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.HasMandant;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.RollenAbhaengigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BooleanAuthorizer;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.lib.cdipersistence.Persistence;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRole.JURIST;
import static ch.dvbern.ebegu.enums.UserRole.REVISOR;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRole.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRole.SUPER_ADMIN;
import static ch.dvbern.ebegu.enums.UserRole.getAllAdminRoles;
import static ch.dvbern.ebegu.util.Constants.ANONYMOUS_USER_USERNAME;
import static ch.dvbern.ebegu.util.Constants.LOGINCONNECTOR_USER_USERNAME;

/**
 * Authorizer Implementation
 */
@RequestScoped
@SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
public class AuthorizerImpl implements Authorizer, BooleanAuthorizer {

	private static final UserRole[] JA_OR_ADM_OR_SCH =
		{ ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS };
	private static final UserRole[] OTHER_AMT_ROLES =
		{ REVISOR, JURIST, STEUERAMT, ADMIN_MANDANT, SACHBEARBEITER_MANDANT };

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Persistence persistence;

	@Inject
	private DossierService dossierService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private InstitutionStammdatenService stammdatenService;

	/**
	 * All non-gemeinde-roles are allowed to see any gemeinde. This is needed because Institutionen and Gesuchsteller need to
	 * see all gemeinde. All other roles which must have a gemeinde linked to it can only see those gemeinde which they belong to
	 */
	@Override
	public void checkReadAuthorization(@Nullable Gemeinde gemeinde) {
		if (gemeinde != null) {
			boolean allGemeindenAllowed = principalBean.isCallerInAnyOfRole(
				UserRole.getRolesWithoutAbhaengigkeit(RollenAbhaengigkeit.GEMEINDE));
			if (allGemeindenAllowed) {
				return;
			}
			boolean allowedForGemeinde = isUserAllowedForGemeinde(gemeinde) &&
				principalBean.isCallerInAnyOfRole(ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS,
					ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, REVISOR, STEUERAMT, JURIST);
			if (!allowedForGemeinde) {
				throwViolation(gemeinde);
			}
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable Gemeinde gemeinde) {
		if (gemeinde != null) {
			boolean allGemeindenAllowed = principalBean.isCallerInAnyOfRole(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT);
			if (allGemeindenAllowed) {
				return;
			}
			boolean allowedForGemeinde = isUserAllowedForGemeinde(gemeinde) &&
				principalBean.isCallerInAnyOfRole(ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE);
			if (!allowedForGemeinde) {
				throwViolation(gemeinde);
			}
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

	@Override
	public void checkReadAuthorizationDossiers(Collection<Dossier> dossiers) {
		if (dossiers != null) {
			dossiers.forEach(this::checkReadAuthorizationDossier);
		}
	}

	@Override
	public void checkReadAuthorizationDossier(@Nonnull String dossierId) {
		Optional<Dossier> dossierOptional = dossierService.findDossier(dossierId);
		dossierOptional.ifPresent(this::checkReadAuthorizationDossier);
	}

	@Override
	public void checkReadAuthorizationDossier(@Nullable Dossier dossier) {
		if (dossier != null) {
			boolean allowed = isReadAuthorizedDossier(dossier);
			if (!allowed) {
				throwViolation(dossier);
			}
		}
	}

	private boolean isReadAuthorizedFall(@Nullable final Fall fall) {
		if (fall == null) {
			return true;
		}

		if (!isMandantMatching(fall)) {
			return false;
		}

		// Gemeinde muss fuer Mandan-Rollen nicht geprueft werden
		if (!principalBean.isCallerInAnyOfRole(ADMIN_MANDANT, SACHBEARBEITER_MANDANT)) {
			validateGemeindeMatches(fall);
		}

		//berechtigte Rollen pruefen
		UserRole[] allowedRoles = { SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TS,
			SACHBEARBEITER_TS, STEUERAMT, JURIST,
			REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT };
		if (principalBean.isCallerInAnyOfRole(allowedRoles)) {
			return true;
		}
		//Gesuchstellereigentuemer pruefen
		return this.isGSOwner(() -> fall);
	}

	/**
	 * For a Role that belongs to a Gemeinde it must be checked that at least one dossier of the fall belongs to the
	 * Gemeinde of the principal
	 */
	private void validateGemeindeMatches(@Nonnull Fall fall) {
		if (principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone().isRoleGemeindeabhaengig()) {
			// in this case we cannot use the service dossierService.findDossiersByFall(fall.getId()) directly because
			// it checks the rights again and we enter in an infinite loop
			Collection<Dossier> dossiers = criteriaQueryHelper
				.getEntitiesByAttribute(Dossier.class, fall, Dossier_.fall);
			if (!dossiers.isEmpty()) { // for no dossiers in Fall no validation is required
				final boolean belongsToGemeinde = dossiers.stream()
					.anyMatch(dossier -> principalBean.belongsToGemeinde(dossier.getGemeinde()));
				if (!belongsToGemeinde) {
					throwViolation(fall);
				}
			}
		}
	}

	/**
	 * For a Role that belongs to a Gemeinde it must be checked that the dossier belongs to the Gemeinde of the
	 * principal
	 */
	private void validateGemeindeMatches(@Nonnull Dossier dossier) {
		if (!isUserAllowedForGemeinde(dossier.getGemeinde())) {
			throwViolation(dossier);
		}
	}

	@Override
	public boolean isReadAuthorizedDossier(@Nullable final Dossier dossier) {
		if (dossier == null) {
			return true;
		}

		if (!isMandantMatching(dossier.getFall())) {
			return false;
		}

		// Gemeinde muss fuer Mandant-Rollen nicht geprueft werden
		if (!principalBean.isCallerInAnyOfRole(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT)
			&& !isUserAllowedForGemeinde(dossier.getGemeinde())) {
			return false;
		}

		//berechtigte Rollen pruefen
		UserRole[] allowedRoles = { SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TS,
			SACHBEARBEITER_TS, STEUERAMT, JURIST, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT };
		if (principalBean.isCallerInAnyOfRole(allowedRoles)) {
			return true;
		}
		//TODO (team) hier muss dann spaeter die Rolle genauer geprüft werden!
		//Gesuchstellereigentuemer pruefen
		return this.isGSOwner(dossier::getFall);
	}

	// todo diese Methode immer verwenden. in die MEthode isReadAuthorizedDossier integrieren
	@Override
	public boolean isReadCompletelyAuthorizedDossier(@Nullable Dossier dossier) {
		if (dossier == null) {
			return true;
		}
		if (!isReadAuthorizedDossier(dossier)) {
			return false;
		}

		final List<Gesuch> allGesuchForDossier = gesuchService.getAllGesuchForDossier(dossier.getId());

		return allGesuchForDossier.isEmpty() || allGesuchForDossier.stream()
			.anyMatch(this::isReadAuthorized);

	}

	private void checkMandantMatches(@Nullable HasMandant mandantEntity) {
		if (!isMandantMatching(mandantEntity)) {
			throwMandantViolation(mandantEntity); // super admin darf auch wenn er keinen mandant hat
		}
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private boolean isMandantMatching(@Nullable HasMandant mandantEntity) {
		if (mandantEntity == null || mandantEntity.getMandant() == null) {
			return true;
		}
		Mandant mandant = mandantEntity.getMandant();
		if (!principalBean.isCallerInRole(SUPER_ADMIN)) {
			if (!mandant.equals(principalBean.getMandant())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void checkSuperadmin() {
		if (!principalBean.isCallerInRole(SUPER_ADMIN)) {
			throw new EJBAccessException("Access Violation. Only accessible for SUPERADMIN");
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
	public void checkWriteAuthorizationDossier(@Nullable Dossier dossier) {
		if (dossier != null) {
			boolean allowed = isReadAuthorizedDossier(dossier);
			if (!allowed) {
				throwViolation(dossier);
			}
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable Gesuch gesuch) throws EJBAccessException {
		if (gesuch == null) {
			return;
		}
		boolean allowedJAORGS = isWriteAuthorized(gesuch);

		//Wir pruefen schulamt separat (schulamt darf schulamt-only Gesuche vom Status FREIGABEQUITTUNG zum Status
		// SACHBEARBEITER_TS schieben)
		boolean allowedSchulamt = false;
		if (!allowedJAORGS && principalBean.isCallerInAnyOfRole(
			SACHBEARBEITER_TS,
			ADMIN_TS,
			SACHBEARBEITER_GEMEINDE,
			ADMIN_GEMEINDE)
			&& AntragStatus.FREIGABEQUITTUNG == gesuch.getStatus()) {
			allowedSchulamt = true;
		}

		//Wir pruefen steueramt separat (steueramt darf nur das Gesuch speichern wenn es im Status PRUEFUNG_STV oder
		// IN_BEARBEITUNG_STV ist)
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
	public void checkWriteAuthorization(@Nullable Verfuegung verfuegung) {
		if (verfuegung == null) {
			return;
		}
		boolean isTagesschule = verfuegung.getPlatz().getBetreuungsangebotTyp().isTagesschule();
		if (isTagesschule) {
			// Der Gesuchsteller darf seine eigene Verfügung speichern: Wenn er eine Mutation macht
			// wird im Vorgängergesuch eine Verfügung erstellt
			if (isGSOwner(() -> extractGesuch(verfuegung.getPlatz()).getFall())) {
				return;
			}
			// Tagesschulen werden neu "verfuegt", wenn die Module akzeptiert werden und das Gesuch schon
			// verfügt/abgeschlossen war. Damit müssen alle Rollen, die Module akzeptieren dürfen, auch
			// die Verfügung speichern dürfen!
			if (!principalBean.isCallerInAnyOfRole(
							SUPER_ADMIN,
							ADMIN_GEMEINDE,
							SACHBEARBEITER_GEMEINDE,
							ADMIN_TS,
							SACHBEARBEITER_TS,
							ADMIN_BG,
							SACHBEARBEITER_BG,
							ADMIN_INSTITUTION,
							SACHBEARBEITER_INSTITUTION,
							ADMIN_TRAEGERSCHAFT,
							SACHBEARBEITER_TRAEGERSCHAFT)) {
				throwViolation(verfuegung);
			}
		} else {
			// Bei BGs bleiben weiterhin die Admins/Sachbearbeiter BG/Gemeinde berechtigt
			if (!principalBean.isCallerInAnyOfRole(
							SUPER_ADMIN,
							ADMIN_BG,
							SACHBEARBEITER_BG,
							ADMIN_GEMEINDE,
							SACHBEARBEITER_GEMEINDE)) {
				throwViolation(verfuegung);
			}
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable FinanzielleSituationContainer finanzielleSituation) {
		if (finanzielleSituation == null) {
			return;
		}
		UserRole[] allowedRoles =
			{ SUPER_ADMIN,
				ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
				ADMIN_BG, SACHBEARBEITER_BG,
				ADMIN_TS, SACHBEARBEITER_TS };
		Gesuch gesuch = extractGesuch(finanzielleSituation);
		Objects.requireNonNull(gesuch);
		if (!isInRoleOrGSOwner(allowedRoles, () -> gesuch)) {
			throwViolation(finanzielleSituation);
		}
		if (!isWriteAuthorizedGesuchBerechnungsrelevanteDaten(gesuch)) {
			throwViolation(finanzielleSituation);
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable AbstractPlatz platz) {
		if (platz == null) {
			return;
		}
		boolean allowed = isReadAuthorized(platz);
		if (!allowed) {
			throwViolation(platz);
		}
	}

	@Override
	public void checkReadAuthorization(@Nonnull Benutzer benutzer) {
		// Der Mandant muss stimmen
		checkMandantMatches(benutzer);
		// Jeder Benutzer darf sich selber lesen
		if (principalBean.getBenutzer().getUsername().equals(benutzer.getUsername())) {
			return;
		}
		// Gesuchsteller duerfen nur sich selber lesen,
		// Admins Instituion/Traegerschaft duerfen nur andere Benutzer mit Institution/Traegschaft Rolle lesen
		// Gemeinde-Admins duerfen nur andere Gemeinde-Benutzer lesen
		// Mandant-Admins duerfen nur andere Mandant-Benutzer lesen
		switch (principalBean.getBenutzer().getRole()) {
		case SUPER_ADMIN:
		case REVISOR: {
			// Alles erlaubt
			return;
		}
		case ADMIN_GEMEINDE:
		case ADMIN_BG:
		case ADMIN_TS: {
			if (benutzer.getRole().getRollenAbhaengigkeit() != RollenAbhaengigkeit.GEMEINDE
			&& benutzer.getRole().getRollenAbhaengigkeit() != RollenAbhaengigkeit.INSTITUTION) {
				throwViolation(benutzer);
			}
			return;
		}
		case ADMIN_TRAEGERSCHAFT:
		case ADMIN_INSTITUTION: {
			if (benutzer.getRole().getRollenAbhaengigkeit() != RollenAbhaengigkeit.TRAEGERSCHAFT
				&& benutzer.getRole().getRollenAbhaengigkeit() != RollenAbhaengigkeit.INSTITUTION) {
				throwViolation(benutzer);
			}
			return;
		}
		case ADMIN_MANDANT: {
			if (benutzer.getRole().getRollenAbhaengigkeit() != RollenAbhaengigkeit.KANTON) {
				throwViolation(benutzer);
			}
			return;
		}
		case GESUCHSTELLER: {
			if (!hasPrincipalName(benutzer)) {
				throwViolation(benutzer);
			}
			return;
		}
		default:
			// Alle anderen sind nicht erlaubt
			throwViolation(benutzer);
		}
	}

	@Override
	public void checkWriteAuthorization(@Nonnull Benutzer benutzer) {
		if (!isWriteAuthorized(benutzer)) {
			throwViolation(benutzer);
		}
	}

	private boolean isWriteAuthorized(@Nonnull Benutzer benutzer) {
		if (ANONYMOUS_USER_USERNAME.equals(principalBean.getPrincipal().getName())
			|| LOGINCONNECTOR_USER_USERNAME.equals(principalBean.getPrincipal().getName())) {
			// when a user logs in, it is created by anonymous. So we must allow that
			return true;
		}
		if (benutzer.equals(principalBean.getBenutzer())) {
			return true;
		}
		if (principalBean.isCallerInRole(SUPER_ADMIN)) {
			return true;
		}
		if (!principalBean.isCallerInAnyOfRole(getAllAdminRoles())
			&& !principalBean.isCallerInRole(SACHBEARBEITER_MANDANT)) {
			return false;
		}
		if (principalBean.isCallerInAnyOfRole(ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE)) {
			Set<Gemeinde> gemeindenOfUser = principalBean.getBenutzer().getCurrentBerechtigung().getGemeindeList();
			return (userHasSameGemeindeAsPrincipal(benutzer))
				|| (benutzer.getInstitution() != null
					&& (tagesschuleBelongsToGemeinde(benutzer.getInstitution().getId(), gemeindenOfUser)
						|| (ferieninselBelongsToGemeinde(benutzer.getInstitution().getId(), gemeindenOfUser))));
		}
		if (principalBean.isCallerInAnyOfRole(ADMIN_MANDANT, SACHBEARBEITER_MANDANT)) {
			return benutzer.getRole().isRoleMandant()
				|| benutzer.getRole().isRoleAnyAdminGemeinde()
				|| benutzer.getRole().isRoleGemeinde()
				|| benutzer.getRole().isRoleAdminTraegerschaftInstitution();
		}
		if (principalBean.isCallerInRole(ADMIN_TRAEGERSCHAFT)) {
			return userBelongsToTraegerschaftOfPrincipal(benutzer);
		}
		if (principalBean.isCallerInRole(ADMIN_INSTITUTION) && benutzer.getInstitution() != null) {
			return userBelongsToInstitutionOfPrincipal(benutzer);
		}

		return false;
	}

	private boolean tagesschuleBelongsToGemeinde(@Nonnull String institutionId, @Nonnull Collection<Gemeinde> userGemeinden) {
		InstitutionStammdaten stammdaten = stammdatenService.fetchInstitutionStammdatenByInstitution(institutionId, false);
		if (stammdaten == null || stammdaten.getInstitutionStammdatenTagesschule() == null) {
			return false;
		}
		return userGemeinden.contains(stammdaten.getInstitutionStammdatenTagesschule().getGemeinde());
	}

	private boolean ferieninselBelongsToGemeinde(@Nonnull String institutionId, @Nonnull Collection<Gemeinde> userGemeinden) {
		InstitutionStammdaten stammdaten = stammdatenService.fetchInstitutionStammdatenByInstitution(institutionId, false);
		if (stammdaten == null || stammdaten.getInstitutionStammdatenFerieninsel() == null) {
			return false;
		}
		return userGemeinden.contains(stammdaten.getInstitutionStammdatenFerieninsel().getGemeinde());
	}

	private boolean userBelongsToInstitutionOfPrincipal(@Nonnull Benutzer benutzer) {
		return benutzer.getRole().getRollenAbhaengigkeit() == RollenAbhaengigkeit.INSTITUTION
			&& Objects.requireNonNull(principalBean.getBenutzer().getInstitution()).equals(benutzer.getInstitution());
	}

	private boolean userBelongsToTraegerschaftOfPrincipal(@Nonnull Benutzer benutzer) {
		RollenAbhaengigkeit abhaengigkeit = benutzer.getRole().getRollenAbhaengigkeit();

		if (abhaengigkeit != RollenAbhaengigkeit.TRAEGERSCHAFT && abhaengigkeit != RollenAbhaengigkeit.INSTITUTION) {
			return false;
		}

		Traegerschaft traegerschaft = Objects.requireNonNull(principalBean.getBenutzer().getTraegerschaft());

		return traegerschaft.equals(benutzer.getTraegerschaft())
			|| institutionService.getAllInstitutionenFromTraegerschaft(traegerschaft.getId())
			.contains(benutzer.getInstitution());
	}

	private boolean userHasSameGemeindeAsPrincipal(@Nonnull Benutzer benutzer) {
		return principalBean.getBenutzer().getCurrentBerechtigung().getGemeindeList().stream()
			.anyMatch(gemeinde -> benutzer.getCurrentBerechtigung().getGemeindeList().contains(gemeinde));
	}

	@Override
	public <T extends AbstractPlatz> void checkReadAuthorizationForAllPlaetze(@Nullable Collection<T>betreuungen) {
		if (betreuungen != null) {
			betreuungen.stream()
				.filter(betreuung -> !isReadAuthorized(betreuung))
				.findAny()
				.ifPresent(this::throwViolation);
		}
	}

	@Override
	public <T extends AbstractPlatz> void checkReadAuthorizationForAnyPlaetze(@Nullable Collection<T> plaetze) {
		if (plaetze != null && !plaetze.isEmpty()
			&& plaetze.stream().noneMatch(this::isReadAuthorized)) {
			throw new EJBAccessException(
				"Access Violation user is not allowed for any of these plaetze");
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable Verfuegung verfuegung) {
		if (verfuegung != null) {
			//an betreuung delegieren
			checkReadAuthorization(verfuegung.getBetreuung());
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable WizardStep step) {
		if (step != null) {
			checkReadAuthorizedGesuchTechnicalData(step.getGesuch());
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable WizardStep step) {
		if (step != null) {
			checkWriteAuthorizedGesuchTechnicalData(step.getGesuch());
		}
	}

	@Override
	public void checkReadAuthorizationVerfuegungen(@Nullable Collection<Verfuegung> verfuegungen) {
		if (verfuegungen != null) {
			verfuegungen.forEach(this::checkReadAuthorization);
		}
	}

	@Override
	public void checkWriteAuthorization(AbstractPlatz abstractPlatz) {
		if (abstractPlatz == null) {
			return;
		}
		boolean allowed = isWriteAuthorized(abstractPlatz);
		if (!allowed) {
			throwViolation(abstractPlatz);
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable ErwerbspensumContainer ewpCnt) {
		if (ewpCnt != null) {
			UserRole[] allowedRoles =
				{ SUPER_ADMIN,
					ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
					ADMIN_BG, SACHBEARBEITER_BG,
					ADMIN_TS, SACHBEARBEITER_TS };
			final Gesuch gesuch = extractGesuch(ewpCnt);
			Objects.requireNonNull(gesuch);
			if (!isInRoleOrGSOwner(allowedRoles, () -> gesuch)) {
				throwViolation(ewpCnt);
			}
			if (!isWriteAuthorizedGesuchBerechnungsrelevanteDaten(gesuch)) {
				throwViolation(ewpCnt);
			}
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable ErwerbspensumContainer ewpCnt) {
		if (ewpCnt != null) {
			//Wenn wir hier 100% korrekt sein wollen muessten wir auch noch das Gesuch laden und den status pruefen.
			UserRole[] allowedRoles =
				{ SACHBEARBEITER_BG, SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, REVISOR, JURIST,
					ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT };
			if (!isInRoleOrGSOwner(allowedRoles, () -> extractGesuch(ewpCnt))) {
				throwViolation(ewpCnt);
			}
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable FinanzielleSituationContainer finanzielleSituation) {
		if (finanzielleSituation != null) {
			// hier fuer alle lesbar ausser fuer institution/traegerschaft
			Gesuch owningGesuch = extractGesuch(finanzielleSituation);
			if (owningGesuch == null) {
				//wenn wir keinen fall finden dann gehen wir davon aus, dass die finanzielle Situation bzw ihr
				// Gesuchsteller noch nicht gespeichert ist
				return;
			}
			validateGemeindeMatches(owningGesuch.getDossier());
			boolean allowedAdminOrSachbearbeiter = isAllowedAdminOrSachbearbeiter(owningGesuch);
			boolean allowedSchulamt = isAllowedSchulamt(owningGesuch);

			boolean allowedOthers = false;
			if (principalBean.isCallerInAnyOfRole(OTHER_AMT_ROLES)
				&& owningGesuch.getStatus().isReadableByJugendamtSchulamtSteueramt()) {
				allowedOthers = true;
			}
			boolean allowedOwner = isGSOwner(owningGesuch::getFall);

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
			validateGemeindeMatches(gesuch.getDossier());
			if (principalBean.isCallerInAnyOfRole(SACHBEARBEITER_TS, ADMIN_TS)) {
				//schulamt darf nur solche lesen die nur_schulamt sind
				return gesuch.hasBetreuungOfSchulamt();
			}
			if (principalBean.isCallerInAnyOfRole(SACHBEARBEITER_BG, ADMIN_BG)) {
				// BG-Benutzer duerfen keine lesen die exklusiv schulamt sind
				return !gesuch.hasOnlyBetreuungenOfSchulamt();
			}
			return principalBean.isCallerInAnyOfRole(SACHBEARBEITER_GEMEINDE, ADMIN_GEMEINDE);
		}
		return false;
	}

	private boolean isInRoleOrGSOwner(UserRole[] allowedRoles, Supplier<Gesuch> gesuchSupplier) {
		if (principalBean.isCallerInAnyOfRole(allowedRoles)) {
			return true;
		}

		return isGSOwner(() -> gesuchSupplier.get().getFall());
	}

	private boolean isGSOwner(Supplier<Fall> fallSupplier) {
		if (!principalBean.isCallerInRole(GESUCHSTELLER.name())) {
			return false;
		}

		Fall fall = fallSupplier.get();

		return (fall != null) && (
			fall.getUserErstellt() == null || (fall.getBesitzer() != null && hasPrincipalName(fall.getBesitzer()))
		);
	}

	private boolean isReadAuthorized(final AbstractPlatz abstractPlatz) {
		final Gesuch gesuch = abstractPlatz.extractGesuch();
		if (isAllowedAdminOrSachbearbeiter(gesuch)) {
			return true;
		}

		boolean isOwnerOrAdmin = isGSOwner(gesuch::getFall);
		if (isOwnerOrAdmin) {
			return true;
		}

		if (principalBean.isCallerInAnyOfRole(ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION)) {
			Institution institution = principalBean.getBenutzer().getInstitution();
			Objects.requireNonNull(
				institution,
				"Institution des Sachbearbeiters muss gesetzt sein " + principalBean.getBenutzer());
			return abstractPlatz.getInstitutionStammdaten().getInstitution().equals(institution);
		}
		if (principalBean.isCallerInAnyOfRole(ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT)) {
			return isTraegerschaftsBenutzerAuthorizedForInstitution(principalBean.getBenutzer(), abstractPlatz.getInstitutionStammdaten().getInstitution());
		}
		if (principalBean.isCallerInAnyOfRole(SACHBEARBEITER_TS, ADMIN_TS)) {
			return isUserAllowedForGemeinde(gesuch.getDossier().getGemeinde())
				&& abstractPlatz.getBetreuungsangebotTyp().isSchulamt();
		}
		return false;
	}

	private boolean isWriteAuthorized(final AbstractPlatz abstractPlatz) {
		// Nach aktuellen Kenntnissen gleich wie lesen
		return isReadAuthorized(abstractPlatz);
	}

	@Override
	public void checkReadAuthorizationFinSit(@Nullable Gesuch gesuch) {
		if (gesuch != null) {
			FinanzielleSituationContainer finSitGs1 = gesuch.getGesuchsteller1() != null ?
				gesuch.getGesuchsteller1().getFinanzielleSituationContainer() :
				null;
			FinanzielleSituationContainer finSitGs2 = gesuch.getGesuchsteller2() != null ?
				gesuch.getGesuchsteller2().getFinanzielleSituationContainer() :
				null;
			checkReadAuthorization(finSitGs1);
			checkReadAuthorization(finSitGs2);
		}
	}

	@Override
	public boolean isReadAuthorized(Gesuch gesuch) {
		if (isAllowedAdminOrSachbearbeiter(gesuch)) {
			return true;
		}
		if (isGSOwner(gesuch::getFall)) {
			return true;
		}
		if (principalBean.isCallerInAnyOfRole(ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION)) {
			Institution institution = principalBean.getBenutzer().getInstitution();
			Objects.requireNonNull(
				institution,
				"Institution des Sachbearbeiters muss gesetzt sein " + principalBean.getBenutzer());
			return gesuch.hasBetreuungOfInstitution(institution); //@reviewer: oder besser ueber service ?
		}
		if (principalBean.isCallerInAnyOfRole(ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT)) {
			Traegerschaft traegerschaft = principalBean.getBenutzer().getTraegerschaft();
			Objects.requireNonNull(
				traegerschaft,
				"Traegerschaft des des Sachbearbeiters muss gesetzt sein " + principalBean.getBenutzer());
			Collection<Institution> institutions =
				institutionService.getAllInstitutionenFromTraegerschaft(traegerschaft.getId());
			return institutions.stream()
				.anyMatch(gesuch::hasBetreuungOfInstitution);  // irgend eine der betreuungen des gesuchs matched
		}
		if (isAllowedSchulamt(gesuch)) {
			return true;
		}
		if (isAllowedSteueramt(gesuch)) {
			return true;
		}
		return isAllowedJuristOrRevisor(gesuch);
	}

	private boolean isAllowedAdminOrSachbearbeiter(Gesuch gesuch) {
		if (principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			return true;
		}
		//JA/SCH Benutzer duerfen nur freigegebene Gesuche anschauen
		if (principalBean.isCallerInAnyOfRole(JA_OR_ADM_OR_SCH)) {
			return gesuch.getStatus().isReadableByJugendamtSchulamtSteueramt()
				&& isUserAllowedForGemeinde(gesuch.getDossier().getGemeinde());
		}
		return isAllowedJuristOrRevisor(gesuch);
	}

	private boolean isUserAllowedForGemeinde(Gemeinde gemeinde) {
		if (gemeinde == null) { // all users may see a null gemeinde
			return true;
		}
		return principalBean.belongsToGemeinde(gemeinde);
	}

	private boolean isAllowedSchulamt(Gesuch gesuch) {
		if (principalBean.isCallerInAnyOfRole(SACHBEARBEITER_TS, ADMIN_TS)) {
			return gesuch.getStatus().isReadableBySchulamtSachbearbeiter()
				&& isUserAllowedForGemeinde(gesuch.getDossier().getGemeinde());
		}
		return false;
	}

	private boolean isAllowedSteueramt(Gesuch gesuch) {
		if (principalBean.isCallerInRole(STEUERAMT)) {
			return gesuch.getStatus().isReadableBySteueramt()
				&& isUserAllowedForGemeinde(gesuch.getDossier().getGemeinde());
		}
		return false;
	}

	private boolean isAllowedJuristOrRevisor(Gesuch gesuch) {
		if (principalBean.isCallerInRole(JURIST)) {
			return gesuch.getStatus().isReadableByJurist()
				&& isUserAllowedForGemeinde(gesuch.getDossier().getGemeinde());
		}
		if (principalBean.isCallerInRole(REVISOR)) {
			return gesuch.getStatus().isReadableByRevisor()
				&& isUserAllowedForGemeinde(gesuch.getDossier().getGemeinde());
		}
		if (principalBean.isCallerInAnyOfRole(ADMIN_MANDANT, SACHBEARBEITER_MANDANT)) {
			return gesuch.getStatus().isReadableByMandantUser();
		}
		return false;
	}

	//this method is named slightly wrong because it only checks write authorization for Admins SachbearbeiterJA and GS
	private boolean isWriteAuthorized(Supplier<Gesuch> gesuchSupplier) {
		if (principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			return true;
		}
		Gesuch gesuch = gesuchSupplier.get();

		validateGemeindeMatches(gesuch.getDossier());

		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		if (userRole == null) {
			return false;
		}
		if (!AntragStatus.writeAllowedForRole(userRole).contains(gesuch.getStatus())) {
			String msg = "Cannot update Gesuch "
				+ gesuch.getId()
				+ " in Status "
				+ gesuch.getStatus()
				+ " in UserRole "
				+ userRole;
			throw new EbeguRuntimeException(
				"isWriteAuthorized",
				ErrorCodeEnum.ERROR_INVALID_EBEGUSTATE,
				gesuch.getId(),
				msg);
		}
		if (principalBean.isCallerInAnyOfRole(JA_OR_ADM_OR_SCH)) {
			return gesuch.getStatus().isReadableByJugendamtSchulamtSteueramt()
				|| AntragStatus.FREIGABEQUITTUNG == gesuch.getStatus();
		}

		return isGSOwner(gesuch::getFall);
	}

	private boolean isWriteAuthorized(@Nullable Gesuch entity) {
		return isWriteAuthorized(() -> entity);
	}

	private void throwViolation(AbstractEntity abstractEntity) {
		throw new EJBAccessException(
			"Access Violation"
				+ " for Entity: " + abstractEntity.getClass().getSimpleName() + "(id=" + abstractEntity.getId() + "):"
				+ " for current user: " + principalBean.getPrincipal()
				+ " in role(s): " + principalBean.discoverRoles()
				+ ", insertUser: " + abstractEntity.getUserErstellt()
				+ '.' + abstractEntity.getMessageForAccessException()
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

	private Gesuch extractGesuch(AbstractPlatz platz) {
		return platz.extractGesuch();
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
			validateGemeindeMatches(mitteilung.getDossier());
			Objects.requireNonNull(userRole);
			switch (userRole) {
			case GESUCHSTELLER: {
				// Beim schreiben (Entwurf speichern oder Mitteilung senden) muss der eingeloggte GS der Absender sein
				if (!isCurrentUserMitteilungsSender(mitteilung)) {
					throwViolation(mitteilung);
				}
				break;
			}
			case ADMIN_INSTITUTION:
			case SACHBEARBEITER_INSTITUTION:
			case ADMIN_TRAEGERSCHAFT:
			case SACHBEARBEITER_TRAEGERSCHAFT:
				if (isNotSenderTyp(mitteilung, MitteilungTeilnehmerTyp.INSTITUTION)) {
					throwViolation(mitteilung);
				}
				break;
			case SACHBEARBEITER_BG:
			case ADMIN_BG:
			case SACHBEARBEITER_GEMEINDE:
			case ADMIN_GEMEINDE:
			case SACHBEARBEITER_TS:
			case ADMIN_TS:
				if (isNotSenderTyp(mitteilung, MitteilungTeilnehmerTyp.JUGENDAMT)) {
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

	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	@Override
	public void checkReadAuthorizationMitteilung(@Nullable Mitteilung mitteilung) {
		if (mitteilung != null) {
			UserRole userRole = principalBean.discoverMostPrivilegedRole();
			Objects.requireNonNull(userRole);
			validateGemeindeMatches(mitteilung.getDossier());
			// Beim Lesen einer Mitteilung muss der eingeloggte Benutzer
			// - der Sender oder der Empfaenger sein (GESUCHSTELLER)
			// - der Sender sein (INSTITUTIONEN)
			// - SenderTyp oder EmpfaengerTyp muss JUGENDAMT sein (SACHBEARBEITER_BG)
			switch (userRole) {
			case GESUCHSTELLER: {
				if (!(isCurrentUserMitteilungsSender(mitteilung) || isCurrentUserMitteilungsEmpfaenger(mitteilung))) {
					throwViolation(mitteilung);
				}
				break;
			}
			case ADMIN_INSTITUTION:
			case SACHBEARBEITER_INSTITUTION:
			case ADMIN_TRAEGERSCHAFT:
			case SACHBEARBEITER_TRAEGERSCHAFT: {
				if (isNotSenderTypOrEmpfaengerTyp(mitteilung, MitteilungTeilnehmerTyp.INSTITUTION)) {
					throwViolation(mitteilung);
				}
				break;
			}
			case SACHBEARBEITER_BG:
			case SACHBEARBEITER_GEMEINDE:
			case ADMIN_TS:
			case SACHBEARBEITER_TS:
			case REVISOR:
			case ADMIN_MANDANT:
			case SACHBEARBEITER_MANDANT: {
				if (isNotSenderTypOrEmpfaengerTyp(mitteilung, MitteilungTeilnehmerTyp.JUGENDAMT)) {
					throwViolation(mitteilung);
				}
				break;
			}
			case SUPER_ADMIN:
			case ADMIN_BG:
			case ADMIN_GEMEINDE: {
				break;
			}
			default: {
				throwViolation(mitteilung);
			}
			}
		}
	}

	@Override
	public void checkReadAuthorizationZahlung(@Nullable Zahlung zahlung) {
		if (zahlung != null && zahlung.getZahlungsauftrag() != null) {
			checkReadAuthorizationZahlungsauftrag(zahlung.getZahlungsauftrag());
		}
	}

	@Override
	public void checkReadAuthorizationZahlungsauftrag(@Nullable Zahlungsauftrag zahlungsauftrag) {
		if (zahlungsauftrag != null) {
			checkReadAuthorization(zahlungsauftrag.getGemeinde());
		}
	}

	@Override
	public void checkWriteAuthorizationZahlungsauftrag(@Nullable Zahlungsauftrag zahlungsauftrag) {
		if (zahlungsauftrag != null) {
			checkWriteAuthorization(zahlungsauftrag.getGemeinde());
		}
	}

	@SuppressWarnings("unused")
	private boolean isReadAuthorization(@Nullable Traegerschaft traegerschaft) {
		// Aktuell sind keine Einschraenkungen zum Lesen von Traegerschaften bekannt.
		return true;
	}

	private boolean isWriteAuthorization(@Nullable Traegerschaft traegerschaft) {
		if (traegerschaft == null) {
			return true;
		}
		if (principalBean.isCallerInAnyOfRole(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT)) {
			// Problem hier: Traegerschaft gehoert aktuell nicht zu einem Mandanten!
			return true;
		}
		if (principalBean.isCallerInAnyOfRole(ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT)) {
			return traegerschaft.equals(principalBean.getBenutzer().getCurrentBerechtigung().getTraegerschaft());
		}
		return false;
	}

	private boolean isReadAuthorizationInstitution(@Nullable Institution institution) {
		if (institution == null) {
			return true;
		}
		InstitutionStammdaten institutionStammdaten = criteriaQueryHelper.getEntityByUniqueAttribute(
			InstitutionStammdaten.class,
			institution,
			InstitutionStammdaten_.institution
		).orElse(null);
		if (institutionStammdaten == null) {
			return true;
		}
		return isReadAuthorizationInstitutionStammdaten(institutionStammdaten);
	}

	private boolean isWriteAuthorizationInstitution(@Nullable Institution institution) {
		if (institution == null) {
			return true;
		}
		InstitutionStammdaten institutionStammdaten = criteriaQueryHelper.getEntityByUniqueAttribute(
			InstitutionStammdaten.class,
			institution,
			InstitutionStammdaten_.institution
		).orElse(null);
		if (institutionStammdaten == null) {
			return true;
		}
		return isWriteAuthorizationInstitutionStammdaten(institutionStammdaten);
	}

	@Override
	public boolean isReadAuthorizationInstitutionStammdaten(@Nullable InstitutionStammdaten institutionStammdaten) {
		if (institutionStammdaten == null) {
			return true;
		}
		if (!isMandantMatching(institutionStammdaten.getInstitution())) {
			return false;
		}

		// Lesen duerfen alle Rollen ausser:
		// - den Institution/Trägerschafts-Rollen: diese duerfen nur ihre eigenen Institutionen lesen
		// - den Gemeindebenutzer: diese duerfen bei Gemeindeabhaengigen Institionen (i.e. Tagesschulen und
		// 		Ferieninseln) nur diejenigen ihrer Gemeinde sehen
		Benutzer currentBenutzer = principalBean.getBenutzer();
		switch (currentBenutzer.getRole()) {
		case GESUCHSTELLER:
		case ADMIN_MANDANT:
		case SACHBEARBEITER_MANDANT:
		case SUPER_ADMIN: {
			return true;
		}
		case ADMIN_GEMEINDE:
		case SACHBEARBEITER_GEMEINDE:
		case ADMIN_BG:
		case SACHBEARBEITER_BG:
		case ADMIN_TS:
		case SACHBEARBEITER_TS:
		case REVISOR:
		case STEUERAMT:
		case JURIST: {
			if (institutionStammdaten.getBetreuungsangebotTyp().isSchulamt()) {
				Gemeinde gemeinde = null;
				if (institutionStammdaten.getInstitutionStammdatenTagesschule() != null) {
					gemeinde = institutionStammdaten.getInstitutionStammdatenTagesschule().getGemeinde();
				}
				if (institutionStammdaten.getInstitutionStammdatenFerieninsel() != null) {
					gemeinde = institutionStammdaten.getInstitutionStammdatenFerieninsel().getGemeinde();
				}
				// Es handelt sich um ein Schulamt-Angebot: Die Gemeinde muss stimmen, falls vorhanden
				if (gemeinde != null) {
					return isUserAllowedForGemeinde(gemeinde);
				}
			}
			return true;
		}
		case ADMIN_INSTITUTION:
		case SACHBEARBEITER_INSTITUTION: {
			return institutionStammdaten.getInstitution().equals(currentBenutzer.getInstitution());
		}
		case ADMIN_TRAEGERSCHAFT:
		case SACHBEARBEITER_TRAEGERSCHAFT: {
			return isTraegerschaftsBenutzerAuthorizedForInstitution(currentBenutzer, institutionStammdaten.getInstitution());
		}
		default: {
			return false;
		}
		}
	}

	@Override
	public boolean isWriteAuthorizationInstitutionStammdaten(@Nullable InstitutionStammdaten institutionStammdaten) {
		if (institutionStammdaten == null) {
			return true;
		}
		if (!isMandantMatching(institutionStammdaten.getInstitution())) {
			return false;
		}

		Benutzer currentBenutzer = principalBean.getBenutzer();

		switch (currentBenutzer.getRole()) {
		case GESUCHSTELLER: {
			// Gesuchsteller ist NIE berechtigt
			return false;
		}
		case ADMIN_INSTITUTION:
		case SACHBEARBEITER_INSTITUTION: {
			return institutionStammdaten.getInstitution().equals(currentBenutzer.getInstitution());
		}
		case ADMIN_TRAEGERSCHAFT:
		case SACHBEARBEITER_TRAEGERSCHAFT: {
			return isTraegerschaftsBenutzerAuthorizedForInstitution(currentBenutzer, institutionStammdaten.getInstitution());
		}
		case ADMIN_GEMEINDE:
		case SACHBEARBEITER_GEMEINDE:
		case ADMIN_BG:
		case SACHBEARBEITER_BG:
		case ADMIN_TS:
		case SACHBEARBEITER_TS: {
			if (institutionStammdaten.getBetreuungsangebotTyp().isKita() || institutionStammdaten.getBetreuungsangebotTyp().isTagesfamilien()) {
				// Kitas und Tageseltern koennen ohne Einschraenkungen gelesen aber nicht editiert werden durch Gemeinde-Benutzer,
				return false;
			}
			Gemeinde gemeinde = null;
			if (institutionStammdaten.getInstitutionStammdatenTagesschule() != null) {
				gemeinde = institutionStammdaten.getInstitutionStammdatenTagesschule().getGemeinde();
			}
			if (institutionStammdaten.getInstitutionStammdatenFerieninsel() != null) {
				gemeinde = institutionStammdaten.getInstitutionStammdatenFerieninsel().getGemeinde();
			}
			// Es handelt sich um ein Schulamt-Angebot: Die Gemeinde vorhanden sein und stimmen
			Objects.requireNonNull(gemeinde, "Gemeinde muss gesetzt sein");
			return isUserAllowedForGemeinde(gemeinde);
		}
		case REVISOR:
		case STEUERAMT:
		case JURIST: {
			return false;
		}
		case ADMIN_MANDANT:
		case SACHBEARBEITER_MANDANT: {
			if (institutionStammdaten.isNew() && !institutionStammdaten.getInstitution().getStatus().isEnabled()) {
				// Es handelt sich um eine Einladung, dies muss moeglich sein
				return true;
			}
			return false;
		}
		case SUPER_ADMIN: {
			// Superadmin darf alles
			return true;
		}
		default: {
			return false;
		}
		}
	}

	@Override
	public void checkReadAuthorizationInstitution(@Nullable Institution institution) {
		if (institution == null) {
			return;
		}
		checkMandantMatches(institution);
		if (!isReadAuthorizationInstitution(institution)) {
			throwViolation(institution);
		}
	}

	@Override
	public void checkWriteAuthorizationInstitution(@Nullable Institution institution) {
		if (institution == null) {
			return;
		}
		checkMandantMatches(institution);
		if (!isWriteAuthorizationInstitution(institution)) {
			throwViolation(institution);
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable Traegerschaft traegerschaft) {
		if (traegerschaft == null) {
			return;
		}
		if (!isReadAuthorization(traegerschaft)) {
			throwViolation(traegerschaft);
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable Traegerschaft traegerschaft) {
		if (traegerschaft == null) {
			return;
		}
		if (!isWriteAuthorization(traegerschaft)) {
			throwViolation(traegerschaft);
		}
	}

	@Override
	public void checkReadAuthorizationInstitutionStammdaten(@Nullable InstitutionStammdaten institutionStammdaten) {
		if (institutionStammdaten == null) {
			return;
		}
		checkMandantMatches(institutionStammdaten.getInstitution());
		if (!isReadAuthorizationInstitutionStammdaten(institutionStammdaten)) {
			throwViolation(institutionStammdaten);
		}
	}

	@Override
	public void checkWriteAuthorizationInstitutionStammdaten(@Nullable InstitutionStammdaten institutionStammdaten) {
		if (institutionStammdaten == null) {
			return;
		}
		checkMandantMatches(institutionStammdaten.getInstitution());
		if (!isWriteAuthorizationInstitutionStammdaten(institutionStammdaten)) {
			throwViolation(institutionStammdaten);
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable RueckforderungFormular rueckforderungFormular) {
		if (rueckforderungFormular == null) {
			return;
		}
		if (principalBean.isCallerInRole(SUPER_ADMIN)) {
			return;
		}
		switch (rueckforderungFormular.getStatus()) {
		case EINGELADEN:
		case IN_BEARBEITUNG_INSTITUTION_STUFE_1:
		case IN_BEARBEITUNG_INSTITUTION_STUFE_2: {
			// Der Kanton muss auch in den "Institution-" Status bearbeiten koennen wegen der Fristverlaengerung
			if (!principalBean.isCallerInAnyOfRole(UserRole.getAllRolesForCoronaRueckforderung())) {
				throwViolation(rueckforderungFormular);
			}
			break;
		}
		case NEU:
		case IN_PRUEFUNG_KANTON_STUFE_1:
		case IN_PRUEFUNG_KANTON_STUFE_2:
		case GEPRUEFT_STUFE_1:
		case VERFUEGT_PROVISORISCH:
		case BEREIT_ZUM_VERFUEGEN:
		case VERFUEGT:
		case ABGESCHLOSSEN_OHNE_GESUCH: {
			if (!principalBean.isCallerInAnyOfRole(UserRole.getMandantRoles())) {
				throwViolation(rueckforderungFormular);
			}
			break;
		}
		default:
			break;
		}
		if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
			checkWriteAuthorizationInstitutionStammdaten(rueckforderungFormular.getInstitutionStammdaten());
		}
	}

	@Override
	public void checkWriteAuthorizationDocument(@Nullable RueckforderungFormular rueckforderungFormular) {
		if (rueckforderungFormular == null) {
			return;
		}
		if (principalBean.isCallerInRole(SUPER_ADMIN)) {
			return;
		}
		switch (rueckforderungFormular.getStatus()) {
		case EINGELADEN:
		case IN_BEARBEITUNG_INSTITUTION_STUFE_1:
		case IN_BEARBEITUNG_INSTITUTION_STUFE_2: {
			// Der Kanton muss auch in den "Institution-" Status bearbeiten koennen wegen der Fristverlaengerung
			if (!principalBean.isCallerInAnyOfRole(UserRole.getAllRolesForCoronaRueckforderung())) {
				throwViolation(rueckforderungFormular);
			}
			break;
		}
		case NEU:
		case IN_PRUEFUNG_KANTON_STUFE_1:
		case GEPRUEFT_STUFE_1:
		case VERFUEGT:
		case ABGESCHLOSSEN_OHNE_GESUCH: {
			if (!principalBean.isCallerInAnyOfRole(UserRole.getMandantRoles())) {
				throwViolation(rueckforderungFormular);
			}
			break;
		}
		default:
			break;
		}
		if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
			checkWriteAuthorizationInstitutionStammdaten(rueckforderungFormular.getInstitutionStammdaten());
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable RueckforderungFormular rueckforderungFormular) {
		if (rueckforderungFormular == null) {
			return;
		}
		if (principalBean.isCallerInRole(SUPER_ADMIN)) {
			return;
		}
		final List<UserRole> allowedRoles = new ArrayList<>();
		allowedRoles.addAll(UserRole.getMandantRoles());
		allowedRoles.addAll(UserRole.getInstitutionTraegerschaftRoles());
		if (!principalBean.isCallerInAnyOfRole(allowedRoles)) {
			throwViolation(rueckforderungFormular);
		}
		if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
			checkWriteAuthorizationInstitutionStammdaten(rueckforderungFormular.getInstitutionStammdaten());
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable AntragStatusHistory antragStatusHistory) {
		if (antragStatusHistory != null) {
			checkReadAuthorizedGesuchTechnicalData(antragStatusHistory.getGesuch());
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable AntragStatusHistory antragStatusHistory) {
		if (antragStatusHistory != null) {
			checkWriteAuthorizedGesuchTechnicalData(antragStatusHistory.getGesuch());
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable DokumentGrund dokumentGrund) {
		if (dokumentGrund != null) {
			checkReadAuthorizedGesuchTechnicalData(dokumentGrund.getGesuch());
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable DokumentGrund dokumentGrund) {
		if (dokumentGrund != null) {
			checkWriteAuthorizedGesuchTechnicalData(dokumentGrund.getGesuch());
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable GeneratedDokument generatedDokument) {
		if (generatedDokument != null) {
			checkReadAuthorizedGesuchTechnicalData(generatedDokument.getGesuch());
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable GeneratedDokument generatedDokument) {
		if (generatedDokument != null) {
			checkWriteAuthorizedGesuchTechnicalData(generatedDokument.getGesuch());
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable Mahnung mahnung) {
		if (mahnung != null) {
			checkReadAuthorizedGesuchTechnicalData(mahnung.getGesuch());
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable Mahnung mahnung) {
		if (mahnung != null) {
			checkWriteAuthorizedGesuchTechnicalData(mahnung.getGesuch());
		}
	}

	@Override
	public void checkReadAuthorization(@Nullable LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer) {
		if (latsGemeindeContainer != null) {
			checkMandantMatches(latsGemeindeContainer.getGemeinde());
			if (principalBean.isCallerInAnyOfRole(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT)) {
				return;
			}
			final boolean gehoertZuGemeinde = principalBean.getBenutzer().getCurrentBerechtigung().getGemeindeList()
				.stream()
				.anyMatch(latsGemeindeContainer.getGemeinde()::equals);
			if (gehoertZuGemeinde) {
				return;
			}
			// Alle anderen sind Stand heute nicht berechtigt
			throwViolation(latsGemeindeContainer);
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer) {
		// Gleiche Berechtigung wie Lesen? Spaeter noch den Status beruecksichtigen!
		checkReadAuthorization(latsGemeindeContainer);
	}

	@Override
	public void checkReadAuthorization(@Nullable LastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainer) {
		if (latsInstitutionContainer != null) {
			checkMandantMatches(latsInstitutionContainer.getGemeinde());
			if (principalBean.isCallerInAnyOfRole(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT)) {
				return;
			} else if (principalBean.getBenutzer().getRole().isRoleGemeindeabhaengig()) {
				final boolean gehoertZuGemeinde = principalBean.getBenutzer().getCurrentBerechtigung().getGemeindeList()
					.stream()
					.anyMatch(latsInstitutionContainer.getGemeinde()::equals);
				if (gehoertZuGemeinde) {
					return;
				}
			} else if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				checkWriteAuthorizationInstitution(latsInstitutionContainer.getInstitution());
			}
			// Alle anderen sind Stand heute nicht berechtigt
			throwViolation(latsInstitutionContainer);
		}
	}

	@Override
	public void checkWriteAuthorization(@Nullable LastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainer) {
		// Gleiche Berechtigung wie Lesen? Spaeter noch den Status beruecksichtigen!
		checkReadAuthorization(latsInstitutionContainer);
	}

	private boolean isNotSenderTypOrEmpfaengerTyp(@Nullable Mitteilung mitteilung, MitteilungTeilnehmerTyp typ) {
		if (mitteilung == null) {
			return true;
		}

		return mitteilung.getSenderTyp() != typ && mitteilung.getEmpfaengerTyp() != typ;
	}

	private boolean isNotSenderTyp(@Nullable Mitteilung mitteilung, MitteilungTeilnehmerTyp typ) {
		if (mitteilung == null) {
			return true;
		}

		return mitteilung.getSenderTyp() != typ;
	}

	private boolean isCurrentUserMitteilungsSender(@Nonnull Mitteilung mitteilung) {
		return principalBean.getBenutzer().equals(mitteilung.getSender());
	}

	private boolean isCurrentUserMitteilungsEmpfaenger(@Nonnull Mitteilung mitteilung) {
		return principalBean.getBenutzer().equals(mitteilung.getEmpfaenger());
	}

	private boolean hasPrincipalName(@Nonnull Benutzer benutzer) {
		return principalBean.getPrincipal().getName().equalsIgnoreCase(benutzer.getUsername());
	}

	private boolean isTraegerschaftsBenutzerAuthorizedForInstitution(@Nonnull Benutzer currentBenutzer, @Nonnull Institution institution) {
		Traegerschaft traegerschaft = currentBenutzer.getTraegerschaft();
		Objects.requireNonNull(traegerschaft,
			"Traegerschaft des Sachbearbeiters muss gesetzt sein " + currentBenutzer);
		Collection<Institution> institutions =
			institutionService.getAllInstitutionenFromTraegerschaft(traegerschaft.getId());
		return institutions.stream()
			.anyMatch(institutionOfCurrentBenutzer -> institutionOfCurrentBenutzer.equals(institution));
	}

	/**
	 * Prueft, ob die berechnungsrelevanten Daten dieses Gesuchs noch veraendert werden duerfen.
	 * Damit sind insbesondere die vom Gesuchsteller eingegebenen Daten gemeint, also keine
	 * Verfuegungsbemerkungen, Statuswechsel etc. sondern Finanzielle Situation oder Erwerbspensum
	 */
	private boolean isWriteAuthorizedGesuchBerechnungsrelevanteDaten(@Nonnull Gesuch gesuch) {
		// Die generelle (etwas weniger strenge) Ueberpruefung:
		if (!isWriteAuthorized(gesuch)) {
			return false;
		}
		// Explizit fuer die Gesuchsdaten sind wir strenger:
		return !gesuch.getStatus().isAnyStatusOfVerfuegtOrVefuegen();
	}

	/**
	 * Prueft, ob die technischen Daten zu einem Gesuch, die nicht direkt Input-Daten sind,
	 * geschrieben / geloescht werden duerfen. Dies sind z.B. WizardSteps, AntragStatusHistory etc.
	 */
	private void checkWriteAuthorizedGesuchTechnicalData(@Nonnull Gesuch gesuch) {
		// Als grosser Unterschied zu den eigentlichen Gesuchsdaten muss hier auch das
		// Lesen und Schreiben von nicht eingereichten Online-Gesuchen moeglich sein, damit
		// z.B. ein Admin eine Online Mutation eines GS loeschen kann.

		if (principalBean.isCallerInRole(SUPER_ADMIN)) {
			return;
		}
		// Das Gesuch darf aber auf keinen Fall bereits verfuegt sein:
		if (gesuch.getStatus().isAnyStatusOfVerfuegt()) {
			throwViolation(gesuch);
		}
		// Der Benutzer muss zumindest fuer das dazugehoerige Dossier grundsaetzlich zustaendig sein
		checkWriteAuthorizationDossier(gesuch.getDossier());
	}

	/**
	 * Prueft, ob die technischen Daten zu einem Gesuch, die nicht direkt Input-Daten sind,
	 * gelesen werden duerfen. Dies sind z.B. WizardSteps, AntragStatusHistory etc.
	 */
	private void checkReadAuthorizedGesuchTechnicalData(@Nonnull Gesuch gesuch) {
		// Als grosser Unterschied zu den eigentlichen Gesuchsdaten muss hier auch das
		// Lesen und Schreiben von nicht eingereichten Online-Gesuchen moeglich sein, damit
		// z.B. ein Admin eine Online Mutation eines GS loeschen kann.

		if (principalBean.isCallerInRole(SUPER_ADMIN)) {
			return;
		}
		// Der Benutzer muss zumindest fuer das dazugehoerige Dossier grundsaetzlich zustaendig sein
		checkReadAuthorizationDossier(gesuch.getDossier());
	}
}
