import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'dv-nav-bar',
  templateUrl: './dv-nav-bar.component.html',
  styleUrls: ['./dv-nav-bar.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DvNavBarComponent implements OnInit {

  public constructor() { }

  public ngOnInit(): void {
  }

}
