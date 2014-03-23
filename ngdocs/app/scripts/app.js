'use strict';

angular.module('gaelykNgDocsApp', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
   'ui.bootstrap',
   'gaelykNgDocsApp.packages'
])
  .config(function ($routeProvider, packagesProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/front.html',
        // controller: 'MainCtrl'
      })
        .when('/docs/:package/:type', {
            templateUrl: 'views/type.html',
            controller: 'TypeCtrl',
            resolve: {
                packages: packagesProvider.requirePackages
            }
        })
        .when('/docs/:package/', {
            templateUrl: 'views/package.html',
            controller: 'PackageCtrl',
            resolve: {
                packages: packagesProvider.requirePackages
            }
        })
      .otherwise({
        redirectTo: '/'
      });
  });
