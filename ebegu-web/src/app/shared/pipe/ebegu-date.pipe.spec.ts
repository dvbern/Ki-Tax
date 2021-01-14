import * as moment from 'moment';
import {EbeguDatePipe} from './ebegu-date.pipe';

describe('EbeguDatePipe', () => {
    it('create an instance', () => {
        const pipe = new EbeguDatePipe();
        expect(pipe).toBeTruthy();
    });
    it('should transform moment date into string of format "DD.MM.YYYY"', () => {
        const pipe = new EbeguDatePipe();
        const date = moment('2020-01-02');
        expect(pipe.transform(date)).toBe('02.01.2020');
    });
});
