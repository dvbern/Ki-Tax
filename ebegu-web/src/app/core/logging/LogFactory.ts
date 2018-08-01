import {detect} from 'detect-browser';
import {randomColor} from 'randomcolor';
import {environment} from '../../../environments/environment';
import {LEVELS, LogLevel, LogModules} from './log-level';

const browser = detect();

export interface FormattingOptions {
    moduleNameMinWidth: number;
}

enum ColorSupport {
    /**
     * No color support at all
     */
    NONE,
    /**
     * Styling of message affects the whole row
     */
    ROW_STYLE,
    /**
     * Multiple styles for the message
     */
    MULTIPLE
}

function findColorSupport(colorsEnabled: boolean): ColorSupport {
    if (!colorsEnabled) {
        return ColorSupport.NONE;
    }

    switch (browser && browser.name) {
        case 'chrome':
        case 'firefox':
        case 'safari':
            return ColorSupport.MULTIPLE;
        case 'edge':
            return ColorSupport.ROW_STYLE;
        default:
            return ColorSupport.NONE;
    }
}

type LogFunction = (message: any, params?: any[]) => void;

/**
 * Primarily compatible to console.log() and friends.
 * For future use: possibly implement this using (e.g.) Sentry
 */
class LogFunctions {
    constructor(
        public readonly error: LogFunction,
        public readonly warn: LogFunction,
        public readonly info: LogFunction,
        public readonly debug: LogFunction
    ) {
    }
}

class DefaultLogFunctions extends LogFunctions {
    constructor() {
        super(console.error, console.warn, console.info, console.debug);
    }
}

function formatModuleName(moduleName: string, options: FormattingOptions): string {
    let aligned = moduleName;
    const minWidth = options.moduleNameMinWidth;
    if (minWidth > 0) {
        const diff = minWidth - moduleName.length;
        for (let i = 0; i < diff; i++) {
            aligned += ' ';
        }
    }

    return aligned;
}

function findLogFunc(logFunctions: LogFunctions, level: LogLevel): LogFunction {
    switch (level) {
        case LogLevel.ERROR:
            return logFunctions.error;
        case LogLevel.WARN:
            return logFunctions.warn;
        case LogLevel.INFO:
            return logFunctions.info;
        case LogLevel.DEBUG:
            return logFunctions.debug;
        default:
            return () => {
                /*nop*/
            };
    }
}

/**
 * See:
 * {@link https://developers.google.com/web/tools/chrome-devtools/console/console-write#styling_console_output_with_css}
 */
function formatColored(args: any[], formattedModuleName: string, colorSupport: ColorSupport,
                       backgroundColor: string): any[] {
    if (colorSupport === ColorSupport.NONE) {
        return [formattedModuleName, ...args];
    }

    if (args.length && typeof args[0] === 'string' && colorSupport === ColorSupport.MULTIPLE) {
        return [
            `%c${formattedModuleName} %c${args[0]}`,
            `background-color: ${backgroundColor}; color: white; border: 1px solid ${backgroundColor};`,
            `border: 1px solid ${backgroundColor};`,
            ...args.slice(1)
        ];
    } else {
        return [
            `%c${formattedModuleName}`,
            `background-color: ${backgroundColor}; color: white; border: 1px solid ${backgroundColor};`,
            ...args
        ];
    }
}

/**
 * Try very hard to get errors displayed... but do not crash the application (i.e.: treat IE/Edge well)
 */
function logFuncFallback(loggingError: any, params: any[]): void {
    // tslint:disable:no-console
    try {
        console.log('Fallback log', loggingError, ...params);
    } catch (ignoredError) {
        // well... duh
        alert(ignoredError);
    }
    // tslint:enable:no-console
}

/**
 * Method that handles the actual logging.
 * Separate type definition for decoupling only.
 */
type LogHandler = (level: LogLevel, args: any[], moduleName: string, backgroundColor: string) => void;

/**
 * The classic logger interface...
 */
export class Log {
    constructor(
        private readonly logHandler: LogHandler,
        public readonly name: string,
        public readonly backgroundColor: string = randomColor({seed: name, format: 'rgb'})
    ) {
        // nop
    }

    public error(...args: any[]): void {
        this.logHandler(LogLevel.ERROR, args, this.name, this.backgroundColor);
    }

    public warn(...args: any[]): void {
        this.logHandler(LogLevel.WARN, args, this.name, this.backgroundColor);
    }

    public info(...args: any[]): void {
        this.logHandler(LogLevel.INFO, args, this.name, this.backgroundColor);
    }

    public debug(...args: any[]): void {
        this.logHandler(LogLevel.DEBUG, args, this.name, this.backgroundColor);
    }

}

/**
 * Usage: const LOG = LogFactory.createLog(MyObjectClass);
 */
export class LogFactory {
    public static logLevel: LogLevel = environment.logLevel;
    public static logModules: LogModules = environment.logModules || {};
    // noinspection PointlessBooleanExpressionJS
    public static logSupportsColor: boolean = environment.logColorsEnabled !== false;

    public static logFunctions: LogFunctions = new DefaultLogFunctions();
    public static formattingOptions: FormattingOptions = {
        moduleNameMinWidth: 30
    };

    public static createLog(name: string): Log {
        return new Log(LogFactory.log, name);
    }


    /**
     * Erlaubt es, den Log-Level eines Moduls zur Laufzeit zu setzen, nuetzlich fuers Debugging.
     */
    public static setModuleLevel(moduleName: string, level?: LogLevel): void {
        LogFactory.logModules[moduleName] = level || LogLevel.NONE;
    }

    /**
     * Pure convenience: set module level to DEBUG.
     */
    public static debugModule(moduleName: string): void {
        this.setModuleLevel(moduleName, LogLevel.DEBUG);
    }

    /**
     * Check if logging is enabled for a given log level.
     */
    public static isEnabled(moduleName: string, level: LogLevel): boolean {
        const moduleLevel = LogFactory.logModules[moduleName] || LogFactory.logLevel;
        const enabled = LEVELS[level].level >= LEVELS[moduleLevel].level;

        return enabled;
    }

    /**
     * Actually log a message to the console
     */
    public static log(level: LogLevel, args: any[], moduleName: string, backgroundColor: string): void {
        if (!LogFactory.isEnabled(moduleName, level)) {
            return;
        }

        const formattedModuleName = formatModuleName(moduleName, LogFactory.formattingOptions);
        const colorSupport = findColorSupport(LogFactory.logSupportsColor);
        const params = formatColored(args, formattedModuleName, colorSupport, backgroundColor);

        const logFunc = findLogFunc(LogFactory.logFunctions, level);
        try {
            logFunc(params[0], ... params.slice(1));
        } catch (loggingError) {
            // happens primarily on IE if the developer console is closed
            logFuncFallback(loggingError, params);
        }
    }
}
