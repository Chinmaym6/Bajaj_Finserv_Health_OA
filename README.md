# Bajaj Finserv Health – Qualifier 1 (Java)

This repository contains my solution for the **Bajaj Finserv Health | Qualifier 1 | Java Online Assessment**.  
The project is a Spring Boot application that **automatically generates a webhook, solves the assigned SQL problem, and submits the final SQL query** when the application starts.

---

## 🚀 Tech Stack
- **Java 17**
- **Spring Boot 3.4.9**
- **Maven 3.9.11**
- **RestTemplate** for API integration
- **PostgreSQL-flavored SQL**

---

## 📝 Problem Statement (Question 1)

From the tables `DEPARTMENT`, `EMPLOYEE`, and `PAYMENTS`, write a query to return:

- **Highest salary (AMOUNT)** paid on a day **not the 1st of the month**  
- **NAME** → concatenation of `FIRST_NAME` and `LAST_NAME`  
- **AGE** → calculated from `DOB`  
- **DEPARTMENT_NAME**  

---

## ✅ Final SQL Query (PostgreSQL)

```sql
SELECT
  p.amount AS salary,
  e.first_name || ' ' || e.last_name AS name,
  DATE_PART('year', AGE(CURRENT_DATE, e.dob))::int AS age,
  d.department_name
FROM payments p
JOIN employee e     ON e.emp_id = p.emp_id
JOIN department d   ON d.department_id = e.department
WHERE EXTRACT(DAY FROM p.payment_time) <> 1
ORDER BY p.amount DESC
LIMIT 1;
