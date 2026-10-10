# SaaS Restaurant Management Platform: Backend

Backend of a SaaS platform that digitalises the restaurant workflow, from browsing the menu to paying the bill. It was built as an internship project following the *Cahier des charges*.

**Stack:** Java 21 · Spring Boot 4 · Spring Data JPA / Hibernate · PostgreSQL · Maven · OpenPDF

---

## Features

| Module | What it does |
|---|---|
| Restaurants & employees | CRUD for restaurants (accepted payment methods, service types), employees with roles |
| Zones, tables, QR codes | Zones per restaurant, tables per zone, a unique QR code per table, waiter-to-zone assignment |
| Menu | Categories, products, variants, supplements, product image upload, public menu endpoint |
| Orders | Create / view / modify / cancel orders, status workflow, filtered listing |
| Payments | Record a payment, PDF invoice generation |

Soft delete (`is_deleted`) is used where it makes sense, with matching `restore` endpoints.

---

## Roles and permissions

| Role | Access |
|---|---|
| `SUPER_ADMIN` | Global management of every restaurant |
| `OWNER` | Full management of their own restaurant |
| `MANAGER` | Menu, zones, tables, employees, orders |
| `WAITER` | Orders of their restaurant, marks orders as served |
| `COOK` | Moves orders through preparation (`IN_PREPARATION`, `READY`) |
| `CASHIER` | Payments and invoices |
| `USER` | A customer: can place orders and see their own |

**Role assignment hierarchy:** an `OWNER` or `MANAGER` can only assign roles strictly *below* their own (a manager cannot assign `MANAGER`, `OWNER` or `SUPER_ADMIN`), and cannot edit their own employee record. `SUPER_ADMIN` bypasses these limits. The ranking lives in `Role.RoleName` and the check in `RoleAssignmentPolicy`.

---

## Getting started

### Prerequisites

- **JDK 21** (a JDK, not a JRE: Maven needs `javac`)
- **PostgreSQL** with an empty database, for example `saas_restaurant`
- No separate Maven install is needed (the project ships with `mvnw`)

### Configuration

Settings are read from environment variables, with defaults in `src/main/resources/application.properties`:

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/saas_restaurant` | JDBC URL |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | *(none, required)* | Database password |

Other properties:

| Property | Default | Description |
|---|---|---|
| `app.upload.dir` | `uploads` | Folder where product images are stored |
| `app.upload.base-url` | `/uploads` | URL prefix used to serve images |
| `app.invoice.vat-rate` | `4` | VAT percentage shown on invoices (prices are tax-inclusive) |
| `app.invoice.currency` | `MAD` | Currency shown on invoices |

Tables are created automatically on startup (`spring.jpa.hibernate.ddl-auto=update`).

### Run

**Windows (PowerShell):**

```powershell
$env:DB_PASSWORD = "your_postgres_password"
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
export DB_PASSWORD=your_postgres_password
./mvnw spring-boot:run
```

The API is then available at `http://localhost:8080`.

---

## Authentication (current state)

There is no login or JWT yet. The acting user is identified by a request header:

```
X-User-Id: <user id>
```

Authorization is enforced in the services: every protected endpoint checks the user's role in the target restaurant. Public endpoints need no header: reading restaurants, reading the menu, and scanning a table QR code. Replacing the header with real authentication (JWT) is the natural next step.

---

## API overview

All responses (except the PDF, PNG and QR scan endpoints) use the same wrapper:

```json
{
  "success": true,
  "status": 200,
  "message": "Employees retrieved successfully",
  "data": { },
  "timestamp": "2026-10-10T18:00:00"
}
```

List endpoints are paginated with `page` (default 0) and `size` (default 20, max 100).

### Restaurants (`/restaurants`)

| Method | Path | Description |
|---|---|---|
| POST | `/restaurants` | Create a restaurant, with its accepted payment methods (`CASH`, `CARD`, `ONLINE`) and service types |
| GET | `/restaurants/{id}` | Get one restaurant (public) |
| GET | `/restaurants` | Search (`name`, `email`, `ownerId`) (public) |
| GET | `/restaurants/deleted` | List deleted restaurants |
| PUT | `/restaurants/{id}` | Update a restaurant and its configuration |
| DELETE | `/restaurants/{id}` | Soft delete |
| POST | `/restaurants/{id}/restore` | Restore |

