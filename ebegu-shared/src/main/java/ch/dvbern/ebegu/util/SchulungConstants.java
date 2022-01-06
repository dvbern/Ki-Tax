/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util;

/**
 * Interface fuer Konstanten zu den Mandanten.
 */
public final class SchulungConstants {
	private final String gemeindeTutorialId;

	private final String gemeindeStammdatenTutorialId;
	private final String traegerschaftFischId;
	private final String institutionForelleId;
	private final String institutionHechtId;
	private final String institutionLachsId;
	private final String institutionTutorialId;
	private final String kitaForelleId;
	private final String tageselternForelleId;
	private final String kitaHechtId;
	private final String kitaTutorialId;
	private final String kitaBruennenStammdatenId;

	private final String gesuchId;

	public SchulungConstants(
			String gemeindeTutorialId,
			String gemeindeStammdatenTutorialId,
			String traegerschaftFischId,
			String institutionForelleId,
			String institutionHechtId,
			String institutionLachsId,
			String institutionTutorialId,
			String kitaForelleId,
			String tageselternForelleId,
			String kitaHechtId,
			String kitaTutorialId,
			String kitaBruennenStammdatenId, String gesuchId) {
		this.gemeindeTutorialId = gemeindeTutorialId;
		this.gemeindeStammdatenTutorialId = gemeindeStammdatenTutorialId;
		this.traegerschaftFischId = traegerschaftFischId;
		this.institutionForelleId = institutionForelleId;
		this.institutionHechtId = institutionHechtId;
		this.institutionLachsId = institutionLachsId;
		this.institutionTutorialId = institutionTutorialId;
		this.kitaForelleId = kitaForelleId;
		this.tageselternForelleId = tageselternForelleId;
		this.kitaHechtId = kitaHechtId;
		this.kitaTutorialId = kitaTutorialId;
		this.kitaBruennenStammdatenId = kitaBruennenStammdatenId;
		this.gesuchId = gesuchId;
	}

	public String getTraegerschaftFischId() {
		return traegerschaftFischId;
	}

	public String getInstitutionForelleId() {
		return institutionForelleId;
	}

	public String getInstitutionHechtId() {
		return institutionHechtId;
	}

	public String getInstitutionLachsId() {
		return institutionLachsId;
	}

	public String getInstitutionTutorialId() {
		return institutionTutorialId;
	}

	public String getKitaForelleId() {
		return kitaForelleId;
	}

	public String getTageselternForelleId() {
		return tageselternForelleId;
	}

	public String getKitaHechtId() {
		return kitaHechtId;
	}

	public String getKitaTutorialId() {
		return kitaTutorialId;
	}

	public String getKitaBruennenStammdatenId() {
		return kitaBruennenStammdatenId;
	}

	public String getGesuchId() {
		return gesuchId;
	}

	public String getGemeindeTutorialId() {
		return gemeindeTutorialId;
	}

	public String getGemeindeStammdatenTutorialId() {
		return gemeindeStammdatenTutorialId;
	}
}
