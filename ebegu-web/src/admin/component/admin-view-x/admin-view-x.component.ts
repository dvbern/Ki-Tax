import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ViewChild,
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Observable} from 'rxjs';
import {DvNgRemoveDialogComponent} from '../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSApplicationProperty} from '../../../models/TSApplicationProperty';
import {AbstractAdminViewX} from '../../abstractAdminViewX';
import {ReindexRS} from '../../service/reindexRS.rest';

@Component({
  selector: 'dv-admin-view-x',
  templateUrl: './admin-view-x.component.html',
  styleUrls: ['./admin-view-x.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminViewXComponent extends AbstractAdminViewX {

    @ViewChild(NgForm) public form: NgForm;

    @ViewChild(MatSort, {static: true}) public sort: MatSort;

    public applicationProperty: TSApplicationProperty;
    public displayedCollection: MatTableDataSource<TSApplicationProperty>;
    public displayedColumns: string[] =  ['name', 'value', 'timestampErstellt'];
    public filterColumns: string[] =  ['filter'];

    public constructor(
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly reindexRS: ReindexRS,
        private readonly dvDialog: MatDialog,
        private readonly cd: ChangeDetectorRef,
        authServiceRS: AuthServiceRS,
    ) {
        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.resetForm();
    }

    public submit(): void {
        if (this.form.invalid) {
            return;
        }
        // Bei STADT_BERN_ASIV_CONFIGURED eine Sicherheitsabfrage machen
        // tslint:disable-next-line:early-exit
        if (this.applicationProperty.name === 'STADT_BERN_ASIV_CONFIGURED'
            && this.applicationProperty.value === 'true') {
            this.dvDialog.open(DvNgRemoveDialogComponent, {data : {
                    title: 'CREATE_MASSENMUTATION_BERN_DIALOG_TITLE',
                    text: 'CREATE_MASSENMUTATION_BERN_DIALOG_TEXT',
            }}).afterClosed().subscribe(() => {
                this.doSave();
            });
        } else {
            this.doSave();
        }
    }

    private doSave(): void {
        // testen ob aktuelles property schon gespeichert ist
        if (this.applicationProperty.isNew()) {
            this.applicationPropertyRS.update(this.applicationProperty.name, this.applicationProperty.value);
        } else {
            this.applicationPropertyRS.create(this.applicationProperty.name, this.applicationProperty.value);
        }
        this.applicationProperty = undefined;
    }

    public editRow(row: TSApplicationProperty): void {
        this.applicationProperty = row;
    }

    public resetForm(): void {
        this.applicationProperty = undefined;
        this.applicationPropertyRS.getAllApplicationProperties().then(response => {
            this.displayedCollection = new MatTableDataSource<TSApplicationProperty>(response);
            this.displayedCollection.sort = this.sort;
            this.cd.markForCheck();
        });
    }

    public startReindex(): Observable<any> {
        return this.reindexRS.reindex();
    }

    public doFilter(value: string): void {
        this.displayedCollection.filter = value;
    }
}
