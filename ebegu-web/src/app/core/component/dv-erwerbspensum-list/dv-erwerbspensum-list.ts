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
import {TSErwerbspensum} from '../../../../models/TSErwerbspensum';
import {TSErwerbspensumContainer} from '../../../../models/TSErwerbspensumContainer';

export class DVErwerbspensumListConfig implements IComponentOptions {
    public transclude = false;

    public bindings = {
        onRemove: '&',
        onAdd: '&',
        onEdit: '&',
        erwerbspensen: '<',
        tableId: '@',
        tableTitle: '@',
        addButtonVisible: '<',
        addButtonText: '@',
        inputId: '@'
    };
    public template = require('./dv-erwerbspensum-list.html');
    public controller = DVErwerbspensumListController;
    public controllerAs = 'vm';
}

export class DVErwerbspensumListController implements IOnInit {
    public static $inject: ReadonlyArray<string> = [];

    public erwerbspensen: TSErwerbspensum[];
    public tableId: string;
    public tableTitle: string;
    public inputId: string;
    public addButtonText: string;
    public addButtonVisible: boolean;
    public onRemove: (pensumToRemove: any) => void;
    public onEdit: (pensumToEdit: any) => void;
    public onAdd: () => void;

    public $onInit(): void {
        if (!this.addButtonText) {
            this.addButtonText = 'add item';
        }
        if (this.addButtonVisible === undefined) {
            this.addButtonVisible = true;
        }
        // clear selected
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this.erwerbspensen.length; i++) {
            const obj: any = this.erwerbspensen[i];
            // FIXME woher kommt dieses Property?
            obj.isSelected = false;
        }
    }

    public removeClicked(
        pensumToRemove: TSErwerbspensumContainer,
        index: any
    ): void {
        this.onRemove({pensum: pensumToRemove, index});
    }

    public editClicked(pensumToEdit: any): void {
        this.onEdit({pensum: pensumToEdit});
    }

    public addClicked(): void {
        this.onAdd();
    }

    public isRemoveAllowed(_pensumToEdit: TSErwerbspensumContainer): boolean {
        // Loeschen erlaubt, solange das Gesuch noch nicht readonly ist. Dies ist notwendig, weil sonst in die Zukunft
        // erfasste Taetigkeiten bei nicht-zustandekommen des Jobs nicht mehr geloescht werden koennen
        // Siehe auch EBEGU-1146 und EBEGU-580
        return this.addButtonVisible && _pensumToEdit.isGSContainerEmpty();
    }
}
