
What is ChiliLog?
=================
ChiliLog is a real time log aggregation, analysis and monitoring tool.

* ChiliLog aggregates your log entries using 'publishers'.  The goal is to create publishers that work on different platforms/frameworks/languages. Publishers transmits your log entries to the ChiliLog Server for storage.

* ChiliLog allows you to view aggregated logs in real time and also to search for historical entries. Because your logs are parsed, you can filter and sort the log data.

* ChiliLog can also monitors your logs for patterns that you define. If your pattern is matched, you will be notified.


Why?
====
I had a big problem the other day (back in 2010) when my data centre provider claimed that I had exceeded by bandwidth allowance.

To find out what was causing my unusually high bandwidth usage, I had to aggregate logs from the firewall, load balancer, web server and app servers. 

Wow - what a pain in the arse!

The logs were in different formats, the timestamps were in different timezones and I had to wait 1 week before I got access to the firewall logs.

I tried to find open source software but I could not find one that did everything I needed in one package.

It was at this moment that the idea for ChiliLog was born.  



Technical Titbits
=================

I've used ChiliLog to find out about newer technologies (well ... new as of 2010).   

* Browser based client coded as static HTML that uses Sproutcore2 and jQuery Ajax to communicate with the server.
* Server coded in Java and uses open source
    * [MongoDB](http://www.mongodb.org/) to store data
    * [HornetQ](http://www.jboss.org/hornetq) for pubsub, and
    * [Netty](http://www.jboss.org/netty) as a web server.
* Tools to help you publish (write) and subscribe (read) log entries from your apps and devices: 
    * [JavaScirpt](https://github.com/chililog/chililog-javascript-pubsub)
    * [Java](https://github.com/chililog/chililog-java-pubsub)
    * [.Net](https://github.com/chililog/chililog-dotnet-pubsub)



Demo Site
=========

Here are the [instructions](https://github.com/chililog/chililog-server/wiki/Demo) on how to access our live demo.

You'll create log entries from your browser and view them in real time. 



Roadmap
=======

ChiliLog is in development and "un-released".

Unfortunately, ChiliLog is not my day job so things are going to take a while.

The current road map is:

* JAN 2011 - Integrate HornetQ and mongoDB, parse incoming logs and store to mongoDB. (Done)

* MAY 2011 - Chiliog Log server without UI. (Done)

* JUL 2011 - Original release date postponed because I am not happy with UI in Sproutcore 1.0. Tables for displaying log entries are not work working very well. (Milestone missed).

* AUG 2011 - UI re-write in Sproutcore 2.0. (Done)

* OCT 2011 - Tentative alpha release.

* DEC 2011 - Monitoring.


Links
=====

* [Documentation](https://github.com/chililog/chililog-server/wiki) 

* [Issues](https://github.com/chililog/server/issues)



Special Note for Sproutcore Coders
==================================

The Sproutcore 2 source files are in [src/main/sc2](https://github.com/chililog/chililog-server/tree/master/src/main/sc2).

Sproutcore functionality used in the code include:

* Templates
* Data Store
* State Charts
* DateTime
* Bindings and custom binding transforms
* Strings Localization
* jQuery UI integration

I'll be blogging on how I've used these features soon. 
 
