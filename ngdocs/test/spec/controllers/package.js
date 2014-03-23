'use strict';

describe('Controller: PackageCtrl', function () {

  // load the controller's module
  beforeEach(module('gaelykNgDocsApp'));

  var PackageCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    PackageCtrl = $controller('PackageCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
