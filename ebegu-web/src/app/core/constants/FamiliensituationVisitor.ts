import {KiBonMandant} from './MANDANTS';
import {MandantVisitor} from './MandantVisitor';

export class FamiliensituationVisitor implements MandantVisitor<any> {
    public process(mandant: KiBonMandant): any {
        return mandant.accept(this);
    }

    public visitAppenzellAusserrhoden(): any {
        return 'gesuch.familiensituation-appenzell';
    }

    public visitBern(): any {
        return 'gesuch.familiensituation-default';
    }

    public visitLuzern(): any {
        return 'gesuch.familiensituation-default';
    }

    public visitSolothurn(): any {
        return 'gesuch.familiensituation-default';
    }

    public visitSchwyz(): any {
        return 'gesuch.familiensituation-schwyz';
    }
}
