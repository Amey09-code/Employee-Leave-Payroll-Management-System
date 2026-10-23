# Employee Management System

Spring Boot 3.4 · Java 21 · Spring Security (BCrypt + WebAuthn passkeys) · Spring Data JPA · Thymeleaf · MySQL · Bootstrap 5

Two roles: **HR** (`ROLE_HR`) manages the company, **Employee** (`ROLE_EMPLOYEE`) uses self-service.

## 1. Prerequisites

| Tool    | Version                          |
|---------|----------------------------------|
| JDK     | 21+                              |
| Maven   | 3.9+                             |
| MySQL   | 8.0+ (running on `localhost:3306`) |
| Browser | Recent Chrome / Edge / Safari / Firefox (for passkeys) |

Lombok is used: if you open the project in an IDE, install the Lombok plugin and enable annotation processing.

## 2. Create the database

```sql
CREATE DATABASE IF NOT EXISTS employee_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

(The JDBC URL also has `createDatabaseIfNotExist=true`, so this step is optional if your MySQL user may create databases.)

Set your MySQL credentials either in `src/main/resources/application.properties` or via environment variables:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

Tables are created automatically by Hibernate (`ddl-auto=update`). `data.sql` then inserts the sample data. It uses
`INSERT IGNORE` with fixed ids, so it is safe to run on every start.

## 3. Run

```bash
mvn spring-boot:run
# or
mvn clean package && java -jar target/employee-management-system-1.0.0.jar
```

Open <http://localhost:8080> (use `localhost`, not `127.0.0.1`, so passkeys work).

## 4. Sample credentials

| Role     | Username      | Password     |
|----------|---------------|--------------|
| HR       | `hr`          | `Hr@12345`   |
| Employee | `john.doe`    | `Emp@12345`  |
| Employee | `jane.smith`  | `Emp@12345`  |
| Employee | `mark.lee`    | `Emp@12345`  |

**Change these passwords (Settings / Password & Biometrics) before using the app for anything real.**

## 5. Passkeys / biometric sign-in

1. Sign in with your password.
2. HR: **Settings → Biometric sign-in**. Employee: **Password & Biometrics**.
3. Type a device name and click **Register passkey**. Your browser/OS asks for fingerprint, face or screen lock.
4. Sign out. On the login page click **Sign in with a passkey** and confirm with your biometric. The password form keeps working as a fallback.
5. Remove a lost device's passkey from the same page.

Requirements and configuration (`application.properties`):

```properties
app.webauthn.rp-id=localhost                       # your domain, no scheme/port
app.webauthn.rp-name=Employee Management System
app.webauthn.allowed-origins=http://localhost:8080 # must exactly match the URL in the address bar
```

* Passkeys only work on **https://** or **http://localhost**. For production use HTTPS and set `rp-id` to your domain
  (e.g. `hr.example.com`) and `allowed-origins` to `https://hr.example.com`.
* Changing `rp-id` invalidates passkeys registered under the old value.
* Credentials are stored in the `passkey_credentials` table (public key only, never a secret).

## 6. Features

**HR (`/hr/**`)** - Dashboard KPIs (employees, present/absent today, pending leaves, payroll summary) · Employee CRUD with
activate/deactivate and login-account creation · Departments · Attendance by date + CSV export · Leave approve/reject with comment ·
Payroll generation, mark-paid, PDF payslips · Reports (headcount, leave status, 6-month payroll, monthly attendance) ·
Settings (company name, leave allowance, payroll percentages, holidays, own password/passkeys).

**Employee (`/employee/**`)** - Dashboard (attendance status, leave balance, latest salary, holidays) · Profile (edit phone/address) ·
Check-in / check-out and history · Leave applications with balance check and cancellation · Salary history + PDF payslips ·
Holiday calendar · Password change and passkey management.

### Payroll formulas (monthly)

```
HRA        = basic x HRA%            (default 20%)
Allowances = basic x allowance%      (default 10%)
Gross      = basic + HRA + allowances + bonus
PF         = basic x PF%             (default 12%)
Tax        = gross x slab            (gross <= 3000: 0%, <= 5000: 5%, above 5000: 10%)
Net        = gross - (PF + tax + other deductions)
```

Percentages are editable in HR Settings. The tax slabs are illustrative: adjust `PayrollService.taxRate()` for your jurisdiction.

## 7. Security notes

* Route rules in `SecurityConfig`: `/hr/**` → HR only, `/employee/**` → employees only, everything else requires login.
* No employee id appears in employee URLs: all data is resolved from the logged-in principal. The two id-based employee endpoints
  (payslip download, leave cancel) verify ownership in the service layer and return 403 otherwise.
* BCrypt password hashing, CSRF protection on every POST, session fixation protection, logout invalidates the session and deletes the cookie.
* Deactivating an employee disables their login immediately.
* CSV export neutralises spreadsheet formula injection.
* Controllers never touch repositories; business rules live in `service`.

## 8. Project structure

```
src/main/java/com/example/employeemanagement
├── config       SecurityConfig, UserDetailsService, JPA-backed WebAuthn repositories
├── controller   Auth/Account, Hr* controllers, EmployeeController
├── dto          form objects and view records
├── entity       User, Employee, Department, Attendance, LeaveRequest, Salary, PasskeyCredential (+ Holiday, Setting)
├── exception    BusinessException, ResourceNotFoundException, GlobalExceptionHandler
├── repository   Spring Data repositories
├── service      business logic, payslip PDF (OpenPDF), CSV export
└── util         WebUtil
src/main/resources
├── templates    auth/ hr/ employee/ fragments/ error/
├── static       css/style.css, js/app.js, js/webauthn.js
├── application.properties
└── data.sql
```

## 9. Troubleshooting

* **`Access denied for user`** - set `DB_USERNAME` / `DB_PASSWORD`.
* **Passkey button does nothing / "not supported"** - use `http://localhost:8080` or HTTPS; check `allowed-origins` matches the address bar exactly.
* **Sample data missing** - make sure the app started once without errors; `data.sql` runs after the tables exist.
* **Passkey classes do not compile** - they target the Spring Security 6.4 WebAuthn API (`spring-boot-starter-parent` 3.4.5, `webauthn4j-core` 0.28.4). If you upgrade Spring Boot,
  re-check `config/JpaUserCredentialRepository` and `config/JpaPublicKeyCredentialUserEntityRepository` against that release.
