import {Injectable} from '@angular/core';
import {CookieService} from 'ngx-cookie-service';
import {Observable, ReplaySubject} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {KiBonMandant, KiBonMandantFull} from '../../core/constants/MANDANTS';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';

@Injectable({
    providedIn: 'root',
})
export class MandantService {

    public get mandant$(): Observable<KiBonMandant> {
        return this._mandant$.asObservable();
    }

    private readonly _mandant$: ReplaySubject<KiBonMandant> = new ReplaySubject<KiBonMandant>(1);

    private readonly _multimandantActive$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    public constructor(
        private readonly windowRef: WindowRef,
        private readonly applicationPropertyService: ApplicationPropertyRS,
        private readonly authService: AuthServiceRS,
        private readonly cookieService: CookieService,
    ) {
        this.applicationPropertyService.getPublicPropertiesCached().then(properties => {
            this._multimandantActive$.next(properties.mulitmandantAktiv);
        });
        this._mandant$.next(MandantService.cookieToMandant(this.cookieService.get('mandant')));
    }

    private static hostnameToMandant(hostname: string): KiBonMandant {
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

    public isMultimandantActive$(): Observable<boolean> {
        return this._multimandantActive$.asObservable();
    }

    public parseHostnameForMandant(): KiBonMandant {
        const hostParts = this.windowRef.nativeWindow.location.hostname.split('.');
        for (const part of hostParts) {
            const potentialMandant = MandantService.hostnameToMandant(part);
            if (potentialMandant !== KiBonMandant.NONE) {
                return potentialMandant;
            }
        }
        return KiBonMandant.NONE;
    }

    public selectMandant(mandant: string, url: string): void {
        const parsedMandant = MandantService.hostnameToMandant(mandant);

        this.setMandantCookie(parsedMandant).then(() => {

            if (parsedMandant !== KiBonMandant.NONE) {
                this.redirectToMandantSubdomain(parsedMandant, url);
            }
        });
    }

    public setMandantCookie(mandant: KiBonMandant): Promise<any> {
        return this.authService.setMandant(MandantService.shortMandantToFull(mandant)) as Promise<any>;
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
