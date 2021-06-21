import {Injectable} from '@angular/core';

/**
 * Simple service to store states. This service should be used if a single instance wants to store the state of an
 * object.
 */
@Injectable({
    providedIn: 'root',
})
export class StateStoreService {

    private readonly _store: Map<string, object> = new Map<string, object>();

    public constructor() {
    }

    public set(key: string, state: object): void {
        this._store.set(key, state);
    }

    public setState(key: string, state: object): void {
        this.set(key, state);
    }

    public store(key: string, state: object): void {
        this.set(key, state);
    }

    public get(key: string): object {
        return this._store.get(key);
    }

    public getState(key: string): object {
        return this.get(key);
    }

    public delete(key: string): void {
        this._store.delete(key);
    }

    public deleteState(key: string): void {
        this.delete(key);
    }

    public resetStore(): void {
        this._store.clear();
    }

    public has(key: string): boolean {
        return this._store.has(key);
    }
}
