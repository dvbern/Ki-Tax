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

import {TSGemeinde} from '../TSGemeinde';
import {TSGesuchsperiode} from '../TSGesuchsperiode';

export enum TSBetreuungsangebotTyp {
    KITA = 'KITA',
    TAGESFAMILIEN = 'TAGESFAMILIEN',
    TAGESSCHULE = 'TAGESSCHULE',
    FERIENINSEL = 'FERIENINSEL'
}

export function getTSBetreuungsangebotTypValuesForMandant(
    tagesschuleEnabledForMandant: boolean
): Array<TSBetreuungsangebotTyp> {
    const angebote: Array<TSBetreuungsangebotTyp> = [];
    angebote.push(TSBetreuungsangebotTyp.KITA);
    angebote.push(TSBetreuungsangebotTyp.TAGESFAMILIEN);
    if (tagesschuleEnabledForMandant) {
        angebote.push(TSBetreuungsangebotTyp.TAGESSCHULE);
        angebote.push(TSBetreuungsangebotTyp.FERIENINSEL);
    }
    return angebote;
}

export function getTSBetreuungsangebotTypValuesForMandantIfTagesschulanmeldungen(
    tagesschuleEnabledForMandant: boolean,
    tagesschuleAnmeldungenConfigured: boolean,
    gemeinde: TSGemeinde,
    gesuchsperiode: TSGesuchsperiode
): Array<TSBetreuungsangebotTyp> {
    const angebote: Array<TSBetreuungsangebotTyp> = [];
    if (gemeinde.angebotBG && gesuchsperiode.gueltigkeit.gueltigBis.isAfter(gemeinde.betreuungsgutscheineStartdatum)) {
        angebote.push(TSBetreuungsangebotTyp.KITA);
        angebote.push(TSBetreuungsangebotTyp.TAGESFAMILIEN);
    }
    if (tagesschuleEnabledForMandant && tagesschuleAnmeldungenConfigured && gemeinde.angebotTS
        && gesuchsperiode.gueltigkeit.gueltigBis.isAfter(gemeinde.tagesschulanmeldungenStartdatum)) {
        angebote.push(TSBetreuungsangebotTyp.TAGESSCHULE);
    }
    if (tagesschuleEnabledForMandant && gemeinde.angebotFI
        && gesuchsperiode.gueltigkeit.gueltigBis.isAfter(gemeinde.ferieninselanmeldungenStartdatum)) {
        angebote.push(TSBetreuungsangebotTyp.FERIENINSEL);
    }
    return angebote;
}

export function getSchulamtBetreuungsangebotTypValues(): Array<TSBetreuungsangebotTyp> {
    return [
        TSBetreuungsangebotTyp.TAGESSCHULE,
        TSBetreuungsangebotTyp.FERIENINSEL,
    ];
}

export function getBgInstitutionenAndTsBetreuungsangebote(): ReadonlyArray<TSBetreuungsangebotTyp> {
    return getBgInstitutionenBetreuungsangebote().concat([TSBetreuungsangebotTyp.TAGESSCHULE]);
}

export function getBgInstitutionenBetreuungsangebote(): ReadonlyArray<TSBetreuungsangebotTyp> {
    return [
        TSBetreuungsangebotTyp.KITA,
        TSBetreuungsangebotTyp.TAGESFAMILIEN,
    ];
}

export function isSchulamt(status: TSBetreuungsangebotTyp): boolean {
    return status === TSBetreuungsangebotTyp.TAGESSCHULE || status === TSBetreuungsangebotTyp.FERIENINSEL;
}

export function isJugendamt(status: TSBetreuungsangebotTyp): boolean {
    return !isSchulamt(status);
}

/**
 * Gibt true zurueck wenn der gegebene betreuungsangebotTyp in der Liste types gefunden wird
 */
export function isOfAnyBetreuungsangebotTyp(
    betreuungsangebotTyp: TSBetreuungsangebotTyp,
    types: TSBetreuungsangebotTyp[],
): boolean {
    return types.filter(type => betreuungsangebotTyp === type).length > 0;
}
