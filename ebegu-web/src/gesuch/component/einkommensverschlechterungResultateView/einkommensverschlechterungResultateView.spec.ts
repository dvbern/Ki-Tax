import '../../../bootstrap.ts';
import 'angular-mocks';
import {EbeguWebGesuch} from '../../gesuch.module';
import GesuchModelManager from '../../service/gesuchModelManager';
import BerechnungsManager from '../../service/berechnungsManager';
import {EinkommensverschlechterungResultateViewController} from './einkommensverschlechterungResultateView';
import TSFinanzielleSituationResultateDTO from '../../../models/dto/TSFinanzielleSituationResultateDTO';
import IInjectorService = angular.auto.IInjectorService;
import IHttpBackendService = angular.IHttpBackendService;
import IStateService = angular.ui.IStateService;

describe('einkommensverschlechterungResultateView', function () {

    let gesuchModelManager: GesuchModelManager;
    let berechnungsManager: BerechnungsManager;
    let ekvrvc: EinkommensverschlechterungResultateViewController;
    let $state: IStateService;

    beforeEach(angular.mock.module(EbeguWebGesuch.name));

    var component: any;
    var scope: angular.IScope;
    var $componentController: any;
    var stateParams: any;
    var state: any;
    var consta: any;
    var errorservice: any;

    beforeEach(angular.mock.inject(function ($injector: any) {
        $componentController = $injector.get('$componentController');
        gesuchModelManager = $injector.get('GesuchModelManager');
        berechnungsManager = $injector.get('BerechnungsManager');
        let $rootScope = $injector.get('$rootScope');
        scope = $rootScope.$new();
        let ebeguRestUtil = $injector.get('EbeguRestUtil');
        let $q = $injector.get('$q');
        stateParams = $injector.get('$stateParams');
        state = $injector.get('$state');
        consta = $injector.get('CONSTANTS');
        errorservice = $injector.get('ErrorService');


        spyOn(berechnungsManager, 'calculateFinanzielleSituation').and.returnValue($q.when({}));
        // ekvrvc = new EinkommensverschlechterungResultateViewController($injector.get('$stateParams'), $injector.get('$state'), gesuchModelManager,
        //     berechnungsManager, $injector.get('CONSTANTS'), $injector.get('ErrorService'));

    }));

    beforeEach(function () {
        gesuchModelManager.initGesuch(false);
        gesuchModelManager.initFamiliensituation();
        gesuchModelManager.initFinanzielleSituation();
    });

    it('should be defined', function () {
        spyOn(berechnungsManager, 'calculateEinkommensverschlechterung').and.returnValue({});
        var bindings: {};
        component = $componentController('einkommensverschlechterungResultateView', {$scope: scope}, bindings);
        expect(component).toBeDefined();
    });

    describe('calculateVeraenderung', () => {
        beforeEach(function () {
            //spyOn(berechnungsManager, 'calculateFinanzielleSituation').and.returnValue($q.when({}));
            ekvrvc = new EinkommensverschlechterungResultateViewController(stateParams, state, gesuchModelManager,
                berechnungsManager, consta, errorservice);

        });
        it('should return + 100.0%', () => {

            setValues(100, 200);
            expect(ekvrvc.calculateVeraenderung()).toEqual('+ 100.0 %');
        });

        it('should return - 50.0 %', () => {

            setValues(200, 100);
            expect(ekvrvc.calculateVeraenderung()).toEqual('- 50.0 %');
        });

        it('should return - 90.0%', () => {

            setValues(200, 20);
            expect(ekvrvc.calculateVeraenderung()).toEqual('- 90.0 %');
        });

        it('should return - 81.2 %', () => {

            setValues(59720, 11230);
            expect(ekvrvc.calculateVeraenderung()).toEqual('- 81.2 %');
        });

        function setValues( massgebendesEinkommen_vj: number, massgebendesEinkommen_bj: number) {
            let finsint: TSFinanzielleSituationResultateDTO = new TSFinanzielleSituationResultateDTO();
            finsint.massgebendesEinkommen = massgebendesEinkommen_bj;

            let finsintvj: TSFinanzielleSituationResultateDTO = new TSFinanzielleSituationResultateDTO();
            finsintvj.massgebendesEinkommen = massgebendesEinkommen_vj;

            spyOn(ekvrvc, 'getResultate').and.returnValue(finsint);
            ekvrvc.resultatVorjahr = finsintvj;
        }

    });
});


