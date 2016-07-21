import '../../../bootstrap.ts';
import 'angular-mocks';
import GesuchModelManager from '../../service/gesuchModelManager';
import {EbeguWebGesuch} from '../../gesuch.module';
import IInjectorService = angular.auto.IInjectorService;
import IHttpBackendService = angular.IHttpBackendService;
import IQService = angular.IQService;
import IScope = angular.IScope;

describe('einkommensverschlechterungView', function () {


    let gesuchModelManager: GesuchModelManager;

    beforeEach(angular.mock.module(EbeguWebGesuch.name));

    var component: any;
    var scope: angular.IScope;
    var $componentController: any;

    beforeEach(angular.mock.inject(function ($injector: any) {
        $componentController = $injector.get('$componentController');
        gesuchModelManager = $injector.get('GesuchModelManager');
        let $rootScope = $injector.get('$rootScope');
        scope = $rootScope.$new();
    }));


    beforeEach(function () {
        gesuchModelManager.initGesuch(false);
        gesuchModelManager.initFamiliensituation();
        gesuchModelManager.initFinanzielleSituation();
    });

    it('should be defined', function () {
        /*
         To initialise your component controller you have to setup your (mock) bindings and
         pass them to $componentController.
         */
        var bindings: {};
        component = $componentController('einkommensverschlechterungView', {$scope: scope}, bindings);
        expect(component).toBeDefined();
    });

});