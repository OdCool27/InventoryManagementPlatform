# Sentra Inventory MVP Progress

## Current Status

Backend MVP complete and verified on 2026-10-26. PostgreSQL migrations, live HTTP application startup, administrator bootstrap, API integration tests, role enforcement, concurrency and OpenAPI schemas passed. `mvn -q package` succeeded with 17 tests and produced the executable JAR. Frontend intentionally not started.

## Completed

- Inspected all original Java files, DTOs, configuration, migrations, Compose and tests.
- Preserved existing code and plural package names; reused JDBC, Spring Security, SpringDoc and record DTO conventions.
- Fixed compilation by targeting installed Java 24 instead of unavailable Java 27.
- Added product/car-part CRUD with deactivation, database-backed categories and combined vehicle/text/category filters.
- Separated inventory from products; retained legacy reserved quantities without expanding reservation workflows.
- Added explicit inventory initialization, set/add/remove operations, reorder thresholds, low/out-of-stock queries.
- Transactional stock changes call PostgreSQL functions with row locking; audit writes share the transaction.
- Implemented persisted employee authentication, BCrypt, environment-based first-admin bootstrap, role restrictions.
- Added audit history and summary/low/out-of-stock reports.
- Added validated requests, sensible HTTP error statuses and Swagger Basic authorization.
- Documented host application + Docker PostgreSQL workflow in README and README.Docker.md.
- Prepared portfolio README with feature overview, technology table, architecture diagram, engineering decisions, linked test evidence and explicit MVP limitations.
- Clarified that custom employee authentication disables Spring's generated default password; documented first-admin setup in .env.
- Cleared populated administrator values from the publishable .env.example; preserved local .env configuration.

## Partially Completed

- None in the backend MVP.

## Not Started

- Optional frontend: no frontend framework or application existed; backend-only delivery.
- Future integrations and expanded cart/order workflows remain outside scope.

## Database Migrations Added

All under `src/main/resources/db/migration/`:

- `V5__establish_base_schema.sql`: intended V1–V4 tables with valid SQL.
- `V6__separate_inventory_and_categories.sql`: inventory/category tables, stock transfer, removal of product stock columns, active flag, uniqueness/indexes.
- `V7__audit_and_inventory_functions.sql`: audit table and two working PostgreSQL functions.

Original V1–V4 files were not edited. Their single-underscore names are ignored by Flyway, and the source SQL contained syntax errors. Fresh-schema migrations succeeded against PostgreSQL 17.11. No database reset or volume deletion was performed.

## Models Added or Modified

Under `src/main/java/org/osyntra/osyntrainventorymanagement/models/`:

- Added `Inventory.java`, `AuditLog.java`.
- Removed stock fields/accessors from `Product.java` and adjusted `CarPart.java` constructor.
- Existing Employee/User roles and status reused.

## DTOs Added or Modified

Under the Java package's `dtos/`:

- `product/ProductRequest.java`, `ProductResponse.java`, `CategoryRequest.java`, `CategoryResponse.java`.
- `inventory/InventoryRequest.java`, `InventoryResponse.java`, `InventorySummary.java`, `QuantityRequest.java`, `StockRequest.java`, `ReorderRequest.java`.
- Completed `user/CreateEmployeeRequest.java`; added `user/EmployeeResponse.java`.
- Other original user DTO placeholders remain unused and unchanged.

## Repositories Added or Modified

Under `repositories/`:

- `ProductRepository.java`: JDBC CRUD, category operations, bound combined filters.
- `InventoryRepository.java`: stock creation/locking, function calls, inventory filters and summary.
- `EmployeeRepository.java`: persisted employee lookup/creation and bootstrap lock.
- `AuditRepository.java`: append and filtered/paged history reads.

## Services Added or Modified

Under `services/`:

- `ProductService.java`, `InventoryService.java`, `EmployeeService.java`, `AuditService.java`, `ReportService.java`.
- Mutations use Spring transactions; the audit actor is resolved server-side.
- Inactive products reject changes; insufficient stock and duplicates return conflicts.

