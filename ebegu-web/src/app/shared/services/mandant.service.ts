import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject} from 'rxjs';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';

export enum KiBonMandant {
    BE = 'be',
    LU = 'lu',
    NONE = ''
}

@Injectable({
    providedIn: 'root',
})
export class MandantService {
    private _mandant$: BehaviorSubject<KiBonMandant> =
        new BehaviorSubject<KiBonMandant>(this.getMandantFromLocalStorageOrHostname());

    private _multimandantActive$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    private readonly LOCAL_STORE_KEY = 'mandant';

    public constructor(
        private windowRef: WindowRef,
        private readonly applicationPropertyService: ApplicationPropertyRS,
    ) {
        this.applicationPropertyService.getPublicPropertiesCached().then(properties => {
            this._multimandantActive$.next(properties.mulitmandantAktiv);
        });
    }

    public isMultimandantActive$(): Observable<boolean> {
        return this._multimandantActive$.asObservable();
    }

    private getMandantFromLocalStorageOrHostname(): KiBonMandant {
        const localStorageMandant = localStorage.getItem(this.LOCAL_STORE_KEY);
        return EbeguUtil.isNotNullOrUndefined(localStorageMandant) ?
            this.findMandant(localStorageMandant) :
            this.parseHostname();
    }

    public parseHostname(): KiBonMandant {
        const hostParts = window.location.hostname.split('.');
        return hostParts.length > 2 ? this.findMandant(hostParts[0]) : KiBonMandant.NONE;
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

        this.windowRef.nativeLocalStorage.setItem(this.LOCAL_STORE_KEY, parsedMandant);

        const host = this.removeMandantFromCompleteHost();

        if (parsedMandant !== KiBonMandant.NONE) {
            window.open(`${window.location.protocol}//${parsedMandant}.${host}/${url}`,
                '_self');
        }
    }

    private removeMandantFromCompleteHost(): string {
        const completeHost = window.location.host;
        const mandantCandidates = Object.values(KiBonMandant).filter(el => el.length > 0);

        let shortenedHost = window.location.host;
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
