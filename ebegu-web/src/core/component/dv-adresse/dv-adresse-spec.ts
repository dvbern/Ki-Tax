import {IScope, IRootScopeService} from 'angular';

describe('dvAdresse', function () {

    beforeEach(angular.mock.module('ebeguWeb.core'));

    let component: any;
    let scope: IScope;
    let $componentController: any;

    beforeEach(angular.mock.inject(function (_$componentController_: any, $rootScope: IRootScopeService) {
        $componentController = _$componentController_;
        scope = $rootScope.$new();
    }));

    it('should be defined', function () {
        /*
         To initialise your component controller you have to setup your (mock) bindings and
         pass them to $componentController.
         */
        let bindings: {};
        component = $componentController('dvAdresse', {$scope: scope}, bindings);
        expect(component).toBeDefined();
    });
});
