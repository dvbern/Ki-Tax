export type User =
    | '[1-Superadmin] E-BEGU Superuser'
    // Admins & Sachbearbeiter
    | '[2-Admin-Kanton-Bern] Bernhard Röthlisberger'
    | '[2-SB-Kanton-Bern] Benno Röthlisberger'
    | '[3-Admin-Institution-Kita-Brünnen] Silvia Bergmann'
    | '[3-SB-Institution-Kita-Brünnen] Sophie Bergmann'
    | '[3-Admin-TS-Paris] Serge Gainsbourg'
    | '[3-SB-TS-Paris] Charlotte Gainsbourg'
    | '[3-Admin-Tägerschaft-Kitas-StadtBern] Bernhard Bern'
    | '[3-SB-Tägerschaft-Kitas-StadtBern] Agnes Krause'
    | '[4-Admin-Unterstützung-BernerSozialdienst] Patrick Melcher'
    | '[4-SB-Unterstützung-BernerSozialdienst] Max Palmer'
    // Gesuchsteller
    | '[5-GS] Emma Gerber'
    | '[5-GS] Heinrich Mueller'
    | '[5-GS] Michael Berger'
    | '[5-GS] Hans Zimmermann'
    | '[5-GS] Jean Chambre'
    // Gemeinde Admins & Sachbearbeiter
    | '[6-P-Admin-BG] Kurt Blaser'
    | '[6-L-Admin-BG] Kurt Schmid'
    | '[6-*-Admin-BG] Kurt Kälin'
    | '[6-P-SB-BG] Jörg Becker'
    | '[6-L-SB-BG] Jörg Keller'
    | '[6-*-SB-BG] Jörg Aebischer'
    | '[6-P-Admin-TS] Adrian Schuler'
    | '[6-L-Admin-TS] Adrian Huber'
    | '[6-*-Admin-TS] Adrian Bernasconi'
    | '[6-P-SB-TS] Julien Schuler'
    | '[6-L-SB-TS] Julien Odermatt'
    | '[6-*-SB-TS] Julien Bucheli'
    | '[6-P-Admin-Gemeinde] Gerlinde Hofstetter'
    | '[6-L-Admin-Gemeinde] Gerlinde Bader'
    | '[6-*-Admin-Gemeinde] Gerlinde Mayer'
    | '[6-P-SB-Gemeinde] Stefan Wirth'
    | '[6-L-SB-Gemeinde] Stefan Weibel'
    | '[6-*-SB-Gemeinde] Stefan Marti'
    | '[6-P-SB-Ferienbetreuung-Gemeinde] Marlene Stöckli'
    | '[6-L-SB-Ferienbetreuung-Gemeinde] Jordan Hefti'
    | '[6-*-SB-Ferienbetreuung-Gemeinde] Valentin Burgener'
    | '[6-P-Admin-Ferienbetreuung-Gemeinde] Sarah Riesen'
    | '[6-L-Admin-Ferienbetreuung-Gemeinde] Jean-Pierre Kraeuchi'
    | '[6-*-Admin-Ferienbetreuung-Gemeinde] Christoph Hütter'
    // Others
    | '[7-P-Steueramt] Rodolfo Geldmacher'
    | '[7-L-Steueramt] Rodolfo Iten'
    | '[7-*-Steueramt] Rodolfo Hermann'
    | '[7-P-Revisor] Reto Revisor'
    | '[7-L-Revisor] Reto Werlen'
    | '[7-*-Revisor] Reto Hug'
    | '[7-P-Jurist] Julia Jurist'
    | '[7-L-Jurist] Julia Adler'
    | '[7-*-Jurist] Julia Lory';

export const getUser = (user: User): User => {
    return user;
};
export const normalizeUser = (user: User) => {
    return /.*] (.*)/.exec(user)[1].split(' ').join('-');
};

export type OnlyValidSelectors<T> = T extends string
    ? T extends `${string}${'[data-test='}${string}`
        ? 'Please specify the value given to data-test="", getByData automatically wraps the value in [data-test="..."]'
        : T
    : never;
