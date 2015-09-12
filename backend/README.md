# Pidifrky Backend

The backend it built on top of [Play framework](https://www.playframework.com/). The build system is SBT.  
You can run the application by running `sbt run` in the directory with build.sbt.  
The application will fail until you provider it a configuration file - application.conf into server/conf directory. The file is not committed because it's supposed to contain
deploy-specific and potentially secret data, like usernames and passwords.

## Device endpoints
All endpoints takes `DeviceBackend.Envelope` as root GPB. Messages specified below are content of the envelope.
### Debug reporting
**URL:** `/debugReport`
**Request:** `DebugReportRequest`