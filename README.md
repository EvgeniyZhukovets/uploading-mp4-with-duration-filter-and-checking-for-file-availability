Uploading MP4 with duration filter and checking for file availability   
[![Build Status](https://travis-ci.com/EvgeniyZhukovets/Uploading-MP4-with-duration-filter-and-checking-for-file-availability.svg?branch=master)](https://travis-ci.com/EvgeniyZhukovets/Uploading-MP4-with-duration-filter-and-checking-for-file-availability)

##### Uploading steps:
  1. Gather MultipartFile
  2. Upload to temp folder
  3. Check uploaded video for duration
  4. Check uploaded video for availability
  5. Move file to destination folder
  6. Remove temp folder
  
##### Stack:
- Maven
- Spring Boot
- Log4J2
- Swagger
- Lombok
- IsoParser (org.mp4parser)

##### Building steps:
  1. Install Maven
  2. Run: mvn clean install
