import { Injectable } from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {TSMessageEvent} from '../../../models/enums/TSErrorEvent';
import {TSHTTPEvent} from '../events/TSHTTPEvent';
import {TSVersionCheckEvent} from '../events/TSVersionCheckEvent';

// Extend this type if you need more events
declare type BroadcastMessageType = TSHTTPEvent | TSVersionCheckEvent | TSMessageEvent;

interface BroadcastMessage<T extends BroadcastMessageType> {
  type: T;
  payload: any;
}

/**
 * Replacement for angular rootScope.broadcast
 */
@Injectable({
  providedIn: 'root'
})
export class BroadcastService {

  private readonly broadcaster = new Subject<BroadcastMessage<BroadcastMessageType>>();

  public constructor() { }

  public broadcast(type: BroadcastMessageType, payload?: any): void {
    this.broadcaster.next({type, payload});
  }

  public on$<T extends BroadcastMessageType>(type: T): Observable<BroadcastMessage<BroadcastMessageType>> {
    return this.broadcaster.asObservable()
        .pipe(
            filter(message => message.type === type)
        );
  }
}
