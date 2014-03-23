'use strict';

angular.module('gaelykNgDocsApp.packages', [])
  .provider('packages', function () {

    // Private variables
    var pkgs = null;

    // Public API for configuration
    this.requirePackages = function (packages) {
        return packages.load()
    };

    // Method for instantiating
    this.$get = function($q, $http){
        return {load: function () {
        var deferred = $q.defer()
        if (pkgs != null)  {
            deferred.resolve(pkgs)
            return deferred.promise
        }
        $http.get('/resources/shortcuts.json').then(function(response){
            deferred.resolve(pkgs = response.data)
        })
      return deferred.promise
    }}}
  });
