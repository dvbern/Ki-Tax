import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('LastenausgleichTSService');

@Injectable({
    providedIn: 'root',
})
export class LastenausgleichTSService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}lats/gemeinde`;
    private readonly ebeguRestUtil = new EbeguRestUtil();
    // return last item but don't provide initial value like BehaviourSubject does
    private lATSAngabenGemeindeContainerStore =
        new ReplaySubject<TSLastenausgleichTagesschuleAngabenGemeindeContainer>(1);

    public constructor(private readonly http: HttpClient) {
    }

    public updateLATSAngabenGemeindeContainerStore(id: string): void {
        const url = `${this.API_BASE_URL}/find/${encodeURIComponent(id)}`;
        this.http.get<TSLastenausgleichTagesschuleAngabenGemeinde[]>(url)
            .subscribe(container => {
                this.next(container);
            }, error => LOG.error(error));
    }

    public getLATSAngabenGemeindeContainer(): Observable<TSLastenausgleichTagesschuleAngabenGemeindeContainer> {
        return this.lATSAngabenGemeindeContainerStore.asObservable();
    }

    public lATSAngabenGemeindeFuerInstitutionenFreigeben(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        this.http.put(
            `${this.API_BASE_URL}/freigebenInstitution`,
            this.ebeguRestUtil.lastenausgleichTagesschuleAngabenGemeindeContainerToRestObject({}, container)
        ).subscribe(result => {
            this.next(result);
        }, err => LOG.error(err));
    }

    public saveLATSAngabenGemeindeContainer(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        this.http.put(
            `${this.API_BASE_URL}/save`,
            this.ebeguRestUtil.lastenausgleichTagesschuleAngabenGemeindeContainerToRestObject({}, container)
        ).subscribe(result => {
            this.next(result);
        }, error => LOG.error(error));
    }

    public emptyStore(): void {
        this.lATSAngabenGemeindeContainerStore =
            new ReplaySubject<TSLastenausgleichTagesschuleAngabenGemeindeContainer>(1);
    }

    private next(result: any): void {
        const savedContainer = this.ebeguRestUtil.parseLastenausgleichTagesschuleAngabenGemeindeContainer(
            new TSLastenausgleichTagesschuleAngabenGemeindeContainer(),
            result
        );
        this.lATSAngabenGemeindeContainerStore.next(savedContainer);
    }
}
