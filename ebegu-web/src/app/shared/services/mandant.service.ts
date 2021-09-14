import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject} from 'rxjs';
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
    private readonly LOCAL_STORE_KEY = 'mandant';

    private readonly _mandant$: BehaviorSubject<KiBonMandant> =
        new BehaviorSubject<KiBonMandant>(this.parseHostname());

    private readonly _multimandantActive$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    public constructor(
        private readonly windowRef: WindowRef,
        private readonly applicationPropertyService: ApplicationPropertyRS,
    ) {
        this.applicationPropertyService.getPublicPropertiesCached().then(properties => {
            this._multimandantActive$.next(properties.mulitmandantAktiv);
        });
    }

    public isMultimandantActive$(): Observable<boolean> {
        return this._multimandantActive$.asObservable();
    }

    public parseHostname(): KiBonMandant {
        const hostParts = window.location.hostname.split('.');
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
