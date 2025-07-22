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

## 🔧 Project Structure

src
├── main
│ ├── java/com/savantlabs/activitytracker
│ │ ├── controller # REST endpoints
│ │ ├── service # Interfaces and Implementations
│ │ ├── client # GitHub API client (WebClient)
│ │ ├── config # WebClient, Caching, Resilience4j
│ │ ├── exception # Custom exception classes
│ │ └── model # Domain models (Repo, Commit)
│ └── resources
│ └── application.properties
├── test
│ └── java/... # Unit tests with mocks

## Run the Project

mvn spring-boot:run
