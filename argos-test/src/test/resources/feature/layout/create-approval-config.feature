@ignore
Feature: create a valid approval config

  Background:
    * url karate.properties['server.baseurl']
    * def layoutPath = '/api/supplychain/'+ __arg.supplyChainId + '/layout'
    * def layoutToBeSigned = read(__arg.json)
    * def keyNumber = __arg.keyNumber
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyPair = defaultTestData.personalAccounts['default-pa'+keyNumber]


  Scenario: create ApprovalConfiguration should return a 201
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-request.json')
    When method POST
    Then status 201
    And match response == read('classpath:testmessages/layout/approval-config-response.json')
    Then match header Location contains layoutPath+'/approvalconfig/'+response.approvalConfigurationId