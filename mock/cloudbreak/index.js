'use strict';

var app = require('connect')();
var http = require('http');
var swaggerTools = require('swagger-tools');
var jsyaml = require('js-yaml');
var fs = require('fs');
var serverPort = 8080;

// swaggerRouter configuration
var options = {
  swaggerUi: '/swagger.json',
  controllers: './controllers',
  useStubs: process.env.NODE_ENV === 'development' ? true : false // Conditionally turn on stubs (mock mode)
};

// The Swagger document (require it, build it programmatically, fetch it from a URL, ...)
var spec = fs.readFileSync('./api/swagger.yaml', 'utf8');
var swaggerDoc = jsyaml.safeLoad(spec);

global.requesttraces = [];
var responseModule=require('./responses/responses.js');
global.responses = responseModule.responses;

// Initialize the Swagger middleware
swaggerTools.initializeMiddleware(swaggerDoc, function (middleware) {
  // Interpret Swagger resources and attach metadata to request - must be first in swagger-tools middleware chain
  app.use(middleware.swaggerMetadata());

  // Validate Swagger requests
  // app.use(middleware.swaggerValidator());
  app.use(function custom(req, res, next) {
    if (req.swagger.apiPath === '/trace' ) {
      res.setHeader('Content-Type', 'application/json');
      res.end(JSON.stringify(requesttraces ));
    } else if (req.swagger.apiPath === '/setup') {
      responses[req.swagger.params.body.value.operationid] = {
        "responses":req.swagger.params.body.value.responses,
      }
      res.end();
    } else {
      requesttraces.push({"url": req.originalUrl, "params": req.swagger.params});
      var response;
      if (typeof responses[req.swagger.operation.operationId] !== 'undefined') {
        response = responses[req.swagger.operation.operationId].responses;
      }
      if (typeof response !== 'undefined') {
         response.every(function(element) {
           if (typeof element.condition === 'undefined' || element.condition === '') {
             res.statusCode=element.statuscode;
             res.end(JSON.stringify(element.response));
             return true;
           } else {
             var isthistheresponse = false;
             try {
               var f = new Function('params', element.condition);
               isthistheresponse = f(req.swagger.params);
             } catch (err) {
               console.log("Err: " + err);
             }
             if (isthistheresponse) {
               res.statusCode=element.statuscode;
               res.end(JSON.stringify(element.response));
             }
           }
         });
      } else {
         res.end();
      }
    }
    next();
  })

  // Route validated requests to appropriate controller
  //app.use(middleware.swaggerRouter(options));

  // Serve the Swagger documents and Swagger UI
  app.use(middleware.swaggerUi());

  // Start the server
  http.createServer(app).listen(serverPort, function () {
    console.log('Your server is listening on port %d (http://localhost:%d)', serverPort, serverPort);
    console.log('Swagger-ui is available on http://localhost:%d/docs', serverPort);
  });
});
