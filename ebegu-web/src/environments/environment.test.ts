import {LogLevel} from '../app/core/logging/log-level';
import {Environment} from './IEnvironment';

// eslint-disable-next-line
export const environment: Environment = {
    production: false,
    test: true,
    hmr: false,
    logLevel: LogLevel.WARN,
    logColorsEnabled: false,
};
