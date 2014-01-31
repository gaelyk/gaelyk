'use strict';

angular.module('gaelykNgDocsApp')
  .controller('MainCtrl', function ($scope, $http, $log) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];

    $http.get('/resources/shortcuts.json').then(function(packages){
       $scope.packages = packages.data;
    });
  });
