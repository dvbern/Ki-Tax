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
import {ControlContainer, NgForm} from '@angular/forms';
import { MatOptionSelectionChange } from '@angular/material/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSGemeinde} from '../../../../models/TSGemeinde';

let nextId = 0;

/**
 * Component fuer den GemeindeDialog. In einem Select muss der Benutzer die Gemeinde auswaehlen.
 * Keine Gemeinde wird by default ausgewaehlt, damit der Benutzer nicht aus Versehen die falsche Gemeinde auswaehlt.
 * Die GemeindeListe wird von aussen gegeben, damit dieser Component von nichts abhaengt. Die ausgewaehlte Gemeinde
 * wird dann beim Close() zurueckgegeben
 */
@Component({
    selector: 'dv-gemeinde-multiselect',
    templateUrl: './gemeinde-multiselect.component.html',
    styleUrls: ['./gemeinde-multiselect.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class GemeindeMultiselectComponent implements OnInit {

    @Input() public required: boolean = false;
    @Input() public selected!: TSGemeinde[]; // Die selektierten Gemeinden
    @Input() public disabled: boolean = false;
    @Input() public allowedInMap$: Observable<TSGemeinde[]>;
    @Input() public showLabel: boolean = true;

    public allowedMap$: Observable<Map<TSGemeinde, boolean>>; // Die Gemeinden, die zur Auswahl stehen sollen
    public inputId = `gemeinde-select-${nextId++}`;

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
        public readonly form: NgForm,
    ) {
    }

    public ngOnInit(): void {
        this.allowedMap$ = this.allowedInMap$ === undefined || this.allowedInMap$ === null
            ? this.createMap$(this.gemeindeRS.getGemeindenForPrincipal$())
            : this.createMap$(this.allowedInMap$);
    }

    private createMap$(gemeindenList$: Observable<TSGemeinde[]>): Observable<Map<TSGemeinde, boolean>> {
        return gemeindenList$.pipe(map(gemeinden => {
                return gemeinden.reduce((currentMap, currentValue) => {
                    const found = this.selected.find(g => g.id === currentValue.id);
                    return currentMap.set(found || currentValue, !!found);
                }, new Map<TSGemeinde, boolean>());
            }),
        );
    }

    public onSelectionChange(item: MatOptionSelectionChange): void {
        if (!item.isUserInput) {
            return;
        }

        if (item.source.selected) {
            this.selected.push(item.source.value);
        } else {
            const index = this.selected.indexOf(item.source.value);
            this.selected.splice(index, 1);
        }
    }

    public isIE(): boolean {
        const ua = navigator.userAgent;
        /* MSIE used to detect old browsers and Trident used to newer ones*/
        return ua.indexOf('MSIE ') > -1 || ua.indexOf('Trident/') > -1;
    }
}
