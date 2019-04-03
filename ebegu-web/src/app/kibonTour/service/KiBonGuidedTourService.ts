/*
 * Copyright © 2019 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

import {ErrorHandler, Injectable} from '@angular/core';
import {GuidedTourService} from 'ngx-guided-tour';
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class KiBonGuidedTourService extends GuidedTourService {

    private readonly guidedTourSubject$ = new BehaviorSubject(false);
    private readonly _guidedTour$ = this.guidedTourSubject$.asObservable();

    private constructor(public errorHandler: ErrorHandler) {
        super(errorHandler);
    }

    public emit(): void {
        this.guidedTourSubject$.next(true);
    }

    public get guidedTour$(): Observable<boolean | null> {
        return this._guidedTour$;
    }

}
