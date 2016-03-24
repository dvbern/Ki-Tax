/// <reference path="../../../../typings/browser.d.ts" />
module app.FamiliensituationView {
    'use strict';

    class FamiliensituationViewComponentConfig implements angular.IComponentOptions {
        transclude: boolean;
        bindings: any;
        templateUrl: string | Function;
        controller: any;
        controllerAs: string;

        constructor() {
            this.transclude = false;
            this.bindings = {};
            this.templateUrl = 'src/gesuch/component/familiensituationView/familiensituationView.html';
            this.controller = FamiliensituationViewController;
            this.controllerAs = 'vm';
        }
    }


    class FamiliensituationViewController  {
        gesuch: any; // todo Add right type

        static $inject = [];
        /* @ngInject */
        constructor() {
        }

    }

    angular.module('ebeguWeb.gesuch').component('familiensituationView', new FamiliensituationViewComponentConfig());

}
