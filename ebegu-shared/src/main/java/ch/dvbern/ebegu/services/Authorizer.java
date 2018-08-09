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

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.WizardStep;

/**
 * Interface fuer eine Klasse welche prueft ob der aktuelle Benutzer fuer ein Gesuch berechtigt ist
 * Wirft eine Exception wenn der aktuelle Benutzer nicht berechtigt ist.
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public interface Authorizer {

	void checkReadAuthorization(@Nullable Gesuch gesuch);

	void checkReadAuthorizationGesuche(@Nullable Collection<Gesuch> gesuche);

	void checkReadAuthorizationGesuchId(String gesuchId);

	/**
	 * prueft ob der aktuell eingeloggte benutzer das gesuch schreiben darf
	 */
	void checkWriteAuthorization(@Nullable Gesuch gesuch);

	/**
	 * prueft ob ein Benutzer einen Fall lesen kall
	 */
	void checkReadAuthorizationFall(String fallId);

	/**
	 * Returns true when the user is authorized to read the dossier
	 */
	boolean isReadAuthorizedDossier(@Nullable Dossier dossier);

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
	void checkReadAuthorization(@Nullable Betreuung betr);

	/**
	 * Prueft, ob der aktuell eingeloggte Benutzer den uebergebenen Benutzer lesen darf
	 */
	void checkReadAuthorization(@Nonnull Benutzer benutzer);

	/**
	 * prueft ob der aktuell eingeloggte benutzer die betreuung schreibend bearbeiten darf
	 */
	void checkWriteAuthorization(@Nullable Betreuung betreuungToRemove);

	/**
	 * prueft ob der aktuell eingeloggte benutzer ALLE betreuung in der Liste lesen darf
	 */
	void checkReadAuthorizationForAllBetreuungen(@Nullable Collection<Betreuung> betreuungen);

	/**
	 * prueft ob der  eingeloggte benutzer EINE der  betreuung in der Liste lesen darf
	 */
	void checkReadAuthorizationForAnyBetreuungen(@Nullable Collection<Betreuung> betreuungen);

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

	void checkReadAuthorization(@Nonnull Collection<FinanzielleSituationContainer> finanzielleSituationen);

	void checkWriteAuthorization(@Nullable FinanzielleSituationContainer finanzielleSituation);

	void checkCreateAuthorizationFinSit(@Nonnull FinanzielleSituationContainer finanzielleSituation);

	void checkReadAuthorization(@Nullable ErwerbspensumContainer ewpCnt);

	/**
	 * extrahiert die {@link FinanzielleSituation} von beiden GS wenn vorhanden und  prueft ob der aktuelle Benutzer berechtigt ist
	 */
	void checkReadAuthorizationFinSit(@Nullable Gesuch gesuch);

	void checkReadAuthorization(@Nullable WizardStep step);

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
}
