//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.chililog.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

//
// This script helps to prepare a mongoDB database for ChiliLog use
//

// **********************************************
// Helper Functions
// **********************************************
function addConfig(configName, configValue) {
	print("\nAdding Config '" + configName + "'");
	db.config.update({ name : configName }, { $set : { value : configValue } },
			true);
	db.config.find({ name : configName }).forEach(printjson);
}

//*************************************************************
// Create our database
//*************************************************************
db = db.getSisterDB("chililog");

// Setup database user for driver authentication
print("\nAdding db.system.user 'chililog'");
if (db.system.users.find({ user : "chililog" }).count() == 0) {
	db.addUser("chililog", "chililog12");
}
db.system.users.find().forEach(printjson);


//*************************************************************
// Deleting collection to start a new
//*************************************************************
db.config.drop();
db.users.drop();
db.repo.drop();
db.repo_chililog.drop();
db.repo_sandpit.drop();


//*************************************************************
// Create default config collections
//*************************************************************
print("\nCreating Config collection");
addConfig("schemaversion", 1);
db.config.ensureIndex({ name : 1 });


//*************************************************************
// Create users
//*************************************************************
db.users.ensureIndex({ username : 1 });
db.users.ensureIndex({ roles : 1, username : 1 });

print("\nCreating ChiliLog Admin User");
var adminUser = {
	username: "admin",
	password: "dJgFcagjd/IXet8RQ1ae9XkJLZ7bFLRkrsWBv+eGRmHvmqjeiX/U2RSPhyB0zosGH0cSONwQMvkhsxHjqhS2TrUAH1/CwSlp", // admin
	roles: [ "system.administrator" ],
	status: "Enabled",
	display_name: "Adminstrator",
	email_address: "admin@chililog.com",
	doc_version: new NumberLong(1) 
};
db.users.insert(adminUser);

var sandpitRepositoryAdminUser = {
    username: "sandpitadmin",
    password: "vEoX9L0rx3Ta3NnVQr7n1dpnBzNnyma6xOTkqMb1P6o886xQmMQVzXPypet9mp1lv8ISfeEs8E/10BewZW9msqJHZTXya7f5", // sandpit
    roles: [ "repo.sandpit.administrator" ],
    status: "Enabled",
    display_name: "Sandpit Repository Administrator",
    email_address: "sandpitadmin@chililog.com",
    doc_version: new NumberLong(1) 
};
db.users.insert(sandpitRepositoryAdminUser);

var sandpitRepositoryWorkbenchUser = {
	username: "sandpitworkbench",
	password: "vEoX9L0rx3Ta3NnVQr7n1dpnBzNnyma6xOTkqMb1P6o886xQmMQVzXPypet9mp1lv8ISfeEs8E/10BewZW9msqJHZTXya7f5", // sandpit
	roles: [ "repo.sandpit.workbench" ],
	status: "Enabled",
	display_name: "Sandpit Repository Workbench User",
	email_address: "sandpitworkbenchuser@chililog.com",
	doc_version: new NumberLong(1) 
};
db.users.insert(sandpitRepositoryWorkbenchUser);

var sandpitRepositoryPublisherUser = {
	username: "sandpitpublisher",
	password: "vEoX9L0rx3Ta3NnVQr7n1dpnBzNnyma6xOTkqMb1P6o886xQmMQVzXPypet9mp1lv8ISfeEs8E/10BewZW9msqJHZTXya7f5", // sandpit
	roles: [ "repo.sandpit.publisher" ],
	status: "Enabled",
	display_name: "Sandpit Repository Publisher User",
	email_address: "sandpitpublisher@chililog.com",
	doc_version: new NumberLong(1) 
};
db.users.insert(sandpitRepositoryPublisherUser);

var sandpitRepositorySubscriberUser = {
	username: "sandpitsubscriber",
	password: "vEoX9L0rx3Ta3NnVQr7n1dpnBzNnyma6xOTkqMb1P6o886xQmMQVzXPypet9mp1lv8ISfeEs8E/10BewZW9msqJHZTXya7f5", // sandpit
	roles: [ "repo.sandpit.subscriber" ],
	status: "Enabled",
	display_name: "Sandpit Repository Subscriber User",
	email_address: "sandpitsubscriber@chililog.com",
	doc_version: new NumberLong(1) 
};
db.users.insert(sandpitRepositorySubscriberUser);

// *************************************************************
// Setup Repositories
// See http://www.mongodb.org/display/DOCS/Indexing+Advice+and+FAQ
//*************************************************************
print("\nAdding ChiliLog Repository");
var chililogRepoConfig = {
	name: "chililog",
	display_name: "Chililog Server",
	description: "Log repository for Chililog Server events",
	startup_status: "ONLINE",
	store_entries_indicator: true,
	storage_queue_durable_indicator: false,
	storage_queue_worker_count: new NumberLong(0),  // No workers because we write direct
	storage_max_keywords: new NumberLong(50),
	max_memory: new NumberLong(1024 * 1024 * 1),	// 1MB
	max_memory_policy: "DROP",
	page_size: new NumberLong(1),
	page_count_cache: new NumberLong(1),
	doc_version: new NumberLong(1)
};
db.repo.insert(chililogRepoConfig);
db.repo_chililog.ensureIndex({ keywords : 1, ts : 1 }, {name: "keyword_ts_index"});

print("\nAdding Sandpit Repository");
var sandpitRepoConfig = {
	name: "sandpit",
	display_name: "Sandpit",
	description: "For testing and playing around",
	startup_status: "ONLINE",
	store_entries_indicator: true,
	storage_queue_durable_indicator: false,
	storage_queue_worker_count: new NumberLong(1),
	storage_max_keywords: new NumberLong(50),
	max_memory: new NumberLong(1024 * 1024 * 20),	// 20MB
	max_memory_policy: "PAGE",
	page_size: new NumberLong(1024 * 1024 * 10),	// 10 MB
	page_count_cache: new NumberLong(3),
	doc_version: new NumberLong(1)
};
db.repo.insert(sandpitRepoConfig);
db.repo_sandpit.ensureIndex({ keywords : 1, ts : 1 }, {name: "keyword_ts_index"});

// *************************************************************
// Finish
//*************************************************************
print("\nFinish");

