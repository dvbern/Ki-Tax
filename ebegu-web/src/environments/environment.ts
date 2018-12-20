import {LogLevel} from '../app/core/logging/log-level';
import {Environment} from './IEnvironment';

// tslint:disable-next-line:naming-convention
export const environment: Environment = {
    production: false,
    test: false,
    hmr: false,
    logLevel: LogLevel.INFO,
    logModules: {},
    sentryDSN: 'https://fd0b368a6cee4b879a0ed06e66444c17@sentry.dvbern.ch/11', //todo important homa reviewer remove
};
