'use strict';

describe('Controller: TypeCtrl', function () {

  // load the controller's module
  beforeEach(module('gaelykNgDocsApp'));

  var TypeCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    TypeCtrl = $controller('TypeCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
