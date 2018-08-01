/*
 * Copyright © 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

export interface LogLevelEntry {
    level: number;
}

export enum LogLevel {
    ERROR = 'ERROR',
    WARN = 'WARN',
    INFO = 'INFO',
    DEBUG = 'DEBUG',
    NONE = 'NONE',
}

export const LEVELS: {[k in LogLevel]: LogLevelEntry} = {
    ERROR: {level: 4},
    WARN: {level: 3},
    INFO: {level: 2},
    DEBUG: {level: 1},
    NONE: {level: 0},
};

// tslint:enable:no-object-literal-type-assertion

/**
 * key: Name vom Logger (typischerweise der Klassenname)
 */
export interface LogModules {
    [key: string]: LogLevel;
}
