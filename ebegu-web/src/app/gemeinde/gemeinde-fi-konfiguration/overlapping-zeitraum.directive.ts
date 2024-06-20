/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {Directive, Input} from '@angular/core';
import {NG_VALIDATORS, Validator} from '@angular/forms';
import {TSFerieninselStammdaten} from '../../../models/TSFerieninselStammdaten';
import {TSFerieninselZeitraum} from '../../../models/TSFerieninselZeitraum';

@Directive({
    selector: '[appOverlappingZeitraum]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: OverlappingZeitraumDirective,
            multi: true
        }
    ]
})
export class OverlappingZeitraumDirective implements Validator {
    @Input('appOverlappingZeitraum')
    private readonly fiStammdatenList: TSFerieninselStammdaten[];

    public validate(): {[key: string]: any} | null {
        return this.fiStammdatenList
            ? overlappingValidator(this.fiStammdatenList)
            : null;
    }
}

function overlappingValidator(
    fiStammdatenList: TSFerieninselStammdaten[]
): {[key: string]: any} | null {
    let zeitraeume = extractZeitraeume(fiStammdatenList);
    // nur valide zeiträume betrachten. Ansonsten ist die Konfiguration sowieso invalid
    zeitraeume = zeitraeume.filter(
        z =>
            z.gueltigkeit && z.gueltigkeit.gueltigAb && z.gueltigkeit.gueltigBis
    );
    zeitraeume.sort(
        (a, b) =>
            a.gueltigkeit.gueltigAb.valueOf() -
            b.gueltigkeit.gueltigAb.valueOf()
    );
    let previous;
    for (const zeitraum of zeitraeume) {
        if (!previous) {
            previous = zeitraum;
            continue;
        }
        if (
            zeitraum.gueltigkeit.gueltigAb.isSameOrBefore(
                previous.gueltigkeit.gueltigBis
            )
        ) {
            return {dvOverlappingZeitraum: {value: zeitraum}};
        }
        previous = zeitraum;
    }
    return null;
}

function extractZeitraeume(
    stammdatenList: TSFerieninselStammdaten[]
): TSFerieninselZeitraum[] {
    let zeitraeume: TSFerieninselZeitraum[] = [];
    for (const stammdaten of stammdatenList) {
        zeitraeume = zeitraeume.concat(stammdaten.zeitraumList);
    }
    return zeitraeume;
}
