import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnDestroy,
    OnInit,
    ViewChild,
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {TSGesuchstellerKardinalitaet} from '../../../../models/enums/TSGesuchstellerKardinalitaet';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {KinderabzugExchangeService} from '../service/kinderabzug-exchange.service';

const LOG = LogFactory.createLog('SchwyzKinderabzugComponent');

@Component({
    selector: 'dv-schwyz-kinderabzug',
    templateUrl: './schwyz-kinderabzug.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchwyzKinderabzugComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild(NgForm)
    public readonly form: NgForm;

    @Input()
    public kindContainer: TSKindContainer;

    private readonly unsubscribe$: Subject<void> = new Subject<void>();

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly cd: ChangeDetectorRef,
        private readonly exchangeService: KinderabzugExchangeService,
    ) {
    }

    public ngOnInit(): void {
        this.exchangeService.getFormValidationTriggered$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(() => {
                this.cd.markForCheck();
            }, err => LOG.error(err));

        this.exchangeService.getFamilienErgaenzendeBetreuungChanged$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(() => {
                this.change();
                this.cd.markForCheck();
            }, err => LOG.error(err));
    }

    public ngAfterViewInit(): void {
        this.exchangeService.addForm(this.form);
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    public wirdKindExternBetreut(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.kindContainer?.kindJA.familienErgaenzendeBetreuung);
    }

    public change(): void {
        this.deleteValuesOfHiddenQuestions();
    }

    private deleteValuesOfHiddenQuestions(): void {
        if (this.kindContainer?.kindJA) {
            if (!this.wirdKindExternBetreut()) {
                this.kindContainer.kindJA.unterhaltspflichtig = undefined;
            }
            if (!this.lebtKindImHaushaltVisible()) {
                this.kindContainer.kindJA.lebtKindAlternierend = undefined;
            }
            if (!this.partnerUnterhaltspflichtigVisible()) {
                this.kindContainer.kindJA.gemeinsamesGesuch = undefined;
            }
        }
    }

    public lebtKindImHaushaltVisible(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.kindContainer?.kindJA.unterhaltspflichtig);
    }

    public partnerUnterhaltspflichtigVisible(): boolean {
        return this.gesuchModelManager.getFamiliensituation().gesuchstellerKardinalitaet === TSGesuchstellerKardinalitaet.ZU_ZWEIT
            && EbeguUtil.isNotNullAndTrue(this.kindContainer?.kindJA.lebtKindAlternierend);
    }
}
