import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'dv-list-sozialdienst',
  templateUrl: './list-sozialdienst.component.html',
  styleUrls: ['./list-sozialdienst.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ListSozialdienstComponent implements OnInit {

    public hiddenDVTableColumns = [
        'fallNummer',
        'familienName',
        'kinder',
        'aenderungsdatum',
        'dokumenteHochgeladen',
        'angebote',
        'institutionen',
        'verantwortlicheTS',
        'verantwortlicheBG',
    ];

  constructor() { }

  ngOnInit(): void {
  }

}
