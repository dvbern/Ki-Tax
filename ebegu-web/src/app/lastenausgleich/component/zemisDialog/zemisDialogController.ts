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

import {IFormController, IPromise} from 'angular';
import {ZemisDialogDTO} from './zemisDialog.interface';
import IDialogService = angular.material.IDialogService;

export class ZemisDialogController {

    public static $inject = ['$mdDialog', 'upload'];

    public jahr: number;
    public form: IFormController;
    private file: File;
    public upload: boolean;

    public constructor(
        private readonly $mdDialog: IDialogService,
        upload: boolean,
    ) {
        this.upload = upload;
    }

    public hide(year: number | null): IPromise<any> {
        return this.$mdDialog.hide(year);
    }

    public cancel(): void {
        this.$mdDialog.cancel();
    }

    public ok(): void {
        if (!this.form.$valid) {
            return;
        }
        const output: ZemisDialogDTO = {
            jahr: this.jahr,
            file: this.file
        };
        this.$mdDialog.hide(output);
    }

    public handleFileInput(files: File[]): void {
        this.file = files[0];
    }
}
