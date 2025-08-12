# Speech-to-SQL AI Application

## Overview

This application converts spoken natural language into accurate SQL queries in real time.
It uses **Deepgram** for high-accuracy speech-to-text transcription and **OpenAI GPT-4** for natural language to SQL translation.
The generated queries are validated for security, executed against a **PostgreSQL** database, and the results are displayed to the user through a simple web interface.

The project is designed for secure, low-latency database interaction, enabling non-technical users to query data by speaking instead of writing SQL.

---

## Key Features

* Speech-to-text integration using Deepgram API for accurate voice transcription.
* Natural language to SQL conversion with OpenAI GPT-4, using schema-awareness for improved accuracy.
* SQL injection prevention and query validation before execution.
* Sub-2-second average end-to-end response time for real-time interaction.
* Lightweight web frontend built with HTML, CSS, and JavaScript, served through Spring Boot.
* Automatic detection of database tables and columns to improve query relevance.

---

## Technology Stack

**Backend:** Java 17, Spring Boot
**Database:** PostgreSQL
**AI Services:** OpenAI GPT-4 API, Deepgram API
**Frontend:** HTML, CSS, JavaScript (served as static resources in Spring Boot)
**Security:** SQL injection prevention, environment-based API key management
**Build Tool:** Maven

---

## System Architecture

```
User Voice Input
        ↓
Deepgram API – Speech-to-Text
        ↓
OpenAI GPT-4 API – Text-to-SQL
        ↓
SQL Validation and Security Checks
        ↓
PostgreSQL Database
        ↓
Results Returned to Frontend
```

---

## Setup Instructions

### Prerequisites

* Java 17 installed
* PostgreSQL running locally or on a remote server
* Maven installed
* API keys for Deepgram and OpenAI

### Steps

1. **Clone the repository**

   ```bash
   git clone https://github.com/<your-username>/speech-to-sql.git
   cd speech-to-sql
   ```

2. **Configure environment variables**
   Create a `.env` file or set the variables in your IDE:

   ```
   DEEPGRAM_API_KEY=your-deepgram-key
   OPENAI_API_KEY=your-openai-key
   DATABASE_URL=jdbc:postgresql://localhost:5432/yourdb
   DATABASE_USER=your-db-user
   DATABASE_PASSWORD=your-db-password
   ```

3. **Build the application**

   ```bash
   mvn clean install
   ```

4. **Run the application**

   ```bash
   mvn spring-boot:run
   ```

5. **Access in browser**
   Open `http://localhost:8080` in your web browser.

---

## Example Use Case

**User Input (spoken):**

> Show me the total sales for last month by region.

**Generated SQL:**

```sql
SELECT region, SUM(sales_amount) 
FROM sales 
WHERE sale_date >= '2025-07-01' AND sale_date <= '2025-07-31'
GROUP BY region;
```

**Sample Output:**

| Region | Total Sales |
| ------ | ----------- |
| East   | 250,000     |
| West   | 310,000     |

---

## Future Enhancements

* Support for additional database types (e.g., MySQL, MongoDB).
* Role-based access control for query execution.
* Caching for repeated queries to reduce execution time.
* Optional voice feedback using text-to-speech.
