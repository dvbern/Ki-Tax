import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSInternePendenz} from '../../../models/TSInternePendenz';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class InternePendenzenRS {

    public readonly serviceURL: string = `${CONSTANTS.REST_API}gesuch/internependenz`;
    private readonly ebeguRestUtil = new EbeguRestUtil();
    // we create an object with gesuchIds as keys and a Subject as values.
    // every time we update, delete, add a InternePendenz, we trigger
    // next() of the associated Subject.
    // this way, the subscriber knows, when to reload the internePendenzCount
    private internePendenzCountUpdates: any;

    public constructor(
        private readonly $http: HttpClient
    ) {
    }

    public getServiceName(): string {
        return 'InternePendenzenRS';
    }

    public createInternePendenz(internePendenz: TSInternePendenz): Observable<TSInternePendenz> {
        return this.$http.post(
            this.serviceURL,
            this.ebeguRestUtil.internePendenzToRestObject({}, internePendenz)
        ).pipe(tap(() => {
            this.setPendenzCountUpdated$(internePendenz.gesuch);
        })).pipe(map(pendenzFromServer => {
            return this.ebeguRestUtil.parseInternePendenz(new TSInternePendenz(), pendenzFromServer);
        }));
    }

    public updateInternePendenz(internePendenz: TSInternePendenz): Observable<TSInternePendenz> {
        return this.$http.put(
            this.serviceURL,
            this.ebeguRestUtil.internePendenzToRestObject({}, internePendenz)
        ).pipe(tap(() => {
            this.setPendenzCountUpdated$(internePendenz.gesuch);
        })).pipe(map(pendenzFromServer => {
            return this.ebeguRestUtil.parseInternePendenz(new TSInternePendenz(), pendenzFromServer);
        }));
    }

    public deleteInternePendenz(internePendenz: TSInternePendenz): Observable<void> {
        return this.$http.delete<void>(
            `${this.serviceURL}/${internePendenz.id}`,
        ).pipe(tap(() => {
            this.setPendenzCountUpdated$(internePendenz.gesuch);
        }));
    }

    public findInternePendenzenForGesuch(gesuch: TSGesuch): Observable<TSInternePendenz[]> {
        return this.$http.get<any[]>(
            `${this.serviceURL}/all/${gesuch.id}`
        ).pipe(map(pendenzenFromServer => {
            return pendenzenFromServer.map(pendenzFromServer => {
                return this.ebeguRestUtil.parseInternePendenz(new TSInternePendenz(), pendenzFromServer);
            });
        }));
    }

    public countInternePendenzenForGesuch(gesuch: TSGesuch): Observable<number> {
        return this.$http.get<number>(
            `${this.serviceURL}/count/${gesuch.id}`
        );
    }

    public getPendenzCountUpdated$(gesuch: TSGesuch): Observable<void> {
        if (!this.internePendenzCountUpdates || !this.internePendenzCountUpdates[gesuch.id]) {
            this.initInternePendenzCount(gesuch);
        }
        return this.internePendenzCountUpdates[gesuch.id];
    }

    private setPendenzCountUpdated$(gesuch: TSGesuch): void {
        if (!this.internePendenzCountUpdates || !this.internePendenzCountUpdates[gesuch.id]) {
            this.initInternePendenzCount(gesuch);
        }
        this.internePendenzCountUpdates[gesuch.id].next();
    }

    private initInternePendenzCount(gesuch: TSGesuch): void {
        if (!this.internePendenzCountUpdates) {
            this.internePendenzCountUpdates = {};
        }
        this.internePendenzCountUpdates[gesuch.id] = new ReplaySubject<void>(1);
        this.internePendenzCountUpdates[gesuch.id].next();
    }

}
