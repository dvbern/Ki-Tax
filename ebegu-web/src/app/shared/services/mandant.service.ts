import {HttpClient} from '@angular/common/http';
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

    public constructor(
        private readonly windowRef: WindowRef,
        private readonly http: HttpClient,
        private readonly cookieService: CookieService,
    ) {
        // Workaround, we somehow get a cyclic dependency when we try to inject this directly
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
            case 'so':
                return KiBonMandant.SO;
            default:
                return KiBonMandant.NONE;
        }
    }

    public async initMandantCookie(): Promise<void> {
        const mandantFromCookie = MandantService.findMandant(this.cookieService.get('mandant'));
        const mandantFromUrl = this.parseHostnameForMandant();

        if (mandantFromCookie !== mandantFromUrl && mandantFromUrl !== KiBonMandant.NONE) {
            await this.setMandantCookie(mandantFromUrl);
            this._mandant$.next(mandantFromUrl);
        } else {
            this._mandant$.next(mandantFromCookie);
        }
        this.initMultimandantActivated();
    }

    private initMultimandantActivated(): void {
        this.http.get(`${CONSTANTS.REST_API}application-properties/public/all`).subscribe(res => {
            const props = this.restUtil.parsePublicAppConfig(res);
            this._multimandantActive$.next(props.mulitmandantAktiv);
            // overwrite cookie if not active
            if (!props.mulitmandantAktiv) {
                this._mandant$.next(KiBonMandant.NONE);
            }
        }, err => LOG.error(err));
    }

    public isMultimandantActive$(): Observable<boolean> {
        return this._multimandantActive$.asObservable();
    }

    public parseHostnameForMandant(): KiBonMandant {
        const regex = /(be|so|lu)(?=.(dvbern|kibon))/g;
        const matches = regex.exec(this.windowRef.nativeWindow.location.hostname);
        if (matches === null) {
            return KiBonMandant.NONE;
        }
        return MandantService.findMandant(matches[0]);
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

    public setMandantCookie(mandant: KiBonMandant): Promise<any> {
        // TODO: Restore AuthService once migrated
        return this.http.post(CONSTANTS.REST_API + 'auth/set-mandant', {name: mandant}).toPromise() as Promise<any>;
    }

    public redirectToMandantSubdomain(mandant: KiBonMandant, url: string): void {
        const host = this.removeMandantFromCompleteHost();
        const environment = this.getEnvironmentFromCompleteHost();
        const environmentWithMandant = environment.length > 0 ? `${environment}-${mandant}` : mandant;
        this.windowRef.nativeWindow.open(`${this.windowRef.nativeWindow.location.protocol}//${environmentWithMandant}.${host}/${url}`,
            '_self');
    }

    public removeMandantFromCompleteHost(): string {
        const mandantCandidates = Object.values(KiBonMandant).filter(el => el.length > 0);
        const shortenedHost = this.windowRef.nativeWindow.location.host.split('.');

        if (!shortenedHost[2].includes('kibon')) {
            return shortenedHost.slice(1, shortenedHost.length).join('.');
        }

        for (const mandantCandidate of mandantCandidates) {
            if (shortenedHost[0].includes(mandantCandidate)) {
                return shortenedHost.slice(1, shortenedHost.length).join('.');
            }
        }
        return shortenedHost.join('.');
    }

    private getEnvironmentFromCompleteHost(): string {
        const environmentRegex = /([a-z]*-(kibon))?(?=(-[a-z]{2})?\.(dvbern|kibon))/;
        const matches = this.windowRef.nativeWindow.location.host.match(environmentRegex);
        if (matches === null) {
            return '';
        }
        return matches[0];
    }
}
