import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';

@Injectable({
    providedIn: 'root'
})
export class FerienbetreuungDokumentService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}ferienbetreuung/dokument`;

    public constructor(
        private readonly http: HttpClient
    ) {
    }

    public deleteDokument(dokumentId: string): Observable<void> {
        return this.http.delete<void>(`${this.API_BASE_URL}/${encodeURIComponent(dokumentId)}`);
    }
}
