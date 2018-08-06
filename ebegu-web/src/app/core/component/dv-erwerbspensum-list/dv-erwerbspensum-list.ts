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

import {IComponentOptions, IOnInit} from 'angular';
import TSErwerbspensum from '../../../../models/TSErwerbspensum';
import TSErwerbspensumContainer from '../../../../models/TSErwerbspensumContainer';

const template = require('./dv-erwerbspensum-list.html');
require('./dv-erwerbspensum-list.less');

export class DVErwerbspensumListConfig implements IComponentOptions {
    transclude = false;

    bindings: any = {
        onRemove: '&',
        onAdd: '&',
        onEdit: '&',
        erwerbspensen: '<',
        tableId: '@',
        tableTitle: '@',
        addButtonVisible: '<',
        addButtonEnabled: '<',
        addButtonText: '@',
        inputId: '@'
    };
    template = template;
    controller = DVErwerbspensumListController;
    controllerAs = 'vm';
}

export class DVErwerbspensumListController implements IOnInit {

    static $inject: ReadonlyArray<string> = [];

    erwerbspensen: TSErwerbspensum[];
    tableId: string;
    tableTitle: string;
    inputId: string;
    addButtonText: string;
    addButtonVisible: boolean;
    addButtonEnabled: boolean;
    onRemove: (pensumToRemove: any) => void;
    onEdit: (pensumToEdit: any) => void;
    onAdd: () => void;

    $onInit() {
        if (!this.addButtonText) {
            this.addButtonText = 'add item';
        }
        if (this.addButtonVisible === undefined) {
            this.addButtonVisible = true;
        }
        if (this.addButtonEnabled === undefined) {
            this.addButtonEnabled = true;
        }
        //clear selected
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this.erwerbspensen.length; i++) {
            const obj: any = this.erwerbspensen[i];
            // FIXME woher kommt dieses Property?
            obj.isSelected = false;

        }
    }

    removeClicked(pensumToRemove: TSErwerbspensumContainer, index: any) {
        this.onRemove({pensum: pensumToRemove, index: index});
    }

    editClicked(pensumToEdit: any) {
        this.onEdit({pensum: pensumToEdit});
    }

    addClicked() {
        this.onAdd();
    }

    isRemoveAllowed(pensumToEdit: any) {
        // Loeschen erlaubt, solange das Gesuch noch nicht readonly ist. Dies ist notwendig, weil sonst in die Zukunft
        // erfasste Taetigkeiten bei nicht-zustandekommen des Jobs nicht mehr geloescht werden koennen
        // Siehe auch EBEGU-1146 und EBEGU-580
        return this.addButtonVisible;
    }

    public isZuschlagErwerbspensumConfigured(): boolean {
        // Wird aktuell ausgeblendet. Koennte aber spaeter von spezifischen Gemeinden einschaltet werden
        return false;
    }
}



