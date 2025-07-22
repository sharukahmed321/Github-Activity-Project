# GitHub Activity Tracker

A production-ready Spring Boot microservice that asynchronously fetches GitHub repositories and commit activity for any user. It uses reactive WebClient, includes rate limiting, caching, circuit breakers, clean architecture, and test coverage.

---

## 🚀 Features

- Fetch public repositories of a GitHub user
- Fetch recent commits for each repository
- Reactive, non-blocking design using WebClient
- GitHub API rate limit handling
- Circuit Breaker & Resilience4j integration
- Centralized exception handling
- Clean Architecture with interfaces
- Caching support
- Full unit test coverage

---

## 🛠️ Tech Stack

- Java 17+
- Spring Boot 3.x
- Spring WebFlux (WebClient)
- Resilience4j (Circuit Breaker)
- Caffeine Cache
- JUnit 5 + Mockito
- Maven

---

## Run the Project

Just Run the Main Application file in the project. To build or compile the project, use ./gradlew build
