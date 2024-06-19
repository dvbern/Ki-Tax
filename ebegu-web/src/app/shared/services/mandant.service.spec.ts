/* eslint-disable  */
import {HttpClientModule} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {CookieService} from 'ngx-cookie-service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {MANDANTS} from '../../core/constants/MANDANTS';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';

import {MandantService} from './mandant.service';

// eslint-disable-next-line
describe('MandantService', () => {
    let service: MandantService;
    const applicationPropertyRSSpy =
        jasmine.createSpyObj<ApplicationPropertyRS>(
            ApplicationPropertyRS.name,
            ['isDevMode', 'getPublicPropertiesCached']
        );
    applicationPropertyRSSpy.getPublicPropertiesCached.and.resolveTo({} as any);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name,
        ['principal$', 'isOneOfRoles']
    );
    const mockWindow = {
        location: {
            hostname: '',
            host: ''
        },
        localStorage: {
            key(): string | null {
                return undefined;
            },
            removeItem(): void {},
            setItem(): void {},
            clear(): void {},
            getItem: (): string => '',
            length: 0
        },
        navigator: {}
    };
    const windowRefSpy = jasmine.createSpyObj<WindowRef>(WindowRef.name, [], {
        get nativeWindow(): any {
            return mockWindow;
        }
    });

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientModule],
            providers: [
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy
                },
                {
                    provide: AuthServiceRS,
                    useValue: authServiceSpy
                },
                CookieService
            ]
        }).overrideProvider(WindowRef, {useValue: windowRefSpy});
        service = TestBed.inject(MandantService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('parseHostname tests', () => {
        describe('local domains', () => {
            it('should parse local url without sudomain to NONE', () => {
                mockWindow.location.hostname = 'local.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.NONE
                );
            });

            it('should parse local url with unknown. sudomain to NONE', () => {
                mockWindow.location.hostname = 'lucerne.local.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.NONE
                );
            });

            it('should parse local url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'local-be.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.BERN
                );
            });

            it('should parse local url with stadtluzern. sudomain to LU', () => {
                mockWindow.location.hostname = 'local-stadtluzern.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.LUZERN
                );
            });
        });
        // eslint-disable-next-line
        describe('dev-kibon domains', () => {
            it('should parse dev-kibon url without sudomain to NONE', () => {
                mockWindow.location.hostname = 'dev.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.NONE
                );
            });

            it('should parse dev-kibon url with unknown. sudomain to NONE', () => {
                mockWindow.location.hostname = 'lucerne.dev.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.NONE
                );
            });

            it('should parse dev-kibon url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'dev-be.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.BERN
                );
            });

            it('should parse dev-kibon url with stadtluzern. sudomain to LU', () => {
                mockWindow.location.hostname = 'dev-stadtluzern.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.LUZERN
                );
            });
        });
        // eslint-disable-next-line
        describe('uat-kibon domains', () => {
            it('should parse uat-kibon url without sudomain to NONE', () => {
                mockWindow.location.hostname = 'uat.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.NONE
                );
            });

            it('should parse uat-kibon url with unknown. sudomain to NONE', () => {
                mockWindow.location.hostname = 'lucerne.uat.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.NONE
                );
            });

            it('should parse uat-kibon url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'uat-be.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.BERN
                );
            });

            it('should parse old uat-be-kibon url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'uat-kibon-be.dvbern.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.BERN
                );
            });

            it('should parse uat-kibon url with stadtluzern. sudomain to LU', () => {
                mockWindow.location.hostname = 'dev-stadtluzern.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.LUZERN
                );
            });
        });
        // eslint-disable-next-line
        describe('kibon domains', () => {
            it('should parse kibon url without sudomain to NONE', () => {
                mockWindow.location.hostname = 'kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.NONE
                );
            });

            it('should parse kibon url with unknown. sudomain to NONE', () => {
                mockWindow.location.hostname = 'lucerne.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.NONE
                );
            });

            it('should parse kibon url with be. sudomain to BE', () => {
                mockWindow.location.hostname = 'be.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.BERN
                );
            });

            it('should parse kibon url with stadtluzern. sudomain to LU', () => {
                mockWindow.location.hostname = 'stadtluzern.kibon.ch';
                expect(service.parseHostnameForMandant()).toEqual(
                    MANDANTS.LUZERN
                );
            });
        });
    });

    describe('removeMandantFromCompleteHost tests', () => {
        const dvBernDomain = 'kibon.ch';
        describe('local tests', () => {
            const dvBernLocalDomain = 'kibon.ch:4200';
            it('should strip nothing from local.kibon.ch:4200', () => {
                // eslint-disable-next-line
                mockWindow.location.host = 'local.kibon.ch:4200';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernLocalDomain);
            });
            it('should strip be from local-be.kibon.ch:4200', () => {
                mockWindow.location.host = 'local-be.kibon.ch:4200';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernLocalDomain);
            });
            it('should strip lu from local-stadtluzern.kibon.ch:4200', () => {
                mockWindow.location.host = 'local-stadtluzern.kibon.ch:4200';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernLocalDomain);
            });
        });
        // eslint-disable-next-line
        describe('dev-kibon tests', () => {
            it('should strip dev-kibon from dev-kibon.kibon.ch', () => {
                // eslint-disable-next-line
                mockWindow.location.host = 'dev.kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernDomain);
            });
            it('should strip dev-be from dev-be.kibon.ch', () => {
                mockWindow.location.host = 'dev-be.kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernDomain);
            });
            it('should strip dev-stadtluzern from dev-stadtluzern.kibon.ch', () => {
                mockWindow.location.host = 'dev-stadtluzern.kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernDomain);
            });
        });
        // eslint-disable-next-line
        describe('uat-kibon tests', () => {
            it('should strip uat-kibon from uat-kibon.kibon.ch', () => {
                // eslint-disable-next-line
                mockWindow.location.host = 'uat.kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernDomain);
            });
            it('should strip uat-be from uat-be.kibon.ch', () => {
                mockWindow.location.host = 'uat-be.kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernDomain);
            });
            it('should strip uat-stadtluzern from uat-stadtluzern.kibon.ch', () => {
                mockWindow.location.host = 'uat-stadtluzern.kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual(dvBernDomain);
            });
        });
        // eslint-disable-next-line
        describe('kibon tests', () => {
            it('should strip nothing from kibon.ch', () => {
                // eslint-disable-next-line
                mockWindow.location.host = 'kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual('kibon.ch');
            });
            it('should strip be from be.kibon.ch:', () => {
                mockWindow.location.host = 'be.kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual('kibon.ch');
            });
            it('should strip lu from stadtluzern.kibon.ch:', () => {
                mockWindow.location.host = 'stadtluzern.kibon.ch';
                expect(
                    service.removeMandantEnvironmentFromCompleteHost()
                ).toEqual('kibon.ch');
            });
        });
    });

    describe('getEnvironmentFromCompleteHost tests', () => {
        describe('local tests', () => {
            const dvBernLocalDomain = 'local';
            it('should get environment local from local.kibon.ch:4200', () => {
                // eslint-disable-next-line
                mockWindow.location.host = 'local.kibon.ch:4200';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    dvBernLocalDomain
                );
            });
            it('should get environment local from local-be.kibon.ch:4200', () => {
                mockWindow.location.host = 'local-be.kibon.ch:4200';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    dvBernLocalDomain
                );
            });
            it('should get environment local from local-stadtluzern.kibon.ch:4200', () => {
                mockWindow.location.host = 'local-stadtluzern.kibon.ch:4200';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    dvBernLocalDomain
                );
            });
        });
        // eslint-disable-next-line
        describe('dev-kibon tests', () => {
            const devEnvironment = 'dev';
            it('should get environment dev from dev.kibon.ch', () => {
                // eslint-disable-next-line
                mockWindow.location.host = 'dev.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    devEnvironment
                );
            });
            it('should get environment dev-kibon from dev-be.kibon.ch', () => {
                mockWindow.location.host = 'dev-be.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    devEnvironment
                );
            });
            it('should get environment dev-kibon from dev-stadtluzern.kibon.ch', () => {
                mockWindow.location.host = 'dev-stadtluzern.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    devEnvironment
                );
            });
        });
        // eslint-disable-next-line
        describe('uat tests', () => {
            const uatEnvironment = 'uat';
            it('should get environment uat from uat.kibon.ch', () => {
                // eslint-disable-next-line
                mockWindow.location.host = 'uat.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    uatEnvironment
                );
            });
            it('should get environment uat-kibon from old uat-kibon-be.dvbern.ch', () => {
                mockWindow.location.host = 'uat-kibon-be.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    uatEnvironment
                );
            });
            it('should get environment uat-kibon from uat-be.kibon.ch', () => {
                mockWindow.location.host = 'uat-be.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    uatEnvironment
                );
            });
            it('should get environment uat-kibon from uat-stadtluzern.kibon.ch', () => {
                mockWindow.location.host = 'uat-stadtluzern.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual(
                    uatEnvironment
                );
            });
        });
        // eslint-disable-next-line
        describe('kibon tests', () => {
            it('should get nothing from kibon.ch', () => {
                // eslint-disable-next-line
                mockWindow.location.host = 'kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual('');
            });
            it('should get nothing from be.kibon.ch:', () => {
                mockWindow.location.host = 'be.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual('');
            });
            it('should get nothing from stadtluzern.kibon.ch:', () => {
                mockWindow.location.host = 'stadtluzern.kibon.ch';
                expect(service.getEnvironmentFromCompleteHost()).toEqual('');
            });
        });
    });
});
