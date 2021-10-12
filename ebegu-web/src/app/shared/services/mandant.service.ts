import {HttpBackend, HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {CookieService} from 'ngx-cookie-service';
import {Observable, ReplaySubject} from 'rxjs';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {KiBonMandant} from '../../core/constants/MANDANTS';
import {LogFactory} from '../../core/logging/LogFactory';
import {WindowRef} from '../../core/service/windowRef.service';

const LOG = LogFactory.createLog('MandantService');

@Injectable({
    providedIn: 'root',
})
export class MandantService {

    public get mandant$(): Observable<KiBonMandant> {
        return this._mandant$.asObservable();
    }

    private readonly _mandant$: ReplaySubject<KiBonMandant> = new ReplaySubject<KiBonMandant>(1);

    private readonly _multimandantActive$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    private readonly restUtil = new EbeguRestUtil();

    // Workaround, we somehow get a cyclic dependency when we try to inject this directly
    private readonly http: HttpClient;

    public constructor(
        private readonly windowRef: WindowRef,
        private readonly httpBackend: HttpBackend,
        // private readonly applicationPropertyService: ApplicationPropertyRS,
        // private readonly authService: AuthServiceRS,
        private readonly cookieService: CookieService,
    ) {
        this._mandant$.next(MandantService.findMandant(this.cookieService.get('mandant')));
        // y tho?
        this.http = new HttpClient(httpBackend);
        this.http.get(`${CONSTANTS.REST_API}application-properties/public/all`).subscribe(res => {
            const props = this.restUtil.parsePublicAppConfig(res);
            this._multimandantActive$.next(props.mulitmandantAktiv);
            // overwrite cookie if not active
            if (!props.mulitmandantAktiv) {
                this._mandant$.next(KiBonMandant.NONE);
            }
        }, err => LOG.error(err));
        // TODO: reenable once ApplicationPropertyRS is migrated
        // tslint:disable-next-line:no-commented-code
        // this.applicationPropertyService.getPublicPropertiesCached().then(properties => {
        //     this._multimandantActive$.next(properties.mulitmandantAktiv);
        // });
    }

    private static findMandant(hostname: string): KiBonMandant {
        switch (hostname.toLocaleLowerCase()) {
            case 'be':
                return KiBonMandant.BE;
            case 'lu':
                return KiBonMandant.LU;
            default:
                return KiBonMandant.NONE;
        }
    }

    public isMultimandantActive$(): Observable<boolean> {
        return this._multimandantActive$.asObservable();
    }

    public parseHostnameForMandant(): KiBonMandant {
        const hostParts = this.windowRef.nativeWindow.location.hostname.split('.');
        for (const part of hostParts) {
            const potentialMandant = MandantService.findMandant(part);
            if (potentialMandant !== KiBonMandant.NONE) {
                return potentialMandant;
            }
        }
        return KiBonMandant.NONE;
    }

    public selectMandant(mandant: string, url: string): void {
        const parsedMandant = MandantService.findMandant(mandant);

        // TODO: Restore AuthService once migrated
        this.http.post(CONSTANTS.REST_API + 'auth/set-mandant', {name: parsedMandant}).subscribe(() => {

            if (parsedMandant !== KiBonMandant.NONE) {
                this.redirectToMandantSubdomain(parsedMandant, url);
            }
        }, error => LOG.error(error));
    }

    public redirectToMandantSubdomain(mandant: KiBonMandant, url: string): void {
        const host = this.removeMandantFromCompleteHost();
        this.windowRef.nativeWindow.open(`${this.windowRef.nativeWindow.location.protocol}//${mandant}.${host}/${url}`,
            '_self');
    }

    public removeMandantFromCompleteHost(): string {
        const completeHost = this.windowRef.nativeWindow.location.host;
        const mandantCandidates = Object.values(KiBonMandant).filter(el => el.length > 0);

        let shortenedHost = this.windowRef.nativeWindow.location.host;
        const firstDotIdx = shortenedHost.indexOf('.');

        mandantCandidates.forEach(mandantCandidate => {
            const idx = completeHost.substring(0, firstDotIdx).indexOf(mandantCandidate);
            if (idx >= 0) {
                shortenedHost = shortenedHost.substring(idx + mandantCandidate.length + 1);
            }
        });

        return shortenedHost;
    }
}
