import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSLastenausgleichTagesschuleAngabenInstitution} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Injectable({
    providedIn: 'root',
})
export class TagesschuleAngabenRS {

    private ebeguRestUtils = new EbeguRestUtil();

    private apiUrl = `${CONSTANTS.REST_API}gemeindeantrag`;

    public constructor(private http: HttpClient) {
    }

    public getAllVisibleTagesschulenAngabenForTSLastenausgleich(lastenausgleichId: string): Observable<TSLastenausgleichTagesschuleAngabenInstitutionContainer[]> {
        return this.http.get<TSLastenausgleichTagesschuleAngabenInstitution[]>(`${this.apiUrl}/${lastenausgleichId}/tagesschulenantraege`)
            .pipe(
                map(lastenausgleichAngabenList => this.ebeguRestUtils.parseLastenausgleichTagesschuleAngabenInstitutionContainerList(lastenausgleichAngabenList))
            );
    }
}
