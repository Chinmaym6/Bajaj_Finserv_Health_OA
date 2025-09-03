package com.chinmay.bfh.util;

public class SqlRepository {

    public static String finalQueryForQuestion1Postgres() {
        return """
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
                """;
    }
}
