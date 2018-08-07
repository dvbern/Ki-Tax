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

import {Component, Input, OnChanges, OnInit} from '@angular/core';
import TSDossier from '../../../models/TSDossier';
import EbeguUtil from '../../../utils/EbeguUtil';
import DossierRS from '../../service/dossierRS.rest';

require('./fallToolbar.less');

@Component({
    selector: 'dv-fall-toolbar',
    template: require('./fallToolbar.template.html'),
})
export class FallToolbarComponent implements OnInit, OnChanges {

    @Input() fallId: string;
    @Input() dossierId: string;
    @Input() defaultGemeindeName: string;
    // fall: TSFall;
    dossierList: TSDossier[] = [];
    selectedDossier?: TSDossier;

    constructor(private dossierRS: DossierRS) {
    }

    ngOnInit(): void {
        this.loadObjects();
    }

    private loadObjects() {
        if (!this.useDefaultValues()) {
            this.dossierRS.findDossiersByFall(this.fallId).then(dossiers => {
                this.dossierList = dossiers;
                this.setSelectedDossier();
            });
        }
    }

    ngOnChanges(changes: any) {
        this.loadObjects();
    }

    public useDefaultValues(): boolean {
        return !this.dossierId && !this.fallId;
    }

    private setSelectedDossier() {
        this.selectedDossier = this.dossierList.find(dossier => dossier.id === this.dossierId);
    }

    private hasBesitzer(): boolean {
        return this.selectedDossier
            && this.selectedDossier.fall
            && !EbeguUtil.isNullOrUndefined(this.selectedDossier.fall.besitzer);
    }

    private getFallNummer(): string {
        if (this.selectedDossier && this.selectedDossier.fall) {
            return EbeguUtil.addZerosToFallNummer(this.selectedDossier.fall.fallNummer);
        }
        return '';
    }

    public openDossier(dossier: TSDossier): void {
        this.selectedDossier = dossier;
    }

    public createNewDossier(): void {

    }

    public isDossierActive(dossier: TSDossier): boolean {
        return !EbeguUtil.isNullOrUndefined(this.selectedDossier)
            && !EbeguUtil.isNullOrUndefined(dossier)
            && this.selectedDossier.id === dossier.id;
    }
}
