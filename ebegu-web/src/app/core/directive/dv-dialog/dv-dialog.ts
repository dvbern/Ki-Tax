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

import {IFormController, ILogService, IPromise} from 'angular';
import {RemoveDialogParams} from '../../../../gesuch/dialog/RemoveDialogController';
import IDialogOptions = angular.material.IDialogOptions;
import IDialogService = angular.material.IDialogService;

export class DvDialog {
    public static $inject: ReadonlyArray<string> = ['$mdDialog', '$log'];

    public constructor(
        private readonly $mdDialog: IDialogService,
        private readonly $log: ILogService
    ) {}

    /**
     * Erstellt einen neuen confim Dialog mit den uebergegebenen Parametern
     *
     * @param template Man kann ein belibiges Template eingeben in dem man das Layout des ganzen Dialogs gestaltet.
     * @param controller Hier implementiert man die verschiedenen Funktionen, die benoetigt sind
     * @param params Ein JS-Objekt {key-value}. Alle definierte Keys werden dann mit dem gegebenen Wert in Controller
     *         injected
     */
    public showDialog(
        template: string,
        controller?: any,
        params?: any
    ): IPromise<any> {
        // form parameter is required for injection for RemoveDialogController, so set missing parameter here.
        // Im IE11 ist controller.name undefined!
        if (controller.name === 'RemoveDialogController' && !params.form) {
            this.$log.error(
                'You should not use showDialog() for a RemoveDialogController. Use showRemoveDialog() instead!'
            );
            params.form = undefined;
        }
        const confirm: IDialogOptions = {
            template,
            controller,
            controllerAs: 'vm',
            locals: params
        };
        return this.$mdDialog.show(confirm);
    }

    /**
     * Erstellt einen neuen remove Dialog mit den uebergegebenen Parametern.
     * Diese Methode soll fuer den RemoveDialog verwendet werden, da sie zwingend ein Form verlangt. Das
     * Form wird benoetigt um das Form beim clicken von CANCEL wieder dirty zu setzen. Falls man kein form hat kann auc
     * undefined uebergeben werden
     *
     * @param template Man kann ein belibiges Template eingeben in dem man das Layout des ganzen Dialogs gestaltet.
     * @param form Fuer den RemoveDialog muss zwingend ein Form mitgegeben werden, damit beim Abbrechen das Form wieder
     *         dirty gesetzt werden kann.
     * @param controller Hier implementiert man die verschiedenen Funktionen, die benoetigt sind
     * @param params Ein JS-Objekt {key-value}. Alle definierte Keys werden dann mit dem gegebenen Wert in Controller
     *         injected
     */
    public showRemoveDialog(
        template: string,
        form: IFormController,
        controller?: any,
        params?: {[k in RemoveDialogParams]?: any}
    ): IPromise<any> {
        // form is the only required parameter, thus it's explicitly kept in the method signature
        params.form = form;
        const confirm: IDialogOptions = {
            template,
            controller,
            controllerAs: 'vm',
            locals: {params}
        };
        return this.$mdDialog.show(confirm);
    }

    public showDialogFullscreen(
        template: string,
        controller?: any,
        params?: any
    ): IPromise<any> {
        const confirm: IDialogOptions = {
            template,
            controller,
            controllerAs: 'vm',
            fullscreen: true,
            locals: params
        };
        return this.$mdDialog.show(confirm);
    }
}
