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

import java.util.Arrays;
import java.util.List;

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
	ADMIN_MANDANT(RollenAbhaengigkeit.NONE),
	SACHBEARBEITER_MANDANT(RollenAbhaengigkeit.NONE),
	GESUCHSTELLER(RollenAbhaengigkeit.NONE);

	@Nonnull
	private final RollenAbhaengigkeit rollenAbhaengigkeit;

	UserRole(@Nonnull RollenAbhaengigkeit rollenAbhaengigkeit) {
		this.rollenAbhaengigkeit = rollenAbhaengigkeit;
	}

	public boolean isRoleSchulamt() {
		return ADMIN_TS == this || SACHBEARBEITER_TS == this || isRoleGemeinde();
	}

	public boolean isRoleJugendamt() {
		return ADMIN_BG == this || SACHBEARBEITER_BG == this || isRoleGemeinde();
	}

	public boolean isRoleGemeinde() {
		return  ADMIN_GEMEINDE == this || SACHBEARBEITER_GEMEINDE == this;
	}

	public boolean isSuperadmin() {
		return SUPER_ADMIN == this;
	}

	public static List<UserRole> getAllAdminSuperAdminRevisorRoles() {
		return Arrays.asList(SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_MANDANT, ADMIN_INSTITUTION,
			ADMIN_TRAEGERSCHAFT, REVISOR);
	}

	/**
	 * Returns only the roles of TS
	 */
	public static List<UserRole> getSchulamtRoles() {
		return Arrays.asList(ADMIN_TS, SACHBEARBEITER_TS);
	}

	/**
	 * Returns only the roles of BG
	 */
	public static List<UserRole> getJugendamtRoles() {
		return Arrays.asList(ADMIN_BG, SACHBEARBEITER_BG);
	}

	public static List<UserRole> getJugendamtSuperadminRoles() {
		return Arrays.asList(ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SUPER_ADMIN);
	}

	public static List<UserRole> getInstitutionTraegerschaftRoles() {
		return Arrays.asList(ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION);
	}

	public static List<UserRole> getInstitutionTraegerschaftAdminRoles() {
		return Arrays.asList(ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION);
	}

	public static List<UserRole> getSuperadminAllGemeindeRoles() {
		return Arrays.asList(SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS);
	}

	/**
	 * ACHTUNG Diese Logik existiert auch im Client TSUser. Aenderungen muessen in beiden Orten gemacht werden.
	 */
	@Nonnull
	public Amt getAmt() {
		switch (this) {
		case ADMIN_BG:
		case SUPER_ADMIN:
		case SACHBEARBEITER_BG: {
			return Amt.JUGENDAMT;
		}
		case ADMIN_TS:
		case SACHBEARBEITER_TS: {
			return Amt.SCHULAMT;
		}
		case ADMIN_GEMEINDE:
		case SACHBEARBEITER_GEMEINDE: {
			return Amt.GEMEINDE;
		}
		default:
			return Amt.NONE;
		}
	}

	public boolean isRoleGemeindeabhaengig(){
		return this.rollenAbhaengigkeit == RollenAbhaengigkeit.GEMEINDE;
	}

	@Nonnull
	public RollenAbhaengigkeit getRollenAbhaengigkeit() {
		return rollenAbhaengigkeit;
	}
}
