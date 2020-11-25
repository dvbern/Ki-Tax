import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-lastenausgleich-ts-side-nav',
  templateUrl: './lastenausgleich-ts-side-nav.component.html',
  styleUrls: ['./lastenausgleich-ts-side-nav.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTsSideNavComponent implements OnInit {

    // TODO: replace with real values
    public readonly lastenausgleichTsId = '1234';
    public readonly status = 'Status';

    public constructor() { }

    public ngOnInit(): void {
  }

}
