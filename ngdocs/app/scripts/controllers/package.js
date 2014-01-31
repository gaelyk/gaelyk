'use strict';

angular.module('gaelykNgDocsApp')
  .controller('PackageCtrl', function ($scope,  $routeParams, packages) {
        $scope.pkg  = packages[ $routeParams.package]
        $scope.name = $routeParams.package
  });
