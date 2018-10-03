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

import {IAugmentedJQuery, IDirective, IDirectiveFactory, IDirectiveLinkFn, ILogService, IScope} from 'angular';

/**
 * This directive is a hack to suppress the enter handler that is defined by angular-material on the md-radio-group.
 * It is a problem because in our case we rely on angular behaving as described in
 * https://docs.angularjs.org/api/ng/directive/form where it specifically says if there are buttons with
 * type=submit in a form they should be triggered on enter.
 * Since the radio-group component does not do this and triggers a form submitt event instead we have to
 * work-around that prevents this. (Otherwise the unsavedChanges plugin sets the form back to pristine which is wrong
 * since no save was triggered).
 *
 * See also https://github.com/angular/material/issues/577
 *
 * @see EBEGU-897
 */
export default class DVSuppressFormSubmitOnEnter implements IDirective {

    public restrict = 'A';
    public link: IDirectiveLinkFn;
    public controller = DVSuppressFormSubmitOnEnterController;
    public require: any = {mdRadioGroupCtrl: 'mdRadioGroup', myCtrl: 'dvSuppressFormSubmitOnEnter'};

    public constructor() {
        this.link = (_scope: IScope, element: IAugmentedJQuery, _attrs, controllers: any) => {
            controllers.myCtrl.mdRadioGroupCtrl = controllers.mdRadioGroupCtrl;
            element.off('keydown'); // alle keydown listener auf dem element abhaengen
            element.bind('keydown', event => { // unseren eigenen listener definieren
                controllers.myCtrl.keydownListener(event, element);

            });
        };
    }

    public static factory(): IDirectiveFactory {
        return () => new DVSuppressFormSubmitOnEnter();
    }
}

/**
 * Direktive  die verhindert dass das form submitted wird wenn man enter drueckt auf einem radio-button
 */
export class DVSuppressFormSubmitOnEnterController {

    public static $inject: string[] = ['$mdConstant', '$mdUtil', '$log'];

    public mdRadioGroupCtrl: any; // see radioButton.js of angular material: mdRadioGroup

    public constructor(private readonly $mdConstant: any,
                       private readonly $mdUtil: any,
                       private readonly $log: ILogService) {

    }

    public keydownListener(ev: any, element: IAugmentedJQuery): void {
        const keyCode = ev.which || ev.keyCode;

        // Only listen to events that we originated ourselves
        // so that we don't trigger on things like arrow keys in
        // inputs.

        // tslint:disable
        if (keyCode != this.$mdConstant.KEY_CODE.ENTER &&
            ev.currentTarget != ev.target) {
            return;
        }
        // tslint:enable

        switch (keyCode) {
            case this.$mdConstant.KEY_CODE.LEFT_ARROW:
            case this.$mdConstant.KEY_CODE.UP_ARROW:
                ev.preventDefault();
                this.mdRadioGroupCtrl.selectPrevious();
                this.setFocus(element);
                return;
            case this.$mdConstant.KEY_CODE.RIGHT_ARROW:
            case this.$mdConstant.KEY_CODE.DOWN_ARROW:
                ev.preventDefault();
                this.mdRadioGroupCtrl.selectNext();
                this.setFocus(element);
                return;
            case this.$mdConstant.KEY_CODE.ENTER:
                // tslint:disable-next-line:no-commented-code
                // event.stopPropagation();    //we do not want to submit the form on enter
                // event.preventDefault();
                this.triggerNextButton(element);
                return;
            default:
                return;
        }
    }

    private setFocus(element: IAugmentedJQuery): void {
        if (!element.hasClass('md-focused')) {
            element.addClass('md-focused');
        }
    }

    private triggerNextButton(element: IAugmentedJQuery): void {
        let nextButtons: IAugmentedJQuery;
        const formElement: IAugmentedJQuery = angular.element(this.$mdUtil.getClosest(element[0], 'form'));
        if (!formElement) {
            return;
        }

        nextButtons = formElement.children().find('input[type="submit"], button[type="submit"]');
        if (nextButtons) {
            nextButtons.first().click();
        } else {
            this.$log.debug('no ".next" button found to click on enter');
        }

    }
}
