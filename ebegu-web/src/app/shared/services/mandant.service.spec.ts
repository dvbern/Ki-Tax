import {TestBed} from '@angular/core/testing';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {KiBonMandant} from '../../core/constants/MANDANTS';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {SharedModule} from '../shared.module';

import {MandantService} from './mandant.service';

describe('MandantService', () => {
    let service: MandantService;
    const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
        ['isDevMode', 'getPublicPropertiesCached']);
    applicationPropertyRSSpy.getPublicPropertiesCached.and.resolveTo({} as any);
    const mockWindow = {
        location: {
            hostname: '',
            host: '',
        },
        localStorage: {
            key(): string | null {
                return undefined;
            },
            removeItem(): void {
            },
            setItem(): void {
            },
            clear(): void {
            },
            getItem: (): string => '',
            length: 0,
        },
        navigator: {
        }
    };
    const windowRefSpy = jasmine.createSpyObj<WindowRef>(WindowRef.name,
        [], {
            get nativeWindow(): any {
                return mockWindow;
            },
        });

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [SharedModule],
            providers: [
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy,
                },
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .overrideProvider(WindowRef, {useValue: windowRefSpy});
        service = TestBed.inject(MandantService);

    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('parseHostname tests', () => {
        describe('local-kibon domains', () => {
            it('should parse local-kibon url without sudomain to NONE', () => {
                mockWindow.location.hostname = 'local-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.NONE);
            });

            it('should parse local-kibon url with unknown. sudomain to NONE', () => {
                mockWindow.location.hostname = 'lucerne.local-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.NONE);
            });

            it('should parse local-kibon url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'be.local-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.BE);
            });

            it('should parse local-kibon url with lu. sudomain to LU', () => {
                mockWindow.location.hostname = 'lu.local-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.LU);
            });
        });
        // tslint:disable-next-line:no-identical-functions
        describe('dev-kibon domains', () => {
            it('should parse dev-kibon url without sudomain to NONE', () => {
                mockWindow.location.hostname = 'dev-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.NONE);
            });

            it('should parse dev-kibon url with unknown. sudomain to NONE', () => {
                mockWindow.location.hostname = 'lucerne.dev-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.NONE);
            });

            it('should parse dev-kibon url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'be.dev-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.BE);
            });

            it('should parse dev-kibon url with lu. sudomain to LU', () => {
                mockWindow.location.hostname = 'lu.dev-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.LU);
            });
        });
        // tslint:disable-next-line:no-identical-functions
        describe('uat-kibon domains', () => {
            it('should parse uat-kibon url without sudomain to NONE', () => {
                mockWindow.location.hostname = 'uat-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.NONE);
            });

            it('should parse uat-kibon url with unknown. sudomain to NONE', () => {
                mockWindow.location.hostname = 'lucerne.uat-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.NONE);
            });

            it('should parse uat-kibon url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'be.uat-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.BE);
            });

            it('should parse uat-kibon url with lu. sudomain to LU', () => {
                mockWindow.location.hostname = 'lu.uat-kibon.dvbern.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.LU);
            });
        });
        // tslint:disable-next-line:no-identical-functions
        describe('kibon domains', () => {
            it('should parse kibon url without sudomain to NONE', () => {
                mockWindow.location.hostname = 'kibon.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.NONE);
            });

            it('should parse kibon url with unknown. sudomain to NONE', () => {
                mockWindow.location.hostname = 'lucerne.kibon.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.NONE);
            });

            it('should parse kibon url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'be.kibon.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.BE);
            });

            it('should parse kibon url with lu. sudomain to LU', () => {
                mockWindow.location.hostname = 'lu.kibon.ch';
                expect(service.parseHostname()).toEqual(KiBonMandant.LU);
            });
        });
    });

    describe ('removeMandantFromCompleteHost tests', () => {
        describe('local-kibon tests', () => {
            it('should strip nothing from local-kibon.dvbern.ch:4200', () => {
                // tslint:disable-next-line:no-duplicate-string
                mockWindow.location.host = 'local-kibon.dvbern.ch:4200';
                expect(service.removeMandantFromCompleteHost()).toEqual('local-kibon.dvbern.ch:4200');
            });
            it('should strip be from be.local-kibon.dvbern.ch:4200', () => {
                mockWindow.location.host = 'be.local-kibon.dvbern.ch:4200';
                expect(service.removeMandantFromCompleteHost()).toEqual('local-kibon.dvbern.ch:4200');
            });
            it('should strip lu from lu.local-kibon.dvbern.ch:4200', () => {
                mockWindow.location.host = 'lu.local-kibon.dvbern.ch:4200';
                expect(service.removeMandantFromCompleteHost()).toEqual('local-kibon.dvbern.ch:4200');
            });
        });
        // tslint:disable-next-line:no-identical-functions
        describe('dev-kibon tests', () => {
            it('should strip nothing from dev-kibon.dvbern.ch:', () => {
                // tslint:disable-next-line:no-duplicate-string
                mockWindow.location.host = 'dev-kibon.dvbern.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('dev-kibon.dvbern.ch:');
            });
            it('should strip be from be.dev-kibon.dvbern.ch:', () => {
                mockWindow.location.host = 'be.dev-kibon.dvbern.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('dev-kibon.dvbern.ch:');
            });
            it('should strip lu from lu.dev-kibon.dvbern.ch:', () => {
                mockWindow.location.host = 'lu.dev-kibon.dvbern.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('dev-kibon.dvbern.ch:');
            });
        });
        // tslint:disable-next-line:no-identical-functions
        describe('uat-kibon tests', () => {
            it('should strip nothing from uat-kibon.dvbern.ch:', () => {
                // tslint:disable-next-line:no-duplicate-string
                mockWindow.location.host = 'uat-kibon.dvbern.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('uat-kibon.dvbern.ch:');
            });
            it('should strip be from be.uat-kibon.dvbern.ch:', () => {
                mockWindow.location.host = 'be.uat-kibon.dvbern.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('uat-kibon.dvbern.ch:');
            });
            it('should strip lu from lu.uat-kibon.dvbern.ch:', () => {
                mockWindow.location.host = 'lu.uat-kibon.dvbern.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('uat-kibon.dvbern.ch:');
            });
        });
        // tslint:disable-next-line:no-identical-functions
        describe('kibon tests', () => {
            it('should strip nothing from kibon.ch:', () => {
                // tslint:disable-next-line:no-duplicate-string
                mockWindow.location.host = 'kibon.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('kibon.ch:');
            });
            it('should strip be from be.kibon.ch:', () => {
                mockWindow.location.host = 'be.kibon.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('kibon.ch:');
            });
            it('should strip lu from lu.kibon.ch:', () => {
                mockWindow.location.host = 'lu.kibon.ch:';
                expect(service.removeMandantFromCompleteHost()).toEqual('kibon.ch:');
            });
        });
    });

});
