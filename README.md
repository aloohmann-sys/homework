# Homework - Notification Dispatch Service

Click **Use this template → Create a new repository**. Create a **private** repository under your own account and invite orion-homework-dev as a collaborator. Complete the assignment and send us your repository link. Please do not publish your solution or open a pull request against this template.

## Objective

Refactor the legacy code to a production ready solution for the notification dispatch service that is maintainable, readable, and scalable using modern practices.

This exercise is intended to be completed in approximately 4 hours.

The application sends `EMAIL`, `SMS`, and `PUSH` notifications, stores the outcome of those attempts, and enforces the operational safeguards already present in the system. 
The goal is to improve the design so the code is easier to understand, easier to extend, and more robust without changing the behavior that matters.

This is a design and refactoring exercise. The focus is on making a small system feel thoughtful, clean, and ready for change. 
It also acknowledges that existing API boundaries and persistence design may not be ideal and should be improved.

## System summary

The service is responsible for:

- sending notifications to different channels
- tracking the result of each attempt
- rejecting unsupported notification types
- rejecting blocked or rate-limited recipients
- recording failures and applying a simple rejection path after repeated problems

## Core expectations

The solution should be clear and defensible.

The project should demonstrate sound engineering judgment through:

- clear, readable code
- sensible use of current best practices
- no unnecessary complexity

## Behavior to preserve

The system must continue to support the existing notification types and operational safeguards:

- `EMAIL`
- `SMS`
- `PUSH`
- blocked or rate-limited recipients are rejected
- unsupported notification types fail clearly
- failures are tracked
- repeated failures eventually trigger a simple rejection path

## What matters most

We care about:

- correctness
- maintainability
- readability
- clear design decisions
- practical modernization
- scalability

## Development process

Please keep a record of the changes you make and why you make them. Preferably, use small, meaningful Git commits with messages that explain both the change and its rationale. Brief notes in the README can supplement the commit history, especially for trade-offs or alternatives you considered.

## Refactor acceptance criteria

- Valid requests for `EMAIL`, `SMS`, and `PUSH` are dispatched and their outcomes are recorded.
- A missing or blank target is rejected; blocked and rate-limited targets are rejected before a dispatch is recorded.
- Unsupported notification types fail clearly and the attempt is recorded as unsupported.
- A failed dispatch is recorded as a failure. After three failures, the next attempt is recorded as rejected without dispatch.

## Refactor design

- Keep the controller focused on HTTP input and output. Preserve the existing routes while the internals are refactored, avoiding an unrelated API migration.
- Use a dispatcher to select a channel handler by normalized notification type, keeping channel-specific sending out of orchestration.
- Put recipient checks and the repeated-failure rejection rule in an enforcement service. Check recipients before recording a dispatch; check the circuit breaker after opening a dispatch so rejected attempts remain in history.
- Use Hibernate/JPA entities and a Spring Data repository for persistence. The orchestration service coordinates enforcement, dispatch, and persistence without owning database details; Hibernate validates the schema initialized from `schema.sql`.
- Retain the existing response shape and database behavior during this refactor. Defer schema/API redesign and bonus features unless needed to preserve the acceptance criteria.

## Implementation note

The failure counter is global. Once it reaches three, all further attempts are rejected before delivery, so it cannot reset through a successful dispatch; recovery requires an external counter reset. This existing policy was preserved rather than silently changed during the structural refactor.

## Constraints

The solution must use Java and Spring Boot. You may upgrade the Java version and dependencies.

## Build and run

The project uses Java 25, Gradle 9.9, and Spring Boot 4.1.2. Use the Gradle wrapper to run the tests or start the application:

```shell
./gradlew test
./gradlew bootRun
```

Everything else may be redesigned or rewritten, including the API, database schema, persistence approach, and project structure, provided the behavior described above is preserved.

Keep the solution proportional to the problem. The goal is to transform a small but messy system into something clean, understandable, and fit for future change.


## Bonus points

These are not required for the core task, but they can earn additional credit if included well:

- adding a update endpoint for notifications
- introducing a proper blacklist functionality to enforcement flow for blocked recipients
