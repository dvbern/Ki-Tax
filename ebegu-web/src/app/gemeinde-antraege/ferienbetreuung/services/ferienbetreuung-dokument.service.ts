import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSSprache} from '../../../../models/enums/TSSprache';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungDokument} from '../../../../models/gemeindeantrag/TSFerienbetreuungDokument';
import {EbeguRestUtil} from '../../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';

@Injectable({
    providedIn: 'root'
})
export class FerienbetreuungDokumentService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}ferienbetreuung/dokument`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        private readonly http: HttpClient,
    ) {
    }

    public getAllDokumente(containerId: string): Observable<TSFerienbetreuungDokument[]> {
        return this.http.get(`${this.API_BASE_URL}/all/${encodeURIComponent(containerId)}`)
            .pipe(map(restDokumente => {
                return this.ebeguRestUtil.parseFerienbetreuungDokumente(restDokumente);
        }));
    }

    public deleteDokument(dokumentId: string): Observable<void> {
        return this.http.delete<void>(`${this.API_BASE_URL}/${encodeURIComponent(dokumentId)}`);
    }

    public generateVerfuegung(
        antrag: TSFerienbetreuungAngabenContainer,
        sprache: TSSprache,
    ): Observable<any> {
        return this.http.post(
            `${this.API_BASE_URL}/docx-erstellen/${encodeURIComponent(antrag.id)}/${sprache}`,
            {responseType: 'blob'}
        );
    }
}
