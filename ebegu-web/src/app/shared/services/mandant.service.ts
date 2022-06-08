import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {CookieService} from 'ngx-cookie-service';
import {Observable, ReplaySubject} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSMandant} from '../../../models/TSMandant';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {KiBonMandant, KiBonMandantFull} from '../../core/constants/MANDANTS';
import {LogFactory} from '../../core/logging/LogFactory';
import {WindowRef} from '../../core/service/windowRef.service';

const LOG = LogFactory.createLog('MandantService');

@Injectable({
    providedIn: 'root',
})
export class MandantService {

    private readonly _mandant$: ReplaySubject<KiBonMandant> = new ReplaySubject<KiBonMandant>(1);

    private readonly _multimandantActive$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    private readonly restUtil = new EbeguRestUtil();

    public get mandant$(): Observable<KiBonMandant> {
        return this._mandant$.asObservable();
    }

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

    private static hostnameToMandant(hostname: string): KiBonMandant {
        switch (hostname.toLocaleLowerCase()) {
            case KiBonMandant.BE:
                return KiBonMandant.BE;
            case KiBonMandant.LU:
                return KiBonMandant.LU;
            case KiBonMandant.SO:
                return KiBonMandant.SO;
            default:
                return KiBonMandant.NONE;
        }
    }

    private static shortMandantToFull(short: KiBonMandant): KiBonMandantFull {
        switch (short) {
            case KiBonMandant.BE:
                return KiBonMandantFull.BE;
            case KiBonMandant.LU:
                return KiBonMandantFull.LU;
            case KiBonMandant.SO:
                return KiBonMandantFull.SO;
            default:
                return KiBonMandantFull.NONE;
        }
    }

    private static cookieToMandant(cookieMandant: string): KiBonMandant {
        switch (cookieMandant) {
            case KiBonMandantFull.BE:
                return KiBonMandant.BE;
            case KiBonMandantFull.SO:
                return KiBonMandant.SO;
            case KiBonMandantFull.LU:
                return KiBonMandant.LU;
            default:
                return KiBonMandant.NONE;
        }
    }

    private static decodeMandantCookie(encodedMandant: string): string {
        return encodedMandant.replace('+', ' ');
    }

    private static isLegacyBeCookie(decodeMandantCookie: string): boolean {
        return decodeMandantCookie === 'be';
    }

    public async initMandantCookies(): Promise<void> {
        await this.initMandantCookie();
        await this.initMultimandantActivated();
        await this.initMandantRedirectCookie();
    }

    private async initMandantCookie(): Promise<void> {
        if (MandantService.isLegacyBeCookie(this.getDecodeMandantCookie())) {
            await this.setMandantCookie(KiBonMandant.BE);
        }
        const mandantFromCookie = MandantService.cookieToMandant(this.getDecodeMandantCookie());
        const mandantFromUrl = this.parseHostnameForMandant();

        if (mandantFromCookie !== mandantFromUrl && mandantFromUrl !== KiBonMandant.NONE) {
            await this.setMandantCookie(mandantFromUrl);
            this._mandant$.next(mandantFromUrl);
        } else {
            this._mandant$.next(mandantFromCookie);
        }
    }

    public async initMandantRedirectCookie(): Promise<void> {
        const mandantFromCookie = MandantService.cookieToMandant(this.getDecodedMandantRedirectCookie());
        const mandantFromUrl = this.parseHostnameForMandant();

        if (mandantFromCookie !== mandantFromUrl && mandantFromUrl !== KiBonMandant.NONE) {
            await this.setMandantRedirectCookie(mandantFromUrl);
        }
    }

    private getDecodeMandantCookie(): string {
        return MandantService.decodeMandantCookie(this.cookieService.get('mandant'));
    }

    private getDecodedMandantRedirectCookie(): string {
        return MandantService.decodeMandantCookie(this.cookieService.get('mandantRedirect'));
    }

