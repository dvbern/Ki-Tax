import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
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
    private readonly lATSAngabenGemeindeContainer =
        new BehaviorSubject<TSLastenausgleichTagesschuleAngabenGemeindeContainer>(undefined);

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

    public saveLATSAngabenGemeindeContainer(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        this.http.put(
            `${this.API_BASE_URL}/save`,
            this.ebeguRestUtil.lastenausgleichTagesschuleAngabenGemeindeContainerToRestObject({}, container)
        ).subscribe(result => {
            const savedContainer = this.ebeguRestUtil.parseLastenausgleichTagesschuleAngabenGemeindeContainer(
                new TSLastenausgleichTagesschuleAngabenGemeindeContainer(),
                result
            );
            this.next(savedContainer);
        });
    }

    private next(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        this.lATSAngabenGemeindeContainer.next(container);
    }
}
