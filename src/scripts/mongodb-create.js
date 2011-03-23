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
db.repositories_info.drop();
db.chililog_repository.drop();
db.test_repository.drop();


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
db.user_roles.ensureIndex({ username : 1 });
db.user_roles.ensureIndex({ role : 1 });
db.user_profiles.ensureIndex({ username : 1 });

print("\nCreating ChiliLog Admin User");
var adminUser = {
	username: "admin",
	password: "dJgFcagjd/IXet8RQ1ae9XkJLZ7bFLRkrsWBv+eGRmHvmqjeiX/U2RSPhyB0zosGH0cSONwQMvkhsxHjqhS2TrUAH1/CwSlp", // admin
	roles: [ "workbench.administrator" ],
	status: "Active",
	display_name: "Adminstrator",
	doc_version: new NumberLong(1) 
};
db.users.insert(adminUser);

// *************************************************************
// Setup Repositories
//*************************************************************
print("\nAdding ChiliLog Repository");
var chiliLogRepo = {
	name: "chililog",
	display_name: "ChiliLog Log",
	description: "Log repository for ChiliLog events",
	startup_status: 'ONLINE',
	is_read_queue_durable: false,
	is_write_queue_durable: false,
	write_queue_worker_count: new NumberLong(1),
	write_queue_max_memory: new NumberLong(1024 * 1024 * 20),
	write_queue_max_memory_policy: "PAGE",
	write_queue_page_size: new NumberLong(1024 * 1024 * 4),
	parsers: [
	    {
	    	name: "Default",
	    	applies_to_source: "All",
	    	applies_to_source_filter: "",
	    	applies_to_host: "None",
	    	applies_to_host_filter: "",
	    	class_name: "com.chililog.server.engine.parsers.DelimitedEntryParser",
	    	parse_field_error_handling: "SkipField",
	    	fields: [ 
	    	          { name: "timestamp", data_type: "Date", properties: { position: "1" } },
	    	          { name: "thread", data_type: "String", properties: { position: "2" } },
	    	          { name: "category", data_type: "String", properties: { position: "3" } },
	    	          { name: "message", data_type: "String", properties: { position: "4" } }
	    	],
	    	properties: { delimiter: "|" }
	    }
	],
	doc_version: new NumberLong(1)
};
db.repositories_info.insert(chiliLogRepo);

db.chililog_repository.ensureIndex({ entry_timestamp : 1, entry_keywords : 1 });


// *************************************************************
// Finish
//*************************************************************
print("\nFinish");

