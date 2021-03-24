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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.util.Set;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxAdresse;

public class JaxFerienbetreuungAngabenStammdaten extends JaxAbstractDTO {

	private static final long serialVersionUID = 4363557668421396679L;

	@Nullable
	private Set<String> amAngebotBeteiligteGemeinden;

	@Nullable
	private String seitWannFerienbetreuungen;

	@Nullable
	private String traegerschaft;

	@Nullable
	private JaxAdresse stammdatenAdresse;

	@Nullable
	private String stammdatenKontaktpersonVorname;

	@Nullable
	private String stammdatenKontaktpersonNachname;

	@Nullable
	private String stammdatenKontaktpersonFunktion;

	@Nullable
	private String stammdatenKontaktpersonTelefon;

	@Nullable
	private String stammdatenKontaktpersonEmail;

	@Nullable
	private String iban;

	@Nullable
	private String kontoinhaber;

	@Nullable
	private JaxAdresse adresseKontoinhaber;

	@Nullable
	private String vermerkAuszahlung;


	@Nullable
	public Set<String> getAmAngebotBeteiligteGemeinden() {
		return amAngebotBeteiligteGemeinden;
	}

	public void setAmAngebotBeteiligteGemeinden(@Nullable Set<String> amAngebotBeteiligteGemeinden) {
		this.amAngebotBeteiligteGemeinden = amAngebotBeteiligteGemeinden;
	}

	@Nullable
	public String getSeitWannFerienbetreuungen() {
		return seitWannFerienbetreuungen;
	}

	public void setSeitWannFerienbetreuungen(@Nullable String seitWannFerienbetreuungen) {
		this.seitWannFerienbetreuungen = seitWannFerienbetreuungen;
	}

	@Nullable
	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Nullable
	public JaxAdresse getStammdatenAdresse() {
		return stammdatenAdresse;
	}

	public void setStammdatenAdresse(@Nullable JaxAdresse stammdatenAdresse) {
		this.stammdatenAdresse = stammdatenAdresse;
	}

	@Nullable
	public String getStammdatenKontaktpersonVorname() {
		return stammdatenKontaktpersonVorname;
	}

	public void setStammdatenKontaktpersonVorname(@Nullable String stammdatenKontaktpersonVorname) {
		this.stammdatenKontaktpersonVorname = stammdatenKontaktpersonVorname;
	}

	@Nullable
	public String getStammdatenKontaktpersonNachname() {
		return stammdatenKontaktpersonNachname;
	}

	public void setStammdatenKontaktpersonNachname(@Nullable String stammdatenKontaktpersonNachname) {
		this.stammdatenKontaktpersonNachname = stammdatenKontaktpersonNachname;
	}

	@Nullable
	public String getStammdatenKontaktpersonFunktion() {
		return stammdatenKontaktpersonFunktion;
	}

	public void setStammdatenKontaktpersonFunktion(@Nullable String stammdatenKontaktpersonFunktion) {
		this.stammdatenKontaktpersonFunktion = stammdatenKontaktpersonFunktion;
	}

	@Nullable
	public String getStammdatenKontaktpersonTelefon() {
		return stammdatenKontaktpersonTelefon;
	}

	public void setStammdatenKontaktpersonTelefon(@Nullable String stammdatenKontaktpersonTelefon) {
		this.stammdatenKontaktpersonTelefon = stammdatenKontaktpersonTelefon;
	}

	@Nullable
	public String getStammdatenKontaktpersonEmail() {
		return stammdatenKontaktpersonEmail;
	}

	public void setStammdatenKontaktpersonEmail(@Nullable String stammdatenKontaktpersonEmail) {
		this.stammdatenKontaktpersonEmail = stammdatenKontaktpersonEmail;
	}

	@Nullable
	public String getIban() {
		return iban;
	}

	public void setIban(@Nullable String iban) {
		this.iban = iban;
	}

	@Nullable
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	public void setKontoinhaber(@Nullable String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	@Nullable
	public JaxAdresse getAdresseKontoinhaber() {
		return adresseKontoinhaber;
	}

	public void setAdresseKontoinhaber(@Nullable JaxAdresse adresseKontoinhaber) {
		this.adresseKontoinhaber = adresseKontoinhaber;
	}

	@Nullable
	public String getVermerkAuszahlung() {
		return vermerkAuszahlung;
	}

	public void setVermerkAuszahlung(@Nullable String vermerkAuszahlung) {
		this.vermerkAuszahlung = vermerkAuszahlung;
	}
}
