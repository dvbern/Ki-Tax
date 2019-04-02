/*
 * Copyright © 2019 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {LogFactory} from '../core/logging/LogFactory';

const LOG = LogFactory.createLog('KiBonGuidedTourService');

@Injectable({
    providedIn: 'root'
})
export class KiBonGuidedTourService {

    private readonly guidedTourSubject$ = new BehaviorSubject(false);
    private readonly _guidedTour$ = this.guidedTourSubject$.asObservable();

    private constructor() {
        // hei
    }

    // private emit$(): Observable<void> {
    //     const id = this.guidedTourSubject$.getValue().id;
    //     const next: Validity =
    //         new Validity(this.invalidLohnausweisViaArbeitnehmer, this.invalidArbeitnehmer, id);
    //
    //     return this.dataStoreService.validity.update$(next)
    //         .pipe(map(() => this.guidedTourSubject$.next(next)));
    // }

    public emit(): void {
        this.guidedTourSubject$.next(true);
    }

    public get guidedTour$(): Observable<boolean | null> {
        return this._guidedTour$;
    }

}