    private initMultimandantActivated(): Promise<void> {
        return this.http.get(`${CONSTANTS.REST_API}application-properties/public/all`).toPromise().then(res => {
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
        const regex = /(be|so|stadtluzern)(?=.(dvbern|kibon))/g;
        const matches = regex.exec(this.windowRef.nativeWindow.location.hostname);
        if (matches === null) {
            return KiBonMandant.NONE;
        }
        return MandantService.hostnameToMandant(matches[0]);
    }

    public selectMandant(mandant: string, url: string): void {
        const parsedMandant = MandantService.hostnameToMandant(mandant);

        if (parsedMandant !== KiBonMandant.NONE) {
            this.redirectToMandantSubdomain(parsedMandant, url);
        }
    }

    public setMandantCookie(mandant: KiBonMandant): Promise<any> {
        // TODO: Restore AuthService once migrated
        return this.http.post(CONSTANTS.REST_API + 'auth/set-mandant',
            {name: MandantService.shortMandantToFull(mandant)}).toPromise() as Promise<any>;
    }

    public setMandantRedirectCookie(mandant: KiBonMandant): Promise<any> {
        // TODO: Restore AuthService once migrated
        return this.http.post(CONSTANTS.REST_API + 'auth/set-mandant-redirect',
            {name: MandantService.shortMandantToFull(mandant)}).toPromise() as Promise<any>;
    }

    public redirectToMandantSubdomain(mandant: KiBonMandant, url: string): void {
        const host = this.removeMandantEnvironmentFromCompleteHost();
        const environment = this.getEnvironmentFromCompleteHost();
        const environmentWithMandant = environment.length > 0 ? `${environment}-${mandant}` : mandant;
        this.windowRef.nativeWindow.open(`${this.windowRef.nativeWindow.location.protocol}//${environmentWithMandant}.${host}/${url}`,
            '_self');
    }

    public removeMandantEnvironmentFromCompleteHost(): string {
        const splitHost = this.windowRef.nativeWindow.location.host.split('.');

        if (splitHost[0] === 'kibon') {
            return splitHost.join('.');
        }

        return splitHost.slice(1, splitHost.length).join('.');
    }

    public getEnvironmentFromCompleteHost(): string {
        const environmentRegex = /([a-z]*-(kibon))?(?=(-(be|so|stadtluzern))?\.(dvbern|kibon))/;
        const matches = this.windowRef.nativeWindow.location.host.match(environmentRegex);
        if (matches === null) {
            return '';
        }
        return matches[0];
    }

    public getMandantRedirect(): KiBonMandant {
        return MandantService.cookieToMandant(this.getDecodedMandantRedirectCookie());
    }

    public getMandantLoginState(mandant: KiBonMandant): string {
        switch (mandant) {
            case KiBonMandant.BE:
            case KiBonMandant.NONE:
            case KiBonMandant.SO:
            case KiBonMandant.LU:
                return 'authentication.login';
            default:
                return 'authentication.locallogin';
        }
    }

    public mandantToKibonMandant(mandant: TSMandant): KiBonMandant {
        switch (mandant.mandantIdentifier) {
            case 'SOLOTHURN':
                return KiBonMandant.SO;
            case 'LUZERN':
                return KiBonMandant.LU;
            case 'BERN':
            default:
                return KiBonMandant.BE;
        }
    }

    public getAll(): Observable<TSMandant[]> {
        return this.http.get<any[]>(`${CONSTANTS.REST_API}mandanten/all`)
            .pipe(
                map(results => results.map(restMandant => this.restUtil.parseMandant(new TSMandant(), restMandant))),
            );
    }

    public getMandantLogoName(mandant: KiBonMandant): string {
        switch (mandant) {
            case KiBonMandant.BE:
                return 'logo-kibon-bern.svg';
            case KiBonMandant.LU:
                return 'logo-kibon-luzern.svg';
            case KiBonMandant.SO:
                return 'logo-kibon-solothurn.svg';
            default:
                return 'logo-kibon-bern.svg';
        }
    }
}
