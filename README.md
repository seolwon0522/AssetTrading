# Business Registration Verification API

This project implements a business registration verification API that integrates with the Korean National Tax Service.

## Testing the Business Verification API

### Using Postman

1. Start the Spring Boot application
2. Send a POST request to `http://localhost:8080/api/business-verification/verify`
3. Set the Content-Type header to `application/json`
4. Use the following JSON body:

```json
{
  "businessNum": "187-09-00256",
  "userName": "차일웅",
  "startDate": "20151111",
  "companyName": "트웬티세븐"
}
```

### Expected Response

```json
{
  "valid": true,
  "businessNum": "187-09-00256"
}
```

## API Parameters

| Parameter    | Description                                | Format      |
|--------------|--------------------------------------------| ------------|
| businessNum  | Business registration number               | With or without hyphens |
| userName     | Owner/representative name                  | Text        |
| startDate    | Business start date                        | YYYYMMDD    |
| companyName  | Company name                               | Text        |

## Important Notes

1. The API key is configured in `application.properties` and `application.yml`
2. The date format must be YYYYMMDD (without hyphens)
3. Business numbers can be entered with or without hyphens (the system will remove them)
4. The API validates against the Korean National Tax Service database

## Troubleshooting

If you encounter issues with the API, check the following:

1. Ensure the API key is correctly encoded in the configuration files
2. Verify that the business information is correct (check against bizno.net)
3. Make sure the date format is correct (YYYYMMDD without hyphens)
4. Check the application logs for detailed error messages 