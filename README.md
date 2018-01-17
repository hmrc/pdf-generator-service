# generate-pdf

## Performance issues with PNG image files

* GhostScript is used to convert PDF to PDF/A format but but has a bug that results in slow conversion, poor rendering and large resulting files.
* using gif format files instead of png avoids this issue

## Using the Service
Please read this [page](https://confluence.tools.tax.service.gov.uk/display/OFFPAY/Using+PDF+Generator) if you would like to consume this service on MDTP
## Endpoint URLs

POST /pdf-generator-service/pdf-generator/generate

## Service Definitions    

All requests use the HTTP `POST` method  

### PDF Generator Service

#### Functionality

	* Generates a PDF based on the submitted html
	* Generated PDF must be PDF/A 1 or PDF/A 2 compliant
	* Generated PDF must match the HTML page from which it was generated

## Request
	* html-www-urlencoded-form with a single field 'html' that contains the html to be converted

## Response
	* HTTP 200 OK with a generated and downloadable PDF
	* HTTP 400 Bad Request for invalid/error scenarios

## Run the application locally

* Install wktohtml https://wkhtmltopdf.org/downloads.html version 0.12.3
* Install Ghostscript https://ghostscript.com/
* Check alias for ghostscript is ```gs
* Check parameters in application.conf after "PdfGenerator Service config - use these values for local running only"

To run the application execute

```
sbt ~run 9852
```

The endpoints can then be accessed with the base url http://localhost:9000/pdf-generator-service/pdf-generator/generate


## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