## Controllers Added or Modified

Under `controllers/`:

- `ProductController.java`, `CategoryController.java`, `InventoryController.java`.
- `EmployeeController.java`, `AuthController.java`, `ReportController.java`, `AuditController.java`.
- `ApiExceptionHandler.java` supplies ProblemDetail responses for application/validation/database conflicts.

Under `config/`:

- `SecurityConfig.java`: HTTP Basic, stateless requests, role-based route permissions.
- `AdminInitializer.java`: initial ADMIN from environment, only with no existing employees.
- `OpenApiConfig.java`: API information and Swagger authentication.

## Stored Procedures / Functions

- `adjust_inventory(p_product_id UUID, p_amount INT, p_set BOOLEAN)`: locked atomic adjustment/set, protects reserved stock and integer bounds, updates timestamp. Called by `InventoryRepository.adjust()`.
- `low_stock_inventory()`: active stock records with available quantity <= reorder threshold. Used by inventory filters and low-stock reports.

## Tests

- `InventoryApiTests.java`: 14 passing test invocations, real PostgreSQL, MockMvc, transactional rollback. Covers CRUD, inventory, reports, audit, validation, authentication, permissions, filters, duplicates, database function rejection, Swagger schemas/UI.
- `InventoryConcurrencyTests.java`: passing concurrent removal test; one succeeds and one conflicts, final quantity and audit count verified.
- Existing `OsyntraInventoryManagementApplicationTests.java`: passing; now uses isolated test schema.
- `LiveServerTests.java`: passing actual HTTP server check, randomly generated fresh schema, admin bootstrap credentials, OpenAPI and Swagger UI.
- Final `mvn -q package`: 17 tests, zero failures/errors/skips; exit code 0.
- Executable artifact: `target/OsyntraInventoryManagement-0.0.1-SNAPSHOT.jar`.
- Standard command: `mvn clean test` with Compose PostgreSQL running and .env configured.

## Known Issues

- Expected Flyway warning for four ignored historical migration filenames; leave them unchanged.
- The previously staged .env has been removed from Git's index while preserving the local file. .gitignore excludes environment files, build output, logs, JVM diagnostics and IDE/OS files; .env.example remains trackable. No credentials were printed or changed.
- Original optional Dockerfile was not updated/tested; run Spring on the host.
- HTTP Basic should use HTTPS outside localhost. No token auth, employee editing, reservation management or reactivation endpoint is included.
- Test schemas require CREATE permission; do not run simultaneous test suites against the same database.
- Existing Java/Maven/Mockito toolchain emits deprecation/agent warnings; these do not fail tests.
- No seeded application employees/products were added to public schema; bootstrap credentials are user-supplied.

## Recommended Next Step

Set SENTRA_ADMIN_EMAIL and SENTRA_ADMIN_PASSWORD in the local environment or .env, run `docker compose up -d postgres` and `mvn spring-boot:run`, then follow README's Swagger walkthrough. Backend implementation and verification are complete; optional frontend work can be scoped separately.

## Exact Files To Continue From

- `README.md`: setup, API workflow and limitations.
- `src/main/java/org/osyntra/osyntrainventorymanagement/services/InventoryService.java`: stock transaction/business behavior.
- `src/main/java/org/osyntra/osyntrainventorymanagement/repositories/InventoryRepository.java`: inventory SQL and function integration.
- `src/main/java/org/osyntra/osyntrainventorymanagement/config/SecurityConfig.java`: role policy.
- `src/main/java/org/osyntra/osyntrainventorymanagement/config/AdminInitializer.java`: bootstrap.
- `src/main/resources/db/migration/V5__establish_base_schema.sql`, `V6__separate_inventory_and_categories.sql`, `V7__audit_and_inventory_functions.sql`.
- `src/test/java/org/osyntra/osyntrainventorymanagement/InventoryApiTests.java`.
- `src/test/java/org/osyntra/osyntrainventorymanagement/InventoryConcurrencyTests.java`.
- `src/test/java/org/osyntra/osyntrainventorymanagement/LiveServerTests.java`.
