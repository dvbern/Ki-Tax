import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {EbeguUtil} from '../../../utils/EbeguUtil';

@Component({
  selector: 'dv-zpv-nr-success',
  templateUrl: './zpv-nr-success.component.html',
  styleUrls: ['./zpv-nr-success.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ZpvNrSuccessComponent implements OnInit {

  private isAuthenticated: boolean;

  public constructor(
      private readonly authService: AuthServiceRS
  ) { }

  public ngOnInit(): void {
    this.isAuthenticated = EbeguUtil.isNotNullOrUndefined(this.authService.getPrincipal());
  }

}
