import { FixtureEinkommensverschlechterung } from '@dv-e2e/fixtures';

const fillEinkommensverschlechterungForm = (dataset: keyof typeof FixtureEinkommensverschlechterung, jahr: 'jahr1' | 'jahr2', gesuchsteller: 'GS1' | 'GS2') => {
    FixtureEinkommensverschlechterung[dataset](({ [jahr]: { [gesuchsteller]: GS } }) => {
        cy.getByData('nettolohn').find('input').type(GS.nettolohn);
        cy.getByData('familienzulage').find('input').type(GS.familienzulage);
        cy.getByData('ersatzeinkommen').find('input').type(GS.ersatzeinkommen);
        cy.getByData('erhalteneAlimente').find('input').type(GS.erhalteneAlimente);
        cy.getByData('bruttoertraege-vermoegen').find('input').type(GS.bruttoertraegeVermoegen);
        cy.getByData('nettoertraege-erbengemeinschaften').find('input').type(GS.nettoertraegeErbengemeinschaften);
        cy.getByData('container.einkommen-in-vereinfachtem-verfahren-abgerechnet',
            `einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.${GS.einkommenInVereinfachtemVerfahrenAbgerechnet}`).click();
        cy.getByData('geleistete-alimente').find('input').type(GS.geleisteteAlimente);
        cy.getByData('abzug-schuldzinsen').find('input').type(GS.abzugSchuldzinsen);
        cy.getByData('gewinnungskosten').find('input').type(GS.gewinnungskosten);
    });
};

const fillResultateForm = (dataset: keyof typeof FixtureEinkommensverschlechterung, jahr: 'jahr1' | 'jahr2') => {
    FixtureEinkommensverschlechterung[dataset](({ [jahr]: { Resultate } }) => {
        cy.getByData('bruttovermoegen1').find('input').type(Resultate.bruttovermoegen1);
        cy.getByData('bruttovermoegen2').find('input').type(Resultate.bruttovermoegen2);
        cy.getByData('schulden1').find('input').type(Resultate.schulden1);
        cy.getByData('schulden2').find('input').type(Resultate.schulden2);
    })
}

const checkResultateForm = (dataset: keyof typeof FixtureEinkommensverschlechterung, jahr: 'jahr1' | 'jahr2') => {
    FixtureEinkommensverschlechterung[dataset](({ [jahr]: { Resultate } }) => {
        cy.getByData('einkommen-beider-gesuchsteller').find('input').should('have.value', Resultate.einkommenBeiderGesuchsteller);
        cy.getByData('einkommen-vorjahr-basis').find('input').should('have.value', Resultate.einkommenVorjahrBasis);
        cy.getByData('einkommen-vorjahr').find('input').should('have.value', Resultate.einkommenVorjahr);
    })
}

export const EinkommensverschlechterungPO = {
    checkResultateForm,
    fillEinkommensverschlechterungForm,
    fillResultateForm,
};
