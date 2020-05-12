#
# Copyright (C) 2019 - 2020 Rabobank Nederland
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

Feature: Non Personal Account

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyPair = defaultTestData.personalAccounts['default-pa1']
    * def rootLabelId = defaultTestData.defaultRootLabel.id;
    * configure headers = call read('classpath:headers.js') { token: #(keyPair.token)}


  Scenario: store a service account with valid name should return a 201
    * def result = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * match result.response == { name: 'sa 1', id: '#uuid', parentLabelId: '#uuid' }

  Scenario: store a service account without SERVICE_ACCOUNT_EDIT permission should return a 403 error
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    Given path '/api/serviceaccount'
    And request { name: 'sa 1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 403

  Scenario: store a service account without authorization should return a 401 error
    * configure headers = null
    Given path '/api/serviceaccount'
    And request { name: 'sa 1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 401

  Scenario: store a service account with a non existing parent label id should return a 403
    Given path '/api/serviceaccount'
    And request { name: 'label', parentLabelId: '940935f6-22bc-4d65-8c5b-a0599dedb510'}
    When method POST
    Then status 403
    And match response.message == 'Access denied'

  Scenario: store two service accounts with the same name should return a 400
    * call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    Given path '/api/serviceaccount'
    And request { name: 'sa 1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 400
    And match response.messages[0].message contains "service account with name: sa 1 and parentLabelId:"

  Scenario: retrieve service account should return a 200
    * def result = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'sa 1', id: '#(result.response.id)', parentLabelId: #(rootLabelId)}

  Scenario: retrieve service account without READ permission should return a 403 error
    * def result = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accounWithNoReadPermission = call read('classpath:feature/account/create-personal-account.feature') {name: 'unauthorized person',email: 'local.unauthorized@extra.nogo'}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["LAYOUT_ADD"]}
    * configure headers = call read('classpath:headers.js') { token: #(accounWithNoReadPermission.response.token)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 403

  Scenario: retrieve service account with implicit READ permission should return a 200 error
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["SERVICE_ACCOUNT_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def result = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(info.labelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'sa 1', id: '#(result.response.id)', parentLabelId: #(info.labelId)}

  Scenario: update a service account should return a 200
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def restPath = '/api/serviceaccount/'+accountId
    Given path restPath
    And request { name: 'sa 2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 200
    And match response == { name: 'sa 2', id: '#(accountId)', parentLabelId: #(rootLabelId)}

  Scenario: update a service account without SERVICE_ACCOUNT_EDIT permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def accountId = createResult.response.id
    * def restPath = '/api/serviceaccount/'+accountId
    Given path restPath
    And request { name: 'sa 2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 403

  Scenario: create a service account key should return a 200
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * def result = call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * match result.response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: create a service account key without SERVICE_ACCOUNT_EDIT permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    Given path '/api/serviceaccount/'+accountId+'/key'
    And request keyPair
    When method POST
    Then status 403

  Scenario: create a service account key without authorization should return a 401 error
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = null
    Given path '/api/serviceaccount/'+accountId+'/key'
    And request keyPair
    When method POST
    Then status 401

  Scenario: get a active service account key should return a 200
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active service account key with implicit read permission should return a 200

    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["SERVICE_ACCOUNT_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def result = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(info.labelId)}
    * def accountId = result.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+result.response.id+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active service account key without READ permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["LAYOUT_ADD"]}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    Given path restPath
    When method GET
    Then status 403

  Scenario: get a active service account key without authorization should return a 401 error
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    * configure headers = null
    Given path restPath
    When method GET
    Then status 401

  Scenario: get active key of authenticated sa should return a 200
    * def keypairResponse = call read('classpath:feature/account/create-service-account-with-key.feature') {accountName: 'sa1', parentLabelId: #(rootLabelId), keyFile: 'sa-keypair1'}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers =  call read('classpath:headers.js') { username: #(keyPair.keyId),password:#(keyPair.hashedKeyPassphrase)}
    Given path '/api/serviceaccount/me/activekey'
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get active key of authenticated sa with invalid credentials should return a 401
    * def keypairResponse = call read('classpath:feature/account/create-service-account-with-key.feature') {accountName: 'sa1', parentLabelId: #(rootLabelId), keyFile: 'sa-keypair1'}
    * def keyPair = keypairResponse.response
    * configure headers =  call read('classpath:headers.js') { username: fake,password:fake}
    Given path '/api/serviceaccount/me/activekey'
    When method GET
    Then status 401

  Scenario: get an active service account key after update should return a 200
    * def createResult = call read('create-service-account.feature') { name: 'sa 1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPathKey = '/api/serviceaccount/'+accountId+'/key'
    * def restPathUpdate = '/api/serviceaccount/'+ accountId
    Given path restPathUpdate
    And request { name: 'sa 2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 200
    Given path restPathKey
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

