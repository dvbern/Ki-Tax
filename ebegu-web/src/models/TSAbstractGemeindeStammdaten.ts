/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {TSAbstractEntity} from './TSAbstractEntity';
import {TSAdresse} from './TSAdresse';
import {TSGemeindeKonfiguration} from './TSGemeindeKonfiguration';
import {TSGesuchsperiode} from './TSGesuchsperiode';

export class TSAbstractGemeindeStammdaten extends TSAbstractEntity {
    public adresse: TSAdresse;
    public mail: string;
    public telefon: string;
    public webseite: string;
    public korrespondenzspracheDe: boolean;
    public korrespondenzspracheFr: boolean;
    public altGemeindeKontaktText: string;
    public hasAltGemeindeKontakt: boolean;
    // ---------- Konfiguration ----------
    public konfigurationsListe: TSGemeindeKonfiguration[];

    public getGemeindeKonfigurationForGesuchsperiode(
        gesuchsperiode: TSGesuchsperiode
    ): TSGemeindeKonfiguration {
        for (const konfigurationsListeElement of this.konfigurationsListe) {
            if (
                konfigurationsListeElement.gesuchsperiode.id ===
                gesuchsperiode.id
            ) {
                return konfigurationsListeElement;
            }
        }
        return undefined;
    }
}
