import {TestBed} from '@angular/core/testing';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {SharedModule} from '../shared.module';

import {MandantService} from './mandant.service';

describe('MandantService', () => {
    let service: MandantService;
    const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
        ['isDevMode', 'getPublicPropertiesCached']);
    applicationPropertyRSSpy.getPublicPropertiesCached.and.resolveTo({} as any);

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [SharedModule],
            providers: [
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy,
                }
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES);
        service = TestBed.inject(MandantService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
