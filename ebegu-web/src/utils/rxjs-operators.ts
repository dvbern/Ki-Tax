/*
 * Copyright © 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

import {Observable} from 'rxjs';
import {filter} from 'rxjs/operators';

export function isNotNullOrUndefined<T>(input: null | undefined | T): input is T {
    return input !== null && input !== undefined;
}

export function ignoreNullAndUndefined<T>(): (source$: Observable<T | null | undefined>) => Observable<T> {
    return (source$: Observable<null | undefined | T>) => source$.pipe(filter(isNotNullOrUndefined));
}
