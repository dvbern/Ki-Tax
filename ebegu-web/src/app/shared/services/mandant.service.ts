import {Injectable} from '@angular/core';
import {CookieService} from 'ngx-cookie-service';
import {Observable, ReplaySubject} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {KiBonMandant} from '../../core/constants/MANDANTS';

@Injectable({
    providedIn: 'root',
})
export class MandantService {

    private readonly _mandant$: ReplaySubject<KiBonMandant> = new ReplaySubject<KiBonMandant>(1);

    private readonly _multimandantActive$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    public constructor(
        private readonly windowRef: WindowRef,
        private readonly applicationPropertyService: ApplicationPropertyRS,
        private readonly authService: AuthServiceRS,
        private readonly cookieService: CookieService
    ) {
        this.applicationPropertyService.getPublicPropertiesCached().then(properties => {
            this._multimandantActive$.next(properties.mulitmandantAktiv);
        });
        this._mandant$.next(this.findMandant(this.cookieService.get('mandant')));
    }

    public isMultimandantActive$(): Observable<boolean> {
        return this._multimandantActive$.asObservable();
    }

    public parseHostname(): KiBonMandant {
        const hostParts = this.windowRef.nativeWindow.location.hostname.split('.');
        for (const part of hostParts) {
            const potentialMandant = this.findMandant(part);
            if (potentialMandant !== KiBonMandant.NONE) {
                return potentialMandant;
            }
        }
        return KiBonMandant.NONE;
    }

    private findMandant(hostname: string): KiBonMandant {
        switch (hostname.toLocaleLowerCase()) {
            case 'be':
                return KiBonMandant.BE;
            case 'lu':
                return KiBonMandant.LU;
            default:
                return KiBonMandant.NONE;
        }
    }

    public get mandant$(): Observable<KiBonMandant> {
        return this._mandant$.asObservable();
    }

    public selectMandant(mandant: string, url: string): void {
        const parsedMandant = this.findMandant(mandant);

        this.authService.setMandant(parsedMandant).then(() => {

            if (parsedMandant !== KiBonMandant.NONE) {
                this.redirectToMandantSubdomain(parsedMandant, url);
            }
        });
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
