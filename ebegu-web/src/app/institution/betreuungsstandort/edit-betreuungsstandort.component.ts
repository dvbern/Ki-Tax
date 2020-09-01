import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {TSBetreuungsstandort} from '../../../models/TSBetreuungsstandort';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import { CONSTANTS } from '../../core/constants/CONSTANTS';

@Component({
    selector: 'dv-edit-betreuungsstandort',
    templateUrl: './edit-betreuungsstandort.component.html',
    styleUrls: ['./edit-betreuungsstandort.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditBetreuungsstandortComponent implements OnInit {

    @Input()
    public stammdaten: TSInstitutionStammdaten;

    public readonly CONSTANTS: any = CONSTANTS;

    public constructor() {
    }

    public ngOnInit(): void {
    }

    public addStandort(): void {
        const newStandort = new TSBetreuungsstandort();
        this.stammdaten.institutionStammdatenBetreuungsgutscheine.betreuungsstandorte.push(newStandort);
    }

    public removeAllStandorte(): void {
        this.stammdaten.institutionStammdatenBetreuungsgutscheine.betreuungsstandorte = [];
    }

    /**
     * Für den Moment sind maximal zwei Standorte möglich. Der erste Standort wird jeweils aus den Stammdaten
     * übernommen.
     */
    public toggleMehrereBetreuungsstandorte(): void {
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.betreuungsstandorte.length === 0) {
            this.addStandort();
            return;
        }
        this.removeAllStandorte();
    }

}
