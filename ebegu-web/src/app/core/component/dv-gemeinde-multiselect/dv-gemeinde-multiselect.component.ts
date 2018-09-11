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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../../models/TSGemeinde';

/**
 * Component fuer den GemeindeDialog. In einem Select muss der Benutzer die Gemeinde auswaehlen.
 * Keine Gemeinde wird by default ausgewaehlt, damit der Benutzer nicht aus Versehen die falsche Gemeinde auswaehlt.
 * Die GemeindeListe wird von aussen gegeben, damit dieser Component von nichts abhaengt. Die ausgewaehlte Gemeinde
 * wird dann beim Close() zurueckgegeben
 */
@Component({
    selector: 'dv-gemeinde-multiselect',
    templateUrl: './dv-gemeinde-multiselect.template.html',
    styleUrls: ['./dv-gemeinde-multiselect.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DvGemeindeMultiselectComponent implements OnInit {

    @Input() enabled: boolean = false;
    @Input() selected: TSGemeinde[]; // Die selektierten Gemeinden

    allowedMap$: Observable<Map<TSGemeinde, boolean>>; // Die Gemeinden, die zur Auswahl stehen sollen

    constructor(private readonly authServiceRS: AuthServiceRS,
                private readonly gemeindeRS: GemeindeRS) {
    }

    ngOnInit() {
        this.allowedMap$ = this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(map(gemeinden => {
                    return gemeinden.reduce((currentMap, currentValue) => {
                        const value = !!this.selected.find(g => g.id === currentValue.id);
                        return currentMap.set(currentValue, value);
                    }, new Map<TSGemeinde, boolean>());
                }),
            );
    }

    onChange(item: {key: TSGemeinde, value: boolean}) {
        if (item.value) {
            this.selected.push(item.key);
        } else {
            const index = this.selected.indexOf(item.key);
            this.selected.splice(index, 1);
        }
    }
}
