# generate-pdf

## Performance issues with PNG image files

* GhostScript is used to convert PDF to PDF/A format but but has a bug that results in slow conversion, poor rendering and large resulting files.
* using gif format files instead of png avoids this issue

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
	* post Content-Type: application/json {"html": "<html><title>Some Html</title></html>"}

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

The endpoints can then be accessed with the base url http://localhost:9852/pdf-generator-service/pdf-generator/generate

## Tips on installing application on Ubuntu

This command will install ghostscript:
```
sudo apt-get install ghostscript
```
This command will install wkhtmltopdf:
```
sudo apt-get install wkhtmltopdf
```
We then need to add this symbolic link so that the service finds the program:
```
sudo ln -s /usr/bin/wkhtmltopdf /usr/local/bin
```
If you need to check the logs of the generator service, you may find it useful to use this command to remove the graphite warnings:

```
sm --start PDF_GENERATOR_SERVICE --appendArgs '{"PDF_GENERATOR_SERVICE":["-Dmicroservice.metrics.graphite.enabled=false"]}' -f
```
This command will bring up the logs of the pdf-generator-service:
```
sm --logs PDF_GENERATOR_SERVICE
```

## License    

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
