/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {Directive, ElementRef, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {debounceTime, merge, throttleTime} from 'rxjs/operators';
import {Subject} from 'rxjs/Subject';
import {Subscription} from 'rxjs/Subscription';


/**
 * This directive solves the problem of double-click: the user clicks more than once and the action gets executed several times, which is not wanted.
 * There are two alternatives when working with the button as an Observable:
 * debounce -> we register every event (click) and wait a "debounceTime", after this time we execute the action just once, with the last registered event
 *             The problem with this approach is that it takes a "debounceTime" for the action to execute so the user sees a delay between click and
 *             action-execution
 * throttle -> we get the first event (click) and execute it, then we wait a "throttleTime" until we start listening again. The benefit here is that
 *             the action gets executed directly, since the delay happens afterwards. (This is the chosen option)
 *
 * //https://coryrylan.com/blog/creating-a-custom-debounce-click-directive-in-angular
 */
@Directive({
    selector: '[dvNgDebounceClick]'
})
export class DvNgDebounceClickDirective implements OnInit, OnDestroy {

    @Input() delay = 1000;
    @Output() debounceClick = new EventEmitter();

    private clicks = new Subject();
    private subscription: Subscription;

    constructor(private domElement: ElementRef) {
    }

    ngOnInit() {
        // we throttle and debounce the click. With throttle we get the first click at the beginning of the delay and with debounce the last one
        // at the end of the delay. this way we are able to disable the button and execute the action at the beginning and to enable the button
        // wihtout executing the action at the end of delay
        //
        // -------------------------------delay-----------------------------------delay---
        // ---click-----click-----click--------------------------click---------------
        // ---action/disable--------------enable-----------------action/disable---enable---

        this.subscription = this.clicks.pipe(
            throttleTime(this.delay),
            merge(this.clicks.pipe(
                debounceTime(this.delay)
            )),
        ).subscribe(e => {
                if (this.isDomElementEnabled()) {
                    this.debounceClick.emit(e);
                }
                this.handleDomElement();
            },
            (err) => {
                console.log('Error. dv-debounce-button-click', err);
            }
        );

        // todo It should wait for an HTTPResponse to come and then enable the button again
    }

    /**
     * It enables the domElement when it is disabled and disables it when it is enabled
     */
    private handleDomElement() {
        this.domElement.nativeElement.disabled = !this.domElement.nativeElement.disabled;
    }

    private isDomElementEnabled() {
        return !this.domElement.nativeElement.disabled;
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    @HostListener('click', ['$event'])
    clickEvent(event: Event) {
        event.preventDefault();
        event.stopPropagation();
        this.clicks.next(event);
    }

}
