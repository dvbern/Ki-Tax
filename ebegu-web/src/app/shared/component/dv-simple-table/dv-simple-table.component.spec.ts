import {ChangeDetectionStrategy, SimpleChange} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {SharedModule} from '../../shared.module';
import {DvSimpleTableColumnDefinition} from './dv-simple-table-column-definition';
import {DvSimpleTableConfig} from './dv-simple-table-config';

import {DvSimpleTableComponent} from './dv-simple-table.component';

describe('DvSimpleTableComponent', () => {
    let component: DvSimpleTableComponent;
    let fixture: ComponentFixture<DvSimpleTableComponent>;

    const data = [
        {a: 'a1', b: 'b1'},
        {a: 'a2', b: 'b2'},
        {a: 'a3', b: 'b3'},
        {a: 'a4', b: 'b4'},
        {a: 'a5', b: 'b5'},
        {a: 'a6', b: 'b6'},
    ];
    const columnDefinition: DvSimpleTableColumnDefinition[] = [
        {displayedName: 'A', attributeName: 'a'},
        {displayedName: 'B', attributeName: 'b'},
    ];

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SharedModule,
                BrowserAnimationsModule,
            ],
            declarations: [DvSimpleTableComponent]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .overrideComponent(DvSimpleTableComponent, {
                set: {changeDetection: ChangeDetectionStrategy.Default}
            })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DvSimpleTableComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display 6 rows', () => {
        component.columns = columnDefinition;
        component.data = data;
        component.ngOnChanges({
            data: new SimpleChange(null, data, false)
        });
        fixture.detectChanges();
        fixture.changeDetectorRef.markForCheck();
        const table = fixture.debugElement.query(By.css('mat-table'));
        expect(table).not.toBeNull();
        const rows = fixture.debugElement.queryAll(By.css('mat-row'));
        expect(rows.length).toEqual(6);
    });

    it('should display 5 rows on first page and 1 row on second page', () => {
        component.columns = columnDefinition;
        component.data = data;
        component.config = new DvSimpleTableConfig('a', 'asc', false, 5);
        component.ngOnChanges({
            data: new SimpleChange(null, data, false)
        });
        fixture.detectChanges();
        fixture.changeDetectorRef.markForCheck();
        let rows = fixture.debugElement.queryAll(By.css('mat-row'));
        expect(rows.length).toEqual(5);

        component.applyPaginator(1);
        fixture.detectChanges();
        fixture.changeDetectorRef.markForCheck();
        rows = fixture.debugElement.queryAll(By.css('mat-row'));
        expect(rows.length).toEqual(1);
    });
});
