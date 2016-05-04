#Exported file must have following structure to be possible import it in the application.

# JSON Structure #
```
{
  "projects" : [Array],
  "requests" : [Array]
}
```

## projects ##
Each "project" array element is a json object. It contains information about saved project data.

### project structure ###
**name** _(String)_ - Name of a project

**id** _(Number)_ - Database ID. Note: it will be used as a reference to find requests and it will change during import.

**time** _(Number)_ - Timestamp when project was created.

## requests ##
Each "requests" array element is a json object. It contains information about request attributes.

### the request structure ###
**encoding** _(String)_ - Payload encoding

**headers** _(String)_ - Headers string (Described in RFC http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html)

**method** _(String)_ - HTTP method (uppercase)

**name** _(String)_ - Name of the request

**payload** _(String)_ - Payload

**url** _(String)_ - The url

**project** _(Number)_ - Referenced project. Default 0.

**time** _(Number)_ - Time of creation.

**skipHeaders** _(Boolean)_ - True if in project and should not overwrite headers value from previous request. Default false.

**skipHistory** _(Boolean)_ - True if in project and should not overwrite history element value from previous requests URL. Default false.

**skipMethod** _(Boolean)_ - True if in project and should not overwrite method value from previous request. Default false.

**skipParams** _(Boolean)_ - True if in project and should not overwrite URL params from previous requests  URL. Default false.

**skipPath** _(Boolean)_ - True if in project and should not overwrite path value from previous requests URL. Default false.

**skipPayload** _(Boolean)_ - True if in project and should not overwrite payload value from previous request. Default false.

**skipProtocol** _(Boolean)_ - True if in project and should not overwrite headers value from previous request. Default false.

**skipServer** _(Boolean)_ - True if in project and should not overwrite server value from previous requests URL. Default false.

**driveId** _(String)_ - ID of the file saved on Google Drive

## Example ##
```
{
   "projects":[{
      "name" : "Some project",
      "id" : 1,
      "time" : 1367546943546
   }], 
   "requests":[{
      "id":1, 
      "encoding":"application/x-www-form-urlencoded", 
      "headers":"Accept-Encoding: compress, gzip\nContent-Length: 3495", 
      "method":"POST", 
      "name":"My request", 
      "payload":"a=content", 
      "project":1, 
      "skipHeaders":false, 
      "skipHistory":false, 
      "skipMethod":false, 
      "skipParams":false, 
      "skipPath":false, 
      "skipPayload":false, 
      "skipProtocol":false, 
      "skipServer":false, 
      "time":1367546943546, 
      "url":"http://my.service.com/", 
      "driveId":""
  }]
}

```