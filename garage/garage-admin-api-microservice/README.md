# Garage API microservice

## Intro

This service is the exposed API facade for the Garage Project, it takes the clients requests and forwards that to the core, it also takes the core answers and sends it back to the client.

## Facade details

{endPoints=[

{[/api/clients/gate/{pRegistration_id}],methods=[DELETE],produces=[application/json]}, 

{[/api/clients/find/{pRegistration_id}],methods=[GET],produces=[application/json]}, 

{[/api/management/status],methods=[GET],produces=[application/json]}, 

{[/api/admin/build/garage],methods=[POST],consumes=[application/json]}, 

{[/api/admin/build/garage],methods=[DELETE]}, 

{[/api/admin/build/level],methods=[POST],consumes=[application/json]}, 

{[/api/admin/build/level/{level_id}],methods=[PUT],consumes=[application/json]}, 

{[/api/admin/build/level],methods=[DELETE]}, 

{[/api/]}, 

{[/api/clients/gate],methods=[POST],consumes=[application/json],produces=[application/json]}, 

{[/endpoints],methods=[GET]}, 

{[/error],produces=[text/html]}, 

{[/error]}

]}

Full Swagger Documentation can be visible at this URL when deployed : http://localhost:8765/garage/swagger-ui.html#/

## Example calls

Please see SOAP UI test to see example calls

For example, the test "ModifyExtendLevel" gives the following status : 

![ModifyExtendLevel](../tests/ModifyExtendLevel.png)

If we move the check one rank up, it fails expecting no free lot while 1 is available : 

![ModifyExtendLevel2](../tests/ModifyExtendLevel2.png)

If a level is deactivated but cars are still present, their lot will be counted in the total lot number until they exit, then the previously occupied lot will not be counted in the total number of lots anymore.

![DesactivateThenAddLevel](../tests/DesactivateThenAddLevel.png)

## Security

As for this demo project, the /api/admin/** endpoints are secured with Basic http authentication over http. For a production grade product, it should be at least over https or best using OAuth2.

Example calls:

- curl admin:password@localhost:8765/garage/api/admin/status

Answers: 

{"nbLevels":3,"nbTotalLots":9,"nbOccupiedLots":0,"nbFreeLots":9,"levels":[{"id":0,"nbOccupiedLots":0,"nbFreeLots":3,"level":{"inUse":true,"nbLevelLots":3},"vehicles":[]},{"id":1,"nbOccupiedLots":0,"nbFreeLots":2,"level":{"inUse":true,"nbLevelLots":2},"vehicles":[]},{"id":2,"nbOccupiedLots":0,"nbFreeLots":4,"level":{"inUse":true,"nbLevelLots":4},"vehicles":[]}]}

- curl admin:choucroute@localhost:8765/garage/api/admin/status

{"timestamp":1458584084945,"status":401,"error":"Unauthorized","message":"Bad credentials","path":"/api/admin/status"}