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
	c_ver: new NumberLong(1) 
};
db.users.insert(adminUser);

var operatorUser = {
		username: "operator",
		password: "vcZ3/4Mw0bCAK+s3T8ROGAfv0XAPOP5M5XVT6NG6d0z/FqSmgIYqYoVteD8i1n8dj9BHPIqi/0HcgBLyl5MccodoAkvOVD2x", // YumCha
		roles: [ "repo.chililog.user" ],
		status: "Enabled",
		display_name: "Operator",
		email_address: "operator@chililog.com",
		c_ver: new NumberLong(1) 
	};
db.users.insert(operatorUser);
	
// *************************************************************
// Setup Repositories
//*************************************************************
print("\nAdding ChiliLog Repository");
var chiliLogRepo = {
	name: "chililog",
	display_name: "ChiliLog Log",
	description: "Log repository for ChiliLog events",
	startup_status: "ONLINE",
	is_read_queue_durable: false,
	is_write_queue_durable: false,
	write_queue_worker_count: new NumberLong(0),				// No workers because we write direct
	write_queue_max_memory: new NumberLong(1024 * 1024 * 1),	// 1MB
	write_queue_max_memory_policy: "DROP",
	write_queue_page_size: new NumberLong(1),
	write_queue_page_count_cache: new NumberLong(1),
	max_keywords: new NumberLong(-1),
	c_ver: new NumberLong(1)
};
db.repositories_info.insert(chiliLogRepo);

// http://www.mongodb.org/display/DOCS/Indexing+Advice+and+FAQ
db.chililog_repository.ensureIndex({ c_keywords : 1, c_ts : 1 }, {name: "keyword_ts_index"});


// *************************************************************
// Finish
//*************************************************************
print("\nFinish");

