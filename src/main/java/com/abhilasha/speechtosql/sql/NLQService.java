package com.abhilasha.speechtosql.sql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NLQService {

    private final JdbcTemplate jdbc;

    public NLQService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> query(String nl) {
        String q = (nl == null ? "" : nl).trim().toLowerCase();

        boolean wantCount     = q.matches(".*\\b(count|how many|number of)\\b.*");
        boolean wantSum       = q.matches(".*\\b(sum|total|revenue|amount)\\b.*");
        boolean wantAvg       = q.matches(".*\\baverage|avg\\b.*");
        boolean wantTopN      = q.matches(".*\\btop\\s+\\d+\\b.*");
        boolean groupByStatus = q.matches(".*\\bby status\\b.*|.*\\bper status\\b.*");
        boolean groupByCust   = q.matches(".*\\bby customer\\b.*|.*\\bper customer\\b.*");
        boolean mentionOrders = q.contains("order");
        boolean mentionCustomers = q.contains("customer");

        int limit = extractTopN(q);
        String statusFilter = extractStatus(q);
        String customerName = extractCustomer(q);
        DateRange range = extractDateRange(q);

        if (!mentionOrders && mentionCustomers) {
            wantTopN = wantTopN || q.contains("top");
            groupByCust = true;
            mentionOrders = true;
        }

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (wantSum) {
            sql.append("SELECT SUM(o.amount) AS total_amount");
        } else if (wantAvg) {
            sql.append("SELECT AVG(o.amount) AS avg_amount");
        } else if (groupByStatus) {
            sql.append("SELECT o.status, COUNT(*) AS cnt");
        } else if (groupByCust || wantTopN || mentionCustomers) {
            sql.append("SELECT c.name AS customer, SUM(o.amount) AS total_amount");
        } else {
            sql.append("SELECT COUNT(*) AS count");
        }

        sql.append(" FROM orders o JOIN customers c ON o.customer_id = c.id WHERE 1=1");

        if (statusFilter != null) {
            sql.append(" AND LOWER(o.status) = ?");
            params.add(statusFilter);
        }
        if (customerName != null) {
            sql.append(" AND LOWER(c.name) = ?");
            params.add(customerName.toLowerCase());
        }
        if (range != null) {
            sql.append(" AND o.created_at BETWEEN ? AND ?");
            params.add(java.sql.Date.valueOf(range.start));
            params.add(java.sql.Date.valueOf(range.end));
        }

        if (groupByStatus) {
            sql.append(" GROUP BY o.status ORDER BY cnt DESC");
        } else if (groupByCust || wantTopN || mentionCustomers) {
            sql.append(" GROUP BY c.name ORDER BY total_amount DESC");
        }
        if (limit > 0) sql.append(" LIMIT ").append(limit);

        String finalSql = sql.toString();
        List<Map<String, Object>> rows = jdbc.queryForList(finalSql, params.toArray());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sql", finalSql);
        out.put("params", params);
        out.put("rows", rows);
        return out;
    }

    private int extractTopN(String q) {
        Matcher m = Pattern.compile("\\btop\\s+(\\d+)\\b").matcher(q);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
    private String extractStatus(String q) {
        for (String s : List.of("completed","pending","cancelled","canceled")) {
            if (q.contains(s)) return s.equals("canceled") ? "cancelled" : s;
        }
        return null;
    }
    private String extractCustomer(String q) {
        Matcher m = Pattern.compile("\\bfor\\s+([a-z]+)\\b").matcher(q);
        if (m.find()) return m.group(1);
        return null;
    }

    private static class DateRange { LocalDate start, end; }
    private DateRange extractDateRange(String q) {
        LocalDate today = LocalDate.now();
        DateRange r = new DateRange();
        if (q.contains("today"))      { r.start=today; r.end=today; return r; }
        if (q.contains("yesterday"))  { r.start=today.minusDays(1); r.end=r.start; return r; }
        if (q.contains("last 7 days") || q.contains("past 7 days")) { r.start=today.minusDays(6); r.end=today; return r; }
        if (q.contains("last week"))  {
            var mon = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
            r.start = mon; r.end = mon.plusDays(6); return r;
        }
        if (q.contains("this week"))  {
            var mon = today.with(java.time.DayOfWeek.MONDAY);
            r.start = mon; r.end = mon.plusDays(6); return r;
        }
        if (q.contains("this month")) {
            r.start = today.with(TemporalAdjusters.firstDayOfMonth());
            r.end   = today.with(TemporalAdjusters.lastDayOfMonth()); return r;
        }
        if (q.contains("last month")) {
            r.start = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            r.end   = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()); return r;
        }
        if (q.contains("this quarter")) {
            int m = today.getMonthValue();
            int qStart = ((m-1)/3)*3 + 1;
            var first = LocalDate.of(today.getYear(), qStart, 1);
            r.start = first; r.end = first.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()); return r;
        }
        if (q.contains("last 30 days") || q.contains("past 30 days")) {
            r.start = today.minusDays(29); r.end = today; return r;
        }
        return null;
    }
}
