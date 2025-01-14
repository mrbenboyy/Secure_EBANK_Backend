# [Frontend Repository: Secure E-Bank Frontend](https://github.com/HAJJIRI-OUSSAMA/Secure_EBANK_Frontend)

## Frontend Repository

The frontend for this project is hosted in a separate repository. You can find it here:

[E-Bank Frontend Repository](https://github.com/HAJJIRI-OUSSAMA/Secure_EBANK_Frontend)

# E-Bank Backend API
This is the backend API for an E-Bank application, built using Spring Boot. The API provides functionalities for managing customers, bank accounts, and account operations such as debit, credit, and transfer. It also includes authentication and authorization using JWT (JSON Web Tokens) and Spring Security.

## Features
- **Customer Management**: Create, update, delete, and retrieve customer information.
- **Bank Account Management**: Create and manage current and savings accounts.
- **Account Operations**: Perform debit, credit, and transfer operations on bank accounts.
- **Account History**: Retrieve the transaction history of a bank account.
- **Authentication and Authorization**: Secure endpoints using JWT and role-based access control.
- **Password Management**: Change password functionality with secure password encoding.

## Technologies Used
- **Spring Boot**: Framework for building the application.
- **Spring Security**: For authentication and authorization.
- **JWT (JSON Web Tokens)**: For secure token-based authentication.
- **Hibernate**: For ORM (Object-Relational Mapping) and database interactions.
- **MySQL/PostgreSQL**: Database for storing application data.
- **Lombok**: For reducing boilerplate code.
- **Maven**: For dependency management.

## API Endpoints
### Authentication
- **POST /auth/login**: Authenticate a user and return a JWT token.
- **POST /auth/change-password**: Change the password for a user.

### Customers
- **GET /customers**: Retrieve a list of all customers (Admin only).
- **GET /customers/search**: Search for customers by keyword (Admin only).
- **GET /customers/{id}**: Retrieve a specific customer by ID.
- **POST /customers**: Create a new customer (Admin only).
- **PUT /customers/{customerId}**: Update an existing customer (Admin only).
- **DELETE /customers/{id}**: Delete a customer by ID (Admin only).
- **GET /customers/{customerId}/accounts**: Retrieve all accounts for a specific customer.
- **GET /customers/email/{email}**: Retrieve a customer by email.

### Bank Accounts
- **GET /accounts/{accountId}**: Retrieve a specific bank account by ID.
- **GET /accounts**: Retrieve a list of all bank accounts (Admin only).
- **GET /accounts/{accountId}/operations**: Retrieve the transaction history for a specific account.
- **GET /accounts/{accountId}/pageOperations**: Retrieve paginated transaction history for a specific account.
- **POST /accounts/debit**: Perform a debit operation on an account.
- **POST /accounts/credit**: Perform a credit operation on an account.
- **POST /accounts/transfer**: Perform a transfer operation between two accounts.
- **POST /accounts/current**: Create a new current account (Admin only).
- **POST /accounts/saving**: Create a new savings account (Admin only).

### Security
- **JWT Authentication**: All endpoints (except /auth/login) require a valid JWT token in the Authorization header.
- **Role-Based Access Control**:
  - **ROLE_ADMIN**: Can access all endpoints.
  - **ROLE_USER**: Can access customer-specific and account-specific endpoints.

## Getting Started
### Prerequisites
- Java 17 or higher
- Maven 3.x
- MySQL/PostgreSQL database

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/e-bank-backend.git
   cd e-bank-backend
   ```
2. Configure the database:
   Update the `application.properties` file with your database credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ebank
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   spring.jpa.hibernate.ddl-auto=update
   ```
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
5. Access the API:
   The API will be available at `http://localhost:8080`.

## Usage
### Authentication
#### Login:
Send a POST request to `/auth/login` with the following body:
```json
{
  "email": "user@example.com",
  "password": "password"
}
```
The response will include a JWT token:
```json
{
  "access-token": "your-jwt-token"
}
```

#### Change Password:
Send a POST request to `/auth/change-password` with the following body:
```json
{
  "email": "user@example.com",
  "oldPassword": "oldPassword",
  "newPassword": "newPassword"
}
```

### Managing Customers
#### Create a Customer:
Send a POST request to `/customers` with the following body:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "password",
  "role": "ROLE_USER"
}
```
#### Update a Customer:
Send a PUT request to `/customers/{customerId}` with the updated customer details.

#### Delete a Customer:
Send a DELETE request to `/customers/{id}`.

### Managing Bank Accounts
#### Create a Current Account:
Send a POST request to `/accounts/current` with the following body:
```json
{
  "balance": 1000.0,
  "overDraft": 500.0,
  "customerDTO": {
    "id": 1
  }
}
```
#### Create a Savings Account:
Send a POST request to `/accounts/saving` with the following body:
```json
{
  "balance": 1000.0,
  "interestRate": 2.5,
  "customerDTO": {
    "id": 1
  }
}
```
#### Perform a Debit Operation:
Send a POST request to `/accounts/debit` with the following body:
```json
{
  "accountId": "account-id",
  "amount": 100.0,
  "description": "Withdrawal"
}
```
#### Perform a Credit Operation:
Send a POST request to `/accounts/credit` with the following body:
```json
{
  "accountId": "account-id",
  "amount": 100.0,
  "description": "Deposit"
}
```
#### Perform a Transfer Operation:
Send a POST request to `/accounts/transfer` with the following body:
```json
{
  "accountSource": "source-account-id",
  "accountDestination": "destination-account-id",
  "amount": 100.0
}
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Spring Boot and Spring Security documentation.
- JWT for token-based authentication.
- Lombok for reducing boilerplate code.
