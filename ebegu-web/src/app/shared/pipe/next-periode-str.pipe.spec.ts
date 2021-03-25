import * as moment from 'moment';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {NextPeriodeStrPipe} from './next-periode-str.pipe';

describe('NextPeriodeStrPipe', () => {
    it('create an instance', () => {
        const pipe = new NextPeriodeStrPipe();
        expect(pipe).toBeTruthy();
    });

    it('should return "2020/21"', () => {
        const periode = new TSGesuchsperiode();
        periode.gueltigkeit = new TSDateRange(moment('2019-01-01'), moment('2020-01-01'));
        const pipe = new NextPeriodeStrPipe();
        expect(pipe.transform(periode)).toBe('2020/21');
    });
});
