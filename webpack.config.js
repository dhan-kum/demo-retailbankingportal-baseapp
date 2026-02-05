var path = require('path');

module.exports = {
    entry: './src/main/js/app.js',
     devtool: 'source-map',
    cache: true,
    mode: 'development',
    output: {
        path: __dirname,
        filename: './src/main/resources/static/built/bundle.js'
    },
    module: {
        rules: [
            {
                test: path.join(__dirname, '.'),
                exclude: /(node_modules)/,
                use: [{
                    loader: 'babel-loader',
                    options: {
                        presets: ["@babel/preset-env", "@babel/preset-react"]
                    }
                }]
            },
            {
                test: /\.css$/,
                use: ["style-loader", "css-loader"]
            },
            {
                test: /\.(jpe?g|png|gif|svg)$/i,
                use: [
                  {
                    loader: 'file-loader',
                    options: {
                      query: {
                        name:'assets/[name].[ext]'
                      }
                    }
                  }
                 ]
              },
                { 
                test: /\.(png|woff|woff2|eot|ttf|svg)$/,
                loader: 'url-loader',
                
                options: {
    				limit: 100000,
  				},
                }
        ]
    }
};