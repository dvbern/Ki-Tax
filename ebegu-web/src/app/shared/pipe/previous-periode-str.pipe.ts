import {Pipe, PipeTransform} from '@angular/core';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';

/**
 * returns next gesuchsperiode as string representation
 * e.g. for periode 2019/20 it returns '2020/21'
 */
@Pipe({
    name: 'previousPeriodeStr'
})
export class PreviousPeriodeStrPipe implements PipeTransform {

    public transform(periode: TSGesuchsperiode): string {
        if (!periode || !periode.gueltigkeit) {
            return '';
        }
        const firstYear = periode.gueltigkeit.gueltigAb.year() - 1;
        const secondYear = firstYear + 1;
        return `${firstYear.toString()}/${secondYear.toString().substr(2)}`;
    }

}
