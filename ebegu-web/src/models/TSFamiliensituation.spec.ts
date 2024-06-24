import * as moment from 'moment';
import {TSFamilienstatus} from './enums/TSFamilienstatus';
import {TSGesuchsperiodeStatus} from './enums/TSGesuchsperiodeStatus';
import {TSFamiliensituation} from './TSFamiliensituation';
import {TSGesuchsperiode} from './TSGesuchsperiode';
import {TSDateRange} from './types/TSDateRange';

describe('Familiensituation', () => {
    describe('spezialfall konkubinat ohne kind wird X jährig während der Periode', () => {
        const periodeStart: moment.Moment = moment('2023-08-01');
        const periodeEnd: moment.Moment = moment('2024-07-31');
        const periode = new TSGesuchsperiode(
            TSGesuchsperiodeStatus.AKTIV,
            new TSDateRange(periodeStart, periodeEnd)
        );

        it('should not be spezialfall if stichtag is before periode', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus =
                TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            familiensituation.startKonkubinat = moment('2023-07-31');
            expect(
                familiensituation.konkuinatOhneKindBecomesXYearsDuringPeriode(
                    periode
                )
            ).toBeFalsy();
        });

        it('should not be spezialfall if stichtag is after periode', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus =
                TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            familiensituation.startKonkubinat = moment('2024-08-01');
            expect(
                familiensituation.konkuinatOhneKindBecomesXYearsDuringPeriode(
                    periode
                )
            ).toBeFalsy();
        });

        it('should be spezialfall if stichtag is in periode', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus =
                TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            familiensituation.startKonkubinat = moment('2023-10-01');
            expect(
                familiensituation.konkuinatOhneKindBecomesXYearsDuringPeriode(
                    periode
                )
            ).toBeTruthy();
        });

        it('should be spezialfall if stichtag is start of periode', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus =
                TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            familiensituation.startKonkubinat = moment(periodeStart);
            expect(
                familiensituation.konkuinatOhneKindBecomesXYearsDuringPeriode(
                    periode
                )
            ).toBeTruthy();
        });

        it('should be spezialfall if stichtag is end of periode', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus =
                TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            familiensituation.startKonkubinat = moment(periodeEnd);
            expect(
                familiensituation.konkuinatOhneKindBecomesXYearsDuringPeriode(
                    periode
                )
            ).toBeTruthy();
        });
    });
    describe('spezialfall konkubinat ohne kind wird nicht X jährig während Periode (kurzes Konkubinat)', () => {
        const periodeStart: moment.Moment = moment('2023-08-01');
        const periodeEnd: moment.Moment = moment('2024-07-31');
        const periode = new TSGesuchsperiode(
            TSGesuchsperiodeStatus.AKTIV,
            new TSDateRange(periodeStart, periodeEnd)
        );

        it('should return false if not Konkubinat kein Kind', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus = TSFamilienstatus.KONKUBINAT;
            expect(
                familiensituation.isShortKonkubinatForEntirePeriode(periode)
            ).toBeFalsy();
        });

        it('should return false if Konkubinat reacheas min Dauer in Periode', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus =
                TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            familiensituation.minDauerKonkubinat = 2;
            familiensituation.startKonkubinat = moment('2021-08-01');
            expect(
                familiensituation.isShortKonkubinatForEntirePeriode(periode)
            ).toBeFalsy();
        });

        it('should return false if Konkubinat is older than min Dauer for enire', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus =
                TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            familiensituation.minDauerKonkubinat = 2;
            familiensituation.startKonkubinat = moment('2021-31-07');
            expect(
                familiensituation.isShortKonkubinatForEntirePeriode(periode)
            ).toBeFalsy();
        });

        it('should return true if Konkubinat is younger than min Dauer for enire', () => {
            const familiensituation = new TSFamiliensituation();
            familiensituation.familienstatus =
                TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            familiensituation.minDauerKonkubinat = 2;
            familiensituation.startKonkubinat = moment('2022-08-01');
            expect(
                familiensituation.isShortKonkubinatForEntirePeriode(periode)
            ).toBeTruthy();
        });
    });
});
