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
// Strings table. By convention, all string  id have an underscore (_) prefix so that
// we know immediately if we display the code or the string.
//

SC.STRINGS = {
 // ********************************************************
  // General
  // ********************************************************
  '_add': 'Add',
  '_save': 'Save',
  '_ok': 'OK',
  '_apply': 'Apply',
  '_cancel': 'Cancel',
  '_refresh': 'Refresh',
  '_refreshing': 'Refreshing ...',
  '_delete': 'Delete',
  '_deleteSelected': 'Delete Selected',
  '_back': '\< Back',
  '_moreActions': 'More Actions',
  '_saveSuccess': 'Changes successfully saved',
  '_start': 'Start',
  '_starting': 'Starting ...',
  '_stop': 'Stop',
  '_stopping': 'Stopping ...',
  '_clear': 'Clear',
  '_search': 'Search',
  '_showMore': 'Show More',
  '_done': 'Done',
  '_next': '>',
  '_nextTooltip': 'Next',
  '_previous': '\<',
  '_previousTooltip': 'Previous',

  '_thousandSeparator': ',',

  '_testError': 'Test Error param1=%@, param2=%@, param3=%@',

  // ********************************************************
  // Engines
  // ********************************************************
  '_documentIDError': 'Expected document id "%@" but received "%@" from server.',

  '_sessionEngine.TokenNotFoundInResponseError': 'Token not found in authentication response',
  '_sessionEngine.VersionNotFoundInResponseError': 'Version number not found in authentication response',
  '_sessionEngine.BuildTimestampNotFoundInResponseError': 'Build timestamp not found in authentication response',

  // ********************************************************
  // Records
  // ********************************************************
  '_repositoryEntryRecord.Severity.Emergency': 'Emergency',
  '_repositoryEntryRecord.Severity.Action': 'Action',
  '_repositoryEntryRecord.Severity.Critical': 'Critical',
  '_repositoryEntryRecord.Severity.Error': 'Error',
  '_repositoryEntryRecord.Severity.Warning': 'Warning',
  '_repositoryEntryRecord.Severity.Notice': 'Notice',
  '_repositoryEntryRecord.Severity.Information': 'Information',
  '_repositoryEntryRecord.Severity.Debug': 'Debug',
  
  '_repositoryStatusRecord.Status.Online': 'Online',
  '_repositoryStatusRecord.Status.Offline': 'Offline',

  // ********************************************************
  // Login Page
  // ********************************************************
  '_login.username': 'Username',
  '_login.username.required': 'Username is required',
  '_login.password': 'Password',
  '_login.password.required': 'Password is required',
  '_login.rememberMe': 'Remember Me',
  '_login.login': 'Login',

  // ********************************************************
  // Stream Page
  // ********************************************************
  '_stream.repository': 'Repository',
  '_stream.severity': 'Severity',
  '_stream.test': 'Send Test Log Entries',

  // ********************************************************
  // Search Page
  // ********************************************************
  '_search.repository': 'Search In Repository',
  '_search.timespan': 'In The Past',
  '_search.timespan.5': '5 minutes',
  '_search.timespan.15': '15 minutes',
  '_search.timespan.30': '30 minutes',
  '_search.timespan.60': '60 minutes',
  '_search.timespan.1440': '24 hours',
  '_search.timespan.10080': '7 days',
  '_search.timespan.20160': '14 days',
  '_search.timespan.43200': '30 days',
  '_search.fromDate': 'From Date',
  '_search.fromDate.invalid': 'Cannot recognise From Date value "%@". Please use the format "yyyy-mm-dd".',
  '_search.fromTime': 'From Time',
  '_search.fromTime.invalid': 'Cannot recognise From Time time value "%@". Please use the format "hh:mm:ss".',
  '_search.toDate': 'To Date',
  '_search.toDate.invalid': 'Cannot recognise From Date value "%@". Please use the format "yyyy-mm-dd".',
  '_search.toTime': 'To Time',
  '_search.toTime.invalid': 'Cannot recognise From Time time value "%@". Please use the format "hh:mm:ss".',
  '_search.keywords': 'Keywords',
  '_search.timestamp': 'Timestamp',
  '_search.source': 'Source',
  '_search.host': 'Host',
  '_search.severity': 'Severity',
  '_search.message': 'Message',
  '_search.fields': 'Fields',
  '_search.savedTimestamp': 'Saved Timestamp',
  '_search.documentID': 'ID',
  '_search.advancedCriteria': 'Advanced Options',
  '_search.condition': 'Conditions',
  '_search.condition.help': '(mongodb JSON format must be used.)',
  '_search.condition.invalid': 'Conditions must be in valid JSON format. Check mongodb script for examples.',
  '_search.showMore': 'Show More Rows',
  '_search.timeSpecifiedError': 'You have specified "In The Past" and a specific date and time. Please specify one or the other but not both.',
  '_search.dateTimeRangeError': 'From date and time is greater than To date and time.',
  '_search.logEntries': 'Log Entries',
  '_search.noRowsFound': 'No log entries found matching your selection criteria',

  // ********************************************************
  // My Profile
  // ********************************************************
  '_myAccount.Title': 'My Account',
  '_myAccount.changeProfile': 'Change Profile',
  '_myAccount.username': 'Username',
  '_myAccount.emailAddress': 'Email Address',
  '_myAccount.emailAddress.invalid': 'Email Address is invalid',
  '_myAccount.emailAddress.required': 'Email Address is required',
  '_myAccount.displayName': 'Display Name',
  '_myAccount.changePassword': 'Change My Password',
  '_myAccount.oldPassword': 'Current Password',
  '_myAccount.oldPassword.invalid': 'Invalid password. Make sure that you have correctly entered your current password.',
  '_myAccount.oldPassword.required': 'Current Password is required.',
  '_myAccount.newPassword': 'New Password',
  '_myAccount.newPassword.invalid': 'You password must be at least 8 characters long.',
  '_myAccount.newPassword.required': 'New Password is required.',
  '_myAccount.confirmPassword': 'Confirm Password',
  '_myAccount.confirmPassword.help': 'Re-enter you new password to confirm that you have typed it correctly',
  '_myAccount.confirmPassword.invalid': 'New Password and Confirm Password are not the same.',
  '_myAccount.confirmPassword.required': 'Confirm New Password is required.',
  '_myAccount.changePassword.success': 'Password successfully changed',
  
  // ********************************************************
  // Admin
  // ********************************************************
  '_admin.repo.name': 'Repository Name',

  '_admin.user.username': 'Username',
  '_admin.user.username.help': 'Only lower case letters (a-z), digits (0-9) and underscore (_) characters allowed.',
  '_admin.user.username.invalid': 'Only lower case letters (a-z), digits (0-9) and underscore (_) characters allowed.',
  '_admin.user.username.required': 'Username is required.',
  '_admin.user.emailAddress': 'Email Address',
  '_admin.user.emailAddress.invalid': 'Email Address "%@" is invalid.',
  '_admin.user.emailAddress.required': 'Email Address is required.',
  '_admin.user.displayName': 'Display Name',
  '_admin.user.displayName.help': 'Name or nickname to display instead of the username',
  '_admin.user.currentStatus': 'Status',
  '_admin.user.currentStatus.enabled': 'Enabled - User can login',
  '_admin.user.currentStatus.disabled': 'Disabled - User cannot login',
  '_admin.user.currentStatus.locked': 'Locked - Password must be reset',
  '_admin.user.password': 'New Password',
  '_admin.user.password.invalid': 'You password must be at least 8 characters long.',
  '_admin.user.password.required': 'New Password is required.',
  '_admin.user.confirmPassword': 'Confirm Password',
  '_admin.user.confirmPassword.help': 'Re-enter you new password to confirm that you have typed it correctly',
  '_admin.user.confirmPassword.invalid': 'New Password and Confirm Password are not the same.',
  '_admin.user.confirmPassword.required': 'Confirm New Password is required.',
  '_admin.user.isSystemAdministrator': 'Is System Administrator?',
  '_admin.user.isSystemAdministrator.yes': 'Yes. - User will have access to all repositories and users.',
  '_admin.user.isSystemAdministrator.no': 'No.',
  '_admin.user.repositoryAccesses': 'Repository Access',
  '_admin.user.repositoryAccesses.repository': 'Repository',
  '_admin.user.repositoryAccesses.role': 'Role',
  '_admin.user.repositoryAccesses.adminRole': 'Administrator',
  '_admin.user.repositoryAccesses.workbenchRole': 'Workbench User',
  '_admin.user.repositoryAccesses.publisherRole': 'Publisher',
  '_admin.user.repositoryAccesses.subscriberRole': 'Subscriber',
  '_admin.user.repositoryAccesses.alreadyExists': 'Access to "%@" repository as "%@" has already been granted.',
  '_admin.user.noRowsFound': 'No users found matching your search criteria.',
  '_admin.user.create': 'New User ...',
  '_admin.user.createTitle': 'New User',
  '_admin.user.editTitle': 'User: %@',
  '_admin.user.generalAttributes': 'General',
  '_admin.user.rolesAttributes': 'Roles',
  '_admin.user.confirmDelete': 'Delete this user?',

  '_admin.repo.name': 'Name',
  '_admin.repo.name.help': 'Only lower case letters (a-z), digits (0-9) and underscore (_) characters allowed.',
  '_admin.repo.name.invalid': 'Only lower case letters (a-z), digits (0-9) and underscore (_) characters allowed.',
  '_admin.repo.name.required': 'Repository Name is required.',
  '_admin.repo.displayName': 'Display Name',
  '_admin.repo.description': 'Description',
  '_admin.repo.startupStatus': 'Startup Status',
  '_admin.repo.startupStatus.online': 'Online - start repository when server starts.',
  '_admin.repo.startupStatus.offline': 'Offline - you must manually start the repository when the server starts',
  '_admin.repo.currentStatus': 'Current Status',
  '_admin.repo.storeEntries' : 'Store Log Entries?',
  '_admin.repo.storeEntries.yes' : 'Yes - Save log entries to the database so they can be searched at a later time.</span>',
  '_admin.repo.storeEntries.no' : 'No  - Do not save log entries. Entries cannot be search.</span>',
  '_admin.repo.storageQueueDurable' : 'Storage Queue Durable?',
  '_admin.repo.storageQueueDurable.yes' : 'Yes - Queued log entries saved to disk to prevent loss of data if server goes down.</span>',
  '_admin.repo.storageQueueDurable.no' : 'No  - Queued log entries not saved to disk to improve throughput.</span>',
  '_admin.repo.storageQueueWorkerCount' : 'Worker Thread Count',
  '_admin.repo.storageQueueWorkerCount.help' : 'More workers means faster storage of entries but higher resource utilization.',
  '_admin.repo.storageQueueWorkerCount.required' : 'Worker Thread Count required.',
  '_admin.repo.storageMaxKeywords' : 'Maximum Keywords',
  '_admin.repo.storageMaxKeywords.help' : 'The more keywords, the more memory and storage is used.',
  '_admin.repo.storageMaxKeywords.required' : 'Maximum Keywords is required.',
  '_admin.repo.maxMemory' : 'Maximum Memory (bytes)',
  '_admin.repo.maxMemory.help' : 'Maximum amount of memory that will be used to queue log entries.',
  '_admin.repo.maxMemory.required' : 'Maximum Memory that will be used to queue log entries is required.',
  '_admin.repo.maxMemoryPolicy' : 'Maximum Memory Policy',
  '_admin.repo.maxMemoryPolicy.page' : 'Page  - New messages will be saved into page files.',
  '_admin.repo.maxMemoryPolicy.drop' : 'Drop  - New messages will be dropped and not processed.',
  '_admin.repo.maxMemoryPolicy.block' : 'Block - Force producers to wait before new messages can be sent.',
  '_admin.repo.pageSize' : 'Page File Size',
  '_admin.repo.pageSize.help' : 'The size of each page file. Only applicable for Paging Mode.',
  '_admin.repo.pageSize.required' : 'Page File Size is required.',
  '_admin.repo.pageSize.invalidSize' : 'Page File Size (%@) must be less than Maximum Memory (%@).',
  '_admin.repo.pageCountCache' : 'Page File Cache',
  '_admin.repo.pageCountCache.help' : 'Number of page files to keep in memory. The more files, the faster the performance.',
  '_admin.repo.pageCountCache.required' : 'Page File Cache is required.',
  '_admin.repo.confirmDelete': 'Delete repository "%@"?',
  '_admin.user.noRowsFound': 'No repositories found matching your search criteria.',
  '_admin.repo.createTitle': 'New Repository',
  '_admin.repo.editTitle': 'Repository Configuration',
  '_admin.repo.generalAttributes': 'General',
  '_admin.repo.pubSubAttributes': 'Publication & Subscription',
  '_admin.repo.storageAttributes': 'Storage',
  '_admin.repo.repositoryAccesses': 'Users',
  '_admin.repo.repositoryAccesses.label': 'Users who can access this repository',
  '_admin.repo.repositoryAccesses.username': 'Username',
  '_admin.repo.repositoryAccesses.userDisplayName': 'User Display Name',
  '_admin.repo.repositoryAccesses.role': 'Role',


  'end': 'end'
};