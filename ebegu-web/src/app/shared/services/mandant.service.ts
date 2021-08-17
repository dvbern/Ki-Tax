import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {EbeguUtil} from '../../../utils/EbeguUtil';
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

    private readonly LOCAL_STORE_KEY = 'mandant';

    public constructor(
        private windowRef: WindowRef,
    ) {
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

        this.windowRef.nativeLocalStorage.setItem('mandant', parsedMandant);

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

        mandantCandidates.forEach(mandantCandidate => {
            const idx = completeHost.indexOf(mandantCandidate);
            if (idx >= 0) {
                shortenedHost = shortenedHost.substring(idx + mandantCandidate.length + 1);
            }
        });

        return shortenedHost;
    }
}