### Employees (`/restaurants/{restaurantId}/employees`)

| Method | Path | Description |
|---|---|---|
| POST | `/` | Assign an existing user as an employee: `{ "userId": 18, "roles": ["WAITER"] }` |
| GET | `/` | Search (`email`, `firstName`, `lastName`, `role`) |
| GET | `/{employeeId}` | Get one employee |
| PUT | `/{employeeId}` | Replace the employee's roles: `{ "roles": ["COOK"] }` |
| DELETE | `/{employeeId}` | Soft delete (owner or admin) |
| POST | `/{employeeId}/restore` | Restore (owner or admin) |
| GET | `/deleted` | List deleted employees (admin only) |

### Zones (`/restaurants/{restaurantId}/zones`)

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create a zone |
| GET | `/` | Search zones (`name`) |
| GET | `/{zoneId}` | Get one zone |
| PUT | `/{zoneId}` | Update a zone |
| DELETE | `/{zoneId}` | Soft delete |
| POST | `/{zoneId}/restore` | Restore |
| GET | `/deleted` | List deleted zones |

### Waiter assignment (`/restaurants/{restaurantId}/zones`)

| Method | Path | Description |
|---|---|---|
| PUT | `/{zoneId}/waiter` | Assign a waiter to a zone |
| GET | `/{zoneId}/waiter` | Get the responsible waiter of a zone |
| GET | `/waiter-assignments` | All waiter assignments of the restaurant |
| DELETE | `/{zoneId}/waiter` | Unassign the waiter |

### Tables and QR codes (`/restaurants/{restaurantId}`)

| Method | Path | Description |
|---|---|---|
| POST | `/zones/{zoneId}/tables` | Create a table in a zone |
| GET | `/tables` | All tables of the restaurant |
| GET | `/zones/{zoneId}/tables` | Tables of one zone |
| GET | `/zones/{zoneId}/tables/{tableId}` | Get one table |
| PUT | `/zones/{zoneId}/tables/{tableId}` | Update a table |
| DELETE | `/zones/{zoneId}/tables/{tableId}` | Soft delete |
| POST | `/zones/{zoneId}/tables/{tableId}/restore` | Restore |
| GET | `/zones/{zoneId}/tables/deleted` | List deleted tables |
| GET | `/zones/{zoneId}/tables/{tableId}/qr-code` | Download the table's QR code as a PNG |

**Scanning a QR code (public):** `GET /public/qr/{qrCodeToken}` identifies the table, its zone and the responsible waiter. The same token is what a customer sends as `tableQrToken` when ordering.

### Menu (`/restaurants/{restaurantId}`)

Reads are **public**; writes need `OWNER`, `MANAGER` or `SUPER_ADMIN` of that restaurant.

| Method | Path | Description |
|---|---|---|
| GET | `/menu` | Whole menu: categories → products → variants and supplements |
| POST | `/categories` | Create a category `{ "name", "description" }` |
| GET | `/categories` | List categories |
| PUT | `/categories/{categoryId}` | Update a category |
| DELETE | `/categories/{categoryId}` | Soft delete (also deletes its products) |
| POST | `/categories/{categoryId}/restore` | Restore (also restores its products) |
| POST | `/products` | Create a product (`multipart/form-data`, see below) |
| GET | `/products?categoryId=` | List products of a category |
| PUT | `/products/{productId}` | Update name, description, price, category |
| PATCH | `/products/{productId}/image` | Replace the image (`multipart`, field `image`) |
| DELETE | `/products/{productId}` | Soft delete |
| POST | `/products/{productId}/restore` | Restore |
| PUT | `/products/{productId}/variants` | Replace all variants: `{ "variants": [{ "name": "Large", "priceModifier": 4.00 }] }` |
| GET | `/products/{productId}/variants` | List variants |
| PUT | `/products/{productId}/supplements` | Replace all supplements: `{ "supplements": [{ "name": "Extra cheese", "price": 1.50 }] }` |
| GET | `/products/{productId}/supplements` | List supplements |

