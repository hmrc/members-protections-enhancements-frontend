# Members Protections Enhancements

A look-up service that allows individuals to see all their protections online, and allow the PSA authenticated online access to view the protections for their members (individuals), therefore making the process fully self-serve and increasing the accuracy of information available to PSAs.


## Dependencies
| Service                          | Link                                                     |
|----------------------------------|----------------------------------------------------------|
| members-protections-enhancements | https://github.com/hmrc/members-protections-enhancements |
| auth                             | https://github.com/hmrc/auth                             |
| manage-pensions-frontend         | https://github.com/hmrc/manage-pensions-frontend         |


## Running the service

Service Manager: sm2 -start MPE_ALL

Port: 30029

Link: http://localhost:30029/members-protections-and-enhancements

Enrolment PSA: `HMRC-PODS-ORG` `PsaId` `A2100005` (local and Staging environments only)

Enrolment PSP: `HMRC-PODSPP-ORG` `PspId` `21000005` (local and Staging environments only)

## Running the Application Locally

Follow the instructions above for running the application, then:

```bash
sm2 --stop MEMBERS_PROTECTIONS_ENHANCEMENTS_FRONTEND
sbt run
```

## Running tests with scoverage
Use this command to run tests with scoverage. It also checks for any dependency updates.
sbt runTestsWithCoverage

## Licence

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).