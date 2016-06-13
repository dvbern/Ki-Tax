import {EbeguWebPendenzen} from '../../pendenzen.module';
import PendenzRS from '../../service/PendenzRS.rest';
import {PendenzenListViewController} from './pendenzenListView';
import {IScope, IQService, IFilterService, IHttpBackendService} from 'angular';
import TSPendenzJA from '../../../models/TSPendenzJA';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import GesuchsperiodeRS from '../../../core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';
describe('pendenzenListView', function () {

    let institutionRS: InstitutionRS;
    let gesuchsperiodeRS: GesuchsperiodeRS;
    let pendenzRS: PendenzRS;
    let pendenzListViewController: PendenzenListViewController;
    let $q: IQService;
    let $scope: IScope;
    let $filter: IFilterService;
    let $httpBackend: IHttpBackendService;


    beforeEach(angular.mock.module(EbeguWebPendenzen.name));

    beforeEach(angular.mock.inject(function ($injector: any) {
        pendenzRS = $injector.get('PendenzRS');
        institutionRS = $injector.get('InstitutionRS');
        gesuchsperiodeRS = $injector.get('GesuchsperiodeRS');
        $q = $injector.get('$q');
        $scope = $injector.get('$rootScope');
        $filter = $injector.get('$filter');
        $httpBackend = $injector.get('$httpBackend');
    }));


    describe('API Usage', function () {
        describe('getPendenzenList', function () {
            it('should return the list with all pendenzen', function () {
                let mockPendenz: TSPendenzJA = new TSPendenzJA(123, 'name', TSAntragTyp.GESUCH, undefined,
                    undefined, [TSBetreuungsangebotTyp.KITA], ['Inst1, Inst2']);
                let result: Array<TSPendenzJA> = [mockPendenz];
                spyOn(pendenzRS, 'getPendenzenList').and.returnValue($q.when(result));

                $httpBackend.when('GET', '/ebegu/api/v1/institutionen').respond({});
                $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});

                pendenzListViewController = new PendenzenListViewController(pendenzRS, undefined, $filter, institutionRS, gesuchsperiodeRS);
                $scope.$apply();
                expect(pendenzRS.getPendenzenList).toHaveBeenCalled();

                let list: Array<TSPendenzJA> = pendenzListViewController.getPendenzenList();
                expect(list).toBeDefined();
                expect(list.length).toBe(1);
                expect(list[0]).toEqual(mockPendenz);
            });
        });
        describe('translateBetreuungsangebotTypList', () => {
            it('returns a comma separated string with all BetreuungsangebotTypen', () => {
                let list: Array<TSBetreuungsangebotTyp> = [TSBetreuungsangebotTyp.KITA, TSBetreuungsangebotTyp.TAGESELTERN];
                expect(pendenzListViewController.translateBetreuungsangebotTypList(list))
                    .toEqual('Tagesstätte für Kleinkinder, Tageseltern');
            });
            it('returns an empty string for invalid values or empty lists', () => {
                expect(pendenzListViewController.translateBetreuungsangebotTypList([])).toEqual('');
                expect(pendenzListViewController.translateBetreuungsangebotTypList(undefined)).toEqual('');
                expect(pendenzListViewController.translateBetreuungsangebotTypList(null)).toEqual('');
            });
        });
    });
});