**Creating a product** is a `multipart/form-data` request with these fields:

| Key | Type | Example |
|---|---|---|
| `name` | text | `Margherita` |
| `description` | text | `Tomato, mozzarella, basil` |
| `basePrice` | text | `8.50` |
| `categoryId` | text | `1` |
| `image` | file | `.png`, `.jpg` or `.webp`, max 5 MB |

Uploaded images are served at `/uploads/products/<file>`.

### Orders (`/restaurants/{restaurantId}/orders`)

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create an order (any logged-in user) |
| GET | `/{orderId}` | Order details (staff, or the customer who placed it) |
| GET | `/` | List orders (staff). Filters: `zoneId`, `tableId`, `status`, `from`, `to` (`yyyy-MM-dd`) |
| GET | `/mine` | The caller's own orders |
| PUT | `/{orderId}` | Replace the items / notes (re-priced from the current menu) |
| PATCH | `/{orderId}/status` | Move to the next status: `{ "status": "CONFIRMED" }` |
| PATCH | `/{orderId}/cancel` | Cancel the order |

Example order:

```json
{
  "tableQrToken": "TOKEN-FROM-THE-TABLE",
  "notes": "No onions",
  "items": [
    { "productId": 1, "variantId": 2, "quantity": 2, "supplementIds": [1, 2] }
  ]
}
```

`tableQrToken` is optional (leave it out for takeaway). Each order line stores a **snapshot** of the product name, variant and prices, so later menu changes never alter existing orders.

#### Status workflow

```
CREATED → CONFIRMED → IN_PREPARATION → READY → SERVED → PAID
```

- Statuses only move forward, one step at a time.
- `CANCELLED` can be set from any status **except `PAID`**.
- `PAID` cannot be set through the status endpoint; it is set by recording a payment.
- Who moves an order into each status: `CONFIRMED` by waiter/cashier, `IN_PREPARATION` and `READY` by cook, `SERVED` by waiter. `MANAGER`, `OWNER` and `SUPER_ADMIN` can do any step.
- Items can be modified by staff while `CREATED` or `CONFIRMED`, and by the customer only while `CREATED`.

### Payments and invoices (`/restaurants/{restaurantId}/orders/{orderId}`)

| Method | Path | Description |
|---|---|---|
| POST | `/payment` | Record the payment: `{ "method": "CASH" }` |
| GET | `/payment` | Get the payment |
| GET | `/invoice` | Download the invoice as a PDF |

Rules:

- One full payment per order. The amount is always the order total, so it is not sent by the client.
- The order must be `SERVED`. On success it becomes `PAID`.
- The method (`CASH`, `CARD`, `ONLINE`) must be accepted by the restaurant. For `ONLINE` an optional `reference` can be sent.
- Recording payments: `CASHIER`, `MANAGER`, `OWNER`, `SUPER_ADMIN`. Viewing the invoice: those roles plus the customer who placed the order.
- Invoice numbers look like `INV-12-000045`. The PDF shows the lines, the total excluding VAT, the VAT (default 4%, prices are tax-inclusive) and the payment method.

---

## Project structure

```
src/main/java/com/restaurant/saas_restaurant/
├── common/        shared code: BaseEntity, Role, ApiResponse, security helpers, image storage, config
├── restaurant/    restaurants and their configuration
├── employee/      employees and role assignment
├── user/          user accounts
├── zone/          zones
├── table/         tables
├── waiterzone/    waiter-to-zone assignment
├── qrcode/        QR code generation and public scan
├── menu/          category · product · variant · supplement
├── order/         orders, order lines, status workflow
└── payment/       payments and PDF invoices
```

Each module follows the same layered structure: `controller → service → repository`, with `dto` and `entity` packages.

## Notes

- Product images are stored on the local disk (`uploads/`). The `ImageUploadService` interface allows swapping in S3 or another storage later.
- The `uploads/` folder is git-ignored (only `.gitkeep` is tracked).
- Never commit a real database password: use the `DB_PASSWORD` environment variable.
