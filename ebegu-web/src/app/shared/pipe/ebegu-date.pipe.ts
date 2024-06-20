import {Pipe, PipeTransform} from '@angular/core';
import * as moment from 'moment';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Pipe({
    name: 'ebeguDate'
})
export class EbeguDatePipe implements PipeTransform {
    public transform(date: moment.Moment): string {
        if (date?.isValid()) {
            return date.format(CONSTANTS.DATE_FORMAT);
        }
        return '';
    }
}
