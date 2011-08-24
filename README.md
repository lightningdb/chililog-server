Special Note for Sproutcore Competition
=======================================

The Sproutcore 2 source files are in [src/main/sc2](https://github.com/chililog/chililog-server/tree/master/src/main/sc2).

Sproutcore functionality used in the code include:

* Templates
* jQuery UI integration
* Data Store
* State Charts
* DateTime
* Bindings and custom binding transforms
* Strings

Other technical titbits:

* Using web sockets to stream live data to browser
* Client coded as static HTML that uses SC2 and jQuery Ajax to communicate with the server.
* Server coded in Java
* Server uses open source [MongoDB](http://www.mongodb.org/) to store data, [HornetQ](http://www.jboss.org/hornetq) for pubsub and [Netty](http://www.jboss.org/netty) as a web server.


__Demo Site__

A [demo site](http://demo.chililog.com/workbench) has been setup. You can login as admin/admin.

It will be done by (late) Sunday night ready for you to review Monday morning. This will save you time from installing and configuring everything.

If you do wish to download it and have a go, see instructions in the [wiki](https://github.com/chililog/chililog-server/wiki).


__Walk Through__

1. Open Safari version 5.0+ or Chrome versions 7 to 13 (not 14). Only these browsers support the correct version web sockets. Please make sure your firewall allows traffic through port 8989 for HTTP and port 61615 for HTTP web socket traffic.

2. Goto http://demo.chililog.com/workbench. 

3. Login as admin/admin

4. Click Stream from the menu at the top of the page.

   * Select the __Sandpit__ repository and and click Start.
   * You will now see live data being published from our logs. We've used web sockets to stream the data down.
   * Click __Send Test Log Entries__ to generate some of your own log messages
   * Click Stop to finish.

5. Click Search from the menu at the top of the page

   * Select the __Sandpit__ repository and and click Search.
   * You will now see historical data including your own test log messages previously generated in step #3.

Other pages have not been implemented. The code to perform CRUD operations with the server have been coded in app_datastore.js and app_engine.js. The HTML pages have not been done - ran out of time.


What?
=====
ChiliLog is a real time log aggregation, analysis and monitoring tool.

* ChiliLog aggregates your log entries using 'publishers'.  The goal is to create publishers that work on different platforms/frameworks/languages. Publishers transmits your log entries to the Chililog Server for storage.

* ChiliLog allows you to view aggregated logs in real time and also to search for historical entries. Because your logs are parsed, you can filter and sort the log data.

* ChiliLog can also monitors your logs for patterns that you define. If your pattern is matched, you will be notified.


Why?
====
I had a big problem the other day when my data centre provider claimed that I had exceeded by bandwidth allowance.

To find out what was causing my unusually high bandwidth usage, I had to aggregate logs from the firewall, load balancer, web server and app servers. 

Wow - what a pain in the arse!

The logs were in different formats, the timestamps were in different timezones and I had to wait 1 week before I got access to the firewall logs.

It was at this moment that the idea for ChiliLog was born.  

I tried to find open source software but I could not find one that did everything I needed in one package.  Due to corporate policy, I could not use a cloud logging service.


When?
====

ChiliLog is in development and "un-released".

Unfortunately, ChiliLog is not my day job so things are going to take a while.

The current road map is:

* JAN 2011 - Integrate HornetQ and mongoDB, parse incoming logs and store to mongoDB. (Done)

* MAY 2011 - Chiliog Log server without UI. (Done)

* JUL 2011 - Original release date postponed because I am not happy with UI in Sproutcore 1.0. Tables for displaying log entries are not work working very well. (Milestone missed).

* AUG 2011 - UI re-write in Sproutcore 2.0.

* OCT 2011 - Tentative alpha release.

* DEC 2011 - Monitoring.


Links
=====

* [Documentation](https://github.com/chililog/chililog-server/wiki) 

* [Issues](https://github.com/chililog/server/issues)


