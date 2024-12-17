# Members Protections Enhancements Frontend
This service is also known as *Members Protections and Enhancements*

A look-up service that allows individuals to see all their protections online, and allow the PSA authenticated online access to view the protections for their members (individuals), therefore making the process fully self-serve and increasing the accuracy of information available to PSAs.

## Dependencies
| Service                           | Link                                                      |
|-----------------------------------|-----------------------------------------------------------|
| members-protections-enhancements  | https://github.com/hmrc/members-protections-enhancements  |


### Endpoints used

| Service                              | HTTP Method | Route                                                 | Purpose                                                            |
|--------------------------------------|-------------|-------------------------------------------------------|--------------------------------------------------------------------|
| Members Protections and Enhancements | POST        | /mpe                                                  | Calls out backend to process the form                              |
| Manage Pension Schemes               | GET         | /manage-pension-schemes/you-need-to-register          | Redirects for users to register                                    |
| Manage Pension Schemes               | GET         | /manage-pension-schemes/administrator-or-practitioner | Redirects for users to register as a administrator or practitioner |

## Running the service

Service Manager: sm2 -start MPE_ALL

Port: 30029

Link: http://localhost:30029/members-protections-and-enhancements

Enrolment PSA: `HMRC-PODS-ORG` `PsaId` `A2100005` (local and Staging environments only)

Enrolment PSP: `HMRC-PODSPP-ORG` `PspId` `21000005` (local and Staging environments only)

## Tests and prototype
[View the prototype here](https://lpe-prototype-7cb785453062.herokuapp.com/)

| Repositories      | Link                                           |
|-------------------|------------------------------------------------|
| Journey tests     | https://github.com/hmrc/mpe-ui-journey-tests   |
| Performance tests | https://github.com/hmrc/mpe-performance-tests  |
| Prototype         | https://github.com/hmrc/lpe-prototype          |

## Confluence Page

https://confluence.tools.tax.service.gov.uk/display/LTPE/05.02.05.01+-+Frontend
