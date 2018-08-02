/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {Component, Input, OnInit} from '@angular/core';
import TSDossier from '../../../models/TSDossier';
import TSFall from '../../../models/TSFall';
import EbeguUtil from '../../../utils/EbeguUtil';
import DossierRS from '../../service/dossierRS.rest';
import FallRS from '../../service/fallRS.rest';

require('./fallToolbar.less');

@Component({
    selector: 'dv-fall-toolbar',
    template: require('./fallToolbar.template.html'),
})
export class FallToolbarComponent implements OnInit {

    @Input() fallId: string;
    fall: TSFall;
    dossierList: TSDossier[] = [];

    constructor(private dossierRS: DossierRS,
        private fallRS: FallRS) {
    }

    public ngOnInit(): void {
        this.fallRS.findFall(this.fallId).then(fall => {
            if (fall) {
                this.fall = fall;
                this.dossierRS.findDossiersByFall(this.fall.id).then(dossiers => {
                    this.dossierList = dossiers;
                });
            }
        });
    }

    // todo KIBON-25 implement this hier and remove it from dossiertoolbar
    private hasBesitzer(): boolean {
        return true;
    //     return this.dossier
    //         && this.dossier.fall
    //         && this.dossier.fall.besitzer !== null
    //         && this.dossier.fall.besitzer !== undefined;
    }

    private getFallNummer(): string {
        return this.fall ? EbeguUtil.addZerosToFallNummer(this.fall.fallNummer) : '';
    }

    public openDossier(dossier: TSDossier): void {

    }

    public createNewDossier(): void {

    }
}
