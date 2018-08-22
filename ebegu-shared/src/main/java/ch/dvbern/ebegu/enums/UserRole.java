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

	SUPER_ADMIN(false),
	ADMIN_BG(true),
	SACHBEARBEITER_BG(true),
	SACHBEARBEITER_TRAEGERSCHAFT(false),
	SACHBEARBEITER_INSTITUTION(false),
	JURIST(true),
	REVISOR(true),
	STEUERAMT(true),
	ADMIN_TS(true),
	SCHULAMT(true),
	GESUCHSTELLER(false);

	private boolean isGemeindeabhaengig;

	UserRole(boolean isGemeindeabhaengig) {
		this.isGemeindeabhaengig = isGemeindeabhaengig;
	}

	public boolean isRoleSchulamt() {
		return ADMIN_TS == this || SCHULAMT == this;
	}

	public boolean isRoleJugendamt() {
		return ADMIN_BG == this || SACHBEARBEITER_BG == this;
	}

	public boolean isRoleGemeinde() {
		//TODO (KIBON-6) Rolle Gemeinde gibts noch nicht. Aber der Superadmin wird wohl sowas wie GEMEINDE haben
		return isSuperadmin();
	}

	public boolean isSuperadmin() {
		return SUPER_ADMIN == this;
	}

	public static List<UserRole> getAdminSuperAdminRoles() {
		return Arrays.asList(SUPER_ADMIN, ADMIN_BG, ADMIN_TS);
	}

	public static List<UserRole> getSchulamtRoles() {
		return Arrays.asList(ADMIN_TS, SCHULAMT);
	}

	public static List<UserRole> getJugendamtRoles() {
		return Arrays.asList(ADMIN_BG, SACHBEARBEITER_BG);
	}

	public static List<UserRole> getJugendamtSuperadminRoles() {
		return Arrays.asList(ADMIN_BG, SACHBEARBEITER_BG, SUPER_ADMIN);
	}

	public static List<UserRole> getInstitutionTraegerschaftRoles() {
		return Arrays.asList(SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION);
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
		case SCHULAMT: {
			return Amt.SCHULAMT;
		}
		default:
			return Amt.NONE;
		}
	}

	public boolean isRoleGemeindeabhaengig(){
		return isGemeindeabhaengig;
	}
}
