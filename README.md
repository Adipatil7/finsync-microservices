# FinSync: Modern Microservices Expense Management Platform

![FinSync Logo](https://img.shields.io/badge/FinSync-Expense--Platform-blueviolet?style=for-the-badge&logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5%20/%204.0.3-6DB33F?style=flat-square&logo=springboot)
![Next.js](https://img.shields.io/badge/Next.js-14-black?style=flat-square&logo=nextdotjs)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-%23231F20.svg?style=flat-square&logo=apache-kafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=flat-square&logo=postgresql)

FinSync is a high-performance, scalable expense management platform built on a modern microservices architecture. It enables users to track personal finances, manage group expenses, and automate complex debt settlements with a seamless, real-time experience.

---

## 🏗️ Architecture Overview

FinSync leverages an event-driven microservices architecture to ensure high availability, scalability, and loose coupling between components.

```mermaid
graph TD
    Client[Next.js Frontend] <--> Gateway[API Gateway]

    subgraph "Microservices"
        Gateway <--> Auth[Auth Service]
        Gateway <--> Group[Group Service]
        Gateway <--> Settlement[Settlement Service]
        Gateway <--> Personal[Personal Finance Service]
    end

    subgraph "Infrastructure"
        Auth -- "user-registered-topic" --> Kafka((Kafka))
        Kafka -- "User Events" --> Group
        Kafka -- "User Events" --> Personal

        Group -- "group-expense-created-topic" --> Kafka
        Kafka -- "Expense Events" --> Settlement

        Group --- DBG[(PostgreSQL - Group)]
        Auth --- DBA[(PostgreSQL - Auth)]
        Settlement --- DBS[(PostgreSQL - Settlement)]
        Personal --- DBP[(PostgreSQL - Personal)]
    end
```

---

## 🚀 Key Features

- **🔐 Unified Authentication**: Secure JWT-based authentication with role-based access control.
- **👥 Collaborative Group Expenses**: Create groups, invite members, and split expenses effortlessly.
- **⚖️ Automated Settlements**: Advanced algorithms to simplify debts and generate optimal settlement plans.
- **📊 Personal Finance Insights**: Detailed analytics, budgeting tools, and transaction categorization.
- **⚡ Real-time Event Synchronization**: Asynchronous service communication via Kafka for decoupled data handling.
- **🎨 Premium UI/UX**: Dark-mode first design with smooth animations and responsive layouts.

---

## 🛠️ Tech Stack

### Backend (Microservices)

- **Language**: Java 21
- **Framework**: Spring Boot (3.3.5 / 4.0.3)
- **Security**: Spring Security + JWT
- **Gateway**: Spring Cloud Gateway
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL 15
- **Messaging**: Apache Kafka (Event-driven architecture)
- **Mapping**: MapStruct for high-performance DTO mapping
- **Utils**: Lombok, Jackson

### Frontend

- **Framework**: Next.js 14 (App Router)
- **Library**: React 18
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: Zustand
- **Data Fetching**: TanStack Query (React Query)
- **Components**: Radix UI, Lucide Icons
- **Animations**: Framer Motion
- **Visualizations**: Recharts

---

## 📂 Service Breakdown

| Service                | Responsibility                                                     |
| :--------------------- | :----------------------------------------------------------------- |
| **API Service**        | Central entry point, request routing, and JWT validation.          |
| **Auth Service**       | User identity management, registration, and secure authentication. |
| **Group Service**      | Management of groups, members, and shared expenses.                |
| **Settlement Service** | Calculation of balances and generation of debt-settlement plans.   |
| **Personal Finance**   | Individual expense tracking, budgeting, and financial analytics.   |
| **Common Events**      | Shared library for event schemas and serializable Kafka payloads.  |

---

## ⚙️ Getting Started

### Prerequisites

- **JDK 21+**
- **Node.js 18+**
- **Docker & Docker Compose**
- **Maven 3.9+**

### Installation

1. **Clone the repository**:

   ```bash
   git clone https://github.com/Adipatil7/finsync-microservices.git
   cd finsync-microservices
   ```

2. **Build the Backend**:

   ```bash
   # Build the parent and install shared events
   cd common-events && mvn clean install
   cd ..
   # Build individual services (optional, docker-compose will handle this)
   ```

3. **Install Frontend Dependencies**:
   ```bash
   cd finsync-frontend
   npm install
   ```

### Running with Docker

The easiest way to start the entire platform is using Docker Compose:

```bash
cd Infrastructure
docker-compose up -d
```

This will spin up:

- 4 PostgreSQL instances (Auth, Group, Settlement, Personal)
- Kafka & Zookeeper/KRaft cluster
- All Backend Microservices
- API Gateway

The services will be available at `http://localhost:8080`.

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

_Created with ❤️ me._
