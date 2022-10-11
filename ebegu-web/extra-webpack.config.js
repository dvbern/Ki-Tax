module.exports = {
    module: {
        rules: [
            {
                test: /\.html$/i,
                exclude: /\.(component|template)\.html$/,
                loader: 'html-loader',
                options: {
                    //TODO: Remove once dv-userselect directive is angular 2+
                    minimize: false
                }
            }
        ]
    }
}
