import {FixtureEinkommensverschlechterung} from '@dv-e2e/fixtures';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
	return cy.getByData('page-title');
};
const getNettoLohn = () => {
    return cy.getByData('nettolohn');
};

const getFamilienzulagen = () => {
    return cy.getByData('familienzulage');
};

const getErsatzeinkommen = () => {
    return cy.getByData('ersatzeinkommen');
};

const getErhalteneAlimente = () => {
    return cy.getByData('erhaltene-alimente');
};

const getBruttoertraegeVermoegen = () => {
    return cy.getByData('brutto-ertraege-vermoegen');
};

const getNettoertraegeErbengemeinschaft = () => {
    return cy.getByData('netto-ertraege-erbengemeinschaften');
};

const getEinkommenInVereinfachtemVerfahren = (answer: string) => {
    return cy.getByData('einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.' + answer);
};

const getAbzugSchuldzinsen = () => {
    return cy.getByData('abzug-schuldzinsen');
};

const getGewinnungskosten = () => {
    return cy.getByData('gewinnungskosten');
};

const getGeleisteteAlimente = () => {
    return cy.getByData('geleistete-alimente');
};




// !! -- PAGE ACTIONS -- !!
const fillEinkommensverschlechterungForm = (
    dataset: keyof typeof FixtureEinkommensverschlechterung,
    jahr: 'jahr1' | 'jahr2',
    gesuchsteller: 'GS1' | 'GS2',
) => {
    FixtureEinkommensverschlechterung[dataset](({[jahr]: {[gesuchsteller]: GS}}) => {
        getNettoLohn().find('input').type(GS.nettolohn);
        getFamilienzulagen().find('input').type(GS.familienzulage);
        getErsatzeinkommen().find('input').type(GS.ersatzeinkommen);
        getErhalteneAlimente().find('input').type(GS.erhalteneAlimente);
        getBruttoertraegeVermoegen().find('input').type(GS.bruttoertraegeVermoegen);
        getNettoertraegeErbengemeinschaft().find('input').type(GS.nettoertraegeErbengemeinschaften);
        getEinkommenInVereinfachtemVerfahren(GS.einkommenInVereinfachtemVerfahrenAbgerechnet).click();
        getGeleisteteAlimente().find('input').type(GS.geleisteteAlimente);
        getAbzugSchuldzinsen().find('input').type(GS.abzugSchuldzinsen);
        getGewinnungskosten().find('input').type(GS.gewinnungskosten);
    });
};


export const EinkommensverschlechterungPO = {
    // page objects
    getPageTitle,
    getNettoLohn,
    getFamilienzulagen,
    getErsatzeinkommen,
    getErhalteneAlimente,
    getBruttoertraegeVermoegen,
    getNettoertraegeErbengemeinschaft,
    getEinkommenInVereinfachtemVerfahren,
    getGeleisteteAlimente,
    getAbzugSchuldzinsen,
    getGewinnungskosten,
    // page actions
    fillEinkommensverschlechterungForm,
};
