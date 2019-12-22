# assembly-voting
Cooperative Assembly Voting API - Using Spring Boot and MongoDB

### Tech

Assembly Voting uses a number of open source projects to work properly:

* [Java] - Uses Java 8 language features.
* [Lombok] - Uses Project Lombok to reduce boilerplate code.
* [Swagger] - Publish Rest Contract for standalone API using Swagger UI resources.
* [Spring Boot] - Run standalone application with Spring Boot.
* [Spring Data] - Maps Mongo DB Documents schema with Spring Data.
* [HttpClient] - Uses Apache HttpClient to request third part API.
* [Mockito] - Uses Mockito to make better unit tests.
* [Hamcrest] - Uses Hamcrest to make tests more smarter.

### Installation
Use gradle to build and install all packages. So use the command bellow to do this:

```sh
$ gradle clean build
```

> Is required to install Mongo DB locally 
> or change the project connection properties 
> to connect to some one else Mongo DB server.

### API Guide

This Project needs to be used for specific meeting agendas separately.

#### Create Agenda

First we need to crate an agenda for meeting.

Calling:

```
[POST] - 'http://localhost:8080/cooperative/assembly/voting/agenda'
(Header) - "Content-Type": "application/json"
```

Sending Content Payload in json format:

```json
{
    "title": "Mudança de Estatuto"
}
```

Request response returns saved id in UUID format:

```json
{
    "data": {
        "id": "60d03a76-5728-4ce3-bf89-4cbe3d1a67ac",
        "title": "Mudança de Estatuto"
    }
}
```

Or an error response in case of ValidationException:

```json
{
    "errors": [
        {
            "code": "ERR0100",
            "detail": "voting.agenda.title.not.empty",
            "title": "Incorrect request format",
            "source": {
                "pointer": "title",
                "parameter": ""
            }
        }
    ]
}
```

#### Create Voting Session

Then we need to open the voting session for the created agenda.

Calling:

```
[POST] - 'http://localhost:8080/cooperative/assembly/voting/session'
(Header) - "Content-Type": "application/json"
```

Sending Content Payload in json format, informing:
- "agendaId" in UUID format for created agenda;
- "deadlineMinutes" to measure since openingTime (now) to closingTime;

```json
{
	"agendaId": "60d03a76-5728-4ce3-bf89-4cbe3d1a67ac",
	"deadlineMinutes": 30
}
```

Request Response returns saved id in UUID format, and time period for voting:

```json
{
    "data": {
        "id": "91745471-b4f9-42f2-8dea-b6b685b5d302",
        "agenda": {
            "id": "60d03a76-5728-4ce3-bf89-4cbe3d1a67ac",
            "title": "Mudança de Estatuto"
        },
        "openingTime": "2019-12-21T18:50:29.157",
        "closingTime": "2019-12-21T19:20:29.157"
    }
}
```

#### Register Vote

Finally we can vote, as user, on created agenda during the voting session.

Calling:

```
[POST] - 'http://localhost:8080/cooperative/assembly/vote'
(Header) - "Content-Type": "application/json"
```

Sending Content Payload in json format, informing:
- "userId" in CPF format (with or without mask);
- "agendaId" in UUID format;
- "choice" that is "YES|NO" (enum);

```json
{
	"userId": "344.472.510-86",
	"agendaId": "60d03a76-5728-4ce3-bf89-4cbe3d1a67ac",
	"choice": "YES"
}
```

Request Response returns saved id in UUID format, and time period for voting:

```json
{
    "data": {
        "id": "25340a4a-c6bd-4a64-ad04-a98d4b967390",
        "userId": "00988896052",
        "session": {
            "id": "91745471-b4f9-42f2-8dea-b6b685b5d302",
            "agenda": {
                "id": "60d03a76-5728-4ce3-bf89-4cbe3d1a67ac",
                "title": "Mudança de Estatuto"
            },
            "openingTime": "2019-12-21T18:50:29.157",
            "closingTime": "2019-12-21T19:20:29.157"
        },
        "choice": "YES"
    }
}
```

#### Counting Vote

For sumarize, we can get counting votes on agenda after the voting session.

Calling:

```
[GET] - 'http://localhost:8080/cooperative/assembly/vote/counting?agendaId=46f821fc-3d81-4d39-ac52-7a0a02eba734'
(Header) - "Content-Type": "application/json"
```

Request Response returns counted votes and period for voting on agenda:

```json
{
    "data": {
        "agenda": "Mudança de Estatuto",
        "openingTime": "2019-12-21T23:25:18.405",
        "closingTime": "2019-12-21T23:55:18.405",
        "totalVotes": 1,
        "affirmativeVotes": 1,
        "negativeVotes": 0
    }
}
```

[Java]: <https://www.oracle.com/technetwork/pt/java/javase/downloads/jdk8-downloads-2133151.html>
[Spring Boot]: <https://start.spring.io/>
[Swagger]: <https://springfox.github.io/springfox/docs/current/>
[Lombok]: <https://projectlombok.org/>
[Spring Data]: <https://spring.io/projects/spring-data>
[HttpClient]: <https://hc.apache.org/>
[Mockito]: <https://site.mockito.org/>
[Hamcrest]: <http://hamcrest.org/JavaHamcrest/>