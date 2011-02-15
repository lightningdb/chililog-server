What?
=====
ChiliLog is a real time log aggregation, analysis and monitoring tool.

* ChiliLog aggregates log feeds via JMS or STOMP, queues the feeds in [HornetQ](http://www.jboss.org/hornetq) before parsing and stored in [mongoDB](http://www.mongodb.org/).  The goal is to build agents for different platforms/frameworks/languages to feed it.

* ChiliLog allows you to "tail" aggregated logs in real time, or search for historical entries. Because your logs are parsed, you can filter and sort the log data.

* ChiliLog also monitors your logs for patterns that you define. If your pattern is matched, you will be notified.


Why?
====
I had a big problem the other day when my data centre provider claimed that I had exceeded by bandwidth allowance.

To find out what was causing my unusually high bandwidth usage, I had to aggregate logs from the firewall, load balancer, web server and app servers. 

Wow - what a pain in the arse.

The logs were in different formats, the timestamps were in different timezones and I had to wait 1 week before I got access to the firewall logs.

It was at this moment that the idea for an ChiliLog was born.  

I tried to find open source software but none did everything I wanted in one package.  Also, due to corporate policy, I could not use a cloud logging service.


When?
====

ChiliLog is in development and "un-released".

Unfortunately, ChiliLog is not my day job.

The current road map is:

* JAN 2011 - Integrate HornetQ and mongoDB, parse incoming logs and store to mongoDB. (Done)

* MAR 2011 - UI for configuration and log analysis. (In Progress)

* APR 2011 - Monitoring.

* MAY 2011 - Tentative alpha release.


Links
=====

* [Documentation](https://github.com/chililog/server/wiki) 

* [Issues](https://github.com/chililog/server/issues)


