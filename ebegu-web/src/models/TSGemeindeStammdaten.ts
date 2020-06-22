/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {TSRoleUtil} from '../utils/TSRoleUtil';
import {TSAbstractEntity} from './TSAbstractEntity';
import {TSAdresse} from './TSAdresse';
import {TSBenutzer} from './TSBenutzer';
import {TSGemeinde} from './TSGemeinde';
import {TSGemeindeKonfiguration} from './TSGemeindeKonfiguration';
import {TSGesuchsperiode} from './TSGesuchsperiode';
import {TSTextRessource} from './TSTextRessource';

export class TSGemeindeStammdaten extends TSAbstractEntity {
    public administratoren: string; // read only
    public sachbearbeiter: string; // read only
    public defaultBenutzerBG: TSBenutzer;
    public defaultBenutzerTS: TSBenutzer;
    public defaultBenutzer: TSBenutzer;
    public gemeinde: TSGemeinde;
    public adresse: TSAdresse;
    public bgAdresse: TSAdresse;
    public bgTelefon: string;
    public bgEmail: string;
    public tsAdresse: TSAdresse;
    public tsTelefon: string;
    public tsEmail: string;
    public beschwerdeAdresse: TSAdresse;
    public mail: string;
    public telefon: string;
    public webseite: string;
    public korrespondenzspracheDe: boolean;
    public korrespondenzspracheFr: boolean;
    public benutzerListeBG: TSBenutzer[]; // read only
    public benutzerListeTS: TSBenutzer[]; // read only
    public kontoinhaber: string;
    public bic: string;
    public iban: string;
    public standardRechtsmittelbelehrung: boolean;
    public rechtsmittelbelehrung: TSTextRessource;
    public benachrichtigungBgEmailAuto: boolean;
    public benachrichtigungTsEmailAuto: boolean;
    public standardDokSignature: boolean;
    public standardDokTitle: string;
    public standardDokUnterschriftTitel: string;
    public standardDokUnterschriftName: string;
    public standardDokUnterschriftTitel2: string;
    public standardDokUnterschriftName2: string;
    public tsVerantwortlicherNachVerfuegungBenachrichtigen: boolean;
    // ---------- Konfiguration ----------
    public konfigurationsListe: TSGemeindeKonfiguration[];
    public externalClients: string[];
    public usernameScolaris: string;

    public getGemeindeKonfigurationForGesuchsperiode(gesuchsperiode: TSGesuchsperiode): TSGemeindeKonfiguration {
        for (const konfigurationsListeElement of this.konfigurationsListe) {
            if (konfigurationsListeElement.gesuchsperiode.id === gesuchsperiode.id) {
                return konfigurationsListeElement;
            }
        }
        return undefined;
    }

    /**
     * Wir suchen einen Defaultbenutzer mit der Rolle BG oder GEMEINDE, falls ein spezifischer gesetzt ist
     * in defaultBenutzerBG, so verwenden wir diesen, sonst pruefen wir, ob der allgemeine Defaultbenutzer
     * zufaellig die gewuenschte Rolle hat. Falls dies auch nicht der Fall ist, geben wir einfach den ersten
     * Benutzer aus der BG-Benutzerliste zurueck.
     * Achtung: Diese Methode ist aehnlich auch auf dem Server vorhanden
     */
    public getDefaultBenutzerWithRoleBG(): TSBenutzer {
        if (this.defaultBenutzerBG && this.defaultBenutzerBG.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGRoles())) {
            return this.defaultBenutzerBG;
        }
        if (this.defaultBenutzer && this.defaultBenutzer.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGRoles())) {
            return this.defaultBenutzer;
        }
        // Es gibt keinen gesetzten Defaultbenutzer mit der gewuenschten Rolle
        console.error('kein defaultbenutzer BG fuer gemeinde', this.gemeinde.name);
        if (this.benutzerListeBG && this.benutzerListeBG.length > 0) {
            return this.benutzerListeBG[0];
        }
        console.error('kein benutzer BG fuer gemeinde', this.gemeinde.name);
        return undefined;
    }

    /**
     * Wir suchen einen Defaultbenutzer mit der Rolle TS oder GEMEINDE, falls ein spezifischer gesetzt ist
     * in defaultBenutzerTS, so verwenden wir diesen, sonst pruefen wir, ob der allgemeine Defaultbenutzer
     * zufaellig die gewuenschte Rolle hat. Falls dies auch nicht der Fall ist, geben wir einfach den ersten
     * Benutzer aus der TS-Benutzerliste zurueck.
     * Achtung: Diese Methode ist aehnlich auch auf dem Server vorhanden
     */
    public getDefaultBenutzerWithRoleTS(): TSBenutzer {
        if (this.defaultBenutzerTS && this.defaultBenutzerTS.hasOneOfRoles(TSRoleUtil.getGemeindeOrTSRoles())) {
            return this.defaultBenutzerTS;
        }
        if (this.defaultBenutzer && this.defaultBenutzer.hasOneOfRoles(TSRoleUtil.getGemeindeOrTSRoles())) {
            return this.defaultBenutzer;
        }
        // Es gibt keinen gesetzten Defaultbenutzer mit der gewuenschten Rolle
        console.error('kein defaultbenutzer TS fuer gemeinde', this.gemeinde.name);
        if (this.benutzerListeTS && this.benutzerListeTS.length > 0) {
            return this.benutzerListeTS[0];
        }
        console.error('kein benutzer TS fuer gemeinde', this.gemeinde.name);
        return undefined;
    }
}
