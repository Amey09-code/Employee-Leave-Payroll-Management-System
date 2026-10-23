-- Idempotent seed data (INSERT IGNORE on fixed primary keys), safe to run on every start.
-- Passwords (BCrypt):  hr / Hr@12345     john.doe, jane.smith, mark.lee / Emp@12345

INSERT IGNORE INTO departments (id, name, description) VALUES
 (1, 'Human Resources', 'People operations, hiring and payroll'),
 (2, 'Engineering', 'Software development and QA'),
 (3, 'Sales', 'Sales and customer success'),
 (4, 'Finance', 'Accounting and budgeting');

INSERT IGNORE INTO users (id, username, password, role, enabled) VALUES
 (1, 'hr',         '$2b$10$47jSKT.qzasXOIV6iqECdenNZITE74nQzeOoI3q6GdyYRN1SDjM0m', 'ROLE_HR', 1),
 (2, 'john.doe',   '$2b$10$jb.SmYKf3p7TJMn/4L7SsupVJecNlHDjSZizoPuugVN2h/cgKMT42', 'ROLE_EMPLOYEE', 1),
 (3, 'jane.smith', '$2b$10$jb.SmYKf3p7TJMn/4L7SsupVJecNlHDjSZizoPuugVN2h/cgKMT42', 'ROLE_EMPLOYEE', 1),
 (4, 'mark.lee',   '$2b$10$jb.SmYKf3p7TJMn/4L7SsupVJecNlHDjSZizoPuugVN2h/cgKMT42', 'ROLE_EMPLOYEE', 1);

INSERT IGNORE INTO employees (id, employee_code, first_name, last_name, email, phone, address, date_of_birth, join_date, designation, basic_salary, active, department_id, user_id) VALUES
 (1, 'EMP0001', 'Hannah', 'Reed',  'hr@company.com',         '+1-555-0100', '12 Park Street',  '1988-04-12', '2019-03-01', 'HR Manager',        6000.00, 1, 1, 1),
 (2, 'EMP0002', 'John',   'Doe',   'john.doe@company.com',   '+1-555-0101', '48 Lake Avenue',  '1992-09-23', '2021-06-15', 'Software Engineer', 5000.00, 1, 2, 2),
 (3, 'EMP0003', 'Jane',   'Smith', 'jane.smith@company.com', '+1-555-0102', '9 Hill Road',     '1994-01-30', '2022-01-10', 'Sales Executive',   3800.00, 1, 3, 3),
 (4, 'EMP0004', 'Mark',   'Lee',   'mark.lee@company.com',   '+1-555-0103', '77 River Lane',   '1990-11-05', '2020-08-20', 'Accountant',        4200.00, 1, 4, 4);

-- Attendance for the last few days (relative to today)
INSERT IGNORE INTO attendance (id, employee_id, work_date, check_in, check_out, status) VALUES
 (1, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:02:00', '17:35:00', 'PRESENT'),
 (2, 3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:15:00', '17:40:00', 'PRESENT'),
 (3, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', '12:30:00', 'HALF_DAY'),
 (4, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:50:00', '17:10:00', 'PRESENT'),
 (5, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:05:00', '17:30:00', 'PRESENT'),
 (6, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:20:00', '17:45:00', 'PRESENT');

INSERT IGNORE INTO leave_requests (id, employee_id, leave_type, start_date, end_date, days, reason, status, hr_comment, applied_at, decided_at) VALUES
 (1, 3, 'ANNUAL', DATE_ADD(CURDATE(), INTERVAL 14 DAY), DATE_ADD(CURDATE(), INTERVAL 16 DAY), 3, 'Family trip', 'PENDING', NULL, NOW(), NULL),
 (2, 2, 'SICK',   DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 19 DAY), 2, 'Fever',       'APPROVED', 'Get well soon', DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY));

-- One paid salary for last month for each employee (formulas: HRA 20%, allowance 10%, PF 12%, tax slabs - see README)
INSERT IGNORE INTO salaries (id, employee_id, salary_month, salary_year, basic, hra, allowances, bonus, gross_salary, pf_deduction, tax_deduction, other_deductions, total_deductions, net_salary, status, generated_at, remarks) VALUES
 (1, 2, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 5000.00, 1000.00, 500.00, 0.00, 6500.00, 600.00, 650.00, 0.00, 1250.00, 5250.00, 'PAID', NOW(), 'Seed data'),
 (2, 3, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 3800.00,  760.00, 380.00, 0.00, 4940.00, 456.00, 247.00, 0.00,  703.00, 4237.00, 'PAID', NOW(), 'Seed data'),
 (3, 4, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 4200.00,  840.00, 420.00, 0.00, 5460.00, 504.00, 546.00, 0.00, 1050.00, 4410.00, 'PAID', NOW(), 'Seed data');

INSERT IGNORE INTO holidays (id, holiday_date, name) VALUES
 (1, DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'Founders Day'),
 (2, DATE_ADD(CURDATE(), INTERVAL 35 DAY), 'Winter Break'),
 (3, DATE_ADD(CURDATE(), INTERVAL 60 DAY), 'Company Retreat'),
 (4, DATE_ADD(CURDATE(), INTERVAL 90 DAY), 'Public Holiday');
