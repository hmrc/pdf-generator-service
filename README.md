
# generate-pdf


## Endpoint URLs
POST /generate-pdf

## Service Definitions

All requests use the HTTP `POST` method

### PDF Generator Service (external facing API - `/generate-pdf`)

#### Functionality

	*Generates a PDF based on the submitted html file
	*Generated PDF must be PDF/A 1 or PDF/A 2 compliant
	*Generated PDF must match the HTML page from which it was generated

## Request
	*Body (Multipart Form): A single html file. 

## Response
	*HTTP 200 OK with a generated and downloadable PDF
	*HTTP 400 Bad Request for invalid/error scenarios

## Run the application locally

To run the application execute

```
sbt ~run 9000
```

The endpoints can then be accessed with the base url http://localhost:9000/pdf-generator


## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
