import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject, Subject} from 'rxjs';

export enum KiBonMandant {
  BE= 'be',
  LU = 'lu',
  NONE = ''
}

@Injectable({
  providedIn: 'root'
})
export class MandantService {
  private _mandant$: BehaviorSubject<KiBonMandant> = new BehaviorSubject<KiBonMandant>(this.parseHostname());

  public constructor() {
  }

  public parseHostname(): KiBonMandant {
    const hostParts = window.location.hostname.split('.');
    return hostParts.length > 2 ? this.findMandant(hostParts[0]) : KiBonMandant.NONE;
  }

  private findMandant(hostname: string): KiBonMandant {
    switch (hostname) {
      case 'be':
        return KiBonMandant.BE;
      case 'lu':
        return KiBonMandant.LU;
      default:
        return KiBonMandant.NONE;
    }
  }

  public get mandant$(): Observable<KiBonMandant> {
    return this._mandant$.asObservable();
  }

  public hasMandant() {
    return false;
  }
}
