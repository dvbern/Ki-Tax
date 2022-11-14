import {HttpContextToken, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class HttpPendingService {

    private readonly set = new Set<number>();
    private readonly REQUEST_CONTEXT_ID = new HttpContextToken<string>(() => null);
    private maxID = 0;

    public constructor() {
    }

    public pending(request: HttpRequest<unknown>): void {
        const id = this.maxID++;
        request.context.set(this.REQUEST_CONTEXT_ID, id.toString());
        this.set.add(id);
    }

    public resolve(request: HttpRequest<unknown>): void {
        const id = +request.context.get(this.REQUEST_CONTEXT_ID);
        if (this.set.has(id)) {
            this.set.delete(id);
        }
    }

    public hasPendingRequests(): boolean {
        return this.set.size > 0;
    }
}
