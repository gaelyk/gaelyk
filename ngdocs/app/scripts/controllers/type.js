'use strict';

angular.module('gaelykNgDocsApp')
  .controller('TypeCtrl', function ($scope,  $routeParams, packages) {
        $scope.pkg  = packages[$routeParams.package]
        if ($scope.pkg) {
            $scope.cls = $scope.pkg[$routeParams.type] || $scope.pkg[$routeParams.package + '.' + $routeParams.type]
        }
        $scope.name = $routeParams.type
    });
