#/bin/bash

curl -X 'POST' \
  'http://localhost:8080/api/companies' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "The transport company"
}'

curl -X 'POST' \
  'http://localhost:8080/api/companies/1/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": 0,
  "name": "Important customer"
}'

curl -X 'POST' \
  'http://localhost:8080/api/companies/1/drivers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "firstName": "John",
  "lastName": "Doe"
}'

curl -X 'POST' \
  'http://localhost:8080/api/companies/1/vehicles' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "registration": "123abc",
  "type": "Bus",
  "capacity": 12
}'

curl -X 'POST' \
  'http://localhost:8080/api/companies/1/passengertransport' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "startAddress": "Street 1",
  "endAddress": "Street 2",
  "startDate": "2024-01-05",
  "endDate": "2024-01-12",
  "numberOfPassengers": 10,
  "price": 120,
  "customerId": 1,
  "vehicleId": 1,
  "driverId": 1
}'

curl -X 'POST' \
  'http://localhost:8080/api/drivers/1/qualifications' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": 0,
  "type": "flammable liquids"
}'
