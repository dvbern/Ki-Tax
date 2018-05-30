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

import * as fs from 'fs';
import * as path from 'path';
import * as webpack from 'webpack';
import {ContextReplacementPlugin, NoEmitOnErrorsPlugin, ProvidePlugin} from 'webpack';
import {chunksSort, root} from './helpers';
import rules from './rules';
import CircularDependencyPlugin = require('circular-dependency-plugin');
import CleanWebpackPlugin = require('clean-webpack-plugin');
import CopyWebpackPlugin = require('copy-webpack-plugin');
import ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');
import HtmlWebpackPlugin = require('html-webpack-plugin');
import DefinePlugin = require('webpack/lib/DefinePlugin');
import CommonsChunkPlugin = webpack.optimize.CommonsChunkPlugin;

let contents = fs.readFileSync(__dirname + '/../pom.xml').toString();

let re = new RegExp('<artifactId>ebegu</artifactId>[\\s\\S]*?<version>(.*?)</version>[\\s\\S]*?<packaging>pom</packaging>', 'im');
let myMatchArray = re.exec(contents);
let parsedversion = (myMatchArray === null) ? 'unknown' : myMatchArray[1];
console.log('Parsed Version from pom is ' + parsedversion);

let currentTime = new Date();

const nodeModules = path.join(process.cwd(), 'node_modules');
const realNodeModules = fs.realpathSync(nodeModules);
const genDirNodeModules = path.join(process.cwd(), 'src', '$$_gendir', 'node_modules');
/**
 * Webpack Constants
 */
export const METADATA = {
    ENV: undefined,
    HMR: undefined,
    HOST: undefined,
    PORT: undefined,
    title: 'seed Webpack from DV Bern',
    baseUrl: '/',
    version: parsedversion,
    buildtstamp: currentTime.toISOString() || ''
};

export const vendorChunk = {
    name: 'vendor',
    minChunks: (module): boolean => {
        return module.resource
            && (module.resource.startsWith(nodeModules)
                || module.resource.startsWith(genDirNodeModules)
                || module.resource.startsWith(realNodeModules));
    },
    chunks: [
        'main',
    ],
};

export const mainChunk = {
    name: 'main',
    minChunks: 2,
    async: 'common',
};

/**
 * Webpack configuration
 *
 * See: http://webpack.github.io/docs/configuration.html#cli
 */
export default (env: string): webpack.Configuration => {

    return {
        entry: {
            'polyfills': root('src', 'polyfills.ts'),
            'vendor': root('src', 'vendor.ts'),
            'main': root('src', 'bootstrap.ts'),
        },
        output: {
            path: path.join(process.cwd(), 'dist'),
            filename: '[name].bundle.js',
            chunkFilename: '[chunkhash].chunk.js',
        },
        module: {
            rules: rules()
        },
        resolve: {
            extensions: ['.ts', '.js'],
        },

        plugins: [
            // clean dist before we start
            new CleanWebpackPlugin(['dist'], {
                root: process.cwd(),
            }),

            // skip the emitting phase whenever there are errors while compiling
            new NoEmitOnErrorsPlugin(),

            new ForkTsCheckerWebpackPlugin({
                checkSyntacticErrors: true,
                // tslint runs through tslint-loader
                tslint: true,
                tsconfig: root('src', 'tsconfig.json')
            }),

            // Detect modules with circular dependencies when bundling with webpack
            new CircularDependencyPlugin({
                exclude: /([\\\/])node_modules([\\\/])/,
                failOnError: false,
            }),

            // run TypeScript checker in a separate thread for build performance gain
            new ProvidePlugin({
                $: 'jquery',
                jQuery: 'jquery',
                'window.jQuery': 'jquery',
                'window.jquery': 'jquery',
                'moment': 'moment'
            }),

            // Moment: include only DE and FR locales
            new ContextReplacementPlugin(/moment[\\/]locale$/, /^\.\/(de)$/),

            // Loesung fuer eien Bug in angular -> https://github.com/angular/angular/issues/20357
            new ContextReplacementPlugin(
                // The (\\|\/) piece accounts for path separators in *nix and Windows

                // For Angular 5, see also https://github.com/angular/angular/issues/20357#issuecomment-343683491
                /\@angular(\\|\/)core(\\|\/)esm5/,
                root('src'), // location of your src
                {
                    // your Angular Async Route paths relative to this root directory
                }
            ),

            // Bundle webpack code into a prefixed chunk
            new CommonsChunkPlugin({
                name: 'inline',
            }),

            // Bundle vendor modules together
            new CommonsChunkPlugin(vendorChunk),

            // Bundle main chunk
            new CommonsChunkPlugin(mainChunk),

            // Plugin: CopyWebpackPlugin
            // Description: Copy files and directories in webpack.
            //
            // Copies project static assets.
            //
            // See: https://www.npmjs.com/package/copy-webpack-plugin
            new CopyWebpackPlugin([
                {from: 'src/assets', to: 'src/assets'},
            ]),

            // Plugin: HtmlWebpackPlugin
            // Description: Simplifies creation of HTML files to serve your webpack bundles.
            // This is especially useful for webpack bundles that include a hash in the filename
            // which changes every compilation.
            //
            // See: https://github.com/ampedandwired/html-webpack-plugin
            new HtmlWebpackPlugin({
                template: 'src/index.html',
                hash: false,
                inject: 'head',
                compile: true,
                favicon: false,
                minify: false,
                cache: true,
                showErrors: true,
                chunks: 'all',
                excludeChunks: [],
                title: 'Ki-Tax',
                xhtml: true,
                chunksSortMode: chunksSort,
            }),

            new DefinePlugin({
                'ENV': JSON.stringify(METADATA.ENV),
                'HMR': METADATA.HMR,
                'VERSION': JSON.stringify(METADATA.version),
                'BUILDTSTAMP': JSON.stringify(METADATA.buildtstamp),
                'process.env': {
                    'ENV': JSON.stringify(METADATA.ENV),
                    'NODE_ENV': JSON.stringify(METADATA.ENV),
                    'HMR': METADATA.HMR,
                    'VERSION': JSON.stringify(METADATA.version)
                }
            })

        ],

        // Include polyfills or mocks for various node stuff
        // Description: Node configuration
        //
        // See: https://webpack.github.io/docs/configuration.html#node
        node: {
            fs: 'empty',
            global: true,
            crypto: 'empty',
            tls: 'empty',
            net: 'empty',
            process: true,
            module: false,
            clearImmediate: false,
            setImmediate: false,
        },
    };
};
