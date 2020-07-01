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

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Keys für die zeitabhängigen E-BEGU-Vorlagen
 */
public enum EbeguVorlageKey {

	// Vorlagen fuer oeffentliche Institutionen
	VORLAGE_NOTRECHT_KITA_DE("/vorlagenNotrecht/Belegung_Antrag_Finanzierung_Kita.xlsx"),
	VORLAGE_NOTRECHT_KITA_FR("/vorlagenNotrecht/Formulaire_demande_financement_corona_garderie.xlsx"),
	VORLAGE_NOTRECHT_TFO_DE("/vorlagenNotrecht/Belegung_Antrag_Finanzierung_TFO.xlsx"),
	VORLAGE_NOTRECHT_TFO_FR("/vorlagenNotrecht/Formulaire_demande_financement_corona_accueil_familial_de_jo.xlsx"),
	// Vorlagen fuer private Institutionen
	VORLAGE_NOTRECHT_PRIVAT_KITA_DE("/vorlagenNotrecht/Belegung_Antrag_Finanzierung_Kita.xlsx"),
	VORLAGE_NOTRECHT_PRIVAT_KITA_FR("/vorlagenNotrecht/Formulaire_demande_financement_corona_garderie.xlsx"),
	VORLAGE_NOTRECHT_PRIVAT_TFO_DE("/vorlagenNotrecht/Belegung_Antrag_Finanzierung_TFO.xlsx"),
	VORLAGE_NOTRECHT_PRIVAT_TFO_FR("/vorlagenNotrecht/Formulaire_demande_financement_corona_accueil_familial_de_jo.xlsx");

	private String defaultVorlagePath;


	EbeguVorlageKey(String defaultVorlagePath) {
		this.defaultVorlagePath = defaultVorlagePath;
	}


	public String getDefaultVorlagePath() {
		return defaultVorlagePath;
	}

	public void setDefaultVorlagePath(String defaultVorlagePath) {
		this.defaultVorlagePath = defaultVorlagePath;
	}

	@Nullable
	public static EbeguVorlageKey getNotrechtVorlageOeffentlicheInstitutionen(@Nonnull String language, @Nonnull BetreuungsangebotTyp angebotTyp) {
		if (angebotTyp == BetreuungsangebotTyp.KITA) {
			if (Locale.FRENCH.getLanguage().equals(language)) {
				return VORLAGE_NOTRECHT_KITA_FR;
			}
			return VORLAGE_NOTRECHT_KITA_DE;
		}
		if (angebotTyp == BetreuungsangebotTyp.TAGESFAMILIEN) {
			if (Locale.FRENCH.getLanguage().equals(language)) {
				return VORLAGE_NOTRECHT_TFO_FR;
			}
			return VORLAGE_NOTRECHT_TFO_DE;
		}
		return null;
	}

	@Nullable
	public static EbeguVorlageKey getNotrechtVorlagePrivateInstitutionen(@Nonnull String language, @Nonnull BetreuungsangebotTyp angebotTyp) {
		if (angebotTyp == BetreuungsangebotTyp.KITA) {
			if (Locale.FRENCH.getLanguage().equals(language)) {
				return VORLAGE_NOTRECHT_PRIVAT_KITA_FR;
			}
			return VORLAGE_NOTRECHT_PRIVAT_KITA_DE;
		}
		if (angebotTyp == BetreuungsangebotTyp.TAGESFAMILIEN) {
			if (Locale.FRENCH.getLanguage().equals(language)) {
				return VORLAGE_NOTRECHT_PRIVAT_TFO_FR;
			}
			return VORLAGE_NOTRECHT_PRIVAT_TFO_DE;
		}
		return null;
	}
}
