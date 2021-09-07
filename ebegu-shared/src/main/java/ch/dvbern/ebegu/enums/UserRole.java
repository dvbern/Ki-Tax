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

package ch.dvbern.ebegu.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public enum UserRole {

	SUPER_ADMIN(RollenAbhaengigkeit.NONE),
	ADMIN_BG(RollenAbhaengigkeit.GEMEINDE),
	SACHBEARBEITER_BG(RollenAbhaengigkeit.GEMEINDE),
	SACHBEARBEITER_TRAEGERSCHAFT(RollenAbhaengigkeit.TRAEGERSCHAFT),
	ADMIN_TRAEGERSCHAFT(RollenAbhaengigkeit.TRAEGERSCHAFT),
	ADMIN_INSTITUTION(RollenAbhaengigkeit.INSTITUTION),
	SACHBEARBEITER_INSTITUTION(RollenAbhaengigkeit.INSTITUTION),
	JURIST(RollenAbhaengigkeit.GEMEINDE),
	REVISOR(RollenAbhaengigkeit.GEMEINDE),
	STEUERAMT(RollenAbhaengigkeit.GEMEINDE),
	ADMIN_TS(RollenAbhaengigkeit.GEMEINDE),
	ADMIN_GEMEINDE(RollenAbhaengigkeit.GEMEINDE),
	SACHBEARBEITER_TS(RollenAbhaengigkeit.GEMEINDE),
	SACHBEARBEITER_GEMEINDE(RollenAbhaengigkeit.GEMEINDE),
	ADMIN_MANDANT(RollenAbhaengigkeit.KANTON),
	SACHBEARBEITER_MANDANT(RollenAbhaengigkeit.KANTON),
	ADMIN_SOZIALDIENST(RollenAbhaengigkeit.SOZIALDIENST),
	SACHBEARBEITER_SOZIALDIENST(RollenAbhaengigkeit.SOZIALDIENST),
	ADMIN_FERIENBETREUUNG(RollenAbhaengigkeit.GEMEINDE),
	SACHBEARBEITER_FERIENBETREUUNG(RollenAbhaengigkeit.GEMEINDE),
	GESUCHSTELLER(RollenAbhaengigkeit.NONE);

	@Nonnull
	private final RollenAbhaengigkeit rollenAbhaengigkeit;

	UserRole(@Nonnull RollenAbhaengigkeit rollenAbhaengigkeit) {
		this.rollenAbhaengigkeit = rollenAbhaengigkeit;
	}

	public boolean isRoleBgOnly() {
		return ADMIN_BG == this || SACHBEARBEITER_BG == this;
	}

	public boolean isRoleTsOnly() {
		return ADMIN_TS == this || SACHBEARBEITER_TS == this;
	}

	public boolean isRoleGemeindeOrBG() {
		return isRoleGemeinde() || isRoleBgOnly();
	}

	public boolean isRoleGemeindeOrTS() {
		return isRoleGemeinde() || isRoleTsOnly();
	}

	public boolean isRoleAnyAdminGemeinde() {
		return ADMIN_GEMEINDE == this || ADMIN_BG == this || ADMIN_TS == this;
	}

	public boolean isRoleGemeinde() {
		return  ADMIN_GEMEINDE == this || SACHBEARBEITER_GEMEINDE == this;
	}

	public boolean isRoleMandant() {
		return  ADMIN_MANDANT == this || SACHBEARBEITER_MANDANT == this;
	}

	public boolean isSuperadmin() {
		return SUPER_ADMIN == this;
	}

	public boolean isRoleFerienbetreuung() {
		return ADMIN_FERIENBETREUUNG == this || SACHBEARBEITER_FERIENBETREUUNG == this;
	}

	public boolean isRoleAdminTraegerschaftInstitution() {
		return  getInstitutionTraegerschaftAdminRoles().contains(this);
	}

	public boolean isRoleTraegerschaftInstitution() {
		return getInstitutionTraegerschaftRoles().contains(this);
	}

	public static List<UserRole> getAllAdminSuperAdminRevisorRoles() {
		return Arrays.asList(SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_MANDANT, ADMIN_INSTITUTION,
			ADMIN_TRAEGERSCHAFT, REVISOR);
	}

	public static List<UserRole> getAllAdminRoles() {
		return Arrays.asList(SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_MANDANT, ADMIN_INSTITUTION,
			ADMIN_TRAEGERSCHAFT, ADMIN_FERIENBETREUUNG, ADMIN_SOZIALDIENST);
	}

	/**
	 * Returns only the roles of TS
	 */
	public static List<UserRole> getTsOnlyRoles() {
		return Arrays.asList(ADMIN_TS, SACHBEARBEITER_TS);
	}

	/**
	 * Returns only the roles of BG
	 */
	public static List<UserRole> getBgOnlyRoles() {
		return Arrays.asList(ADMIN_BG, SACHBEARBEITER_BG);
	}

	/**
	 * Returns the roles of BG and Gemeinde
	 */
	public static List<UserRole> getBgAndGemeindeRoles() {
		return Arrays.asList(ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE);
	}

	/**
	 * Returns the roles of BG, TS and Gemeinde
	 */
	public static List<UserRole> getTsBgAndGemeindeRoles() {
		return Arrays.asList(ADMIN_TS, SACHBEARBEITER_TS, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE);
	}

	/**
	 * Returns the roles of TS and Gemeinde
	 */
	public static List<UserRole> getTsAndGemeindeRoles() {
		return Arrays.asList(ADMIN_TS, SACHBEARBEITER_TS, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE);
	}

	public static List<UserRole> getMandantRoles() {
		return Arrays.asList(ADMIN_MANDANT, SACHBEARBEITER_MANDANT);
	}

	public static List<UserRole> getMandantSuperadminRoles() {
		return Arrays.asList(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT);
	}

	public static List<UserRole> getJugendamtSuperadminRoles() {
		return Arrays.asList(ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SUPER_ADMIN);
	}

	public static List<UserRole> getInstitutionTraegerschaftRoles() {
		return Arrays.asList(ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION);
	}

	public static List<UserRole> getAllRolesForCoronaRueckforderung() {
		return Arrays.asList(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION);
	}

	public static List<UserRole> getInstitutionTraegerschaftAdminRoles() {
		return Arrays.asList(ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION);
	}

	public static List<UserRole> getSuperadminAllGemeindeRoles() {
		return Arrays.asList(SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS);
	}

	public static List<UserRole> getAllGemeindeAdminRoles() {
		return Arrays.asList(ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS);
	}

	public static List<UserRole> getAllGemeindeSachbearbeiterRoles() {
		return Arrays.asList(SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS);
	}

	public static List<UserRole> getAllGemeindeFerienbetreuungRoles() {
		return Arrays.asList(ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG,
			ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG);
	}

	public static List<UserRole> getAllGemeindeFerienbetreuungMandantSuperadminRoles() {
		List<UserRole> roles = new ArrayList<>();
		roles.add(SUPER_ADMIN);
		roles.addAll(getMandantRoles());
		roles.addAll(getAllGemeindeFerienbetreuungRoles());
		return roles;
	}

	public static List<UserRole> getAllInstitutionAdminRoles() {
		return Arrays.asList(ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT);
	}

	public static List<UserRole> getAllInstitutionSachbearbeiterRoles() {
		return Arrays.asList(SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT);
	}

	public static List<UserRole> getMandantBgGemeindeRoles() {
		List<UserRole> roles = new ArrayList<>();
		roles.add(SUPER_ADMIN);
		roles.addAll(getMandantBgGemeindeOnlyRoles());
		return roles;
	}

	public static List<UserRole> getMandantBgGemeindeOnlyRoles() {
		List<UserRole> roles = new ArrayList<>();
		roles.addAll(getMandantRoles());
		roles.addAll(getBgAndGemeindeRoles());
		return roles;
	}

	public static List<UserRole> getRolesByAbhaengigkeit(RollenAbhaengigkeit abhaengigkeit) {
		return Arrays.stream(UserRole.values())
			.filter(userRole -> userRole.getRollenAbhaengigkeit() == abhaengigkeit)
			.collect(Collectors.toList());
	}

	public static List<UserRole> getRolesWithoutAbhaengigkeit(RollenAbhaengigkeit abhaengigkeit) {
		return Arrays.stream(UserRole.values())
			.filter(userRole -> userRole.getRollenAbhaengigkeit() != abhaengigkeit)
			.collect(Collectors.toList());
	}

	public static List<UserRole> getRolesByAbhaengigkeiten(List<RollenAbhaengigkeit> abhaengigkeitList) {
		return Arrays.stream(UserRole.values())
			.filter(userRole -> abhaengigkeitList.contains(userRole.getRollenAbhaengigkeit()))
			.collect(Collectors.toList());
	}

	public static List<UserRole> getAllSozialdienstRoles() {
		return Arrays.asList(ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST);
	}

	public boolean isRoleGemeindeabhaengig(){
		return this.rollenAbhaengigkeit == RollenAbhaengigkeit.GEMEINDE;
	}

	public boolean isRoleSozialdienstabhaengig(){
		return this.rollenAbhaengigkeit == RollenAbhaengigkeit.SOZIALDIENST;
	}

	@Nonnull
	public RollenAbhaengigkeit getRollenAbhaengigkeit() {
		return rollenAbhaengigkeit;
	}

	public boolean isInstitutionRole() {
		return SACHBEARBEITER_INSTITUTION == this
			|| ADMIN_INSTITUTION == this
			|| SACHBEARBEITER_TRAEGERSCHAFT == this
			|| ADMIN_TRAEGERSCHAFT == this;
	}

	public static List<UserRole> getAllInstitutionRoles() {
		List<UserRole> all = getAllInstitutionAdminRoles();
		all.addAll(getAllInstitutionSachbearbeiterRoles());
		return all;
	}
}
