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

package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GeneratedDokument;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;

/**
 * Interface fuer eine Klasse welche prueft ob der aktuelle Benutzer fuer ein Gesuch berechtigt ist
 * Wirft eine Exception wenn der aktuelle Benutzer nicht berechtigt ist.
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public interface Authorizer {

	void checkReadAuthorization(@Nullable Gemeinde gemeinde);

	void checkWriteAuthorization(@Nullable Gemeinde gemeinde);

	void checkReadAuthorization(@Nullable Gesuch gesuch);

	void checkReadAuthorizationGesuche(@Nullable Collection<Gesuch> gesuche);

	/**
	 * prueft ob der aktuell eingeloggte benutzer das gesuch schreiben darf
	 */
	void checkWriteAuthorization(@Nullable Gesuch gesuch);

	/**
	 * Returns true when the user is authorized to read the dossier
	 */
	boolean isReadAuthorizedDossier(@Nullable Dossier dossier);

	/**
	 * Returns true when the user is authorized to read the gesuch.
	 */
	boolean isReadAuthorized(@Nullable Gesuch gesuch);

	/**
	 * Returns true when the user is authorized to read the dossier and all subobjects it contains. This is method is useful for some cases
	 * like when an Institution must get a dossier since it is only allowed when some gesuch of the dossier has a Betreuung of the
	 * given institution
	 * IMPORTANT. this method will do a deep check into the dossier so it will take more time. It should only be used when really needed.
	 */
	boolean isReadCompletelyAuthorizedDossier(@Nullable Dossier dossier);

	/**
	 * Wirft eine Exception, wenn der eingeloggte Benutzer nicht die Superadmin Rolle hat.
	 */
	void checkSuperadmin();

	/**
	 * prueft ob der aktuell eingeloggte benutzer den Fall mit id schreibend bearbeiten darf
	 */
	void checkWriteAuthorization(@Nullable Fall fall);

	/**
	 * prueft ob der aktuell eingeloggte benutzer das Dossier schreibend bearbeiten darf
	 */
	void checkWriteAuthorizationDossier(@Nullable Dossier dossier);

	/**
	 * prueft ob der aktuell eingeloggte benutzer den fall lesen darf
	 */
	void checkReadAuthorizationFall(@Nullable Fall fall);

	/**
	 * prueft ob der aktuell eingeloggte benutzer fuer ALLE uebergebnen faelle berechtigt ist
	 */
	void checkReadAuthorizationFaelle(@Nullable Collection<Fall> faelle);

	/**
	 * prueft ob der aktuell eingeloggte benutzer fuer ALLE uebergebnen Dossiers berechtigt ist
	 */
	void checkReadAuthorizationDossiers(@Nullable Collection<Dossier> dossiers);

	/**
	 * prueft, ob der aktuell eingeloggte Benutzer das uebergebene Dossier lesen darf
	 */
	void checkReadAuthorizationDossier(@Nonnull String dossierId);

	/**
	 * prueft, ob der aktuell eingeloggte Benutzer das uebergebene Dossier lesen darf
	 */
	void checkReadAuthorizationDossier(@Nullable Dossier dossier);

	/**
	 * prueft ob der aktuell eingeloggte benutzer die betreuung lesen darf
	 */
	void checkReadAuthorization(@Nullable AbstractPlatz platz);

	/**
	 * Prueft, ob der aktuell eingeloggte Benutzer den uebergebenen Benutzer lesen darf
	 */
	void checkReadAuthorization(@Nonnull Benutzer benutzer);

	/**
	 * prueft ob der aktuell eingeloggte benutzer die betreuung schreibend bearbeiten darf
	 */
	void checkWriteAuthorization(@Nullable AbstractPlatz abstractPlatz);

	/**
	 * prueft ob der aktuell eingeloggte benutzer den Benutzer schreibend bearbeiten darf
	 */
	void checkWriteAuthorization(@Nonnull Benutzer benutzer);

	/**
	 * prueft ob der aktuell eingeloggte benutzer ALLE Plaetze in der Liste lesen darf
	 */
	<T extends AbstractPlatz> void checkReadAuthorizationForAllPlaetze(@Nullable Collection<T> betreuungen);

	/**
	 * prueft ob der  eingeloggte benutzer EINEN der Plaetze in der Liste lesen darf
	 */
	<T extends AbstractPlatz> void checkReadAuthorizationForAnyPlaetze(@Nullable Collection<T> plaetze);

	/**
	 * prueft ob der aktuell eingeloggte Benutzer die Verfuegung lesen darf
	 */
	void checkReadAuthorization(@Nullable Verfuegung verfuegung);

	/**
	 * prueft ob der aktuell eingeloggte Benutzer die ALLE verfuegungen in der liste lesen darf
	 */
	void checkReadAuthorizationVerfuegungen(@Nullable Collection<Verfuegung> verfuegungen);

	/**
	 * prueft ob der aktuell eingeloggte benutzer die verfuegung schreibend bearbeiten darf
	 */
	void checkWriteAuthorization(@Nullable Verfuegung verfuegung);

	void checkReadAuthorization(@Nullable FinanzielleSituationContainer finanzielleSituation);

	void checkWriteAuthorization(@Nullable FinanzielleSituationContainer finanzielleSituation);

	void checkWriteAuthorization(@Nullable ErwerbspensumContainer ewpCnt);

	void checkReadAuthorization(@Nullable ErwerbspensumContainer ewpCnt);

	/**
	 * extrahiert die {@link FinanzielleSituation} von beiden GS wenn vorhanden und  prueft ob der aktuelle Benutzer berechtigt ist
	 */
	void checkReadAuthorizationFinSit(@Nullable Gesuch gesuch);

	void checkReadAuthorization(@Nullable WizardStep step);

	void checkWriteAuthorization(@Nullable WizardStep step);

	/**
	 * prueft ob der aktuelle Benutzer ein Gesuch fuer die Freigabe lesen darf
	 */
	void checkReadAuthorizationForFreigabe(Gesuch gesuch);

	/**
	 * Prueft, ob der aktuelle Benutzer diese Mitteilung schreiben (senden oder Entwurf speichern) darf
	 */
	void checkWriteAuthorizationMitteilung(@Nullable Mitteilung mitteilung);

	/**
	 * Prueft, ob der aktuelle Benutzer alle uebergebenen Mitteilungen lesen darf
	 */
	void checkReadAuthorizationMitteilungen(@Nonnull Collection<Mitteilung> mitteilungen);

	/**
	 * Prueft, ob der aktuelle Benutzer die uebergebene Mitteilung lesen darf
	 */
	void checkReadAuthorizationMitteilung(@Nullable Mitteilung mitteilung);

	/**
	 * Prueft, ob der aktuelle Benutzer die uebergebene Zahlung lesen darf
	 */
	void checkReadAuthorizationZahlung(@Nullable Zahlung zahlung);

	/**
	 * Prueft, ob der aktuelle Benutzer den uebergebenen Zahlungsauftrag lesen darf
	 */
	void checkReadAuthorizationZahlungsauftrag(@Nullable Zahlungsauftrag zahlungsauftrag);

	/**
	 * Prueft, ob der aktuelle Benutzer den uebergebenen Zahlungsauftrag editieren/erstellen darf
	 */
	void checkWriteAuthorizationZahlungsauftrag(@Nullable Zahlungsauftrag zahlungsauftrag);

	boolean isReadAuthorizationInstitutionStammdaten(@Nullable InstitutionStammdaten institutionStammdaten);

	boolean isWriteAuthorizationInstitutionStammdaten(@Nullable InstitutionStammdaten institutionStammdaten);

	void checkReadAuthorizationInstitution(@Nullable Institution institution);

	void checkWriteAuthorizationInstitution(@Nullable Institution institution);

	void checkReadAuthorization(@Nullable Traegerschaft traegerschaft);

	void checkWriteAuthorization(@Nullable Traegerschaft traegerschaft);

	void checkReadAuthorizationInstitutionStammdaten(@Nullable InstitutionStammdaten institutionStammdaten);

	void checkWriteAuthorizationInstitutionStammdaten(@Nullable InstitutionStammdaten institutionStammdaten);

	void checkWriteAuthorization(@Nullable RueckforderungFormular rueckforderungFormular);

	void checkWriteAuthorizationDocument(@Nullable RueckforderungFormular rueckforderungFormular);

	void checkReadAuthorization(@Nullable RueckforderungFormular rueckforderungFormular);

	void checkReadAuthorization(@Nullable AntragStatusHistory antragStatusHistory);

	void checkWriteAuthorization(@Nullable AntragStatusHistory antragStatusHistory);

	void checkReadAuthorization(@Nullable DokumentGrund dokumentGrund);

	void checkWriteAuthorization(@Nullable DokumentGrund dokumentGrund);

	void checkReadAuthorization(@Nullable GeneratedDokument generatedDokument);

	void checkWriteAuthorization(@Nullable GeneratedDokument generatedDokument);

	void checkReadAuthorization(@Nullable Mahnung mahnung);

	void checkWriteAuthorization(@Nullable Mahnung mahnung);

	void checkReadAuthorization(@Nullable LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer);

	void checkWriteAuthorization(@Nullable LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer);

	void checkReadAuthorization(@Nullable LastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainer);

	void checkWriteAuthorization(@Nullable LastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainer);
}
