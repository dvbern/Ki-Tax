import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Injectable({
    providedIn: 'root',
})
export class LastenausgleichTSService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}lats/gemeinde`;
    private readonly ebeguRestUtil = new EbeguRestUtil();
    // return last item but don't provide initial value like BehaviourSubject does
    private readonly lATSAngabenGemeindeContainer =
        new ReplaySubject<TSLastenausgleichTagesschuleAngabenGemeindeContainer>(1);

    public constructor(private readonly http: HttpClient) {
    }

    public updateLATSAngabenGemeindeContainer(id: string): void {
        const url = `${this.API_BASE_URL}/find/${encodeURIComponent(id)}`;
        this.http.get<TSLastenausgleichTagesschuleAngabenGemeinde[]>(url)
            .pipe(map(container => this.ebeguRestUtil.parseLastenausgleichTagesschuleAngabenGemeindeContainer(
                new TSLastenausgleichTagesschuleAngabenGemeindeContainer(),
                container
            )))
            .subscribe(container => {
                this.next(container);
            });
    }

    public getLATSAngabenGemeindeContainer(): Observable<TSLastenausgleichTagesschuleAngabenGemeindeContainer> {
        return this.lATSAngabenGemeindeContainer.asObservable();
    }

    public lATSAngabenGemeindeFuerInstitutionenFreigeben(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        this.http.put(
            `${this.API_BASE_URL}/freigebenInstitution`,
            this.ebeguRestUtil.lastenausgleichTagesschuleAngabenGemeindeContainerToRestObject({}, container)
        ).subscribe(result => {
            this.next(result);
        });
    }

    public saveLATSAngabenGemeindeContainer(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        this.http.put(
            `${this.API_BASE_URL}/save`,
            this.ebeguRestUtil.lastenausgleichTagesschuleAngabenGemeindeContainerToRestObject({}, container)
        ).subscribe(result => {
            this.next(result);
        });
    }

    private next(result: any): void {
        const savedContainer = this.ebeguRestUtil.parseLastenausgleichTagesschuleAngabenGemeindeContainer(
            new TSLastenausgleichTagesschuleAngabenGemeindeContainer(),
            result
        );
        this.lATSAngabenGemeindeContainer.next(savedContainer);
    }
}
