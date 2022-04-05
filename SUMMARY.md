# Summary

## Comments

Thank you for giving this opportunity by proving a test-assignment to understand and to get know about the tech stacks and practices of Mercans. It really helped me to get an overview about the technologies used.

The Acceptance Criteria has enough details to know about the work needs to be done, and It's easy to understand.

I have tried writing the code in Kotlin so that i can learn more about Kotlin.

As per my understanding from the assignment given, I have implemented the rest api's for uploading, downloading, deleting and retrieving the file metadata. I observed a expireTime field in the fileupload api spec, so I thought of implementing a scheduler which will run at specific intervals and checks if the files are expired and deletes them from database and disk.

**Rest API's:**

*File upload*

    POST /files

The API validates the requests against valid sources, emptyfile, filesize, etc.. and process the file for uploading. It first checks that the file already exists for the given filename, source, employeeCreatorId and if not then it wrties the file metadata to database and stores the file in the disk.

It's a multipart/form-data request with the following form fields:

    - meta # JSON of additional meta. Example: {"creatorEmployeeId": 1}
    - source # timesheet, mss, hrb, ...
    - expireTime # optional
    - content # file content

Iam getting the name and contentType from the file uploaded.

Sample response:

    > HTTP 201
    {
        "data": {
            "token": "6701d36f-ac24-4cd2-92d6-f7704e810352"
        },
        "errors": null,
        "status": 200
    }

*GET file metdata endpoint*

    POST /files/metas
	
	The Get File Metadata api retrieves the file metadata for the input tokens and returns it to the client.

Sample Request:
    > HTTP 200
    {
        "tokens":
            [
            "   454c36e2-2bb1-49b9-84eb-0a1de6d5a58b"
            ]
    }

Sample Response;

    >HTTP 200
    {
    "data": [
        {
            "token": "18f5f7c1-4980-418b-9bb1-8024b2ad20a0",
            "size": 88241,
            "source": "HRB",
            "meta": {
                "creatorEmployeeId": "jaii"
            },
            "expireTime": "2022-05-29T15:00:35.000+00:00",
            "fileName": "jaii_sony_bill.pdf",
            "contentType": "application/pdf",
            "createTime": "2022-04-04T11:14:39.579+00:00"
        }
    ],
    "errors": null,
    "status": 200
}

*File download endpoint*

    GET /file/{token}
	
	The File Download API checks if the file exists for the given token and if exists it returns the file in the body + additional headers

Sample Response:

    > HTTP 200
    X-Filename: "test.pdf"
    X-Filesize: "525"
    X-CreateTime: "2022-03-21T15:45:22Z"
    Content-Type: "application/pdf"

*File delete*

    DELETE /file/{token}

The File Delete API checks if the file exists for the given token and deletes the file from DB and Disk.
Sample Response:
    > HTTP 200
    {
        "data": true,
        "errors": null,
        "status": 200
    }

*Swagger* UI URL - http://localhost:6011/swagger-ui.html

**Scheduler:**

-   FileDeletionScheduler

The FileDeletionScheduler scheduler which will run at specific intervals(Example: Every one minute) and checks if the files are expired and deletes them from databse and disk.


## Which part of the assignment took the most time and why?
I was thinking that is an user is allowed to upload multiple files and if not then on what criteria i can check if a file already exists and what needs to be if file exists.
Then I made the application in way that an user is allowed is to upload one file for one specific source. So that if a user uploads same file for the same source then i am throwing file exists error. Also iam saving the file with the name as $username_$filename so that i will not conflict with while other users uploads. Thinking this part took some time.

And coding in Kotlin took some time and as the syntax and usability is different from Java.

## What You learned

I know some basics of kotlin and with this assignment i have learned more about Kotlin and it was great.

## TODOs

Creation of a signup and login api's and authorization using JWT token.
Adding more test cases for the app.
