# AWX Operator Tests

This directory contains test classes for the AWX Operator project.

## Unit Tests

Run unit tests with:

```bash
mvn test
```

## Remote Integration Tests

The project includes remote integration tests that validate functionality against a real AWX instance.

### Configuration

Remote integration tests use a dedicated Spring profile configuration in `application-remotetest.yml`:

```yaml
# AWX connection properties for remote testing
awx:
  baseUrl: http://82.165.203.242:32060
  username: ${AWX_USERNAME:admin}
  password: ${AWX_PASSWORD:password}
```

### Running Remote Tests

To run the remote integration tests, use:

```bash
mvn test -Dtest=AwxProjectServiceRemoteIT -Dspring.profiles.active=remotetest -DAWX_USERNAME=username -DAWX_PASSWORD=password
```

Replace `username` and `password` with valid AWX credentials.

### Test Features

The remote integration tests are ordered and perform the following operations:

1. **List Projects Test** (`@Order(1)`) - Retrieves and validates the list of existing projects
2. **Create Project Test** (`@Order(2)`) - Creates a new test project with unique name
3. **Get Project Test** (`@Order(3)`) - Retrieves and validates the created project
4. **Delete Project Test** (`@Order(4)`) - Deletes the project and verifies removal

The tests share state between methods using `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` and are ordered with `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`.

### Important Notes

⚠️ **WARNING**: Remote integration tests create and delete actual resources in the target AWX instance. Use with caution.

- Tests use proper setup/teardown with `@BeforeAll` and `@AfterAll`
- Cleanup logic ensures resources are deleted even if tests fail or are skipped
- Each step is in its own method for better error isolation and reporting
- Tests include additional assertions to verify state between steps 