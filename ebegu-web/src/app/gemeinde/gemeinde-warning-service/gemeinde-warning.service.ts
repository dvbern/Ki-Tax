import {Injectable} from '@angular/core';
import {TSGemeindeKonfiguration} from '../../../models/TSGemeindeKonfiguration';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

/**
 * Dieser Service wird verwendet, um alle Gemeindekonfigurationen von aktiven Gesuchsperioden
 * vor dem Speichern zu überprüfen.
 * Dazu werden die Konfigurationen dieser Gesuchsperioden beim erstmaligen Laden als JSON Repräsentation
 * abgespeichert. Vor dem Speichern werden die möglicherweise veränderten Konfigurationen damit verglichen und
 * falls sich etwas geändert hat, wird eine Warnung gezeigt.
 */

@Injectable({
    providedIn: 'root'
})
export class GemeindeWarningService {

    private readonly dangerousKonfigurationenStr: {gesuchsperiode: TSGesuchsperiode, json: string}[] = [];
    private readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    public constructor() {
    }

    public init(konfigurationen: TSGemeindeKonfiguration[]): void {
        for (const k of konfigurationen) {
            if (k.gesuchsperiode.isEntwurf()) {
                continue;
            }
            this.dangerousKonfigurationenStr.push({
                gesuchsperiode: k.gesuchsperiode,
                json: this.prepareJsonCompareString(k)
            });
        }
    }

    public showWarning(konfiguration: TSGemeindeKonfiguration[]): boolean {
        for (const k of konfiguration) {
            for (const dangerousKonfig of this.dangerousKonfigurationenStr) {
                if (k.gesuchsperiode !== dangerousKonfig.gesuchsperiode) {
                    continue;
                }
                const identical = dangerousKonfig.json === this.prepareJsonCompareString(k);
                if (!identical) {
                    return true;
                }
            }
        }
        return false;
    }

    private prepareJsonCompareString(konfiguration: TSGemeindeKonfiguration): string {
        // wir müessen die Konfiguration zuerst zum Rest Object konvertieren, damit die Konfigurationen verglichen werden können
        const konfigurationRestObj = this.ebeguRestUtil.gemeindeKonfigurationToRestObject({}, konfiguration);
        // json Representation wird verwendet, damit die Objekte deep verglichen werden können
        return JSON.stringify(
            konfigurationRestObj.konfigurationen.map(k => {
                return {
                    key: k.key,
                    value: k.value
                };
            })
        );
    }
}
